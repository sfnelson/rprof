/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface Template<Data, Parameter> {
	public String createTable(Parameter p);
	
	public String insert(Parameter p);
	public PreparedStatementSetter inserter(final Data r);
	public String update(Parameter p);
	public PreparedStatementSetter updater(final Data r);
	public String delete(Parameter p);
	public PreparedStatementSetter deleter(final Data r);
	
	public String countSelectAll(Parameter p);
	public String selectAll(Parameter p);
	public String select(Parameter p, int offset, int limit);
	public <T> RowMapper<Data> mapper(T param);

	public String drop(Parameter p);
}
