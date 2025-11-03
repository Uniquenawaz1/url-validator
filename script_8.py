
# Create Maven wrapper files
mvnw_content = """#!/bin/sh
# Maven Wrapper Script
echo "Downloading Maven Wrapper..."
mvn -N io.takari:maven:wrapper
"""

mvnw_cmd_content = """@echo off
REM Maven Wrapper for Windows
mvn -N io.takari:maven:wrapper
"""

with open(f"{base_path}/mvnw", "w") as f:
    f.write(mvnw_content)

with open(f"{base_path}/mvnw.cmd", "w") as f:
    f.write(mvnw_cmd_content)

print("Maven wrapper scripts created!")
