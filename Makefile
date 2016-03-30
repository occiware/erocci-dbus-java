PROJECT = erocci-dbus-java

JNI_PATH ?= /usr/lib/jni

UNAME := $(shell uname)

$(info "UNAME: $(UNAME)")
ifeq ($(UNAME), Linux)
$(info "Linux platform detected")
ifeq ($(wildcard $(JNI_PATH)/libunix-java.so),)
$(error "Can not find libunix-java.so in $$(JNI_PATH)/")
endif
endif
ifeq ($(UNAME), Darwin)
 $(info "Mac OS X platform detected")
 JNI_PATH = $(JAVA_HOME)/jre/lib
ifeq ($(wildcard $(JNI_PATH)/libunix-java.dylib),)
$(error "Can not find libunix-java.dylib in $$(JNI_PATH)/")
endif
endif

$(info "JNI Path : $(JNI_PATH)")

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
$(info "LIBRARY path : $(LD_LIBRARY_PATH)")
all: $(JAR)

$(JAR):
	$(MVN) $(MVN_FLAGS) install

deps: deps_dbus_java
	$(install_dbus_java)
	./dbus-java-install-script.sh

ifneq ($(shell which apt-get),)
deps_dbus_java:
	@if [ ! -e /usr/share/java/dbus-bin.jar ]; then \
	  echo "INSTALL dbus-java-bin"; sudo apt-get install dbus-java-bin; \
	fi
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
