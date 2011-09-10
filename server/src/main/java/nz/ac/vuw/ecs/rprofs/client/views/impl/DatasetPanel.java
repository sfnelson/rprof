package nz.ac.vuw.ecs.rprofs.client.views.impl;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
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
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;

import java.util.Date;
import java.util.List;

public class DatasetPanel extends Composite implements DatasetListView {

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
		CellTable<DatasetProxy> table = new CellTable<DatasetProxy>();

		table.addColumn(new Column<DatasetProxy, Date>(new DateCell(DateTimeFormat.getFormat("MMMM dd, h:mm"))) {
			@Override
			public Date getValue(DatasetProxy d) {
				return d.getStarted();
			}
		});

		table.addColumn(new Column<DatasetProxy, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DatasetProxy d) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if (d.getStopped() == null) {
					sb.appendHtmlConstant("<em>running</em>");
				} else {
					long time = d.getStopped().getTime() - d.getStarted().getTime();
					sb.append(time / 1000);
					sb.appendEscaped(" seconds");
				}
				return sb.toSafeHtml();
			}
		});

		ActionCell<DatasetProxy> inspect = new ActionCell<DatasetProxy>(
				new SafeHtmlBuilder().appendEscaped("Inspect").toSafeHtml(),
				new ActionCell.Delegate<DatasetProxy>() {
					@Override
					public void execute(DatasetProxy ds) {
						presenter.selectDataset(ds);
					}
				});
		table.addColumn(new Column<DatasetProxy, DatasetProxy>(inspect) {
			@Override
			public DatasetProxy getValue(DatasetProxy ds) {
				return ds;
			}
		});

		ActionCell<DatasetProxy> stop = new ActionCell<DatasetProxy>(
				new SafeHtmlBuilder().appendEscaped("Stop").toSafeHtml(),
				new ActionCell.Delegate<DatasetProxy>() {
					@Override
					public void execute(DatasetProxy ds) {
						presenter.stopDataset(ds);
					}
				});
		table.addColumn(new Column<DatasetProxy, DatasetProxy>(stop) {
			@Override
			public DatasetProxy getValue(DatasetProxy ds) {
				return ds;
			}
		});

		ActionCell<DatasetProxy> remove = new ActionCell<DatasetProxy>(
				new SafeHtmlBuilder().appendEscaped("Delete").toSafeHtml(),
				new ActionCell.Delegate<DatasetProxy>() {
					@Override
					public void execute(DatasetProxy ds) {
						presenter.deleteDataset(ds);
					}
				});
		table.addColumn(new Column<DatasetProxy, DatasetProxy>(remove) {
			@Override
			public DatasetProxy getValue(DatasetProxy ds) {
				return ds;
			}
		});

		ProvidesKey<DatasetProxy> keyProvider = new ProvidesKey<DatasetProxy>() {
			@Override
			public String getKey(DatasetProxy item) {
				return item.getHandle();
			}
		};
		table.setSelectionModel(new SingleSelectionModel<DatasetProxy>(keyProvider));

		return table;
	}
}
