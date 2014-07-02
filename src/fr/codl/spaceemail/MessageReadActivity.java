package fr.codl.spaceemail;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class MessageReadActivity extends Activity {
	private Email m;
	private SQLiteDatabase db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_read);
		
		Intent intent = getIntent();
		if(intent == null)
			finish();
		Uri data = intent.getData();
		int message_id = Integer.parseInt(data.getSchemeSpecificPart());
		
		Log.v("MessageReadActivity onCreate", String.format("data: %s, id: %d", data, message_id));
				
		SpaceEmailDbHelper helper = new SpaceEmailDbHelper(getApplicationContext());
		db = helper.getReadableDatabase();
		
		m = Email.get(db, message_id);
		
		TextView sender = (TextView)findViewById(R.id.sender);
		sender.setText(String.format("from %s", m.from()));

		TextView subject = (TextView)findViewById(R.id.subject);
		subject.setText(m.subject());

		new GetBodyTask().execute();
	}
	
	private class GetBodyTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... _){
			return m.fetchBody();
		}
		
		@Override
		protected void onPostExecute(String result){
			Log.v("GetBodyTask", String.format("Body: %s", result));
			WebView webview = (WebView)findViewById(R.id.body);
			webview.loadData(result, "text/html", "UTF-8");
			m.save(db);
		
		}
		
	}
}
