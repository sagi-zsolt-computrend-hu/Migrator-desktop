package ct.migratordesktop.exportal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import ct.migratordesktop.models.EcostatColumn;
import ct.migratordesktop.util.Converters;
import ct.migratordesktop.util.Stopper;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ExportStep implements Runnable, Converters {
	public ExportStep( ExportServiceImpl exportServiceImpl ) {
		super();
		this.exportService = exportServiceImpl;
	}

	private ExportServiceImpl	exportService;
	@Setter()
	private String						tableName;
	private AtomicInteger			exported;
	private AtomicLong				id;
	private List<String>			columnNameList;
	private List<String>			columnTypeList;
	private String						insertCommand;
	private int								rowCount;
	private Stopper						stopper;
	private List<String>			selectList;

	@SneakyThrows
	@Override
	public void run() {
		stopper = new Stopper().start();
		id = new AtomicLong();
		exported = new AtomicInteger();
		selectList = List.of();
		try {
			rowCount = exportService.getEcoStatDataSource().getCount( tableName );
			log.info( "start {} rowCount:{}", tableName, rowCount );
			final var create = /*exportServiceImpl.getExportDataSource().*/getCreateTableFromEcostatColumns( tableName );

			exportService.getExportDataSource().execute( create.toUpperCase() );
			if ( rowCount > 0 ) {
				this.columnNameList = exportService.getExportRepository().getColumnNameListFromEcostatColumns( tableName );
				this.columnTypeList = exportService.getExportRepository().getColumnTypeListFromEcostatColumns( tableName );
				this.insertCommand = getInsertCommand( tableName  );
				log.debug( "insertCommand:{}", insertCommand );
				final var selectHelper = new SelectHelper();
				selectHelper.setTableName( tableName );
				final var columnList = exportService.getExportRepository().getColumnNameListFromEcostatColumns( tableName );
				selectHelper.setColumns( columnList.stream().collect( Collectors.joining( ",", " ", " " ) ) );
				selectHelper.setPageSize( exportService.getExportProperties().getPageSize() );
				selectHelper.setRowCount( rowCount );
				selectList = selectHelper.getSelectList();
				try (final var connRead = exportService.getEcoStatDataSource().getConnection();
					final var connWrite = exportService.getExportDataSource().getConnection();) {
					connRead.setReadOnly( true );
					connWrite.setAutoCommit( false );
					for ( int i = 0; i < selectList.size(); i++ ) {
						final var sqlSelect = selectList.get( i );
						try (final var st = connRead.prepareStatement( sqlSelect, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY )) {
							st.setMaxRows( Math.min( exportService.getExportProperties().getPageSize(), rowCount ) );
							st.setFetchSize( Math.min( exportService.getExportProperties().getPageSize(), rowCount ) );
							try (final var rs = st.executeQuery();) {
								writeResultSet( connWrite, rs );
							}
						}
					}
				}
			}
		}
		catch ( Exception e ) {
			log.error( "export", e );
		}
		finally {
			log.info( "stop  {} rowCount:{} Time:{} Pages:{}", tableName, exportService.getExportDataSource().getCount( tableName ), stopper.getTime(), selectList.size() );
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
		final var ecostatColumnList = exportService.getExportRepository().findAllByTableNameOrderByColumnIdAsc( tableName.toUpperCase() );
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
