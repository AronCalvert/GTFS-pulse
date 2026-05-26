import threading
import time
from datetime import datetime
from provider import StaticDataProvider
from manager import FleetManager
from flask import Flask, Response  # type: ignore


app = Flask(__name__)

data = StaticDataProvider()
fleet = FleetManager(data)


def simulation_loop():
    while True:
        fleet.update_positions()
        time.sleep(1)


@app.route("/gtfsr/vehicles")
def gtfsr_vehicles():
    feed = fleet.serialize_vehicle_positions()
    payload = feed.SerializeToString()
    return Response(payload, mimetype="application/x-protobuf")


@app.route("/gtfsr/trips")
def gtfsr_trips():
    feed = fleet.serialize_trip_updates()
    payload = feed.SerializeToString()
    return Response(payload, mimetype="application/x-protobuf")


if __name__ == "__main__":
    fleet.spawn_fleet(datetime.now().strftime("%H:%M:%S"))
    data._save_cache()
    t = threading.Thread(target=simulation_loop, daemon=True)
    t.start()
    app.run(host='0.0.0.0', port=5000, use_reloader=False)

