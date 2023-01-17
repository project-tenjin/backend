#!/usr/bin/env bash

set -ex
export OPENSSL_CONF=/dev/null

./jasmine.sh
./gradlew clean build
