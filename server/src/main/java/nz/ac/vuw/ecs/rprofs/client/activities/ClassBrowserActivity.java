package nz.ac.vuw.ecs.rprofs.client.activities;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.events.DatasetEvent;
import nz.ac.vuw.ecs.rprofs.client.requests.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.requests.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ClassBrowserActivity extends AbstractActivity
implements ReportView.Presenter, DatasetEvent.DatasetHandler {

	private ProfilerFactory factory;
	private ReportView view;
	private DatasetProxy dataset;

	public ClassBrowserActivity(ProfilerFactory factory) {
		this.factory = factory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = factory.getClassBrowser();
		panel.setWidget(view);

		view.setPresenter(this);

		eventBus.addHandler(DatasetEvent.getType(), this);
	}

	@Override
	public void onStop() {
		view.clear();
	}

	@Override
	public void selectPackage(String pkg) {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.findClasses(pkg).using(dataset).fire(new Receiver<List<ClassProxy>>() {
			@Override
			public void onSuccess(List<ClassProxy> response) {
				view.showClasses(response);
			}
		});
	}

	@Override
	public void selectClass(ClassProxy cls) {
		view.showFields(cls.getFields());
	}

	@Override
	public void selectMethod(MethodProxy method) {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectField(FieldProxy field) {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectInstance(InstanceProxy instance) {
		// TODO Auto-generated method stub

	}

	@Override
	public void datasetSelected(DatasetProxy dataset) {
		if (this.dataset != null) return;

		this.dataset = dataset;

		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.findPackages().using(dataset).fire(new Receiver<List<String>>() {
			@Override
			public void onSuccess(List<String> response) {
				view.showPackages(response);
			}
		});
	}

}
