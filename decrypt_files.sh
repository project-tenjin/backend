#!/usr/bin/env bash

set -ex

openssl aes-256-cbc -K $KEY_FROM_LASTPASS -iv $IV_FROM_LASTPASS -in src/main/resources/google_sheets_credentials.json.enc -out src/main/resources/google_sheets_credentials.json -d
openssl aes-256-cbc -K $KEY_FROM_LASTPASS -iv $IV_FROM_LASTPASS -in src/main/resources/google_sheets_acceptance_credentials.json.enc -out src/main/resources/google_sheets_acceptance_credentials.json -d
openssl aes-256-cbc -K $KEY_FROM_LASTPASS -iv $IV_FROM_LASTPASS -in src/main/resources/application-production.yml.enc -out src/main/resources/application-production.yml -d
openssl aes-256-cbc -K $KEY_FROM_LASTPASS -iv $IV_FROM_LASTPASS -in src/main/resources/application.yml.enc -out src/main/resources/application.yml -d
openssl aes-256-cbc -K $KEY_FROM_LASTPASS -iv $IV_FROM_LASTPASS -in src/test/resources/application.yml.enc -out src/test/resources/application.yml -d