@echo off
setlocal

if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java.exe"
)

rem Muda para o diretorio onde o script esta (onde esta o pom.xml)
cd /d "%~dp0"

set "WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar"

echo ========================================
echo  Controle Financeiro Pessoal
echo  Iniciando aplicacao...
echo  Diretorio: %cd%
echo ========================================

"%JAVA_EXE%" "-Dmaven.multiModuleProjectDirectory=%cd%" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain spring-boot:run

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERRO ao iniciar a aplicacao.
    echo Verifique se o Java 21+ esta instalado e o JAVA_HOME configurado.
    pause
)
