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

### Compilation in IDE

Project requires non-standard compilation settings to:
- emit parameter names for JDBI mappers
- apply aspects in post-compile weave

The simplest way to achieve above is to delegate compilation to Gradle. [IntelliJ IDE provides this capability out of the box](https://www.jetbrains.com/help/idea/gradle.html#delegate_build_gradle).

#### Manual compilation settings

##### Emitting parameter names in IDE

JDBI mapping between database and Java objects requires Java compiler to emit parameter names.  

To enable it add the `-parameters` setting to your compiler arguments in your IDE (make sure you recompile your code after).

##### Applying aspects in IDE

AspectJ aspects are used to deliver certain features of the library. Since that the build process needs to be extended by post-compile weave.

Process might differ from IDE to IDE but in general the following steps are required:
- installing AJC compiler and selecting AJC compiler for the project
- configuring compilation process so that compilation of *.java files is delegated to javac compiler
- configuring compilation process so that post-compile weave kicks off when compilation of *.java files completed

Make sure you recompile your code afterwards.

### Using Lombok in IDE

Lombok project is used to generate boilerplate code such as accessors methods or methods such as `equals`, `hashCode`, `toString`.

Gradle builds are configured with right annotation processor however when building the project in IDE, Lombok plugin will be required to compile.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
