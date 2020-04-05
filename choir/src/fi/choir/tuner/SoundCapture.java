package fi.choir.tuner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class SoundCapture {

	AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

	float rate = 44100.0f;
	int channels = 1;
	int frameSize = 4;
	int sampleSize = 16;
	boolean bigEndian = false;

	final int bufSize = 16384;

	String errStr;

	double duration, seconds;

	TargetDataLine line;

	Thread thread;
	private ByteArrayOutputStream out;

	public void start() {

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				duration = 0;

				AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels,
						rate, bigEndian);

				DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

				if (!AudioSystem.isLineSupported(info)) {
					shutDown("Line matching " + info + " not supported.");
					return;
				}

				// get and open the target data line for capture.

				try {
					line = (TargetDataLine) AudioSystem.getLine(info);
					line.open(format, line.getBufferSize());
				} catch (LineUnavailableException ex) {
					shutDown("Unable to open the line: " + ex);
					return;
				} catch (SecurityException ex) {
					shutDown(ex.toString());
					// JavaSound.showInfoDialog();
					return;
				} catch (Exception ex) {
					shutDown(ex.toString());
					return;
				}

				// play back the captured audio data
				out = new ByteArrayOutputStream();
				int frameSizeInBytes = format.getFrameSize();
				int bufferLengthInFrames = line.getBufferSize() / 8;
				int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
				byte[] data = new byte[bufferLengthInBytes];
				int numBytesRead;

				line.start();

				while (thread != null) {
					if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
						break;
					}
					out.write(data, 0, numBytesRead);
				}

				// we reached the end of the stream.
				// stop and close the line.
				line.stop();
				line.close();
				line = null;

				// stop and close the output stream
				try {
					out.flush();
					out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}

		});
		thread.setName("Capture");
		thread.start();
	}

	public void stop() {
		thread = null;
	}

	private void shutDown(String message) {
		if ((errStr = message) != null && thread != null) {
			thread = null;
			System.err.println(errStr);
		}
	}

	public static int toInt(byte[] bytes) {
		return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	public static void main(String s[]) throws Exception {
		SoundCapture ssc = new SoundCapture();
		ssc.start();
		System.out.println("Started");
		Thread.sleep(3000);
		ssc.stop();
		System.out.println("Stopped");
		ssc.show();
	}

	private void show() {
		byte audioBytes[] = out.toByteArray();
		SoundUtil.showSignal(audioBytes);
	}
}