package ct.migratordesktop.importal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "import")
public class ImportProperties {
	private int			pageSize						= 100000;
	private String	importColumnsWhere	= "TABLE_NAME like 'MK_%' or TABLE_NAME in ('F_NAPLO')";
	private int			threads		= 4;
}