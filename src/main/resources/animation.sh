# 1. Create a safe temporary output folder (OUTSIDE of src/)
mkdir -p ./src/main/resources/animationBash

./gradlew installDist

CAMERA="Orthogonal"

# Convert camera to lowercase for the folder path (macOS/Linux safe)
CAMERA_LOWER=$(echo "$CAMERA" | tr '[:upper:]' '[:lower:]')

# 2. Boot the JVM EXACTLY ONCE and let Kotlin do the 360-frame loop internally
echo "Starting Kotlin Ray Tracer..."
build/install/SirRender/bin/SirRender demo \
    -W 640 -H 480 \
    -c "$CAMERA" \
    -n 90 \
    -o ./src/main/resources/animationBash

# 3. Create the video from the Kotlin-generated path
# Note: Kotlin automatically puts them in a subfolder (e.g., ./src/main/resources/animationBash/perspective/frame_000.png)
echo "Stitching video with ffmpeg..."
ffmpeg -r 25 -f image2 -s 640x480 \
    -i ./src/main/resources/animationBash/"$CAMERA_LOWER"/frame_%03d.png \
    -vcodec libx264 -pix_fmt yuv420p \
    ./src/main/resources/spheres"$CAMERA".mp4 \
    && rm -rf ./src/main/resources/animationBash

echo "Done! Video saved to ./src/main/resources/spheres${CAMERA}.mp4"