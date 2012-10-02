package m3.gatech.edu.ApkDownloader1;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.GetAssetResponse.InstallAsset;

public class Program {

	private static void printUsage() {
		System.out
				.println("Please provide properties file(absoulte path) as second argument\n");
		System.out.println("Properties File Format:");
	}

	public static int noOfAppsDownloaded = 0;

	public static void main(String[] args) {
		if (args != null && args.length != 1) {
			printUsage();
			return;
		}
		if (!PropertyParser.parsePropertiesFile(args[0])) {
			System.out
					.println("Problem occured while trying to do automation setup");
			return;
		}

		MarketSession session = null;

		if (PropertyParser.useoneTimeLogin) {
			session = new MarketSession(true);
			System.out.println("Login...");

			session.login(PropertyParser.userName, PropertyParser.password,
					PropertyParser.androidId);

			System.out.println("Login done");
		}
		// First 1. get the top apps. better be it be a list
		HashMap<String, ArrayList<String>> appsToDownload = getAppsToDownload();

		// 2. Download all the apps one by one
		for (String cat : appsToDownload.keySet()) {
			String targetDir = PropertyParser.baseDownloadDir + "/" + cat;
			(new File(targetDir)).mkdirs();
			for (String app : appsToDownload.get(cat)) {
				downloadApp(session, app, targetDir);
			}

		}

		System.out.println("Total No Of Apps Downloaded:" + noOfAppsDownloaded);
	}

	private static HashMap<String, ArrayList<String>> getAppsToDownload() {
		HashMap<String, ArrayList<String>> targetApps = new HashMap<String, ArrayList<String>>();
		String urlTemplate = "http://dev.appaware.com/1/top.json?d=%s&t=%s&c=%d&cc=worldwide&num=%d&page=1&client_token=%s";
		try {
			// For each category
			for (int i = 1; i <= 31; i++) {
				ArrayList<String> apps = new ArrayList<String>();
				String urlToFetch = String.format(urlTemplate,
						PropertyParser.timeFrame,
						PropertyParser.applicationSelectionGroup, i,
						PropertyParser.noOfTopApplications,
						PropertyParser.appAwareToken);
				// System.out.println(urlToFetch);
				String jsonOutput = getHTML(urlToFetch);
				// System.out.println(jsonOutput);
				JSONArray results = (JSONArray) ((JSONObject) (new JSONParser())
						.parse(jsonOutput)).get("results");
				for (int j = 0; j < results.size(); j++) {
					apps.add((String) ((JSONObject) results.get(j))
							.get("package_name"));
				}
				targetApps.put(Integer.toString(i), apps);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return targetApps;

	}

	private static String getHTML(String urlToRead) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static boolean downloadApp(MarketSession session, String appID,
			String appDir) {
		boolean retVal = false;
		try {
			String fileToSave = appDir + "/" + appID + ".apk";

			if (!(new File(fileToSave)).exists()) {

				if (!PropertyParser.useoneTimeLogin) {
					session = new MarketSession(true);
					// System.out.println("Login...");

					session.login(PropertyParser.userName,
							PropertyParser.password, PropertyParser.androidId);
				}

				// System.out.println("Login done");

				InstallAsset ia = session.queryGetAssetRequest(appID)
						.getInstallAsset(0);
				String cookieName = ia.getDownloadAuthCookieName();
				String cookieValue = ia.getDownloadAuthCookieValue();
				URL url = new URL(ia.getBlobUrl());

				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("User-Agent",
						"Android-Market/2 (sapphire PLAT-RC33); gzip");
				conn.setRequestProperty("Cookie", cookieName + "="
						+ cookieValue);

				InputStream inputstream = (InputStream) conn.getInputStream();

				System.out.println("Downloading " + fileToSave);
				noOfAppsDownloaded++;
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(fileToSave));
				byte buf[] = new byte[1024];
				int k = 0;
				while ((k = inputstream.read(buf)) != -1) {
					stream.write(buf, 0, k);
				}
				inputstream.close();
				stream.close();
				retVal = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			retVal = false;
		}
		return retVal;
	}

}