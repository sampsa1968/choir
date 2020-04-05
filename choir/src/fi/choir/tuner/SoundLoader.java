package fi.choir.tuner;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import fi.choir.tuner.SoundUtil.SoundException;

public class SoundLoader {

	Thread thread;
	private ByteArrayOutputStream out;

	private AudioInputStream audioInputStream;

	public static void main(String s[]) throws Exception {
		SoundLoader sl = new SoundLoader();
		sl.load(new File("C:\\Users\\SampsaLaine\\Documents\\eclipse\\gps\\testfiles\\song1.wav"));
		SoundUtil.showSignal(sl.getBytes(), sl.getSamplingRate());
	}

	private float getSamplingRate() {
		return audioInputStream.getFormat().getSampleRate();
	}

	public byte[] getBytes() throws SoundException {
		return SoundUtil.getBytes(this.getAudioInputStream());
	}

	public void load(File fileIn) throws SoundException {
		try {
			audioInputStream = AudioSystem.getAudioInputStream(fileIn);
		} catch (Exception e) {
			throw new SoundException(e);
		}
	}

	public AudioInputStream getAudioInputStream() {
		return audioInputStream;
	}

}