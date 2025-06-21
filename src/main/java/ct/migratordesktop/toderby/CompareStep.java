package ct.migratordesktop.toderby;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "CompareStep")
class CompareStep implements Runnable {
	public CompareStep( DerbyServiceImpl derbyServiceImpl ) {
		super();
		this.derbyService = derbyServiceImpl;
	}

	private DerbyServiceImpl	derbyService;
	@Setter()
	private String						tableName;

	@Override
	public void run() {
		if ( false == derbyService.getEcoStatDataSource().existTable( tableName ) ) {
			log.error( "ecoStat Table {} Not exist", tableName );
			return;
		}
		if ( false == derbyService.getDerbyDataSource().existTable( tableName ) ) {
			log.error( "derby Table {} Not exist", tableName );
			return;
		}
		final var ecoStatCount = derbyService.getEcoStatDataSource().getCount( tableName );
		final var derbyCount = derbyService.getDerbyDataSource().getCount( tableName );
		if ( ecoStatCount == derbyCount )
			log.info( "Table {} count match:{}", tableName, ecoStatCount );
		else
			log.error( "Table {} count mismatch ecoStatCount:{} derbyCount:{}", tableName, ecoStatCount, derbyCount );
	}
}

