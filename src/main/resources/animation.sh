./gradlew installDist

CAMERA="Perspective"  # choose between "Perspective" and "Orthogonal"

# 2. Generate frames into the temporary folder
for angle in $(seq 0 359); do
    angleNNN=$(printf "%03d" "$angle")
    build/install/SirRender/bin/SirRender demo \
        -W 640 -H 480 \
        -c "$CAMERA" \
        -i "$angle" \
        -o animation/img${CAMERA}_"${angleNNN}".png
done

# 3. Create the video, save it OUTSIDE the temp folder, then delete the temp folder
ffmpeg -r 25 -f image2 -s 640x480 \
    -i ./src/main/resources/animation/img${CAMERA}_%03d.png \
    -vcodec libx264 -pix_fmt yuv420p \
    ./src/main/resources/spheres${CAMERA}.mp4 # \
    # && rm -rf ./src/main/resources/animation