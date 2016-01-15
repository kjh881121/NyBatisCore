package org.nybatis.core.db.session.executor.util;

import org.nybatis.core.conf.Const;
import org.nybatis.core.db.session.executor.GlobalSqlParameter;
import org.nybatis.core.model.NDate;
import org.nybatis.core.model.NMap;
import org.nybatis.core.util.StringUtil;
import org.nybatis.core.util.TypeUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DbUtils {

    public static NMap getParameterMergedWithGlobalParam( Object parameter ) {

		NMap newParam = toNMapParameter( parameter );

		newParam.putAll( GlobalSqlParameter.getThreadLocalParameters() );

		return newParam;

	}

	public static NMap toNMapParameter( Object parameter ) {

		NMap param = new NMap();

		if( parameter == null ) return param;

		if( isPrimitive(parameter) ) {

			NMap singleParam = new NMap( );
			singleParam.put( Const.db.PARAMETER_SINGLE, parameter );

			param.fromBean( singleParam );

			return param;

		} else {

			NMap tempParam = new NMap();
			tempParam.fromBean( parameter );

		}

		return param;

	}

	public static boolean isPrimitive( Object object ) {
		return object == null || isPrimitive( object.getClass() );
	}

	public static boolean isPrimitive( Class<?> klass ) {

		if( klass == null ) return false;

		if( klass == String.class     ) return true;
		if( klass == int.class        ) return true;
		if( klass == Integer.class    ) return true;
		if( klass == double.class     ) return true;
		if( klass == Double.class     ) return true;
		if( klass == float.class      ) return true;
		if( klass == Float.class      ) return true;
		if( klass == long.class       ) return true;
		if( klass == Long.class       ) return true;
		if( klass == boolean.class    ) return true;
		if( klass == Boolean.class    ) return true;
		if( klass == byte.class       ) return true;
		if( klass == Byte.class       ) return true;
		if( klass == short.class      ) return true;
		if( klass == Short.class      ) return true;
		if( klass == BigDecimal.class ) return true;
		if( klass == BigInteger.class ) return true;
		if( klass == char.class       ) return true;
		if( klass == Character.class  ) return true;
		if( klass == byte[].class     ) return true;
		if( klass == Byte[].class     ) return true;
		if( klass == Date.class       ) return true;
		if( klass == Calendar.class   ) return true;
		if( klass == NDate.class      ) return true;
		if( klass == Object.class     ) return true;
		return TypeUtil.isArray( klass );

	}

	/**
	 * Get attrubite involving parameters
	 *
	 * <pre>
	 * NMap parameter = new NMap( "{'name':'abc', 'age':'2'}" );
	 *
	 * DbUtils.getParameterBindedValue( "1", parameter ) --> 1
	 * DbUtils.getParameterBindedValue( "#{name}", parameter ) --> abc
	 * DbUtils.getParameterBindedValue( "PRE #{age} POST", parameter ) --> PRE 2 POST
	 * </pre>
	 *
	 * @param value value text. if value has '#{..}', it is replaced by value of parameter.
	 *                 key of value is inner text of '#{..}' pattern.
	 * @param parameter parameter contains key and value
	 * @return
	 */
	public static String getParameterBindedValue( String value, Map parameter ) {

		Pattern pattern = Pattern.compile( "#\\{(.+?)\\}" );

		Matcher matcher = pattern.matcher( value );

		StringBuffer sb = new StringBuffer();

		while( matcher.find() ) {

			String key = matcher.group().replaceAll( "#\\{(.+?)\\}", "$1" );
			String val = StringUtil.nvl( parameter.get( key ) );

			matcher.appendReplacement( sb, val );
		}

		matcher.appendTail( sb );

		return sb.toString();

	}

}