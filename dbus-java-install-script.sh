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

mvn install:install-file -Dfile=/usr/share/java/dbus-2.8.jar -DgroupId=net.windwards.3rdparty -DartifactId=dbus -Dversion=2.8 -Dpackaging=jar

mvn install:install-file -Dfile=/usr/share/java/dbus-bin-2.8.jar -DgroupId=net.windwards.3rdparty -DartifactId=dbus-bin -Dversion=2.8 -Dpackaging=jar

mvn install:install-file -Dfile=/usr/share/java/hexdump-0.2.jar -DgroupId=net.windwards.3rdparty -DartifactId=matthew-hexdump -Dversion=0.2 -Dpackaging=jar

mvn install:install-file -Dfile=/usr/share/java/unix-0.5.jar -DgroupId=net.windwards.3rdparty -DartifactId=matthew-unix -Dversion=0.5 -Dpackaging=jar

mvn install:install-file -Dfile=/usr/share/java/debug-enable-1.1.jar -DgroupId=net.windwards.3rdparty -DartifactId=matthew-debug -Dversion=1.1 -Dpackaging=jar

