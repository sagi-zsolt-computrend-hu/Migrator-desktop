package ct.migratordesktop.datasources;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import ct.migratordesktop.util.ArrayDataModelFormatter;
import ct.migratordesktop.util.ArrayDataModell;
import jakarta.annotation.Nonnull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
public abstract class AbstractDataSource {
	protected static final String	T	= "\t";
	protected static final String	A	= "\"";

	//public abstract String execute( String string );

	public abstract String getDataSourceInfo();

	//public abstract Connection getConnection();

	public abstract JdbcTemplate getJdbcTemplate();

	//public abstract boolean isOracle();

	//	public abstract int getCount( String migratorTeszt );

	//public abstract boolean existTable( String tableName );

	//public abstract void dropTable( String tableName );

	protected abstract DataSource getDataSource();

	//	public abstract DataSourceConfiguration getDataSourceConfiguration();
	//	public JdbcTemplate getJdbcTemplate() {
	//		return new JdbcTemplate(getDataSource());
	//	}

	//	protected JdbcTemplate getJdbcTemplate() {
	//		return new JdbcTemplate( getDataSource() );
	//	}

	@SneakyThrows
	public Connection getConnection() {
		return getDataSource().getConnection();
	}

	public void dropTable( String tableName ) {
		if ( existTable( tableName ) )
			if ( isOracle() )
				execute( "DROP TABLE ÷÷ CASCADE CONSTRAINTS PURGE".replace( "÷÷", tableName ) );
			else
				execute( "DROP TABLE " + tableName );
	}

	protected Connection getConnection( JdbcTemplate jdbcTemplate ) {
		try {
			return jdbcTemplate.getDataSource().getConnection();
		}
		catch ( Exception e ) {
			log.error( "getConnection", e );
			return null;
		}
	}

	protected List<String> getConnInfo( Connection connection, String name ) {
		final var ret = new ArrayList<String>();
		ret.add( "Name:" + name );
		try {
			ret.add( " ProductVersion:" + connection.getMetaData().getDatabaseProductVersion().replace( "\r", "" ).replace( "\n", "" ) );
			ret.add( " Url:" + connection.getMetaData().getURL() + " UserName:" + connection.getMetaData().getUserName() );
			ret.add( " Driver:" + connection.getMetaData().getDriverName() + " " + connection.getMetaData().getDriverVersion()
				+ " AutoCommit:" + connection.getAutoCommit() + " Closed:" + connection.isClosed() + " ReadOnly:" + connection.isReadOnly() );
			ret.add( "\n" );
		}
		catch ( Exception e ) {
			log.error( "getConnInfo", e );
			ret.add( "Error:" + e.getMessage() );
		}
		return ret;
	}

	public String execute( /*JdbcTemplate jdbcTemplate,*/ @Nonnull String sql ) {
		final var trim = sql.replace( "\n", "" ).replace( "\t", "" ).replaceAll( "\\s{2,}", " " ).trim();
		try {
			getJdbcTemplate().execute( sql );
			log.debug( "Success execute:{}", trim );
			return "";
		}
		catch ( Exception e ) {
			log.error( "Error in execute:{} {}", e.getMessage(), trim );
			return e.getMessage();
		}
	}

	@SneakyThrows
	public boolean isMsSql() {
		return getJdbcTemplate().getDataSource().getConnection().getMetaData().getURL().contains( "sqlserver" );
	}

	public int getCount( String tableName ) {
		Integer count = 0;
		try {
			count = getJdbcTemplate().queryForObject( "select count(*) from " + tableName, Integer.class );
		}
		catch ( Exception e ) {
			count = -1;
			log.error( "getCount {} {}", tableName, e.getMessage() );
		}
		return count;
	}

	@SneakyThrows
	private boolean isTip( String tip ) {
		try (final var conn = getJdbcTemplate().getDataSource().getConnection()) {
			return conn.getMetaData().getURL().contains( tip );
		}
	}

	//@SneakyThrows
	public boolean isMemory() {
		return isTip( "memory" );
		//return getJdbcTemplate().getDataSource().getConnection().getMetaData().getURL().contains( "memory" );
	}

	//@SneakyThrows
	public boolean isOracle() {
		return isTip( "oracle" );
		//return getJdbcTemplate().getDataSource().getConnection().getMetaData().getURL().contains( "oracle" );
	}

	//@SneakyThrows
	public boolean isDerby() {
		return isTip( "derby" );
		//return getJdbcTemplate().getDataSource().getConnection().getMetaData().getURL().contains( "derby" );
	}

	public boolean existTable( String tableName ) {
		//		return true;
		var ret = 0;
		String sql = "";
		if ( isOracle() )
			sql = "SELECT count(*) FROM USER_TABLES WHERE TABLE_NAME = '÷table÷'".replace( "÷table÷", tableName.trim().toUpperCase() );
		if ( isDerby() )
			sql = "SELECT count(*) \n" +
				" FROM sys.sysschemas s, sys.systables t\n" +
				" WHERE s.schemaid = t.schemaid\n" +
				" AND t.TABLENAME = '÷table÷'\n".replace( "÷table÷", tableName.trim().toUpperCase() ) +
				" AND s.SCHEMANAME = 'APP'\n";
		try {
			ret = getJdbcTemplate().queryForObject( sql, Integer.class );
		}
		catch ( Exception e ) {
			log.debug( "existTable {}", e.getMessage() );
		}
		return ret > 0;
	}

	@SneakyThrows
	protected String getDataSourceInfo( DataSource dataSource, String prefix ) {
		String ret = "";
		final var modell = new ArrayDataModell().addHeader( List.of( prefix, "" ) );
		try {
			final var ds = (HikariDataSource)dataSource;
			try (final var conn = ds.getConnection()) {
				modell.add( "ProductVersion" ).add( conn.getMetaData().getDatabaseProductVersion().replace( "\r", "" ).replace( "\n", "" ) );
				modell.add( "Driver" ).add( conn.getMetaData().getDriverName() + " " + conn.getMetaData().getDriverVersion() );
				modell.add( "url - username" ).add( conn.getMetaData().getURL() + " UserName:" + conn.getMetaData().getUserName() );
				modell.add( "AutoCommit" ).add( conn.getAutoCommit() );
			}
			modell.add( "ConnectionTimeout" ).add( ds.getConnectionTimeout() + "" );
			modell.add( "ConnectionInitSql" ).add( ds.getConnectionInitSql() + "" );
			modell.add( "ConnectionTestQuery" ).add( ds.getConnectionTestQuery() + "" );
			//			modell.add( "DataSourceJNDI" ).add( ds.getDataSourceJNDI() + "" );
			//			modell.add( "HealthCheckProperties" ).add( ds.getHealthCheckProperties() + "" );
			//			modell.add( "DataSourceClassName" ).add( ds.getDataSourceClassName() + "" );
			//			modell.add( "DataSourceProperties" ).add( ds.getDataSourceProperties() + "" );
			//			modell.add( "DriverClassName" ).add( ds.getDriverClassName() + "" );
			modell.add( "KeepaliveTime" ).add( ds.getKeepaliveTime() + "" );
			modell.add( "IdleTimeout" ).add( ds.getIdleTimeout() + "" );
			modell.add( "InitializationFailTimeout" ).add( ds.getInitializationFailTimeout() + "" );
			modell.add( "IsolateInternalQueries" ).add( ds.isIsolateInternalQueries() + "" );
			modell.add( "LeakDetectionThreshold" ).add( ds.getLeakDetectionThreshold() + "" );
			modell.add( "MaxLifetime" ).add( ds.getMaxLifetime() + "" );
			modell.add( "MaximumPoolSize" ).add( ds.getMaximumPoolSize() + "" );
			modell.add( "MinimumIdle" ).add( ds.getMinimumIdle() + "" );
			modell.add( "PoolName" ).add( ds.getPoolName() + "" );
			modell.add( "ReadOnly" ).add( ds.isReadOnly() + "" );
			modell.add( "RegisterMbeans" ).add( ds.isRegisterMbeans() + "" );
			modell.add( "ScheduledExecutor" ).add( ds.getScheduledExecutor() + "" );
			modell.add( "ThreadFactory" ).add( ds.getThreadFactory() + "" );
			modell.add( "ValidationTimeout" ).add( ds.getValidationTimeout() + "" );

			modell.add( "HikariConfigMXBean.LeakDetectionThreshold" ).add( ds.getHikariConfigMXBean().getLeakDetectionThreshold() + "" );
			modell.add( "HikariConfigMXBean.MaximumPoolSize" ).add( ds.getHikariConfigMXBean().getMaximumPoolSize() + "" );
			modell.add( "HikariConfigMXBean.MaxLifetime" ).add( ds.getHikariConfigMXBean().getMaxLifetime() + "" );
			modell.add( "HikariConfigMXBean.MinimumIdle" ).add( ds.getHikariConfigMXBean().getMinimumIdle() + "" );
			modell.add( "HikariConfigMXBean.PoolName" ).add( ds.getHikariConfigMXBean().getPoolName() + "" );
			modell.add( "HikariConfigMXBean.ValidationTimeout" ).add( ds.getHikariConfigMXBean().getValidationTimeout() + "" );

			modell.add( "HikariPoolMXBean.TotalConnections" ).add( ds.getHikariPoolMXBean().getTotalConnections() + "" );
			modell.add( "HikariPoolMXBean.IdleConnections" ).add( ds.getHikariPoolMXBean().getIdleConnections() + "" );
			modell.add( "HikariPoolMXBean.ActiveConnections" ).add( ds.getHikariPoolMXBean().getActiveConnections() + "" );
			modell.add( "HikariPoolMXBean.ThreadsAwaitingConnection" ).add( ds.getHikariPoolMXBean().getThreadsAwaitingConnection() + "" );
		}
		catch ( Exception e ) {
			log.error( "getDataSourceInfo", e );
		}
		finally {
			final var arrayDataModelFormatter = new ArrayDataModelFormatter( modell );
			arrayDataModelFormatter.setColumnInfo( false ).setSorszamozas( false )
				.setKeret( ArrayDataModelFormatter.KERET.PROPERTY )
				.setTopSeparator( false )
				.setHeaderCompress( false ).setBottomSeparator( false ).setTopSeparator( false );
			ret = arrayDataModelFormatter.getFormatted();
		}
		return ret;
	}
}