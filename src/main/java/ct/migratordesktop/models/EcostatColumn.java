package ct.migratordesktop.models;

import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EcostatColumn {
	private Long		id;
	private String	tableName;
	private String	columnName;
	private Integer	columnId;
	private String	dataType;
	private Integer	dataLength;
	private Integer	dataPrecision;
	private Integer	dataScale;
	private String	nullable;
	private String	dataDefault;

	public Integer getDataPrecision() {
		return Objects.nonNull( dataPrecision ) ? dataPrecision : 0;
	}

	public Integer getDataScale() {
		return Objects.nonNull( dataScale ) ? dataScale : 0;
	}

	public static final String	TABLE_NAME	= "ECOSTAT_COLUMS";
	public static final String	table				= ""
		+ "CREATE TABLE " + TABLE_NAME + " ( "
		+ " ID bigint NOT NULL PRIMARY KEY, "
		+ " TABLE_NAME varchar(128), "
		+ " COLUMN_NAME varchar(128), "
		+ " COLUMN_ID decimal(5), "
		+ " DATA_TYPE varchar(128), "
		+ " DATA_LENGTH decimal(5), "
		+ " DATA_PRECISION decimal(5), "
		+ " DATA_SCALE decimal(5), "
		+ " NULLABLE varchar(1), "
		+ " DATA_DEFAULT varchar(128) "
		+ ")";
}
