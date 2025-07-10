package ct.migratordesktop.toderby;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ct.migratordesktop.datasources.derby.DerbyDataSourceImpl;
import ct.migratordesktop.datasources.ecostat.EcoStatDataSourceImpl;
import ct.migratordesktop.models.EcostatColumn;
import ct.migratordesktop.models.PrimaryKey;
import ct.migratordesktop.repositories.derby.DerbyRepository;
import ct.migratordesktop.util.Stopper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class DerbyServiceImpl {
	@Getter
	@Autowired
	private DerbyProperties				derbyProperties;
	@Lazy
	@Autowired
	@Getter
	private DerbyDataSourceImpl		derbyDataSource;
	@Getter
	@Autowired
	private EcoStatDataSourceImpl	ecoStatDataSource;
	@Autowired
	@Getter
	private DerbyRepository				derbyRepository;

	private List<String>					tableNameList	= List.of();
	@Autowired
	DerbyRepository								ecostatColumnsRepository;

	public void toDerby() {
		final var stopper = new Stopper().start();
		try {
			log.info( "Export start Properties:{}", derbyProperties );
			final var dropTableNameList = derbyDataSource.getAllTableNamesFromDerby();
			log.debug( "Drop tables: {} {}", dropTableNameList.size(), dropTableNameList );
			dropTableNameList.forEach( tableName -> derbyDataSource.dropTable( tableName ) );
			exportColumns();
			exportPrimaryKeys();
			tableNameList = derbyDataSource.getTableNamesFromEcostatColumns();
			log.debug( "exporting tables: {}", tableNameList );
			//
			final var executor = Executors.newFixedThreadPool( derbyProperties.getStepThreads() );
			for ( String tableName : tableNameList ) {
				final var step = new DerbyStep( this );
				step.setTableName( tableName );
				executor.execute( step );
			}
			executor.shutdown();
			executor.awaitTermination( 100, TimeUnit.HOURS );
		}
		catch ( Exception e ) {
			log.error( "toDerby", e );
		}
		finally {
			log.info( "toDerby stop tablesCount:{} Time:{}", tableNameList.size(), stopper.getTime() );
		}
	}

	private void exportColumns() {
		log.info( "exportColumns Started" );
		derbyDataSource.execute( EcostatColumn.table );

		final var sql = "select TABLE_NAME,COLUMN_NAME,COLUMN_ID,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE,NULLABLE,DATA_DEFAULT"
			+ " from USER_TAB_COLUMNS WHERE " +
			derbyProperties.getDerbyColumnsWhere() +
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
			log.info( "exportColumns Ended rows:{}", derbyDataSource.getCount( EcostatColumn.TABLE_NAME ) );
		}
	}

	public void exportPrimaryKeys() {
		log.info( "exportPrimaryKeys Started" );
		derbyDataSource.execute( PrimaryKey.table );
		final var in = Stream.concat( derbyRepository.getTableNamesFromEcostatColumns().stream(), Stream.of( "_" ) ).map( t -> "'" + t.toUpperCase() + "'" ).collect( Collectors.joining( ",", " (", ") " ) );

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
			log.info( "exportPrimaryKeys Ended rows:{}", derbyDataSource.getCount( PrimaryKey.TABLE_NAME ) );
		}
	}

	@SneakyThrows
	public void compare() {
		final var tableNameList = derbyDataSource.getTableNamesFromEcostatColumns();
		final var stopper = new Stopper().start();
		try {
			log.info( "Export Compare start TableCount:{} Properties:{}", tableNameList.size(), derbyProperties );
			final var executor = Executors.newFixedThreadPool( derbyProperties.getStepThreads() );
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