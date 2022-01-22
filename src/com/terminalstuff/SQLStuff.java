package com.terminalstuff;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Statement;

public class SQLStuff {


	String userName = "";
	String password = "";
	String dbms = "";
	String serverName = "";
	String portNumber = "";
	String dbName = "";
	
	Connection conn = null;
	
	public Connection getConnection() throws SQLException {
		/*
		Properties prop = new Properties();
		String fileName = "/home/ikrypto/deploy/terminal.config";
		InputStream is = null;
		
			
		try {
		    is = new FileInputStream(fileName);
		} catch (FileNotFoundException ex) {
			System.out.println("Exception:\n" + ex);
		}
		try {
		    prop.load(is);
		} catch (IOException ex) {
			System.out.println("Exception:\n" + ex);
		}
		*/
		
		userName = System.getenv("userName"); //prop.getProperty("userName"); 
		password = System.getenv("password"); //prop.getProperty("password"); 
		dbms = System.getenv("dbms"); //prop.getProperty("dbms"); 
		serverName = System.getenv("serverName"); //prop.getProperty("serverName"); 
		portNumber = System.getenv("portNumber"); //prop.getProperty("portNumber"); 
		dbName = System.getenv("dbName"); //prop.getProperty("dbName"); 

	    Properties connectionProps = new Properties();
	    connectionProps.put("user", this.userName);
	    connectionProps.put("password", this.password);

	    if (this.dbms.equals("mysql")) {
	        conn = DriverManager.getConnection(
	                   "jdbc:" + this.dbms + "://" +
	                   this.serverName +
	                   ":" + this.portNumber + "/",
	                   connectionProps);
	    } else if (this.dbms.equals("derby")) {
	        conn = DriverManager.getConnection(
	                   "jdbc:" + this.dbms + ":" +
	                   this.dbName +
	                   ";create=true",
	                   connectionProps);
	    }
	    System.out.println(MainClass.CColor.GREEN_BOLD + "Connected to terminal logging database" + MainClass.CColor.RESET);
	    return conn;
	}
	
	public void executeQuery(String dbName, PreparedStatement query)
	    throws SQLException {
		
	    try {
	        query.executeUpdate();
	    } catch (SQLException e ) {
	        System.out.println(e);
	    } finally {
	        if (query != null) { query.close(); }
	    }
	}
	
	public ResultSet fetchTable(String dbName, String query)
	    throws SQLException {

	    Statement stmt = null;
	    ResultSet rs = null;
	    try {
	        stmt = conn.createStatement();
	        rs = stmt.executeQuery(query);
	        /*while (rs.next()) {
	            String coffeeName = rs.getString("COF_NAME");
	            int supplierID = rs.getInt("SUP_ID");
	            float price = rs.getFloat("PRICE");
	            int sales = rs.getInt("SALES");
	            int total = rs.getInt("TOTAL");
	            System.out.println(coffeeName + "\t" + supplierID +
	                               "\t" + price + "\t" + sales +
	                               "\t" + total);
	        }*/
	    } catch (SQLException e ) {
	    	e.printStackTrace(System.out);
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	return rs;
	}
}
