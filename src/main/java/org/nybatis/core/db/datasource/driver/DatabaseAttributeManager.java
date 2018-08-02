package org.nybatis.core.db.datasource.driver;

import org.nybatis.core.exception.unchecked.DatabaseConfigurationException;
import org.nybatis.core.log.NLogger;
import org.nybatis.core.reflection.Reflector;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Database attribute Manager
 *
 * @author nayasis@gmail.com
 * @since 2015-10-29
 */
public class DatabaseAttributeManager {

    private static Map<String, DatabaseAttribute> driverRepository = new Hashtable<>();

    public static void add( DatabaseAttribute databaseAttribute ) {
        driverRepository.put( databaseAttribute.getDatabase(), databaseAttribute );
    }

    public static DatabaseAttribute get( DataSource datasource ) throws DatabaseConfigurationException {

        Connection  connection = null;

        try {

            connection = datasource.getConnection();

            return get( connection );

        } catch( SQLException e ) {
            throw new DatabaseConfigurationException( e );
        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                } catch( SQLException e ) {}
            }
        }

    }


    private static DatabaseAttribute get( Connection connection ) throws SQLException {

        Connection realConnection = Reflector.unwrapProxy( connection );

        String className = realConnection.getClass().getName();

        NLogger.trace( "---------------------------------------------------------------------" );
        NLogger.trace( "Connection class name : {}", className );
        NLogger.trace( "---------------------------------------------------------------------" );

        for( DatabaseAttribute attribute : driverRepository.values() ) {
            if( attribute.isMatched( className ) ) {
                NLogger.trace( attribute );
                return attribute.clone();
            }
        }

        if( ! driverRepository.containsKey( className ) ) {
            add( new DatabaseAttribute( className, className ) );
        }

        DatabaseAttribute databaseAttribute = driverRepository.get( className );

        NLogger.trace( databaseAttribute );

        return databaseAttribute.clone();

    }

    public static DatabaseAttribute get( String driverType ) {
        return driverRepository.get( driverType );
    }

    public static boolean contains( String driverType ) {
        return driverRepository.containsKey( driverType );
    }

    static {

        add( new DatabaseAttribute( DatabaseName.ORACLE )
            .setPageSqlPre( "SELECT * FROM ( SELECT ROWNUM AS nybatis_page_rownum, NYBATIS_PAGE_VIEW.* FROM (\n" )
            .setPageSqlPost( "\n) NYBATIS_PAGE_VIEW WHERE rownum <= #{end} ) WHERE nybatis_page_rownum >= #{start}" )
            .setPingQuery( "SELECT 1 FROM DUAL" )
        );

        add( new DatabaseAttribute( DatabaseName.MYSQL )
            .setPageSqlPost( "LIMIT #{offset}, #{count}" )
        );
        add( new DatabaseAttribute( DatabaseName.MARIA )
            .setPageSqlPost( "LIMIT #{offset}, #{count}" )
        );
        add( new DatabaseAttribute( DatabaseName.SQLITE )
            .setPageSqlPost( "LIMIT #{count} OFFSET #{offset}" )
        );

        add( new DatabaseAttribute( DatabaseName.H2 )
            .setPageSqlPost( "LIMIT #{count} OFFSET #{offset}" )
        );

        //   Not Tested  !
        add( new DatabaseAttribute( DatabaseName.DERBY )
            .setPageSqlPre( "SELECT * FROM ( SELECT ROWNUMBER() OVER() AS nybatis_page_rownum, NYBATIS_PAGE_VIEW.* FROM (\n" )
            .setPageSqlPost( "\n) NYBATIS_PAGE_VIEW WHERE rownum <= #{end} ) WHERE nybatis_page_rownum >= #{start}" )
        );
        add( new DatabaseAttribute( DatabaseName.HSQL )
            .setPageSqlPost( "LIMIT #{count} OFFSET #{offset}" )
        );
        add( new DatabaseAttribute( DatabaseName.MSSQL )
            .setPageSqlPre( "SELECT * FROM ( SELECT ROWNUM AS nybatis_page_rownum, NYBATIS_PAGE_VIEW.* FROM (\n" )
            .setPageSqlPost( "\n) NYBATIS_PAGE_VIEW WHERE rownum <= #{end} ) WHERE nybatis_page_rownum >= #{start}" )
        );
        add( new DatabaseAttribute( DatabaseName.POSTGRE )
            .setPageSqlPost( "LIMIT #{count} OFFSET #{offset}" )
        );
        add( new DatabaseAttribute( DatabaseName.SYBASE )
            .setPageSqlPre( "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS nybatis_page_rownum, NYBATIS_PAGE_VIEW.* FROM (\n" )
            .setPageSqlPost( "\n) NYBATIS_PAGE_VIEW WHERE rownum <= #{end} ) WHERE nybatis_page_rownum >= #{start}" )
        );
        add( new DatabaseAttribute( DatabaseName.DB2 )
            .setPageSqlPre( "SELECT * FROM ( SELECT ROWNUMBER() OVER() AS nybatis_page_rownum, NYBATIS_PAGE_VIEW.* FROM (\n" )
            .setPageSqlPost( "\n) NYBATIS_PAGE_VIEW WHERE rownum <= #{end} ) WHERE nybatis_page_rownum >= #{start}" )
        );
        add( new DatabaseAttribute( DatabaseName.ODBC ) );

    }

}
