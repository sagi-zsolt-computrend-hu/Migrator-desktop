package ct.migratordesktop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * @author sagizs
 *
 */
public class ArrayDataModell implements Serializable {

	public ArrayDataModell() {
		clear();
	}

	private static final long		serialVersionUID	= 1L;
	private List<List<Object>>	values;
	private List<Object>				headers;
	private List<Object>				row;

	public ArrayDataModell addHeader( Object... header ) {
		Arrays.stream( header ).forEach( e -> addHeader( e ) );
		return this;
	}

			public ArrayDataModell addHeader( Enum<?>... header ) {
				Arrays.stream( header ).forEach( e -> addHeader( e.name() ) );
				return this;
			}

	public ArrayDataModell addHeader( Object header ) {
		headers.add( header );
		return this;
	}

	public void clear() {
		this.values = new ArrayList<>();
		this.headers = new ArrayList<>();
		this.row = new ArrayList<>();
	}

	public ArrayDataModell add( Object cell ) {
		row.add( cell );
		if ( row.size() == headers.size() ) {
			List<Object> r = new ArrayList<>();
			row.stream().forEach( c -> r.add( c ) );
			addRow( r );
			row.clear();
		}
		return this;
	}

	public ArrayDataModell addHeader( List<Object> headers ) {
		headers.stream().forEach( h -> addHeader( h ) );
		return this;
	}

	public ArrayDataModell addRow( List<Object> row ) {
		values.add( row );
		return this;
	}

	public List<List<Object>> getValues() {
		return values;
	}

	public List<Object> getHeaders() {
		return headers;
	}

	public String getFormatted() {
		ArrayDataModelFormatter arrayDataModelFormatter = new ArrayDataModelFormatter( this );
		return arrayDataModelFormatter.getFormatted();
	}

	public void append( ArrayDataModell appendDml ) {
		appendDml.getValues().stream().forEach( r -> this.values.add( r ) );
	}

	public int getColumnIndex( Object header ) {
		Integer ret = null;
		for ( int i = 0; i < headers.size(); i++ ) {
			if ( Objects.equals( header, headers.get( i ) ) ) {
				ret = i;
				break;
			}
		}
		return Optional.ofNullable( ret ).orElseThrow( ()->new NoSuchElementException(  "Az adatmodellben oszlop nem található:" + header ) );
		//return ret;
	}

	public Object getCell( int row, Object header ) {
		Object ret = "" ; 
		final int columnIndex = getColumnIndex( header );
		try {
			ret = values.get( row ).get( columnIndex );
		}
		catch ( Exception e ) {
			if (e instanceof IndexOutOfBoundsException) 
				throw new NoSuchElementException(  "Az adatmodellben sor nem található:" + row );
		}
		return ret;
	}

//	public <T> T getCell( int row, final Class<T> clazz, Object header ) {
//		T ret = (T)getCell( row, header );
//
//		return ret;
//	}

}
