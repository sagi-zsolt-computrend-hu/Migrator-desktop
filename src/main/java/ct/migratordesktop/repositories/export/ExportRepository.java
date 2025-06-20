package ct.migratordesktop.repositories.export;

import java.util.List;

import ct.migratordesktop.models.EcostatColumn;
import ct.migratordesktop.models.PrimaryKey;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExportRepository {
	@Select(value = "SELECT * FROM APP.ECOSTAT_COLUMS")
	@Results({
		@Result(property = "id", column = "ID"),
		@Result(property = "tableName", column = "TABLE_NAME"),
		@Result(property = "columnName", column = "COLUMN_NAME"),
		@Result(property = "columnId", column = "COLUMN_ID"),
		@Result(property = "dataType", column = "DATA_TYPE"),
		@Result(property = "dataLength", column = "DATA_LENGTH"),
		@Result(property = "dataPrecision", column = "DATA_PRECISION"),
		@Result(property = "dataScale", column = "DATA_SCALE"),
		@Result(property = "nullable", column = "NULLABLE"),
		@Result(property = "dataDefault", column = "DATA_DEFAULT")
	})
	List<EcostatColumn> getAllFromEcostatColumns();

	@Insert("INSERT INTO APP.ECOSTAT_COLUMS"
		+ "      (ID,                      TABLE_NAME,                     COLUMN_NAME,                     COLUMN_ID,                     DATA_TYPE,                     DATA_LENGTH,                     DATA_PRECISION,                     DATA_SCALE,                     NULLABLE,    DATA_DEFAULT)"
		+ "VALUES(#{id, jdbcType=NUMERIC}, #{tableName, jdbcType=VARCHAR}, #{columnName, jdbcType=VARCHAR}, #{columnId, jdbcType=NUMERIC}, #{dataType, jdbcType=NUMERIC}, #{dataLength, jdbcType=NUMERIC}, #{dataPrecision, jdbcType=NUMERIC}, #{dataScale, jdbcType=NUMERIC}, #{nullable}, #{dataDefault, jdbcType=VARCHAR})"
	)
	void insertToEcostatColumns( EcostatColumn ecostatColumns );
	
	@Select("SELECT DISTINCT TABLE_NAME FROM APP.ECOSTAT_COLUMS where table_name not in('ECOSTAT_PRIMARY_KEYS','ECOSTAT_COLUMS')")
	List<String> getTableNamesFromEcostatColumns();

	@Insert("INSERT INTO APP.ECOSTAT_PRIMARY_KEYS"
		+ "      (ID,                      TABLE_NAME,                     PRIMARY_KEY)"
		+ "VALUES(#{id, jdbcType=NUMERIC}, #{tableName, jdbcType=VARCHAR}, #{primaryKey, jdbcType=VARCHAR})"
	)
	void insertToPrimaryKey( PrimaryKey primaryKey );
	
//	@Select("SELECT COLUMN_NAME FROM APP.ECOSTAT_COLUMS where table_name =#{tableName}")
	@Select(value = "SELECT * FROM APP.ECOSTAT_COLUMS where table_name =#{tableName} order by COLUMN_ID")
	@Results({
		@Result(property = "id", column = "ID"),
		@Result(property = "tableName", column = "TABLE_NAME"),
		@Result(property = "columnName", column = "COLUMN_NAME"),
		@Result(property = "columnId", column = "COLUMN_ID"),
		@Result(property = "dataType", column = "DATA_TYPE"),
		@Result(property = "dataLength", column = "DATA_LENGTH"),
		@Result(property = "dataPrecision", column = "DATA_PRECISION"),
		@Result(property = "dataScale", column = "DATA_SCALE"),
		@Result(property = "nullable", column = "NULLABLE"),
		@Result(property = "dataDefault", column = "DATA_DEFAULT")
	})
	List<EcostatColumn> findAllByTableNameOrderByColumnIdAsc(@Param("tableName") String tableName );

	@Select(value = "SELECT COLUMN_NAME FROM APP.ECOSTAT_COLUMS where table_name =#{tableName} order by COLUMN_ID")
	List<String> getColumnNameListFromEcostatColumns( String tableName );

	@Select(value = "SELECT DATA_TYPE FROM APP.ECOSTAT_COLUMS where table_name =#{tableName} order by COLUMN_ID")
	List<String> getColumnTypeListFromEcostatColumns( String tableName );
}
