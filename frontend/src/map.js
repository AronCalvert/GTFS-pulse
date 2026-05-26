import { apiFetch } from './api.js';
import { isLoggedIn } from './auth.js';
import { getFavouritedStopIds, addFavouriteStop, setStopsCache, renderWidget } from './favourites.js';
import { initSearch } from './search.js';

const map = L.map('map', { zoomControl: true, attributionControl: false })
  .setView([53.3498, -6.2603], 11);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
}).addTo(map);

const MIN_ZOOM = 10;
const STOP_ZOOM = 14;
const POLL_INTERVAL_MS = 10000;

const markers = {};
const renderer = L.canvas();
const STALE_MS = 2 * 60 * 1000;

let allStopsData = [];
const stopMarkers = {};
const routeTypeMap = {};

const VEHICLE_STYLES = {
  0: { color: '#f97316', radius: 5 },  // luas
  2: { color: '#00ff87', radius: 6 },  // rail
  3: { color: '#00e5ff', radius: 5 },  // busses
};
const DEFAULT_VEHICLE_STYLE = VEHICLE_STYLES[3];


function epochToTime(epoch) {
  const d = new Date(epoch * 1000);
  return d.toLocaleTimeString('en-IE', { hour: '2-digit', minute: '2-digit', hour12: false });
}

function secsToTime(secs) {
  const h = Math.floor(secs / 3600) % 24;
  const m = Math.floor((secs % 3600) / 60);
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

function lookupStopName(stopId) {
  const stop = allStopsData.find(s => s.stopId === stopId);
  return stop ? stop.stopName : stopId;
}

window._toggleFavourite = async (stopId, btn) => {
  try {
    await addFavouriteStop(stopId);
    btn.textContent = '★ Saved';
    btn.classList.add('saved');
  } catch (e) {
    console.warn('Failed to save favourite:', e);
  }
};


async function fetchRouteTypes() {
  try {
    const routes = await apiFetch('/routes');
    routes.forEach(r => { routeTypeMap[r.routeId] = r.routeType; });
  } catch (e) {
    console.warn('Failed to load routes:', e);
  }
}

async function fetchStops() {
  try {
    allStopsData = await apiFetch('/stops');
    setStopsCache(allStopsData);
    renderWidget();
    renderStops();
    initSearch(map, allStopsData, stopMarkers, renderStops);
  } catch (e) {
    console.warn('Failed to load stops:', e);
  }
}

async function buildArrivalsPopupContent(stopId, name) {
  const arrivals = await apiFetch(`/stops/${stopId}/arrivals`);
  const nowEpoch = Math.floor(Date.now() / 1000);
  const upcoming = arrivals.filter(a => a.arrivalTime >= nowEpoch).slice(0, 8);

  const isFav = isLoggedIn() && getFavouritedStopIds().has(stopId);
  const saveBtn = isLoggedIn()
    ? `<button class="save-btn${isFav ? ' saved' : ''}" onclick="window._toggleFavourite('${stopId}', this)">${isFav ? '★ Saved' : '☆ Save'}</button>`
    : '';

  const rows = upcoming.length > 0
    ? upcoming.map(a =>
        `<div class="arrival-row">
          <span class="arrival-route">${a.routeShortName}</span>
          <span class="arrival-headsign">${a.headsign || ''}</span>
          <span class="arrival-time">${epochToTime(a.arrivalTime)}</span>
        </div>`
      ).join('')
    : '<div class="stop-meta" style="padding:6px 0">No upcoming arrivals</div>';

  return `<div class="stop-popup">
    <div class="stop-popup-header">
      <div class="stop-name">${name}</div>
      ${saveBtn}
    </div>
    <div class="stop-meta" style="margin-bottom:8px">ARRIVALS</div>
    ${rows}
  </div>`;
}

function renderStops() {
  const zoom = map.getZoom();

  if (zoom < STOP_ZOOM) {
    Object.values(stopMarkers).forEach(m => map.removeLayer(m));
    Object.keys(stopMarkers).forEach(id => delete stopMarkers[id]);
    return;
  }

  const bounds = map.getBounds().pad(0.15);
  const inBounds = new Set();

  allStopsData.forEach(({ stopId, stopName: name, stopCode, stopLat: lat, stopLon: lng }) => {
    if (!bounds.contains([lat, lng])) return;
    inBounds.add(stopId);
    if (stopMarkers[stopId]) return;

    const stopIcon = L.divIcon({
      className: '',
      html: '<div class="stop-marker"></div>',
      iconSize: [8, 8],
      iconAnchor: [4, 4],
      popupAnchor: [0, -6],
    });

    const marker = L.marker([lat, lng], { icon: stopIcon }).bindPopup(
      `<div class="stop-popup"><div class="stop-name">${name}</div><div class="stop-meta">${stopCode || stopId}</div></div>`,
      { maxWidth: 260 }
    );

    marker.on('click', async () => {
      marker.setPopupContent(
        `<div class="stop-popup"><div class="stop-name">${name}</div><div class="stop-meta">Loading arrivals...</div></div>`
      );
      marker.openPopup();
      try {
        marker.setPopupContent(await buildArrivalsPopupContent(stopId, name));
      } catch {
        marker.setPopupContent(
          `<div class="stop-popup"><div class="stop-name">${name}</div><div class="stop-meta">No data available</div></div>`
        );
      }
    });

    marker.addTo(map);
    stopMarkers[stopId] = marker;
  });

  Object.keys(stopMarkers).forEach(id => {
    if (!inBounds.has(id)) {
      map.removeLayer(stopMarkers[id]);
      delete stopMarkers[id];
    }
  });

}

map.on('moveend', renderStops);
fetchStops();
fetchRouteTypes();

map.on('zoomend', () => {
  const visible = map.getZoom() >= MIN_ZOOM;
  Object.values(markers).forEach(({ marker }) => visible ? marker.addTo(map) : map.removeLayer(marker));
  renderStops();
});

setInterval(() => {
  const now = Date.now();
  Object.keys(markers).forEach(id => {
    if (now - markers[id].lastSeen > STALE_MS) {
      map.removeLayer(markers[id].marker);
      delete markers[id];
    }
  });
  document.getElementById('stat-vehicles').textContent = Object.keys(markers).length.toLocaleString();
}, 60000);


function animate() {
  const now = Date.now();
  if (map.getZoom() >= MIN_ZOOM) {
    const bounds = map.getBounds().pad(0.1);
    Object.values(markers).forEach(entry => {
      if (entry.stopped) return;
      if (!bounds.contains(entry.targetLatLng)) return;
      const t = Math.min((now - entry.startTime) / POLL_INTERVAL_MS, 1);
      entry.marker.setLatLng([
        entry.startLatLng[0] + (entry.targetLatLng[0] - entry.startLatLng[0]) * t,
        entry.startLatLng[1] + (entry.targetLatLng[1] - entry.startLatLng[1]) * t,
      ]);
    });
  }
  requestAnimationFrame(animate);
}
animate();


function showToast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 2500);
}


document.addEventListener('auth:logout', () => showToast('Session expired — please log in again'));


document.addEventListener('favourites:flyto', ({ detail: { stopId, latlng } }) => {
  map.flyTo(latlng, 16);
  map.once('moveend', () => {
    const marker = stopMarkers[stopId];
    if (marker) marker.fire('click');
  });
});


const RECONNECT_DELAY_MS = 5000;

function connect() {
  const socket = new SockJS('http://localhost:8080/api/v1/ws');
  const stomp = Stomp.over(socket);
  stomp.debug = null;

  stomp.connect({}, () => {
    document.getElementById('dot').className = 'dot live';
    document.getElementById('status-text').textContent = 'LIVE';
    showToast('Connected to GTFS Pulse');

    stomp.subscribe('/topic/vehicles', (message) => {
      const fleet = JSON.parse(message.body);
      const now = Date.now();

      fleet.forEach(vehicle => {
        const { tripId, routeId, latitude: lat, longitude: lng, currentStatus } = vehicle;
        if (lat == null || lng == null) return;

        const stopped = currentStatus === 'STOPPED_AT';
        const entry = markers[tripId];

        if (entry) {
          const current = entry.marker.getLatLng();
          entry.startLatLng  = [current.lat, current.lng];
          entry.targetLatLng = [lat, lng];
          entry.startTime    = now;
          entry.stopped      = stopped;
          entry.lastSeen     = now;
          if (stopped) entry.marker.setLatLng([lat, lng]);
        } else {
          const { color, radius } = VEHICLE_STYLES[routeTypeMap[routeId]] ?? DEFAULT_VEHICLE_STYLE;
          const marker = L.circleMarker([lat, lng], {
            renderer,
            radius,
            color,
            fillColor: color,
            fillOpacity: 0.9,
            weight: 0,
          });

          marker.on('click', async () => {
            marker.bindPopup(
              `<div class="stop-popup"><div class="stop-name">Loading...</div></div>`,
              { maxWidth: 220 }
            ).openPopup();
            try {
              const [next, route] = await Promise.all([
                apiFetch(`/vehicle/${tripId}/stops/next`),
                apiFetch(`/routes/${routeId}`),
              ]);
              marker.setPopupContent(
                `<div class="stop-popup">
                  <div class="stop-name">Route ${route.routeShortName}</div>
                  <div class="stop-meta" style="margin-bottom:6px">${route.routeLongName}</div>
                  <div class="stop-meta">NEXT STOP</div>
                  <div class="stop-name">${lookupStopName(next.stopId)}</div>
                  <div class="stop-meta">${secsToTime(next.arrivalTime)}</div>
                </div>`
              );
            } catch {
              marker.setPopupContent(
                `<div class="stop-popup"><div class="stop-meta">No data available</div></div>`
              );
            }
          });

          if (map.getZoom() >= MIN_ZOOM) marker.addTo(map);
          markers[tripId] = {
            marker,
            lastSeen:     now,
            startLatLng:  [lat, lng],
            targetLatLng: [lat, lng],
            startTime:    now,
            stopped,
          };
        }
      });

      document.getElementById('stat-time').textContent = new Date().toTimeString().slice(0, 8);
      document.getElementById('stat-vehicles').textContent = Object.keys(markers).length.toLocaleString();
    });

  }, () => {
    document.getElementById('dot').className = 'dot error';
    document.getElementById('status-text').textContent = 'DISCONNECTED';
    showToast('Connection lost — retrying...');
    setTimeout(connect, RECONNECT_DELAY_MS);
  });
}

connect();
