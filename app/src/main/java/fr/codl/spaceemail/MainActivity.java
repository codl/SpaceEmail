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
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener{
    private SQLiteDatabase db;
    private SwipeRefreshLayout swipe;
    private CursorAdapter list_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipe = (SwipeRefreshLayout) findViewById(R.id.email_list_fragment);
        SpaceEmailDbHelper helper = new SpaceEmailDbHelper(getApplicationContext());
        db = helper.getWritableDatabase();

        Cursor cursor = getCursor();
        list_adapter = new EmailCursorAdapter(this, cursor, 0);
        ListView listView = findViewById(R.id.emails);
        listView.setAdapter(list_adapter);
        listView.setOnItemClickListener(this);
    }

    private Cursor getCursor(){
        return db.query(
                "emails",
                new String[]{"sender", "subject", "id", "_id"},
                null, null, null, null, "_id DESC", null);
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View view, int position, long id){
        // Get the cursor, positioned to the corresponding row in the result set
        Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        // Get the state's capital from this row in the database.
        int remote_id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

        Intent i = new Intent(this, MessageReadActivity.class);
        i.setData(Uri.parse(String.format("spaceemail:%d", remote_id)));
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public void refresh(){
        if(swipe != null)
            swipe.setRefreshing(true);
        new GetMailTask().execute();
    }

    private class GetMailTask extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... x){
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo == null || !networkInfo.isConnected())
                return null;

            return SpaceEmailAPI.newMail();
        }

        @Override
        protected void onPostExecute(String result){
            if(swipe != null)
                swipe.setRefreshing(false);
            if(result == null || result.length() == 0){
                Context context = getApplicationContext();
                CharSequence text = "Couldn't fetch email.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else{
                Email email = Email.fromListing(result);
                email.create(db);
                Cursor cursor = getCursor();
                list_adapter.changeCursor(cursor);
                list_adapter.notifyDataSetChanged();
                Log.i("EmailList", "Refreshed.");
            }
        }

    }

    private class EmailCursorAdapter extends CursorAdapter{
        public EmailCursorAdapter(Context context, Cursor cursor, int flags){
            super(context, cursor, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent){
            Activity activity = (Activity) context;
            LayoutInflater li = activity.getLayoutInflater();
            View view = li.inflate(R.layout.list_email, parent, false);
            bindView(view, context, cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor){
            TextView sender = view.findViewById(R.id.sender);
            TextView subject = view.findViewById(R.id.subject);
            int id = cursor.getInt(2);
            String sender_string = cursor.getString(0);
            String subject_string = cursor.getString(1);
            Email eml = new Email(id, sender_string, subject_string);
            sender.setText(eml.sender());
            subject.setText(eml.subject());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
        } else if(id == R.id.action_refresh){
            refresh();
        }
        return super.onOptionsItemSelected(item);
    }
}
