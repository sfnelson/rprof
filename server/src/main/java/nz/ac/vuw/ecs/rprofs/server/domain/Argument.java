package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table( name = "event_args" )
public class Argument implements Comparable<Argument> {

	@Id
	@GeneratedValue
	Long id;

	@Version
	Integer version;

	int position;

	@ManyToOne
	Instance parameter;

	public Argument() {}

	public Argument(int position, Instance arg) {
		this.position = position;
		this.parameter = arg;
	}

	public Long getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
	}

	public int getPosition() {
		return position;
	}

	public Instance getParameter() {
		return parameter;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (!(obj instanceof Argument)) {
			return false;
		}
		Argument a = (Argument) obj;
		return position == a.position;
	}

	@Override
	public int hashCode() {
		return position;
	}

	@Override
	public int compareTo(Argument o) {
		return o.position - position;
	}
}
