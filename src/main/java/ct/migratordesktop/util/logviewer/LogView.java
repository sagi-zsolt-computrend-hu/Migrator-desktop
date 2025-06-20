package ct.migratordesktop.util.logviewer;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogView {
	private Appender<ILoggingEvent>	appender;
	private PatternLayout						layout;
	@Setter
	private boolean									reverse;
	@Setter
	private String									level;

	public LogView() {
	}

	public void reset() {
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		appender = (Appender<ILoggingEvent>)lc.getLogger(
			Logger.ROOT_LOGGER_NAME ).getAppender( LogViewInitializer.NAME );
		appender.reset();
	}

	public void init() {
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		appender = (Appender<ILoggingEvent>)lc.getLogger(
			Logger.ROOT_LOGGER_NAME ).getAppender( LogViewInitializer.NAME );
		layout = new PatternLayout();

		/*
		 *  "%black", "%red", "%green","%yellow","%blue", "%magenta","%cyan", "%white", "%gray",
		 *   "%boldRed","%boldGreen", "%boldYellow", "%boldBlue", "%boldMagenta""%boldCyan", "%boldWhite"
		 *    and "%highlight" as conversion words. 
		*/

		layout.setContext( lc );
		layout.setPattern( "%yellow(%d{HH:mm:ss.SSS}) %highlight(%.-1level) %green(%logger{36}) - %msg%xEx{3}" );
		layout.start();
	}

	public String getLog() {
		StringBuffer output = new StringBuffer( 10000 );
		printLogs( 0, output );
		return output.toString();
	}

	public String getLog( int last ) {
		StringBuffer output = new StringBuffer( 10000 );
		printLogs( last, output );
		return output.toString();
	}

	private void printLogs( int last, StringBuffer output ) {
		int count = -1;
		if ( appender != null ) {
			count = appender.getLength();
		}
		int i = 0;
		if ( count == -1 ) {
			output.append( "Failed to locate CyclicBuffer\r\n" );
		} else if ( count == 0 ) {
			output.append( "No logging events to display\r\n" );
		} else if ( reverse )
			for ( i = count - 1; i >= last; i-- ) {
				addLog( output, i );
			}
		else {
			var lastNum = 0;
			if ( last > 0 )
				if ( count > last )
					lastNum = count - last;
			for ( i = lastNum; i < count; i++ ) {
				addLog( output, i );
			}
		}
	}

	private void addLog( StringBuffer output, int i ) {
		final var loggingEvent = (LoggingEvent)appender.get( i );
		output.append( layout.doLayout( loggingEvent ) + "\n" );
	}
}