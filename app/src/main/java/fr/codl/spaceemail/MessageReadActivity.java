package fr.codl.spaceemail;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class MessageReadActivity extends Activity {
	private Email m;
	private SQLiteDatabase db;
	private Boolean profanity_filter;
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
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		profanity_filter = sharedPref.getBoolean("profanity_filter", true);
		
		TextView sender = (TextView)findViewById(R.id.sender);
		SpannableString from = new SpannableString(String.format("from %s", m.from()));
		
		from.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 5, from.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		sender.setText(from);

		TextView subject = (TextView)findViewById(R.id.subject);
		subject.setText(m.subject());
		setTitle(m.subject());
		
		String maybe_body = m.body();
		
		if(maybe_body != null){
			if(profanity_filter)
				maybe_body = ProfanityFilter.filter(maybe_body);
			TextView body = (TextView)findViewById(R.id.body);
			body.setText(Html.fromHtml(maybe_body));
		}
		else new GetBodyTask().execute();
	}
	
	private class GetBodyTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... _){
			return m.fetchBody();
		}
		
		@Override
		protected void onPostExecute(String result){
			Log.v("GetBodyTask", String.format("Body: %s", result));
			
			if(profanity_filter)
				result = ProfanityFilter.filter(result);
			
			TextView body = (TextView)findViewById(R.id.body);
			body.setText(Html.fromHtml(result));
			m.save(db);
		
		}
		
	}
}
