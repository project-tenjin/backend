#!/usr/bin/env bash

set -ex

git pull -r
./gradlew clean build
git push

