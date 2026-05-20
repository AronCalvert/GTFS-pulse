# GTFS Pulse — API Reference

Base URL: `/api/v1`

---

## Bus `/bus`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/bus/update` | Receive fleet update from Python simulator |
| `GET` | `/bus/{tripId}` | Get current position of a specific bus |
| `GET` | `/bus/{tripId}/stops` | All stops for a trip in sequence order |
| `GET` | `/bus/{tripId}/stops/passed` | Stops already passed by this bus |
| `GET` | `/bus/{tripId}/stops/next` | Next upcoming stop for this bus |

---

## Stops `/stops`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/stops/{stopId}` | Get stop info by ID |
| `GET` | `/stops/{stopId}/arrivals` | Upcoming arrivals at a stop |

---

## Routes `/routes`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/routes/{routeId}` | Get route info |
| `GET` | `/routes/{routeId}/trips` | All trips for a route |

---

## Admin `/admin`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/admin/reload-gtfs` | Trigger GTFS static data reload |

---

## Notes

- All timestamps are in seconds since midnight (GTFS format)
- Real-time bus positions are pushed via WebSocket to `/topic/buses`
- Static GTFS data is served from PostgreSQL
- Live bus state is held in-memory via `ConcurrentHashMap`
