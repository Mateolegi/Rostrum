package com.mateolegi.rostrum.constant;

/**
 * Contains the attributes that the JSON configuration file can have.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class ConfigurationFileConstants {

    /**
     * JSON filename
     */
    public static final String JSON_FILENAME = "rostrum.json";

    /**
     * Array of datasources parameters.
     */
    public static final String DATA_SOURCES = "data-sources";

    /**
     * Name to identify the connection.
     */
    public static final String PERSISTENCE_UNIT = "persistence-unit";

    /**
     * Database management system
     */
    public static final String DATABASE_CONNECTION = "db-connection";

    /**
     * Database host url
     */
    public static final String DATABASE_HOST = "host";


    /**
     * Database port
     */
    public static final String DATABASE_PORT = "port";

    /**
     * Database name
     */
    public static final String DATABASE_NAME = "database";

    /**
     * Database user
     */
    public static final String DATABASE_USER = "user";

    /**
     * Database password
     */
    public static final String DATABASE_PASSWORD = "password";

    /**
     * Database connection url
     */
    public static final String DATABASE_URL = "url";


    /**
     * Driver class name
     */
    public static final String DATABASE_DRIVER = "driver";

    /**
     * Packages where entities are located
     */
    public static final String ENTITY_PACKAGE = "entity-package";

    /**
     * Bcrypt configurations
     */
    public static final String BCRYPT = "bcrypt";

    /**
     * Hash iterations bcrypt
     */
    public static final String ITERATIONS = "iterations";

    /**
     * AES256 configurations
     */
    public static final String AES256 = "aes256";

    /**
     * AES256 secret key
     */
    public static final String SECRET_KEY = "secret-key";

    /**
     * AES256 salt
     */
    public static final String SALT = "salt";

    private ConfigurationFileConstants() {}
}
