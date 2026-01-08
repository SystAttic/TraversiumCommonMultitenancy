# Traversium Common Multitenancy

A Spring Boot library that provides schema-based multi-tenancy support for Kotlin/Java applications with automatic tenant context management and database schema isolation.

## Features

- Schema-based multi-tenancy with automatic tenant context management
- Thread-local tenant context for safe concurrent request handling
- Automatic database schema creation for new tenants
- Flyway integration for tenant-specific database migrations
- Spring Boot auto-configuration for easy setup
- HTTP interceptor for automatic tenant context cleanup
- Tenant ID sanitization utilities for database schema compatibility

## Requirements

- Java 17 or higher
- Spring Boot 3.5.7 or compatible version
- PostgreSQL database
- Maven

## Installation

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
    <version>1.2.0</version> <!-- Replace with the version you want -->
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

## Usage

### Basic Configuration

The library uses Spring Boot auto-configuration. Once added as a dependency, it will automatically configure multi-tenancy support.

### Setting Tenant Context

Set the tenant ID for the current request context:

```kotlin
import traversium.commonmultitenancy.TenantContext

// Set tenant for current thread
TenantContext.setTenant("tenant-123")

// Get current tenant
val currentTenant = TenantContext.getTenant() // Returns "tenant-123" or "public" if not set

// Clear tenant context
TenantContext.clear()
```

### Tenant ID Sanitization

Since tenant IDs may contain characters not allowed in database schema names (like hyphens), use the sanitization utilities:

```kotlin
import traversium.commonmultitenancy.TenantUtils

// Convert tenant ID to schema-safe format
val schemaName = TenantUtils.sanitizeTenantIdForSchema("tenant-123") // Returns "tenant_123"

// Convert back to original format
val tenantId = TenantUtils.desanitizeTenantIdFromSchema("tenant_123") // Returns "tenant-123"
```

### Automatic Schema Creation

The library automatically creates database schemas for new tenants when they make their first request. Each tenant gets an isolated schema with Flyway migrations applied.

### HTTP Request Handling

The `TenantInterceptor` automatically cleans up the tenant context after each request to prevent context leakage between requests.

## Architecture

### Components

- **TenantContext**: Thread-local storage for tenant ID, ensuring thread-safe tenant isolation
- **TenantInterceptor**: Spring MVC interceptor that cleans up tenant context after request completion
- **SchemaBasedMultiTenantConnectionProvider**: Manages database connections per tenant schema
- **FlywayTenantMigration**: Handles database migrations for tenant schemas
- **MultiTenantAutoConfiguration**: Spring Boot auto-configuration for multi-tenancy setup
- **TenantUtils**: Utility functions for tenant ID sanitization

### Database Schema Strategy

The library uses a schema-per-tenant approach:
- Each tenant has a separate database schema
- Schema names are derived from sanitized tenant IDs
- Flyway migrations are automatically applied to new tenant schemas
- Default schema is "public" when no tenant is set

## Development

### Building the Project

```bash
./mvnw clean install
```

### Running Tests

```bash
./mvnw test
```

## Versioning

This project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Current version: `1.3.0-SNAPSHOT`

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed list of changes.

## Release Workflow

For maintainers: See [USAGE.md](USAGE.md) for instructions on running the release workflow.

## License

This project is part of the Traversium platform.

## Author

Maja Razinger

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Hibernate Multi-Tenancy](https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html#multitenancy)
