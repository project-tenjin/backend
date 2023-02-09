#!/usr/bin/env bash

set -ex
export OPENSSL_CONF=/dev/null

./gradlew clean build
