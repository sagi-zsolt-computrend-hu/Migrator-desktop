package ct.migratordesktop.exportal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ct.migratordesktop.datasources.ecostat.EcoStatDataSourceImpl;
import ct.migratordesktop.datasources.export.ExportDataSourceImpl;
import ct.migratordesktop.models.EcostatColumn;
import ct.migratordesktop.models.PrimaryKey;
import ct.migratordesktop.repositories.ecostat.EcostatRepository;
import ct.migratordesktop.repositories.export.ExportRepository;
import ct.migratordesktop.util.Stopper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class ExportServiceImpl {
	@Getter
	@Autowired
	private ExportProperties			exportProperties;
	@Lazy
	@Autowired
	@Getter
	private ExportDataSourceImpl	exportDataSource;
	@Getter
	@Autowired
	private EcoStatDataSourceImpl	ecoStatDataSource;
	@Autowired
	private EcostatRepository			ecostatRepository;

	@Autowired
	@Getter
	private ExportRepository			exportRepository;

	private List<String>					tableNameList	= List.of();
	@Autowired
	ExportRepository							ecostatColumnsRepository;

	public void exportal() {
		final var stopper = new Stopper().start();
		try {
			log.info( "Export start Properties:{}", exportProperties );
			final var dropTableNameList = exportDataSource.getAllTableNamesFromExport();
			log.debug( "Drop tables: {} {}", dropTableNameList.size(), dropTableNameList );
			dropTableNameList.forEach( tableName -> exportDataSource.dropTable( tableName ) );
			exportColumns_();
			exportPrimaryKeys();
			tableNameList = exportDataSource.getTableNamesFromEcostatColumns();
			log.debug( "exporting tables: {}", tableNameList );
			//
			final var executor = Executors.newFixedThreadPool( exportProperties.getThreads() );
			for ( String tableName : tableNameList ) {
				final var step = new ExportStep( this );
				step.setTableName( tableName );
				executor.execute( step );
			}
			executor.shutdown();
			executor.awaitTermination( 100, TimeUnit.HOURS );
		}
		catch ( Exception e ) {
			log.error( "export", e );
		}
		finally {
			log.info( "Export stop tablesCount:{} Time:{}", tableNameList.size(), stopper.getTime() );
		}
	}
	//	private void exportColumns() {
	//		log.info( "exportColumns Started" );
	//		exportDataSource.execute( EcostatColumn.table );
	//
	//		final var sql = "select rownum ID, TABLE_NAME,COLUMN_NAME,COLUMN_ID,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE,NULLABLE,DATA_DEFAULT"
	//			+ " from USER_TAB_COLUMNS WHERE " +
	//			exportProperties.getExportColumnsWhere() +
	//			" order by table_name, column_id";
	//	//	var  xxx = ecoStatDataSource.getJdbcTemplate().query( sql, new ResultSetExtractor<EcostatColumn> );
	//		long id = 0;
	//		try  {
	//			var ecostatColumnList = ecostatRepository.getAllColumnsFromEcostat("1=1"/*exportProperties.getExportColumnsWhere()*/);
	//
	//			for ( EcostatColumn ecostatColumn : ecostatColumnList ) {
	////				System.out.println();
	//				ecostatColumn.setId( ++id );
	//				//ecostatColumn.setDataDefault(  Objects.isNull(  ecostatColumn.getDataDefault()) ? "": ecostatColumn.getDataDefault());
	//				ecostatColumnsRepository.insertToEcostatColumns( ecostatColumn );
	////				ecostatColumn.setDataDefault(  Objects.isNull(  ecostatColumn.getDataDefault()) ? "": ecostatColumn.getDataDefault());
	////				final var ins = new EcostatColumn();
	////				ins.setId( ++id );
	////				ins.setTableName( ecostatColumn.getTableName() );
	////				ins.setColumnName( ecostatColumn.getColumnName() );
	////				ins.setColumnId( ecostatColumn.getColumnId() );
	////				ins.setDataType( ecostatColumn.getDataType() );
	////				ins.setDataLength( ecostatColumn.getDataLength() );
	////				ins.setDataPrecision( ecostatColumn.getDataPrecision() );
	////				ins.setDataScale( ecostatColumn.getDataScale() );
	////				ins.setNullable( ecostatColumn.getNullable() );
	////				final var string = ecostatColumn.getDataDefault();
	////				ecostatColumnsRepository.insert( ins );
	//
	//			}
	//			
	//		}
	//		catch ( Exception e ) {
	//			log.error( "exportColumns {} {} ", e.getMessage(),  id );
	//		}
	//		finally {
	//			log.info( "exportColumns Ended rows:{}", exportDataSource.getCount( EcostatColumn.TABLE_NAME ) );
	//		}
	//	}

	private void exportColumns_() {
		log.info( "exportColumns Started" );
		exportDataSource.execute( EcostatColumn.table );

		final var sql = "select TABLE_NAME,COLUMN_NAME,COLUMN_ID,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE,NULLABLE,DATA_DEFAULT"
			+ " from USER_TAB_COLUMNS WHERE " +
			exportProperties.getExportColumnsWhere() +
			" order by table_name, column_id";
		long id = 0;
		try (final var conn = ecoStatDataSource.getConnection()) {
			try (final var st = conn.prepareStatement( sql )) {
				try (final var rs = st.executeQuery()) {
					while ( rs.next() ) {
						final var ecostatColumn = new EcostatColumn();
						ecostatColumn.setId( ++id );
						ecostatColumn.setTableName( rs.getString( "TABLE_NAME" ) );
						ecostatColumn.setColumnName( rs.getString( "COLUMN_NAME" ) );
						ecostatColumn.setColumnId( rs.getInt( "COLUMN_ID" ) );
						ecostatColumn.setDataType( rs.getString( "DATA_TYPE" ) );
						ecostatColumn.setDataLength( rs.getInt( "DATA_LENGTH" ) );
						ecostatColumn.setDataPrecision( rs.getObject( "DATA_PRECISION", Integer.class ) );
						ecostatColumn.setDataScale( rs.getObject( "DATA_SCALE", Integer.class ) );
						ecostatColumn.setNullable( rs.getObject( "NULLABLE", String.class ) );
						//final var string = rs.getString( "DATA_DEFAULT" );
						//ecostatColumn.setDataDefault(  Objects.isNull(  string) ? "": string);
						ecostatColumnsRepository.insertToEcostatColumns( ecostatColumn );
					}
				}
			}

		}
		catch ( SQLException e ) {
			log.error( "exportColumns {} {} {}", e.getMessage(), sql, id );
		}
		finally {
			log.info( "exportColumns Ended rows:{}", exportDataSource.getCount( EcostatColumn.TABLE_NAME ) );
		}
	}

	public void exportPrimaryKeys() {
		log.info( "exportPrimaryKeys Started" );
		exportDataSource.execute( PrimaryKey.table );
		final var in = Stream.concat( exportRepository.getTableNamesFromEcostatColumns().stream(), Stream.of( "_" ) ).map( t -> "'" + t.toUpperCase() + "'" ).collect( Collectors.joining( ",", " (", ") " ) );

		String sql = "SELECT cols.table_name, LISTAGG(cols.column_name,',') WITHIN GROUP (ORDER BY cols.position) PRIMARY_KEY " +
			" FROM user_constraints cons, user_cons_columns cols\n" +
			" WHERE cons.constraint_type like 'P' \n" +
			" AND cols.table_name IN " + in +
			" AND cons.constraint_name = cols.constraint_name " +
			" GROUP BY cols.table_name ";
		long id = 0;
		try (Connection conn = ecoStatDataSource.getConnection()) {
			try (PreparedStatement st = conn.prepareStatement( sql )) {
				try (ResultSet rs = st.executeQuery()) {
					while ( rs.next() ) {
						final var primaryKey = new PrimaryKey();
						primaryKey.setId( ++id );
						primaryKey.setTableName( rs.getString( "TABLE_NAME" ) );
						primaryKey.setPrimaryKey( rs.getString( "PRIMARY_KEY" ) );
						ecostatColumnsRepository.insertToPrimaryKey( primaryKey );
					}
				}
			}
		}
		catch ( SQLException e ) {
			log.error( "exportPrimaryKeys", e );
		}
		finally {
			log.info( "exportPrimaryKeys Ended rows:{}", exportDataSource.getCount( PrimaryKey.TABLE_NAME ) );
		}
	}

	@SneakyThrows
	public void compare() {
		final var tableNameList = exportDataSource.getTableNamesFromEcostatColumns();
		final var stopper = new Stopper().start();
		try {
			log.info( "Export Compare start TableCount:{} Properties:{}", tableNameList.size(), exportProperties );
			final var executor = Executors.newFixedThreadPool( exportProperties.getThreads() );
			for ( String tableName : tableNameList ) {
				final var step = new CompareStep( this );
				step.setTableName( tableName );
				executor.execute( step );
			}
			executor.shutdown();
			executor.awaitTermination( 100, TimeUnit.HOURS );
		}
		catch ( Exception e ) {
			log.error( "Compare", e );
		}
		finally {
			log.info( "compare stop TableCount:{} Time:{}", tableNameList.size(), stopper.getTime() );
		}

	}
}