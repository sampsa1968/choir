import java.util.Set;

/** A singer of a given category (e.g. S, A, T or B) that performs one or more songs (Tracks)
 * 
 * @author SampsaLaine
 *
 */
public class Singer {
	private String name;
	private String category;
	Set<Track> tracks;

	public Singer(String nam, String cat) {
		this.name = nam;
		this.category = cat;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
