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

### Emitting parameter names

JDBI mapping between database and Java objects requires Java compiler to emit parameter names.  

To enable it add the `-parameters` setting to your compiler arguments in your IDE (make sure you recompile your code after).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
