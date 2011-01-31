package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "field_writes")
public class FieldWriteRecord {

	public static final int CONSTRUCTOR_PHASE = 0;
	public static final int POST_CONSTRUCTOR_PHASE = 1;

	@GeneratedValue(strategy=GenerationType.AUTO)
	@Id
	long id;

	@Version
	int version;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "instance_index", referencedColumnName = "index")
	})
	Instance instance;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "field_index", referencedColumnName = "index"),
		@JoinColumn(name = "field_owner_index", referencedColumnName = "owner_index")
	})
	Field field;

	int phase;

	public FieldWriteRecord() {}

	public FieldWriteRecord(Instance instance, Field field, int phase) {
		this.instance = instance;
		this.field = field;
		this.phase = phase;
	}

	public long getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public Instance getInstance() {
		return instance;
	}

	public Field getField() {
		return field;
	}

	public int getPhase() {
		return phase;
	}
}
