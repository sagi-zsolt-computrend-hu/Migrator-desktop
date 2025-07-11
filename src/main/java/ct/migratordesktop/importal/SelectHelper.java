package ct.migratordesktop.importal;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class SelectHelper {
	@Setter
	protected Integer	pageSize	= 100000;
	protected Integer	pageCount;
	@Setter
	protected String	columns;
	@Setter
	protected String	tableName;
	protected String	insertCommand;
	protected String	mode			= "normal";
	@Setter
	private int				rowCount;

	public SelectHelper() {

	}

	public List<String> getSelectList() {
		final var ret = new ArrayList<String>();
		if ( rowCount < pageSize )
			ret.add( "SELECT" + columns + " FROM " + tableName + " ORDER BY ID_DERBY" );
		else {
			recalc();
			for ( int i = 0; i < pageCount; i++ ) {
				ret.add( getSelectSql( i ) );
			}
		}
		return ret;
	}

	public String getSelectSql( int pageNumber ) {
		String selectSql = "SELECT ×columns× FROM ×tableName× "
			.replace( "×tableName×", this.tableName )
			.replace( "×columns×", columns );
		if ( false == isNoPage() ) {
			selectSql += " WHERE ID_DERBY BETWEEN ×from× AND ×to× "
				.replace( "×from×", getFrom( pageNumber ) )
				.replace( "×to×", getTo( pageNumber ) );
		}
		selectSql += " ORDER BY ID_DERBY ";
		return selectSql;
	}

	public String getFrom( int pageNumber ) {
		return Integer.toString( 1 + pageNumber * pageSize );
	}

	public String getTo( int pageNumber ) {
		return Integer.toString( 0 + (pageNumber + 1) * pageSize );
	}

	private void recalc() {
		if ( isNoPage() ) {
			pageCount = 1;
		} else {
			pageCount = (rowCount / pageSize);
			if ( pageCount * pageSize < rowCount )
				++pageCount;
		}
	}

	private boolean isNoPage() {
		return this.pageSize <= 0 || this.pageSize > rowCount;
	}
}