/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package fi.choir.tuner;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import be.tarsos.dsp.util.PitchConverter;

public class SpecPanel extends JComponent implements ComponentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3729805747119272534L;

	private BufferedImage bufferedImage;
	private Graphics2D bufferedGraphics;

	private int position;

	public SpecPanel() {
		bufferedImage = new BufferedImage(640 * 4, 480 * 4, BufferedImage.TYPE_INT_RGB);
		bufferedGraphics = bufferedImage.createGraphics();
		this.addComponentListener(this);
	}

	private int frequencyToBin(final double frequency, boolean horizontal) {
		final double minFrequency = 50; // Hz
		final double maxFrequency = 11000; // Hz
		int bin = 0;
		double dim = horizontal ? getWidth() : getHeight();
		final boolean logaritmic = true;
		if (frequency != 0 && frequency > minFrequency && frequency < maxFrequency) {
			double binEstimate = 0;
			if (logaritmic) {
				final double minCent = PitchConverter.hertzToAbsoluteCent(minFrequency);
				final double maxCent = PitchConverter.hertzToAbsoluteCent(maxFrequency);
				final double absCent = PitchConverter.hertzToAbsoluteCent(frequency * 2);
				binEstimate = (absCent - minCent) / maxCent * dim;
			} else {
				binEstimate = (frequency - minFrequency) / maxFrequency * dim;
			}
			if (binEstimate > 700) {
				System.out.println(binEstimate + "");
			}
			bin = (int) dim - 1 - (int) binEstimate;
		}
		return bin;
	}

	public void paintComponent(final Graphics g) {
		g.drawImage(bufferedImage, 0, 0, null);
	}

	String currentPitch = "";

	public static double[] toDoubleArray(float[] arr) {
		double[] ret = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			ret[i] = (double) arr[i];
		return ret;
	}

	public static double std(double[] vec) {
		return std(vec, mean(vec));
	}

	public static double std(double[] vec, double mean) {
		double sum = 0;
		double count = 0;
		double dif;
		for (int i = 0; i < vec.length; i++) {
			if (!Double.isNaN(vec[i])) {
				dif = vec[i] - mean;
				sum += dif * dif;
				count++;
			}
		}
		if (count == 0) {
			return Double.NaN;
		}
		if (count == 1) {
			return 0;
		} else {
			return Math.sqrt(sum / (count - 1));
		}
	}


	public static double mean(double[] vec) {
		double sum = 0;
		int count = 0;
		for (int i = 0; i < vec.length; i++) {
			if (!Double.isNaN(vec[i])) {
				sum += vec[i];
				count++;
			}
		}
		if (count == 0) {
			return Double.NaN;
		} else {
			return sum / count;
		}
	}



	public void drawSpectrogram(double pitch, float[] amplitudes) {
		double[] amplitudesd = toDoubleArray(amplitudes);
		double ma = std(amplitudesd);
		boolean zero = false;
		if (ma < 1) {
			zero = true;
		}

		// amplitudesd = ArrayMath.sqrt(amplitudesd);

		double maxAmplitude = 0;
		// for every pixel calculate an amplitude
		double[] pixeledAmplitudesd = new double[getHeight()];
		// iterate the lage arrray and map to pixels
		for (int i = amplitudesd.length / 800; i < amplitudesd.length; i++) {
			int pixelY = frequencyToBin(i * 44100 / (amplitudesd.length * 8), false);
			pixeledAmplitudesd[pixelY] += amplitudesd[i];
			maxAmplitude = Math.max(pixeledAmplitudesd[pixelY], maxAmplitude);
		}

		// draw the pixels
		for (int i = 0; i < pixeledAmplitudesd.length; i++) {
			Color color = Color.black;
			if (maxAmplitude != 0) {

				final int greyValue = (int) (Math.log1p(pixeledAmplitudesd[i] / maxAmplitude) / Math.log1p(1.0000001)
						* 255);
				color = zero ? Color.black : new Color(greyValue, greyValue, greyValue);
			}
			bufferedGraphics.setColor(color);
			bufferedGraphics.fillRect(position, i, 3, 1);
		}

		if (pitch != -1) {
			int pitchIndex = frequencyToBin(pitch, false);
			bufferedGraphics.setColor(Color.RED);
			bufferedGraphics.fillRect(position, pitchIndex, 1, 1);
			currentPitch = new StringBuilder("Current frequency: ").append((int) pitch).append("Hz").toString();
		}

		bufferedGraphics.clearRect(0, 0, 190, 30);
		bufferedGraphics.setColor(Color.WHITE);
		bufferedGraphics.drawString(currentPitch, 20, 20);

		for (int i = 100; i < 500; i += 100) {
			int bin = frequencyToBin(i, false);
			bufferedGraphics.drawLine(0, bin, 5, bin);
		}

		for (int i = 500; i <= 20000; i += 500) {
			int bin = frequencyToBin(i, false);
			bufferedGraphics.drawLine(0, bin, 5, bin);
		}

		for (int i = 100; i <= 20000; i *= 10) {
			int bin = frequencyToBin(i, false);
			bufferedGraphics.drawString(String.valueOf(i), 10, bin);
		}

		repaint();
		position += 3;
		position = position % getWidth();
	}

	public void drawFFT(double pitch, float[] amplitudes) {
		double[] amplitudesd = toDoubleArray(amplitudes);

		int wi = getWidth();
		int he = getHeight();

		double maxAmplitude = 0;
		// for every pixel calculate an amplitude
		int[] pad = new int[wi];
		// iterate the lage arrray and map to pixels
		for (int i = amplitudesd.length / 800; i < amplitudesd.length; i++) {
			int pixelY = frequencyToBin(i * 44100 / (amplitudesd.length * 8), true);
			if (pixelY < 0)
				continue;
			pad[pixelY] += amplitudesd[i];
			maxAmplitude = Math.max(pad[pixelY], maxAmplitude);
		}

		for (int i = 0; i < pad.length; i++)
			pad[i] = he - pad[i];

		bufferedGraphics.setColor(Color.WHITE);
		bufferedGraphics.fillRect(0, 0, wi, getHeight());

		int len = pad.length;
		bufferedGraphics.setColor(Color.BLUE);
		int[] xPoints = createIncreasingArray(0, len);
		xPoints = flip(xPoints);
		bufferedGraphics.drawPolyline(xPoints, pad, len);

		repaint();
	}

	public static int[] createIncreasingArray(int start, int length) {
		int[] array = new int[length];
		for (int i = 0; i < length; i++) {
			array[i] = start + i;
		}
		return array;
	}


	public static int[] flip(int[] vec) {
		int[] ret = new int[vec.length];
		int len = ret.length;
		for (int i = 0; i < vec.length; i++)
			ret[len - i - 1] = vec[i];
		return ret;
	}

	public static double[] flip(double[] vec) {
		double[] ret = new double[vec.length];
		int len = ret.length;
		for (int i = 0; i < vec.length; i++)
			ret[len - i - 1] = vec[i];
		return ret;
	}


	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		bufferedGraphics = bufferedImage.createGraphics();
		position = 0;
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

}
