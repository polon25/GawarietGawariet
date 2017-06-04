package gawarietGawariet.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase{

	public DataBase() throws SQLException{
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:h2:users", "sa", "");
			create(conn);
		} finally {
			if (conn!= null){
				conn.close();
			}
		}
	}
	
	private void create(Connection conn) throws SQLException{
		Statement statement = conn.createStatement(); 
		statement.execute("CREATE TABLE IF NOT EXISTS users(" +
				"Login CHAR, " +
				"Password CHAR," +
				"Address CHAR," +
				"Port INT," +
				");");
	}

}
