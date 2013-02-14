package org.my.facebookalgorithm.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.my.facebookalgorithm.utilities.DownloadHandler;
import org.my.facebookalgorithm.utilities.DownloadHandler.InvalidUrlException;
import org.my.facebookalgorithm.utilities.DownloadHandler.NetworkConnectionException;

public class NumberOfMutualFriends implements FaceBookAPI {
	@Override
	public String callAPI(String token, String id) {
		try{
			   //logger.log(LogService.LOG_INFO,"Inside call");
				URL url = new URL("https://graph.facebook.com/fql?q=SELECT+uid,name,mutual_friend_count+FROM+user+WHERE+uid+IN(SELECT+uid1+FROM+friend+WHERE+uid2=me())&"+token);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");	
				String val = DownloadHandler.getResponse(connection);
				//logger.log(LogService.LOG_INFO,"value ="+val);
				return val;
			} catch (IOException e1) {
				//logger.log(LogService.LOG_INFO, e1.getMessage());
			} catch (InvalidUrlException e1) {
				//logger.log(LogService.LOG_INFO, e1.getMessage());
			} catch (NetworkConnectionException e1) {
				//logger.log(LogService.LOG_INFO, e1.getMessage());
			} 
			return "No data";
	}	

}
