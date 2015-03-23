import java.util.Scanner;
import java.io.FileReader;
import java.sql.*;
import java.util.Random;
import java.lang.Math;
public class updateDB {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "pallavi";

	static int abs(int input) {
		if (input < 0)
			return -1 * input;
		else
			return input;
	}

	static int randRoundoff(float a) {
		Random random = new Random();
		if (random.nextInt() % 2 == 1) {
			return (int) Math.floor(a);
		} else {
			return (int) Math.ceil(a);
		}
	}

	static void updateDBs(Connection conn, int traj, int x, int y) {
		String sql = null;
		Statement stmt = null;
		ResultSet rs;
		String newVal = null;
		String gridKey = null;
		try {
			stmt = conn.createStatement();
			
			// Insert into the TRAJECTORY table
			sql = "SELECT id FROM TRAJECTORY WHERE id=" + traj;
			rs = stmt.executeQuery(sql);
			if(!rs.next()){
				//insert the record
				sql = "INSERT INTO TRAJECTORY VALUES ("+traj+",'"+x+" "+y+"')";
				stmt.executeUpdate(sql);
			}else{
				//update the record
				newVal = rs.getString("grids");
				newVal = newVal + " "+x +" "+ y;
				sql = "UPDATE TRAJECTORY SET grids='"+newVal+"' WHERE id="+traj;
				stmt.executeUpdate(sql);
			}
			
			//Insert into the GRID table
			gridKey = "'"+x+" "+y+"'";
			sql = "SELECT id FROM GRID WHERE id="+gridKey;
			rs = stmt.executeQuery(sql);
			if(!rs.next()){
				//insert the record
				sql = "INSERT INTO GRID VALUES ("+gridKey+","+traj+")";
				stmt.executeUpdate(sql);
			}else{
				//update the record
				newVal = rs.getString("trajectoryIds");
				newVal = newVal + " " + traj;
				sql = "UPDATE GRID SET trajectoryIds='"+newVal+"' WHERE id="+gridKey;
				stmt.executeUpdate(sql);
			}
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
	}

	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;
		int traj1, pt1x, pt1y;
		int traj2, pt2x, pt2y;
		int ptx, pty;
		int i, j;

		try {
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			System.out.println("Creating database...");
			stmt = conn.createStatement();

			String sql = "CREATE DATABASE DM_PROJ";
			stmt.executeUpdate(sql);
			System.out.println("Database DM_PROJ created successfully...");

			// create two tables; one corresponding to TRAJECTORY data and
			// one for GRID data

			sql = "CREATE TABLE TRAJECTORY(id INTEGER not NULL,"
					+ "grids VARCHAR(10000), PRIMARY KEY(id))"; // reconsider
																// 10000
			// id is the trajectory ID
			// grids is space separated values of x,y coordinates of
			// grids in the trajectory
			stmt.executeUpdate(sql);
			System.out.println("Table TRAJECTORY created successfully");

			sql = "CREATE TABLE GRID(id VARCHAR(300) not NULL, trajectoryIds VARCHAR(10000))";
			stmt.executeUpdate(sql);
			System.out.println("Table GRID created successfully");

			// Read the file line by line and update the two tables
			Scanner fp = new Scanner(new FileReader("/host/Users/Pallavi/Desktop/Acads/" +
													"sem-6/Data-Mining/Project/data/partition_cells/cells.txt"));

			traj1 = fp.nextInt();
			pt1x = fp.nextInt();
			pt1y = fp.nextInt();
			pt2x=pt2y=traj2=-1;

			while (fp.hasNext()) {
				// update traj1 details into the database
				updateDBs(conn,traj1,pt1x,pt1y);
				/* Will write functions for that later */

				traj2 = fp.nextInt();
				pt2x = fp.nextInt();
				pt2y = fp.nextInt();

				// check if both points are of same trajectory
				if (traj1 == traj2) {
					// need to augment the path
					if (abs(pt1x - pt2x) > 2) {
						i = pt1x - pt1y;
						i = i / 3; // step size
						ptx = pt1x;
						pty = pt1y;

						for (j = 0; j < 3; j++) {// augmenting atmost 3 points
							ptx = ptx + i;
							pty = pty
									+ i
									* randRoundoff((pt1y - pt2y)
											/ (pt1x - pt2y));
							// insert point into the tables
							updateDBs(conn,traj1,ptx,pty);
						}
					} else {
						if (abs(pt1y - pt2y) > 2) {
							i = pt1y - pt2y;
							i = i / 3;
							ptx = pt1x;
							pty = pt1y;

							for (j = 0; j < 3; j++) {
								pty += i;
								ptx += i
										* randRoundoff((pt1x - pt2x)
												/ (pt1y - pt2y));
								// insert point into the database
								updateDBs(conn,traj1,ptx,pty);
							}
						}// end if(abs(pt1y-pt2y)>2)
					}// end if-else(abs(pt1x-pt2x)>2)
				} else {
					// if not just assign pt2 to pt1
					traj1 = traj2;
					pt1x = pt2x;
					pt1y = pt2y;
				}// end if-else(traj1 == traj2)
			}// end while(fp.hasNext())
				// enter pt2 into the database
			if(pt2x != -1){
				updateDBs(conn,traj2,pt2x,pt2y);
			}
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
	}// end main
}
