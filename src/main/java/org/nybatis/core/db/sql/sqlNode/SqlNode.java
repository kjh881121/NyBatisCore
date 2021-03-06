package org.nybatis.core.db.sql.sqlNode;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.nybatis.core.db.datasource.DatasourceManager;
import org.nybatis.core.db.datasource.driver.DatabaseAttribute;
import org.nybatis.core.db.session.executor.GlobalSqlParameter;
import org.nybatis.core.db.session.executor.util.QueryParameter;
import org.nybatis.core.db.sql.sqlNode.element.RootSqlElement;
import org.nybatis.core.exception.unchecked.SqlConfigurationException;
import org.nybatis.core.exception.unchecked.SqlParseException;
import org.nybatis.core.util.StringUtil;
import org.nybatis.core.validation.Validator;
import org.nybatis.core.xml.node.Node;

public class SqlNode {

	private String               sqlId;
	private RootSqlElement       structuredSql;
	private SqlProperties        properties;
	private Map<String, SqlNode> keySqls = null;
	private Set<String>          environmentIds = new LinkedHashSet<>();
	private int                  sqlHash = 0;

	private static final Map<String, SqlNode> NULL_KEY_SQLS = new HashMap<>();

	public SqlNode( String sqlId, RootSqlElement structuredSql, String environment, Node inputXmlSql, Map<String,SqlNode> keySqls ) {

		this.sqlId         = sqlId;
		this.structuredSql = structuredSql;
		this.properties    = new SqlProperties( environment, inputXmlSql );
		this.keySqls       = Validator.nvl( keySqls, NULL_KEY_SQLS );

		addEnvironmentId( environment );

	}

	/**
	 * get sql string
	 *
	 * @param param	binding parameter
	 * @return	sql string made by parameter
	 */
	public String getText( QueryParameter param ) {
		return getText( param, false, false );
	}

	/**
	 * get sql string
	 *
	 * @param param		binding parameter
	 * @param isPage	true if yout want to build page sql.
	 * @param isCount	true if yout want to build count sql.
	 * @return	sql string made by parameter
	 */
    public String getText( QueryParameter param, boolean isPage, boolean isCount ) {

    	try {

    		String sql = structuredSql.toString( param );

			if( DatasourceManager.isExist( getEnvironmentId() ) ) {
				DatabaseAttribute envAttr = DatasourceManager.getAttributes( getEnvironmentId() );
				if( isPage ) {
					sql = String.format( "%s%s%s", envAttr.getPageSqlPre(), sql, envAttr.getPageSqlPost() );
				} else if( isCount ) {
					sql = String.format( "%s%s%s", envAttr.getCountSqlPre(), sql, envAttr.getCountSqlPost() );
				}
			}

			return StringUtil.compressEnter( sql );

    	} catch( SqlConfigurationException e ) {
    		throw new SqlConfigurationException( e, "Sql(id:{}) is not valid. {}\n{}", sqlId, e.getMessage(), structuredSql.toString() );
    	} catch( SqlParseException e ) {
    		throw new SqlParseException( e, "Sql(id:{}) is not valid. {}\n{}", sqlId, e.getMessage(), structuredSql.toString() );
    	}

    }

    public String getSqlId() {
    	return sqlId;
    }

	public void setSqlId( String sqlId ) {
		this.sqlId = sqlId;
	}

	public int getSqlHash() {
		return sqlHash;
	}

	public SqlNode setSqlHash( int sqlHash ) {
		this.sqlHash = sqlHash;
		return this;
	}

	public SqlNode setSqlHash( String sql ) {
		this.sqlHash = getHash( sql );
		return this;
	}

	private int getHash( String sql ) {
		return StringUtil.compressSpaceOrEnter( sql ).hashCode();
	}

	public boolean isSameSqlHash( String sql ) {
		return this.sqlHash == getHash( sql );
	}

	public void addEnvironmentId( String id ) {
		if( StringUtil.isNotEmpty(id) ) {
			environmentIds.add( id );
		}
	}

	/**
	 * Get environment id.
	 *
	 * if global default environment id is set by {@link GlobalSqlParameter#setDefaultEnvironmentId(String)}
	 * and it is one of multiple environments,
	 * global default environment is returned.
	 *
	 * @return environment id
	 */
    public String getEnvironmentId() {

		String globalDefaultEnvironmentId = GlobalSqlParameter.getDefaultEnvironmentId();

		if( environmentIds.isEmpty() ) return Validator.nvl( globalDefaultEnvironmentId, DatasourceManager.getDefaultEnvironmentId() );

		if( environmentIds.contains( globalDefaultEnvironmentId ) ) return globalDefaultEnvironmentId;

		return properties.getEnvironmentId();

    }

    public SqlProperties getProperties() {
    	return properties;
    }

	public Map<String, SqlNode> getKeySqls() {
		return keySqls;
	}

	public String toString() {
    	return String.format( "id  : [%s], %s\nSql : \n%s",
				sqlId,
				properties,
				structuredSql.toString()
		);
    }

	public String getSqlSkeleton() {
		return structuredSql.toString();
	}

	public void setMainId( String mainId ) {
		structuredSql.setMainId( mainId );
	}

	public boolean containsEnvironmentId( String environmentId ) {
		return environmentIds.contains( environmentId );
	}

}
