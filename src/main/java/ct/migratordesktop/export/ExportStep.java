package ct.migratordesktop.export;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

import ct.migratordesktop.datasources.derby.DerbyDataSourceConfiguration;
import ct.migratordesktop.export.ExportServiceImpl.StepRecord;
import ct.migratordesktop.util.ArrayDataModelFormatter;
import ct.migratordesktop.util.Converters;
import ct.migratordesktop.util.SelectModell;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j(topic = "ExportStep")
public class ExportStep implements Converters, Runnable {

	@Setter
	private StepRecord stepRecord;
	private DerbyDataSourceConfiguration	derbyDataSourceConfiguration;

	public ExportStep( DerbyDataSourceConfiguration derbyDataSourceConfiguration ) {
		super();
		this.derbyDataSourceConfiguration = derbyDataSourceConfiguration;
	}
	@SneakyThrows
	public void run() {
		Path url = null;
		try {
			final var jdbcTemplate = new JdbcTemplate( derbyDataSourceConfiguration.dataSource() );
			url = Paths.get( derbyDataSourceConfiguration.dataSource().getConnection().getMetaData().getURL()
				.replace( "jdbc:derby:directory://", "" ) ).getParent();
			url = Paths.get( url.toString(), "export1", stepRecord.fileName().toLowerCase(Locale.ROOT) + ".txt" );
			var selectModell = new SelectModell( jdbcTemplate, stepRecord.sql() ).select( stepRecord.params() );
			var arrayDataModelFormatter = new ArrayDataModelFormatter( selectModell );

			arrayDataModelFormatter.setColumnInfo( false ).setSorszamozas( false )
				.setKeret( ArrayDataModelFormatter.KERET.SZIMPLAD ).setTopSeparator( false )
				.setHeaderCompress( false ).setBottomSeparator( false );

			Files.write( Paths.get( url.toString() ), arrayDataModelFormatter.getFormatted().getBytes( StandardCharsets.UTF_8 ),
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE );
		}
		catch ( Exception e ) {
			log.error( "Error", e );
		}

		finally {
			log.info( "Export {} ({})",url, Files.size( url )/*, tableName, dataSource.getCount( tableName )*/ );
		}
	}

}