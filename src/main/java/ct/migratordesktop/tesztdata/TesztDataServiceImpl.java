package ct.migratordesktop.tesztdata;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ct.migratordesktop.datasources.AbstractDataSource;
import ct.migratordesktop.datasources.derby.DerbyDataSourceImpl;
import ct.migratordesktop.datasources.medkontroll.MedkontrollDataSourceImpl;
import ct.migratordesktop.util.Converters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TesztDataServiceImpl implements Converters {
//	@Lazy
//	@Autowired
//	private AkkorDataSourceImpl		akorDataSourceImpl;
	@Lazy
	@Autowired
	private DerbyDataSourceImpl	derbyDataSourceImpl;
	@Lazy
	@Autowired
	private MedkontrollDataSourceImpl		medKontrollDataSourceImpl;
	@Lazy
	@Autowired
	public TesztDataProperties		tesztDataProperties;

	public void generateExport() {
		generate( derbyDataSourceImpl );
	}

//	@SneakyThrows
//	public void generateAkkor() {
//		generate( akorDataSourceImpl );
//
//	}
	
	public void generateMedkontroll() {
		generate( medKontrollDataSourceImpl );
	}

	@SneakyThrows
	private void generate( AbstractDataSource ds ) {
		log.info( "Properties {}", tesztDataProperties.toString() );
		for ( int i = 1; i <= tesztDataProperties.getTableNum(); i++ ) {
			final var tableName = tesztDataProperties.getTableName() + padLeft( i, 3, "0" );
					ds.dropTable( tableName );
		}
		final var executor = Executors.newFixedThreadPool( tesztDataProperties.getThreads() );
		for ( int i = 1; i <= tesztDataProperties.getTableNum(); i++ ) {
			var teszt = new TesztDataStep( ds );
			teszt.setProperties( tesztDataProperties );
			final var tableName = tesztDataProperties.getTableName() + padLeft( i, 3, "0" );
			teszt.setTableName( tableName );
			executor.execute( teszt );
		}
		executor.shutdown();

		executor.awaitTermination( 100, TimeUnit.HOURS );

	}

}
