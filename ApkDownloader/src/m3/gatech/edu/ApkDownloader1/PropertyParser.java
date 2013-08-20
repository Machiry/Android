package m3.gatech.edu.ApkDownloader1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;


public class PropertyParser {

	public static String baseDownloadDir = "";
	public static String timeFrame = "week";
	public static String applicationSelectionGroup = "popular";
	public static int noOfTopApplications = 20;
	public static String appAwareToken = "";
	public static String curruserName = "";
	public static String currpassword = "";
	public static String androidId = "";
	public static boolean useoneTimeLogin = false;
	public static ArrayList<String> registeredAccounts = new ArrayList<String>();
	public static String srcJsonFilePath = null;
	public static ArrayList<String> passwords = new ArrayList<String>();
	

	public static boolean parsePropertiesFile(String propertiesFile) {
		boolean retVal = true;
		try {
			InputStream is = new FileInputStream(propertiesFile);
			Properties prop = new Properties();
			prop.load(is);
			
			baseDownloadDir = prop.getProperty("download_dir");
			if (baseDownloadDir == null) {
				displayPropertiesUsage();
				return false;
			}

			appAwareToken = prop.getProperty("app_ware_token");
			if (appAwareToken == null) {
				displayPropertiesUsage();
				return false;
			}
			
			String temp = prop.getProperty("usernames");
			if (temp == null) {
				
				displayPropertiesUsage();
				return false;
			} else{
				registeredAccounts = new ArrayList<String>(Arrays.asList(temp.split(",")));
			}
			
			temp = prop.getProperty("passwords");
			if (temp == null) {
				displayPropertiesUsage();
				return false;
			} else{
				passwords = new ArrayList<String>(Arrays.asList(temp.split(",")));
				if(registeredAccounts.size() != passwords.size()){
				displayPropertiesUsage();
				return false;
				}
			}
			
			androidId = prop.getProperty("android_id");
			if (androidId == null) {
				displayPropertiesUsage();
				return false;
			}
			
			if(prop.getProperty("timeframe") != null){
				timeFrame = prop.getProperty("timeframe");
			}
			
			if(prop.getProperty("app_popularity") != null){
				applicationSelectionGroup = prop.getProperty("app_popularity");
			}
			
			temp = prop.getProperty("one_time_login");
			if (temp != null && temp.equals("1")) {
				useoneTimeLogin = true;
			}
			
			temp = prop.getProperty("no_of_apps");
			if (temp != null) {
				noOfTopApplications = Integer.parseInt(temp);
			}
			
			srcJsonFilePath = prop.getProperty("appJsonFile");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	private static void displayPropertiesUsage() {
		System.out.println("Seems like Some Required Properties are missing. ask m4kh1ry, he sucks.");
	
	}

}
