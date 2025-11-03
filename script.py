
import os
import zipfile

# Create project structure
project_name = "url-validator-spring-boot"
base_path = project_name

# Create directory structure
directories = [
    f"{base_path}/src/main/java/com/urlvalidator/controller",
    f"{base_path}/src/main/java/com/urlvalidator/service",
    f"{base_path}/src/main/java/com/urlvalidator/model",
    f"{base_path}/src/main/resources",
    f"{base_path}/src/main/resources/static",
    f"{base_path}/frontend"
]

for directory in directories:
    os.makedirs(directory, exist_ok=True)

print("Project structure created successfully!")
print(f"Base path: {base_path}")
