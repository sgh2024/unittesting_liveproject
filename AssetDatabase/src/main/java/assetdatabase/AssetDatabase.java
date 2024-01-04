package assetdatabase;

import liquibase.*;
import liquibase.database.*;
import liquibase.database.jvm.*;
import liquibase.exception.*;
import liquibase.resource.*;

import java.io.*;
import java.sql.*;

public class AssetDatabase {
    private final Liquibase liquibase;

    public AssetDatabase(Connection connection) throws DatabaseException {
        final var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

        liquibase = new Liquibase("assetdatabase/definition.yml", new ClassLoaderResourceAccessor(getClass().getClassLoader()), database);
    }

    public void latest() throws LiquibaseException {
        liquibase.update(new Contexts());
    }
}
