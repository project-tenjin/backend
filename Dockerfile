########################################
 FROM seleniarm/standalone-chromium:latest as tester
 ########################################

 USER root

 RUN apt-get update -qqy && apt-get install -qqy npm

 # Set the working directory
 WORKDIR /app

 # Copy package.json and package-lock.json
 COPY package*.json ./

 # Install dependencies
 RUN npm install

 # Copy the source code
 COPY . .

RUN useradd -ms /bin/bash jasmine
 USER jasmine

 # Run jasmine-browser-runner and jasmine-core with chromium
 CMD npm test
