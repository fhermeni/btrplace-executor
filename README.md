# Btrplace Executor #

A simple artifact to execute reconfiguration plans.

[![Build Status](http://btrp.inria.fr:8080/jenkins/buildStatus/icon?job=DEV btrplace-executor)](http://btrp.inria.fr:8080/jenkins/job/DEV%20btrplace-executor/)

## Integration ##

This artifact in in a private repository so you have first to edit your `pom.xml` to declare them:

```xml
<repositories>
    <repository>
        <id>btrp-releases</id>
        <url>http://btrp.inria.fr/repos/releases</url>
    </repository>
    <repository>
        <id>btrp-snapshots</id>
        <url>http://btrp.inria.fr/repos/snapshot-releases</url>
    </repository>
</repositories>
```

Next, just declare the dependency:

```xml
<dependency>
   <groupId>btrplace</groupId>
   <artifactId>executor</artifactId>
   <version>1.1.1</version>
</dependency>
```

## Building from sources ##

Requirements:
* JDK 7+
* maven 3+

The source of the released versions are directly available in the `Tag` section.
You can also download them using github features.
Once downloaded, move to the source directory then execute the following command
to make the jar:

    $ mvn clean install

If the build succeeded, the resulting jar will be automatically
installed in your local maven repository and available in the `target` sub-folder.

## Documentation ##

* releases: http://btrp.inria.fr/executor/ (`apidocs` always refers to the last release)
* snapshot-releases: http://btrp.inria.fr/executor/apidocs-snapshot

## Usage ##

Have a look at the `Executor` class. You will have to provide custom `actuators` to make the abstract
actions of BtrPlace fit your environment.

To develop an `Actuator`, you must implement the interface and `ActuatorBuilder`. The second class will
be provided to the `ActuatorFactory` to create your actuator when a compatible action will have to be executed

## Copyright ##
Copyright (c) 2013 University of Nice-Sophia Antipolis. See `LICENSE.txt` for details
