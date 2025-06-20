package ct.migratordesktop;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
//import liquibase.integration.spring.SpringLiquibase;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "ct.migratordesktop","ct.akkor"})
public class MigratorDesktopApplication {
	private static final String _CONFIG_LOCATION = "migrator.config.location";

	public static void main( String[] args ) {
		String configLocation = System.getenv( _CONFIG_LOCATION );
		if ( Objects.isNull( configLocation ) )
			configLocation = System.getProperty( _CONFIG_LOCATION );
		if ( Objects.nonNull( configLocation ) ) {
			File file = new File( configLocation.replace( "file:///", "" ) );
			if ( false == Files.isReadable( file.toPath() ) ) {
				System.err.println( "Fatal Error" );
				System.err.println( "Config file not found or not readable: " + file.getAbsolutePath() );
				System.err.println( "System.exit( 1 )" );
				System.exit( 1 );
			}
		}

		if ( Objects.nonNull( configLocation ) )
			System.setProperty( "spring.config.location", configLocation );

		String jansi = System.getProperty( "jansi", "false" );
		if ( Boolean.valueOf( jansi.trim() ) )
			AnsiConsole.systemInstall();
		else {
			while ( AnsiConsole.isInstalled() ) {
				AnsiConsole.systemUninstall();//org.fusesource.jansi.AnsiMain.main( "" );
			}
		}
		//		SpringApplication application = new SpringApplication( Migrator2Application.class );
		//		application.run( args );
		try {
			UIManager.setLookAndFeel( new FlatMacDarkLaf() );
		}
		catch ( UnsupportedLookAndFeelException e ) {
			e.printStackTrace();
		}
		new SpringApplicationBuilder( MigratorDesktopApplication.class ).headless( false ).run( args );
	}
//	@Bean
//	public SpringLiquibase liquibase() {
//		SpringLiquibase liquibase = new SpringLiquibase();
//		liquibase.setShouldRun( false );
//		return liquibase;
//	}
}
