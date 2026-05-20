import math


class BusInstance:
    def __init__(self, trip_id, route_id, block_id, direction_id, coords,
                 stop_times, snap_indices, segment_distances, current_time):
        self.trip_id = trip_id
        self.route_id = route_id
        self.block_id = block_id
        self.direction_id = direction_id
        self.coords = coords
        self.stop_times = stop_times
        self.stop_coord_indices = snap_indices
        self.segment_distances = segment_distances
        self.current_time = current_time
        self.bearing = 0.0
        self.speed = 0.0
        self.current_stop_sequence = 0
        self.finished = False
        self._stop_window = self._get_stop_window()
        self.current_index = self.stop_coord_indices[0] if self.stop_coord_indices else 0

    def start_time_str(self):
        secs = int(self.stop_times[0]["departure_secs"]) if self.stop_times else 0
        h, remainder = divmod(secs, 3600)
        m, s = divmod(remainder, 60)
        return f"{h:02d}:{m:02d}:{s:02d}"

    def get_current_stop_info(self):
        prev_idx, next_idx = self._stop_window
        if next_idx is None or prev_idx == next_idx:
            idx = max(prev_idx, 0)
            st = self.stop_times[idx]
            return st["stop_sequence"], str(st["stop_id"]), "STOPPED_AT"
        st = self.stop_times[next_idx]
        return st["stop_sequence"], str(st["stop_id"]), "IN_TRANSIT_TO"

    def _get_stop_window(self):
        for i, st in enumerate(self.stop_times):
            if self.current_time < st["arrival_secs"]:
                return i - 1, i
            if st["arrival_secs"] <= self.current_time <= st["departure_secs"]:
                return i, i
        return len(self.stop_times) - 1, None

    def move(self):
        self.current_time += 1
        prev_coord_idx = self.current_index
        prev_stop_idx, next_stop_idx = self._get_stop_window()
        self._stop_window = (prev_stop_idx, next_stop_idx)

        if next_stop_idx is None:
            self.finished = True
            return

        if prev_stop_idx == next_stop_idx:
            self.speed = 0.0
            return

        prev_st = self.stop_times[prev_stop_idx] if prev_stop_idx >= 0 else None
        next_st = self.stop_times[next_stop_idx]

        if prev_st is None:
            self.current_index = 0
            self.speed = 0.0
            return

        segment_start = prev_st["departure_secs"]
        segment_end = next_st["arrival_secs"]
        if segment_end <= segment_start:
            return

        progress = (self.current_time - segment_start) / (segment_end - segment_start)
        progress = max(0.0, min(1.0, progress))

        idx_start = self.stop_coord_indices[prev_stop_idx]
        idx_end = self.stop_coord_indices[next_stop_idx]
        self.current_index = int(idx_start + progress * (idx_end - idx_start))
        self.current_stop_sequence = next_st["stop_sequence"]

        if self.current_index + 1 < len(self.coords):
            p1 = self.coords[self.current_index]
            p2 = self.coords[self.current_index + 1]
            self.bearing = self.calculate_bearing(p1, p2)

        start_i = int(prev_coord_idx)
        end_i = min(int(self.current_index), len(self.segment_distances) - 1)
        if start_i < end_i:
            self.speed = sum(self.segment_distances[i] for i in range(start_i, end_i)) * 1000
        else:
            self.speed = 0.0

    def get_position(self):
        idx = min(int(self.current_index), len(self.coords) - 1)
        return self.coords[idx]

    def calculate_bearing(self, p1, p2):
        lat1, lon1 = map(math.radians, p1)
        lat2, lon2 = map(math.radians, p2)
        delta_lon = lon2 - lon1
        y = math.sin(delta_lon) * math.cos(lat2)
        x = math.cos(lat1) * math.sin(lat2) - math.sin(lat1) * math.cos(lat2) * math.cos(delta_lon)
        return (math.degrees(math.atan2(y, x)) + 360) % 360
