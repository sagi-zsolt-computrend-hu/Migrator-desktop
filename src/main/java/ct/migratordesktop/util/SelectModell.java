package ct.migratordesktop.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

public class SelectModell extends ArrayDataModell{
	private static final List<String>	TRUEBOOLEANLIST	= List.of( "true", "igen", "1", "*" );
	private final JdbcTemplate				jdbcTemplate;
	private final String							sql;
	private List<Map<String, Object>>	rowList					= List.of();

	public SelectModell( JdbcTemplate jdbcTemplate, String sql ) {
		super();
		this.jdbcTemplate = jdbcTemplate;
		this.sql = sql;

	}

	public SelectModell select( Object... params ) {
		rowList = jdbcTemplate.queryForList( sql, params );

		if (!rowList.isEmpty()) {
			rowList.get( 0 ).keySet().forEach( e->addHeader( e ) );
			rowList.forEach( r->{
				r.values().forEach(e->{
					if (e instanceof String s)
						add( s.replaceAll("[\\t\\n\\r]", " ") );
					else
						add( e );
				} );
			} );
		}
		
		
		return this;
	}

	public int getRowCount() {
		return rowList.size();
	}

	public Map<String, Object> getRow( int rowNum ) {
		return rowList.get( rowNum );
	}

	public <T> T getCell( int rowNum, String columnName, final Class<T> clazz, ExtraParams... extraParams ) {
		T ret = null;
		final var row = rowList.get( rowNum );
		Assert.isTrue( row.containsKey( columnName ), "hibás oszlopnév" );
		ret = conv( row.get( columnName ), clazz, extraParams );
		return ret;
	}

	@SuppressWarnings({ "unchecked" })
	private <T> T conv( Object value, final Class<T> clazz, ExtraParams... extraParams ) {
		Object ret = null;

		if ( Objects.nonNull( value ) && value.getClass().equals( clazz ) ) {
			ret = value;
		} else {
			final var StringValue = value.toString();
			if ( clazz == Double.class ) {
				if ( Objects.nonNull( StringValue ) && false == StringValue.isBlank() ) {
					ret = Double.parseDouble( StringValue );
					if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
						if ( (Double)ret == Double.valueOf( 0d ) )
							ret = null;
				} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
					ret = Double.valueOf( 0d );
			} else if ( clazz == Boolean.class ) {
				ret = TRUEBOOLEANLIST.contains( StringValue.trim().toLowerCase() );
			} else if ( clazz == Integer.class ) {
				if ( Objects.nonNull( StringValue ) && false == StringValue.isBlank() ) {
					ret = Integer.valueOf( StringValue.replaceAll( "\\..*", "" ) );
					if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
						if ( ((Integer)ret).intValue() == 0 )
							ret = null;
				} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
					ret = Integer.valueOf( 0 );
			} else if ( clazz == Long.class ) {
				if ( Objects.nonNull( StringValue ) && false == StringValue.isBlank() ) {
					ret = Long.valueOf( StringValue );
					if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
						if ( ((Long)ret).intValue() == 0 )
							ret = null;
				} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
					ret = Long.valueOf( 0L );
			} else if ( clazz == Float.class ) {
				if ( Objects.nonNull( StringValue ) && false == StringValue.isBlank() ) {
					ret = Float.valueOf( StringValue );
					if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
						if ( ((Float)ret).intValue() == 0 )
							ret = null;
				} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
					ret = Float.valueOf( 0f );
			} else if ( clazz == BigInteger.class ) {
				if ( Objects.nonNull( StringValue ) && false == StringValue.isBlank() ) {
					ret = new BigInteger( StringValue );
					if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
						if ( ((BigInteger)ret).intValue() == 0 )
							ret = null;
				} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
					ret = BigInteger.ZERO;
			} else if ( clazz == BigDecimal.class ) {
				if ( Objects.nonNull( StringValue ) && false == StringValue.isBlank() ) {
					ret = new BigDecimal( StringValue );
					if ( Arrays.asList( extraParams ).contains( ExtraParams.ZeroAsNull ) )
						if ( ((BigDecimal)ret).intValue() == 0 )
							ret = null;
				} else if ( Arrays.asList( extraParams ).contains( ExtraParams.NullAndBlankAsZero ) )
					ret = BigDecimal.ZERO;
			} else
				ret = value;
		}

		return (T)ret;

	}

	public enum ExtraParams {
		ZeroAsNull, NullAndBlankAsZero
	}
}
