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

warn "Pulling latest docker image..."
docker pull potic/potic-articles:$TAG_TO_DEPLOY

warn "Killing currently running docker image..."
docker kill potic-articles; docker rm potic-articles

warn "Starting docker image..."
docker run -dit --name potic-articles --restart on-failure --link potic-pocket-api --link potic-mongodb --link potic-users -e LOG_PATH=/mnt/logs -v /mnt/logs:/mnt/logs -e MONGO_PASSWORD=$MONGO_PASSWORD -e LOGZIO_TOKEN=$LOGZIO_TOKEN -p 40402:8080 potic/potic-articles:$TAG_TO_DEPLOY

warn "Wait 30sec to check status"
sleep 30

warn "Currently running docker images"
docker ps -a
