package interval;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntervalTest {

	public static void main( String[] args ) {
		List<Interval> intervals = new ArrayList<>();
		intervals.add( new Interval( LocalDate.of( 2024, 1, 3 ), LocalDate.of( 2024, 1, 10 ) ) );
		
		intervals.add( new Interval( LocalDate.of( 2024, 2, 5 ), LocalDate.of( 2024, 2, 15 ) ) );
		intervals.add( new Interval( LocalDate.of( 2024, 2, 25 ), LocalDate.of( 2024, 2, 25 ) ) );
		
		intervals.add( new Interval( LocalDate.of( 2024, 3, 5 ), LocalDate.of( 2026, 2, 15 ) ) );
		//		intervals.add( new Interval( LocalDate.of( 2024, 3, 20 ), LocalDate.of( 2024, 3, 25 ) ) );
		//		intervals.add( new Interval( LocalDate.of( 2024, 3, 25 ), LocalDate.of( 2024, 4, 25 ) ) );
		//intervals.add( new Interval( LocalDate.of( 2014, 1, 1 ), LocalDate.of( 2024, 1, 1 ) ) );

		intervals.add( new Interval( LocalDate.of( 2026, 11, 3 ), LocalDate.of( 2026, 12, 10 ) ) );
		intervals.add( new Interval( LocalDate.of( 2049, 1, 3 ), null ));
		System.out.println(
			hasOverlappingIntervals( intervals ) );
//		System.out.println("hasOverlappingIntervals :"+
//			hasOverlappingIntervals( mergeOverlappingIntervals( intervals ) ) );

		//printIntervalsTimeline(intervals,LocalDate.of( 2026, 1, 1 ),LocalDate.of( 2034, 12, 1 ));
	}

	public static List<Interval> mergeOverlappingIntervals( List<Interval> intervals ) {
		if ( intervals.isEmpty() )
			return Collections.emptyList();

		// Sort by start date
		intervals.sort( Comparator.comparing( i -> i.getStart() ) );

		List<Interval> merged = new ArrayList<>();
		Interval prev = intervals.get( 0 );

		for ( int i = 1; i < intervals.size(); i++ ) {
			Interval curr = intervals.get( i );
			if ( !curr.getStart().isAfter( prev.getEnd() ) ) { // Overlaps or touches
				prev = new Interval( prev.getStart(),
					curr.getEnd().isAfter( prev.getEnd() ) ? curr.getEnd() : prev.getEnd() );
			} else {
				merged.add( prev );
				prev = curr;
			}
		}
		merged.add( prev );
		return merged;
	}

	public static void printIntervalsTimeline( List<Interval> intervals, LocalDate min, LocalDate max ) {
		for ( Interval interval : intervals ) {
			StringBuilder line = new StringBuilder();
			for ( LocalDate date = min; !date.isAfter( max ); date = date.plusDays( 1 ) ) {
				if ( !date.isBefore( interval.getStart() ) && !date.isAfter( interval.getEnd() ) ) {
					line.append( "#" );
				} else {
					line.append( "." );
				}
			}
			System.out.println( line + " " + interval.getStart() + " to " + interval.getEnd() );
		}
	}

	public static boolean hasOverlappingIntervals( List<Interval> intervals ) {
		if ( intervals.size() < 2 )
			return false;
		intervals.sort( Comparator.comparing( Interval::getStart ) );
		for ( int i = 1; i < intervals.size(); i++ ) {
			if ( !intervals.get( i ).getStart().isAfter( intervals.get( i - 1 ).getEnd() ) ) {
				return true;
			}
		}
		return false;
	}
}
