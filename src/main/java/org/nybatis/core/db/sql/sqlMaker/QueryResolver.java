package org.nybatis.core.db.sql.sqlMaker;

import com.jayway.jsonpath.InvalidPathException;
import org.nybatis.core.conf.Const;
import org.nybatis.core.db.datasource.DatasourceManager;
import org.nybatis.core.db.datasource.driver.DatabaseName;
import org.nybatis.core.db.sql.mapper.SqlType;
import org.nybatis.core.exception.unchecked.JsonPathNotFoundException;
import org.nybatis.core.exception.unchecked.SqlParseException;
import org.nybatis.core.model.NMap;
import org.nybatis.core.util.StringUtil;
import org.nybatis.core.validation.Assertion;

import java.util.*;

public class QueryResolver {

    private enum Comment { NONE, BLOCK, LINE }

    private   List<String>     binarySql   = new LinkedList<>();
    private   List<String>     binaryKeys  = new LinkedList<>();
    private   String           originalSql = "";

    private   Map<String, BindStruct> bindStructs = new HashMap<>();
    private   Map<String, BindParam>  bindParams  = new HashMap<>();

    public QueryResolver( String sql, NMap param ) {
        Assertion.isNotEmpty( sql, "SQL is empty." );
    	originalSql = sql;
        String dynamicSql = makeDynamicSql( sql, param );
        makeSql( dynamicSql, param );
    }

    public String getSql() {

    	StringBuilder sql = new StringBuilder();

        int index = 0;

    	for( String line : binarySql ) {

            if( line != null ) {

                // remaining unbind sentence "#{param...}" cause NPE when running executeUpdate().
                // 'statement.setEscapeProcessing( false )' can not avoid error because it cause another problem.
                // so remove remaining unbind sentence to avoid NPE.
                if( ! line.startsWith( "#{" ) || ! line.endsWith( "}" ) ) {
                    sql.append( line );
                }

                continue;
            }

            String key = binaryKeys.get( index++ );

            BindParam bindParam = bindParams.get( key );

            if( bindParam.getType() == SqlType.LIST ) {

                List params = (List) bindParam.getValue();

                List marks = new ArrayList<>();
                for( int i = 0; i < params.size(); i++ ) marks.add( "?" );

                sql.append( StringUtil.join( marks, "," ) );

            } else {
                sql.append( '?' );
            }

    	}

    	return sql.toString();


    }

    public String getOriginalSql() {
    	return originalSql;
    }

    public String getDebugSql() {
        return getDebugSql( null );
    }

    public String getDebugSql( String environmentId ) {

    	StringBuilder sql = new StringBuilder();

        DatabaseName dbName = DatasourceManager.getDatabaseName( environmentId );

        int index = 0;

    	for( String line : binarySql ) {
    		if( line == null ) {
                String key = binaryKeys.get( index++ );
    			sql.append( bindStructs.get( key ).toDebugParam( bindParams.get(key), dbName ) );
    		} else {
    			sql.append( line );
    		}
    	}

    	return sql.toString();
    }

    public List<BindParam> getBindParams() {

        List<BindParam> params = new ArrayList<>();

        for( String key : binaryKeys ) {
            params.add( bindParams.get( key ) );
        }

        return params;

    }

    public List<BindStruct> getBindStructs() {
        List<BindStruct> params = new ArrayList<>();

        for( String key : binaryKeys ) {
            params.add( bindStructs.get( key ) );
        }

        return params;
    }

    public static String makeDynamicSql( String query, NMap param ) {

        StringBuilder newQuery = new StringBuilder();

        int previousStartIndex = 0;

        while( true ) {

            int startIndex = getParamStartIndex( query, '$', previousStartIndex );

            if( startIndex == -1 ) break;

            int endIndex = getParamEndIndex( query, startIndex + 2 );

            String key = query.substring( startIndex + 2, endIndex ) ;
            int keyPropIndex = key.indexOf( ':' );
            if( keyPropIndex >= 0 ) key = key.substring( 0, keyPropIndex );

            newQuery.append( query.substring( previousStartIndex, startIndex ) );

            try {
                Object val = getValue( param, key );
                newQuery.append( val );
            } catch( JsonPathNotFoundException e ) {
                newQuery.append( String.format( "${%s}", key ) );
            }

            previousStartIndex = endIndex + 1;

        }

        newQuery.append( query.substring( previousStartIndex ) );

        return newQuery.toString();

    }

    private void makeSql( String sql, NMap param ) {

        QuotChecker quotChecker = new QuotChecker();

        int previousStartIndex = 0;

        while( true ) {

            int startIndex = getParamStartIndex( sql, '#', previousStartIndex, quotChecker );

            if( startIndex == -1 ) break;

            int endIndex = getParamEndIndex( sql, startIndex + 2 );

            String key = sql.substring( startIndex + 2, endIndex );

            String prevPhrase = sql.substring( previousStartIndex, startIndex );

            binarySql.add( prevPhrase );

            if( ! hasKey( param, key ) ) {
                bindParams.put( key, null );

            } else {

                BindStruct bindStruct = new BindStruct( key, quotChecker.isOn() );
                BindParam  bindParam  = null;

                try {
                    bindParam = bindStruct.toBindParam( getValue(param, bindStruct.getKey()) );
                } catch( JsonPathNotFoundException e ) {}

                bindStructs.put( key, bindStruct );
                bindParams.put( key, bindParam );

                if( bindParam.getType() == SqlType.LIST ) {

                    int index = 0;

                    for( Object o : (List) bindParam.getValue() ) {

                        String subKey = String.format( "%s_%d", key, index++ );

                        BindStruct bindStructSub = new BindStruct( subKey, bindStruct.getType(), bindStruct.isOut(), quotChecker.isOn() );
                        BindParam  bindParamSub  = bindStruct.toBindParam( o );

                        bindStructs.put( subKey, bindStructSub );
                        bindParams.put( subKey, bindParamSub );

                    }

                }

            }

            BindParam bindParam = bindParams.get( key );

            if( bindParam == null ) {
                binarySql.add( String.format( "#{%s}", key ) );

            } else if( bindParam.getType() == SqlType.LIST ) {

                int size = ( (List) bindParam.getValue() ).size();

                for( int i = 0; i < size; i++ ) {

                    String subKey = String.format( "%s_%d", bindParam.getKey(), i );

                    binaryKeys.add( subKey );

                    if( i != 0 ) binarySql.add( "," );

                    binarySql.add( null );

                }

            } else {

                binaryKeys.add( key );
                binarySql.add( null );

            }

            previousStartIndex = endIndex + 1;

        }

        binarySql.add( sql.substring( previousStartIndex ) );

    }

    private boolean hasKey( NMap param, String key ) {

        if( key.contains( ":" ) ) {
            key = key.substring( 0, key.indexOf( ":" ) );
        }

        if( param.containsKey( Const.db.PARAMETER_SINGLE ) ) return true;

        try {
            param.getByJsonPath( key );
            return true;
        } catch( JsonPathNotFoundException e ) {
            return false;
        } catch( InvalidPathException e ) {
            throw new SqlParseException( "Invalid json path(\"{}\") when searching parameter key.\n>> IN-Parameter :\n{}", key, param.toDebugString( true, true ) );
        }

    }

    private static Object getValue( NMap param, String key ) throws JsonPathNotFoundException {

        Object value;

        try {
            value = param.getByJsonPath( key );
        } catch( JsonPathNotFoundException e ) {
            value = param.getByJsonPath( Const.db.PARAMETER_SINGLE );
        }

        return value;

    }

    private static int getParamStartIndex( String sql, char findChar, int startIndex ) {
        return getParamStartIndex( sql, findChar, startIndex, null );
    }

    private static int getParamStartIndex( String sql, char findChar, int startIndex, QuotChecker quotChecker ) {

        Comment status = Comment.NONE;

        for( int i = startIndex, iCnt = sql.length() - 2; i < iCnt; i++ ) {

            char currC = sql.charAt( i );
            char nextC = sql.charAt( i + 1 );

            if( quotChecker != null ) {
                quotChecker.check( currC );
            }

            switch( status ) {

                case NONE :

                    if( currC == findChar && nextC == '{' ) return i;

                    // '/*' 형식의 주석시작지점 검색
                    if( currC == '/' && nextC == '*' ) {
                        status = Comment.BLOCK;
                        i++;

                    // '--' 형식의 주석시작지점 검색
                    } else if( currC == '-' && nextC == '-' ) {
                        status = Comment.LINE;
                        i++;

                    }

                    break;

                case BLOCK :

                    if( currC == '*' && nextC == '/' ) {
                        status = Comment.NONE;
                        i++;
                    }

                    break;

                case LINE :

                    if( currC == '\n' ) {
                        status = Comment.NONE;
                    }

                    break;

            }

        }

        return -1;

    }

    private static int getParamEndIndex( String sql, int startIndex ) {

        for( int i = startIndex, iCnt = sql.length(); i < iCnt; i++ ) {
            if( sql.charAt( i ) == '}' ) return i;
        }

        return sql.length();

    }

}
