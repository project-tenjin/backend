# ReDI Attendance Tracking (Codename Tenjin) ![Travis Build](https://travis-ci.org/project-tenjin/backend.svg?branch=master)
Java Spring backend for project Tenjin

# System Requirements

* Ruby (>= 2)
* Java 8

# Running locally

* [install travis CLI](https://github.com/travis-ci/travis.rb#installation)
* decrypt Google Sheets credentials: `travis encrypt-file -d --key KEY_FROM_LASTPASS --iv IV_FROM_LASTPASS src/main/resources/google_sheets_credentials.json.enc src/main/resources/google_sheets_credentials.json`
* `./gradlew bootRun`

# Maintaining the Spreadsheet

## Managing Courses
To add new courses, add a new sheet at the bottom of Google Spreadsheet, by clicking the "+" button.

![finding the plus icon](docs/images/add_course.png?raw=true)

Give the newly created sheet the name of your new course. Copy & Paste the structure of an existing course onto your
new sheet and adjust the data accordingly.

# Deploying

Travis is deploying to Cloud Foundry (user is redi-project-tenjin@googlegroups.com). No manual deploys
should ever be needed / done. Simply run all tests locally and push to github:

`./ship.sh`
