#!/bin/bash


$GRAALVM_HOME/bin/java \
  -agentlib:native-image-agent=config-output-dir=./config/linux,config-write-period-secs=30 \
  -cp build/libs/chi-all.jar \
  gh.marad.chi.MainKt \
  native/seed.chi $@
