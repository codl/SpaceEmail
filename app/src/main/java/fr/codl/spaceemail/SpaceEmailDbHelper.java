package fr.codl.spaceemail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SpaceEmailDbHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "SpaceEmail.db";
    
    public SpaceEmailDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE emails ("+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "id INTEGER,"+
                "sender TEXT,"+
                "subject TEXT,"+
                "color TEXT DEFAULT '',"+
                "body TEXT DEFAULT NULL,"+
                "date TEXT DEFAULT NULL)");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion == 1){
		    db.execSQL("ALTER TABLE emails ADD COLUMN date TEXT DEFAULT NULL");
        }
	}
}
