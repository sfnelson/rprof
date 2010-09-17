/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface Template<Data, Parameter> {
	public String createTable(Parameter p);
	
	public String insert(Parameter p);
	public BatchPreparedStatementSetter inserter(final List<Data> classes);
	public String update(Parameter p);
	public BatchPreparedStatementSetter updater(final List<Data> records);
	public String delete(Parameter p);
	public BatchPreparedStatementSetter deleter(final List<Data> records);
	
	public String countSelect(Parameter p, Object... filter);
	public String select(Parameter p, Object... filter);
	public String select(Parameter p, int offset, int limit, Object... filter);
	public <T> RowMapper<Data> mapper(T param);

	public String drop(Parameter p);
}
