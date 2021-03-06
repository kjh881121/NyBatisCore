package org.nybatis.core.db.sql.mapper.implement;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.nybatis.core.db.sql.mapper.SqlType;
import org.nybatis.core.db.sql.mapper.TypeMapperIF;

public class StringMapper implements TypeMapperIF<String>{

	@Override
    public void setParameter( PreparedStatement statement, int index, String param ) throws SQLException {
		statement.setString( index, param );
    }

	@Override
	public void setOutParameter( CallableStatement statement, int index ) throws SQLException {
		statement.registerOutParameter( index, SqlType.find(String.class).toCode() );
	}

	@Override
    public String getResult( ResultSet resultSet, String columnName ) throws SQLException {
	    return resultSet.getString( columnName );
    }

	@Override
    public String getResult( ResultSet resultSet, int columnIndex ) throws SQLException {
		return resultSet.getString( columnIndex );
    }

	@Override
    public String getResult( CallableStatement statement, int columnIndex ) throws SQLException {
	    return statement.getString( columnIndex );
    }

}
