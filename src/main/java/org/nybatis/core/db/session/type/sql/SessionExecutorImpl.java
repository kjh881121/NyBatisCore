package org.nybatis.core.db.session.type.sql;

import org.apache.poi.ss.formula.functions.T;
import org.nybatis.core.db.session.executor.SqlBean;
import org.nybatis.core.db.session.executor.SqlExecutor;
import org.nybatis.core.db.sql.reader.SqlReader;
import org.nybatis.core.db.sql.repository.SqlRepository;
import org.nybatis.core.db.sql.sqlNode.SqlNode;
import org.nybatis.core.exception.unchecked.SqlConfigurationException;
import org.nybatis.core.model.NMap;
import org.nybatis.core.validation.Assertion;

/**
 * Session executor implements
 *
 * @author nayasis@gmail.com
 * @since 2015-09-12
 */
public class SessionExecutorImpl implements SessionExecutor {

    private SqlSessionImpl sqlSession;
    private SqlBean        sqlBean;

    public SessionExecutorImpl( SqlSessionImpl sqlSession ) {
        this.sqlSession = sqlSession;
    }

    public SessionExecutor sqlId( String id ) {
        return sqlId( id, null );
    }

    public SessionExecutor sqlId( String id, Object parameter ) {
        SqlNode sqlNode = SqlRepository.get( id );
        Assertion.isNotNull( sqlNode, new SqlConfigurationException( "There is no sql id({}) in repository.", id ) );
        sqlBean = new SqlBean( sqlNode, parameter );
        return this;
    }

    public SessionExecutor sql( String sql ) {
        return sql( sql, null );
    }

    public SessionExecutor sql( String sql, Object parameter ) {

        SqlNode sqlNode = new SqlReader().read( sqlSession.getProperties().getEnvironmentId(), sql );

        sqlBean = new SqlBean( sqlNode, parameter );

        return this;
    }

    private SqlExecutor getExecutor() {
        try {
            return new SqlExecutor( sqlSession.getToken(), sqlBean.init( sqlSession.getProperties() ) );
        } finally {
            sqlSession.initProperties();
        }
    }

    @Override
    public NMap select() {
        return getExecutor().select();
    }

    @Override
    public <T> T select( Class<T> returnType ) {
        return getExecutor().select( returnType );
    }

    @Override
    public ListExecutor list() {
        return new ListExecutorImpl( sqlSession, sqlBean );
    }

    @Override
    public int execute() {
        return getExecutor().update();
    }

    @Override
    public NMap call() {
        return getExecutor().call();
    }

    @Override
    public NMap call( Class<?>... listReturnTypes ) {
        return getExecutor().call( listReturnTypes );
    }

    @Override
    public <T> T call( Class<T> returnType, Class<?>... listReturnTypes ) {
        return getExecutor().call( returnType, listReturnTypes );
    }

    @Override
    public SessionExecutor setParameter( Object parameter ) {
        sqlBean.setParameter( parameter );
        return this;
    }

    @Override
    public SessionExecutor addParameter( Object parameter ) {
        sqlBean.addParameter( parameter );
        return this;
    }

    @Override
    public SessionExecutor addParameter( String key, Object value ) {
        sqlBean.addParameter( key, value );
        return this;
    }

    @Override
    public NMap getParameters() {
        return sqlBean.getInputParams();
    }

    @Override
    public String getDatabaseName() {
        return sqlBean.getDatasourceAttribute().getDatabase();
    }

}