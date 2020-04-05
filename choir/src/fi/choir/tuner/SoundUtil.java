package fi.choir.tuner;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import fi.datarangers.dmtools.data.Data;
import fi.datarangers.dmtools.data.Variable;
import fi.datarangers.dmtools.gui.plotter.LinePlotBrowser;
import fi.datarangers.dmtools.math.VariableMath;
import fi.datarangers.dmtools.util.DataUtil;
import fi.datarangers.dmtools.util.VariableUtil;

/**
 * @author Sampsa Laine
 */
public class SoundUtil {

	public static byte[] getBytes(AudioInputStream audioInputStream) throws SoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
		if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
			bytesPerFrame = 1;
		}

		int numBytes = 1024 * bytesPerFrame;
		byte[] audioBytes = new byte[numBytes];
		try {
			while ((audioInputStream.read(audioBytes)) != -1) {
				baos.write(audioBytes);
			}
		} catch (Exception ex) {
			throw new SoundException("Unable to convert byte array");
		}
		return baos.toByteArray();
	}

	public static double[] toSignal(byte[] data) {
		int n = data.length;
		double MAX_16_BIT = Short.MAX_VALUE; // 32,767
		double[] d = new double[n / 2];
		for (int i = 0; i < n / 2; i++) {
			d[i] = ((short) (((data[2 * i + 1] & 0xFF) << 8) + (data[2 * i] & 0xFF))) / ((double) MAX_16_BIT);
		}
		return d;
	}

	public static void showSignal(byte[] audioBytes, float f) {
		double[] doubl = toSignal(audioBytes);
		Variable x = DataUtil.createRunningVariable(doubl.length);
		x = VariableMath.divide(x, (double) f);
		Variable v = VariableUtil.createVariable(doubl);
		Data dat = DataUtil.createData(v);
		dat.setLabels(x);
		LinePlotBrowser.showPlot(dat);
	}

	public static class SoundException extends Exception {

		public SoundException(String string) {
			super(string);
		}

		public SoundException(Exception e) {
			super(e);
		}

	}

	public static void showSignal(byte[] audioBytes) {
		double[] doubl = toSignal(audioBytes);
		Variable x = DataUtil.createRunningVariable(audioBytes.length);
		Variable v = VariableUtil.createVariable(doubl);
		Data dat = DataUtil.createData(x, v);
		LinePlotBrowser.showPlot(dat);
	}

}