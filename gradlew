#!/bin/bash
set -e

APP_BASE_NAME=$(basename "$0")
GRADLE_VERSION=8.2
GRADLE_ZIP="gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_DIR="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}"

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

if [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    mkdir -p "$HOME/.gradle/wrapper/dists"
    curl -fsSL -o "/tmp/$GRADLE_ZIP" "https://services.gradle.org/distributions/$GRADLE_ZIP"
    unzip -q -o "/tmp/$GRADLE_ZIP" -d "$HOME/.gradle/wrapper/dists/"
fi

exec "$GRADLE_DIR/bin/gradle" "$@"
