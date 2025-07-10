package ct.migratordesktop.export;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ct.migratordesktop.datasources.derby.DerbyDataSourceConfiguration;
import ct.migratordesktop.util.Converters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExportServiceImpl implements Converters {
	@Autowired
	DerbyDataSourceConfiguration derbyDataSourceConfiguration;

	public void generateDerby() {
		generate();
	}

	@SneakyThrows
	private void generate() {
		final var executor = Executors.newFixedThreadPool( 4 );
		exp( executor, new StepRecord( "ECOSTAT_COLUMS", """
			select * From ECOSTAT_COLUMS
						""" ) );
		
		executor.shutdown();
		executor.awaitTermination( 100, TimeUnit.HOURS );
	}

	private void exp( ExecutorService executor, StepRecord stepRecord ) {
		var step = new ExportStep( derbyDataSourceConfiguration );
		step.setStepRecord( stepRecord );
		executor.submit( step );
	}

	record StepRecord( String fileName, String sql, Object... params ) {
	}

}
