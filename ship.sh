#!/usr/bin/env bash

set -ex

git pull -r
./tests.sh
git push
