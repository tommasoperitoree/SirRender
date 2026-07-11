#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

# ── config (override via env vars) ───────────────────────────────────────────
SCENE_FILE="${SCENE_FILE:-./scenes/Sun-RedSphere.txt}"
WIDTH="${WIDTH:-640}"
HEIGHT="${HEIGHT:-360}"
NUM_FRAMES="${NUM_FRAMES:-36}"       # clock will go from 0 to 360 in NUM_FRAMES steps
FPS="${FPS:-12}"
NUM_RAYS="${NUM_RAYS:-10}"
DEPTH="${DEPTH:-5}"
ANTIALIASING="${ANTIALIASING:-10}"
# output dirs
SCENE_NAME=$(basename "$SCENE_FILE" .txt)
PFM_DIR="${PFM_DIR:-./outputs/animations/$SCENE_NAME}"
VIDEO_OUT="${VIDEO_OUT:-./outputs/animations/$SCENE_NAME.mp4}"
# ─────────────────────────────────────────────────────────────────────────────

mkdir -p "$PFM_DIR"
mkdir -p "$(dirname "$VIDEO_OUT")"

echo "Building SirRender..."
./gradlew installDist --quiet

CLOCK_STEP=$(echo "scale=6; 360 / $NUM_FRAMES" | bc)

echo "Scene     : $SCENE_FILE"
echo "Frames    : $NUM_FRAMES  (clock step = ${CLOCK_STEP}°)"
echo "Resolution: ${WIDTH}×${HEIGHT}  rays=$NUM_RAYS  depth=$DEPTH  aa=$ANTIALIASING"
echo "PFM output: $PFM_DIR"
echo ""

for i in $(seq 0 $((NUM_FRAMES - 1))); do
    CLOCK=$(echo "scale=6; $i * $CLOCK_STEP" | bc)
    printf "  Frame %03d / %03d  (clock = %s°)\r" "$((i+1))" "$NUM_FRAMES" "$CLOCK"
    SirRender render \
        --input-file "$SCENE_FILE" \
        --clock "$CLOCK" \
        --name "$(printf 'frame_%03d' "$i")" \
        -w "$WIDTH" -h "$HEIGHT" \
        -n "$NUM_RAYS" -d "$DEPTH" -a "$ANTIALIASING" \
        --render \
        -o "$PFM_DIR" \
        2>/dev/null

done
echo -e "\nAll frames rendered → $PFM_DIR"

echo "Stitching video..."
ffmpeg -y \
    -r "$FPS" \
    -f image2 \
    -s "${WIDTH}x${HEIGHT}" \
    -i "$PFM_DIR/frame_%03d.png" \
    -vcodec libx264 \
    -pix_fmt yuv420p \
    "$VIDEO_OUT"

echo ""
rm "$PFM_DIR"/frame_*.png
echo "Done! Video saved → $VIDEO_OUT"
echo "(PFM frames kept in $PFM_DIR — use: SirRender pfm-to-gif -i $PFM_DIR -o output.gif)"