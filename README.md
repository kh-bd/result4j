# Result4j

Result4j is a library with simple functional primitives like `Option` and `Result` with compiler support for them.

[![CI latest](https://github.com/kh-bd/result4j/actions/workflows/main-tests.yml/badge.svg)](https://github.com/kh-bd/result4j/actions/workflows/main-tests.yml)

## Why do we need it?

All we love functional primitives like `Option` and `Result`.
To use such primitives properly we have to write our code in so-called monad style
with higher-order functions like `map` and `flatmap`.

For example, we have a simplified service to sign a document.
First of all, we need to search a document in a storage.
If document was not found, the method should return an error.
Then do sign document. the `doSignDocument` method can return an error, to signal
that signing didn't go well. And lastly, we need to save signed document to storage back
and return DTO of that document.

Code might look like this:

```java

class SignService {

    Result<GenericError, DocumentDto> sign(UUID id) {
        return Result.fromOptional(repository.findById(id), GenericError.entityNotFound(id))
                .flatMap(document -> doSignDocument(document))
                .map(repository::save)
                .map(mapper::toDto);
    }
}

```

Such code style is good but there is no any compiler support.
We have to write all those `maps` and `flatmaps` everywhere.

The main idea of this library is to add a special method `unwrap` and compiler support for that method.
The invocation of this method is going to be detected at compile time and original code is rewritten.

For example, if we have a local variable declaration like this

```java
Result<GenericError, DocumentDto> sign(UUID id) {
    Document document = findDocumentById(id).unwrap();
    // ...
}
```

it's going to be rewritten at compile time to something like this

```java
Result<GenericError, DocumentDto> sign(UUID id) {
    Result<GenericError, Document> $$rev = findDocumentById(id);
    if ($$rev.isError()) {
        return Result.error($$rev.getError());
    }
    Document document = $$rev.get();
    // ...
}

```

So, the original code can be rewritten in more imperative way

```java

class SignService {

    Result<GenericError, DocumentDto> sign(UUID id) {
        Document document = Result.fromOptional(repository.findById(id), GenericError.entityNotFound(id)).unwrap();
        Document signed = doSignDocument(document).unwrap();
        return Result.success(mapper.toDto(repository.save(signed)));
    }
}

```

Such code is much cleaner, easier to read and write then original one and at the same time explicitly propagates errors
as original code does.

## Versions

We are going to support separate version for each LTS release as long as that release is supported.
In the following table, you can find the latest result4j version for each supported java version.

| Java<br/> version | Latest release                                                                                                                                                                                                    |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `11`              | [![Maven jdk11](https://img.shields.io/maven-central/v/dev.khbd.result4j/result4j?color=brightgreen&versionSuffix=_jre11)](https://central.sonatype.com/artifact/dev.khbd.result4j/result4j/0.1.4_jre11/overview) 
| `17`              | [![Maven jdk17](https://img.shields.io/maven-central/v/dev.khbd.result4j/result4j?color=brightgreen&versionSuffix=_jre17)](https://central.sonatype.com/artifact/dev.khbd.result4j/result4j/0.1.4_jre17/overview) |
| `21`              | [![Maven jdk21](https://img.shields.io/maven-central/v/dev.khbd.result4j/result4j?color=brightgreen&versionSuffix=_jre21)](https://central.sonatype.com/artifact/dev.khbd.result4j/result4j/0.1.4_jre21/overview) |

## Maven support

To enable `unwrap()` call processing in maven-based projects you have to configure `maven-compiler-plugin` to enable `result4j` plugin during compilation.
Add the following configuration to your `pom.xml` file and that's it.

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <!-- enable result4j compiler plugin -->
            <arg>-Xplugin:result4j</arg>
        </compilerArgs>
        <annotationProcessorPaths>
            <path>
                <groupId>dev.khbd.result4j</groupId>
                <artifactId>result4j-processor</artifactId>
                <version>VERSION</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Compiler plugin uses internal jdk api and this api is [strongly encapsulated by default](https://openjdk.org/jeps/403) in jdk 17.
To relax it at compile time configuration should be changed accordingly.

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <fork>true</fork>
        <compilerArgs>
            <!-- enable result4j compiler plugin -->
            <arg>-Xplugin:result4j prettyPrint=true</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
        </compilerArgs>
        <annotationProcessorPaths>
            <path>
                <groupId>dev.khbd.result4j</groupId>
                <artifactId>result4j-processor</artifactId>
                <version>VERSION</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>

```
Additional exports are needed only for compiling process, resulted code will not be dependent on internal jdk api.

## Gradle support

todo
