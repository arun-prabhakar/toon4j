# Publishing to Maven Central

This document describes how to publish the toon4j library to Maven Central using GitHub Actions.

## Prerequisites

Before you can publish to Maven Central, you need to:

1. **Register with Maven Central**: Create an account at https://central.sonatype.com/
2. **Generate a namespace**: Request verification for the `im.arun` namespace
3. **Generate credentials**: Create a user token from your Sonatype account
4. **Create a GPG key**: Generate a GPG key pair for signing artifacts

## Setup Instructions

### 1. Generate GPG Key

If you don't already have a GPG key, generate one:

```bash
gpg --full-generate-key
```

Select:
- Kind: RSA and RSA
- Key size: 4096 bits
- Expiration: 0 (never expires) or choose an appropriate period
- Enter your name and email

### 2. Export GPG Private Key

Export your private key in ASCII format:

```bash
gpg --list-secret-keys --keyid-format LONG
# Note the key ID from the output (e.g., rsa4096/ABCD1234)

gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc
```

### 3. Publish GPG Public Key

Your public key needs to be published to key servers:

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

### 4. Configure GitHub Secrets

Add the following secrets to your GitHub repository (Settings → Secrets and variables → Actions):

| Secret Name | Description | How to Get |
|------------|-------------|------------|
| `MAVEN_CENTRAL_USERNAME` | Your Maven Central username | From https://central.sonatype.com/ (Account → Generate User Token) |
| `MAVEN_CENTRAL_TOKEN` | Your Maven Central token | From https://central.sonatype.com/ (Account → Generate User Token) |
| `GPG_PRIVATE_KEY` | Your GPG private key | Content of the `private-key.asc` file created above |
| `GPG_PASSPHRASE` | Your GPG key passphrase | The passphrase you set when creating the GPG key |

**Important**: After adding the GPG_PRIVATE_KEY secret, securely delete the `private-key.asc` file:
```bash
shred -vfz -n 10 private-key.asc
```

## Publishing a Release

To publish a new version to Maven Central:

1. **Update the version** in `pom.xml`:
   ```xml
   <version>X.Y.Z</version>
   ```

2. **Commit and push** the version change:
   ```bash
   git add pom.xml
   git commit -m "Release version X.Y.Z"
   git push
   ```

3. **Create a GitHub release**:
   - Go to your repository on GitHub
   - Click "Releases" → "Create a new release"
   - Create a new tag (e.g., `v0.0.1`)
   - Fill in the release title and description
   - Click "Publish release"

4. The GitHub Actions workflow will automatically:
   - Build the project
   - Run tests
   - Sign the artifacts with GPG
   - Deploy to Maven Central
   - Auto-publish the release (due to `autoPublish: true` in pom.xml)

## Workflow Details

The GitHub Actions workflow (`.github/workflows/maven-publish.yml`) is triggered on release creation and performs these steps:

1. Checks out the code
2. Sets up JDK 17 with Maven Central credentials
3. Imports the GPG private key for signing
4. Builds and verifies the project (`mvn clean verify`)
5. Deploys to Maven Central with the `release` profile active (`mvn deploy -Prelease`)

The `release` profile in `pom.xml` enables GPG signing of all artifacts.

## Verifying Publication

After the workflow completes:

1. Check the Actions tab in GitHub to verify the workflow succeeded
2. Visit https://central.sonatype.com/publishing/deployments to check deployment status
3. Once published, your artifact will be available at:
   - https://central.sonatype.com/artifact/im.arun/toon4j
   - https://repo1.maven.org/maven2/im/arun/toon4j/

Note: It may take a few hours for the artifact to appear in Maven Central's search and sync to mirrors.

## Troubleshooting

### GPG Signing Fails
- Verify the `GPG_PRIVATE_KEY` secret contains the entire key including BEGIN/END markers
- Verify the `GPG_PASSPHRASE` is correct
- Check that your key hasn't expired

### Authentication Fails
- Verify your `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_TOKEN` are correct
- Ensure the token hasn't expired
- Check that you have the correct permissions on the namespace

### Deployment Fails
- Ensure all required metadata is present in `pom.xml` (name, description, url, licenses, developers, scm)
- Verify source and javadoc JARs are being generated
- Check that artifacts are properly signed

### Namespace Verification Issues
- Ensure you've completed namespace verification at https://central.sonatype.com/
- For GitHub-based verification, you may need to create a verification repository

## Manual Publishing (Local)

If you need to publish manually from your local machine:

1. Create a `~/.m2/settings.xml` file with your credentials:
   ```xml
   <settings>
     <servers>
       <server>
         <id>central</id>
         <username>YOUR_USERNAME</username>
         <password>YOUR_TOKEN</password>
       </server>
     </servers>
   </settings>
   ```

2. Run the Maven deploy command:
   ```bash
   mvn clean deploy -Prelease
   ```

## References

- [Maven Central Documentation](https://central.sonatype.org/publish/publish-guide/)
- [Central Portal](https://central.sonatype.com/)
- [GPG Documentation](https://www.gnupg.org/documentation/)
