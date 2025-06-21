package ct.migratordesktop.importal;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ct.migratordesktop.datasources.derby.DerbyDataSourceImpl;
import ct.migratordesktop.datasources.medkontroll.MedkontrollDataSourceImpl;
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
public class ImportServiceImpl {
	@Lazy
	@Autowired
	@Getter
	private DerbyRepository			exportRepository;
	@Lazy
	@Getter
	@Autowired
	private ImportProperties			importProperties;
	@Lazy
	@Autowired
	@Getter
	private DerbyDataSourceImpl	derbyDataSource;
	@Lazy
	@Getter
	@Autowired
	protected MedkontrollDataSourceImpl	medkontrollDataSource;
	private List<String>					tableNameList	= List.of();

	public void importal() {
		final var stopper = new Stopper().start();
		try {
			log.info( "Import start Properties:{}", importProperties );
			tableNameList = derbyDataSource.getTableNamesFromEcostatColumns();
			log.debug( "Drop tables: {} {}", tableNameList.size(), tableNameList );
			tableNameList.forEach( tableName -> 	medkontrollDataSource.dropTable( tableName ) );
			log.debug( "importing tables:{}", tableNameList );
			final var executor = Executors.newFixedThreadPool( importProperties.getThreads() );
			for ( String tableName : tableNameList ) {
				final var step = new ImportStep( this );
				step.setTableName( tableName );
				executor.execute( step );
			}
			executor.shutdown();
			executor.awaitTermination( 100, TimeUnit.HOURS );
		}
		catch ( Exception e ) {
			log.error( "export", e );
		}
		finally {
			log.info( "Export stop tablesCount:{} Time:{}", tableNameList.size(), stopper.getTime() );
		}

	}

	@SneakyThrows
	public void compare() {
		final var tableNameList = derbyDataSource.getTableNamesFromEcostatColumns();
		final var stopper = new Stopper().start();
		try {
			log.info( "Import Compare start TableCount:{} Properties:{}", tableNameList.size(), importProperties );
			final var executor = Executors.newFixedThreadPool( importProperties.getThreads() );
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