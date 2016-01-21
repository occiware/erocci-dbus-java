MVN ?= $(shell which mvn)

ifeq ($(MVN),)
$(error "Missing maven command...")
endif

JAR = target/erocci-dbus-java-1.0-SNAPSHOT.jar
SERVICE = "org.ow2.erocci.backend.BackendDBusService"

DBUS_SESSION_BUS_ADDRESS = $(shell printenv DBUS_SESSION_BUS_ADDRESS | sed -e 's/kernel:.*;//')
export DBUS_SESSION_BUS_ADDRESS

all: $(JAR)

$(JAR):
	$(MVN) install

run: all
	$(MVN) exec:java -Dexec.mainClass=$(SERVICE)

clean:
	$(MVN) clean


.PHONY: all run clean
