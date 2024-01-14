# Result4j

Result4j is a library with simple functional primitives like `Option`, `Either`, etc. and compiler support for them.

[![CI latest](https://github.com/kh-bd/result4j/actions/workflows/main-tests.yml/badge.svg)](https://github.com/kh-bd/result4j/actions/workflows/main-tests.yml)

## Why do we need it?

All we love functional primitives like `Option` and `Either`.
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
    
    Either<GenericError, DocumentDto> sign(UUID id) {
        return repository.findById(id)
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
Either<GenericError, DocumentDto> sign(UUID id) {
    Document document = repository.findById(id).unwrap();
    // ...
}
```

it's going to be rewritten at compile time to something like this

```java
Either<GenericError, DocumentDto> sign(UUID id) {
    Either<GenericError, Document> $$rev = repository.findById(id);
    if ($$rev.isLeft()) {
        return Either.left($$rev.getLeft());
    }
    Document document = $$rev.getRight();
    // ...
}

```

So, the original code can be rewritten in something like this


```java

class SignService {

    Either<GenericError, DocumentDto> sign(UUID id) {
        Document document = repository.findById(id).unwrap();
        Document signed = doSignDocument(document).uwwrap();
        return mapper.toDto(repository.save(signed));
    }
}

```

Such code is much cleaner, easier to read and write then original one.

## Versions

We are going to support separate version for each LTS release as long as that release is supported.
In the following table, you can find the latest result4j version for each supported java version.

| Java<br/> version | Latest release                                                                                                                                                                                        |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `17`              | [![Maven jdk17](https://img.shields.io/maven-central/v/dev.khbd.result4j/result4j?color=brightgreen&versionSuffix=_jre17)](https://mvnrepository.com/artifact/dev.khbd.result4j/result4j/0.0.1_jre17) |
| `21`              | [![Maven jdk21](https://img.shields.io/maven-central/v/dev.khbd.result4j/result4j?color=brightgreen&versionSuffix=_jre21)](https://mvnrepository.com/artifact/dev.khbd.result4j/result4j/0.0.1_jre21) |

## Maven support

todo

## Gradle support

todo