#!/usr/bin/env sh

print_help() {
  echo "Script to run docker containers for Spring Boot Template API service

  Usage:

  ./run-in-docker.sh [OPTIONS]

  Options:
    --clean, -c                   Clean and install current state of source code
    --install, -i                 Install current state of source code
    --param PARAM=, -p PARAM=     Parse script parameter
    --help, -h                    Print this help block

  Available parameters:

  "
}

# script execution flags
GRADLE_CLEAN=true
GRADLE_INSTALL=true

# TODO custom environment variables application requires.
# TODO also consider enlisting them in help string above ^
# TODO sample: DB_PASSWORD   Defaults to 'dev'
# environment variables
#DB_PASSWORD=dev
#S2S_URL=localhost
#S2S_SECRET=secret

# Test S2S key - not used in any HMCTS key vaults or services
export S2S_SECRET=AAAAAAAAAAAAAAAB
export S2S_MICROSERVICE=am_accessmgmt_api

build_service_auth_app() {
    git clone https://github.com/hmcts/service-auth-provider-app.git
    cd service-auth-provider-app
    ./gradlew build
    docker build -t hmcts/service-token-provider .
    cd .. && rm -rf service-auth-provider-app
}

build_s2s_image() {
    git clone git@github.com:hmcts/s2s-test-tool.git
    cd s2s-test-tool
    git checkout allow-all-microservices
    ./gradlew build
    docker build -t hmcts/service-token-provider .
    cd .. && rm -rf s2s-test-tool
}

clean_old_docker_artifacts() {
    docker stop am-accessmgmt-api
    docker stop am-accessmgmt-api-db
    docker stop service-token-provider

    docker rm am-accessmgmt-api
    docker rm am-accessmgmt-api-db
    docker rm service-token-provider

    docker rmi hmcts/am-accessmgmt-api
    docker rmi hmcts/am-accessmgmt-api-db
    docker rmi hmcts/service-token-provider

}

execute_script() {

  clean_old_docker_artifacts

  build_s2s_image

  build_service_auth_app

  cd $(dirname "$0")/..

  if [ ${GRADLE_CLEAN} = true ]
  then
    echo "Clearing previous build.."
    ./gradlew clean
  fi

  if [ ${GRADLE_INSTALL} = true ]
  then
    echo "Assembling distribution.."
    ./gradlew assemble
  fi

  export SERVER_PORT="${SERVER_PORT:-8090}"

#  echo "Assigning environment variables.."
#
#  export DB_PASSWORD=${DB_PASSWORD}
#  export S2S_URL=${S2S_URL}
#  export S2S_SECRET=${S2S_SECRET}

  chmod +x bin/*

  echo "Bringing up docker containers.."

  docker-compose up
}

while true ; do
  case "$1" in
    -h|--help) print_help ; shift ; break ;;
    -c|--clean) GRADLE_CLEAN=true ; GRADLE_INSTALL=true ; shift ;;
    -i|--install) GRADLE_INSTALL=true ; shift ;;
    -p|--param)
      case "$2" in
#        DB_PASSWORD=*) DB_PASSWORD="${2#*=}" ; shift 2 ;;
#        S2S_URL=*) S2S_URL="${2#*=}" ; shift 2 ;;
#        S2S_SECRET=*) S2S_SECRET="${2#*=}" ; shift 2 ;;
        *) shift 2 ;;
      esac ;;
    *) execute_script ; break ;;
  esac
done
