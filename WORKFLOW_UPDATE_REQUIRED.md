# URGENT: Workflow Update Required

## Problem
The GitHub Actions workflow at `.github/workflows/maven-publish.yml` needs to be updated to fix build failures. The current workflow uses Java 11 but the project requires Java 17, and it's configured for GitHub Packages instead of Maven Central.

## Current Build Errors
1. `error: invalid target release: 17` - Wrong Java version
2. `Cannot invoke "org.apache.maven.settings.Server.clone()" because "server" is null` - Missing Maven Central configuration

## Required Changes

Replace the entire content of `.github/workflows/maven-publish.yml` with:

```yaml
# This workflow will build a package using Maven and then publish it to Maven Central when a release is created
# For more information see: https://github.com/actions/setup-java/blob/main/docs/advanced-usage.md#apache-maven-with-a-settings-path

name: Publish to Maven Central

on:
  release:
    types: [created]

jobs:
  publish:
    runs-on: ubuntu-latest
    permissions:
      contents: read

    steps:
    - name: Check out code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        server-id: central
        server-username: MAVEN_USERNAME
        server-password: MAVEN_PASSWORD
        gpg-private-key: ${{ secrets.GPG_PRIVATE_KEY }}
        gpg-passphrase: MAVEN_GPG_PASSPHRASE

    - name: Build with Maven
      run: mvn -B clean verify

    - name: Publish to Maven Central
      run: mvn -B deploy -Prelease
      env:
        MAVEN_USERNAME: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
        MAVEN_PASSWORD: ${{ secrets.MAVEN_CENTRAL_TOKEN }}
        MAVEN_GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
```

## How to Apply This Fix

### Option 1: Edit on GitHub (Recommended)
1. Go to: https://github.com/arun-prabhakar/toon4j/edit/claude/github-actions-maven-publish-011CUbS8wnFLpKRA6CVQgptk/.github/workflows/maven-publish.yml
2. Replace the entire file content with the above
3. Commit directly to the branch

### Option 2: Update Locally and Push
```bash
# Copy the workflow content above to .github/workflows/maven-publish.yml
# Then commit and push:
git add .github/workflows/maven-publish.yml
git commit -m "Update workflow for Maven Central with Java 17"
git push origin claude/github-actions-maven-publish-011CUbS8wnFLpKRA6CVQgptk
```

## Why This Couldn't Be Pushed Automatically

GitHub Apps require explicit `workflows` permission to modify workflow files (`.github/workflows/*`). This is a security feature. The Claude Code GitHub App currently doesn't have this permission, which is why this file must be updated manually.

## What This Fixes

1. **Java Version**: Updates from Java 11 to Java 17 to match project requirements
2. **Maven Central Configuration**: Adds proper server credentials setup for Maven Central publishing
3. **GPG Signing**: Configures GPG key for artifact signing
4. **Environment Variables**: Maps GitHub secrets to Maven credentials properly

Once this workflow is updated, the GitHub Actions build will succeed and be able to publish to Maven Central (after you've configured the required secrets - see PUBLISHING.md).
