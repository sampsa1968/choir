package fi.choir.tuner;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;

import fi.datarangers.dmtools.common.CustomerInfo;
import fi.datarangers.dmtools.common.GeneralException;
import fi.datarangers.dmtools.common.Settings;
import fi.datarangers.dmtools.util.GuiUtil;
import fi.datarangers.dmtools.util.LogUtil;
import fi.datarangers.flow.controller.FlowController;
import fi.datarangers.flow.view.FlowWindow;

public class Tuner {

	public static void main(String[] args) throws GeneralException, InterruptedException {
		LogUtil.initLogger();
		GuiUtil.initGui();
		FlowWindow.dockingInit = false;
		FlowController.playerMode = true;

		try {
			Settings.getCommonSettings().initialize("Tuner");
			CustomerInfo.update();

		} catch (GeneralException e) {
			e.printStackTrace();
		}

		try {

			final TunerController ic = new TunerController();
			ic.load(new File("C:\\Users\\SampsaLaine\\Documents\\Dropbox (Personal)\\Kuoro\\Tuner\\souda souda sinisorsa.wav"));
			final TunerView panel = ic.getView();
			ic.setView(panel);

			final JFrame frame = new JFrame("DataPlayer");
			frame.setSize(900, 600);
			frame.setContentPane(panel);
			// frame.pack();

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					ic.exit();
				}
			});


			frame.setVisible(true);
			frame.repaint();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
