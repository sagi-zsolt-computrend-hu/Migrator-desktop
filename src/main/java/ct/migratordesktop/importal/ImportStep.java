package ct.migratordesktop.importal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ct.migratordesktop.datasources.medkontroll.MedkontrollDataSourceImpl;
import ct.migratordesktop.models.EcostatColumn;
import ct.migratordesktop.util.Converters;
import ct.migratordesktop.util.Stopper;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ImportStep implements Runnable,Converters {
	public ImportStep( ImportServiceImpl importServiceImpl ) {
		super();
		this.importService = importServiceImpl;
	}

	private ImportServiceImpl	importService;
	@Setter
	private String						tableName;
	private AtomicInteger			imported;
	private List<String>			columnNameList;
	private List<String>			columnTypeList;
	private String						insertCommand;
	private int rowCount;
	private List<String> selectList = List.of();
	private Stopper stopper;

	@SneakyThrows
	@Override
	public void run() {
		stopper = new Stopper().start();
		this.imported = new AtomicInteger( 0 );
		try {
			rowCount = importService.getDerbyDataSource().getCount( tableName );
			log.info( "start {} rowCount:{}", tableName, rowCount );
			final var create = getCreateTableFromEcostatColumns(  tableName );
			importService.getMedkontrollDataSource().execute( create.toUpperCase() );
			if ( rowCount > 0 ) {
				this.columnNameList = importService.getDerbyRepository().getColumnNameListFromEcostatColumns( tableName );
				this.columnTypeList = importService.getDerbyRepository().getColumnTypeListFromEcostatColumns( tableName );
				this.insertCommand = getInsertCommand( tableName );
				log.debug( "insertCommand:{}", insertCommand );
				final var selectHelper = new SelectHelper();
				selectHelper.setTableName( tableName );
				selectHelper.setColumns( columnNameList.stream().collect( Collectors.joining( ",", " ", " " ) ) );
				selectHelper.setPageSize( importService.getImportProperties().getPageSize() );
				selectHelper.setRowCount( rowCount );
				selectList = selectHelper.getSelectList();
				try (final var connRead = importService.getDerbyDataSource().getConnection();
					final var connWrite = importService.getMedkontrollDataSource().getConnection();) {
					connWrite.setAutoCommit( false );
					connRead.setReadOnly( true );
					for ( int i = 0; i < selectList.size(); i++ ) {
						final var sqlSelect = selectList.get( i );
						try (final var st = connRead.prepareStatement( sqlSelect, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY )) {
							st.setMaxRows( Math.min( importService.getImportProperties().getPageSize(), rowCount ) );
							st.setFetchSize( Math.min( importService.getImportProperties().getPageSize(), rowCount ) );
							try(final var rs = st.executeQuery();){
								writeResultSet( connWrite, rs );
							}
						}
					}
				}
			}
		}
		catch ( Exception e ) {
			log.error( "import", e );
		}
		finally {
			log.info( "stop  {} rowCount:{} Time:{} Pages:{}", tableName, importService.getMedkontrollDataSource().getCount( tableName ), stopper.getTime(), selectList.size() );
		}
	}
	private String getInsertCommand( String tableName ) {
		final var qu = columnNameList.stream().map( e -> "?" ).collect( Collectors.joining( ",", "", "" ) );
		final var columns = columnNameList.stream().collect( Collectors.joining( "," ) );
		final var insertCommand = "INSERT INTO " + tableName + " ("
		//	+ ("ID_EXPORT,")
			+ columns + ") VALUES (" + qu + ")";
		return insertCommand;
	}

	private String getCreateTableFromEcostatColumns(  String tableName ) {

		final var sorok = new ArrayList<String>();
		final var maxLenTableName = 55;
		final var ecostatColumnList = importService.getDerbyRepository().findAllByTableNameOrderByColumnIdAsc( tableName.toUpperCase() );
		for ( EcostatColumn columnsEntity : ecostatColumnList ) {
			var sor = "  " + padRight( columnsEntity.getColumnName(), maxLenTableName );
			sor += " " + columnsEntity.getDataType();
			String[] words = { "DATE", "FLOAT", "bigint" };
			if ( false == Arrays.stream( words ).anyMatch( columnsEntity.getDataType()::contains ) ) {
				if ( columnsEntity.getDataLength() > 0 && columnsEntity.getDataPrecision() == 0 )
					sor += "(" + columnsEntity.getDataLength();// + ")";
				if ( columnsEntity.getDataPrecision() > 0 )
					sor += "(" + columnsEntity.getDataPrecision() + "," + columnsEntity.getDataScale();
				if ( columnsEntity.getDataType().contentEquals( "VARCHAR2" ) )
					sor += " CHAR";
				sor += ")";
			}
			sor = padRight( sor, maxLenTableName + 18 );
			if ( columnsEntity.getDataDefault() != null && false == columnsEntity.getDataDefault().contentEquals(  "NULL" )  )
				sor += " DEFAULT " + columnsEntity.getDataDefault();
			if ( columnsEntity.getNullable().equals( "N" ) )
				sor += " NOT NULL";
//			if ( columnsEntity.getExtraAfter() != null )
//				sor += columnsEntity.getExtraAfter();
			sorok.add( sor );

		}
		return sorok.stream().collect( Collectors.joining( ",\n", "create table " + tableName.toUpperCase() + " (\n", ")" ) );
	}

	private void writeResultSet( Connection connWrite, ResultSet rs ) throws Exception {
		try (final var st = connWrite.prepareStatement( this.insertCommand )) {
			while ( rs.next() ) {
				for ( int i = 0; i < this.columnTypeList.size() + 0; i++ ) {
					final var columnType = this.columnTypeList.get( i - 0 );
					final var columnName = this.columnNameList.get( i - 0 );

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
				imported.incrementAndGet();
			}
		}
		finally {
			connWrite.commit();
			if ( this.selectList.size() > 1 )
				log.debug( "imported {} {} / ({}) Time:{}", tableName, imported, this.rowCount, this.stopper.getTime() );
		}
	}

}
