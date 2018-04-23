#!/usr/bin/env bash

set -ex

travis encrypt-file -d --key $KEY_FROM_LASTPASS --iv $IV_FROM_LASTPASS src/main/resources/google_sheets_credentials.json.enc src/main/resources/google_sheets_credentials.json
travis encrypt-file -d --key $KEY_FROM_LASTPASS --iv $IV_FROM_LASTPASS src/main/resources/google_sheets_acceptance_credentials.json.enc src/main/resources/google_sheets_acceptance_credentials.json
travis encrypt-file -d --key $KEY_FROM_LASTPASS --iv $IV_FROM_LASTPASS src/main/resources/application-production.yml.enc src/main/resources/application-production.yml
travis encrypt-file -d --key $KEY_FROM_LASTPASS --iv $IV_FROM_LASTPASS src/main/resources/application.yml.enc src/main/resources/application.yml
travis encrypt-file -d --key $KEY_FROM_LASTPASS --iv $IV_FROM_LASTPASS src/test/resources/application.yml.enc src/test/resources/application.yml