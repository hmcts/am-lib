#!/usr/bin/env bash

dir=$(realpath $(dirname ${0}))

command='gatling.sh -m -sf /gatling/workspace/simulations -bdf /gatling/workspace/bodies -rf /gatling/workspace/results'

docker run -it --network=host \
    -v ${dir}/conf:/etc/gatling/conf \
    -v ${dir}:/gatling/workspace:rw,z \
    -w /gatling/workspace \
    -e TEST_URL \
    hmcts/moj-gatling-image:2.3.1-1.0 ${command}