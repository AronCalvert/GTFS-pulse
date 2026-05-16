import pandas as pd


class StaticDataProvider:
    PATH = "../data/"

    def __init__(self):
        print("Loading GTFS data...")
        self.stop_times = pd.read_csv(self.PATH + "stop_times.txt")
        self.trips = pd.read_csv(self.PATH + "trips.txt")
        self.shapes = pd.read_csv(self.PATH + "shapes.txt")
        self.routes = pd.read_csv(self.PATH + "routes.txt")
        self.calendar = pd.read_csv(self.PATH + "calendar.txt")

        print("Preprocessing stop times...")
        times = pd.to_timedelta(self.stop_times["arrival_time"])  # type:ignore
        self.stop_times["arrival_secs"] = times.dt.total_seconds().astype("Int32")
        self.stop_times.dropna(subset=["arrival_secs"], inplace=True)

        print("Indexing trip lifetimes...")
        self.trip_bounds = (  # type:ignore
            self.stop_times.groupby("trip_id")["arrival_secs"]
            .agg(["min", "max"])
            .rename(columns={"min": "trip_start", "max": "trip_end"})
        )

        print("Indexing trips lookup...")
        self.trips_indexed = (
            self.trips[["trip_id", "route_id", "shape_id", "block_id"]]  # type: ignore
            .drop_duplicates("trip_id")
            .set_index("trip_id")
        )

        print("Building shape lookup table...")
        shapes_sorted = self.shapes.sort_values(["shape_id", "shape_pt_sequence"])
        self.shape_map = {
            sid: grp[["shape_pt_lat", "shape_pt_lon"]].values.tolist()
            for sid, grp in shapes_sorted.groupby("shape_id", observed=True)
        }

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
