package fi.choir.tuner;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import be.tarsos.dsp.example.InputPanel;
import be.tarsos.dsp.example.PitchDetectionPanel;
import be.tarsos.dsp.util.fft.FFT;
import fi.datarangers.dmtools.common.GeneralException;
import fi.datarangers.dmtools.data.Data;
import fi.datarangers.dmtools.event.StatusMonitor;
import fi.datarangers.dmtools.gui.common.DefaultAction;
import fi.datarangers.dmtools.gui.common.HotColorMap;
import fi.datarangers.dmtools.gui.plotter.ScatterPlotChart;
import fi.datarangers.dmtools.gui.som.SomPlane;
import fi.datarangers.dmtools.math.som.Som;
import fi.datarangers.dmtools.util.GenericUtil;
import fi.datarangers.dmtools.util.GuiUtil;
import net.miginfocom.swing.MigLayout;

public class TunerView extends JPanel {

	protected static final double DEFAULT_RANGE = 3;
	private TunerController controller;
	private JPanel west;
	private JSplitPane center;
	private JButton record;
	private JButton load;
	private JPanel south;
	private JTabbedPane tabs;
	private JButton stop;
	private JPanel somPanel;
	private JPanel fftPanel;
	protected boolean divInit = true;
	private SpecPanel spePanel;
	private JPanel pitchDetection;
	private ActionListener algoListener;
	private InputPanel inputPanel;
	private SpecPanel fftSpecPanel;
	private JPanel playControls;
	private JTextField rangeField;
	private ScatterPlotChart linePlot;

	public TunerView(TunerController ic) {
		super(new BorderLayout());
		this.controller = ic;
		initialize();
	}

	private void initialize() {
		add(getWest(), BorderLayout.WEST);
		add(getCenter());
	}

	private JPanel getWest() {
		if (west != null)
			return west;
		west = new JPanel();
		west.setLayout(new BorderLayout());
		JPanel buts = new JPanel();
		buts.setLayout(new BoxLayout(buts, BoxLayout.X_AXIS));
		buts.add(getLoad());
		buts.add(getRecord());
		buts.add(getStop());
		buts.setBorder(BorderFactory.createTitledBorder("File utils"));
		west.add(buts, BorderLayout.NORTH);

		JPanel cent = new JPanel();
		cent.setLayout(new BoxLayout(cent, BoxLayout.Y_AXIS));
		cent.add(getInputChannel());
		cent.add(getPitchDetection());
		cent.add(getPlayControls());
		west.add(cent);

		return west;
	}

	private Component getInputChannel() {
		if (inputPanel != null)
			return inputPanel;
		inputPanel = new InputPanel();

		inputPanel.addPropertyChangeListener("mixer", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				try {
					getController().setMixer((Mixer) arg0.getNewValue());
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedAudioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		return inputPanel;
	}

	private JPanel getPitchDetection() {
		if (pitchDetection != null)
			return pitchDetection;
		pitchDetection = new JPanel(new BorderLayout());
		pitchDetection.add(new PitchDetectionPanel(getAlgoListener()), BorderLayout.NORTH);
		return pitchDetection;
	}

	private JPanel getPlayControls() {
		if (playControls != null)
			return playControls;
		playControls = new JPanel(new BorderLayout());

		JButton play = new JButton(new DefaultAction("", false, "play") {
			@Override
			public void actionImpl(ActionEvent event) throws GeneralException {
				getController().play();
			}
		});

		JButton rewind = new JButton(new DefaultAction("", false, "fastbackward2") {
			@Override
			public void actionImpl(ActionEvent event) throws GeneralException {
				getController().backward();
			}
		});

		JButton forward = new JButton(new DefaultAction("", false, "fastforward2") {
			@Override
			public void actionImpl(ActionEvent event) throws GeneralException {
				getController().forward();
			}
		});

		JButton pause = new JButton(new DefaultAction("", false, "pause") {
			@Override
			public void actionImpl(ActionEvent event) throws GeneralException {
				getController().pause();
			}
		});

		JButton stop = new JButton(new DefaultAction("", false, "stop") {
			@Override
			public void actionImpl(ActionEvent event) throws GeneralException {
				getController().stop();
			}
		});

		JPanel buts = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
		buts.add(rewind);
		buts.add(play);
		buts.add(pause);
		buts.add(forward);
		buts.add(stop);

		playControls.add(buts, BorderLayout.NORTH);

		JPanel cent = new JPanel(new MigLayout());
		cent.add(new JLabel("Range (s)"));
		cent.add(getRangeField(), "wrap");

		playControls.add(cent);

		return playControls;
	}

	private JTextField getRangeField() {
		if (rangeField != null)
			return rangeField;
		rangeField = new JTextField();
		rangeField.setPreferredSize(new Dimension(50, 20));
		rangeField.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent arg0) {
				double range = GenericUtil.parseNumberSafe(rangeField.getText(), DEFAULT_RANGE);
				getController().setAnalysisLength(range);
			}

		});
		return rangeField;
	}

	private ActionListener getAlgoListener() {
		if (algoListener != null)
			return algoListener;

		algoListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				String name = e.getActionCommand();
				getController().setPitchAlgorithm(name);
			}
		};
		return algoListener;
	}

	private JButton getRecord() {
		if (record != null)
			return record;
		record = new JButton(new DefaultAction("Record", false, "curve") {

			@Override
			public void actionImpl(ActionEvent event) throws GeneralException {
				getController().setRecord(true);
				update();
			}
		});

		return record;
	}

	private JButton getStop() {
		if (stop != null)
			return stop;
		stop = new JButton(new DefaultAction("Stop", false, "stop") {

			@Override
			public void actionImpl(ActionEvent event) throws GeneralException {
				getController().setRecord(false);
				update();
			}
		});

		return stop;
	}

	private JButton getLoad() {
		if (load != null)
			return load;
		load = new JButton(new DefaultAction("Load", false, "open") {

			@Override
			public void actionImpl(ActionEvent event) throws GeneralException {
				File lo = GuiUtil.openFileDialog(west, "wav", "audio-file", false);
				if (lo == null)
					return;
				try {
					StatusMonitor.toHourglass(TunerView.this);
					getController().load(lo);
					getController().setRecord(true);
					update();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					StatusMonitor.toNormal(TunerView.this);
				}
			}
		});

		return load;
	}

	private TunerController getController() {
		return controller;
	}

	private JSplitPane getCenter() {
		if (center != null)
			return center;
		center = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getTabs(), getSouth());
		center.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent eve) {
				if (divInit) {
					center.setDividerLocation(0.7);
					divInit = false;
				}
			}
		});
		return center;
	}

	private JPanel getSouth() {
		if (south != null)
			return south;
		south = new JPanel(new BorderLayout());
		return south;
	}

	private Component getTabs() {
		if (tabs != null)
			return tabs;
		tabs = new JTabbedPane();
		tabs.addTab("SOM", getSomPanel());
		tabs.addTab("Spectrogram", getSpectrogramPanel());
		tabs.addTab("FFT", getFftPanel());
		return tabs;
	}

	private JPanel getSomPanel() {
		if (somPanel == null) {
			somPanel = new JPanel(new BorderLayout());
			somPanel.setBorder(BorderFactory.createTitledBorder("SOM"));
		}
		return somPanel;
	}

	private JPanel getFftPanel() {
		if (fftPanel == null) {
			fftPanel = new JPanel(new BorderLayout());
			fftPanel.setBorder(BorderFactory.createTitledBorder("FFT"));
			fftPanel.add(getFFTPanel());
		}
		return fftPanel;
	}

	private SpecPanel getSpectrogramPanel() {
		if (spePanel != null)
			return spePanel;
		spePanel = new SpecPanel();
		return spePanel;
	}

	private SpecPanel getFFTPanel() {
		if (fftSpecPanel != null)
			return fftSpecPanel;
		fftSpecPanel = new SpecPanel();
		return fftSpecPanel;
	}

	private void createLineplot(Data d) throws GeneralException {
		if (linePlot != null)
			return;
		linePlot = new ScatterPlotChart(d.getLabels(), d.getVariable(0));
		linePlot.setDraft(true);
		linePlot.setMarkers(false);
		linePlot.setLine(true);
	}

	void update() {
		StatusMonitor.toHourglass(TunerView.this);
		try {
			Data d = getController().getSignal();
			if (d == null)
				return;

			getSouth().removeAll();
			getSouth().add(getLinePlot());
			getSouth().revalidate();

			Som som = getController().getSom();
			SomPlane sp = new SomPlane(som, 0, new HotColorMap(0.5f));
			sp.setShowVectors(true);
			getSomPanel().removeAll();
			getSomPanel().add(sp);

			// Data fft = DataMath.fft(d);
			// ScatterPlotChart fline = new ScatterPlotChart(fft.getLabels(),
			// fft.getVariable(0));
			// fline.setDraft(true);
			// fline.setMarkers(false);
			// fline.setLine(true);
			// getFftPanel().removeAll();

			repaint();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			StatusMonitor.toNormal(TunerView.this);
		}
	}

	private ScatterPlotChart getLinePlot() throws GeneralException {
		if (linePlot == null) {
			Data d = getController().getSignal();
			if (d == null)
				throw new GeneralException("Cannot create lineplot");
			createLineplot(d);
		}

		return linePlot;
	}

	public void drawSpectrogram(float pitch, float[] amplitudes) {
		getSpectrogramPanel().drawSpectrogram(pitch, amplitudes);
	}

	public void drawFFT(float pitch, float[] amplitudes) {
		getFFTPanel().drawFFT(pitch, amplitudes);
	}

	void setBox(double min, double max) throws GeneralException {
		getLinePlot().getAxisX().setBox(min, max);
	}

}
