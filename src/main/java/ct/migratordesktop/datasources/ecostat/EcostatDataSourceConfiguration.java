package ct.migratordesktop.datasources.ecostat;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(
	basePackages = {"**.repositories.ecostat"},
	sqlSessionFactoryRef = "ecostatSqlSessionFactory")
public class EcostatDataSourceConfiguration {
	public static final String PREFIX = "spring.datasource.ecostat";

	@Bean("ecostatDataSource")
	@ConfigurationProperties(prefix = PREFIX)
	public /*Hikari*/DataSource dataSource() {
		return DataSourceBuilder.create().build();
		//return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	@Bean("ecostatSqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory(
		@Autowired @Qualifier("ecostatDataSource") DataSource dataSource ) throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource( dataSource );
		return sessionFactory.getObject();
	}
}
