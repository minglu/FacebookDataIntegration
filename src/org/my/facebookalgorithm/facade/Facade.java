package org.my.facebookalgorithm.facade;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.my.facebookalgorithm.FriendsPair;
import org.my.facebookalgorithm.api.FaceBookAPI;
import org.my.facebookalgorithm.api.MyDetailsAPI;
import org.my.facebookalgorithm.utilities.CSVWriter;
import org.my.facebookalgorithm.utilities.DownloadHandler;
import org.osgi.service.log.LogService;
import org.my.facebookalgorithm.utilities.DownloadHandler.InvalidUrlException;
import org.my.facebookalgorithm.utilities.DownloadHandler.NetworkConnectionException;

public class Facade {
	private CSVWriter csv;
	private LogService logger;

	public Facade(LogService logger) {
		this.logger = logger;
	}

	public String getAccessToken() {
		try {
			URI url = new URI(
					"https://www.facebook.com/dialog/oauth?client_id=283202715139589"
							+ "&redirect_uri=https://morning-fjord-1741.herokuapp.com/token.php&scope=manage_friendlists"
							+ "&response_type=token" 
							+ "&scope=email,user_about_me,user_activities,user_birthday,user_education_history," +
							"user_events,user_hometown,user_interests,user_likes,user_groups,user_location,user_religion_politics,friends_events,read_friendlists");
			Desktop.getDesktop().browse(url);
		} catch (URISyntaxException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (IOException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		}

		String input = JOptionPane.showInputDialog("Enter Access Token:");
		return input;
	}

	// check login
	public String checkLogin() {
		try {
			URL url = new URL(
					"https://morning-fjord-1741.herokuapp.com/CheckLogin.php");
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			return DownloadHandler.getResponse(connection);
		} catch (IOException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (InvalidUrlException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (NetworkConnectionException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		}

		return "0";
	}

	// writes the CSV file
	public void writeCSVFile(List<FriendsPair> list) throws IOException {

		final JFileChooser fc = new JFileChooser();
		int userSelection = fc.showSaveDialog(null);
		File fileToSave = null;
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			fileToSave = fc.getSelectedFile();
			System.out.println("Save as file: " + fileToSave.getAbsolutePath());
		}

		CSVWriter writer = new CSVWriter(fileToSave.getAbsolutePath());
		String[] entries = { "name1", "name2", "CommonEvent" };
		writer.writeNext(entries);
		for (FriendsPair pair : list) {
			String[] nameList = { pair.getName1(), pair.getName2(),
					pair.getCommonEvent() };
			// this.logger.log(LogService.LOG_INFO,
			// "name1 ="+pair.getName1()+"name2 ="+pair.getName2());
			writer.writeNext(nameList);
		}
		writer.close();
	}

	public String getMyName(String token) throws JSONException {
		FaceBookAPI mydetails = new MyDetailsAPI();
		String data = mydetails.callAPI(token, "");
		JSONObject obj = new JSONObject(new JSONTokener(data));
		return obj.getString("name");
	}

	public String getMyId(String token) throws JSONException {
		FaceBookAPI mydetails = new MyDetailsAPI();
		String data = mydetails.callAPI(token, "");
		JSONObject obj = new JSONObject(new JSONTokener(data));
		return obj.getString("id");
	}

}
