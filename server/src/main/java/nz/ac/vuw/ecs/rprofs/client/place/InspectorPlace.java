package nz.ac.vuw.ecs.rprofs.client.place;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.place.shared.InspectorPlaceHistoryMapper;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;


public class InspectorPlace extends com.google.gwt.place.shared.Place {

	public static final InspectorPlace NOWHERE = new InspectorPlace();

	private static final String DATASET = "ds";
	private static final String REPORT = "r";
	private static final String INSTANCE = "i";

	private final String datasetHandle;
	private final String reportHandle;
	private final long instanceIndex;

	private String token = null;

	public InspectorPlace(Place... args) {
		InspectorPlace p = new InspectorPlace();
		for (Place a: args) {
			p = p.setPlace(a);
		}

		datasetHandle = p.datasetHandle;
		reportHandle = p.reportHandle;
		instanceIndex = p.instanceIndex;
	}

	private InspectorPlace() {
		datasetHandle = null;
		reportHandle = null;
		instanceIndex = 0;
	}

	private InspectorPlace(String datasetHandle, String reportHandle, long instance) {
		this.datasetHandle = datasetHandle;
		this.reportHandle = reportHandle;
		this.instanceIndex = instance;
	}

	public String getDatasetHandle() {
		return datasetHandle;
	}

	public String getReportHandle() {
		return reportHandle;
	}

	public long getInstance() {
		return instanceIndex;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof InspectorPlace)) return false;

		InspectorPlace other = (InspectorPlace) o;

		return this.getToken().equals(other.getToken());
	}

	@Override
	public int hashCode() {
		return getToken().hashCode();
	}

	@Override
	public String toString() {
		return getToken();
	}

	public InspectorPlace setPlace(Place place) {
		if (place instanceof InspectorPlace) {
			return (InspectorPlace) place;
		}
		if (place instanceof DatasetPlace) {
			return setDataset((DatasetPlace) place);
		}
		if (place instanceof ReportPlace<?>) {
			return setReport((ReportPlace<?>) place);
		}
		if (place instanceof InstancePlace) {
			return setInstance((InstancePlace) place);
		}

		return this;
	}

	public InspectorPlace setDataset(DatasetPlace dataset) {
		if (dataset.getDataset() == null) {
			return new InspectorPlace(null, reportHandle, instanceIndex);
		}
		else {
			return new InspectorPlace(dataset.getDataset().getHandle(), reportHandle, instanceIndex);
		}
	}

	public InspectorPlace setReport(ReportPlace<?> report) {
		return new InspectorPlace(datasetHandle, report.getHandle(), instanceIndex);
	}

	public InspectorPlace setInstance(InstancePlace instance) {
		if (instance.getInstance() == null) {
			return new InspectorPlace(datasetHandle, reportHandle, 0);
		}
		else {
			return new InspectorPlace(datasetHandle, reportHandle, instance.getInstance().getIndex());
		}
	}

	private String getToken() {
		if (token != null) return token;

		StringBuilder s = new StringBuilder("");
		if (datasetHandle != null) {
			s.append("&");
			s.append(DATASET);
			s.append("=");
			s.append(datasetHandle);
		}
		if (reportHandle != null) {
			s.append("&");
			s.append(REPORT);
			s.append("=");
			s.append(reportHandle);
		}
		if (instanceIndex != 0) {
			s.append("&");
			s.append(INSTANCE);
			s.append("=");
			s.append(instanceIndex);
		}

		if (s.length() > 0) {
			token = s.substring(1);

		}
		else {
			token = "";
		}

		return token;
	}

	public static class InspectorPlaceTokenizer implements PlaceTokenizer<InspectorPlace> {
		@Override
		public String getToken(InspectorPlace place) {
			return place.getToken();
		}

		@Override
		public InspectorPlace getPlace(String token) {
			List<String> parts = InspectorPlaceHistoryMapper.tokenize(token, '&');

			Map<String, String> pairs = Collections.newMap();
			for (String part: parts) {
				List<String> pair = InspectorPlaceHistoryMapper.tokenize(part, '=');
				if (pair.size() > 1) {
					pairs.put(pair.get(0), pair.get(1));
				}
			}

			String dataset = null, report = null;
			long instance = 0;
			if (pairs.containsKey(DATASET)) {
				dataset = pairs.get(DATASET);
			}
			if (pairs.containsKey(REPORT)) {
				report = pairs.get(REPORT);
			}
			if (pairs.containsKey(INSTANCE)) {
				instance = Long.parseLong(pairs.get(INSTANCE));
			}

			return new InspectorPlace(dataset, report, instance);
		}
	}
}
