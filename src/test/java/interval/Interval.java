package interval;

import java.time.LocalDate;
import java.util.Objects;

public class Interval {
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Interval [start=" );
		builder.append( start );
		builder.append( ", end=" );
		builder.append( end );
		builder.append( "]" );
		return builder.toString();
	}

	private final LocalDate	start;
	public LocalDate getStart() {
		return start;
	}

	public LocalDate getEnd() {
		return end;
	}

	private LocalDate	end;

	public Interval( LocalDate start, LocalDate end ) {
		this.start = start;
		this.end = end;
		if (Objects.isNull( this.end ))
			this.end = LocalDate.MAX;
	}

	public boolean contains( LocalDate date ) {
		return (date.equals( start ) || date.isAfter( start )) &&
			(date.equals( end ) || date.isBefore( end ));
	}
}