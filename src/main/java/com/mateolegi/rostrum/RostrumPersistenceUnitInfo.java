package com.mateolegi.rostrum;

import com.mateolegi.rostrum.constant.PropertiesConstants;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.json.simple.JSONObject;
import org.reflections.Reflections;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static com.mateolegi.rostrum.constant.ConfigurationFileConstants.PERSISTENCE_UNIT;

/**
 * Implements the container and used by the persistence provider when creating an
 * {@link javax.persistence.EntityManagerFactory}.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class RostrumPersistenceUnitInfo implements PersistenceUnitInfo {

    public static String JPA_VERSION = "2.2";
    private JSONObject datasource;
    private String persistenceProviderClassName = PersistenceProvider.class.getName();

    public RostrumPersistenceUnitInfo() {
        datasource = Properties.getDataSource(null);
    }

    public RostrumPersistenceUnitInfo(String persistenceUnitName) {
        datasource = Properties.getDataSource(persistenceUnitName);
    }
    /**
     * Returns the name of the persistence unit. Corresponds to the <code>name</code> attribute in the
     * <code>persistence.xml</code> file.
     * @return the name of the persistence unit
     */
    @Override
    public String getPersistenceUnitName() {
        return (String) datasource.get(PERSISTENCE_UNIT);
    }

    /**
     * Returns the fully qualified name of the persistence provider implementation class. Corresponds to the
     * <code>provider</code> element in the <code>persistence.xml</code> file.
     * @return the fully qualified name of the persistence provider implementation class
     */
    @Override
    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    /**
     * Returns the transaction type of the entity managers created by the <code>EntityManagerFactory</code>. The
     * transaction type corresponds to the <code>transaction-type</code> attribute in the <code>persistence.xml</code>
     * file.
     * @return transaction type of the entity managers created by the EntityManagerFactory
     */
    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    /**
     * Returns the JTA-enabled data source to be used by the persistence provider. The data source corresponds to the
     * <code>jta-data-source</code> element in the <code>persistence.xml</code> file or is provided at deployment or
     * by the container.
     * @return the JTA-enabled data source to be used by the persistence provider
     */
    @Override
    public DataSource getJtaDataSource() {
        return null;
    }

    /**
     * Returns the non-JTA-enabled data source to be used by the persistence provider for accessing data outside a JTA
     * transaction. The data source corresponds to the named <code>non-jta-data-source</code> element in the
     * <code>persistence.xml</code> file or provided at deployment or by the container.
     * @return the non-JTA-enabled data source to be used by the persistence provider for accessing data outside a JTA
     * transaction
     */
    @Override
    public DataSource getNonJtaDataSource() {
        return null;
    }

    /**
     * Returns the list of the names of the mapping files that the persistence provider must load to determine the
     * mappings for the entity classes. The mapping files must be in the standard XML mapping format, be uniquely
     * named and be resource-loadable from the application classpath.  Each mapping file name corresponds to a
     * <code>mapping-file</code> element in the <code>persistence.xml</code> file.
     * @return the list of mapping file names that the persistence provider must load to determine the mappings for the
     * entity classes
     */
    @Override
    public List<String> getMappingFileNames() {
        return null;
    }

    /**
     * Returns a list of URLs for the jar files or exploded jar file directories that the persistence provider must
     * examine for managed classes of the persistence unit. Each URL corresponds to a <code>jar-file</code> element
     * in the <code>persistence.xml</code> file. A URL will either be a file: URL referring to a jar file or referring
     * to a directory that contains an exploded jar file, or some other URL from which an InputStream in jar format
     * can be obtained.
     * @return a list of URL objects referring to jar files or directories
     */
    @Override
    public List<URL> getJarFileUrls() {
        return null;
    }

    /**
     * Returns the URL for the jar file or directory that is the root of the persistence unit. (If the persistence unit
     * is rooted in the WEB-INF/classes directory, this will be the URL of that directory.) The URL will either be a
     * file: URL referring to a jar file or referring to a directory that contains an exploded jar file, or some other
     * URL from which an InputStream in jar format can be obtained.
     * @return a URL referring to a jar file or directory
     */
    @Override
    public URL getPersistenceUnitRootUrl() {
        return getClass().getProtectionDomain().getCodeSource().getLocation();
    }

    /**
     * Returns the list of the names of the classes that the persistence provider must add to its set of managed
     * classes. Each name corresponds to a named <code>class</code> element in the <code>persistence.xml</code> file.
     * @return the list of the names of the classes that the persistence provider must add to its set of managed
     * classes
     */
    @Override
    public List<String> getManagedClassNames() {
        String packages = (String) datasource.get(PropertiesConstants.JPA_ENTITY_PACKAGE);
        return new Reflections(packages)
                .getSubTypesOf(Object.class)
                .parallelStream()
                .map(Class::getName)
                .collect(Collectors.toList());
    }

    /**
     * Returns whether classes in the root of the persistence unit that have not been explicitly listed are to be
     * included in the set of managed classes. This value corresponds to the <code>exclude-unlisted-classes</code>
     * element in the <code>persistence.xml</code> file.
     * @return whether classes in the root of the persistence unit that have not been explicitly listed are to be
     * included in the set of managed classes
     */
    @Override
    public boolean excludeUnlistedClasses() {
        return false;
    }

    /**
     * Returns the specification of how the provider must use a second-level cache for the persistence unit.
     * The result of this method corresponds to the <code>shared-cache-mode</code> element in the
     * <code>persistence.xml</code> file.
     * @return the second-level cache mode that must be used by the provider for the persistence unit
     * @since Java Persistence 2.0
     */
    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.UNSPECIFIED;
    }

    /**
     * Returns the validation mode to be used by the persistence provider for the persistence unit.  The validation
     * mode corresponds to the <code>validation-mode</code> element in the <code>persistence.xml</code> file.
     * @return the validation mode to be used by the persistence provider for the persistence unit
     * @since Java Persistence 2.0
     */
    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.AUTO;
    }

    /**
     * Returns a properties object. Each property corresponds to a <code>property</code> element in the
     * <code>persistence.xml</code> file or to a property set by the container.
     * @return Properties object
     */
    @Override
    public java.util.Properties getProperties() {
        java.util.Properties properties = new java.util.Properties();
        properties.put("javax.persistence.jdbc.url", "jdbc:mariadb://localhost:3306/social_network");
        properties.put("javax.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
        properties.put("javax.persistence.jdbc.user", "root");
        properties.put("javax.persistence.jdbc.password", "1216");
        return properties;
    }

    /**
     * Returns the schema version of the <code>persistence.xml</code> file.
     * @return persistence.xml schema version
     * @since Java Persistence 2.0
     */
    @Override
    public String getPersistenceXMLSchemaVersion() {
        return JPA_VERSION;
    }

    /**
     * Returns ClassLoader that the provider may use to load any classes, resources, or open URLs.
     * @return ClassLoader that the provider may use to load any classes, resources, or open URLs
     */
    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Add a transformer supplied by the provider that will be called for every new class definition or class
     * redefinition that gets loaded by the loader returned by the {@link PersistenceUnitInfo#getClassLoader} method.
     * The transformer has no effect on the result returned by the {@link PersistenceUnitInfo#getNewTempClassLoader}
     * method. Classes are only transformed once within the same classloading scope, regardless of how many persistence
     * units they may be a part of.
     * @param transformer provider-supplied transformer that the container invokes at class-(re)definition time
     */
    @Override
    public void addTransformer(ClassTransformer transformer) {

    }

    /**
     * Return a new instance of a ClassLoader that the provider may use to temporarily load any classes, resources,
     * or open URLs. The scope and classpath of this loader is exactly the same as that of the loader returned by
     * {@link PersistenceUnitInfo#getClassLoader}. None of the classes loaded by this class loader will be visible to
     * application components. The provider may only use this ClassLoader within the scope of the
     * {@link javax.persistence.spi.PersistenceProvider#createContainerEntityManagerFactory} call.
     * @return temporary ClassLoader with same visibility as current loader
     */
    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }
}
