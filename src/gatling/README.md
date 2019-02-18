# How to run Gatling locally

## Prerequisites

- [Docker](https://www.docker.com)
- [realpath-osx](https://github.com/harto/realpath-osx) (Mac OS only)

## Running

Before you run simulation make sure AM testing service is up and running (for more info check the project's README).

When above precondition is met run simulation using the following command:

```$bash
 $ ./run.sh
```

You can optionally override default `TEST_URL` (set to `http://localhost:3704`) the following way:

```$bash
 $ TEST_URL=http://localhost:3703 ./run.sh
```

Bash script will:

1. mount Gatling config and sources Docker container
2. run simulation in same way as it happens in CI pipeline

When simulation is complete HTML report will be available in `src/gatling/reports` directory.

## IDE Setup

The following steps will make simulation development easier in IntelliJ IDEA IDE:   

1. copy lib directory from [Gatling bundle](https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/2.3.1/gatling-charts-highcharts-bundle-2.3.1-bundle.zip) to `src/gatling/lib` directory 
2. in IDE right click the `src/gatling/lib` directory and select "Add as Library..." from menu
3. in IDE click the `src/gatling/simulations` directory and mark it as "Sources root"
