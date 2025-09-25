if [ ! -d "$(pwd)/command/config" ]; then
  echo "Error: config folder does not exist in current directory"
  exit 1
fi

docker run -d --rm \
  --name videostreamingpoc \
  -v "$(pwd)/command/config:/config:ro" \
  -p 8080:8080 videostreamingpoc:latest