/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.db;


import org.hibernate.type.StandardBasicTypes;

public class PostgreSQLDialect extends org.hibernate.dialect.PostgreSQLDialect {
	public PostgreSQLDialect() {
		super();

		registerFunction("band", new PostgreSQLBitwiseAndFunction("band", StandardBasicTypes.INTEGER));
	}
}