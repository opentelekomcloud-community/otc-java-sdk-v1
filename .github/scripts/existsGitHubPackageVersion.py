#####################################################################
# Script checks for existence of given package in github repository
# 
# Inputs:
#   USERNAME:         The handle for the GitHub user account.
#   PACKAGE_TYPE:     The type of supported package
#   PACKAGE_NAME:     The name of the package
#   PACKACGE_VERSION: The version to check   
#   OUTPUT_NAME:      The name of the output variable to store result,
#                     defaults to: PACKAGE_EXISTS
#
# output
#  'true' or 'false' in GITHUB_OUTPUT
#
# to access output, use:
#   "${{ steps.[STEPNAME].outputs.PACKAGE_EXISTS }}"
#####################################################################
import os
import requests
import json

# Define the necessary variables
# see: https://docs.github.com/en/rest/packages/packages?apiVersion=2022-11-28#list-package-versions-for-a-package-owned-by-a-user
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN")
USERNAME = os.environ.get("USERNAME")
PACKAGE_TYPE = os.environ.get("PACKAGE_TYPE", "maven")
PACKAGE_NAME = os.environ.get("PACKAGE_NAME")
PACKAGE_VERSION_NAME = os.environ.get("PACKAGE_VERSION_NAME")
OUTPUT_NAME=os.environ.get("OUTPUT_NAME","PACKAGE_EXISTS")

HEADERS = {
    "Authorization": f"token {GITHUB_TOKEN}",
    "Accept": "application/vnd.github.v3+json",
}

url = f"https://api.github.com/users/{USERNAME}/packages/{PACKAGE_TYPE}/{PACKAGE_NAME}/versions"

# Make the GET request to the API
response = requests.get(url, headers=HEADERS)

exists = "false"

# Check if the request was successful
if response.status_code == 200:
    packages = json.loads(response.text)
    for package in packages:
        if package["name"] == PACKAGE_VERSION_NAME:
            exists = "true"
            break
else:
    print(f"response {response.text}")

# return 'true' or 'false' in GITHUB_OUTPUT
with open(os.environ["GITHUB_OUTPUT"], "a") as fh:
    fh.write(f"{OUTPUT_NAME}={exists}\n")
