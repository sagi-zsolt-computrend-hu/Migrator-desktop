package ct.migratordesktop.toderby;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "derby")
public class DerbyProperties {
	private int			pageSize						= 100000;
	private String	derbyColumnsWhere	= "TABLE_NAME like 'MK_%'";
	private int			threads							= 4;
}