import java.sql.SQLException;

import javax.swing.JOptionPane;

/**
 * The main class for the choir application
 * 
 * @author SampsaLaine
 *
 */
public class ChoirApp {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting 2");
		if (args.length == 0) 
			args = new String[] { "run", "C:\\Users\\SampsaLaine\\Documents\\eclipse\\choir/data.db" };
		ChoirController cc = new ChoirController(args[1]);
		ChoirUI ui = cc.getUi();
		JOptionPane.showMessageDialog(null, ui);
	}

}
