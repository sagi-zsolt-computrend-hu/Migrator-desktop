package ct.migratordesktop.models;

import lombok.Data;

@Data

public class PrimaryKey {
	public static final String	TABLE_NAME	= "ECOSTAT_PRIMARY_KEYS";

	private Long								id;
	private String							tableName;
	private String							primaryKey;

	public static final String	table				= ""
		+ "CREATE TABLE " + TABLE_NAME + " ( "
		+ " ID bigint NOT NULL PRIMARY KEY, "
		+ " TABLE_NAME varchar(128), "
		+ " PRIMARY_KEY varchar(128) "
		+ ")";
}