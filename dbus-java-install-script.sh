#!/bin/sh

#
# Copyright (c) 2015-2016 Linagora
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# sudo apt-get install dbus-java-bin

mvn install:install-file -Dfile="./lib/dbus-2.8.jar" -DgroupId=net.windwards.3rdparty -DartifactId=dbus -Dversion=2.8 -Dpackaging=jar

mvn install:install-file -Dfile="./lib/dbus-bin-2.8.jar" -DgroupId=net.windwards.3rdparty -DartifactId=dbus-bin -Dversion=2.8 -Dpackaging=jar

mvn install:install-file -Dfile="./lib/hexdump-0.2.jar" -DgroupId=net.windwards.3rdparty -DartifactId=matthew-hexdump -Dversion=0.2 -Dpackaging=jar

mvn install:install-file -Dfile="./lib/unix-0.5.jar" -DgroupId=net.windwards.3rdparty -DartifactId=matthew-unix -Dversion=0.5 -Dpackaging=jar

mvn install:install-file -Dfile="./lib/debug-enable-1.1.jar" -DgroupId=net.windwards.3rdparty -DartifactId=matthew-debug -Dversion=1.1 -Dpackaging=jar
# Install Eclipse OCL jars into the local repo.
mvn install:install-file -Dfile="./lib/org.eclipse.ocl.pivot-1.0.1.v20150908-1239.jar" -DpomFile="./lib/org.eclipse.ocl.pivot-1.0.1.v20150908-1239.pom"
mvn install:install-file -Dfile="./lib/org.eclipse.ocl.common-1.3.0.v20150519-0914.jar" -DpomFile="./lib/org.eclipse.ocl.common-1.3.0.v20150519-0914.pom"

mvn install:install-file -Dfile="./lib/docker-java-3.0.0-SNAPSHOT-jar-with-dependencies.jar"  -DgroupId="com.github.docker-java" -DartifactId="docker-java" -Dversion="3.0.0-SNAPSHOT" -Dpackaging=jar

mvn install:install-file -Dfile="./lib/org.occiware.clouddesigner.parent-0.1.0-SNAPSHOT.pom" -DgroupId=Clouddesigner -DartifactId=org.occiware.clouddesigner.parent -Dversion=0.1.0-SNAPSHOT -Dpackaging=pom
mvn install:install-file -Dfile="./lib/org.occiware.clouddesigner.occi-0.1.0-SNAPSHOT.jar" -DpomFile="./lib/org.occiware.clouddesigner.occi-0.1.0-SNAPSHOT.pom"
mvn install:install-file -Dfile="./lib/org.occiware.clouddesigner.occi.infrastructure-0.1.0-SNAPSHOT.jar"  -DgroupId="Clouddesigner" -DartifactId="org.occiware.clouddesigner.occi.infrastructure" -Dversion="0.1.0-SNAPSHOT" -Dpackaging=jar
mvn install:install-file -Dfile="./lib/org.occiware.clouddesigner.occi.docker-0.1.0-SNAPSHOT.jar"  -DgroupId="Clouddesigner" -DartifactId="org.occiware.clouddesigner.occi.docker" -Dversion="0.1.0-SNAPSHOT" -Dpackaging=jar
mvn install:install-file -Dfile="./lib/org.occiware.clouddesigner.occi.docker.connector-0.1.0-SNAPSHOT.jar"  -DgroupId="Clouddesigner" -DartifactId="org.occiware.clouddesigner.occi.docker.connector" -Dversion="0.1.0-SNAPSHOT" -Dpackaging=jar
mvn install:install-file -Dfile="./lib/org.occiware.clouddesigner.occi.docker.preference-0.1.0-SNAPSHOT.jar"  -DgroupId="Clouddesigner" -DartifactId="org.occiware.clouddesigner.occi.docker.preference" -Dversion="0.1.0-SNAPSHOT" -Dpackaging=jar
