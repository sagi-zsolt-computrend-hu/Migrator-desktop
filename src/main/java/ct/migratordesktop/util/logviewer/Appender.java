package ct.migratordesktop.util.logviewer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.helpers.CyclicBuffer;
import lombok.Setter;

class Appender<E> extends AppenderBase<E> {

	private CyclicBuffer<E>	cb;
	@Setter
	private int							maxSize	= 1512;

	public void start() {
		cb = new CyclicBuffer<E>( maxSize );
		super.start();
	}

	public void stop() {
		cb = null;
		super.stop();
	}

	@Override
	protected void append( E eventObject ) {
		if ( !isStarted() ) {
			return;
		}
		var e = (ILoggingEvent)eventObject;
		if ( e.getLevel().isGreaterOrEqual( Level.INFO ) )
			cb.add( eventObject );
	}

	public int getLength() {
		if ( isStarted() ) {
			return cb.length();
		} else {
			return 0;
		}
	}

	public E get( int i ) {
		if ( isStarted() ) {
			return cb.get( i );
		} else {
			return null;
		}
	}

	public void reset() {
		cb.clear();
	}

}
