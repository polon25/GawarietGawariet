package gawarietGawariet.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase{
	
	String tableName=null;
	
	public DataBase(String tableName) throws SQLException{
		this.tableName=tableName;
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
		if (tableName.equals("users")){	//Utworzenie tablicy userów
			statement.execute("CREATE TABLE IF NOT EXISTS users(" +
					"Login CHAR, " +
					"Password CHAR," +
					"Address CHAR," +
					"Port INT," +
					");");
		}
		else if(!tableName.equals(null)){	//Utworzenie tablic znajomych i wiadomości
			String statementString="CREATE TABLE IF NOT EXISTS "+tableName+"(PAL CHAR);";
			statement.execute(statementString);
		}
	}

}
