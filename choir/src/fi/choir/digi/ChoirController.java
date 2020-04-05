package fi.choir.digi;
public class ChoirController {
	private ChoirUI ui;
	private ChoirModel model;

	public ChoirController(String dbName) throws Exception {
//		model = new ChoirModel(this, dbName);
//		ui = new ChoirUI(this);
		ChoirPlayer.playTest();
	}

	public ChoirUI getUi() {
		return ui;
	}

	public void setUi(ChoirUI ui) {
		this.ui = ui;
	}

	public ChoirModel getModel() {
		return model;
	}

	public void setModel(ChoirModel model) {
		this.model = model;
	}
}
