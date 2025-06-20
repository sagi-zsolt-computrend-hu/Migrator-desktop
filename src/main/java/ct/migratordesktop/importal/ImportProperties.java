package ct.migratordesktop.importal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "import")
public class ImportProperties {
	private int			pageSize						= 100000;
	private String	exportColumnsWhere	= "TABLE_NAME like 'MK_%'";
	private int			threads		= 4;
}