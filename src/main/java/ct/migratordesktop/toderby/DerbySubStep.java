package ct.migratordesktop.toderby;

import java.sql.Connection;
import java.sql.ResultSet;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "DerbyStep")
class DerbySubStep implements Runnable {
	private final String		sqlSelect;
	private final DerbyStep	derbyStep;
	private final String		index;

	public DerbySubStep( DerbyStep derbyStep, String sqlSelect, String index ) {
		super();
		this.derbyStep = derbyStep;
		this.sqlSelect = sqlSelect;
		this.index = index;
	}

	@Override
	public void run() {
		if ( !index.isEmpty() )
			log.info( "SubStep start {} {} executor:{}", derbyStep.getTableName(), index, derbyStep.getExecutor().getActiveCount() );
		try (final var connRead = derbyStep.getDerbyService().getEcoStatDataSource().getConnection();
			final var connWrite = derbyStep.getDerbyService().getDerbyDataSource().getConnection();) {
			connRead.setReadOnly( true );
			connWrite.setAutoCommit( false );
			//			for ( int i = 0; i < selectList.size(); i++ ) {
			//				final var sqlSelect = selectList.get( i );
			//				var xx= new DerbySubStep( this ,sqlSelect);
			try (final var st = connRead.prepareStatement( sqlSelect, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY )) {
				st.setMaxRows( Math.min( derbyStep.getDerbyService().getDerbyProperties().getPageSize(), derbyStep.getRowCount() ) );
				st.setFetchSize( Math.min( derbyStep.getDerbyService().getDerbyProperties().getPageSize(), derbyStep.getRowCount() ) );
				try (final var rs = st.executeQuery();) {
					writeResultSet( connWrite, rs );
				}
			}

			//		}

		}
		catch ( Exception e ) {
			log.error( "export", e );
		}
		finally {
		}
	}

	private void writeResultSet( Connection connWrite, ResultSet rs ) throws Exception {
		try (final var st = connWrite.prepareStatement( derbyStep.getInsertCommand() )) {
			while ( rs.next() ) {
				st.setLong( 1, derbyStep.getId().incrementAndGet() );
				for ( int i = 1; i < derbyStep.getColumnTypeList().size() + 1; i++ ) {
					final var columnType = derbyStep.getColumnTypeList().get( i - 1 );
					final var columnName = derbyStep.getColumnNameList().get( i - 1 );

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
				derbyStep.getExported().incrementAndGet();
			}
		}
		//		finally {
		connWrite.commit();
		//			if ( this.selectList.size() > 1 )
		//				log.debug( "exported {} {} / ({}) Time:{}", tableName, exported, this.rowCount, this.stopper.getTime() );
		//		}
		//	}
	}
}