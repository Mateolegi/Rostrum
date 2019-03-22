package com.mateolegi.rostrum.constant;

import com.mateolegi.rostrum.DBProvider;
import com.mateolegi.rostrum.exception.DatabaseNotSupportedException;
import com.mateolegi.rostrum.exception.DriverNotFoundException;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseProvider {

    public static String DERBY = "derby";
    public static String H2 = "h2";
    public static String HSQLDB = "hsqldb";
    public static String MARIADB = "mariadb";
    public static String MYSQL = "mysql";
    public static String POSTGRES = "postgres";
    public static String ORACLE = "oracle";

    public static Map<String, DBProvider> PROVIDERS = Stream.of(new Object[][] {
            { DERBY, new DBProvider(DERBY, "org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:?database") },
            { H2, new DBProvider(H2, "org.h2.Driver", "jdbc:h2:?database") },
            { HSQLDB, new DBProvider(HSQLDB, "org.hsqldb.jdbcDriver", "jdbc:hsqldb:?database") },
            { MARIADB, new DBProvider(MARIADB, "org.mariadb.jdbc.Driver",
                    "jdbc:mariadb://?host:?port/?database") },
            { MYSQL, new DBProvider(MYSQL, "com.mysql.jdbc.Driver", "jdbc:mysql://?host:?port/?database") },
            { POSTGRES, new DBProvider(POSTGRES, "org.postgresql.Driver",
                    "jdbc:postgresql://?host:?port/?database") },
            { ORACLE, new DBProvider(ORACLE, "oracle.jdbc.driver.OracleDriver",
                    "jdbc:oracle:thin:@?host:?port:?database") }
    }).collect(Collectors.collectingAndThen(
            Collectors.toMap(data -> (String) data[0], data -> (DBProvider) data[1]),
            Collections::unmodifiableMap
    ));

    public static DBProvider getDatabaseProvider(String databaseProvider) {
        if (!PROVIDERS.containsKey(databaseProvider)) {
            throw new DatabaseNotSupportedException(databaseProvider + " is not supported.");
        }
        return PROVIDERS.get(databaseProvider);
    }

    public static void validateProvider(String clazz) {
        try {
            Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new DriverNotFoundException("Can't find class for " + clazz + ", add it to the dependencies.");
        }
    }
}
