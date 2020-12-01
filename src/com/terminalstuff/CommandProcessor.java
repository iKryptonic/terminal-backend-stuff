package com.terminalstuff;



public class CommandProcessor {

	public static void parseCmd(String s, AuthSocket a) {
	    String[] cmd = s.split(" ");
	    try {
	        switch (cmd[0]) {
	            case "exit":
	                System.out.println("Shutting down...");
	                break;
	
	            case "database":
	                switch (cmd[1]) {
	                    case "new":
	                        System.out.println("creating db: " + cmd[2]);
	                        break;
	
	                    default:
	                        System.out.println("Unknown parameter");
	                        break;
	                }
	                break;
	            case "broadcast":
	            	a.broadcast(cmd[1]);
	            	break;

	            case "shutdownallservers":
	            	System.out.println("its done boss.");
	            	break;
	
	            default:
	                System.out.println("Unknown command");
	                break;
	        }
	    } catch (Exception ArrayIndexOutOfBoundsException) {
	        System.out.println("Too few arguments passed");
	    }
	}
}
