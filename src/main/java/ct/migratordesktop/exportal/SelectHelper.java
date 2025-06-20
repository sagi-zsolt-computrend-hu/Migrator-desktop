package ct.migratordesktop.exportal;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

class SelectHelper {

	@Setter
	private String	tableName;
	@Setter
	private String	columns;
	@Setter
	private int			rowCount;
	@Setter
	private int			pageSize	= 10000;
	@Setter
	private String	orderBy		= " ROWID ";

	private String	selectSql	= "SELECT * FROM ( SELECT a.*, rownum r__ FROM ( SELECT ×columns× FROM ×tableName× ORDER BY ×orderBy× ) a " +
		"  WHERE rownum < ×p_1× " +
		") WHERE r__ >= ×p_2× ";
	private int			pageCount;

	public List<String> getSelectList() {
		final var ret = new ArrayList<String>();
		if ( rowCount < pageSize )
			ret.add( "SELECT" + columns + " FROM " + tableName + " ORDER BY ×orderBy×".replace( "×orderBy×", orderBy ) );
		else {
			recalc();
			for ( int i = 1; i < pageCount + 1; i++ ) {
				ret.add( getSelectSql( i ) );
			}
		}
		return ret;
	}

	public String getSelectSql( int pageNumber ) {

		final String sql = selectSql
			.replace( "×tableName×", tableName )
			.replace( "×columns×", columns )
			.replace( "×orderBy×", orderBy )
			.replace( "×p_1×", getRowNumTo( pageNumber ) )
			.replace( "×p_2×", getRowNumFrom( pageNumber ) );
		return sql;
	}

	private boolean isNoPage() {
		return this.pageSize <= 0 || this.pageSize > rowCount;
	}

	public String getRowNumFrom( int pageNumber ) {
		return Integer.toString( (((pageNumber - 1) * pageSize) + 1) );
	}

	public String getRowNumTo( int pageNumber ) {
		return Integer.toString( ((pageNumber * pageSize) + 1) );
	}

	private void recalc() {
		if ( rowCount <= 0 ) {
			pageCount = 0;
		} else if ( isNoPage() ) {
			pageCount = 1;
		} else {
			pageCount = (rowCount / pageSize);
			if ( pageCount * pageSize < rowCount )
				++pageCount;
		}
	}
}