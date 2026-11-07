#!/bin/bash

cd "$(dirname "$0")"

# Announce
echo "INFO: Starting the visualization app using the code in the 'core' folder."

# Kill any existing process on port 7001 to prevent errors
echo "INFO: Attempting to free up port 7001..."
pkill -f "gradle.*run" 2>/dev/null || true

# Ensure gradlew has execute permissions
echo "INFO: Setting execute permissions for gradlew..."
chmod +x app/backend/gradlew

# Function to clean up the background process
cleanup() {
    echo -e "\nINFO: Ctrl+C detected. Shutting down visualization app..."
    # Kill the entire process group of the Gradle task using POSIX-compliant syntax
    kill -TERM -- -$GRADLE_PID
    echo "INFO: Shutdown complete."
}

# Set the trap
trap cleanup SIGINT SIGTERM

# Run the backend in a new process group
cd app/backend
./gradlew run --quiet &

# Get the PID of the background process
GRADLE_PID=$!

# Instruct
echo "INFO: Visualization is ready. Open http://localhost:7001 in your browser."
echo "INFO: Press Ctrl+C in this terminal to stop the server."

# Wait for the background process to finish. The script will pause here.
# The `wait` command will be interrupted by the `trap`.
wait $GRADLE_PID

