import { apiFetch, getToken } from './api.js';

let favourites = [];
let stopsCache = [];

export function setStopsCache(stops) {
  stopsCache = stops;
}

export function getFavouritedStopIds() {
  return new Set(favourites.map(f => f.stopId));
}

function getStopName(stopId) {
  const stop = stopsCache.find(s => s.stopId === stopId);
  return stop ? stop.stopName : stopId;
}

function getStopLatLng(stopId) {
  const stop = stopsCache.find(s => s.stopId === stopId);
  return stop ? [stop.stopLat, stop.stopLon] : null;
}

export async function loadFavourites() {
  if (!getToken()) return;
  try {
    favourites = await apiFetch('/favourite/all');
    renderWidget();
  } catch {
    favourites = [];
  }
}

export async function addFavouriteStop(stopId) {
  await apiFetch('/favourite/set', {
    method: 'POST',
    body: JSON.stringify({ stopId, routeId: null }),
  });
  await loadFavourites();
}

async function removeFavourite(id) {
  await apiFetch(`/favourite/${id}`, { method: 'DELETE' });
  favourites = favourites.filter(f => f.Id !== id);
  renderWidget();
}

export function renderWidget() {
  const list = document.getElementById('favourites-list');
  if (favourites.length === 0) {
    list.innerHTML = '<li class="favourites-empty">No favourites yet</li>';
    return;
  }
  list.innerHTML = favourites.map(f => `
    <li class="favourites-item" data-stop-id="${f.stopId}" data-id="${f.Id}">
      <span class="favourites-dot">●</span>
      <span class="favourites-name">${getStopName(f.stopId)}</span>
      <button class="favourites-remove" data-id="${f.Id}">✕</button>
    </li>
  `).join('');

  list.querySelectorAll('.favourites-item').forEach(item => {
    item.addEventListener('click', (e) => {
      if (e.target.classList.contains('favourites-remove')) return;
      const stopId = item.dataset.stopId;
      const latlng = getStopLatLng(stopId);
      if (latlng) {
        document.dispatchEvent(new CustomEvent('favourites:flyto', { detail: { stopId, latlng } }));
      }
    });
  });

  list.querySelectorAll('.favourites-remove').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.stopPropagation();
      await removeFavourite(Number(btn.dataset.id));
    });
  });
}

export function initFavourites() {
  document.addEventListener('auth:login', async () => {
    document.getElementById('favourites-widget').style.display = 'block';
    await loadFavourites();
  });

  document.addEventListener('auth:logout', () => {
    document.getElementById('favourites-widget').style.display = 'none';
    favourites = [];
    renderWidget();
  });

  if (getToken()) {
    document.getElementById('favourites-widget').style.display = 'block';
    loadFavourites();
  }
}
