# How to run Gatling locally
### Prerequisites

- [Gatling](https://gatling:test.io/)
- [Gradle](https://gradle.org/)


### Running
Start am-lib project
```bash
gradle bootRun
```

Run Gatling inside the /src/gatling folder
```bash
gatling.sh -m -sf simulations -df data -bdf bodies -rf reports -bf binaries
```

