package fi.choir.tuner;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.tarsos.dsp.util.fft.FFT;
import fi.datarangers.dmtools.common.GeneralException;
import fi.datarangers.dmtools.data.Data;
import fi.datarangers.dmtools.data.Variable;
import fi.datarangers.dmtools.math.ArrayMath;
import fi.datarangers.dmtools.math.som.Som;
import fi.datarangers.dmtools.util.ArrayUtil;
import fi.datarangers.dmtools.util.DataUtil;

public class TunerController {

	private TunerView view;
	private TunerModel model;
	private Thread recorder;
	private AudioDispatcher dispatcher;

	private int bufferSize = 1024 * 4;
	private int overlap = 768 * 4;
	private Mixer currentMixer;
	private PitchEstimationAlgorithm algo = PitchEstimationAlgorithm.YIN;
	private PitchDetectionHandler handler;
	protected float pitch;

	FFT fft = new FFT(bufferSize);

	public void setView(TunerView view) {
		this.view = view;
	}

	public TunerController() throws GeneralException {
	}

	public void exit() {
		System.exit(0);
	}

	public void load(File lo) throws GeneralException {
		try {
			getModel().load(lo);
			setFile(lo);
			getView().update();
		} catch (Exception e) {
			throw new GeneralException(e);
		}
	}

	public TunerModel getModel() {
		if (model == null)
			model = new TunerModel(this);
		return model;
	}

	public Data getSignal() {
		return getModel().getSignal();
	}

	public void setRecord(boolean b) {
		if (b) {
			recorder = new Thread(dispatcher, "Audio dispatching");
			recorder.start();
		} else {
			if (dispatcher != null) {
				dispatcher.stop();
			}
			recorder.interrupt();
			recorder = null;
		}

	}

	// private Thread getRecorder() {
	// if (recorder != null)
	// return recorder;
	// recorder = new Thread(new Runnable() {
	// @Override
	// public void run() {
	// int c = 0;
	// while (!recorder.isInterrupted()) {
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// break;
	// }
	// System.out.println("Recordn: " + c++);
	//
	// // System.out.println("Seconds: " + getModel().getSignalSeconds());
	// }
	// recorder = null;
	// }
	// });
	// return recorder;
	// }

	public Som getSom() throws GeneralException {
		return getModel().getSom();
	}

	public void setMixer(Mixer mixer) throws LineUnavailableException, UnsupportedAudioFileException {

		float sampleRate = getModel().getSampleRate();

		if (dispatcher != null) {
			dispatcher.stop();
		}
		final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
		final DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
		TargetDataLine line;
		line = (TargetDataLine) mixer.getLine(dataLineInfo);
		final int numberOfSamples = bufferSize;
		line.open(format, numberOfSamples);
		line.start();
		final AudioInputStream stream = new AudioInputStream(line);

		JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
		// create a new dispatcher
		dispatcher = new AudioDispatcher(audioStream, bufferSize, overlap);

		currentMixer = mixer;

		// add a processor, handle pitch event.
		dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, getHandler()));
		dispatcher.addAudioProcessor(fftProcessor);

	}

	public void setFile(File audioFile) throws LineUnavailableException, UnsupportedAudioFileException {

		float sampleRate = getModel().getSampleRate();

		if (dispatcher != null) {
			dispatcher.stop();
		}
		try {
			dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);
			AudioFormat format = AudioSystem.getAudioFileFormat(audioFile).getFormat();
			dispatcher.addAudioProcessor(new AudioPlayer(format));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// add a processor, handle pitch event.
		dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, getHandler()));
		dispatcher.addAudioProcessor(fftProcessor);

	}

	private PitchDetectionHandler getHandler() {
		if (handler != null)
			return handler;
		handler = new PitchDetectionHandler() {

			@Override
			public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
				if (pitchDetectionResult.isPitched()) {
					pitch = pitchDetectionResult.getPitch();
				} else {
					pitch = -1;
				}
			}

		};
		return handler;
	}

	AudioProcessor fftProcessor = new AudioProcessor() {

		float[] amplitudes = new float[bufferSize / 2];

		@Override
		public void processingFinished() {
			// TODO Auto-generated method stub
		}

		@Override
		public boolean process(AudioEvent audioEvent) {
			float[] audioFloatBuffer = audioEvent.getFloatBuffer();
			float mi = ArrayMath.min(audioFloatBuffer);
			float ma = ArrayMath.max(audioFloatBuffer);
			System.out.println("mi ja ma:" + mi + " " + ma);
			float[] transformbuffer = new float[bufferSize * 2];
			System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length);
			fft.forwardTransform(transformbuffer);
			fft.modulus(transformbuffer, amplitudes);
			getView().drawSpectrogram(pitch, amplitudes);
			getView().drawFFT(pitch, amplitudes);
			getView().repaint();
			return true;
		}

	};

	protected TunerView getView() {
		if (view == null)
			view = new TunerView(this);
		return view;
	}

	public void setPitchAlgorithm(String name) {
		PitchEstimationAlgorithm newAlgo = PitchEstimationAlgorithm.valueOf(name);
		algo = newAlgo;
		try {
			setMixer(currentMixer);
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		}
	}

	public void setAnalysisLength(double range) {
		getModel().setAnalysisLength(range);
	}

	public void play() {
		getModel().play();
		fireMajorStep();
	}

	public void forward() {
		getModel().forward();
		fireMajorStep();
	}

	public void backward() {
		getModel().backward();
		fireMajorStep();
	}

	public void stop() {
		getModel().stop();
		fireMajorStep();
	}

	public void pause() {
		getModel().pause();
		fireMajorStep();
	}

	public void fireMinorStep() {
		try {
			double max = getPlayPosition();
			double min = max - getAnalysisLength();
			getView().setBox(min, max);
			updateFftPeriod(min, max);

		} catch (GeneralException e) {
			e.printStackTrace();
		}
	}

	public void fireMajorStep() {
		fireMinorStep();
	}

	public double getPlayPosition() {
		return getModel().getPlayPosition();
	}

	public double getAnalysisLength() {
		return getModel().getAnalysisLength();
	}

	public boolean updateFftPeriod(double start, double end) {
		Data d = getModel().getSignal();
		Data sub = DataUtil.getRowsBetween(d, d.getLabels(), start, end);
		Variable v = sub.getVariable(0);
		float[] audioFloatBuffer = ArrayUtil.floatArray(v.getDoubleArray());
		double par = Math.log(audioFloatBuffer.length) / Math.log(2);
		double pow = Math.ceil(par);
		double len = Math.pow(2, pow);
		float[] transformbuffer = new float[(int) len * 2];
		float[] amplitudes = new float[(int) len / 2];

		if (amplitudes.length > 0)
			try {
				System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length);
				fft.forwardTransform(transformbuffer);
				fft.modulus(transformbuffer, amplitudes);

				amplitudes = ArrayMath.multiply(amplitudes, 5);
//				getView().drawSpectrogram(pitch, amplitudes);
				getView().drawFFT(pitch, amplitudes);
				getView().repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return true;
	}

}
