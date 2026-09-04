#!/usr/bin/env python3
"""Produce a coordinate-free summary of DJI Pilot 2 WPML/KMZ missions."""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import statistics
import xml.etree.ElementTree as ET
import zipfile
from collections import Counter
from pathlib import Path
from typing import Iterable


TEMPLATE_FIELDS = (
    "flyToWaylineMode",
    "finishAction",
    "exitOnRCLost",
    "executeRCLostAction",
    "takeOffSecurityHeight",
    "globalTransitionalSpeed",
    "droneEnumValue",
    "droneSubEnumValue",
    "payloadEnumValue",
    "payloadSubEnumValue",
    "templateType",
    "heightMode",
    "globalShootHeight",
    "autoFlightSpeed",
    "caliFlightEnable",
    "elevationOptimizeEnable",
    "smartObliqueEnable",
    "facadeWaylineEnable",
    "inclinedGimbalPitch",
    "shootType",
    "direction",
    "margin",
    "inclinedFlightSpeed",
    "efficiencyFlightModeEnable",
    "orthoCameraOverlapH",
    "orthoCameraOverlapW",
    "orthoLidarOverlapH",
    "orthoLidarOverlapW",
    "inclinedCameraOverlapH",
    "inclinedCameraOverlapW",
    "dewarpingEnable",
    "imageFormat",
    "returnMode",
)


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def first_text(element: ET.Element, key: str) -> str | None:
    for child in element.iter():
        if local_name(child.tag) == key:
            value = (child.text or "").strip()
            if value:
                return value
    return None


def all_text(element: ET.Element, key: str) -> list[str]:
    values = []
    for child in element.iter():
        if local_name(child.tag) == key:
            value = (child.text or "").strip()
            if value:
                values.append(value)
    return values


def read_xml(archive: zipfile.ZipFile, member: str) -> tuple[bytes, ET.Element]:
    raw = archive.read(member)
    return raw, ET.fromstring(raw)


def number(value: str | None) -> int | float | str | None:
    if value is None:
        return None
    try:
        parsed = float(value)
    except ValueError:
        return value
    if parsed.is_integer():
        return int(parsed)
    return round(parsed, 9)


def distribution(values: Iterable[str]) -> dict[str, int]:
    counts = Counter(values)
    return dict(sorted(counts.items(), key=lambda item: (-item[1], item[0])))


def placemark_coordinates(folder: ET.Element) -> list[tuple[float, float]]:
    points = []
    for element in folder.iter():
        if local_name(element.tag) != "Placemark":
            continue
        coordinates = first_text(element, "coordinates")
        if not coordinates:
            continue
        longitude, latitude, *_ = (float(part) for part in coordinates.split(","))
        points.append((latitude, longitude))
    return points


def geometry_summary(points: list[tuple[float, float]]) -> dict[str, object]:
    if not points:
        return {"waypoint_count": 0, "path_length_m": 0.0}
    latitude_origin = sum(point[0] for point in points) / len(points)
    longitude_origin = sum(point[1] for point in points) / len(points)
    local = [
        (
            (longitude - longitude_origin) * 111_320.0 * math.cos(math.radians(latitude_origin)),
            (latitude - latitude_origin) * 111_132.0,
        )
        for latitude, longitude in points
    ]
    segments = [
        math.hypot(local[index][0] - local[index - 1][0], local[index][1] - local[index - 1][1])
        for index in range(1, len(local))
    ]
    min_east = min(point[0] for point in local)
    max_east = max(point[0] for point in local)
    min_north = min(point[1] for point in local)
    max_north = max(point[1] for point in local)
    width = max_east - min_east
    height = max_north - min_north

    def normalized_position(point: tuple[float, float]) -> dict[str, float]:
        return {
            "east_fraction": round((point[0] - min_east) / width, 3) if width else 0.5,
            "north_fraction": round((point[1] - min_north) / height, 3) if height else 0.5,
        }

    def percentile(values: list[float], fraction: float) -> float:
        if not values:
            return 0.0
        ordered = sorted(values)
        position = (len(ordered) - 1) * fraction
        lower = math.floor(position)
        upper = math.ceil(position)
        if lower == upper:
            return ordered[lower]
        return ordered[lower] + (ordered[upper] - ordered[lower]) * (position - lower)

    first_heading = 0.0
    if len(local) > 1:
        delta_east = local[1][0] - local[0][0]
        delta_north = local[1][1] - local[0][1]
        first_heading = math.degrees(math.atan2(delta_east, delta_north)) % 360.0
    return {
        "waypoint_count": len(points),
        "path_length_m": round(sum(segments), 1),
        "median_segment_m": round(statistics.median(segments), 2) if segments else 0.0,
        "segment_p25_m": round(percentile(segments, 0.25), 2),
        "segment_p75_m": round(percentile(segments, 0.75), 2),
        "extent_width_m": round(width, 1),
        "extent_height_m": round(height, 1),
        "start_bbox_position": normalized_position(local[0]),
        "end_bbox_position": normalized_position(local[-1]),
        "first_segment_heading_deg": round(first_heading, 2),
    }


def action_group_summaries(folder: ET.Element) -> list[dict[str, object]]:
    groups = []
    for element in folder.iter():
        if local_name(element.tag) != "actionGroup":
            continue
        groups.append(
            {
                "group_id": number(first_text(element, "actionGroupId")),
                "start_index": number(first_text(element, "actionGroupStartIndex")),
                "end_index": number(first_text(element, "actionGroupEndIndex")),
                "mode": first_text(element, "actionGroupMode"),
                "trigger_type": first_text(element, "actionTriggerType"),
                "trigger_param": number(first_text(element, "actionTriggerParam")),
                "functions": all_text(element, "actionActuatorFunc"),
            }
        )
    return groups


def summarize(path: Path, label: str) -> dict[str, object]:
    with zipfile.ZipFile(path) as archive:
        template_raw, template = read_xml(archive, "wpmz/template.kml")
        waylines_raw, waylines = read_xml(archive, "wpmz/waylines.wpml")

    template_values = {
        key: number(first_text(template, key))
        for key in TEMPLATE_FIELDS
        if first_text(template, key) is not None
    }

    folders = [
        element
        for element in waylines.iter()
        if local_name(element.tag) == "Folder" and first_text(element, "waylineId") is not None
    ]
    folder_summaries = []
    capture_distances = []
    for folder in folders:
        speeds = [float(value) for value in all_text(folder, "waypointSpeed")]
        intervals = [float(value) for value in all_text(folder, "actionTriggerParam")]
        if speeds and intervals:
            capture_distances.extend(speed * interval for speed in set(speeds) for interval in set(intervals))
        folder_summaries.append(
            {
                "wayline_id": number(first_text(folder, "waylineId")),
                **geometry_summary(placemark_coordinates(folder)),
                "waypoint_speeds_mps": distribution(all_text(folder, "waypointSpeed")),
                "capture_intervals_s": distribution(all_text(folder, "actionTriggerParam")),
                "actions": distribution(all_text(folder, "actionActuatorFunc")),
                "heading_modes": distribution(all_text(folder, "waypointHeadingMode")),
                "turn_modes": distribution(all_text(folder, "waypointTurnMode")),
                "action_groups": action_group_summaries(folder),
            }
        )

    waypoint_count = sum(int(folder["waypoint_count"]) for folder in folder_summaries)
    return {
        "schema": "openfly.dji-wpml-audit.v1",
        "label": label,
        "archive_sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
        "template_sha256": hashlib.sha256(template_raw).hexdigest(),
        "waylines_sha256": hashlib.sha256(waylines_raw).hexdigest(),
        "privacy": "mission names and absolute coordinates omitted",
        "template": template_values,
        "wayline_count": len(folder_summaries),
        "waypoint_count": waypoint_count,
        "derived_capture_distances_m": sorted({round(value, 3) for value in capture_distances}),
        "waylines": folder_summaries,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("missions", nargs="+", type=Path, help="DJI WPML .kmz files")
    parser.add_argument("--pretty", action="store_true", help="pretty-print JSON")
    args = parser.parse_args()
    summaries = [summarize(path, f"mission_{index + 1}") for index, path in enumerate(args.missions)]
    output: object = summaries[0] if len(summaries) == 1 else summaries
    print(json.dumps(output, ensure_ascii=False, indent=2 if args.pretty else None, sort_keys=True))


if __name__ == "__main__":
    main()
