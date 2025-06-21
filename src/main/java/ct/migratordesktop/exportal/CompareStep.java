package ct.migratordesktop.exportal;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "CompareStep")
class CompareStep implements Runnable {
	public CompareStep( ExportServiceImpl exportServiceImpl ) {
		super();
		this.exportService = exportServiceImpl;
	}

	private ExportServiceImpl	exportService;
	@Setter()
	private String						tableName;

	@Override
	public void run() {
		if ( false == exportService.getEcoStatDataSource().existTable( tableName ) ) {
			log.error( "ecoStat Table {} Not exist", tableName );
			return;
		}
		if ( false == exportService.getExportDataSource().existTable( tableName ) ) {
			log.error( "export Table {} Not exist", tableName );
			return;
		}
		final var ecoStatCount = exportService.getEcoStatDataSource().getCount( tableName );
		final var exportCount = exportService.getExportDataSource().getCount( tableName );
		if ( ecoStatCount == exportCount )
			log.info( "Table {} count match:{}", tableName, ecoStatCount );
		else
			log.error( "Table {} count mismatch ecoStatCount:{} exportCount:{}", tableName, ecoStatCount, exportCount );

	}
}
