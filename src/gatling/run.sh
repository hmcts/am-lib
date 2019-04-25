#!/usr/bin/env bash

dir=$(realpath $(dirname ${0}))

command='gatling.sh --simulations-folder /gatling/workspace/simulations --results-folder /gatling/workspace/results --resources-folder /gatling/workspace/resources'

docker run -it --network=host \
    -v ${dir}/conf:/etc/gatling/conf \
    -v ${dir}:/gatling/workspace:rw,z \
    -w /gatling/workspace \
    -e TEST_URL \
    hmcts/gatling:3.1.1-java-8-1.0 ${command}
