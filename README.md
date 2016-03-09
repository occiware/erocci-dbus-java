# Erocci java-dbus integration

## Prerequisite

* JDK
* Maven
* java D-Bus implementation. It can be installed with:

```sh
$ make deps
```

See "troubleshooting" part below in case of error.

## Quickstart

```sh
$ make run
```

## Project format

`src/main/dbus/` contains the XML file describing the erocci DBus interface.

pom.xml imports the maven-dbus-plugin (to generate a java interface)
and specifies dependencies (to compile).

Note: the maven-dbus-plugin version is the one available online at
maven central (0.1). To compile locally your own version from sources,
set version to 0.2-SNAPSHOT in pom.xml.

## Code generation

```sh
$ mvn clean install
```
The java interface is generated in src/generated/java, then compiled in
target/classes .

## Launch as standalone service (outside Eclipse/IDE)

```sh
$ mvn exec:java -Dexec.mainClass="org.ow2.erocci.backend.BackendDBusService"
```

The dbus service is called "org.ow2.erocci.backend".

See "troubleshooting" part below in case of error.

## Interface with erocci

An erocci-dbus-java.config file is provided (in the project's home directory).
Launch erocci with this file used as configuration; for example, in the erocci directory:

```sh
./start.sh -c ../erocci-dbus-java/erocci-dbus-java.config
```

## Troubleshooting


* "no unix-java in java.library.path"
Generally occurs when you do not use your system's default JDK. To fix it:
Copy libunix-java.so in your JDK's jre/lib/<arch> directory, eg.
sudo cp /usr/lib/jni/libunix-java.so /usr/lib/jvm/jdk1.8.0_45/jre/lib/amd64/

* "Failed to connect to bus unknown address type kernel"

Check the DBUS_SESSION_BUS_ADDRESS environment variable. It is a list of one or more items (separated by semicolons).
The 1st item should start with "unix:". If its starts with "kernel:", remove the 1st item from the list.
Example:
```sh
$ printenv DBUS_SESSION_BUS_ADDRESS
kernel:path=/sys/fs/kdbus/1000-user/bus;unix:path=/run/user/1000/bus
$ export DBUS_SESSION_BUS_ADDRESS=unix:path=/run/user/1000/bus
```

(can also be done using a sed command:
```sh
DBUS_SESSION_BUS_ADDRESS=$(printenv DBUS_SESSION_BUS_ADDRESS | sed -e 's/kernel:.*;//' ).
```
## Progress dashboard
<a href="http://github.com/erocci/occiware/erocci-dbus-java/blob/master/doc/devstatus.md" class="module">Development status</a>

## Installation from scratch
### On Ubuntu 15.10
* Install openjdk 8 : ```sudo apt-get install openjdk-8-jdk``` 
* Download Erlang 1.8.x for Ubuntu on the website editor (https://www.erlang-solutions.com/resources/download.html) or use : 
```sudo apt-get install erlang```
* Git Clone the Erocci project
* Git Clone the Erocci-dbus-java project
* Install maven3 using  ```sudo apt-get install maven```
* Install dbus binaries for java : sudo apt-get install dbus-java-bin
* Copy jni file like this : ```cp /usr/lib/jni/libunix-java.so /usr/lib/jvm/java-1.8.0-openjdk-amd64/lib/amd64/```
* Go to erocci-dbus-java root directory
* ```make deps```
* ```make```
* Go to Erocci root directory
* launch ```./bootstrap``` and then ```make``` after the message "release successfully created" will display if all ok.
* Launch erocci-dbus-java on first place before Erocci
* Launch Erocci as describe upper.


