#!/usr/bin/env sh

##############################################################################
##
##  Stop and kill currently running docker image, pull newest version and
##  run it.
##
##############################################################################

warn ( ) {
    echo "$*"
}

warn "Currently running docker images"
docker ps -a

warn "Killing currently running docker image..."
docker kill potic-articles; docker rm potic-articles

warn "Pulling latest docker image..."
docker pull potic/potic-articles:$TAG_TO_DEPLOY

warn "Starting docker image..."
docker run -dit --name potic-articles -e LOG_PATH=/logs -v /logs:/logs -e MONGO_PASSWORD=$MONGO_PASSWORD -p 40402:8080 potic/potic-articles:$TAG_TO_DEPLOY

warn "Currently running docker images"
docker ps -a
