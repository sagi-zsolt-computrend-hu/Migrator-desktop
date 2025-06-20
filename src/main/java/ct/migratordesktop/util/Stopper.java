package ct.migratordesktop.util;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Stopper implements Serializable {
	private static final long	serialVersionUID	= 1L;
	private long							startTime;
	private long							endTime;
	private String						pattern						= "HH:mm:ss";

	public Stopper start() {
		startTime = System.currentTimeMillis();
		endTime = -1;
		return this;
	}

	public Stopper stop() {
		endTime = System.currentTimeMillis();
		return this;
	}

	public String getTime() {
		String ret = "";
		if ( startTime > 0 ) {
			ret = LocalTime.MIDNIGHT.plus( Duration.of( ((endTime == -1 ? System.currentTimeMillis() : endTime) - startTime), ChronoUnit.MILLIS ) ).format( DateTimeFormatter.ofPattern( pattern ) );
		}
		return ret;
	}

	public String getPattern() {
		return pattern;
	}

	public Stopper setPattern( String pattern ) {
		this.pattern = pattern;
		return this;
	}

	public Stopper reset() {
		startTime = -1;
		endTime = -1;
		return this;
	}
}
