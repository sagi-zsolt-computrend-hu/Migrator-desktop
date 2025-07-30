package ct.migratordesktop.export;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Properties;

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
	//private Properties										intezmenyProperties;
	@Setter
	private StepRecord										stepRecord;
	private DerbyDataSourceConfiguration	derbyDataSourceConfiguration;
	@Setter
	private Path path = null;	
	
	
	public ExportStep( DerbyDataSourceConfiguration derbyDataSourceConfiguration ) {
		super();
		this.derbyDataSourceConfiguration = derbyDataSourceConfiguration;
	}

	@SneakyThrows
	public void run() {
		Path stepPath = null;
		//intezmenyProperties = new Properties();
		try {
//			path = Paths.get( derbyDataSourceConfiguration.dataSource().getConnection().getMetaData().getURL()
//				.replace( "jdbc:derby:directory://", "" ) ).getParent();
//			path = Paths.get( path.toString(), "export" );
//			if ( !Files.exists( Paths.get( path.toString() ) ) )
//				Files.createDirectories( Paths.get( path.toString() ) );
//			final var filePath = Paths.get( path.toString(),"intezmeny.properties" );
//			try (var inputStream = Files.newInputStream( filePath );
//				var reader = new InputStreamReader( inputStream, StandardCharsets.UTF_8 )) {
//				intezmenyProperties.load( reader );
//			}
			final var jdbcTemplate = new JdbcTemplate( derbyDataSourceConfiguration.dataSource() );
			final var selectModell = new SelectModell( jdbcTemplate, stepRecord.sql() ).select( stepRecord.params() );
			final var arrayDataModelFormatter = new ArrayDataModelFormatter( selectModell ).setTizedesjel( "." ).setNULL( "" );

			arrayDataModelFormatter.setColumnInfo( false ).setSorszamozas( false )
				.setKeret( ArrayDataModelFormatter.KERET.SZIMPLAD ).setTopSeparator( false )
				.setHeaderCompress( false ).setBottomSeparator( false );

			stepPath = Paths.get( this.path.toString(), stepRecord.fileName().toLowerCase( Locale.ROOT ) + ".txt" );
			Files.write( Paths.get( stepPath.toString() ), arrayDataModelFormatter.getFormatted().trim().getBytes( StandardCharsets.UTF_8 ),
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE );
		}
		catch ( Exception e ) {
			log.error( "Error", e );
		}

		finally {
			log.info( "Export {} ({})", stepPath, Files.size( stepPath ) );
		}
	}

}