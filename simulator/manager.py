import math
import time
from datetime import datetime, timezone
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
            stop_times = self.provider.get_stop_times_for_trip(row["trip_id"])
            snap_indices = self.provider.get_trip_snap_indices(row["trip_id"])
            segment_distances = self.provider.get_shape_segment_distances(
                row["shape_id"]
            )
            if coords and stop_times and snap_indices:
                bus = BusInstance(
                    trip_id=row["trip_id"],
                    route_id=row["route_id"],
                    block_id=row["block_id"],
                    direction_id=row.get("direction_id"),
                    coords=coords,
                    stop_times=stop_times,
                    snap_indices=snap_indices,
                    segment_distances=segment_distances,
                    current_time=now_secs,
                )
                self.active_buses.append(bus)
        print(f"Fleet spawned: {len(self.active_buses)} buses active.")

    def update_positions(self):
        for bus in self.active_buses:
            bus.move()
        self.active_buses = [b for b in self.active_buses if not b.finished]

    def serialize_vehicle_positions(self):
        feed = gtfs_realtime_pb2.FeedMessage()
        current_ts = int(time.time())
        today = datetime.now(timezone.utc).strftime("%Y%m%d")
        feed.header.gtfs_realtime_version = "2.0"
        feed.header.incrementality = gtfs_realtime_pb2.FeedHeader.FULL_DATASET  # type: ignore
        feed.header.timestamp = current_ts
        for bus in self.active_buses:
            lat, lon = bus.get_position()
            safe_lat = lat if not math.isnan(lat) else 0.0
            safe_lon = lon if not math.isnan(lon) else 0.0
            t_id = str(bus.trip_id) if bus.trip_id is not None else "unknown_trip"
            r_id = str(bus.route_id) if bus.route_id is not None else "unknown_route"
            b_id = str(bus.block_id) if bus.block_id is not None else f"V_{t_id}"
            stop_seq, stop_id, status_str = bus.get_current_stop_info()

            entity = feed.entity.add()
            entity.id = t_id

            entity.vehicle.trip.trip_id = t_id
            entity.vehicle.trip.route_id = r_id
            entity.vehicle.trip.start_time = bus.start_time_str()
            entity.vehicle.trip.start_date = today
            entity.vehicle.trip.schedule_relationship = (
                gtfs_realtime_pb2.TripDescriptor.SCHEDULED
            )
            if bus.direction_id is not None and not math.isnan(float(bus.direction_id)):
                entity.vehicle.trip.direction_id = int(bus.direction_id)

            entity.vehicle.position.latitude = safe_lat
            entity.vehicle.position.longitude = safe_lon
            entity.vehicle.position.bearing = bus.bearing
            entity.vehicle.position.speed = bus.speed

            entity.vehicle.current_stop_sequence = stop_seq
            entity.vehicle.stop_id = stop_id
            entity.vehicle.current_status = getattr(
                gtfs_realtime_pb2.VehiclePosition,
                status_str,
                gtfs_realtime_pb2.VehiclePosition.IN_TRANSIT_TO,
            )
            entity.vehicle.timestamp = current_ts
            entity.vehicle.vehicle.id = b_id
        return feed

    def serialize_trip_updates(self):
        feed = gtfs_realtime_pb2.FeedMessage()
        current_ts = int(time.time())
        today = datetime.now(timezone.utc).strftime("%Y%m%d")
        feed.header.gtfs_realtime_version = "2.0"
        feed.header.incrementality = gtfs_realtime_pb2.FeedHeader.FULL_DATASET
        feed.header.timestamp = current_ts

        import datetime as dt

        midnight_unix = int(dt.datetime.combine(dt.date.today(), dt.time()).timestamp())

        for bus in self.active_buses:
            t_id = str(bus.trip_id) if bus.trip_id is not None else "unknown_trip"
            r_id = str(bus.route_id) if bus.route_id is not None else "unknown_route"

            _, next_stop_idx = bus._stop_window
            if next_stop_idx is None:
                continue

            remaining_stops = bus.stop_times[next_stop_idx:]
            if not remaining_stops:
                continue

            entity = feed.entity.add()
            entity.id = f"tu_{t_id}"

            entity.trip_update.trip.trip_id = t_id
            entity.trip_update.trip.route_id = r_id
            entity.trip_update.trip.start_time = bus.start_time_str()
            entity.trip_update.trip.start_date = today
            entity.trip_update.trip.schedule_relationship = (
                gtfs_realtime_pb2.TripDescriptor.SCHEDULED
            )

            for stop in remaining_stops:
                stu = entity.trip_update.stop_time_update.add()
                stu.stop_sequence = stop["stop_sequence"]
                stu.stop_id = str(stop["stop_id"])
                stu.arrival.time = midnight_unix + int(stop["arrival_secs"])
                stu.departure.time = midnight_unix + int(stop["departure_secs"])

        return feed
