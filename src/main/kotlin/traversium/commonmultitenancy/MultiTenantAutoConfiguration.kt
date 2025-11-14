package traversium.commonmultitenancy

import org.hibernate.cfg.AvailableSettings
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
@AutoConfiguration
class MultiTenantAutoConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    fun dataSource(dataSourceProperties: DataSourceProperties): DataSource {
        return dataSourceProperties.initializeDataSourceBuilder().build()
    }

    @Bean
    fun hibernatePropertiesCustomizer(
        connectionProvider: MultiTenantConnectionProvider<String>,
        tenantResolver: CurrentTenantIdentifierResolver<String>
    ): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { properties ->
            properties[AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER] = connectionProvider
            properties[AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER] = tenantResolver
        }
    }

    @Bean
    fun currentTenantIdentifierResolver(): CurrentTenantIdentifierResolver<String> {
        return object : CurrentTenantIdentifierResolver<String> {

            override fun resolveCurrentTenantIdentifier(): String {
                val tenant = TenantContext.getTenant()
                return tenant
            }

            override fun validateExistingCurrentSessions(): Boolean {
                return false
            }
        }
    }
    
    @Bean
    fun multiTenantConnectionProvider(
        dataSource: DataSource,
        flywayTenantMigration: FlywayTenantMigration
    ): MultiTenantConnectionProvider<String> {
        return SchemaBasedMultiTenantConnectionProvider(dataSource, flywayTenantMigration)
    }
}