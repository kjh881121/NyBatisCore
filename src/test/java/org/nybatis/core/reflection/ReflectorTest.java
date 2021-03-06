package org.nybatis.core.reflection;

import org.adrianwalker.multilinestring.Multiline;
import org.nybatis.core.clone.Cloner;
import org.nybatis.core.db.constant.NullValue;
import org.nybatis.core.log.NLogger;
import org.nybatis.core.model.NDate;
import org.nybatis.core.model.NMap;
import org.nybatis.core.reflection.vo.*;
import org.nybatis.core.testModel.Link;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class ReflectorTest {

	@Test
	public void simpleTest() {

		NLogger.debug( Reflector.toJson( makeTestPerson(), true ) );

		String json = Reflector.toJson( makeTestPerson(), false );

		assertEquals( "{\"firstName\":\"Hwasu\",\"lastName\":\"Jung\",\"phone\":{\"code\":2,\"number\":\"322-3493\"},\"fax\":{\"code\":9999,\"number\":\"00100\"},\"phoneList\":[]}", json );

        Map map1 = Reflector.toMapFrom( json );

		NLogger.debug( map1 );

		Person p1 = Reflector.toBeanFrom( json, Person.class );

		NLogger.debug( Reflector.toString( p1 ) );

		Person p2 = Reflector.toBeanFrom( map1, Person.class );
		NLogger.debug( Reflector.toString( p2 ) );

		Map map2 = Reflector.toMapFrom( p2 );

		NLogger.debug( map2 );

	}

	private Person makeTestPerson() {

		Person p = new Person();

		p.firstName = "Hwasu";
		p.lastName  = "Jung";
		p.phone.code = 2;
		p.phone.number = "322-3493";
		p.fax.code = 9999;
		p.fax.number = "00100";

		return p;

	}

	@Test
	public void cloneTest() {

		Link link = new Link( new File("//NAS/emul/SuperFamicom/emulator/snes9x1.45 NK Custom/snes9x.exe") );

		link.setId( 123456 );
		link.setTitle( "Merong" );
		link.setGroupName( "Samurai Showdown" );

		NLogger.debug( link );

		link = link.clone();

		link.setId( null );
		link.setTitle( "Modified Merong !!" );

		NLogger.debug( link );

	}

	@Test
	public void singleQuoteTesst() {

		String jsonText = String.format( "{'path':'%s/target/classes/ibatis_config/sql'}", "MERONG");

		NLogger.debug( jsonText );

		Map<String, Object> map = Reflector.toMapFrom( jsonText );

		NLogger.debug( map      );

	}

	@Test
	public void objectMapperTest() throws ParseException {

		FromVo fromVo = new FromVo( "Hwasu", 39, "1977-01-22" );

		NLogger.debug( "fromVo : {}", Reflector.toJson( fromVo ) );

		Map map = Reflector.toMapFrom( fromVo );

		NLogger.debug( new NMap( map ).toDebugString() );

		NMap expectedMap = new NMap();

		expectedMap.put( "name", "Hwasu" );
		expectedMap.put( "age", 39 );
		expectedMap.put( "birth", "1977-01-22 00:00:00" );

		assertEquals( expectedMap, map, "convert bean to map" );

		ToVo bean = Reflector.toBeanFrom( map, ToVo.class );

		NLogger.debug( bean );

		ToVo expectedBean = new ToVo( "Hwasu", "1977-01-22" );
		expectedBean.setAge( 39 );

		assertEquals( expectedBean.toString(), bean.toString(), "convert map to bean" );

	}

	@Test
	public void mergeMapTest() {

		NMap fromNMap = new NMap();

		fromNMap.put( "name", "hwasu" );
		fromNMap.put( "age",  "20" );

		ToVo toVo = new ToVo();

		NLogger.debug( "before\n{}", fromNMap );

		Reflector.merge( fromNMap, toVo );

		NLogger.debug( "after\n{}", toVo );

		assertEquals( toVo.age, 40 );
		assertEquals( toVo.name, "hwasu" );
		assertNotNull( toVo.birth );

	}

	@Test
	public void simpleArrayTest() {

		List array = Arrays.asList( "A", "B", "C", "D", "E", 99 );

		String json = Reflector.toJson( array );

		assertEquals( json, "[\"A\",\"B\",\"C\",\"D\",\"E\",99]" );

		List arrayFromJson = Reflector.toListFromJson( json );

		assertEquals( arrayFromJson, array );

	}

	@Test
	public void copyTest() {

		Person person = new Person();

		person.firstName = "Hwasu";
		person.lastName  = "Jung";

		person.phone = new PhoneNumber( 0, "Phone-111-222-333" );
		person.fax   = new PhoneNumber( 0, "Fax-77948-22328" );
		person.phoneList.add( person.phone );
		person.phoneList.add( person.fax );

		System.out.println( person );

		Person clone = new Person();
		Reflector.copy( person, clone );

		System.out.println( clone );

		PersonAnother another = new PersonAnother();

		another.prefix = "testPrefix";

		Reflector.copy( person, another );

		System.out.println( another );

	}

	@Test
	public void mergeBeanTest() {

		Person person = new Person();

		person.firstName = "Hwasu";
		person.lastName  = "Jung";

		person.phone = new PhoneNumber( 0, "Phone-111-222-333" );
		person.fax   = new PhoneNumber( 0, "Fax-77948-22328" );

		PersonAnother another = new PersonAnother();

		another.prefix = "testPrefix";

		Reflector.merge( person, another );

		System.out.println( another );

		assertEquals( another.lastName, "Jung" );
		assertEquals( another.prefix, "testPrefix" );
		assertTrue( another.fax.equals( person.fax ) );

	}

	@Test
	public void nullTest() {

		NDate date = Reflector.toBeanFrom( null, NDate.class );

		NLogger.debug( date );

		Person person = Reflector.toBeanFrom( null, Person.class );

		NLogger.debug( person );


	}

	@Test
	public void variableNamedWithCharacterAndNumber() {
		String json = "{\"S01\":\"Y\",\"S02\":\"N\"}";
		TestVo testVo = Reflector.toBeanFrom( json, TestVo.class );
		assertEquals( testVo.toString(), json );
	}

	@Test
	public void nullValueTest() {

		int a = 0;
		assertTrue( a == NullValue.INTEGER );

		Integer b = 0;
		assertFalse( b == NullValue.INTEGER );

	}

	@Test
	public void setNybatisDbNullValueTest() {

		Person person = new Person();

		person.firstName = "HWASU";
		person.lastName  = "JUNG";
		person.age       = 0;
		person.weight    = 25L;
		person.birthDate = new Date();
		person.birthNDate = new NDate();

		person.profileNList.add( "key1", "val1" );
		person.profileNList.add( "key2", "val2" );

		assertTrue( person.age != NullValue.INTEGER, "age must be defined as Integer class." );

		NLogger.debug( Reflector.toJson( person ) );

		person.firstName         = NullValue.STRING;
		person.lastName          = NullValue.STRING;
		person.age               = NullValue.INTEGER;
		person.weight            = NullValue.LONG;
		person.phoneList         = NullValue.LIST;
		person.previousAddresses = NullValue.ARRAY_STRING;
		person.profileNList      = NullValue.NLIST;
		person.profileSet        = NullValue.SET;
		person.birthDate         = NullValue.DATE;
		person.birthNDate        = NullValue.NDATE;

		for( int i = 0; i < 10; i++ ) {
			NLogger.debug( "---------------------" );
			NLogger.debug( Reflector.toJson( person ) );
		}

		NLogger.debug( "---------------------" );

		Map<String, Object> map = Reflector.toMapFrom( person );

		assertEquals( map.get( "firstName" ).toString(), NullValue.STRING );
		assertEquals( map.get( "lastName" ).toString(), NullValue.STRING );
		assertEquals( map.get( "age" ).toString(), NullValue.STRING );
		assertEquals( map.get( "weight" ).toString(), NullValue.STRING );
		assertEquals( map.get( "phoneList" ).toString(), NullValue.STRING );
		assertEquals( map.get( "previousAddresses" ).toString(), NullValue.STRING );
		assertEquals( map.get( "profileNList" ).toString(), NullValue.STRING );
		assertEquals( map.get( "profileSet" ).toString(), NullValue.STRING );
		assertEquals( map.get( "birthDate" ).toString(), NullValue.STRING );
		assertEquals( map.get( "birthNDate" ).toString(), NullValue.STRING );

	}

	@Test
	public void parseJson() throws IOException {

		TestRes res = Reflector.toBeanFrom( validJson, TestRes.class );
		NLogger.debug( Reflector.toJson( res, true ) );

		try {

			Reflector.toBeanFrom( invalidJson, TestRes.class );
			assertFalse( true );

		} catch( Exception e ) {
			NLogger.debug( e );
		}

	}

	@Test
	public void populate() throws InvocationTargetException, IllegalAccessException {

		System.out.println( invalidJson );

		TestRes res = Reflector.toBeanFrom( validJson, TestRes.class );

		System.out.println( Reflector.toJson(  res, true ) );

		Map<String, Object> map = Reflector.toMapFrom( populationJson );

//		BeanUtils.populate( res, map );

		Cloner cloner = new Cloner();
//		cloner.shallowClone(  )

		Reflector.copy( map, res );

		System.out.println( Reflector.toJson( res, true ) );

	}

	/**
	{
		"name": "test",
		"count": 5,
		"date": "20140101T000000+0900",
		"etcProp": {},
		"subList": [{
			"name": "sub-1",
			"value": "sub-value-1"
		}, {
			"name": "sub-2",
			"value": "sub-value-2"
		}],
	    "sub" : {
		 "name": "sub-3",
		 "value": "sub-value-3"
	    }
	}
	 */
	@Multiline
	private String validJson;

	/**
	{
		"count": 7,
		"sub": {
			"value": "populated-3"
		}
	}
	 */
	@Multiline
	private String populationJson;

	/**
	{
		"name": "test",
		"count": 5,
		"date": {
			"type": "date/reg",
			"text": "20140101T000000+0900"
		},
		"etcProp": {},
		"subList": [{
			"name": "sub-1",
			"value": "sub-value-1"
		}, {
			"name": "sub-2",
			"value": "sub-value-2"
		}]
	}
	 */
	@Multiline
	private String invalidJson;

}
