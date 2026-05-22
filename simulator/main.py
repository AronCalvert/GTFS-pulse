import threading
import time
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


@app.route("/gtfsr")
def gtfsr():
    feed = fleet.serialize_fleet()
    payload = feed.SerializeToString()
    return Response(payload, mimetype="application/x-protobuf")


if __name__ == "__main__":
    fleet.spawn_fleet("09:00:00")
    data._save_cache()
    t = threading.Thread(target=simulation_loop, daemon=True)
    t.start()
    app.run(host='0.0.0.0', port=5000, use_reloader=False)

