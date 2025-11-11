# Using TraversiumCommonMultitenancy Library

## Setup for Consumer Projects

### 1. Add GitHub Packages Repository

Add this to your consumer project's `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/SystAttic/TraversiumCommonMultitenancy</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

### 2. Add the Dependency

```xml
<dependency>
    <groupId>traversium</groupId>
    <artifactId>common-multitenancy</artifactId>
    <version>1.0.0</version> <!-- Replace with the version you want -->
</dependency>
```

### 3. Configure Authentication

GitHub Packages requires authentication even for public repositories.

Create or edit `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

### 4. Create GitHub Personal Access Token

1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token with `read:packages` permission
3. Use this token as the password in your `settings.xml`


## Running the Release Workflow

1. Go to your repository on GitHub
2. Click on "Actions" tab
3. Select "Release Maven Library" workflow
4. Click "Run workflow"
5. Select the `dev` branch
6. Enter the next SNAPSHOT version (e.g., `1.1.0-SNAPSHOT`)
7. Click "Run workflow"

The workflow will:
- Create a release version from the current SNAPSHOT
- Build and deploy to GitHub Packages
- Push the release to the `main` branch
- Create a Git tag
- Update `dev` branch with the next SNAPSHOT version
- Create a GitHub Release with notes