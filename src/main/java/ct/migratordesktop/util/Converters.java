package ct.migratordesktop.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import lombok.SneakyThrows;

public interface Converters {
	public static final LocalDate					OF1970										= LocalDate.EPOCH;//LocalDate.of( 1970, 1, 1 );
	public static final String						P_YYYYpMMpDD							= "yyyy.MM.dd";
	public static final String						P_YYYYMM									= "yyyyMM";
	public static final String						P_YYYYMMDD								= "yyyyMMdd";
	public static final String						P_HHkMMkSS								= "HH:mm:ss";
	public static final String						P_YYYYpMMpDDsHHkMMkSS			= P_YYYYpMMpDD + " " + P_HHkMMkSS;
	public static final DateTimeFormatter	YYYYmMMmDDspHHkMMksspSSS	= DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss.SSS" );
	public static final DateTimeFormatter	YYYYpMMpDDspHHkMMksspSSS	= DateTimeFormatter.ofPattern( "yyyy.MM.dd HH:mm:ss.SSS" );
	public static final DateTimeFormatter	YYYYpMMpDDspHHkMMkss			= DateTimeFormatter.ofPattern( P_YYYYpMMpDDsHHkMMkSS );
	public static final DateTimeFormatter	YYYYmMMmDD_HHmMMmss				= DateTimeFormatter.ofPattern( "yyyy-MM-dd_HH-mm-ss" );
	public static final DateTimeFormatter	YYpMMpDDspHHkMM						= DateTimeFormatter.ofPattern( "yy.MM.dd HH:mm" );
	public static final DateTimeFormatter	YYpMMpDDspHHpMM						= DateTimeFormatter.ofPattern( "yy.MM.dd HH.mm" );
	public static final DateTimeFormatter	YYYYMM										= DateTimeFormatter.ofPattern( P_YYYYMM );
	public static final DateTimeFormatter	YYYYMMDD									= DateTimeFormatter.ofPattern( P_YYYYMMDD );
	public static final DateTimeFormatter	YYYYpMMpDD								= DateTimeFormatter.ofPattern( P_YYYYpMMpDD );
	public static final DateTimeFormatter	HHkmmksspSSS							= DateTimeFormatter.ofPattern( "HH:mm:ss.SSS" );
	public static final DateTimeFormatter	HHkmmksspSS								= DateTimeFormatter.ofPattern( "HH:mm:ss.SS" );
	public static final DateTimeFormatter	HHkMMkss									= DateTimeFormatter.ofPattern( P_HHkMMkSS );
	public static final String						ISCRLF										= "\\r?\\n";
	public static final String						CRLF											= "\r\n";
	public static final String						validChars								= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_öüóőúéáűíÖÜÓŐÚÉÁŰÍ";
	public static final String						REMOVEDOTZERO							= "REMOVEDOTZERO";
	public static final String						PADLEFT										= "PADLEFT";

	default java.sql.Date convertToSqlDate( Date dateToConvert ) {
		return new java.sql.Date( dateToConvert.getTime() );
	}

	default Timestamp convertToSqlTimeStamp( Date dateToConvert ) {
		if ( Objects.nonNull( dateToConvert ) )
			return new Timestamp( dateToConvert.getTime() );
		else
			return null;
	}

	default Timestamp convertToSqlTimeStamp( LocalDateTime localDateTime ) {
		if ( Objects.nonNull( localDateTime ) )
			return java.sql.Timestamp.valueOf( localDateTime );
		else
			return null;

	}

	default Date convertToDate( LocalDate dateToConvert ) {
		return java.sql.Date.valueOf( dateToConvert );
	}

	default Date convertToDate( LocalDateTime dateToConvert ) {
		return java.sql.Timestamp.valueOf( dateToConvert );
	}

	default LocalDate convertToLocalDate( Date dateToConvert ) {
		if ( Objects.nonNull( dateToConvert ) )
			return new java.sql.Date( dateToConvert.getTime() ).toLocalDate();
		else
			return null;
	}

	default LocalDateTime convertToLocalDateTime( Date dateToConvert ) {
		if ( Objects.nonNull( dateToConvert ) )
			return new java.sql.Timestamp( dateToConvert.getTime() ).toLocalDateTime();
		else
			return null;
	}

	default String repeat( String str, int n ) {
		return Stream.generate( () -> str ).limit( n ).collect( Collectors.joining() );
		//String.join("", Collections.nCopies(n, str))
	}

	default String timeBetweenStartNow( long startTime, DateTimeFormatter formatter ) {
		return timeBetweenStartEnd( startTime, System.currentTimeMillis(), formatter );
	}

	default String timeBetweenStartEnd( long startTime, long endTime, DateTimeFormatter formatter ) {
		String ret = LocalTime.MIDNIGHT.plus( Duration.of( (endTime - startTime), ChronoUnit.MILLIS ) ).format( formatter );
		return ret;
	}

	default String toCamelCase( String text, String extra, boolean firstUpper ) {
		Builder<String> ret = Stream.builder();
		String[] words = text.split( "[\\W_]+" );
		for ( int i = 0; i < words.length; i++ ) {
			String word = words[i];
			if ( i == 0 && false == firstUpper )
				word = word.isEmpty() ? word : word.toLowerCase( Locale.getDefault() );
			else
				word = word.isEmpty() ? word : Character.toUpperCase( word.charAt( 0 ) ) + word.substring( 1 ).toLowerCase( Locale.getDefault() );
			ret.add( word );
		}
		return ret.build().collect( Collectors.joining( extra ) );
	}

	default String toFileName( String str, boolean addTimeStamp ) {
		StringBuilder fileName = new StringBuilder();

		for ( int i = 0; i < str.toCharArray().length; i++ ) {
			if ( validChars.contains( Character.toString( str.toCharArray()[i] ) ) )
				fileName.append( Character.toString( str.toCharArray()[i] ) );
			else
				fileName.append( "_" );
		}
		String ret = fileName.toString();
		ret = ret.replaceAll( "[_]{2,}", "_" ).replaceAll( "^[_]{1,}", "" ).replaceAll( "[_]{1,}$", "" );
		if ( addTimeStamp )
			ret += "_" + LocalDateTime.now().format( YYYYmMMmDD_HHmMMmss );
		return ret;
	}

	@SuppressWarnings({ "unchecked" })
	default <T> T conv( String value, final Class<T> clazz, String... extraPars ) throws Exception {
		Object ret = null;
		if ( value != null && false == value.equalsIgnoreCase( "[n]" ) )
			if ( clazz == Double.class )
				ret = Double.parseDouble( value );
			else if ( clazz == Boolean.class )
				ret = Boolean.valueOf( value );
			else if ( clazz == Integer.class )
				ret = Integer.valueOf( value.replaceAll( "\\..*", "" ) );
			else if ( clazz == Long.class )
				ret = Long.valueOf( value );
			else if ( clazz == Float.class )
				ret = Float.valueOf( value );
			else if ( clazz == BigInteger.class )
				ret = BigInteger.valueOf( Long.parseLong( value ) );
			else if ( clazz == BigDecimal.class )
				ret = BigDecimal.valueOf( Long.parseLong( value ) );
			else if ( clazz == Date.class && extraPars.length == 0 ) {
				ret = DateFormat.getDateTimeInstance().parse( value );
			} else if ( clazz == Date.class && extraPars.length == 1 ) {
				if ( false == value.trim().isEmpty() ) {
					LocalDateTime parse = LocalDateTime.parse( value, getPattern( extraPars[0] ) );
					ret = new Date( java.sql.Timestamp.valueOf( parse ).getTime() );
				}
			} else
				ret = value;
		return (T)ret;
	}

	@SuppressWarnings({ "unchecked" })
	default <T> T conv( Object value, final Class<T> clazz, Object... extraPars ) throws Exception {
		Object ret = null;
		if ( value != null ) {
			String strValue = value.toString();
			if ( clazz == String.class ) {
				final List<Object> asList = Arrays.asList( extraPars );
				if ( asList.contains( REMOVEDOTZERO ) )
					strValue = strValue.replace( ".0", "" );
				int iPadLeft = asList.indexOf( PADLEFT );
				if ( iPadLeft > -1 )
					strValue = padLeft( strValue, (Integer)extraPars[iPadLeft + 1], (String)extraPars[iPadLeft + 2] );
				ret = strValue;
			} else if ( clazz == Double.class )
				ret = Double.parseDouble( strValue );
			else if ( clazz == Boolean.class )
				ret = Boolean.valueOf( strValue );
			else if ( clazz == Integer.class )
				ret = Integer.valueOf( strValue.replaceAll( "\\..*", "" ) );
			else if ( clazz == Long.class )
				ret = Long.valueOf( strValue );
			else if ( clazz == Float.class )
				ret = Float.valueOf( strValue );
			else if ( clazz == BigInteger.class )
				ret = BigInteger.valueOf( Long.parseLong( strValue ) );
			else if ( clazz == BigDecimal.class )
				ret = BigDecimal.valueOf( Long.parseLong( strValue ) );
			else if ( clazz == Date.class && extraPars.length == 0 ) {
				ret = DateFormat.getDateTimeInstance().parse( strValue );
			} else if ( clazz == Date.class && extraPars.length == 1 ) {
				if ( false == strValue.trim().isEmpty() ) {
					LocalDateTime parse = LocalDateTime.parse( strValue, getPattern( (String)extraPars[0] ) );
					ret = new Date( java.sql.Timestamp.valueOf( parse ).getTime() );
				}
			} else
				ret = value;
		}
		return (T)ret;
	}

	default DateTimeFormatter getPattern( String pattern ) {
		DateTimeFormatter ret = new DateTimeFormatterBuilder()
			.appendPattern( pattern )
			.parseDefaulting( ChronoField.MONTH_OF_YEAR, 1 ).parseDefaulting( ChronoField.DAY_OF_MONTH, 1 )
			.parseDefaulting( ChronoField.HOUR_OF_DAY, 0 ).parseDefaulting( ChronoField.MINUTE_OF_HOUR, 0 )
			.parseDefaulting( ChronoField.SECOND_OF_MINUTE, 0 )
			.toFormatter();
		return ret;
	}

	default String trimRight( Object value ) {
		String ret = (String)value;
		int endIndex = 0;
		for ( int i = ret.length(); i > 0; i-- ) {
			final char charAt = ret.charAt( i - 1 );
			if ( false == Character.isWhitespace( charAt ) ) {
				endIndex = i;
				break;
			}
		}
		return ret.substring( 0, endIndex );
	}

	default String trimLeft( Object value ) {
		String ret = (String)value;
		int beginIndex = ret.length();

		for ( int i = 0; i < ret.length(); i++ ) {
			final char charAt = ret.charAt( i );
			if ( false == Character.isWhitespace( charAt ) ) {
				beginIndex = i;
				break;
			}
		}
		return ret.substring( beginIndex );
	}

	default String toFormat( Object... values ) {
		return Arrays.asList( values ).stream().map( v -> {
			String ret = "";
			try {
				ret = conv( v, String.class );
			}
			catch ( Exception e ) {
				ret = "Err.";
			}
			return ret;
		} ).collect( Collectors.joining( "|", "[", "]" ) );
	}

	default String replaceFull( String value, String target, String replacement ) {
		while ( value.contains( target ) ) {
			value = value.replace( target, replacement );
		}
		return value;
	}

	default String padLeft( Object value, int size ) {
		return padLeft( value, size, " " );
	}

	default String padLeft( Object value, int size, String padChar ) {
		String ret = Objects.toString( value, "" ).trim();
		if ( ret.length() < size )
			ret = String.format( "%" + size + "s", ret );
		ret = ret.replace( " ", padChar );
		return ret;
	}
	
	default String padRight( Object value, int size ) {
		return padRight( value, size, " " );
	}

	default String padRight( Object value, int size , String padChar ) {
		String ret = Objects.toString( value, "" );
		if ( ret.length() < size )
			ret = String.format( "%1$-" + size + "s", ret );
		ret = ret.replace( " ", padChar );
		return ret;
	}

	
	@SneakyThrows
	default <T extends Number> List<T> toList( Class<T> ListClass, String value ) {
		List<T> ret = new ArrayList<>();
		String[] arr = value.split( "," );

		for ( int i = 0; i < arr.length; i++ ) {
			ret.add( conv( arr[i], ListClass ) );
		}
		return ret;
	}

	default String distinctString( String value ) {
		String ret = "";
		ret = Arrays.stream( value.split( "," ) ).distinct().collect( Collectors.joining( "," ) );
		return ret;
	}

	enum KERET {
		//https://www.fileformat.info/info/unicode/block/box_drawing/list.htm //"╔═╤╦╗"╔═╤╦╗","║ │║║","╟─┼╫╢","╠═╪╬╣","╚═╧╩╝"
		// @formatter:off
	SZIMPLA        ("│","─","┼","┬","┴","┌","┐","┘","└","├","┤"),
	DUPLA          ("║","═","╬","╦","╩","╔","╗","╝","╚","╠","╣"),
	KARAKTER       ("|","-","+"," "," "," "," "," "," "," "," "),
	DUPLA_SZ_SZ    ("║","═","┼","╤","╧","╔","╗","╝","╚","╟","╢"),
	DUPLA_SZ1_SZ   ("║","═","╫","╦","╩","╔","╗","╝","╚","╟","╢"),
	DUPLA_SZ2_SZ   ("║","═","╪","╤","╧","╔","╗","╝","╚","╠","╣"),
	; // @formatter:on

		public final String	fuggoleges;
		public final String	vizszintes;
		public final String	kereszt;
		public final String	teteje;
		public final String	alja;
		public final String	sarokBalTeteje;
		public final String	sarokBalAlja;
		public final String	sarokJobbTeteje;
		public final String	sarokJobbAlja;
		public final String	jobbVonal;
		public final String	balVonal;

		KERET( String fuggoleges, String vizszintes, String kereszt, String teteje, String alja,
			String sarokBalTeteje, String sarokJobbTeteje, String sarokJobbAlja, String sarokBalAlja,
			String balVonal, String jobbVonal ) {
			this.fuggoleges = fuggoleges;
			this.vizszintes = vizszintes;
			this.kereszt = kereszt;
			this.teteje = teteje;
			this.alja = alja;
			this.sarokBalTeteje = sarokBalTeteje;
			this.sarokBalAlja = sarokBalAlja;
			this.sarokJobbTeteje = sarokJobbTeteje;
			this.sarokJobbAlja = sarokJobbAlja;
			this.jobbVonal = jobbVonal;
			this.balVonal = balVonal;
		}
	}
	//	System.out.println( KERET.SZIMPLA.sarokBalTeteje + KERET.SZIMPLA.vizszintes    + KERET.SZIMPLA.teteje + KERET.SZIMPLA.sarokJobbTeteje );
	//	System.out.println( KERET.SZIMPLA.fuggoleges     + ""   + "X"                  + KERET.SZIMPLA.balVonal  + KERET.SZIMPLA.jobbVonal );
	//	System.out.println( KERET.SZIMPLA.balVonal       + KERET.SZIMPLA.vizszintes    + KERET.SZIMPLA.kereszt + KERET.SZIMPLA.jobbVonal );
	//	System.out.println( KERET.SZIMPLA.fuggoleges     + ""   + "X"                  + KERET.SZIMPLA.balVonal  + KERET.SZIMPLA.jobbVonal );
	//	System.out.println( KERET.SZIMPLA.sarokBalAlja   + KERET.SZIMPLA.vizszintes    + KERET.SZIMPLA.alja + KERET.SZIMPLA.sarokJobbAlja );
	//
	//	System.out.println( KERET.DUPLA.sarokBalTeteje   + KERET.DUPLA.vizszintes      + KERET.DUPLA.teteje + KERET.DUPLA.sarokJobbTeteje );
	//	System.out.println( KERET.DUPLA.fuggoleges       + ""   + "X"                  + KERET.DUPLA.balVonal  + KERET.DUPLA.jobbVonal );
	//	System.out.println( KERET.DUPLA.balVonal         + KERET.DUPLA.vizszintes      + KERET.DUPLA.kereszt + KERET.DUPLA.jobbVonal );
	//	System.out.println( KERET.DUPLA.fuggoleges       + ""   + "X"                  + KERET.DUPLA.balVonal  + KERET.DUPLA.jobbVonal );
	//	System.out.println( KERET.DUPLA.sarokBalAlja     + KERET.DUPLA.vizszintes      + KERET.DUPLA.alja + KERET.DUPLA.sarokJobbAlja );
	//
	//	System.out.println( KERET.DUPLA_SZ_SZ.sarokBalTeteje + KERET.DUPLA_SZ_SZ.vizszintes + KERET.DUPLA_SZ_SZ.teteje + KERET.DUPLA_SZ_SZ.sarokJobbTeteje );
	//	System.out.println( KERET.DUPLA_SZ_SZ.fuggoleges     + ""   + "X"                   + KERET.SZIMPLA.balVonal  + KERET.DUPLA_SZ_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ_SZ.balVonal       + KERET.SZIMPLA.vizszintes     + KERET.DUPLA_SZ_SZ.kereszt + KERET.DUPLA_SZ_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ_SZ.fuggoleges     + ""   + "X"                   + KERET.SZIMPLA.balVonal  + KERET.DUPLA_SZ_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ_SZ.sarokBalAlja   + KERET.DUPLA_SZ_SZ.vizszintes + KERET.DUPLA_SZ_SZ.alja + KERET.DUPLA_SZ_SZ.sarokJobbAlja );
	//
	//
	//	System.out.println( KERET.DUPLA_SZ1_SZ.sarokBalTeteje + KERET.DUPLA_SZ1_SZ.vizszintes + KERET.DUPLA_SZ1_SZ.teteje + KERET.DUPLA_SZ1_SZ.sarokJobbTeteje );
	//	System.out.println( KERET.DUPLA_SZ1_SZ.fuggoleges     + ""   + "X"                   + KERET.DUPLA_SZ1_SZ.balVonal  + KERET.DUPLA_SZ1_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ1_SZ.balVonal       + KERET.SZIMPLA.vizszintes     + KERET.DUPLA_SZ1_SZ.kereszt + KERET.DUPLA_SZ1_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ1_SZ.fuggoleges     + ""   + "X"                   + KERET.DUPLA_SZ1_SZ.balVonal  + KERET.DUPLA_SZ1_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ1_SZ.sarokBalAlja   + KERET.DUPLA_SZ_SZ.vizszintes + KERET.DUPLA_SZ1_SZ.alja + KERET.DUPLA_SZ1_SZ.sarokJobbAlja );
	//
	//	System.out.println( KERET.DUPLA_SZ2_SZ.sarokBalTeteje + KERET.DUPLA_SZ2_SZ.vizszintes + KERET.DUPLA_SZ2_SZ.teteje + KERET.DUPLA_SZ2_SZ.sarokJobbTeteje );
	//	System.out.println( KERET.DUPLA_SZ2_SZ.fuggoleges     + ""   + "X"                   + "╞"  + KERET.DUPLA_SZ2_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ2_SZ.balVonal       + KERET.DUPLA_SZ2_SZ.vizszintes     + KERET.DUPLA_SZ2_SZ.kereszt + KERET.DUPLA_SZ2_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ2_SZ.fuggoleges     + ""   + "X"                   + "╞"  + KERET.DUPLA_SZ2_SZ.jobbVonal );
	//	System.out.println( KERET.DUPLA_SZ2_SZ.sarokBalAlja   + KERET.DUPLA_SZ_SZ.vizszintes + KERET.DUPLA_SZ2_SZ.alja + KERET.DUPLA_SZ2_SZ.sarokJobbAlja );

}