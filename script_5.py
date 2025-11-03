
# Create application.properties
app_properties = """# Server Configuration
server.port=8080

# Application Name
spring.application.name=URL Validator

# Logging
logging.level.root=INFO
logging.level.com.urlvalidator=DEBUG
"""

with open(f"{base_path}/src/main/resources/application.properties", "w") as f:
    f.write(app_properties)

print("application.properties created!")
