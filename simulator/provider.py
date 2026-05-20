import atexit
import os
import pickle

import pandas as pd
from math import sin, cos, radians, asin, sqrt

SNAP_CACHE_PATH = "../data/.snap_cache.pkl"


def _haversine(lat1, lon1, lat2, lon2):
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    return 2 * asin(sqrt(a)) * 6371  # km


def _snap_stops_to_shape(stop_times, coords):
    indices = []
    search_from = 0
    for st in stop_times:
        best_idx = search_from
        best_dist = float("inf")
        for i in range(search_from, len(coords)):
            dist = _haversine(st["stop_lat"], st["stop_lon"], coords[i][0], coords[i][1])
            if dist < best_dist:
                best_dist = dist
                best_idx = i
        indices.append(best_idx)
        search_from = best_idx
    return indices


class StaticDataProvider:
    PATH = "../data/"

    def __init__(self):
        print("Loading GTFS data...")
        self.stop_times = pd.read_csv(self.PATH + "stop_times.txt")
        self.trips = pd.read_csv(self.PATH + "trips.txt")
        self.shapes = pd.read_csv(self.PATH + "shapes.txt")
        self.routes = pd.read_csv(self.PATH + "routes.txt")
        self.calendar = pd.read_csv(self.PATH + "calendar.txt")
        self.stops = pd.read_csv(self.PATH + "stops.txt")

        print("Preprocessing stop times...")
        self.stop_times["arrival_secs"] = (
            self.stop_times["arrival_time"].map(_parse_gtfs_time_to_secs).astype("Int32")
        )
        self.stop_times.dropna(subset=["arrival_secs"], inplace=True)
        self.stop_times["departure_secs"] = (
            self.stop_times["departure_time"].map(_parse_gtfs_time_to_secs).astype("Int32")
        )
        self.stop_times.dropna(subset=["departure_secs"], inplace=True)
        print("Indexing trip lifetimes...")
        self.trip_bounds = (  # type:ignore
            self.stop_times.groupby("trip_id")["arrival_secs"]
            .agg(["min", "max"])
            .rename(columns={"min": "trip_start", "max": "trip_end"})
        )

        print("Indexing trips lookup...")
        self.trips_indexed = (
            self.trips[["trip_id", "route_id", "shape_id", "block_id", "direction_id"]]  # type: ignore
            .drop_duplicates("trip_id")
            .set_index("trip_id")
        )

        print("Building shape lookup table...")
        shapes_sorted = self.shapes.sort_values(["shape_id", "shape_pt_sequence"])
        self.shape_map = {
            sid: grp[["shape_pt_lat", "shape_pt_lon"]].values.tolist()
            for sid, grp in shapes_sorted.groupby("shape_id", observed=True)
        }

        print("Building stop times lookup...")
        self.stop_times_map = {
            trip_id: grp[
                [
                    "stop_sequence",
                    "arrival_secs",
                    "departure_secs",
                    "stop_id",
                    "stop_lat",
                    "stop_lon",
                ]
            ].to_dict("records")  # type: ignore
            for trip_id, grp in self.stop_times.merge(
                self.stops[["stop_id", "stop_lat", "stop_lon"]],
                on="stop_id",
                how="left",
            )
            .sort_values("stop_sequence")
            .groupby("trip_id")
        }

        print("Pre-computing shape segment distances...")
        self.shape_segment_distances = {
            shape_id: [
                _haversine(coords[i][0], coords[i][1], coords[i + 1][0], coords[i + 1][1])
                for i in range(len(coords) - 1)
            ]
            for shape_id, coords in self.shape_map.items()
        }

        self._trips_shape = self.trips_indexed["shape_id"].to_dict()
        self._snap_cache = self._load_snap_cache()
        atexit.register(self._save_snap_cache)

        print("--- Data loaded and indexed successfully ---")

    def get_active_trips_at_time(self, target_time_str):
        hh, mm, ss = map(int, target_time_str.split(":"))
        now = hh * 3600 + mm * 60 + ss

        active = self.trip_bounds[
            (self.trip_bounds["trip_start"] <= now)
            & (self.trip_bounds["trip_end"] >= now)
        ]
        return active.join(self.trips_indexed, how="inner").reset_index()

    def get_shape_coords(self, shape_id):
        return self.shape_map.get(shape_id, [])

    def get_stop_times_for_trip(self, trip_id):
        return self.stop_times_map.get(trip_id, [])

    def _load_snap_cache(self):
        if os.path.exists(SNAP_CACHE_PATH):
            print("Loading snap cache from disk...")
            with open(SNAP_CACHE_PATH, "rb") as f:
                return pickle.load(f)
        print("No snap cache found — will compute lazily.")
        return {}

    def _save_snap_cache(self):
        print(f"Saving snap cache ({len(self._snap_cache)} trips) to disk...")
        with open(SNAP_CACHE_PATH, "wb") as f:
            pickle.dump(self._snap_cache, f)

    def get_trip_snap_indices(self, trip_id):
        if trip_id not in self._snap_cache:
            coords = self.shape_map.get(self._trips_shape.get(trip_id, ""), [])
            stop_times = self.stop_times_map.get(trip_id, [])
            self._snap_cache[trip_id] = _snap_stops_to_shape(stop_times, coords) if coords and stop_times else []
        return self._snap_cache[trip_id]

    def get_shape_segment_distances(self, shape_id):
        return self.shape_segment_distances.get(shape_id, [])

def _parse_gtfs_time_to_secs(time_str):
    try:
        h, m, s = map(int, time_str.split(":"))
        return h * 3600 + m * 60 + s
    except:
        return None
