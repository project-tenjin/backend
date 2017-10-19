#!/usr/bin/env bash

set -ex

bundle exec rake jasmine:ci JASMINE_CONFIG_PATH=./src/test/javascripts/support/jasmine.yml
