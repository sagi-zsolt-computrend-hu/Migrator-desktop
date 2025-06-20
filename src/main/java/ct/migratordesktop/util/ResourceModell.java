package ct.migratordesktop.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

@Slf4j
public class ResourceModell {
	private static final String				SPLITCHAR				= "│";
	private static final List<String>	TRUEBOOLEANLIST	= List.of( "true", "igen", "1", "*" );
	private final List<String>				rowList					= new ArrayList<>();
	private final List<String>				headerList			= new ArrayList<>();

	public ResourceModell(  ) {
		super();
	}

	@SneakyThrows
	public ResourceModell readResource(String resourceName) {;
		final var resource = new ClassPathResource( "initdb/" + resourceName + ".txt" );
		final var resourceString = new String( resource.getInputStream().readAllBytes(),StandardCharsets.UTF_8 );
		return readString( resourceString );
	}

	@SneakyThrows
	public ResourceModell readString(String resourceString) {
		this.rowList.clear();
		this.headerList.clear();
		
		var lines =  resourceString.lines().toList();//.collect( Collectors.toList() );
		this.rowList.addAll( lines.stream().skip( 2 )
			.filter( l -> false == l.startsWith( "//" ) ).filter( l -> false == l.startsWith( "--" ) )
			.collect( Collectors.toList() ) );
		final var split = lines.get( 0 ).split( SPLITCHAR );
		for ( var i = 0; i < split.length; i++ ) {
			headerList.add( split[i].trim().toLowerCase() );
		}
		return this;
	}
	
	
	public int getRowCount() {
		return rowList.size();
	}

	public String getRow( int rowNum ) {
		return rowList.get( rowNum );
	}

	public int getColumnIndex( String header ) {
		return headerList.indexOf( header.toLowerCase() );
	}

	public <T> T getCell( int row, String columnName, final Class<T> clazz, ExtraParams... extraParams ) {
		T ret = null;
		final var columnIndex = getColumnIndex( columnName );
		if ( columnIndex < 0 )
			throw new IllegalArgumentException( "Column not found: " + columnName );
		final var values = rowList.get( row ).split( SPLITCHAR );
		var value = "";
		if ( columnIndex < values.length )
			value = values[columnIndex].trim();
		ret = conv( value, clazz, extraParams );
		return ret;
	}

	@SuppressWarnings({ "unchecked" })
	private <T> T conv( String value, final Class<T> clazz, ExtraParams... extraParams ) {
		Object ret = null;

		if ( clazz == Double.class ) {
			if ( Objects.nonNull( value ) && false == value.isBlank() ) {
				ret = Double.parseDouble( value );
				if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
					if ( (Double)ret == Double.valueOf( 0d ) )
						ret = null;
			} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
				ret = Double.valueOf( 0d );
		} else if ( clazz == Boolean.class ) {
			ret = TRUEBOOLEANLIST.contains( value.trim().toLowerCase() );
		} else if ( clazz == Integer.class ) {
			if ( Objects.nonNull( value ) && false == value.isBlank() ) {
				ret = Integer.valueOf( value.replaceAll( "\\..*", "" ) );
				if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
					if ( ((Integer)ret).intValue() == 0 )
						ret = null;
			} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
				ret = Integer.valueOf( 0 );
		} else if ( clazz == Long.class ) {
			if ( Objects.nonNull( value ) && false == value.isBlank() ) {
				ret = Long.valueOf( value );
				if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
					if ( ((Long)ret).intValue() == 0 )
						ret = null;
			} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
				ret = Long.valueOf( 0L );
		} else if ( clazz == Float.class ) {
			if ( Objects.nonNull( value ) && false == value.isBlank() ) {
				ret = Float.valueOf( value );
				if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
					if ( ((Float)ret).intValue() == 0 )
						ret = null;
			} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
				ret = Float.valueOf( 0f );
		} else if ( clazz == BigInteger.class ) {
			if ( Objects.nonNull( value ) && false == value.isBlank() ) {
				ret = new BigInteger( value );
				if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
					if ( ((BigInteger)ret).intValue() == 0 )
						ret = null;
			} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
				ret = BigInteger.ZERO;
		} else if ( clazz == BigDecimal.class ) {
			if ( Objects.nonNull( value ) && false == value.isBlank() ) {
				ret = new BigDecimal( value );
				if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
					if ( ((BigDecimal)ret).intValue() == 0 )
						ret = null;
			} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
				ret = BigDecimal.ZERO;
		} else
			ret = value;
		return (T)ret;

	}

	protected void clearResource() {
		this.rowList.clear();
		this.headerList.clear();

	}

	public enum ExtraParams {
		ZeroAsNull, NullAndBlankAsZero
	}
}
