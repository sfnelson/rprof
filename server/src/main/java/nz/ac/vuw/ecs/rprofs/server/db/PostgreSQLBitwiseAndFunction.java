/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.List;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class PostgreSQLBitwiseAndFunction extends StandardSQLFunction implements SQLFunction {

	public PostgreSQLBitwiseAndFunction(String name) {
		super(name);
	}

	public PostgreSQLBitwiseAndFunction(String name, Type type) {
		super(name, type);
	}

	@Override
	public String render(Type firstArgumentType, @SuppressWarnings("rawtypes") List args,
			SessionFactoryImplementor sessionFactory) {
		if (args.size() != 2) {
			throw new IllegalArgumentException("bitwise AND is binary");
		}
		return String.format("(%s & %s)", args.get(0).toString(), args.get(1).toString());
	}
}