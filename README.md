# ReDI Attendance Tracking (Codename Tenjin)
Java Spring backend for project Tenjin

# System Requirements

* Ruby (>= 2)
* Java 8

# Running locally

* [install travis CLI](https://github.com/travis-ci/travis.rb#installation)
* decrypt Google Sheets credentials: `travis encrypt-file -d --key KEY_FROM_LASTPASS --iv IV_FROM_LASTPASS src/main/resources/google_sheets_credentials.json.enc src/main/resources/google_sheets_credentials.json`
* `./gradlew bootRun`

# Deploying

Travis is deploying to Cloud Foundry (user is redi-project-tenjin@googlegroups.com). No manual deploys
should ever be needed / done. Simply run all tests locally and push to github:

`./ship.sh`
