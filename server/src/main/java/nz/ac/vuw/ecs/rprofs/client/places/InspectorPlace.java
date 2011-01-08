package nz.ac.vuw.ecs.rprofs.client.places;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.ReportProxy;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;


public class InspectorPlace extends com.google.gwt.place.shared.Place {

	private static final String DATASET = "ds";
	private static final String REPORT = "r";

	private final String datasetHandle;
	private final String reportHandle;

	private String token = null;

	public InspectorPlace(DatasetProxy dataset, ReportProxy report) {
		if (dataset == null) {
			datasetHandle = null;
		}
		else {
			this.datasetHandle = dataset.getHandle();
		}

		if (report == null) {
			reportHandle = null;
		}
		else {
			this.reportHandle = report.getReference();
		}
	}

	private InspectorPlace(String datasetHandle, String reportHandle) {
		this.datasetHandle = datasetHandle;
		this.reportHandle = reportHandle;
	}

	public String getDatasetHandle() {
		return datasetHandle;
	}

	public String getReportHandle() {
		return reportHandle;
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

	public Place setDataset(DatasetProxy dataset) {
		return new InspectorPlace(dataset.getHandle(), reportHandle);
	}

	public Place setReport(ReportProxy report) {
		return new InspectorPlace(datasetHandle, report.getReference());
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
			List<String> parts = ProfilerPlaceHistoryMapper.tokenize(token, '&');

			Map<String, String> pairs = Collections.newMap();
			for (String part: parts) {
				List<String> pair = ProfilerPlaceHistoryMapper.tokenize(part, '=');
				if (pair.size() > 1) {
					pairs.put(pair.get(0), pair.get(1));
				}
			}

			String dataset = null, report = null;
			if (pairs.containsKey(DATASET)) {
				dataset = pairs.get(DATASET);
			}
			if (pairs.containsKey(REPORT)) {
				report = pairs.get(REPORT);
			}

			return new InspectorPlace(dataset, report);
		}
	}
}
