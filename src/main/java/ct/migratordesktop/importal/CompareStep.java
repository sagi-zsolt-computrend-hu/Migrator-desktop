package ct.migratordesktop.importal;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class CompareStep implements Runnable {
	private ImportServiceImpl	importService;
	public CompareStep( ImportServiceImpl importServiceImpl ) {
		super();
		this.importService = importServiceImpl;
	}

	@Setter()
	private String						tableName;

	@Override
	public void run() {
		if ( false == importService.getMedkontrollDataSource().existTable( tableName ) ) {
			log.error( "Medkontroll Table {} Not exist", tableName );
			return;
		}
		if ( false == importService.getDerbyDataSource().existTable( tableName ) ) {
			log.error( "export Table {} Not exist", tableName );
			return;
		}
		final var medKontrollCount = importService.getMedkontrollDataSource().getCount( tableName );
		final var exportCount = importService.getDerbyDataSource().getCount( tableName );
		if ( medKontrollCount == exportCount )
			log.info( "Table {} count match:{}", tableName, medKontrollCount );
		else
			log.error( "Table {} count mismatch alapCount:{} exportCount:{}", tableName, medKontrollCount, exportCount );
	}
}
