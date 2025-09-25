if [ ! -d "$(pwd)/command/config" ]; then
  echo "Error: config folder does not exist in current directory"
  exit 1
fi

if [ "$(docker ps -aq -f name=^videostreamingpoc$)" ]; then
  echo "Removing existing container: videostreamingpoc"
  docker rm -f videostreamingpoc
fi

docker run -d --rm \
  --name videostreamingpoc \
  -v "$(pwd)/command/config:/config:ro" \
  -p 8080:8080 videostreamingpoc:latest