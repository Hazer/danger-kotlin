export SHELL := $(shell echo $$SHELL)
TOOL_NAME = danger-kotlin
VERSION = 0.5.1

LOCAL_PREFIX = ~/.danger
PREFIX = /usr/local
LOCAL_BIN_PATH = $(LOCAL_PREFIX)/bin
LOCAL_INSTALL_PATH = $(LOCAL_BIN_PATH)/$(TOOL_NAME)
INSTALL_PATH = $(PREFIX)/bin/$(TOOL_NAME)
BUILD_PATH = danger-kotlin/build/bin/runner/releaseExecutable/$(TOOL_NAME).kexe
LOCAL_LIB_INSTALL_PATH = $(LOCAL_PREFIX)/lib
LIB_INSTALL_PATH = $(PREFIX)/lib/danger
TAR_FILENAME = $(TOOL_NAME)-$(VERSION).tar.gz

install: build
	mkdir -p $(PREFIX)/bin
	mkdir -p $(LIB_INSTALL_PATH)
	cp -f $(BUILD_PATH) $(INSTALL_PATH)
	cp -f danger-kotlin-library/build/libs/danger-kotlin.jar $(LIB_INSTALL_PATH)/danger-kotlin.jar

build:
	./gradlew shadowJar -p danger-kotlin-library
	./gradlew build -p danger-kotlin-kts
	./gradlew build -p danger-kotlin

uninstall:
	rm -rf $(INSTALL_PATH)
	rm -f $(LIB_INSTALL_PATH)/danger-kotlin.jar

install_local:
	./gradlew shadowJar -p danger-kotlin-library
	./gradlew build -p danger-kotlin-kts
	./gradlew build -p danger-kotlin
	mkdir -p $(LOCAL_BIN_PATH)
	mkdir -p $(LOCAL_LIB_INSTALL_PATH)
	cp -f $(BUILD_PATH) $(LOCAL_INSTALL_PATH)
	cp -f danger-kotlin-library/build/libs/danger-kotlin.jar $(LOCAL_LIB_INSTALL_PATH)/danger-kotlin.jar

run_local:
	@PATH=$(PATH):$(LOCAL_BIN_PATH); \
	DEBUG=true DANGER_KOTLIN_JAR=$(LOCAL_LIB_INSTALL_PATH)/danger-kotlin.jar danger-kotlin local

danger_ci: install_local
	@PATH=$(PATH):$(LOCAL_BIN_PATH); \
	DANGER_KOTLIN_JAR=$(LOCAL_LIB_INSTALL_PATH)/danger-kotlin.jar danger-kotlin ci