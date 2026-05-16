// ── Map setup ──
const map = L.map('map', { zoomControl: true, attributionControl: false })
  .setView([53.3498, -6.2603], 11);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19
}).addTo(map);

const MIN_ZOOM = 10;

// ── Marker pool — stores { marker, lastSeen } per tripId ──
const markers = {};
const renderer = L.canvas();
const STALE_MS = 10 * 60 * 1000;

map.on('zoomend', () => {
  const visible = map.getZoom() >= MIN_ZOOM;
  Object.values(markers).forEach(({ marker }) => visible ? marker.addTo(map) : map.removeLayer(marker));
});

// Sweep stale markers every 10 seconds
setInterval(() => {
  const now = Date.now();
  Object.keys(markers).forEach(id => {
    if (now - markers[id].lastSeen > STALE_MS) {
      map.removeLayer(markers[id].marker);
      delete markers[id];
    }
  });
  document.getElementById('stat-buses').textContent = Object.keys(markers).length.toLocaleString();
}, 60000);

// ── Stats ──
let updateCount = 0;
let lastRateCheck = Date.now();

setInterval(() => {
  const now = Date.now();
  const elapsed = (now - lastRateCheck) / 1000;
  document.getElementById('stat-rate').textContent = elapsed > 0 ? (updateCount / elapsed).toFixed(1) : '0';
  updateCount = 0;
  lastRateCheck = now;
}, 1000);

// ── Toast ──
function showToast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 2500);
}

// ── WebSocket / STOMP ──
const socket = new SockJS('http://localhost:8080/ws');
const stomp  = Stomp.over(socket);
stomp.debug  = null;

stomp.connect({}, () => {
  document.getElementById('dot').className = 'dot live';
  document.getElementById('status-text').textContent = 'LIVE';
  showToast('Connected to GTFS Pulse');

  stomp.subscribe('/topic/buses', (message) => {
    const fleet = JSON.parse(message.body);
    updateCount++;
    const now = Date.now();

    fleet.forEach(bus => {
      const { tripId, routeId, latitude: lat, longitude: lng } = bus;
      if (lat == null || lng == null) return;

      if (markers[tripId]) {
        markers[tripId].marker.setLatLng([lat, lng]);
        markers[tripId].lastSeen = now;
      } else {
        const marker = L.circleMarker([lat, lng], {
          renderer,
          radius: 3,
          color: '#00e5ff',
          fillColor: '#00e5ff',
          fillOpacity: 0.9,
          weight: 0
        }).bindPopup(`<b>Route ${routeId}</b><br>Trip: ${tripId}`);
        if (map.getZoom() >= MIN_ZOOM) marker.addTo(map);
        markers[tripId] = { marker, lastSeen: now };
      }
    });
    document.getElementById('stat-time').textContent = new Date().toTimeString().slice(0, 8);
    document.getElementById('stat-buses').textContent = Object.keys(markers).length.toLocaleString();
  });

}, () => {
  document.getElementById('dot').className = 'dot error';
  document.getElementById('status-text').textContent = 'DISCONNECTED';
  showToast('Connection lost — retrying...');
});
