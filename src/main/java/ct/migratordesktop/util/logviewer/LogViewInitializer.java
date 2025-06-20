package ct.migratordesktop.util.logviewer;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component

public class LogViewInitializer implements CommandLineRunner, Ordered {
	private static final int MAXSIZE = 1500;
	public static String NAME = "Migrator";
	public LogViewInitializer() {
	}

	@Override
	public void run( String... args ) throws Exception {
			final var loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();

			Appender<ILoggingEvent> appender = new Appender<ILoggingEvent>();
			appender.setContext( loggerContext );
			appender.setName( NAME );
			appender.setMaxSize( MAXSIZE );
			appender.start();
			Logger logger = (Logger)LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME );
			logger.addAppender( appender );//logger.setAdditive(true); 
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}
}
