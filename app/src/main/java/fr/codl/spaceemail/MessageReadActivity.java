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
import android.widget.TextView;

public class MessageReadActivity extends Activity{
    private Email m;
    private SQLiteDatabase db;
    private Boolean profanity_filter;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_read);

        Intent intent = getIntent();
        if(intent == null)
            finish();
        Uri data = intent.getData();
        int message_id = Integer.parseInt(data.getSchemeSpecificPart());

        SpaceEmailDbHelper helper = new SpaceEmailDbHelper(getApplicationContext());
        db = helper.getReadableDatabase();

        m = Email.get(db, message_id);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        profanity_filter = sharedPref.getBoolean("profanity_filter", true);

        update();

        if(m.body() == null || m.date() == null){
            new FetchTask().execute();
        }
    }

    private void update(){


        TextView sender = findViewById(R.id.sender);
        SpannableString sender_string = new SpannableString(String.format("from %s", m.from()));

        sender_string.setSpan(
                new StyleSpan(android.graphics.Typeface.BOLD),
                5,
                sender_string.length(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        sender.setText(sender_string);

        String subject_string = m.subject();
        TextView subject = findViewById(R.id.subject);
        subject.setText(subject_string);

        String body_string = m.body();
        if(body_string != null){
            TextView body = findViewById(R.id.body);
            if(profanity_filter){
                body.setText(Html.fromHtml(ProfanityFilter.filter(m.body())));
            } else{
                body.setText(Html.fromHtml(m.body()));
            }
        }

        String date_string = m.date();
        if(date_string != null){
            TextView date = findViewById(R.id.date);
            date.setText(m.date());
        }
    }

    private class FetchTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... x){
            m.fetch();
            m.save(db);
            return null;
        }

        @Override
        protected void onPostExecute(Void x){
            update();
        }

    }
}
