package org.nybatis.core.db.orm.table.customAnnotationInsert;

import java.util.List;
import org.nybatis.core.db.configuration.builder.DatabaseConfigurator;
import org.nybatis.core.db.orm.table.customAnnotationInsert.entity.Person;
import org.nybatis.core.db.orm.table.customAnnotationInsert.entity.PersonProperty;
import org.nybatis.core.db.session.SessionManager;
import org.nybatis.core.db.session.type.orm.OrmSession;
import org.nybatis.core.log.NLogger;
import org.nybatis.core.reflection.Reflector;
import org.nybatis.core.reflection.core.JsonConverter;
import org.nybatis.core.reflection.mapper.NObjectSqlMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Column annotation test in ORM sql
 *
 * boolean   to  Y/N
 * Map,List  to  JsonText
 *
 * @author nayasis@gmail.com
 * @since 2017-11-27
 */
public class OrmTableInsertAnnotationTest {

    public static final String envirionmentId = "h2";

    @BeforeClass
    public void init() {
        DatabaseConfigurator.build( "/config/dbLogicTest/tableCreator/configTableCreation.xml");
    }

    @Test
    public void jsonTest() {

        JsonConverter jsonConverter = new JsonConverter( new NObjectSqlMapper() );

        String json = jsonConverter.toJson( getSampleData(), true );
        NLogger.debug( json );

        Person person = jsonConverter.toBeanFrom( json, Person.class );

        NLogger.debug( Reflector.toJson( person, true ) );

        // convert json from List (not list string)
        json  = "{\"id\" : \"01001\",\"name\" : \"Jhon Dow\",\"age\" : 21,\"used\" : \"Y\",\"property\" : [{\"id\":\"01001\",\"nation\":\"America\",\"location\":\"Delaware\"}] }";
        person = jsonConverter.toBeanFrom( json, Person.class );

        NLogger.debug( Reflector.toJson( person, true ) );

    }

    @Test
    public void test() {

        OrmSession<Person> session = SessionManager.openOrmSession( Person.class );
        session.table().drop().set();

        Person sampleData = getSampleData();
        NLogger.debug( Reflector.toJson( sampleData ) );

        session.insert( sampleData );
        List<Person> list = session.list().select();

        Assert.assertEquals( list.size(), 1 );
        NLogger.debug( list );

    }

    private Person getSampleData() {
        Person person = new Person( "01001", "Jhon Dow", 21  );
        person.getProperty().add( new PersonProperty( "01001", "America", "Delaware" ) );
        return person;
    }

}
