# Workshop Registration (EX2 - Maven Evolution)

Multi-module Maven project that implements the Workshop Registration API.

## Requirements

- JDK 25
- Apache Maven 3.9+

## Project structure

```
root            (pom, aggregator + parent)
|- core         (pom, aggregator + parent)
|  |- model-impl   implementations of Person, Instructor, Student, Workshop, Team
|  `- logic-impl   WorkshopSystemImpl (+ JsonExporter), logging, e-mail validation, JSON export
`- app          Main + log4j2.xml, builds the uber jar workshop-cli.jar
```

## One-time setup: install the course jars into the local repository

The API jars and the tester are not on Maven Central, so they must be installed
into the local repository (`~/.m2`) once on every computer, **before** the first build.
The jars are in the `libs` folder. Run these commands from the root of the project.

### Windows (PowerShell)

```powershell
mvn install:install-file "-Dfile=libs\model-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=model" "-Dversion=2.0" "-Dpackaging=jar" "-DgeneratePom=true"
mvn install:install-file "-Dfile=libs\logic-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=logic" "-Dversion=2.0" "-Dpackaging=jar" "-DgeneratePom=true"
mvn install:install-file "-Dfile=libs\workshop-tester-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=workshop-tester" "-Dversion=2.0" "-Dpackaging=jar" "-DgeneratePom=true"
```

### Linux / macOS

```bash
mvn install:install-file -Dfile=libs/model-2.0.jar -DgroupId=patmal.workshops -DartifactId=model -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=libs/logic-2.0.jar -DgroupId=patmal.workshops -DartifactId=logic -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=libs/workshop-tester-2.0.jar -DgroupId=patmal.workshops -DartifactId=workshop-tester -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
```

## Build

From the root folder:

```
mvn clean install
```

This builds all modules in the right order and creates the uber jar
`app/target/workshop-cli.jar`.

## Run the application

```
java -jar app/target/workshop-cli.jar
```

It creates demo data, writes `instructors.json`, `students.json` and `teams.json`
to the current folder and logs what it did.

## Run the tester

Windows:

```
java -cp "libs\workshop-tester-2.0.jar;app\target\workshop-cli.jar" patmal.workshops.main.Main
```

Linux / macOS:

```
java -cp "libs/workshop-tester-2.0.jar:app/target/workshop-cli.jar" patmal.workshops.main.Main
```

Expected last line: `RESULT: ALL OK`

## Libraries used

| Purpose | Library | Where |
|---|---|---|
| Logging | Log4j 2 (`log4j-api` in logic-impl, `log4j-core` at runtime in app) | logic-impl, app |
| E-mail validation | Apache Commons Validator | logic-impl |
| JSON export | Jackson Databind | logic-impl |

All versions are properties in the root `pom.xml`.