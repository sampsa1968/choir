package fi.choir.tuner;

import java.io.File;

import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import fi.choir.tuner.SoundUtil.SoundException;
import fi.datarangers.dmtools.common.GeneralException;
import fi.datarangers.dmtools.data.Data;
import fi.datarangers.dmtools.data.ListData;
import fi.datarangers.dmtools.data.ListVariable;
import fi.datarangers.dmtools.data.Variable;
import fi.datarangers.dmtools.event.StatusMonitor;
import fi.datarangers.dmtools.math.ArrayMath;
import fi.datarangers.dmtools.math.VariableMath;
import fi.datarangers.dmtools.math.som.Som;
import fi.datarangers.dmtools.util.ArrayUtil;
import fi.datarangers.dmtools.util.DataUtil;
import fi.datarangers.dmtools.util.GuiUtil;
import fi.datarangers.dmtools.util.VariableUtil;

public class TunerModel {

	protected static final double SLEEP = 0.05, MAJOR = 1;
	private static final double WIND_STEP = 1;
	private float sampleRate = 44000;
	private Data signal;
	private Som som;
	private Data spectrogram;
	private Thread playThread;
	protected double playPosition = 0;
	private TunerController controller;
	private double analysisLength = 3;

	public TunerModel(TunerController tunerController) {
		this.controller = tunerController;
	}

	public static void main(String[] args) throws Exception {
		GuiUtil.initGui();
		StatusMonitor.setHeadless(true);

		String file = "C:\\Users\\SampsaLaine\\Documents\\Dropbox (Personal)\\Kuoro\\Tuner\\souda souda sinisorsa.wav";
		TunerController c = new TunerController();
		TunerModel model = c.getModel();
		model.load(new File(file));

		c.getPitch();
	}

	private void getScores() {
		// TODO Auto-generated method stub

	}

	private void setPreferredArea(int[] is) {
		// TODO Auto-generated method stub

	}

	public void trainSom() throws GeneralException {
		Data spec = getSpektrogram();
		if (spec != null) {
			getSom().make(spec);
		}
	}

	public Som getSom() throws GeneralException {
		if (som == null) {
			som = new Som();
			trainSom();
		}
		return som;
	}

	private Data getWavelets() {
		double[] sig = this.getSignal().getVariable(0).getDoubleArray();
		// Wavelet w = new Wavelet(sig, 100);
		// w.computeResiduals();
		// Data out = w.getSignals();
		// out = DataUtil.merge(out, w.getResiduals());
		// return out;
		return null;
	}

	private Data getSpektrogram() throws GeneralException {
		if (spectrogram == null)
			spectrogram = DataUtil.createRandomData(200, 6);
		return spectrogram;
	}

	public void load(File file) throws SoundException {

		SoundLoader sl = new SoundLoader();
		sl.load(file);
		byte[] bytes = sl.getBytes();
		sampleRate = sl.getAudioInputStream().getFormat().getSampleRate();

		Variable v = VariableUtil.createVariable(SoundUtil.toSignal(bytes));
		double min = VariableMath.min(v).doubleValue();
		double max = VariableMath.max(v).doubleValue();
		double sca = 2 / (max - min);
		VariableMath.multiply(v, sca);
		double me = VariableMath.mean(v);
		VariableMath.subtract(v, me, true);
		signal = DataUtil.createData(v);

		Variable time = DataUtil.createRunningVariable("Time", v.getRowCount());
		time = VariableMath.divide(time, sampleRate);
		signal.setLabels(time);
	}

	private int getLength() {
		return getSignal().getRowCount();
	}

	public Data getSignal() {
		return signal;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public double getSignalSeconds() {
		int len = getLength();
		double sec = (double) len / getSampleRate();
		return sec;
	}

	public void play() {
		getPlayThread().start();
	}

	private Thread getPlayThread() {
		if (playThread == null) {
			playThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true)
						try {
							if (Thread.interrupted())
								break;
							Thread.sleep((long) (SLEEP * 1000));
							playPosition += SLEEP;
							playPosition = ArrayMath.round(playPosition, 2);
							System.out.println("pos " + playPosition);
							getController().fireMinorStep();
							float rem = (float) ((float) playPosition % MAJOR);
							if (rem == 0)
								getController().fireMajorStep();
						} catch (Exception e) {
							break;
						}
				}

			});
		}
		return playThread;
	}

	protected TunerController getController() {
		return controller;
	}

	public void stop() {
		pause();
		playPosition = 0;
	}

	public void pause() {
		if (playThread != null) {
			playThread.interrupt();
			playThread = null;
		}
	}

	public void forward() {
		playPosition += WIND_STEP;
		getController().fireMajorStep();
	}

	public void backward() {
		playPosition -= WIND_STEP;
		getController().fireMajorStep();
	}

	public double getPlayPosition() {
		return playPosition;
	}

	public double getAnalysisLength() {
		return analysisLength;
	}

	public void setAnalysisLength(double analysisLength) {
		this.analysisLength = analysisLength;
	}

	public Data getPitch(PitchEstimationAlgorithm calgorithm, int cbufferSize) {

		Data ret = new ListData();
		Variable time = new ListVariable("time");
		Variable freq = new ListVariable("freq");
		ret.setLabels(time);
		ret.addVariable(freq);

		PitchDetector cdetector = calgorithm.getDetector(sampleRate, cbufferSize);

		Data sigd = getSignal();
		double[] sig = getSignal().getVariable(0).getDoubleArray();
		float[] sigf = ArrayUtil.floatArray(sig);
		for (int i = 0; i < sig.length - cbufferSize; i += cbufferSize) {
			float[] part = ArrayUtil.getRows(sigf, i, i + cbufferSize);
			PitchDetectionResult result = cdetector.getPitch(part);
			boolean succ = result.isPitched();
			if (succ) {
				time.addCell(sigd.getLabel(i));
				freq.addCell(result.getPitch());
			}
		}

		return ret;
	}

}
