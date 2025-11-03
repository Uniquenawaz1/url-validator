#!/usr/bin/env sh
# Maven Wrapper (POSIX) - downloads takari wrapper if missing
MVNW_HOME="$(dirname "$0")/.mvn/wrapper"
WRAPPER_JAR="$MVNW_HOME/maven-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading maven wrapper jar..."
  mkdir -p "$MVNW_HOME"
  curl -fsSL -o "$WRAPPER_JAR" "https://repo1.maven.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar" || {
    echo "Failed to download maven wrapper jar. Install Maven or ensure curl is available."
    exit 1
  }
fi

exec java -jar "$WRAPPER_JAR" "$@"
#!/bin/sh
# Maven Wrapper Script
echo "Downloading Maven Wrapper..."
mvn -N io.takari:maven:wrapper
