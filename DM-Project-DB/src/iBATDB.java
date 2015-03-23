import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class iBATDB {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/DM_Project";

	static final String USER = "root";
	static final String PASS = "pallavi";
	
	public static Connection conn = null;
	public static Statement stmt = null;
	
	public void SetUp() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void TearDown() {
		try {
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	
	public List<String> Query (String table, String input) {
		List<String> ret = new ArrayList<String>();
		String sql = new String();
		
		try {
			if (table.equals("Trajectory")) {
				sql = "SELECT Cells FROM Trajectory WHERE TrajectoryId = \"" + input + "\"";
			} else {
				sql = "SELECT Trajectories FROM Grid WHERE CellNo = \"" + input + "\"";
			}
			
			ResultSet resultSet;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				String row = resultSet.getString(1);
				row = row.trim();
				ret.add(row);
			}
			resultSet.close();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ret;
	}
}