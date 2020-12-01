package com.terminalstuff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class QuickHTTPRequest {
	public QuickHTTPRequest() {
		
	}
	
	public String makeRequest(String url) {
		String rtn = "";
		URL obj;
    	HttpURLConnection con = null;
    	int responseCode = 0;
		try {
			obj = new URL(url);
    		con = (HttpURLConnection) obj.openConnection();
    		con.setRequestMethod("GET");
    		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
    		responseCode = con.getResponseCode();
		} catch (MalformedURLException e2) {
			e2.printStackTrace(System.out);
		} catch (ProtocolException e) {
			e.printStackTrace(System.out);
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		StringBuffer response = null;
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = null;
			try {
			in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
				in.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			rtn = response.toString();
			
		} else {
			rtn = "request failed";
		}
		return rtn;
	}
}
