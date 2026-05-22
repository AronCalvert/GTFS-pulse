// ── Map setup ──
const map = L.map('map', { zoomControl: true, attributionControl: false })
  .setView([53.3498, -6.2603], 11);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19
}).addTo(map);

const MIN_ZOOM = 10;
const STOP_ZOOM = 14;
const POLL_INTERVAL_MS = 10000;

// ── Marker pool — stores { marker, lastSeen, startLatLng, targetLatLng, startTime, stopped } per tripId ──
const markers = {};
const renderer = L.canvas();
const STALE_MS = 10 * 60 * 1000;

// ── Stops ──
let allStopsData = [];
const stopMarkers = {};

async function fetchStops() {
  try {
    const res = await fetch('/api/v1/stops');
    allStopsData = await res.json();
    renderStops();
  } catch (e) {
    console.warn('Failed to load stops:', e);
  }
}

function renderStops() {
  const zoom = map.getZoom();
  const statEl = document.getElementById('stat-stops');

  if (zoom < STOP_ZOOM) {
    Object.values(stopMarkers).forEach(m => map.removeLayer(m));
    Object.keys(stopMarkers).forEach(id => delete stopMarkers[id]);
    statEl.textContent = '--';
    return;
  }

  const bounds = map.getBounds().pad(0.15);
  const inBounds = new Set();

  allStopsData.forEach(({ stopId, stopName, stopCode, stopLat: lat, stopLon: lng }) => {
    if (!bounds.contains([lat, lng])) return;
    inBounds.add(stopId);
    if (stopMarkers[stopId]) return;

    const marker = L.circleMarker([lat, lng], {
      renderer,
      radius: 6,
      color: '#ff6b35',
      fillColor: '#ff6b35',
      fillOpacity: 1,
      weight: 2,
    }).bindPopup(
      `<div class="stop-popup"><div class="stop-name">${stopName}</div><div class="stop-meta">${stopCode || stopId}</div></div>`,
      { maxWidth: 200 }
    );

    marker.on('click', async () => {
      marker.bindPopup(
        `<div class="stop-popup"><div class="stop-name">${stopName}</div><div class="stop-meta">Loading arrivals...</div></div>`,
        { maxWidth: 260 }
      ).openPopup();
      try {
        const res = await fetch(`/api/v1/stops/${stopId}/arrivals`);
        const arrivals = await res.json();
        const nowSecs = new Date().getHours() * 3600 + new Date().getMinutes() * 60 + new Date().getSeconds();
        const upcoming = arrivals.filter(a => a.arrivalTime >= nowSecs).slice(0, 8);
        const rows = upcoming.length > 0
          ? upcoming.map(a =>
              `<div class="arrival-row">
                <span class="arrival-route">${a.routeShortName}</span>
                <span class="arrival-headsign">${a.headsign || ''}</span>
                <span class="arrival-time">${secsToTime(a.arrivalTime)}</span>
              </div>`
            ).join('')
          : '<div class="stop-meta" style="padding:6px 0">No upcoming arrivals</div>';
        marker.setPopupContent(
          `<div class="stop-popup">
            <div class="stop-name">${stopName}</div>
            <div class="stop-meta" style="margin-bottom:8px">SCHEDULED ARRIVALS</div>
            ${rows}
          </div>`
        );
      } catch {
        marker.setPopupContent(
          `<div class="stop-popup"><div class="stop-name">${stopName}</div><div class="stop-meta">No data available</div></div>`
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

  statEl.textContent = inBounds.size.toLocaleString();
}

map.on('moveend', renderStops);
fetchStops();

map.on('zoomend', () => {
  const visible = map.getZoom() >= MIN_ZOOM;
  Object.values(markers).forEach(({ marker }) => visible ? marker.addTo(map) : map.removeLayer(marker));
  renderStops();
});

// Sweep stale markers every 60 seconds
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

// ── Animation loop ──
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

// ── Helpers ──
function secsToTime(secs) {
  const h = Math.floor(secs / 3600) % 24;
  const m = Math.floor((secs % 3600) / 60);
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

function stopName(stopId) {
  const stop = allStopsData.find(s => s.stopId === stopId);
  return stop ? stop.stopName : stopId;
}

// ── Toast ──
function showToast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 2500);
}

// ── WebSocket / STOMP ──
const RECONNECT_DELAY_MS = 5000;

function connect() {
  const socket = new SockJS('http://localhost:8080/api/v1/ws');
  const stomp  = Stomp.over(socket);
  stomp.debug  = null;

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
          const marker = L.circleMarker([lat, lng], {
            renderer,
            radius: 5,
            color: '#00e5ff',
            fillColor: '#00e5ff',
            fillOpacity: 0.9,
            weight: 0
          });

          marker.on('click', async () => {
            marker.bindPopup(
              `<div class="stop-popup"><div class="stop-name">Loading...</div></div>`,
              { maxWidth: 220 }
            ).openPopup();
            try {
              const [nextRes, routeRes] = await Promise.all([
                fetch(`/api/v1/vehicle/${tripId}/stops/next`),
                fetch(`/api/v1/routes/${routeId}`)
              ]);
              const next = await nextRes.json();
              const route = await routeRes.json();
              marker.setPopupContent(
                `<div class="stop-popup">
                  <div class="stop-name">Route ${route.routeShortName}</div>
                  <div class="stop-meta" style="margin-bottom:6px">${route.routeLongName}</div>
                  <div class="stop-meta">NEXT STOP</div>
                  <div class="stop-name">${stopName(next.stopId)}</div>
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
