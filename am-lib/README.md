# Access Management Library

## Building the library

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install Gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

When command completed successfully the binary artifacts will be available under `build/libs` directory. 

## Developer notes

### Using Lombok in IDE

Lombok project is used to generate boilerplate code such as accessors methods or methods such as `equals`, `hashCode`, `toString`.

Gradle builds are configured with right annotation processor however when building the project in IDE, Lombok plugin will be required to compile.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
