MVN ?= $(shell which mvn)

ifeq ($(MVN),)
$(error "Missing maven command...")
endif

JNI_PATH ?= /usr/lib/jni

ifeq ($(wildcard $(JNI_PATH)/libunix-java.so),)
$(error "Can not find libunix-java.so in $$(JNI_PATH)/")
endif

JAR = target/erocci-dbus-java-1.0-SNAPSHOT.jar
SERVICE = "org.ow2.erocci.backend.BackendDBusService"

DBUS_SESSION_BUS_ADDRESS = $(shell printenv DBUS_SESSION_BUS_ADDRESS | sed -e 's/kernel:.*;//')
export DBUS_SESSION_BUS_ADDRESS

LD_LIBRARY_PATH += :$(JNI_PATH)
export LD_LIBRARY_PATH

all: $(JAR)

$(JAR):
	$(MVN) install

run: all
	$(MVN) exec:java -Djava.library.path=$(JNI_PATH) -Dexec.mainClass=$(SERVICE)

clean:
	$(MVN) clean


.PHONY: all run clean
