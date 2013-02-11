package org.my.facebookalgorithm;

import java.awt.Desktop;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmExecutionException;
import org.cishell.framework.data.BasicData;
import org.cishell.framework.data.Data;
import org.cishell.framework.data.DataProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.my.facebookalgorithm.api.FaceBookAPI;
import org.my.facebookalgorithm.api.FriendsCommonEventAPI;
import org.my.facebookalgorithm.api.FriendsEventAPI;
import org.my.facebookalgorithm.api.FriendsInAppAPI;
import org.my.facebookalgorithm.api.FriendsWithFriendsAPI;
import org.my.facebookalgorithm.api.MutualFriendsAPI;
import org.my.facebookalgorithm.api.MyFriendsAPI;
import org.my.facebookalgorithm.facade.Facade;
import org.my.facebookalgorithm.utilities.DownloadHandler;
import org.my.facebookalgorithm.utilities.DownloadHandler.InvalidUrlException;
import org.my.facebookalgorithm.utilities.DownloadHandler.NetworkConnectionException;
import org.osgi.service.log.LogService;

public class facebookAlgo implements Algorithm {
	private Data[] data;
	private Dictionary parameters;
	private CIShellContext ciShellContext;
	private LogService logger;
	private List<FriendsPair> pairList;
	private Facade facade;

	public facebookAlgo(Data[] data, Dictionary parameters,
			CIShellContext ciShellContext) {
		this.data = data;
		this.parameters = parameters;
		this.ciShellContext = ciShellContext;
		this.logger = (LogService) ciShellContext.getService(LogService.class
				.getName());

		pairList = new ArrayList<FriendsPair>();
		facade = new Facade(this.logger);
	}

	public Data[] execute() throws AlgorithmExecutionException {
		this.logger.log(LogService.LOG_INFO, "Call to Facebook API");
		this.logger
				.log(LogService.LOG_WARNING,
						"The use of the Facebook API is governed by following policies:");
		this.logger
				.log(LogService.LOG_WARNING,
						"This is a Facebook application that helps "
								+ "user export data out of Facebook for reuse in Visualization or any possible method of "
								+ "digital story telling. Data is exported in csv format. ");
		this.logger
				.log(LogService.LOG_WARNING,
						"According to Facebook's Statement of Rights and Responsibility. "
								+ "You own all of the content and information you post on Facebook, and you can control how it is shared through your privacy and application settings.");
		this.logger
				.log(LogService.LOG_INFO, "Please refer the following link:");
		this.logger.log(LogService.LOG_WARNING,
				"https://developers.facebook.com/policy");

//		String loginStatus = facade.checkLogin();
//
//		if (loginStatus.equals("0")) {

			int confirmMsg = JOptionPane
					.showConfirmDialog(
							null,
							"Please login in your web browser and copy the access token returned to allow Sci2 to access your Friends infomation",
							"Are you ready to login in Web browser?",
							JOptionPane.YES_NO_OPTION);

			if (confirmMsg == JOptionPane.YES_OPTION) {
				getFriendsNetwork();
			}
//		} else {
//			getFriendsNetwork();
//		}
		return null;
	}

	public void getFriendsNetwork() {
		this.logger.log(LogService.LOG_INFO, "Opening Facebook login page");

		String token = facade.getAccessToken();
		this.logger.log(LogService.LOG_INFO, "Access Token: " + token);
		if (token != null) {
			String data = "access_token=" + token;
			String myName = "";
			String myId = "";
			try {
				myName = facade.getMyName(data);
				myId = facade.getMyId(data);
			} catch (JSONException e1) {
				logger.log(LogService.LOG_INFO, e1.getMessage());
			}

			// call friends API and store it in hash map
			HashMap<Long, String> idToName = new HashMap<Long, String>();
			idToName.put(Long.parseLong(myId), myName);

			JSONObject obj;
			try {

				FaceBookAPI fb = new MyFriendsAPI();
				JSONObject friendsObj = new JSONObject(fb.callAPI(data, ""));

				JSONArray friendsArray = friendsObj.getJSONArray("data");
				int len = friendsArray.length();
				for (int i = 0; i < len; i++) {
					JSONObject currentResult = friendsArray.getJSONObject(i);
					String friendOnename = currentResult.getString("name");
					Long id = currentResult.getLong("id");
					idToName.put(id, friendOnename);
					pairList.add(new FriendsPair(myName, friendOnename));

				}

				// call the MutualAPI
				fb = new MutualFriendsAPI();
				obj = new JSONObject(fb.callAPI(data, ""));
				JSONArray jsonArray = obj.getJSONArray("data");
				
				//call FriendsEventAPI
				fb = new FriendsEventAPI();
				obj = new JSONObject(fb.callAPI(data, ""));
				JSONArray jsonArrayEvent = obj.getJSONArray("data");
				HashMap<Long, String> eventIdToName = new HashMap<Long, String>();
				for (int i=0;i<jsonArrayEvent.length();i++){
					JSONObject currentResult = jsonArrayEvent.getJSONObject(i);
					String eventName = currentResult.getString("name");
					Long id = currentResult.getLong("eid");
					eventIdToName.put(id, eventName);
					this.logger.log(LogService.LOG_INFO, "eventName ="+eventName+" id ="+ id);
				}
				
				//call FriendsCommonEventAPI
				fb = new FriendsCommonEventAPI();
				obj = new JSONObject(fb.callAPI(data, ""));
				JSONArray jsonArrayCommonEvent = obj.getJSONArray("data");
				HashMap<Long, ArrayList<Long>> commonEvents = new HashMap<Long, ArrayList<Long>>();
				for (int i=0;i<jsonArrayCommonEvent.length();i++){
					JSONObject currentResult = jsonArrayCommonEvent.getJSONObject(i);
					Long uid = currentResult.getLong("uid");
					Long eid = currentResult.getLong("eid");
					if(commonEvents.get(eid)==null){
						ArrayList<Long> uidList = new ArrayList<Long>();
						uidList.add(uid);
						commonEvents.put(eid, uidList);
					}else{
						ArrayList<Long> uidList = commonEvents.get(eid);
						uidList.add(uid);
						commonEvents.put(eid, uidList);
					}
					this.logger.log(LogService.LOG_INFO, "uid ="+uid+" eid ="+ eid);
				}
				
				//
				len = jsonArray.length();
				for (int i = 0; i < len; i++) {
					JSONObject currentResult = jsonArray.getJSONObject(i);
					Long id1 = currentResult.getLong("uid1");
					Long id2 = currentResult.getLong("uid2");
					FriendsPair fp = new FriendsPair(idToName.get(id1),
							idToName.get(id2));
					pairList.add(fp);
				}
				
				for(FriendsPair fp: pairList){
					Iterator<Entry<Long, ArrayList<Long>>> it = commonEvents.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry pairs = (Map.Entry) it.next();
						ArrayList<Long> uid = (ArrayList<Long>) pairs.getValue();
						if(uid.contains(fp.getName1())&&uid.contains(fp.getName2())){
							fp.setCommonEvent(eventIdToName.get(pairs.getKey()));
						}else{
							fp.setCommonEvent("");
						}
					}
				}
				
				facade.writeCSVFile(pairList);
			}

			catch (JSONException e) {
				logger.log(LogService.LOG_INFO, e.getMessage());
			}

			catch (IOException e) {
				logger.log(LogService.LOG_INFO, e.getMessage());
			}
		}
	}

	void getFriendsOfFriendsNames() {
		String token = facade.getAccessToken();
		this.logger.log(LogService.LOG_INFO, "Access Token: " + token);
		String data = "access_token=" + token;
		String myName = "";
		try {
			myName = facade.getMyName(data);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONObject obj;
		try {
			FaceBookAPI fb = new FriendsInAppAPI(logger);
			obj = new JSONObject(fb.callAPI(data, ""));

			JSONArray jsonArray = obj.getJSONArray("data");
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject currentResult = jsonArray.getJSONObject(i);
				String friendOnename = currentResult.getString("name");
				Long id = currentResult.getLong("uid");
				FriendsPair fp = new FriendsPair(myName, friendOnename);
				pairList.add(fp);

				this.logger.log(LogService.LOG_INFO, "Name = " + friendOnename);
				this.logger.log(LogService.LOG_INFO, "id = " + id);
				// code for friends of friends
				FaceBookAPI ff = new FriendsWithFriendsAPI();
				String string = ff.callAPI(data, id.toString());
				if (string.equals("No data") || string.isEmpty())
					continue;
				JSONObject ffobj = new JSONObject(string);
				JSONArray friensArray = ffobj.getJSONArray("data");

				for (int j = 0; j < friensArray.length(); j++) {
					JSONObject innerResult = friensArray.getJSONObject(j);
					String friendTwoName = innerResult.getString("name");
					// this.logger.log(LogService.LOG_INFO,
					// "friends friendName = "+friendOnename);
					this.logger.log(LogService.LOG_INFO,
							"friends friendName = " + friendTwoName);

					pairList.add(new FriendsPair(friendOnename, friendTwoName));
				}
			}
			// to get my friends
			FaceBookAPI myFriends = new MyFriendsAPI();
			obj = new JSONObject(myFriends.callAPI(data, ""));
			JSONArray friendsArray = obj.getJSONArray("data");
			len = friendsArray.length();
			for (int j = 0; j < len; j++) {
				JSONObject innerResult = friendsArray.getJSONObject(j);
				String friendTwoName = innerResult.getString("name");
				// this.logger.log(LogService.LOG_INFO,
				// "friends friendName = "+friendOnename);
				this.logger.log(LogService.LOG_INFO, "friends friendName = "
						+ friendTwoName);

				pairList.add(new FriendsPair(myName, friendTwoName));
			}
		} catch (JSONException e) {
			logger.log(LogService.LOG_INFO, e.getMessage());
		}
		try {
			facade.writeCSVFile(pairList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(LogService.LOG_INFO, e.getMessage());
		}

	}

}
