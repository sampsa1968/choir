package fi.choir.digi;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * The model containing all data required for operations
 * 
 * @author SampsaLaine
 *
 */
public class ChoirModel {
	private String dbName;
	private ChoirController controller;

	public ChoirModel(ChoirController choirController, String db) throws Exception {
		this.dbName = db;
		this.controller = choirController;
		initConnection();
	}

	public Set<Song> getSongs() throws Exception {
		Set<Song> ret = new HashSet<Song>();
		Connection conn = getConnection();
		try {
			ResultSet rs = conn.prepareStatement("select * from songs").executeQuery();
			while (rs.next()) {
				Song s = new Song();
				ret.add(s);
			}
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public Set<Singer> getSingers() throws Exception {
		Set<Singer> ret = new HashSet<Singer>();
		Connection conn = getConnection();
		try {
			ResultSet rs = conn.prepareStatement("select * from singers").executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				String cat = rs.getString("category");
				Singer s = new Singer(name, cat);
				ret.add(s);
			}
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private Connection getConnection() throws Exception {
		String url = "jdbc:sqlite:";
		File dbf = new File(getDbName());
		if (!dbf.exists())
			throw new Exception("Data base not found: " + getDbName());
		String pa = url + dbf.getPath();
		return DriverManager.getConnection(pa);
	}

	private void initConnection() throws Exception {
		initSongs();
		initSingers();
	}

	private void initSingers() throws Exception {
		Connection conn = getConnection();
		try {
			conn.prepareStatement(
					"CREATE TABLE IF NOT EXISTS SINGERS (id integer primary key, name string, category string);")
					.execute();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	private void initSongs() throws Exception {
		Connection conn = getConnection();
		try {
			conn.prepareStatement(
					"CREATE TABLE IF NOT EXISTS SONGS (id integer primary key, name string, positions string, director string, listeners string);")
					.execute();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	private String getDbName() {
		return dbName;
	}

}