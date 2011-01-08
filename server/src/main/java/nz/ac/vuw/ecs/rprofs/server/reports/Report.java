/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Version;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class Report {

	public static List<Report> findAllReports() {
		return Arrays.asList(
				new Report("Classes", "classes", "Classes"),
				new Report("Instances", "instance", "instances")
		);
	}

	public Report generateReport() {
		return this;
	}

	public Report updateReport() {
		return this;
	}

	@Id
	private String reference;

	private String title;

	private String description;

	@Version
	private int version;

	public Report() {}

	public Report(String title, String reference, String description) {
		this.title = title;
		this.reference = reference;
		this.description = description;
	}

	public String getId() {
		return reference;
	}

	public String getReference() {
		return reference;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public int getVersion() {
		return version;
	}
}
