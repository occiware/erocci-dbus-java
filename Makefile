PROJECT = erocci-dbus-java

JNI_PATH ?= /usr/lib/jni

ifeq ($(wildcard $(JNI_PATH)/libunix-java.so),)
$(error "Can not find libunix-java.so in $$(JNI_PATH)/")
endif

MVN ?= $(shell which mvn)
ifeq ($(MVN),)
$(error "Missing maven command...")
endif

MVN_FLAGS = -Djava.library.path=$(JNI_PATH)

JAR = target/$(PROJECT)-$(FULL_VERSION).jar
SERVICE = org.ow2.erocci.backend.BackendDBusService

# Java D-Bus implementation does not handle kernel address
DBUS_SESSION_BUS_ADDRESS = $(shell printenv DBUS_SESSION_BUS_ADDRESS | sed -e 's/kernel:.*;//')
export DBUS_SESSION_BUS_ADDRESS

LD_LIBRARY_PATH += :$(JNI_PATH)
export LD_LIBRARY_PATH

all: $(JAR)

$(JAR):
	$(MVN) $(MVN_FLAGS) install

deps: deps_dbus_java
	$(install_dbus_java)
	$(MVN) install:install-file -Dfile=/usr/share/java/dbus-2.8.jar -DgroupId=net.windwards.3rdparty -DartifactId=dbus -Dversion=2.8 -Dpackaging=jar
	$(MVN) install:install-file -Dfile=/usr/share/java/dbus-bin-2.8.jar -DgroupId=net.windwards.3rdparty -DartifactId=dbus-bin -Dversion=2.8 -Dpackaging=jar
	$(MVN) install:install-file -Dfile=/usr/share/java/hexdump-0.2.jar -DgroupId=net.windwards.3rdparty -DartifactId=matthew-hexdump -Dversion=0.2 -Dpackaging=jar
	$(MVN) install:install-file -Dfile=/usr/share/java/unix-0.5.jar -DgroupId=net.windwards.3rdparty -DartifactId=matthew-unix -Dversion=0.5 -Dpackaging=jar
	$(MVN) install:install-file -Dfile=/usr/share/java/debug-enable-1.1.jar -DgroupId=net.windwards.3rdparty -DartifactId=matthew-debug -Dversion=1.1 -Dpackaging=jar
	./maven_install_deps.sh
	
ifneq ($(shell which apt-get),)
deps_dbus_java:
	sudo apt-get install dbus-java-bin
else
deps_dbus_java:
	@echo "Don't know how to install dbus-java-bin on your system..."; \
	false
endif

run: all
	$(MVN) exec:java $(MVN_FLAGS) -Dexec.mainClass="$(SERVICE)"

clean:
	$(MVN) clean

.PHONY: all run deps clean deps_dbus_java
