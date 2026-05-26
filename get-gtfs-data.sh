#!/bin/bash
set -e
curl -L "https://www.transportforireland.ie/transitData/Data/GTFS_Realtime.zip" -o /tmp/gtfs.zip
mkdir -p data
unzip -o /tmp/gtfs.zip -d data
rm /tmp/gtfs.zip
echo "Done."
