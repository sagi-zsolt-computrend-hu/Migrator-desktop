package ct.migratordesktop.export;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ct.migratordesktop.datasources.AbstractDataSource;
import ct.migratordesktop.datasources.derby.DerbyDataSourceImpl;
import ct.migratordesktop.datasources.medkontroll.MedkontrollDataSourceImpl;
import ct.migratordesktop.tesztdata.TesztDataProperties;
import ct.migratordesktop.util.Converters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExportServiceImpl implements Converters {
//	@Lazy
//	@Autowired
//	private AkkorDataSourceImpl		akorDataSourceImpl;
	@Lazy
	@Autowired
	private DerbyDataSourceImpl	derbyDataSourceImpl;
	@Lazy
	@Autowired
	private MedkontrollDataSourceImpl		medKontrollDataSourceImpl;

	public void generateDerby() {
		generate( derbyDataSourceImpl );
	}


	@SneakyThrows
	private void generate( AbstractDataSource ds ) {
		//log.info( "Properties {}", tesztDataProperties.toString() );
//		for ( int i = 1; i <= tesztDataProperties.getTableNum(); i++ ) {
//			final var tableName = tesztDataProperties.getTableName() + padLeft( i, 3, "0" );
//					ds.dropTable( tableName );
//		}
		final var executor = Executors.newFixedThreadPool( 4 );
		for ( int i = 1; i <= 50; i++ ) {
			var step = new ExportStep( ds );
			executor.execute( step );
		}
		executor.shutdown();

		executor.awaitTermination( 100, TimeUnit.HOURS );

	}

}
