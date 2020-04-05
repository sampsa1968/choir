/**
 * The model containing all data required for operations
 * 
 * @author SampsaLaine
 *
 */
public class ChoirPlayer {
	private ChoirController controller;

	public ChoirPlayer(ChoirController choirController) throws Exception {
		this.controller = choirController;
		playTest();
	}

	public static synchronized void playTest() throws Exception {
	}
}
