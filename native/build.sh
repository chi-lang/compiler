#!/bin/bash

$GRAALVM_HOME/bin/native-image \
    -H:+ReportExceptionStackTraces \
    --report-unsupported-elements-at-runtime \
    --initialize-at-run-time=party.iroiro \
    --no-fallback \
    -Ob \
    -march=native \
    -H:+UnlockExperimentalVMOptions \
    -H:ReflectionConfigurationFiles=config/linux/reflect-config.json \
    -H:JNIConfigurationFiles=config/linux/jni-config.json \
    -H:DynamicProxyConfigurationFiles=config/linux/proxy-config.json \
    -H:SerializationConfigurationFiles=config/linux/serialization-config.json \
    -H:ResourceConfigurationFiles=config/linux/resource-config.json \
    -cp build/libs/chi-all.jar \
    gh.marad.chi.MainKt \
    chi
