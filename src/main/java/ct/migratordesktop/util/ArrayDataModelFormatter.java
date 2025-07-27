package ct.migratordesktop.util;

import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

/**
 * @author sagizs
 *
 */
public class ArrayDataModelFormatter {
	private static final String								_NULL_							= "[n]";
	private static final String								YYYY_MM_DD_HH_MM_SS	= "yyyy.MM.dd HH:mm:ss";
	private ArrayDataModell										arrayDataModell;
	private boolean														sorszamozas;
	private boolean														columnInfo;
	private boolean														header;
	private boolean														separator;
	private boolean														topSeparator;
	private boolean														bottomSeparator;
	private CharSequence											prefix;
	private CharSequence											suffix;
	private KERET															keret;
	private boolean														headerCompress;
	private List<Row>													rows;
	private List<Column>											columns;
	private List<String>											formatted;
	private Hashtable<Integer, List<Object>>	headerExtras;
	private String														tizedesjel					= ",";

	public ArrayDataModelFormatter( ArrayDataModell arrayDataModell ) {
		this.arrayDataModell = arrayDataModell;
		headerExtras = new Hashtable<>();
		init();
	}

	private void build() {
		{//Adatok átmásolása
			this.arrayDataModell.getValues().stream().forEach( modelRow -> {
				rows.addAll( extractModelRow( modelRow ) );
			} );
			// esetleges kiegészítése
			rows.forEach( row -> {
				for ( int i = row.cells.size(); i < this.arrayDataModell.getHeaders().size(); i++ ) {
					row.cells.add( new Cell( null ) );
				}
			} );
		}
		{//columnok & header létrehozása
			for ( Object hder : this.arrayDataModell.getHeaders() ) {
				final Column column = new Column();
				Header header = new Header( column, hder );
				column.header = header;
				columns.add( column );
			}
		}
		{// columnok beállítása adatsorok alapján
			rows.forEach( row -> {
				for ( int i = 0; i < Math.min( row.cells.size(), columns.size() ); i++ ) {
					columns.get( i ).check( row.cells.get( i ), false );
				}
			} );
		}
		{// headerek beállítása
			columns.forEach( c -> c.createHeader() );
		}
		{// header extrák sorhossz beállítása
			int extraLen = 0;
			for ( Column column : columns ) {
				extraLen = Math.max( extraLen, column.header.extras.size() );
			}
			for ( Column column : columns ) {
				for ( int i = column.header.extras.size(); i < extraLen; i++ ) {
					column.header.extras.add( new Cell( "" )/*.createFormatted( column )*/ );
				}
			}
		}
		{// headerek beállítása
			for ( Column column : columns ) {
				column.header.headercompress();
				column.header.createFormatted();
				column.header.check();
			}
		}
		{//adatok formálása
			rows.forEach( row -> {
				for ( int i = 0; i < Math.min( row.cells.size(), columns.size() ); i++ ) {
					row.cells.get( i ).createFormatted( columns.get( i ) );
				}
			} );
		}
		rows.forEach( row -> {
			String formattedRow = row.cells.stream().map( c -> c.formatted ).collect( Collectors.joining( keret.fuggoleges, "", "" ) );
			this.formatted.add( formattedRow );
		} );

	}

	public String getFormatted() {
		return getFormattedAsList().stream().collect( Collectors.joining( "\n", "", "" ) );
	}

	public List<String> getFormattedAsList() {
		Builder<String> ret = Stream.builder();
		build();
		int sorszamLen = 0;
		if ( this.isSorszamozas() )
			sorszamLen = Integer.toString( formatted.size() ).trim().length();
		final String sorszamSpace = repl( sorszamLen, ' ' );
		final String sorszamKeret = this.isSorszamozas() ? keret.sorszam : "";

		if ( topSeparator )
			for ( String s : getTopSeparator() ) {
				ret.add( prefix + sorszamSpace + sorszamKeret + s + suffix );
			}

		if ( header )
			getHeader().forEach( h -> ret.add( prefix + sorszamSpace + sorszamKeret + h + suffix ) );

		if ( separator )
			getSeparator().forEach( s -> ret.add( prefix + sorszamSpace + sorszamKeret + s + suffix ) );

		int sorSzam = 0;
		for ( String s : formatted ) {
			String srsz = this.isSorszamozas() ? padLeft( ++sorSzam, sorszamLen ) : "";
			ret.add( prefix + srsz + sorszamKeret + s + suffix );
		}

		if ( bottomSeparator )
			getBottomSeparator().forEach( s -> ret.add( prefix + sorszamSpace + sorszamKeret + s + suffix ) );

		final Pattern RTRIM = Pattern.compile( "\\s+$" );
		return ret.build().map( s -> RTRIM.matcher( s ).replaceAll( "" ) ).collect( Collectors.toList() );
	}

	protected List<Row> extractModelRow( List<Object> modelRow ) {
		List<Row> ret = new ArrayList<Row>();
		int rowNum = 1;
		for ( Object value : modelRow ) {
			if ( value instanceof List<?> ) {
				rowNum = Math.max( rowNum, ((List<?>)value).size() );
			}
		}
		for ( int i = 0; i < rowNum; i++ ) {
			Row row = new Row( modelRow.size() );
			ret.add( row );
		}
		for ( int i = 0; i < modelRow.size(); i++ ) {
			Object value = modelRow.get( i );
			if ( value instanceof List<?> ) {
				List<?> list = ((List<?>)value);
				for ( int j = 0; j < list.size(); j++ ) {
					ret.get( j ).cells.get( i ).value = list.get( j );
				}
			} else
				ret.get( 0 ).cells.get( i ).value = value;

		}
		return ret;
	}

	private void init() {
		this.formatted = new ArrayList<>();
		this.columns = new ArrayList<>();
		this.rows = new ArrayList<>();
		this.sorszamozas = true;
		this.columnInfo = true;
		this.header = true;
		this.separator = true;
		this.prefix = "";
		this.suffix = "";
		this.keret = KERET.SZIMPLA;
		this.headerCompress = true;
	}

	private String padRight( Object value, Number size ) {
		return String.format( "%1$-" + size.toString() + "s", value );
	}

	private String padLeft( Object value, int size ) {
		return String.format( "%1$" + size + "s", value );
	}

	private List<String> getSeparator() {
		List<String> ret = new ArrayList<>();
		Builder<String> b = Stream.builder();
		for ( int i = 0; i < columns.size(); i++ ) {
			String space = new String( new char[columns.get( i ).columnLenght] ).replace( '\0', keret.vizszintes.charAt( 0 ) );
			b.add( space );
		}
		ret.add( b.build().collect( Collectors.joining( keret.kereszt, "", "" ) ) );
		return ret;
	}

	private List<String> getTopSeparator() {
		List<String> ret = new ArrayList<>();
		Builder<String> b = Stream.builder();
		for ( int i = 0; i < columns.size(); i++ ) {
			String space = new String( new char[columns.get( i ).columnLenght] ).replace( '\0', keret.vizszintes.charAt( 0 ) );
			b.add( space );
		}
		ret.add( b.build().collect( Collectors.joining( keret.top, "", "" ) ) );
		return ret;
	}

	private List<String> getBottomSeparator() {
		List<String> ret = new ArrayList<>();
		Builder<String> b = Stream.builder();
		for ( int i = 0; i < columns.size(); i++ ) {
			String space = new String( new char[columns.get( i ).columnLenght] ).replace( '\0', keret.vizszintes.charAt( 0 ) );
			b.add( space );
		}
		ret.add( b.build().collect( Collectors.joining( keret.bottom, "", "" ) ) );
		return ret;
	}

	private List<String> getHeader() {
		List<String> ret = new ArrayList<>();
		{
			Builder<String> b1 = Stream.builder();
			for ( Column column : columns ) {
				b1.add( column.header.header1.formatted );
			}
			ret.add( b1.build().collect( Collectors.joining( keret.fuggoleges, "", "" ) ) );
		}
		{
			for ( Column col : columns ) {
				if ( col.header.header2.formatted.trim().length() > 0 ) {
					Builder<String> b = Stream.builder();
					for ( Column column : columns ) {
						b.add( column.header.header2.formatted );
					}
					ret.add( b.build().collect( Collectors.joining( keret.fuggoleges, "", "" ) ) );
					break;
				}
			}
		}
		{
			int extraLen = 0;
			for ( Column column : columns ) {
				extraLen = Math.max( extraLen, column.header.extras.size() );
			}
			if ( extraLen > 0 )
				for ( int i = 0; i < extraLen; i++ ) {
					Builder<String> b1 = Stream.builder();
					for ( Column column : columns ) {
						try {
							b1.add( column.header.extras.get( i ).formatted );
						}
						catch ( Exception e ) {
						}
					}
					ret.add( b1.build().collect( Collectors.joining( keret.fuggoleges, "", "" ) ) );
				}
		}

		if ( columnInfo ) {
			Builder<String> b = Stream.builder();
			for ( Column column : columns ) {
				b.add( column.header.colType.formatted );
			}
			ret.add( b.build().collect( Collectors.joining( keret.fuggoleges, "", "" ) ) );
		}
		if ( columnInfo ) {
			Builder<String> b = Stream.builder();
			for ( Column column : columns ) {
				b.add( column.header.colInfo.formatted );
			}
			ret.add( b.build().collect( Collectors.joining( keret.fuggoleges, "", "" ) ) );
		}

		return ret;
	}

	private String repl( int length, char ch ) {
		if ( length < 0 )
			return "";
		else
			return CharBuffer.allocate( length ).toString().replace( '\0', ch );
	}

	public boolean isSorszamozas() {
		return sorszamozas;
	}

	public ArrayDataModelFormatter setSorszamozas( boolean sorszamozas ) {
		this.sorszamozas = sorszamozas;
		return this;
	}

	public boolean isColumnInfo() {
		return columnInfo;
	}

	public ArrayDataModelFormatter setColumnInfo( boolean columnInfo ) {
		this.columnInfo = columnInfo;
		return this;
	}

	public boolean isHeader() {
		return header;
	}

	public ArrayDataModelFormatter setHeader( boolean header ) {
		this.header = header;
		return this;
	}

	public boolean isSeparator() {
		return separator;
	}

	public ArrayDataModelFormatter setSeparator( boolean separator ) {
		this.separator = separator;
		return this;
	}

	public CharSequence getPrefix() {
		return prefix;
	}

	public ArrayDataModelFormatter setPrefix( CharSequence prefix ) {
		this.prefix = prefix;
		return this;
	}

	public CharSequence getSuffix() {
		return suffix;
	}

	public ArrayDataModelFormatter setSuffix( CharSequence suffix ) {
		this.suffix = suffix;
		return this;
	}

	public boolean isHeaderCompress() {
		return headerCompress;
	}

	public ArrayDataModelFormatter setHeaderCompress( boolean headerCompress ) {
		this.headerCompress = headerCompress;
		return this;
	}

	public KERET getKeret() {
		return keret;
	}

	public ArrayDataModelFormatter setKeret( KERET keret ) {
		this.keret = keret;
		return this;
	}

	public ArrayDataModelFormatter addHeaderExtra( int colNum, Object value ) {
		if ( false == headerExtras.containsKey( colNum ) )
			headerExtras.put( colNum, new ArrayList<Object>() );
		headerExtras.get( colNum ).add( value );
		return this;
	}

	public boolean isTopSeparator() {
		return topSeparator;
	}

	public ArrayDataModelFormatter setTopSeparator( boolean topSeparator ) {
		this.topSeparator = topSeparator;
		return this;
	}

	public boolean isBottomSeparator() {
		return bottomSeparator;
	}

	public ArrayDataModelFormatter setBottomSeparator( boolean bottomSeparator ) {
		this.bottomSeparator = bottomSeparator;
		return this;
	}
	
	public ArrayDataModelFormatter setTizedesjel( String tizedesjel ) {
		this.tizedesjel = tizedesjel;
		return this;
	}

	class Row {
		public List<Cell> cells = new ArrayList<>();

		public Row( int size ) {
			for ( int i = 0; i < size; i++ ) {
				cells.add( new Cell( "" ) );
			}
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append( cells );
			return builder.toString();
		}

	}

	class Cell {
		private Object	value;
		private String	formatted;

		public Cell( Object value ) {
			this.value = value;
		}

		public Cell createFormatted( Column column ) {
			if ( value == null ) {
				formatted = padRight( _NULL_, column.columnLenght );
			} else if ( column.egeszLenght > -1 ) {
				String egesz = getEgesz( value );
				String stringValue = "";
				stringValue += egesz;
				if ( column.tizedesLenght > 0 ) {
					String tizedes = getTizedes( value );
					if ( !tizedes.isEmpty() ) {
						stringValue += tizedesjel;
						stringValue += tizedes;
						stringValue += repl( column.tizedesLenght - tizedes.length(), ' ' );
						stringValue = repl( column.columnLenght - stringValue.length(), ' ' ) + stringValue;
					} else {
						stringValue += repl( column.tizedesLenght + 1, ' ' );
						stringValue = repl( column.columnLenght - stringValue.length(), ' ' ) + stringValue;
					}
				} else {
					stringValue = repl( column.columnLenght - stringValue.length(), ' ' ) + stringValue;
				}

				formatted = stringValue;
			} else if ( value instanceof Date ) {
				String datum = new java.sql.Timestamp( ((Date)value).getTime() ).toLocalDateTime().format( DateTimeFormatter.ofPattern( YYYY_MM_DD_HH_MM_SS ) );
				formatted = padRight( datum, column.columnLenght );
			} else
				formatted = padRight( value, column.columnLenght );
			return this;

		}

		private String getEgesz( Object cell ) {
			String ret = "";
			if ( cell != null && cell instanceof Number ) {
				final BigDecimal bigDecimal = new BigDecimal( cell.toString() );
				String[] szamok = bigDecimal.toPlainString().split( "\\." );
				ret = szamok[0];
			}
			return ret;
		}

		private String getTizedes( Object cell ) {
			String ret = "";
			if ( cell != null && cell instanceof Number ) {
				final BigDecimal bigDecimal = new BigDecimal( cell.toString() );
				String[] szamok = bigDecimal.toPlainString().split( "\\." );
				if ( szamok.length == 2 )
					ret = szamok[1].trim().replaceAll( "0*$", "" );
			}
			return ret;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append( "Cell [value=" ).append( value ).append( "]" );
			return builder.toString();
		}
	}

	class Column {
		private int			dataLenght		= -1;
		private int			columnLenght	= -1;
		private String	type					= "?";
		private int			tizedesLenght	= -1;
		private int			egeszLenght		= -1;
		private Header	header;

		public Column() {
		}

		private void createHeader() {
			header.build();
		}

		private void check( Cell cell, boolean isHeader ) {
			if ( cell != null )
				check( cell.value, isHeader );
		}

		private void check( List<Cell> cells, boolean isHeader ) {
			cells.stream().filter( c -> c != null ).forEach( c -> check( c.value, isHeader ) );
		}

		private void check( Object value, boolean isHeader ) {
			if ( value != null ) {
				if ( type.equalsIgnoreCase( "?" ) )
					type = value.getClass().getSimpleName()
						.replace( "BigDecimal", "BigD" ).replace( "String", "Str" ).replace( "BigInteger", "BigI" );
				if ( /*value instanceof oracle.sql.TIMESTAMP ||*/ value instanceof Date ) {
					columnLenght = Math.max( columnLenght, YYYY_MM_DD_HH_MM_SS.length() );
					dataLenght = Math.max( dataLenght, YYYY_MM_DD_HH_MM_SS.length() );
				} else if ( value instanceof Number ) {
					String egesz = getEgesz( value );
					String tizedes = getTizedes( value );
					egeszLenght = Math.max( egeszLenght, egesz.length() );
					tizedesLenght = Math.max( tizedesLenght, tizedes.length() );
					columnLenght = Math.max( columnLenght, egeszLenght + tizedesLenght + (tizedes.isEmpty() ? 0 : 1) );
					if ( !isHeader )
						dataLenght = Math.max( dataLenght, egeszLenght + tizedesLenght + (tizedes.isEmpty() ? 0 : 1) );
				} else {
					columnLenght = Math.max( columnLenght, value.toString().length() );
					if ( !isHeader )
						dataLenght = Math.max( dataLenght, value.toString().length() );
				}
			} else {
				columnLenght = Math.max( columnLenght, _NULL_.length() );
				if ( !isHeader )
					dataLenght = Math.max( dataLenght, _NULL_.length() );
			}
		}

		private String getEgesz( Object value ) {
			String ret = "";
			if ( value != null && value instanceof Number ) {
				final BigDecimal bigDecimal = new BigDecimal( value.toString() );
				String[] szamok = bigDecimal.toPlainString().split( "\\." );
				ret = szamok[0];
			}
			return ret;
		}

		private String getTizedes( Object value ) {
			String ret = "";
			if ( value != null && value instanceof Number ) {
				final BigDecimal bigDecimal = new BigDecimal( value.toString() );
				String[] szamok = bigDecimal.toPlainString().split( "\\." );
				if ( szamok.length == 2 )
					ret = szamok[1].trim().replaceAll( "0*$", "" );
			}

			return ret;
		}
	}

	class Header {
		private String			orig;
		private Cell				header1;
		private Cell				header2;
		private Cell				colInfo;
		private Cell				colType;
		private Column			column;
		private List<Cell>	extras;

		public Header( Column column, Object orig ) {
			this.orig = orig == null ? _NULL_ : orig.toString().trim();
			this.column = column;
			header1 = new Cell( "" );
			header2 = new Cell( "" );
			colInfo = new Cell( "" );
			colType = new Cell( "" );
		}

		public void build() {
			header1.value = orig == null ? _NULL_ : orig.toString().trim();
			this.extras = new ArrayList<>();
			if ( headerExtras.containsKey( columns.indexOf( this.column ) ) )
				headerExtras.get( columns.indexOf( this.column ) ).stream().forEach( e -> this.extras.add( new Cell( e ) ) );
			this.column.check( this.extras, true );
			if ( columnInfo ) {
				{
					colType.value = column.type;
					this.column.check( this.colType, true );
				}
				{
					String info = "";
					if ( colType.value.toString().equalsIgnoreCase( "?" ) ) {
					} else {
						info += column.columnLenght;
						if ( column.egeszLenght > -1 ) {
							info += "(" + column.egeszLenght;
							if ( column.egeszLenght > -1 )
								info += "," + column.tizedesLenght;
							info += ")";
						} else {
							info += "(" + column.dataLenght + ")";
						}
					}
					colInfo.value = info;
					this.column.check( this.colInfo, true );
				}
			}
			check();
		}

		protected void check() {
			this.column.check( this.extras, true );
			this.column.check( this.colType, true );
			this.column.check( this.colInfo, true );
		}

		protected void createFormatted() {
			this.createFormatted( header1 );
			this.createFormatted( header2 );
			this.createFormatted( colInfo );
			this.createFormatted( colType );
			this.createFormatted( extras );
		}

		protected void headercompress() {
			if ( headerCompress ) {
				if ( column.columnLenght < orig.length() ) {
					if ( orig.length() > 4 ) {
						int valaszto = orig.length() / 2;
						header1.value = orig.substring( 0, valaszto );
						header2.value = orig.substring( valaszto );
					}
				}
			} else {
				header1.value = orig;
				header2.value = "";
			}
			this.column.check( this.header1, true );
			this.column.check( this.header2, true );
		}

		private void createFormatted( List<Cell> rows ) {
			rows.stream().forEach( c -> createFormatted( c ) );
		}

		private void createFormatted( Cell cell ) {
			cell.formatted = padRight( cell.value, this.column.columnLenght );

		}
	}

	public enum KERET {
		//https://www.fileformat.info/info/unicode/block/box_drawing/list.htm
		/*
			------------------------------------
			╏ ╍╎╌┆╳▕
			⏸
			
			X1234567890
			X▬▬▬▬▬▬▬▬▬▬
			X1234567890
			X▐▐▐▐▐▐▐▐▐▐
			X1234567890
			X▌▌▌▌▌▌▌▌▌▌
			X1234567890
			X▉▉▉▉▉▉▉▉▉▉
			X1234567890
			X▄▄▄▄▄▄▄▄▄▄
			X1234567890
			X▀▀▀▀▀▀▀▀▀▀
			X1234567890
			Xǀǀǀǀǀǀǀǀǀǀ
			X1234567890
			Xǁǁǁǁǁǁǁǁǁǁ
			
			
		 */
		// @formatter:off
	SZIMPLA    ("║","│","─","┼","┬","┴"),
	SZIMPLAD   ("║","│","═","╪","╤","╧"),
	ALSOS      ("╳","¦","_","_","_","_"),
	KARAKTER   ("|","|","-","+","-","-"),
	KARAKTER2  ("ǁ","ǀ","-","ǀ","ǀ","ǀ"),
	DUPLA      ("│","║","═","╬","╦","╩"),
	VASTAG     ("▐","▌","▄","▄","▄","▄"),
	SZOKOZ     (" "," "," "," "," "," "),
	PROPERTY   (" ",":","-","+","-","-")
	; // @formatter:on

		public final String	sorszam;
		public final String	fuggoleges;
		public final String	vizszintes;
		public final String	kereszt;
		public final String	top;
		public final String	bottom;

		KERET( String sorszam, String fuggoleges, String vizszintes, String kereszt, String top, String bottom ) {
			this.sorszam = sorszam;
			this.fuggoleges = fuggoleges;
			this.vizszintes = vizszintes;
			this.kereszt = kereszt;
			this.top = top;
			this.bottom = bottom;
		}
	}
}