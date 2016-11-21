package org.nybatis.core.db.configuration.builder;

import org.nybatis.core.conf.Const;
import org.nybatis.core.context.NThreadLocal;
import org.nybatis.core.file.FileUtil;
import org.nybatis.core.log.NLogger;
import org.nybatis.core.util.ClassUtil;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Database Configuration Builder
 *
 * @author nayasis@gmail.com
 *
 */
public class DatabaseConfigurator {

	/**
	 * Build Database Configration from given path<br><br>
	 *
	 * First it builds from given path itself.<br>
	 * but if given path dose not exist, it will build from basepath({@link org.nybatis.core.conf.Const.path#getConfigDatabase}) + given path.
	 *
	 * @param filePath 	given configuration path
	 * @param reload  	reload configuration
	 */
	public static void build( String filePath, boolean reload ) {

		filePath = FileUtil.nomalizeSeparator( filePath );

		if( FileUtil.notExists( filePath ) ) {

			try {

				String modifiedPath = Paths.get( Const.path.getConfigDatabase(), filePath ).toString();

				if( FileUtil.notExists( modifiedPath ) ) {
					NLogger.error( "Database configuration file is not exist.\n\t - in [{}]\n\t - in [{}]", filePath, modifiedPath );
					filePath = null;
				}

				filePath = modifiedPath;

			} catch( InvalidPathException e ) {
				NLogger.error( e, "Database configuration file is not exist.\n\t - in [{}]", filePath );
				filePath = null;
			}

		}

		new ConfigurationBuilder().readFrom( filePath, reload );

		// Delete temporary thread local key
		NThreadLocal.clear();

	}

	/**
	 * Build All Database Configration from files in dafault path<br><br>
	 *
	 * default path is DatabaseConfigurationPath({@link org.nybatis.core.conf.Const.path#getConfigDatabase})
	 *
	 * @param reload  	reload configuration
	 */
	public static void build( boolean reload ) {

		String dbConfDir = Const.path.toResourceName( Const.path.getConfigDatabase() );

		List<String> resourceNames = ClassUtil.findResources( dbConfDir + "/*.xml" );

		for( String resourceName : resourceNames ) {
			new ConfigurationBuilder().readFrom( resourceName, reload );
		}

	}

	/**
	 * Build Database Configration from given path<br><br>
	 *
	 * First it builds from given path itself.<br>
	 * but if given path dose not exist, it will build from basepath({@link org.nybatis.core.conf.Const.path#getConfigDatabase}) + given path.
	 *
	 * @param filePath given configuration path
	 */
	public static void build( String filePath ) {
		build( filePath, false );
	}

	/**
	 * Build All Database Configration from files in dafault path<br><br>
	 *
	 * default path is DatabaseConfigurationPath({@link org.nybatis.core.conf.Const.path#getConfigDatabase})
	 *
	 */
	public static void build() {
		build( false );
	}

}
