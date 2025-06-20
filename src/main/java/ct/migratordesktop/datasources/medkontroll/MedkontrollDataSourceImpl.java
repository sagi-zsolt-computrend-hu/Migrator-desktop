package ct.migratordesktop.datasources.medkontroll;

import java.sql.Connection;
import java.util.Objects;

import javax.sql.DataSource;

import ct.migratordesktop.datasources.AbstractDataSource;
import ct.migratordesktop.datasources.export.ExportDataSourceConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MedkontrollDataSourceImpl extends AbstractDataSource {
	private JdbcTemplate jdbcTemplate;
	@Autowired
	@Getter
	private MedkontrollDataSourceConfiguration dataSourceConfiguration;
	@Override
	protected DataSource getDataSource() {
		return this.dataSourceConfiguration.dataSource();
	}

//	@Override
//	public String execute( String string ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Connection getConnection() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public boolean isOracle() {
//		// TODO Auto-generated method stub
//		return false;
//	}

//	@Override
//	public int getCount( String migratorTeszt ) {
//		// TODO Auto-generated method stub
//		return 0;
//	}

//	@Override
//	public boolean existTable( String tableName ) {
//		// TODO Auto-generated method stub
//		return false;
//	}

//	@Override
//	public void dropTable( String tableName ) {
//		// TODO Auto-generated method stub
//
//	}

//	@Override
//	public JdbcTemplate getJdbcTemplate() {
//		return new JdbcTemplate(dataSourceConfiguration.dataSource());
//	}
	@Override
	public JdbcTemplate getJdbcTemplate() {
		if (Objects.isNull( this.jdbcTemplate ))
			this.jdbcTemplate = new JdbcTemplate(dataSourceConfiguration.dataSource()) ;
		return this.jdbcTemplate;
	}

	@Override
	public String getDataSourceInfo() {
		return getDataSourceInfo(dataSourceConfiguration.dataSource(),MedkontrollDataSourceConfiguration.PREFIX);
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