package fi.choir.digi;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class ChoirTest {
	/*
	 * Play a *.wav or *.au file
	 * 
	 * @param args args[0] on command line is name of file to play
	 */
	static int frameSample;
	static int timeofFrame;
	static int N;
	static int runTimes;
	static int bps;
	static int channels;
	static double times;
	static int bufSize;
	static int frameSize;
	static int frameRate;
	static long length;

	public static void main(String[] args) {
		try {
			String arg = "search-ms:displayname=Search%20Results%20in%20Downloads&crumb=System.Generic.String%3Awav&crumb=location:C%3A%5CUsers%5CSampsaLaine%5CDownloads";
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(arg));
			AudioInputStream a;

			File file = new File(args[0]); /* To get the file size */
			length = file.length();
			System.out.println("File size : " + length);

			AudioFormat af = ais.getFormat();
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);

			if (!AudioSystem.isLineSupported(info)) {
				System.out.println("unsupported line");
				System.exit(0);
			}

			frameRate = (int) af.getFrameRate();
			System.out.println("Frame Rate: " + frameRate);

			frameSize = af.getFrameSize();
			System.out.println("Frame Size: " + frameSize);

			bufSize = frameRate * frameSize / 10;
			System.out.println("Buffer Size: " + bufSize);

			channels = af.getChannels();
			System.out.println("Channels : " + channels);

			bps = af.getSampleSizeInBits();
			System.out.println("Bits per sample : " + bps);

			times = (double) (length / (frameRate * channels * bps / 8));
			System.out.println("Duration of the songs : " + times + " seconds");

			byte[] data2 = new byte[bufSize];
			int bytesRead2;
			timeofFrame = 20; // 20ms
			frameSample = (timeofFrame * frameRate) / 1000;
			N = frameSample;
			runTimes = (int) (times * 1000) / 20;

			byte[] data = new byte[bufSize];
			int bytesRead;
			/*
			 * SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			 * line.open(af, bufSize); line.start();
			 * 
			 * while ((bytesRead = ais.read(data, 0, data.length)) != -1) line.write(data,
			 * 0, bytesRead);
			 * 
			 * line.drain(); line.stop();
			 * 
			 * long time = line.getMicrosecondPosition();
			 * System.out.println("time by playing " + time); line.close();
			 */

			int[][] freq = new int[runTimes][N];

			int temp = 0;

			/*
			 * Want to read the data of the wav and do the DFT later, but i don't know how
			 * to read data and save it correctly?" for (int i = 0; i < runTimes; i++)
			 * //for(int j=0;j<N;j++) { a.read(freq[i], j, N); j = N; }
			 * 
			 * 
			 */

			FileInputStream fis = null;
			BufferedInputStream bis = null;
			DataInputStream dis = null;

			fis = new FileInputStream(file);

// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

// dis.available() returns 0 if the file does not have more lines.
			while (dis.available() != 0) {

// this statement reads the line from the file and print it to the console.
				for (int i = 0; i < 1; i++)
					for (int j = 0; j < N; j++) {
						freq[i][j] = (int) dis.readByte();

					}
			}
			System.out.println(freq[0][0]);

// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/*
		 * for (int i = 0; i < 10; i++) //for(int j=0;j<N;j++) { freq[i] = a.read(file);
		 * //j = N; } System.out.println(freq[0]);
		 */

		catch (Exception e) {
			System.out.println(e.toString());
		}
		System.exit(0);
	}
}
