# GTFS Pulse

A real-time public transit tracking application for Ireland's bus, rail, and tram network. Vehicle positions and arrival predictions update live via WebSocket, built on a multi-service event-driven architecture.

[![GTFS Pulse Demo](https://img.youtube.com/vi/0tKUQoa6Occ/maxresdefault.jpg)](https://www.youtube.com/watch?v=0tKUQoa6Occ)

## Overview

GTFS Pulse ingests GTFS-Realtime feed (vehicle positions and trip updates), processes it through a Kafka, caches the vehicle live positions in Redis, and pushes it to connected browsers over WebSocket. Static schedule data (stops, routes, trips, calendars) is loaded from GTFS into PostgreSQL and used to resolve scheduled arrival times, with real-time delays also done where possible.

## Architecture

```
┌─────────────┐     protobuf/HTTP      ┌──────────────┐
│  Simulator  │ ─────────────────────► │   Ingestor   │
│  (Python)   │   every 10 seconds     │ (Spring Boot)│
└─────────────┘                        └──────┬───────┘
                                              │ Kafka
                                    ┌─────────▼────────┐
                                    │  vehicle-positions│
                                    │  trip-updates     │
                                    └─────────┬─────────┘
                                              │
                                    ┌─────────▼────────┐
                                    │     Backend       │
                                    │  (Spring Boot)    │
                                    │                   │
                                    │  Kafka Consumer   │
                                    │  → Redis cache    │
                                    │  WebSocket broker │
                                    │  REST API         │
                                    └─────────┬─────────┘
                                              │ SockJS/STOMP
                                    ┌─────────▼────────┐
                                    │    Frontend       │
                                    │  (Vite + Leaflet) │
                                    └──────────────────┘
```

**Simulator** — Python/Flask service that plays Ireland's GTFS static data as a live GTFS-Realtime feed, simulating vehicle movement between stops. Serves vehicle positions and trip updates as protobuf over HTTP.

**Ingestor** — Spring Boot service that polls the simulator every 10 seconds, parses the protobuf feed, and publishes vehicle positions and trip updates to separate Kafka topics. The system is designed to be feed agnostic. So the Simulator should be able to be swapped out for a real GTFS-Realtime feed without too many changes.

**Backend** — Spring Boot service with three responsibilities: 1. consuming Kafka messages and writing them to Redis 2. serving a REST API for stops, routes, and arrival predictions 3. broadcasting live vehicle positions to connected clients over WebSocket (SockJS/STOMP).

**Frontend** — JS single-page app using Leaflet for the map. Connects to the WebSocket for live vehicle positions and queries the REST API for stop arrivals on demand.

## features

- live vehicle positions with smooth animation interpolated between poll intervals
- stop arrival predictions merging scheduled times (from PostgreSQL) with real-time delays (from Redis)
- calendar-aware scheduling, correctly filters active services by day of week and exception dates
- stop search, client-side filtering across all stops with fly-to and auto-popup on result click
- user accounts with JWT authentication, register, login and session expiry handling
- favourites, save stops, fly to them from the sidebar widget
- vehicles not seen in the last 2 minutes are removed from the map

## tech stack

| Layer | Technology |
|---|---|
| Frontend | Vanilla JS, Vite, Leaflet, SockJS, STOMP |
| Backend | Java 21, Spring Boot 4, Spring Security, Spring WebSocket |
| Messaging | Apache Kafka |
| Cache | Redis |
| Database | PostgreSQL, Spring Data JPA |
| Simulator | Python, Flask, GTFS-Realtime protobuf |
| Infrastructure | Docker Compose |

## Running locally

**Prerequisites:** Docker

**1. Download the GTFS static data**

The `./data` directory is not included in the repository. Fetch the public Transport for Ireland feed before starting:

```bash
./get-gtfs-data.sh
```

**2. Start the stack**

```bash
docker compose up
```

This starts all services — Kafka, Redis, PostgreSQL, the simulator, ingestor, backend, and frontend. The app will be up at `http://localhost:5173`.

The dataloading step for the Simulator will take some time on the first run but later runs will be cached.

**3. Load the data into PostgreSQL (first run only)**

Once the stack is up, register a user, promote them to `ADMIN` in the database, then load the data into postgres by hitting this protected endpoint:

```bash
curl -X POST http://localhost:8080/api/v1/admin/reload-gtfs \
  -H "Authorization: Bearer <your_admin_jwt>"
```

This populates PostgreSQL with stops, routes, trips, and calendar data.

## Project Structure

```
├── backend/        Spring Boot REST API, WebSocket broker, Kafka consumer
├── frontend/       map, auth, favourites, search
├── ingestor/       Spring Boot GTFS-Realtime poller and Kafka producer
├── simulator/      Python GTFS-Realtime feed simulator
└── docker-compose.yml
```

