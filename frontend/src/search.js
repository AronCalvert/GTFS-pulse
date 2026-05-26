export function initSearch(map, allStopsData, stopMarkers, renderStops) {
  const input = document.getElementById('search-input');
  const results = document.getElementById('search-results');

  function flyToStop(stop) {
    map.flyTo([stop.stopLat, stop.stopLon], 16);
    input.value = '';
    results.classList.remove('open');

    map.once('moveend', () => {
      const marker = stopMarkers[stop.stopId];
      if (marker) marker.fire('click');
    });
  }

  function renderResults(matches) {
    results.innerHTML = '';

    if (matches.length === 0) {
      results.innerHTML = '<li class="search-result-empty">No stops found</li>';
      results.classList.add('open');
      return;
    }

    matches.forEach(stop => {
      const li = document.createElement('li');
      li.className = 'search-result-item';
      li.innerHTML = `
        <div class="search-result-name">${stop.stopName}</div>
        <div class="search-result-code">${stop.stopCode || stop.stopId}</div>
      `;
      li.addEventListener('click', () => flyToStop(stop));
      results.appendChild(li);
    });

    results.classList.add('open');
  }

  input.addEventListener('input', () => {
    const query = input.value.trim().toLowerCase();

    if (!query) {
      results.classList.remove('open');
      return;
    }

    const matches = allStopsData
      .filter(s => s.stopName?.toLowerCase().includes(query) || s.stopCode?.toLowerCase().includes(query))
      .slice(0, 10);

    renderResults(matches);
  });

  document.addEventListener('click', e => {
    if (!document.getElementById('search-widget').contains(e.target)) {
      results.classList.remove('open');
    }
  });
}
