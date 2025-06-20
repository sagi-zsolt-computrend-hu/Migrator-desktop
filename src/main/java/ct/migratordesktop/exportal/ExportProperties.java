package ct.migratordesktop.exportal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "export")
public class ExportProperties {
	private int			pageSize						= 100000;
	private String	exportColumnsWhere	= "TABLE_NAME like 'MK_%'";
	private int			threads							= 4;
}