#!/bin/bash
confirm () {
    read -r -p "${1:-Are you sure? [y/N]} " response
    case $response in
        [yY][eE][sS]|[yY])
            true
            ;;
        *)
            false
            ;;
    esac
}

TAG="ardoq/ardoq-maven-addon:latest"

echo "building $TAG"
docker build -t $TAG .
confirm "Push $TAG to DockerHub? [y/N]" && docker push $TAG



