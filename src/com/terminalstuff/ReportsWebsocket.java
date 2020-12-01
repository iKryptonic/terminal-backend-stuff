package com.terminalstuff;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;

public class ReportsWebsocket {

    Configuration config;
    final SocketIOServer server;
	public ReportsWebsocket(int port) {
		config = new Configuration();
	    config.setHostname("localhost");
	    config.setPort(port);
		server = new SocketIOServer(config);
	}

	public void addListeners() {
        server.addEventListener("chatevent", Report.class, new DataListener<Report>() {
            @Override
            public void onData(SocketIOClient client, Report data, AckRequest ackRequest) {
                System.out.println("DATA RECV: "+data);
                if(ackRequest.isAckRequested()) {
                	ackRequest.sendAckData("yep");
                }
            }
        });
	}
        

   public void init() {
	   	this.addListeners();
	    server.start();
	}
}
