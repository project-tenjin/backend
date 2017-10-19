#!/usr/bin/env bash

set -ex

./jasmine.sh
./gradlew clean build
