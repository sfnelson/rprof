package nz.ac.vuw.ecs.rprofs.client.views.impl;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.ui.TimeFormat;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;

import java.util.Date;
import java.util.List;

public class DatasetPanel extends Composite implements DatasetListView {

	public interface DatasetTableResources extends CellTable.Resources {
		@Override
		@ClientBundle.Source({"nz/ac/vuw/ecs/rprofs/client/views/impl/DatasetTable.css"})
		CellTable.Style cellTableStyle();
	}

	private static DatasetPanelUiBinder uiBinder = GWT.create(DatasetPanelUiBinder.class);

	interface DatasetPanelUiBinder extends UiBinder<Widget, DatasetPanel> {
	}

	@UiField
	CellTable<DatasetProxy> table;

	private Presenter presenter;

	public DatasetPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setNumDatasets(int numDatasets) {
		table.setRowCount(numDatasets);
	}

	@Override
	public void setDatasets(List<DatasetProxy> datasets) {
		table.setRowData(datasets);
	}

	@Override
	public void setSelected(DatasetProxy dataset) {
		table.getSelectionModel().setSelected(dataset, true);
	}

	@UiFactory
	CellTable<DatasetProxy> createTable() {
		ProvidesKey<DatasetProxy> keyProvider = new ProvidesKey<DatasetProxy>() {
			@Override
			public String getKey(DatasetProxy item) {
				return item.getHandle();
			}
		};

		CellTable.Resources resources = GWT.create(DatasetTableResources.class);

		CellTable<DatasetProxy> table = new CellTable<DatasetProxy>(0, resources, keyProvider);

		table.addColumn(new Column<DatasetProxy, Date>(new DateCell(DateTimeFormat.getFormat("MMMM dd, h:mma"))) {
			@Override
			public Date getValue(DatasetProxy d) {
				return d.getStarted();
			}
		});

		table.addColumn(new Column<DatasetProxy, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DatasetProxy d) {
				if (d.getStopped() == null) {
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					sb.appendHtmlConstant("<em>running</em>");
					return sb.toSafeHtml();
				} else {
					long time = d.getStopped().getTime() - d.getStarted().getTime();
					return TimeFormat.DOUBLE_PRECISION.format(time / 1000);
				}
			}
		});

		List<HasCell<DatasetProxy, ?>> controls = Lists.<HasCell<DatasetProxy, ?>>newArrayList(
				new DatasetColumn(new ActionCell<DatasetProxy>(
						new SafeHtmlBuilder().appendEscaped("Inspect").toSafeHtml(),
						new ActionCell.Delegate<DatasetProxy>() {
							@Override
							public void execute(DatasetProxy ds) {
								presenter.selectDataset(ds);
							}
						})),
				new DatasetColumn(new ActionCell<DatasetProxy>(
						new SafeHtmlBuilder().appendEscaped("Stop").toSafeHtml(),
						new ActionCell.Delegate<DatasetProxy>() {
							@Override
							public void execute(DatasetProxy ds) {
								presenter.stopDataset(ds);
							}
						})),
				new DatasetColumn(new ActionCell<DatasetProxy>(
						new SafeHtmlBuilder().appendEscaped("Delete").toSafeHtml(),
						new ActionCell.Delegate<DatasetProxy>() {
							@Override
							public void execute(DatasetProxy ds) {
								presenter.deleteDataset(ds);
							}
						}))
		);

		table.addColumn(new Column<DatasetProxy, DatasetProxy>(
				new CompositeCell<DatasetProxy>(controls)) {
			@Override
			public DatasetProxy getValue(DatasetProxy dataset) {
				return dataset;
			}
		});

		final SingleSelectionModel<DatasetProxy> selectionModel = new SingleSelectionModel<DatasetProxy>();
		table.setSelectionModel(selectionModel);
		table.addDomHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					presenter.selectDataset(selectionModel.getSelectedObject());
					event.stopPropagation();
					event.preventDefault();
				} else if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
					presenter.deleteDataset(selectionModel.getSelectedObject());
					event.stopPropagation();
					event.preventDefault();
				}
			}
		}, KeyUpEvent.getType());

		return table;
	}

	private static class DatasetColumn extends Column<DatasetProxy, DatasetProxy> {
		public DatasetColumn(Cell<DatasetProxy> cell) {
			super(cell);
		}

		public DatasetProxy getValue(DatasetProxy dataset) {
			return dataset;
		}
	}
}
