.PHONY: all shadow-jar native-config native clean install

SHELL := /bin/bash

GRAALVM_HOME ?= $(JAVA_HOME)
FAT_JAR := build/libs/chi-all.jar
CONFIG_DIR := config/linux
BINARY := chi
INSTALL_DIR := $(HOME)/.local/bin

all: native

# Build the uber JAR with all dependencies
shadow-jar:
	./gradlew shadowJar

$(FAT_JAR): shadow-jar

# Generate GraalVM native-image agent configuration
native-config: $(FAT_JAR)
	$(GRAALVM_HOME)/bin/java \
		--enable-native-access=ALL-UNNAMED \
		-agentlib:native-image-agent=config-output-dir=./$(CONFIG_DIR),config-write-period-secs=30 \
		-cp $(FAT_JAR) \
		gh.marad.chi.MainKt native/seed.chi

# Build the native image
native: $(FAT_JAR)
	$(GRAALVM_HOME)/bin/native-image \
		-H:+ReportExceptionStackTraces \
		--report-unsupported-elements-at-runtime \
		--initialize-at-run-time=party.iroiro \
		--enable-native-access=ALL-UNNAMED \
		--no-fallback \
		-Ob \
		-march=native \
		--gc=serial \
		-H:+UnlockExperimentalVMOptions \
		-H:ReflectionConfigurationFiles=$(CONFIG_DIR)/reflect-config.json \
		-H:JNIConfigurationFiles=$(CONFIG_DIR)/jni-config.json \
		-H:DynamicProxyConfigurationFiles=$(CONFIG_DIR)/proxy-config.json \
		-H:SerializationConfigurationFiles=$(CONFIG_DIR)/serialization-config.json \
		-H:ResourceConfigurationFiles=$(CONFIG_DIR)/resource-config.json \
		-cp $(FAT_JAR) \
		gh.marad.chi.MainKt \
		$(BINARY)

# Install the binary to ~/.local/bin
install: native
	mkdir -p $(INSTALL_DIR)
	cp $(BINARY) $(INSTALL_DIR)/$(BINARY)
	@echo "Installed $(BINARY) to $(INSTALL_DIR)/$(BINARY)"

clean:
	./gradlew clean
	rm -f $(BINARY)
