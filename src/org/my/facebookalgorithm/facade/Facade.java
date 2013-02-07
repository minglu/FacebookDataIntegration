package org.my.facebookalgorithm.facade;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.osgi.service.log.LogService;

public class Facade {
	private CSVWriter csv;
	private LogService logger;
	
	public Facade(LogService logger)
	{
	    this.logger= logger;
	}
	public String getAccessToken()
	{
		try {
			URI url = new URI("https://www.facebook.com/dialog/oauth?client_id=283202715139589&redirect_uri=https://morning-fjord-1741.herokuapp.com/token.php&scope=manage_friendlists&response_type=token");		
			Desktop.getDesktop().browse(url);			
		} catch (URISyntaxException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (IOException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		}
		
		String input =  JOptionPane.showInputDialog("Enter Access Token:");
         return input;
	}
	
	//writes the  CSV file
	public void writeCSVFile(List<FriendsPair> list) throws IOException	{
		
		final JFileChooser fc = new JFileChooser();
		int userSelection = fc.showSaveDialog(null);
		File fileToSave = null;
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    fileToSave = fc.getSelectedFile();
		    System.out.println("Save as file: " + fileToSave.getAbsolutePath());
		}
		
		CSVWriter writer = new CSVWriter(fileToSave.getAbsolutePath());
		String[] entries ={ "name1", "name2"};
		writer.writeNext(entries);
		for(FriendsPair pair:list)
		{
			String[] nameList= {pair.getName1(),pair.getName2()};
			//this.logger.log(LogService.LOG_INFO, "name1 ="+pair.getName1()+"name2 ="+pair.getName2());
			writer.writeNext(nameList);
		}
		writer.close();		
	}
	
	public String getMyName(String token) throws JSONException{
		FaceBookAPI mydetails = new MyDetailsAPI();
		String data = mydetails.callAPI(token, "");
		JSONObject obj = new JSONObject(new JSONTokener(data));
		return obj.getString("name");
	}
}
