# ReDI Attendance Tracking (Codename Tenjin) [![Build Status](https://travis-ci.org/project-tenjin/backend.svg?branch=master)](https://travis-ci.org/project-tenjin/backend)
Java Spring backend for project Tenjin

# Requirements

* Ruby (>= 2)
* Java 8
* LastPass access (user redi-project-tenjin@googlegroups.com)
* [CF CLI](https://github.com/cloudfoundry/cli#downloads)
* [Travis CLI](https://github.com/travis-ci/travis.rb#installation)

# Running locally

* decrypt Google Sheets credentials: `travis encrypt-file -d --key KEY_FROM_LASTPASS --iv IV_FROM_LASTPASS src/main/resources/google_sheets_credentials.json.enc src/main/resources/google_sheets_credentials.json`
* `./gradlew bootRun`

# Maintaining the Spreadsheet

## Specifying the Google Sheet

### Getting the sheet id
The sheet id comes from the link to the spreadsheet.
Eg. `https://docs.google.com/spreadsheets/d/13xIEyaqGgaUQkt8vAYV8sTct7ilM3EgAb669MQIkBRI/edit` -> `13xIEyaqGgaUQkt8vAYV8sTct7ilM3EgAb669MQIkBRI`

### Configuration
This goes into `src/main/resources/application.yml` for production and `src/test/resources/application.yml` for integration tests.

## Managing Courses
To add new courses, add a new sheet at the bottom of Google Spreadsheet, by clicking the "+" button.

![finding the plus icon](docs/images/add_course.png?raw=true)

Give the newly created sheet the name of your new course. Copy & Paste the structure of an existing course onto your
new sheet and adjust the data accordingly.

## Managing Students
Students **must** be in row `B` of each course sheet. They **must** start in column `B:4`. There can be as many students as required.
Simply add a new student row to add a new student or remove a row entirely to remove the student.

## Managing Course Dates
The first date of the course **must** start at `C:2` and the last three cells in the `C` row **must**  contain `Late`, `Excused absence` and `Unexcused absence`.

## Additional Information
Any sheet with an asterisk in its name, for example `This course is not shown*` will not be listed in the app.

# Deploying

Travis is deploying to Cloud Foundry (user is redi-project-tenjin@googlegroups.com). No manual deploys
should ever be needed / done. Simply run all tests locally and push to github:

`./ship.sh`

## Get application logs

The app is hosted on Cloud Foundry.
Login to the account:

```bash
cf login -a https://api.run.pivotal.io
# enter your credentials (username redi-project-tenjin@googlegroups.com, PW in Last Pass)
# select desired space, e.g. "development". Org should be preselected to "tenjin".
cf logs backend-tenjin
```

# Security
## Basic Auth
Cloud Foundry is configured with two environment variables with the username and password for basic auth.

You can get them by `cf env tenjin-backend `

The output will be

```
Getting env variables for app tenjin-backend in org tenjin / space development as ...
...
snip
...
User-Provided:
CREDENTIALS_USERNAME: userNameFromLastPass
CRENTIALS_PASSWORD passwordFromLastPass
```

These were set at one point with the commands from the secure note `Environment Variables for basic auth` in lastpass.

```
cf set-env tenjin-backend CREDENTIALS_USERNAME userNameFromLastPass
cf set-env tenjin-backend CREDENTIALS_PASSWORD passwordFromLastPass
```
