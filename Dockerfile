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

# Run jasmine-browser-runner and jasmine-core with chromium
CMD npm test

########################################
FROM openjdk:8-jdk-alpine as builder
########################################

# Set environment variables for the application
ENV APP_HOME=/usr/app
ENV APP_NAME=backend

# Create the app home directory
RUN mkdir $APP_HOME

# Set the working directory
WORKDIR $APP_HOME

# Copy the application files
COPY . .

# Install Gradle
RUN apk add --no-cache gradle

# Install Pnpm
# RUN pnpm i

# Build the application
RUN ./gradlew build

# Expose the application port
EXPOSE 8080

# Start the application
CMD ["java", "-jar", "build/libs/$APP_NAME.jar"]
