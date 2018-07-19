# ReDI Attendance Tracking (Codename Tenjin) [![Build Status](https://travis-ci.org/project-tenjin/backend.svg?branch=master)](https://travis-ci.org/project-tenjin/backend)
Java Spring app for project Tenjin.

# Environments

* [Acceptance](https://tenjin-acceptance.cfapps.io)
* [Production](https://app.redi-school.org)

# Requirements

* Ruby (>= 2)
* Java 8
* LastPass access (user redi-project-tenjin@googlegroups.com)
* [CF CLI](https://github.com/cloudfoundry/cli#downloads)
* [Travis CLI](https://github.com/travis-ci/travis.rb#installation)

# Running locally

* decrypt Google Sheets credentials: `travis encrypt-file -d --key KEY_FROM_LASTPASS --iv IV_FROM_LASTPASS src/main/resources/google_sheets_credentials.json.enc src/main/resources/google_sheets_credentials.json`
* `./gradlew bootRun`

## Running the tests

First, make sure `jasmine` is installed for JavaScript tests. This requires
`ruby` and `bundler`. If `bundler` is not installed on your machine yet, run
`gem install bundler`. Then run `bundle install` to install `jasmine`.

To run all the tests, simply run `tests.sh`.

### Jasmine

Jasmine is configured with a custom config location. Either run `jasmine.sh`,
install a shell tool which reads the included `.env` file (such as
[dotenv.sh](https://github.com/gchaincl/dotenv.sh)) or run it directly while
setting the `JASMINE_CONFIG_PATH` manually (`bundle exec rake jasmine:ci JASMINE_CONFIG_PATH=./src/test/javascripts/support/jasmine.yml`).

# Maintaining the Spreadsheet

## Specifying the Google Sheet

### Getting the sheet id
The sheet id comes from the link to the spreadsheet.
Eg. `https://docs.google.com/spreadsheets/d/13xIEyaqGgaUQkt8vAYV8sTct7ilM3EgAb669MQIkBRI/edit` -> `13xIEyaqGgaUQkt8vAYV8sTct7ilM3EgAb669MQIkBRI`

### Configuration
This goes into environment specific config files
`src/main/resources/application.yml` is default and has acceptance configs
`src/main/resources/application-production.yml` for production
`src/test/resources/application.yml` for integration tests.

## Managing Courses, Students and Course Dates
This can be done in the spreadsheet in Google Sheets. Please refer to the [Admin user guide] (https://docs.google.com/document/d/1z9lAxz9RiwG7kkgZsX_en_9pqCNMZH6-5sIyLrLkLz0/edit#heading=h.tai0xfv6k8fe) to see how to.

# Deploying

Travis is deploying to Cloud Foundry (user is redi-project-tenjin@googlegroups.com) to acceptance and then to production, if tests pass.
No manual deploys should ever be needed / done. Simply run all tests locally and push to github:

`./ship.sh`

## Get application logs

The app is hosted on Cloud Foundry.
Login to the account:

```bash
cf login -a https://api.run.pivotal.io
# enter your credentials (username redi-project-tenjin@googlegroups.com, PW in Last Pass)
# select desired space, e.g. "acceptance". Org should be preselected to "tenjin".
cf logs backend-tenjin
```

# Security
## Basic Auth
Cloud Foundry is configured with two environment variables with the username and password for basic auth.

You can get them by `cf env tenjin-backend `

The output will be

```
Getting env variables for app tenjin-backend in org tenjin / space production as ...
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
