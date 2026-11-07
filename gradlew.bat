@echo off
setlocal

set GRADLE_VERSION=8.2
set GRADLE_DIR=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%

if not exist "%GRADLE_DIR%\bin\gradle.bat" (
    echo Downloading Gradle %GRADLE_VERSION%...
    powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%TEMP%\gradle.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\gradle.zip' -DestinationPath '%USERPROFILE%\.gradle\wrapper\dists\'"
)

call "%GRADLE_DIR%\bin\gradle.bat" %*
