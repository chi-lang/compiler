#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

$GRAALVM_HOME/bin/java -cp $SCRIPT_DIR/build/libs/chi-all.jar gh.marad.chi.MainKt $@
