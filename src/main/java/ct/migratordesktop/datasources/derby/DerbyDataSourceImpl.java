package ct.migratordesktop.datasources.derby;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import ct.migratordesktop.datasources.AbstractDataSource;
import ct.migratordesktop.util.ArrayDataModelFormatter;
import ct.migratordesktop.util.ArrayDataModell;
//import ct.migratordesktop.datasources.AbstractDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DerbyDataSourceImpl extends AbstractDataSource {

	@Autowired
	@Getter
	private DerbyDataSourceConfiguration dataSourceConfiguration;

	private JdbcTemplate jdbcTemplate;
	
	public List<String> getAllTableNamesFromExport() {
		List<String> ret = List.of();
		final var sql = "SELECT tablename  FROM sys.sysschemas s, sys.systables t WHERE s.schemaid = t.schemaid AND s.SCHEMANAME = 'APP'";

		try {
			ret = getJdbcTemplate().queryForList( sql, String.class );
		}
		catch ( Exception e ) {
			log.debug( "getAllTableNamesFromExport {}", e.getMessage() );
		}
		return ret;
	}
	
	
//	@Override
//	public String execute( String sql ) {
//		return execute(getJdbcTemplate(),sql);
//	}

//	@Override
//	@SneakyThrows
//	public Connection getConnection() {
//		return dataSourceConfiguration.dataSource().getConnection();
//	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		if (Objects.isNull( this.jdbcTemplate ))
			this.jdbcTemplate = new JdbcTemplate(dataSourceConfiguration.dataSource()); 
		return this.jdbcTemplate;
	}

	
	
//	@Override
//	public boolean isOracle() {
//		return false;
//	}

//	@Override
//	public int getCount( String tableName ) {
//		return getCount( getJdbcTemplate(), tableName );
//	}

//	public boolean existTable( String tableName ) {
//		var ret = 0;
//		final var sql = "SELECT count(*) \n" +
//			" FROM sys.sysschemas s, sys.systables t\n" +
//			" WHERE s.schemaid = t.schemaid\n" +
//			" AND t.TABLENAME = '÷table÷'\n".replace( "÷table÷", tableName.trim().toUpperCase() ) +
//			" AND s.SCHEMANAME = 'APP'\n";
//
//		try {
//			ret = getJdbcTemplate().queryForObject( sql, Integer.class );
//		}
//		catch ( Exception e ) {
//			log.debug( "existTable {}", e.getMessage() );
//		}
//		return ret > 0;
//	}


	

	@Override
	public String getDataSourceInfo() {
		return getDataSourceInfo(dataSourceConfiguration.dataSource(),DerbyDataSourceConfiguration.PREFIX);
	}
	public List<String> getTableNamesFromEcostatColumns() {
		List<String> ret = List.of();
		final var sql = "SELECT DISTINCT TABLE_NAME FROM APP.ECOSTAT_COLUMS where table_name not in('ECOSTAT_PRIMARY_KEYS','ECOSTAT_COLUMS')";
		try {
			ret = getJdbcTemplate().queryForList( sql, String.class );
		}
		catch ( Exception e ) {
			log.debug( "getExportableTableNames {}", e.getMessage() );
		}
		return ret;
	}


	@Override
	protected DataSource getDataSource() {
		return this.dataSourceConfiguration.dataSource();
	}
	public String exportCheck( ) {
		final var arrayDataModell = new ArrayDataModell();
		arrayDataModell.addHeader( "schemaname", "tablename", "count" );
		var ret = "ok";

		final var sql = "SELECT schemaname, tablename,\n" +
			"SYSCS_UTIL.SYSCS_CHECK_TABLE(schemaname, tablename)\n" +
			"FROM sys.sysschemas s, sys.systables t\n" +
			"WHERE s.schemaid = t.schemaid order by schemaname,tablename\n";

		var count = 0;
		try {
			try (final var conn = getDataSource().getConnection()) {
				try (final var st = conn.prepareStatement( sql )) {
					try (final var rs = st.executeQuery()) {
						while ( rs.next() ) {
							final var sql1 = "SELECT count(*) FROM " + A + rs.getString( 1 ) + A + "." + A + rs.getString( 2 ) + A;
							try (final var st1 = conn.prepareStatement( sql1 )) {
								try (final var rs1 = st1.executeQuery()) {
									if ( rs1.next() ) {
										count = rs1.getInt( 1 );
										log.trace( "schema:{} table:{} count:{}", rs.getString( 1 ), rs.getString( 2 ), count );
									}
								}
							}
							arrayDataModell.add( rs.getString( 1 ) ).add( rs.getString( 2 ) ).add( count );
						}
					}
				}
			}
			final var arrayDataModelFormatter = new ArrayDataModelFormatter( arrayDataModell );
			arrayDataModelFormatter.setColumnInfo( false )
				.setKeret( ArrayDataModelFormatter.KERET.SZOKOZ )
				.setHeaderCompress( false ).setBottomSeparator( false ).setTopSeparator( false );
			ret = arrayDataModelFormatter.getFormatted();
		}
		catch ( SQLException e ) {
			log.error( "exportCheck", e );
			ret = e.getMessage();
		}
		return ret;
	}

	//	@Value("${ecostat.username:app}")
	//	private String	ecostatUsername;
	//
	//	@Value("${ecostat.url:jdbc:derby:memory:ecostatDb;create=true}")
	//	private String	ecostatUrl;
	//
	//	@Value("${ecostat.password:app}")
	//	private String	ecostatPassword;
	//
	//	@Value("${ecostat.driverClassName:oracle.jdbc.driver.OracleDriver}")
	//	private String	ecostatDriverClassName;
	//
	//	@SneakyThrows
	//	public Connection getConnection() {
	//		Class.forName( ecostatDriverClassName );
	//		final Connection connection = DriverManager.getConnection( this.ecostatUrl, this.ecostatUsername, this.ecostatPassword );
	//		connection.setReadOnly( true );
	//		return connection;
	//	}
	//
	//	public String getConnInfo() {
	//		String ret = "";
	//		try (final var connection = getConnection();) {
	//			ret = getConnInfo( connection, "EcoStat" ).stream().collect( Collectors.joining( "\n" ) );
	//		}
	//		catch ( Exception e ) {
	//			log.error( e.getMessage(), e );
	//			ret += "" +
	//				"\necostatUrl            :" + ecostatUrl +
	//				"\necostatUsername       :" + ecostatUsername +
	//				"\necostatDriverClassName:" + ecostatDriverClassName + "\n";
	//			ret += "Error:" + e.getMessage();
	//		}
	//
	//		return ret;
	//	}
	//
	//	public int getCount( String tableName ) {
	//		Integer count = -1;
	//		if ( existTable( tableName ))
	//		try {
	//			try (final var conn = getConnection()) {
	//				try (final var stmt = conn.createStatement()) {
	//					final var sql = "select count(*) from " + tableName.toUpperCase();
	//					try (final var rs = stmt.executeQuery( sql )) {
	//						if ( rs.next() ) {
	//							count = rs.getInt( 1 );
	//						}
	//					}
	//				}
	//			}
	//		}
	//		catch ( Exception e ) {
	//			count = -1;
	//			log.error( "getCount {} {}", tableName, e.getMessage().trim() );
	//		}
	//		return count;
	//	}
	//
	//	public boolean existTable( String tableName ) {
	//		var count = 0;
	//		try {
	//			try (final var conn = getConnection()) {
	//				try (final var stmt = conn.createStatement()) {
	//					final var sql = "SELECT COUNT(TABLE_NAME) FROM USER_TABLES WHERE TABLE_NAME = '" + tableName.toUpperCase() + "'";
	//					try (final var rs = stmt.executeQuery( sql )) {
	//						if ( rs.next() ) {
	//							count = rs.getInt( 1 );
	//						}
	//					}
	//				}
	//			}
	//		}
	//		catch ( Exception e ) {
	//			count = -1;
	//			log.error( "existsTable {} {}", tableName, e.getMessage() );
	//		}
	//		return count == 1;
	//	}
	//
	//	@Override
	//	public String execute( String string ) {
	//		return null;
	//	}
	//
	//	@Override
	//	public boolean isOracle() {
	//		return true;
	//	}
	//
	//	@Override
	//	public void dropTable( String tableName ) {
	//		log.warn( "Nem adaható ki drop table {}", tableName );
	//	}

}