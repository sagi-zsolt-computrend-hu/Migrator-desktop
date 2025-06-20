package ct.migratordesktop.tesztdata;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "testdata")
public class TesztDataProperties {
	private String	tableName	= "TESZT_";
	private int			recNum		= 100000;
	private int			commit		= 10000;
	private int			tableNum	= 30;
	private int			threads		= 4;
}