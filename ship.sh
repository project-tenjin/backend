#!/usr/bin/env bash

set -ex

git pull -r
./gradlew clean check
git push
