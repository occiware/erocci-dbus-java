# Erocci java-dbus integration

[![Build Status](https://travis-ci.org/occiware/erocci-dbus-java.svg?branch=master)](https://travis-ci.org/occiware/erocci-dbus-java)

## Progress dashboard
[Progress development dashboard](doc/devstatus.md)

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

## Installation from scratch
### On Ubuntu 15.10
* Install openjdk 8 : ```sudo apt-get install openjdk-8-jdk``` 
* Download Erlang 1.8.x for Ubuntu on the website editor (https://www.erlang-solutions.com/resources/download.html) or use : 
```sudo apt-get install erlang```
* Git Clone the Erocci project
* Git Clone the Erocci-dbus-java project
* Install maven3 using  ```sudo apt-get install maven```
* Install dbus binaries for java : ```sudo apt-get install dbus-java-bin```
* Copy jni file like this : ```cp /usr/lib/jni/libunix-java.so /usr/lib/jvm/java-1.8.0-openjdk-amd64/lib/amd64/```
* Go to erocci-dbus-java root directory
* ```make deps```
* ```make```
* Go to Erocci root directory
* launch ```./bootstrap``` and then ```make``` after the message "release successfully created" will display if all ok.
* Launch erocci-dbus-java on first place before Erocci
* Launch Erocci as describe upper.

### On Mac OSX platform (El capitan) 
* Install XCode, java jdk 8 and set env variable JAVA_HOME
* Install D-BUS with : ```brew install dbus```, it may be installed on path : ```/usr/local/Cellar/d-bus/1.10.6/```
* If you don't have maven installed : ```brew install maven``` , this will install maven 3 in ```/usr/local/Cellar/maven/3.3.9/```
* Download and build libmatthew-java : 
    * ```cd $JAVA_HOME/include/``` 
    * ```sudo ln -s darwin linux```
    * Go to your favorite directory to clone libmatthew-java project (for ex: cd ~/myworkspace/)
    * ```git clone https://github.com/abstractj/libmatthew-java```
    * ```cd libmatthew-java```
    * ```mvn install```
* Copy jni libraries libunix-java.so to your lib native path like this (note that extension is .dylib) : 
```sudo cp ./libmatthew-java/libunix/target/libunix-java.so $JAVA_HOME/jre/lib/libunix-java.dylib```
* Launch dbus :
    * if necessary create directory ```~/Library/LaunchAgents/```
    * Create a symbolic link : ```ln -sfv /usr/local/opt/d-bus/*.plist ~/Library/LaunchAgents```
    * Edit your .bash_profile file and add : ```export DBUS_SESSION_BUS_ADDRESS=unix:path=$DBUS_LAUNCHD_SESSION_BUS_SOCKET```
    * Edit the file :  ```/usr/local/Cellar/d-bus/1.10.6/share/dbus-1/session.conf``` , Add this lines after EXTERNAL auth :
            <br/>```<auth>ANONYMOUS</auth>```
  			<br/>```<allow_anonymous/>```
    * Launch dbus agent : ```launchctl load ~/Library/LaunchAgents/org.freedesktop.dbus-session.plist```
    * Restart your system
* build erocci-dbus-java :
    * ```./dbus-java-install.sh```
    * ```make```
    * ```make run```
* For Erocci, use these instruction for Mac OSX : https://github.com/erocci/erocci
* Some links that may help : 
    * http://blog.roderickmann.org/2015/03/using-dbus-on-os-x-yosemite/
    * https://github.com/brianmcgillion/DBus/blob/master/README.launchd
* <b>Special usage with docker</b> :
	* Install docker-tools 1.8.3 (not the last version, a lot of api updates give error on docker connector after this version, this will be updated has soon as possible, stay tuned).
	* If you have El capitan, you must disable the apple root security added from this version of OS X, this is mandatory for making aliases on docker-machine and VBoxManage binary.
	* Aliases on binary :
		* ```sudo ln -s /usr/local/bin/VBoxManage /usr/bin/VBoxManage```
		* ```sudo ln -s /usr/local/bin/docker-machine /usr/bin/docker-machine```
	* Launch docker default virtual box machine
	* Launch erocci-dbus-java with parameter :
		* ```mvn exec:java -Dexec.mainClass="org.ow2.erocci.backend.BackendDBusService" -Dexec.args="docker"```
	* Launch Erocci :
		* Update core xml on Erocci with this file erocci/deps/erocci_core/priv/schemas/occi/occi-core.xml : [occi-core.xml](doc/occi-core.xml)
		Note that is necessary for adding infrastructure scheme to core directly, this is a hack for now because when Erocci will be updated, it may be possible to add multiple schema see [Erocci issue #36](https://github.com/erocci/erocci/issues/36)

		* ```./start.sh -c ../erocci-dbus-java/erocci-dbus-java.config```

## CI

Visit CI results on http://travis-ci.org/occiware/erocci-dbus-java

