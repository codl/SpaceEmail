package fr.codl.spaceemail;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Email {
	int id;
	String from;
	String subject;
	String body;

	public Email(int id, String from, String subject){
		this.id = id;
		this.from = from;
		this.subject = subject;
	}

	public Email(int id, String from, String subject, String body){
		this.id = id;
		this.from = from;
		this.subject = subject;
		this.body = body;
	}
	
	public static Email get(SQLiteDatabase db, int id){
		Cursor c = db.query("emails", new String[]{"sender", "subject", "body"}, "id = ?", new String[]{String.valueOf(id)}, null, null, null, null);
		c.moveToFirst();
		String from = c.getString(0);
		String subject = c.getString(1);
		String body = c.getString(2);
		Email e = new Email(id, from, subject, body);
		return e;
	}
	
	public int id(){ return id; }
	public String from(){ return from; }
	public String subject(){ return subject; }
	
	public String fetchBody(){
		if(body != null)
			return body;
		
		body = SpaceEmailAPI.getBody(id);
		return body;
	}
	
	public static Email fromHtml(String html){
		Document doc = Jsoup.parse(html);
		Elements divs = doc.getElementsByTag("div");
		if(divs.size() < 1)
			return null;
		
		Element div = divs.get(0);
		int id = Integer.parseInt(div.attr("data-id"));
		
		String from = "";
		String subject = "";
		
		Elements left = div.getElementsByClass("left");
		if(left.size() >= 1)
			from = left.get(0).text();
					
		Elements right = div.getElementsByClass("right");
		if(right.size() >= 1)
			subject = right.get(0).text();
		
		Log.v("Email.fromHtml", String.format("id %d, from %s, subj %s", id, from, subject));
		
		return new Email(id, from, subject);
	}
	
	public Boolean create(SQLiteDatabase db){
		ContentValues row = new ContentValues(3);
		row.put("id", id);
		row.put("sender", from);
		row.put("subject", subject);
		row.put("body", body);
		return db.insert("emails", null, row) != -1;
	}
	
	public Boolean save(SQLiteDatabase db){
		ContentValues row = new ContentValues(3);
		row.put("id", id);
		row.put("sender", from);
		row.put("subject", subject);
		row.put("body", body);
		return db.update("emails", row, "id = ?", new String[]{String.format("%d", id)}) != -1;
	}
}
