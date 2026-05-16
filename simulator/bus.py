import math


class BusInstance:
    def __init__(
        self, trip_id, route_id, block_id, coords, start_secs, end_secs, current_time
    ):
        self.trip_id = trip_id
        self.route_id = route_id
        self.coords = coords
        self.block_id = block_id
        self.bearing = 0

        total_duration = end_secs - start_secs
        elapsed_time = current_time - start_secs
        progress_pct = elapsed_time / total_duration

        self.current_index = int(len(self.coords) * progress_pct)

        total_ticks = total_duration / 2
        self.step_size = len(self.coords) / total_ticks

    def move(self):
        prev_index = int(self.current_index)
        self.current_index += self.step_size
        curr_index = int(self.current_index)

        if curr_index > prev_index and curr_index + 1 < len(self.coords):
            p1 = self.coords[curr_index]
            p2 = self.coords[curr_index + 1]
            self.bearing = self.calculate_bearing(p1, p2)

    def get_position(self):
        idx = min(int(self.current_index), len(self.coords) - 1)
        return self.coords[idx]

    def calculate_bearing(self, p1, p2):
        lat1, lon1 = map(math.radians, p1)
        lat2, lon2 = map(math.radians, p2)

        delta_lon = lon2 - lon1

        y = math.sin(delta_lon) * math.cos(lat2)
        x = math.cos(lat1) * math.sin(lat2) - math.sin(lat1) * math.cos(
            lat2
        ) * math.cos(delta_lon)

        bearing = math.atan2(y, x)
        return (math.degrees(bearing) + 360) % 360
