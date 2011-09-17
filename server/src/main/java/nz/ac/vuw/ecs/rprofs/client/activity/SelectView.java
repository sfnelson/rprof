package nz.ac.vuw.ecs.rprofs.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nz.ac.vuw.ecs.rprofs.client.HistoryMapper;
import nz.ac.vuw.ecs.rprofs.client.place.HasView;
import nz.ac.vuw.ecs.rprofs.client.place.PlaceBuilder;
import nz.ac.vuw.ecs.rprofs.client.views.ViewListView;

import javax.validation.constraints.NotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 17/09/11
 */
public class SelectView extends AbstractActivity implements ViewListView.Presenter {

	@NotNull
	private final ViewListView view;

	@NotNull
	private final PlaceController pc;

	@NotNull
	private final HistoryMapper mapper;

	@Inject
	public SelectView(@NotNull ViewListView views, @NotNull PlaceController pc, @NotNull HistoryMapper mapper) {
		this.view = views;
		this.pc = pc;
		this.mapper = mapper;
	}

	@Override
	public void start(@NotNull AcceptsOneWidget panel, @NotNull EventBus eventBus) {
		view.setPresenter(this);

		view.addPlace("classes", createProvider("classes"));
		view.addPlace("instances", createProvider("instances"));
		view.addPlace("fields", createProvider("fields"));
		view.addPlace("events", createProvider("events"));
	}

	public void setPlace(HasView place) {
		view.setSelected(place.getView());
	}

	@Override
	public void selectView(String view) {
		pc.goTo(PlaceBuilder.create()
				.setView(view)
				.get(pc.getWhere()));
	}

	private Provider<String> createProvider(final String view) {
		return new Provider<String>() {
			@Override
			public String get() {
				Place target = PlaceBuilder.create().setView(view).get(pc.getWhere());
				return mapper.getToken(target);
			}
		};
	}
}
