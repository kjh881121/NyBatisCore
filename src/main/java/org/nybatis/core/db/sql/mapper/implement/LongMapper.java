package org.nybatis.core.db.sql.mapper.implement;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.nybatis.core.db.sql.mapper.SqlType;
import org.nybatis.core.db.sql.mapper.TypeMapperIF;

public class LongMapper implements TypeMapperIF<Long>{

	@Override
    public void setParameter( PreparedStatement statement, int index, Long param ) throws SQLException {
		statement.setLong( index, param );
    }

	@Override
	public void setOutParameter( CallableStatement statement, int index ) throws SQLException {
		statement.registerOutParameter( index, SqlType.find(Long.class).toCode() );
	}

	@Override
    public Long getResult( ResultSet resultSet, String columnName ) throws SQLException {
	    return resultSet.getLong( columnName );
    }

	@Override
    public Long getResult( ResultSet resultSet, int columnIndex ) throws SQLException {
		return resultSet.getLong( columnIndex );
    }

	@Override
    public Long getResult( CallableStatement statement, int columnIndex ) throws SQLException {
	    return statement.getLong( columnIndex );
    }

}
