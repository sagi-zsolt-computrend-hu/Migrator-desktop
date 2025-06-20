package ct.migratordesktop.repositories.ecostat;

import java.util.List;

import ct.migratordesktop.models.EcostatColumn;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

@Mapper
public interface EcostatRepository {
	
	
//	@Select("select TABLE_NAME,COLUMN_NAME,COLUMN_ID,DATA_TYPE,DATA_LENGTH,DATA_PRECISION ,DATA_SCALE ,NULLABLE,DATA_DEFAULT DATA_DEFAULT"
//		+ " from USER_TAB_COLUMNS WHERE " +
//		" #{xxx, jdbcType=NUMERIC}"+//"TABLE_NAME like 'MK_%'" + //exportProperties.getExportColumnsWhere() +
//		" order by table_name, column_id")
//	@Results({
//		@Result(property = "id", column = "ID"),
//		@Result(property = "tableName", column = "TABLE_NAME"),
//		@Result(property = "columnName", column = "COLUMN_NAME"),
//		@Result(property = "columnId", column = "COLUMN_ID"),
//		@Result(property = "dataType", column = "DATA_TYPE"),
//		@Result(property = "dataLength", column = "DATA_LENGTH"),
//		@Result(property = "dataPrecision", column = "DATA_PRECISION"),
//		@Result(property = "dataScale", column = "DATA_SCALE"),
//		@Result(property = "nullable", column = "NULLABLE"),
//		@Result(property = "dataDefault", column = "DATA_DEFAULT")
//	})
//	List<EcostatColumn> getAllColumnsFromEcostat(@Param("xxx") String xxx);
//	
//	
//	 public interface UserMapper {
//	   @Select({ "" })
//	   User select(@Nonnull String name, @Nullable Integer age);
//	 }

}