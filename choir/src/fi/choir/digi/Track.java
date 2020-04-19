package fi.choir.digi;

/**
 * The technical recording of a Singers performance of a Song
 * 
 * @author SampsaLaine
 *
 */
public class Track {
	Singer singer;
	Song song;
	Choir choir;
	byte[] audioBytes;
	byte[] videoBytes;
	int sampleRate;
	String format;
	
	public double[] getSignal() {
		
		return null;
	}
}
