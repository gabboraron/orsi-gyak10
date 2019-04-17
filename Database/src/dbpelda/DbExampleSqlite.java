package dbpelda;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbExampleSqlite {
	public static void main(String[] args) throws Exception {
		String user = "abc";
		String password = "";
		String url = "jdbc:sqlite:sqlite_test.db";

		loadDbDriver("org.sqlite.JDBC");

		try (
			Connection conn = DriverManager.getConnection(url, user, password);
		) {
		    createTables(conn);
		    insertData(conn);
		    queryData(conn);
		    queryData2(conn);
		}
	}

	private static void loadDbDriver(String driverClassName) throws Exception {
		try {
			Class.forName(driverClassName);
		} catch (Exception e) {
			System.err.println("ERROR: failed to load JDBC driver.");
			throw e;
		}
	}

	private static void insertData(Connection conn) throws SQLException {
		try (
			PreparedStatement prep = conn.prepareStatement("insert into people values (?, ?);");
		) {
			addPerson(prep, "Gandhi", 1869);
			addPerson(prep, "Turing", 1912);
			addPerson(prep, "Wittgenstein", 1889);
			addPerson(prep, "Frege", 1848);

			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
		}
	}

	private static void queryData(Connection conn) throws SQLException {
		try (
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from people;");
		) {
			while (rs.next()) {
			  String name = rs.getString("name");
			  int birthyear = rs.getInt("birthyear");
			  System.out.printf("name = %s, birthyear = %d%n", name, birthyear);
			}
		}
	}

	private static void queryData2(Connection conn) throws SQLException {
		try (
			PreparedStatement p = conn.prepareStatement("select * from people p where p.birthyear < ? and p.name <> ?;");
			PreparedStatement p2 = conn.prepareStatement("select count(*) from people p where p.birthyear < ? and p.name <> ?;");
		) {
			p.setInt(1, 1900);
			p.setString(2, "Wittgenstein");
			p2.setInt(1, 1900);
			p2.setString(2, "Wittgenstein");

			try (
				ResultSet rs = p.executeQuery();
				ResultSet rs2 = p2.executeQuery();
			) {
				rs2.next();
				int resultCount = rs2.getInt(1);
				System.out.println("count = " + resultCount);

				while (rs.next()) {
				  String name = rs.getString("name");
				  int birthyear = rs.getInt("birthyear");
				  System.out.printf("name = %s, birthyear = %d%n", name, birthyear);
				}
			}
		}
	}

	private static void addPerson(PreparedStatement prep, String name, int birthyear) throws SQLException {
		prep.setString(1, name);
		prep.setInt(2, birthyear);
		prep.addBatch();
	}

	private static void createTables(Connection conn) throws SQLException {
		try (
			Statement stat = conn.createStatement();
		) {
			stat.executeUpdate("drop table if exists people;");
			stat.executeUpdate("create table people (name varchar(80), birthyear int);");
		}
	}

}
