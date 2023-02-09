# ReDI Attendance Tracking (Codename Tenjin) [![Build Status](https://travis-ci.org/project-tenjin/backend.svg?branch=master)](https://travis-ci.org/project-tenjin/backend)
Java Spring app for project Tenjin.

# Environments

* [Acceptance](https://tenjin-acceptance.cfapps.io)
* [Production](https://app.redi-school.org)

# Requirements

* Java 11
* LastPass access (user redi-project-tenjin@googlegroups.com)
* [CF CLI](https://github.com/cloudfoundry/cli#downloads)
* [Travis CLI](https://github.com/travis-ci/travis.rb#installation)
* [Okta Admin console](https://dev-411538-admin.oktapreview.com/dev/console)

# Running locally

## Build and setup

* clone the project with git
* make sure you have a JDK that supports Java 11
* you need to decrypt encrypted files. Get the key and iv from Lastpass and set as env variables:
    * `export KEY_FROM_LASTPASS="<key>"`
    * `export IV_FROM_LASTPASS="<iv>"`
    * then run shell script:
    ```bash
    ./decrypt_files.sh
    ```
* now build and start the project once with gradle:
    ```bash
    ./gradlew bootRun
    ```
  * this will download all dependencies, compile the code and execute tests
  * then it will even start the app 
  * make sure everything is working fine and there is no error message in the output
* you can import the project into your favourite IDE, it should recognize the gradle config files and configure the project accordingly

## Starting the app

- In your IDE you can start the main method of [`AttendanceWebApplication`](src/main/java/org/redischool.attendance/AttendanceWebApplication.java)
- open http://localhost:8080 in your Browser
- you will be redirected to octa login page
- login with the credentials you got from lastpass
- now you should see your locally running attendance tracking app which asks you to select your class

Hints:
- make sure the user you use for okta login has the permission to view a course and to have a course with this name in your google sheet
- as the testsheet is publicly visible, do not enter real names or other personal data

## Running the tests

Build a docker image using the first part of the Dockerfile.
```shell
docker build . --target tester --tag "backend:tester"
```

Then run the built image.
```shell
docker run --name backend-tester --rm "backend:tester"
```

### Jasmine

The frontend code is tested using a Javascript test framework called Jasmine. The package.json file defines its configuration.

# Maintaining the Spreadsheet

> **See the [Admin user guide](https://docs.google.com/document/d/1fIz3po2vJMzwlIx3VeiIKGo0GpHNdIp_59fDWBSWB1Q/edit#heading=h.9uvtxou998ud)**

## Integration with Google Sheets
The spreadsheet is accessed by a service account (acceptance@tenjin-attendance.iam.gserviceaccount.com) associated to 
our team email (redi.project.tenjin@gmail.com). For the integration to work, the spreadsheet has to be shared (with 
editing rights) with the service account email.

The service account email is different for production! You can find it in google_sheets_credentials.json in the 
field "client_email".


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
This can be done in the spreadsheet in Google Sheets. Please refer to 
the [Admin user guide](https://docs.google.com/document/d/1z9lAxz9RiwG7kkgZsX_en_9pqCNMZH6-5sIyLrLkLz0) to see how to.

# Deploying

!!! THIS IS CURRENTLY NOT WORKING

Travis is deploying to Cloud Foundry (user is redi-project-tenjin@googlegroups.com) to acceptance and then to production, if tests pass.
No manual deploys should ever be needed / done. Simply run all tests locally and push to github:

`./ship.sh`

!!! CURRENT SOLUTION:

- add heroku as second remote to your project
- push to the heroku git repo, then this code is built and deployed automatically

## Get application logs

!!! THIS IS OUTDATED, AT LEAST PRODUCTION IS RUNNING ON HEROKU, ACCEPTANCE IS NOT RUNNING AT ALL 

The app is hosted on Cloud Foundry.
Login to the account:

```bash
cf login -a https://api.run.pivotal.io
# enter your credentials (username redi-project-tenjin@googlegroups.com, PW in Last Pass)
# select desired space, e.g. "acceptance". Org should be preselected to "tenjin".
cf logs backend-tenjin
```

!!! CURRENT SOLUTION:

- create heroku account
- let someone give you access to the project on heroku
- go to the heroku web frontend and get logs there (probably also possible via heroku cli)

# Security
## OKTA (Single Sign On)

### Login
Okta is an integrated identity and mobility management service. Built from the ground up in the cloud. We have a account 
setup for test and acceptance and can be managed here:
[Okta Admin console](https://dev-411538-admin.oktapreview.com/dev/console)

`spring-security-oauth2` has easy integration with okta. Configurations need to be setup in `application.yml` for 
`security.oauth2` config and the application should be configured with `@EnableOAuth2Sso`.
More info on how to setup the app can be found [here](https://developer.okta.com/blog/2017/11/20/add-sso-spring-boot-15-min)

In Okta, An App called `Tenjin Attendance App` is created and users who are allowed to access the app, can be added to 
the app on the Okta Admin console.
App configurations should be :
* Type: Web
* Allowed grant types : Authorization Code
* Login redirect URIs: All environements that use this app should be configured here. For e.g.: http://localhost:8080/authorization-code/callback
* On creation, a new client id and secret is generated which should be added to the `application.yml`.

### Access Control (Course Permissions)

List of courses can be viewed by all users who are added to the app but viewing the attendance of students and editing the attendance is only permissible by users who are configured in the right group on the Okta Admin console.
To add a new course: Create a new group in Okta Admin console, with the EXACT same NAME and add users who should have access to the course to the new group.
Implementation of the same can be found in `OktaGroupsCourseAccessValidator` class. Group names of the user are extracted from the access token claims.
Detailed explanation of how to setup the access token claims can be found [here](https://developer.okta.com/blog/2017/10/13/okta-groups-spring-security).

Detailed explanation on how to add a new course in OKTA and give teachers permissions can be found in the [Admin guide](https://docs.google.com/document/d/1z9lAxz9RiwG7kkgZsX_en_9pqCNMZH6-5sIyLrLkLz0/edit#bookmark=id.bzo0ztlrjgjg)
