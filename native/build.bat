@echo off

%GRAALVM_HOME%\bin\native-image ^
    -H:+ReportExceptionStackTraces ^
    --report-unsupported-elements-at-runtime ^
    --initialize-at-run-time=party.iroiro ^
    --no-fallback ^
    -H:ReflectionConfigurationFiles=config/reflect-config.json ^
    -H:JNIConfigurationFiles=config/jni-config.json ^
    -H:DynamicProxyConfigurationFiles=config/proxy-config.json ^
    -H:SerializationConfigurationFiles=config/serialization-config.json ^
    -H:ResourceConfigurationFiles=config/resource-config.json ^
    -cp build\libs\chi-all.jar ^
    gh.marad.chi.MainKt ^
    chi