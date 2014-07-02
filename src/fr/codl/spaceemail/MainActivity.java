package fr.codl.spaceemail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener {
	private SQLiteDatabase db;
	private SwipeRefreshLayout swipe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		swipe = (SwipeRefreshLayout) findViewById(R.id.email_list_fragment);
		SpaceEmailDbHelper helper = new SpaceEmailDbHelper(getApplicationContext());
		db = helper.getWritableDatabase();
		
		loadListView();
		ListView listView = (ListView) findViewById(R.id.emails);
		listView.setOnItemClickListener(this);
	}
	
	private void loadListView(){
		String[] from = new String[]{ "sender", 		"subject" 	 };
		int[] to = new int[]		{ R.id.sender_name, R.id.subject };
		Cursor cursor = db.query("emails", new String[]{"sender", "subject", "id", "_id"}, null, null, null, null, "_id DESC", null);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_email, cursor, from, to, 0);
		ListView listView = (ListView) findViewById(R.id.emails);
		listView.setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
	   // Get the cursor, positioned to the corresponding row in the result set
	   Cursor cursor = (Cursor) listView.getItemAtPosition(position);
	 
	   // Get the state's capital from this row in the database.
	   int remote_id =  cursor.getInt(cursor.getColumnIndexOrThrow("id"));

	   Intent i = new Intent(this, MessageReadActivity.class);
	   i.setData(Uri.parse(String.format("spaceemail:%d", remote_id)));
	   startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public void refresh() {
        if(swipe != null)
        	swipe.setRefreshing(true);
		new GetMailTask().execute();
	}
	
	private class GetMailTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... _) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo == null || !networkInfo.isConnected())
				return null;
		    
		    return SpaceEmailAPI.getMail();
		}
		
		@Override
		protected void onPostExecute(String result) {
	        if(swipe != null)
	        	swipe.setRefreshing(false);
			if(result == null || result.length() == 0){
				Context context = getApplicationContext();
				CharSequence text = "Couldn't fetch email.";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
			else {
				Email.fromHtml(result).create(db);
				loadListView();
				Log.i("EmailList", "Refreshed.");
			}
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		int id = item.getItemId();
		
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
		}
		else if (id == R.id.action_refresh) {
			refresh();
		}
		return super.onOptionsItemSelected(item);
	}
}
