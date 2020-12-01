package com.terminalstuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DatabaseManager {
	
	Connection SQLCon = null;
	SQLStuff newSQL = null;
	
	public DatabaseManager() {
		try {
			newSQL = new SQLStuff();
			SQLCon =  newSQL.getConnection();
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	} // default constructor
	
	public void addChatLog(String serverkey, String username, String userid, String time, String message) throws SQLException {
		
		if(SQLCon.isClosed()) {
			SQLCon = newSQL.getConnection();
		}
		
		String queryString = "INSERT INTO `terminal_logging`.`chat_logs` (`serverkey`, `username`, `userid`, `time`, `message`) VALUES ('"+serverkey+"', '"+username+"', '"+userid+"', '"+time+"', ?)";
		PreparedStatement pstmt = newSQL.conn.prepareStatement( queryString );
		pstmt.setString( 1, message); 
		
		newSQL.executeQuery("terminal_logging", pstmt);
	}
	
}
