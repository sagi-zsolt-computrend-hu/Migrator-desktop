package ct.migratordesktop.toderby;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import ct.migratordesktop.models.EcostatColumn;
import ct.migratordesktop.util.Converters;
import ct.migratordesktop.util.Stopper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "DerbyStep")
class DerbyStep implements Runnable, Converters {
	public DerbyStep( DerbyServiceImpl derbyService ) {
		super();
		this.derbyService = derbyService;
	}
	@Getter
	private final DerbyServiceImpl	derbyService;
	@Setter
	@Getter
	private String						tableName;
	@Getter
	private AtomicInteger			exported;
	@Getter
	private AtomicLong				id;
	@Getter
	private List<String>			columnNameList;
	@Getter
	private List<String>			columnTypeList;
	@Getter
	private String						insertCommand;
	@Getter
	private int								rowCount;
	private Stopper						stopper;
	private List<String>			selectList;
	@Getter
	private ThreadPoolExecutor executor;
	
	@SneakyThrows
	@Override
	public void run() {
		stopper = new Stopper().start();
		id = new AtomicLong();
		exported = new AtomicInteger();
		selectList = List.of();
		try {
			rowCount = derbyService.getEcoStatDataSource().getCount( tableName );
			final var create = /*exportServiceImpl.getExportDataSource().*/getCreateTableFromEcostatColumns( tableName );

			derbyService.getDerbyDataSource().execute( create.toUpperCase() );
			if ( rowCount > 0 ) {
				this.columnNameList = derbyService.getDerbyRepository().getColumnNameListFromEcostatColumns( tableName );
				this.columnTypeList = derbyService.getDerbyRepository().getColumnTypeListFromEcostatColumns( tableName );
				this.insertCommand = getInsertCommand( tableName  );
				log.debug( "insertCommand:{}", insertCommand );
				final var selectHelper = new SelectHelper();
				selectHelper.setTableName( tableName );
				final var columnList = derbyService.getDerbyRepository().getColumnNameListFromEcostatColumns( tableName );
				selectHelper.setColumns( columnList.stream().collect( Collectors.joining( ",", " ", " " ) ) );
				selectHelper.setPageSize( derbyService.getDerbyProperties().getPageSize() );
				selectHelper.setRowCount( rowCount );
				selectList = selectHelper.getSelectList();
				log.info( "start {} rowCount:{} subStep:{}", tableName, rowCount ,selectList.size());
//				try (final var connRead = derbyService.getEcoStatDataSource().getConnection();
//					final var connWrite = derbyService.getDerbyDataSource().getConnection();) {
//					connRead.setReadOnly( true );
//					connWrite.setAutoCommit( false );
				executor = new ThreadPoolExecutor(derbyService.getDerbyProperties().getSubStepThreads(),derbyService.getDerbyProperties().getSubStepThreads(),0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());					//Executors.newFixedThreadPool( derbyService.getDerbyProperties().getSubStepThreads( ));
					for ( int i = 0; i < selectList.size(); i++ ) {
						final var sqlSelect = selectList.get( i );
						var subStep= new DerbySubStep( this ,sqlSelect,selectList.size() == 1 ? "":(i+1)+"/" + selectList.size() );
						executor.execute( subStep );
//						try (final var st = connRead.prepareStatement( sqlSelect, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY )) {
//							st.setMaxRows( Math.min( derbyService.getDerbyProperties().getPageSize(), rowCount ) );
//							st.setFetchSize( Math.min( derbyService.getDerbyProperties().getPageSize(), rowCount ) );
//							try (final var rs = st.executeQuery();) {
//								writeResultSet( connWrite, rs );
//							}
//						}
					}
					executor.shutdown();
					executor.awaitTermination( 100, TimeUnit.HOURS );
				}
//			}
		}
		catch ( Exception e ) {
			log.error( "export", e );
		}
		finally {
			log.info( "stop  {} rowCount:{} Time:{} ", tableName, derbyService.getDerbyDataSource().getCount( tableName ), stopper.getTime() );
		}
	}

	private void writeResultSet( Connection connWrite, ResultSet rs ) throws Exception {
		try (final var st = connWrite.prepareStatement( this.insertCommand )) {
			while ( rs.next() ) {
				st.setLong( 1, id.incrementAndGet() );
				for ( int i = 1; i < this.columnTypeList.size() + 1; i++ ) {
					final var columnType = this.columnTypeList.get( i - 1 );
					final var columnName = this.columnNameList.get( i - 1 );

					switch ( columnType ) {
					case "String" -> st.setString( i + 1, rs.getString( columnName ) );
					case "BigDecimal" -> st.setBigDecimal( i + 1, rs.getBigDecimal( columnName ) );
					case "LocalDate" -> st.setDate( i + 1, rs.getDate( columnName ) );
					case "LocalDateTime" -> st.setTimestamp( i + 1, rs.getTimestamp( columnName ) );
					case "Integer" -> st.setInt( i + 1, rs.getInt( columnName ) );
					case "Float" -> st.setFloat( i + 1, rs.getFloat( columnName ) );
					case "Long" -> st.setLong( i + 1, rs.getLong( columnName ) );
					default -> st.setObject( i + 1, rs.getObject( columnName ) );
					}
				}
				st.execute();
				exported.incrementAndGet();
			}
		}
		finally {
			connWrite.commit();
			if ( this.selectList.size() > 1 )
				log.debug( "exported {} {} / ({}) Time:{}", tableName, exported, this.rowCount, this.stopper.getTime() );
		}
	}

	private String getCreateTableFromEcostatColumns( String tableName ) {
		final var sorok = new ArrayList<String>();
		final var maxLenTableName = 55;
		final var ecostatColumnList = derbyService.getDerbyRepository().findAllByTableNameOrderByColumnIdAsc( tableName.toUpperCase() );
		ecostatColumnList.add( 0, getIdExport() );
		for ( EcostatColumn ecostatColumn : ecostatColumnList ) {
			convertToDerby( ecostatColumn );
			var sor = "  " + padRight( ecostatColumn.getColumnName(), maxLenTableName );
			sor += " " + ecostatColumn.getDataType();
			String[] words = { "DATE", "FLOAT", "bigint" };
			if ( false == Arrays.stream( words ).anyMatch( ecostatColumn.getDataType()::contains ) ) {
				if ( ecostatColumn.getDataLength() > 0 && ecostatColumn.getDataPrecision() == 0 )
					sor += "(" + ecostatColumn.getDataLength() + ")";
				if ( ecostatColumn.getDataPrecision() > 0 )
					sor += "(" + ecostatColumn.getDataPrecision() + "," + ecostatColumn.getDataScale() + ")";
			}
			sor = padRight( sor, maxLenTableName + 18 );
			//			if ( columnsEntity.getNullable().equals( "N" ) )
			//				sor += " NOT NULL";
			//			if ( columnsEntity.getDataDefault() != null )
			//				sor += " DEFAULT " + columnsEntity.getDataDefault();
			//			if ( columnsEntity.getExtraAfter() != null )
			//				sor += columnsEntity.getExtraAfter();
			sorok.add( sor );

		}
		return sorok.stream().collect( Collectors.joining( ",\n", "create table " + tableName.toUpperCase() + " (\n", ")" ) );
	}

	private void convertToDerby( EcostatColumn e ) {
		if ( e.getDataType().equalsIgnoreCase( "INTEGER" ) ) {
			e.setDataType( "NUMBER" );
			e.setDataPrecision( 10 );
			e.setDataScale( 0 );
		}
		e.setDataType( e.getDataType().replace( "VARCHAR2", "varchar" ) );
		if ( e.getDataType().equalsIgnoreCase( "NUMBER" ) ) {
			if ( e.getDataPrecision() > 31 )
				e.setDataPrecision( 31 );
			e.setDataType( "DECIMAL" );
		}
	}

	private String getInsertCommand( String tableName ) {
		final var qu = columnNameList.stream().map( e -> "?" ).collect( Collectors.joining( ",", "?,", "" ) );
		final var columns = columnNameList.stream().collect( Collectors.joining( "," ) );
		final var insertCommand = "INSERT INTO " + tableName + " ("
			+ ("ID_EXPORT,")
			+ columns + ") VALUES (" + qu + ")";
		return insertCommand;
	}

	private EcostatColumn getIdExport() {
		final var columnsEntity = new EcostatColumn();
		columnsEntity.setColumnName( "ID_EXPORT" );
		columnsEntity.setDataType( "bigint" );
		columnsEntity.setDataLength( 0 );
		columnsEntity.setDataPrecision( 0 );
		columnsEntity.setDataScale( 0 );
		columnsEntity.setNullable( "N" );
		columnsEntity.setDataDefault( null );
		//columnsEntity.setExtraAfter( " PRIMARY KEY" );
		return columnsEntity;
	}

}
