#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

# ── config (override via env vars) ───────────────────────────────────────────
CAMERA="${CAMERA:-Perspective}"
WIDTH="${WIDTH:-640}"
HEIGHT="${HEIGHT:-480}"
NUM_FRAMES="${NUM_FRAMES:-36}"       # 36 frames = 10° steps for a full 360°
FPS="${FPS:-12}"
OUTPUT_DIR="${OUTPUT_DIR:-./outputs/animations/animateDemo}"
VIDEO_OUT="${VIDEO_OUT:-./outputs/animations/animateDemo.mp4}"
# ─────────────────────────────────────────────────────────────────────────────

mkdir -p "$OUTPUT_DIR"
mkdir -p "$(dirname "$VIDEO_OUT")"

echo "Building SirRender..."
./gradlew installDist --quiet

ANGLE_STEP=$(echo "scale=6; 360 / $NUM_FRAMES" | bc)

echo "Rendering $NUM_FRAMES frames (${ANGLE_STEP}° per step) at ${WIDTH}×${HEIGHT}..."
for i in $(seq 0 $((NUM_FRAMES - 1))); do
    ANGLE=$(echo "scale=6; $i * $ANGLE_STEP" | bc)
    printf "  Frame %03d / %03d  (angle = %s°)\r" "$((i+1))" "$NUM_FRAMES" "$ANGLE"
    SirRender demo \
        -w "$WIDTH" -h "$HEIGHT" -c "$CAMERA" \
        --observer-angle "$ANGLE" \
        --render \
        --name "$(printf 'frame_%03d' "$i")" \
        -o "$OUTPUT_DIR" \
        2>/dev/null
done
echo -e "\nAll frames rendered → $OUTPUT_DIR"

echo "Stitching video..."
ffmpeg -y \
    -r "$FPS" \
    -f image2 \
    -s "${WIDTH}x${HEIGHT}" \
    -i "$OUTPUT_DIR/frame_%03d.png" \
    -vcodec libx264 \
    -pix_fmt yuv420p \
    "$VIDEO_OUT"

echo "Cleaning up frames..."
rm "$OUTPUT_DIR"/frame_*.png
rmdir "$OUTPUT_DIR" 2>/dev/null || true   # remove dir only if empty

echo "Done! Video saved → $VIDEO_OUT"