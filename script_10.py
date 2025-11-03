
import shutil

# Create the zip file
zip_filename = "url-validator-complete"
shutil.make_archive(zip_filename, 'zip', base_path)

print(f"✅ Complete application packaged: {zip_filename}.zip")
print(f"\nProject includes:")
print("- Spring Boot backend with REST API")
print("- Beautiful frontend UI")
print("- Complete documentation")
print("- Maven configuration")
print("- Ready to run!")
