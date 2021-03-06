# Access Management Testing Service

[![Build Status](https://travis-ci.org/hmcts/am-lib.svg?branch=master)](https://travis-ci.org/hmcts/am-lib)

## Purpose

Access Management Testing Service helps testing [Access Management library](am-lib). 

The application exposes:
 - functional endpoints mirroring features of AM library
 - health endpoint (http://localhost:3703/health)
 - metrics endpoint (http://localhost:3703/metrics)

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install Gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application locally

Run database service in Docker:

```bash
  docker-compose up -d am-accessmgmt-api-db
```

Run application locally:

```bash
  ./gradlew bootRun
```

This will start the API application exposing port `3703`.

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:3703/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Running the application in Docker

Build application artifact (JAR):

```bash
  ./gradlew assemble
```

Create Docker image of application:

```bash
  docker-compose build
```

Run application and its dependencies in Docker:

```bash
  docker-compose up
```

This will start the API container exposing port `3704` (note change from the port mentioned in section above).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:3704/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application in Docker

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
