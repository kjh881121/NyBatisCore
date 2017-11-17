package org.nybatis.core.db.sql.orm.sqlmaker;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.nybatis.core.db.datasource.DatasourceManager;
import org.nybatis.core.db.datasource.driver.DatabaseName;
import org.nybatis.core.db.sql.mapper.SqlType;
import org.nybatis.core.db.sql.orm.reader.EntityLayoutReader;
import org.nybatis.core.db.sql.orm.vo.Column;
import org.nybatis.core.db.sql.orm.vo.TableIndex;
import org.nybatis.core.db.sql.orm.vo.TableLayout;
import org.nybatis.core.db.sql.repository.TableLayoutRepository;
import org.nybatis.core.exception.unchecked.SqlConfigurationException;
import org.nybatis.core.util.StringUtil;

import static org.nybatis.core.db.datasource.driver.DatabaseName.*;

/**
 * ORM DDL SQL maker
 *
 * @author nayasis@gmail.com
 * @since 2017-11-16
 */
public class OrmTableSqlMaker {

    private String environmentId;

    public OrmTableSqlMaker( String environmentId ) {
        this.environmentId = environmentId;
    }

    private TableLayout getTableLayout( Class klass ) {
        try {
            return TableLayoutRepository.getLayout( environmentId, EntityLayoutReader.getTableName(klass) );
        } catch( SqlConfigurationException e ) {
            return null;
        }
    }

    private TableLayout getEntityLayout( Class klass ) {
        EntityLayoutReader reader = new EntityLayoutReader();
        return reader.getTableLayout( klass );
    }

    public String sqlCreateTable( TableLayout tableLayout ) {

        StringBuilder sql = new StringBuilder();

        sql.append( "CREATE TABLE " );
        if( isDatabase( MY_SQL, MARIA, SQLITE, H2, DERBY, HSQL, POSTGRE, SYBASE ) ) {
            sql.append( "IF NOT EXISTS " );
        }
        sql.append( tableLayout.getName() ).append( " (\n" );
        sql.append( toColumnString(tableLayout) ).append( "\n" );
        sql.append( ")" );

        return sql.toString();

    }

    private boolean isDatabase( DatabaseName... dbName ) {
        String database = DatasourceManager.getAttributes( environmentId ).getDatabase();
        for( DatabaseName name : dbName ) {
            if( database.equalsIgnoreCase(name.name) ) return true;
        }
        return false;
    }

    private boolean isNotDatabase( DatabaseName... dbName ) {
        return ! isDatabase( dbName );
    }

    private String toColumnString( TableLayout table ) {
        List<String> columns = new ArrayList<>();
        for( Column column : table.getColumns() ) {
            columns.add( toColumnString( column ) );
        }
        return StringUtil.join( columns, ",\n" );
    }

    private String toColumnString( Column column ) {

        // number_test   NUMBER(32,3) not null default 12

        StringBuilder sb = new StringBuilder();
        sb.append( '\t' );
        sb.append( column.getName() ).append( '\t' );

        int dataType = column.getDataType();
        switch( dataType ) {
            case Types.VARCHAR :
                if( isDatabase(ORACLE) ) {
                    sb.append( "VARCHAR2" );
                } else {
                    sb.append( SqlType.find(dataType).name );
                }
                sb.append( "(" ).append( column.getSize() ).append( ")" );
                break;
            case Types.DATE :
                if( isDatabase(H2) ) {
                    if( ! column.isDefinedByAnnotation() ) {
                        sb.append( "TIMESTAMP" );
                    }
                } else {
                    sb.append( SqlType.find(dataType).name );
                }
                break;
            case Types.NUMERIC :
            case Types.DECIMAL :
                sb.append( "NUMBER" );
                if( column.getSize() != null ) {
                    sb.append( "(" ).append( column.getSize() );
                    if( column.getPrecison() != null ) {
                        sb.append( "," ).append( column.getPrecison() );
                    }
                    sb.append( ")" );
                }
                break;
            default :
                sb.append( SqlType.find(dataType).name );
        }

        sb.append( " " );

        if( column.isNotNull() ) {
            sb.append( "NOT NULL " );
        }

        if( StringUtil.isNotEmpty(column.getDefaultValue()) ) {
            sb.append( "DEFAULT " );
            if( isNumeric(dataType) ) {
                sb.append( column.getDefaultValue() );
            } else {
                sb.append( "'" ).append( column.getDefaultValue() ).append( "'" );
            }
        }

        return sb.toString();

    }

    public String sqlDropColumn( Column column, TableLayout table ) {
        return String.format( "ALTER TABLE %s DROP (%s)", table.getName(), column.getName() );
    }

    public String sqlModifyColumn( Column column, TableLayout table ) {
        StringBuilder sb = new StringBuilder();
        sb.append( String.format("ALTER TABLE %s (\n", table.getName()) );
        sb.append( toColumnString( column ) );
        sb.append( "\n)" );
        return toString();
    }

    public String sqlDropTable( TableLayout tableLayout ) {
        return String.format( "DROP TABLE %s", tableLayout.getName() );
    }

    public String sqlDropPkIndex( TableLayout tableLayout ) {
        if( StringUtil.isEmpty(tableLayout.getPkName()) ) return null;
        return String.format( "ALTER TABLE %s DROP CONSTRAINT %s", tableLayout.getName(), tableLayout.getPkName() );
    }

    public String sqlAddPkIndex( TableLayout tableLayout ) {
        String pkName = tableLayout.getPkName();
        if( StringUtil.isEmpty(pkName) ) {
            pkName = "PK_" + tableLayout.getName();
        }
        return String.format( "ALTER TABLE %s ADD CONSTRAINT %s PRIMARY_KEY (%s)",
            tableLayout.getName(), tableLayout.getPkName(), StringUtil.join(tableLayout.getPkColumnNames(),",") );
    }

    public String sqlDropIndex( TableIndex index ) {
        return String.format( "DROP INDEX %s", index.getName() );
    }

    public String sqlCreateIndex( TableIndex index, TableLayout table ) {
        return String.format( "CREATE INDEX %s ON %s(%s)", index.getName(), table.getName(), StringUtil.join(index.getColumnNames(),",") );
    }

    private boolean isNumeric( int dataType ) {
        switch( dataType ) {
            case java.sql.Types.BIT :
            case java.sql.Types.TINYINT :
            case java.sql.Types.SMALLINT :
            case java.sql.Types.INTEGER :
            case java.sql.Types.BIGINT :
            case java.sql.Types.FLOAT :
            case java.sql.Types.REAL :
            case java.sql.Types.DOUBLE :
            case java.sql.Types.NUMERIC :
            case java.sql.Types.DECIMAL :
                return true;
        }
        return false;
    }

    private boolean isString( int dataType ) {
        switch( dataType ) {
            case java.sql.Types.VARCHAR :
            case java.sql.Types.CHAR :
                return true;
        }
        return false;
    }

}
