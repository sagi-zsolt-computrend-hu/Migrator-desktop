package ct.migratordesktop;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import ct.migratordesktop.datasources.derby.DerbyDataSourceConfiguration;
import ct.migratordesktop.datasources.ecostat.EcostatDataSourceConfiguration;
import ct.migratordesktop.datasources.medkontroll.MedkontrollDataSourceConfiguration;
import ct.migratordesktop.util.ArrayDataModelFormatter;
import ct.migratordesktop.util.ArrayDataModell;
import ct.migratordesktop.util.SelectModell;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcAccessor;

@Slf4j
//@Component
public class Runner implements CommandLineRunner {
	protected static final String				T	= "\t";
	protected static final String				A	= "\"";

	//	@Autowired
	//	ArchiveRepository								archiveRepository;

//	@Autowired
//	EcostatColumnsRepository						ecostatColumnsRepository;
//
	@Autowired
	DerbyDataSourceConfiguration				derbyDataSourceConfiguration;

	@Autowired
	EcostatDataSourceConfiguration			ecostatDataSourceConfiguration;

	@Autowired
	MedkontrollDataSourceConfiguration	medkontrollDataSourceConfiguration;

//	@Autowired
//	AkkorDataSourceConfiguration				akkorDataSourceConfiguration;

	
	//@Autowired FelhasznaloService felhasznaloService;
	
//@Autowired
//private IntezmenyService intezmenyService;
	
	
	//	@Autowired
	//  JdbcTemplate jdbcTemplate;
	//
	//@Autowired
	//  private NamedParameterJdbcTemplate jdbcTemplate1;

	//@Autowired
	//	@org.springframework.beans.factory.annotation.Autowired(required=true)
	//	private JdbcTemplate jdbcTemplate;

	@Override
	public void run( String... args ) throws Exception {
		

//		var f1 = new SaveFelhasznaloRequest();
//		f1.setFelhasznalonev( "lkjélkjéklj" );
//		f1.setBeosztas( "asdasdasd" );
//		f1.setErvenyessegKezdete(LocalDateTime.now());
//		f1.setEmail( "íyÍY@ggh.hu" );
//		f1.setTelefonszam( "9878979" );
//		f1.setNev( "++++12" );
//		felhasznaloService.saveFelhasznalo( f1, 2L );
//		
		
//	var fi = new IntezmenyListaRequest();
//	//fi.getMainFilter().setIranyitoszam( new FilterIntegerValue( 6500 ) );
//	fi.setPaginatorEnabled( false );
//	fi.setOrder( new IntezmenyListaOrder() );
//	var lr = intezmenyService.findByFilter( fi  );
//	System.out.println( "Intezmeny" + lr.getRowCount() );
////	lr.getListElements().forEach(System.out::println);
//	//
//	var xx = new SaveIntezmenyRequestDTO();
//	//
//	var jdbctAkkor = new JdbcTemplate(akkorDataSourceConfiguration.dataSource());
//	//
//	var dokuTarLis= jdbctAkkor.queryForList( "select id from doku_tar", Long.class);
//	System.out.println( "dokuTarLis" + dokuTarLis.size() );
//	
//	record EcostatColums (Long id , String table_name,String column_name) {}
//	
//	
////	intezmenyService.saveIntezmeny( xx.toDomain(), 2L );
//	

	
	//RowMapper<EcostatColums> xxxx;
	
	
	
	
	xxx("MK_A%");
	
//	for ( Map<String, Object> row : xxxList ) {
//		Object e= row.get("sss");
//	}
	
 
		
		
		//		final var aPay = archiveRepository.getPayments();
		//		final var cPay = centralRepository.getPayments();
		//final var axPay = archiveRepository.getPaymentById( 1 );
//		var exportConn = exportDataSourceConfiguration.dataSource().getConnection();
//		var ecostatConn = ecostatDataSourceConfiguration.dataSource().getConnection();
//		var medkontrollConn = medkontrollDataSourceConfiguration.dataSource().getConnection();
//		medkontrollConn.setReadOnly( true );
//		var akkorConn = akkorDataSourceConfiguration.dataSource().getConnection();

		//		var exdsc = (HikariConfig)exportDataSourceConfiguration;
		//		var ecdsc = (HikariDataSource)ecostatDataSourceConfiguration.dataSource();

//		var exds = (HikariDataSource)exportDataSourceConfiguration.dataSource();
//		var ecds = (HikariDataSource)ecostatDataSourceConfiguration.dataSource();
//
//		exds.getHikariConfigMXBean().getPoolName();
//		ecds.getHikariConfigMXBean().getPoolName();

		//var xx = ecostatColumnsRepository.getTableNamesFromEcostatColumns();
		//medkontrollDataSourceConfiguration.dataSource();
		//archiveDataSourceConfiguration.jdbcTemplate().execute( "delete from ECOSTAT_COLUMS where id > 4000" );
//		HikariConfig config = new HikariConfig();
		//		var namedjdbcTemplate = new NamedParameterJdbcTemplate( archiveDataSourceConfiguration.dataSource() );
		/*
				var jdbcTemplate = new JdbcTemplate( exportDataSourceConfiguration.dataSource() );
				jdbcTemplate.execute( "delete from ECOSTAT_COLUMS where id > 4000" );
		
				for ( int i = 5000; i < 6000; i++ ) {
					EcostatColumns rec = EcostatColumns.builder().id( Long.parseLong( i + "" ) ).columnId( 22 ).columnName( "yíx" )
						.tableName( "dddddd" ).dataDefault( "sda" ).nullable( A )
						.dataLength( 22 ).dataPrecision( 2 ).dataScale( 3 ).dataType( "ss" ).build();
					ecostatColumnsRepository.insert( rec );
		
				}
		
				final var ecostatColumnsList = ecostatColumnsRepository.getAll();
		
				jdbcTemplate.execute( "delete from ECOSTAT_COLUMS where id > 4000" );
		*/

//		System.out.println( getDataSourceInfo( ecostatDataSourceConfiguration.dataSource(), "ecostat" ).stream().collect( Collectors.joining( "\n" ) ) );
//		System.out.println();
//		System.out.println( getDataSourceInfo( exportDataSourceConfiguration.dataSource(), "export" ).stream().collect( Collectors.joining( "\n" ) ) );
//		System.out.println();
//		System.out.println( getDataSourceInfo( medkontrollDataSourceConfiguration.dataSource(), "medkontroll" ).stream().collect( Collectors.joining( "\n" ) ) );
//		System.out.println();
//		System.out.println( getDataSourceInfo( akkorDataSourceConfiguration.dataSource(), "akkor" ).stream().collect( Collectors.joining( "\n" ) ) );

		//		System.out.println( exportCheck( jdbcTemplate ) );
		//		System.out.println( getConnInfo( ecostatConn, "ecostat" ).stream().collect( Collectors.joining( "\n" ) ) );
		//		System.out.println( getConnInfo( exportConn, "export" ).stream().collect( Collectors.joining( "\n" ) ) );
		//		System.out.println( getConnInfo( medkontrollConn, "medkontroll" ).stream().collect( Collectors.joining( "\n" ) ) );
		//		System.out.println( getConnInfo( akkorConn, "akkor" ).stream().collect( Collectors.joining( "\n" ) ) );
	//System.exit( 0 );
	}

	private void xxx( Object... params ) {//new Object[]{"MK%"}
		var jdbctExport = new JdbcTemplate(derbyDataSourceConfiguration.dataSource());
		var selectModell = new SelectModell(jdbctExport, "select * from ecostat_colums where table_name like ?" )
			.select( params );
		
		for ( var rowNum = 0; rowNum < selectModell.getRowCount(); rowNum++ ) {
			var COLUMN_NAME = selectModell.getCell( rowNum, "COLUMN_NAME", String.class );
			var COLUMN_ID = selectModell.getCell( rowNum, "COLUMN_ID", Long.class );
			System.out.println(COLUMN_NAME);
		}
		
	}

	@SneakyThrows
	private Collection<String> getDataSourceInfo( DataSource dataSource, String string ) {
		List<String> ret = List.of();
		final var modell = new ArrayDataModell().addHeader( List.of( string, "" ) );
		try {
			final var ds = (HikariDataSource)dataSource;
			try (final var conn = ds.getConnection()) {
				modell.add( "ProductVersion" ).add( conn.getMetaData().getDatabaseProductVersion().replace( "\r", "" ).replace( "\n", "" ) );
				modell.add( "Driver" ).add( conn.getMetaData().getDriverName() + " " + conn.getMetaData().getDriverVersion() );
				modell.add( "url - username" ).add( conn.getMetaData().getURL() + " UserName:" + conn.getMetaData().getUserName() ) ;
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
				.setKeret( ArrayDataModelFormatter.KERET.SZIMPLAD )
				.setTopSeparator( false )
				.setHeaderCompress( false ).setBottomSeparator( false ).setTopSeparator( false );
			ret = arrayDataModelFormatter.getFormattedAsList();
		}
		return ret;
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

	public String exportCheck( JdbcAccessor jdbcTemplate ) {
		final var arrayDataModell = new ArrayDataModell();
		arrayDataModell.addHeader( "schemaname", "tablename", "count" );
		var ret = "ok";

		final var sql = "SELECT schemaname, tablename,\n" +
			"SYSCS_UTIL.SYSCS_CHECK_TABLE(schemaname, tablename)\n" +
			"FROM sys.sysschemas s, sys.systables t\n" +
			"WHERE s.schemaid = t.schemaid order by schemaname,tablename\n";

		var count = 0;
		try {
			try (final var conn = jdbcTemplate.getDataSource().getConnection()) {
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
}
