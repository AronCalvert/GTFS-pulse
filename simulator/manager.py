import math
import time
from bus import BusInstance
from google.transit import gtfs_realtime_pb2


class FleetManager:
    def __init__(self, data_provider):
        self.provider = data_provider
        self.active_buses = []

    def spawn_fleet(self, snapshot_time_str):
        manifest = self.provider.get_active_trips_at_time(snapshot_time_str)

        hh, mm, ss = map(int, snapshot_time_str.split(":"))
        now_secs = hh * 3600 + mm * 60 + ss

        self.active_buses = []
        records = manifest.to_dict("records")

        for row in records:
            coords = self.provider.get_shape_coords(row["shape_id"])
            if coords:
                bus = BusInstance(
                    trip_id=row["trip_id"],
                    route_id=row["route_id"],
                    block_id=row["block_id"],
                    coords=coords,
                    start_secs=row["trip_start"],
                    end_secs=row["trip_end"],
                    current_time=now_secs,
                )
                self.active_buses.append(bus)

        print(f"Fleet spawned: {len(self.active_buses)} buses active.")

    def update_positions(self):
        for bus in self.active_buses:
            bus.move()

        self.active_buses = [
            b for b in self.active_buses if b.current_index < len(b.coords)
        ]

    def serialize_fleet(self):
        feed = gtfs_realtime_pb2.FeedMessage()  # type: ignore
        current_ts = int(time.time())

        feed.header.gtfs_realtime_version = "2.0"
        feed.header.incrementality = gtfs_realtime_pb2.FeedHeader.FULL_DATASET  # type: ignore
        feed.header.timestamp = current_ts

        for bus in self.active_buses:
            lat, lon = bus.get_position()
            safe_lat = lat if not math.isnan(lat) else 0.0
            safe_lon = lon if not math.isnan(lon) else 0.0
            safe_bearing = getattr(bus, "bearing", 0.0)

            t_id = str(bus.trip_id) if bus.trip_id is not None else "unknown_trip"
            r_id = str(bus.route_id) if bus.route_id is not None else "unknown_route"
            b_id = str(bus.block_id) if bus.block_id is not None else f"V_{t_id}"

            entity = feed.entity.add()
            entity.id = str(t_id)
            entity.vehicle.trip.trip_id = str(t_id)
            entity.vehicle.trip.route_id = str(r_id)
            entity.vehicle.position.latitude = safe_lat
            entity.vehicle.position.longitude = safe_lon
            entity.vehicle.position.bearing = safe_bearing
            entity.vehicle.timestamp = current_ts
            entity.vehicle.vehicle.id = b_id
        return feed
