package nz.ac.vuw.ecs.rprofs.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import nz.ac.vuw.ecs.rprofs.client.Factory;
import nz.ac.vuw.ecs.rprofs.client.activity.shared.AbstractListActivity;
import nz.ac.vuw.ecs.rprofs.client.place.ShowDataset;
import nz.ac.vuw.ecs.rprofs.client.place.shared.HasDataset;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.requestfactory.shared.Receiver;

import javax.annotation.Nullable;
import java.util.List;

public class SelectDatasetActivity extends AbstractActivity implements DatasetListView.Presenter {

	private final Factory factory;

	private DatasetListView view;
    private @Nullable HasDataset place;

	public SelectDatasetActivity(Factory factory) {
		this.factory = factory;
	}

    public void setSelected(@Nullable HasDataset place) {
        this.place = place;
    }

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = factory.getDatasetView();

        view.setPresenter(this);
		panel.setWidget(view);

		refresh();
	}

	@Override
	public void selectDataset(DatasetProxy dataset) {
		ShowDataset newPlace = new ShowDataset(dataset.getHandle());
		factory.getPlaceController().goTo(newPlace);
	}

	@Override
	public void stopDataset(DatasetProxy dataset) {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.stopDataset(dataset.getHandle()).fire(new Receiver<Void>() {
            @Override
            public void onSuccess(Void response) {
                refresh();
            }
        });
	}

	@Override
	public void deleteDataset(DatasetProxy dataset) {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.deleteDataset(dataset.getHandle()).fire(new Receiver<Void>() {
			@Override
			public void onSuccess(Void response) {
				refresh();
			}
		});
	}

	private void refresh() {
		factory.getRequestFactory().datasetRequest().findAllDatasets()
                .fire(new Receiver<List<DatasetProxy>>() {
                    @Override
                    public void onSuccess(List<DatasetProxy> response) {
                        refresh(response);
                    }
                });
	}

    private void refresh(List<DatasetProxy> datasets) {
        view.setNumDatasets(datasets.size());
        view.setDatasets(datasets);

		HasDataset place = this.place;
        if (place != null) {
            for (DatasetProxy d: datasets) {
                if (place.getDatasetHandle().equals(d.getHandle())) {
                    view.setSelected(d);
                }
            }
        }
    }
}
