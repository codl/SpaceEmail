package fr.codl.spaceemail;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import android.util.Log;

public class SpaceEmailAPI {
	private static String get(String endpoint, String data){
		try{
			URL url = new URL(endpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
	        conn.setReadTimeout(10000);
	        conn.setConnectTimeout(15000);
	        conn.setRequestMethod("POST");
	        conn.setDoOutput(true);
	        conn.setRequestProperty("Content-Encoding", "application/x-www-form-urlencoded; charset=UTF-8");
	        byte[] rawData = data.getBytes("UTF-8");
	        conn.setFixedLengthStreamingMode(rawData.length);
	        conn.connect();
	        OutputStream os = conn.getOutputStream();
	        os.write(rawData);
	        InputStream is = conn.getInputStream();
	        Reader reader = new InputStreamReader(is, "UTF-8");        
	        char[] buffer = new char[10000];
	        reader.read(buffer);
	        String result = new String(buffer).trim(); // the trim is necessary to remove all the trailing NUL characters
	        Log.v("SpaceEmailAPI", String.format("Received: %s", result));
	        return result;
		} catch (Exception e) {
			return null;
		}
	}
	public static String getMail(){
		return get("http://space.galaxybuster.net/lib/get.php", "");
	}
	public static String getBody(int id) {
		try {
			JSONArray arr = (JSONArray) new JSONTokener(get("http://space.galaxybuster.net/lib/view.php", String.format("id=%d", id))).nextValue();
			String html = arr.getString(0);
			Document doc = Jsoup.parse(html);
			Element msgBody = doc.getElementById("msgBody");
			return msgBody.html();
		} catch (JSONException e) {
			Log.e("SpaceEmailAPI", "couldn't decode incoming JSON");
			return null;
		}
	}
}
