@echo off
setlocal

set WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%~dp0.mvn\wrapper\maven-wrapper.properties

if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java.exe
)

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%~dp0" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
