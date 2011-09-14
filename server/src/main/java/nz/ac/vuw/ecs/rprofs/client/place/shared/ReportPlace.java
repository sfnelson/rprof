package nz.ac.vuw.ecs.rprofs.client.place.shared;

import com.google.common.collect.Maps;
import com.google.gwt.place.shared.Place;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseClasses;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseEvents;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseFields;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseInstances;

import java.util.Map;

public abstract class ReportPlace<P extends ReportPlace<P>> extends CompositePlace<P> implements HasDataset {

	private static final Map<String, CompositeTokenizer<? extends ReportPlace<?>>> types = Maps.newHashMap();

	static {
		register(BrowseClasses.TYPE, BrowseClasses.TOKENIZER);
		register(BrowseEvents.TYPE, BrowseEvents.TOKENIZER);
		register(BrowseFields.TYPE, BrowseFields.TOKENIZER);
		register(BrowseInstances.TYPE, BrowseInstances.TOKENIZER);
	}

	protected static void register(String report, CompositeTokenizer<? extends ReportPlace<?>> type) {
		types.put(report, type);
	}

	public static ReportPlace<?> create(String report, Place current) {
		Map<String, String> params;
		if (current instanceof CompositePlace) {
			params = ((CompositePlace<?>) current).parameters;
		} else {
			params = Maps.newHashMap();
		}
		CompositeTokenizer<? extends ReportPlace<?>> tok = types.get(report);
		if (tok == null) {
			System.err.println("report type unknown: " + report);
			System.err.println(types);
			return null;
		}
		ReportPlace<?> p = tok.create();
		p.parameters.putAll(params);
		return p;
	}

	public ReportPlace() {
	}

	@Override
	public String getDatasetHandle() {
		return getParameter("ds");
	}
}
