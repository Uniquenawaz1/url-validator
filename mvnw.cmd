@echo off
REM Maven Wrapper for Windows - bootstraps the Takari Maven Wrapper if needed
SETLOCAL ENABLEDELAYEDEXPANSION
set MVNW_HOME=%~dp0.mvn\wrapper
set WRAPPER_JAR=%MVNW_HOME%\maven-wrapper.jar
if not exist "%WRAPPER_JAR%" (
	echo Downloading maven wrapper jar...
	powershell -Command "(New-Object System.Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar','%WRAPPER_JAR%')"
	if %ERRORLEVEL% neq 0 (
		echo Failed to download maven wrapper jar. Ensure internet access or install Maven.
		exit /b 1
	)
)

java -jar "%WRAPPER_JAR%" %*
ENDLOCAL
