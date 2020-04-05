import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * A performance of a musical piece recorded to the system.
 * 
 * @author SampsaLaine
 *
 */
public class Song {
	String id;
	String name;
	Choir choir;
	Set<Track> tracks;
	Set<Position> positions;

	/**
	 * creates a real time stream of music, which is mixed from the Singers in their
	 * Positions, with echo added, in respect to the Listener. The stream can be
	 * played to the respondent of this stream, and the respondent can change the
	 * position of the Listener in real time to influence the mixing.
	 * 
	 * @return
	 */
	public OutputStream play() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);
		return bos;
	}
}
