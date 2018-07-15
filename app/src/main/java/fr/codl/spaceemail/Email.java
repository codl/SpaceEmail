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
	private int id;
	private String sender;
	private String subject;
	private String body;
	private String date;

	public Email(int id, String sender, String subject){
		this.id = id;
		this.sender = sender;
		this.subject = subject;
	}

	public Email(int id, String sender, String subject, String body, String date){
		this.id = id;
		this.sender = sender;
		this.subject = subject;
        this.body = body;
        this.date = date;
	}
	
	public static Email get(SQLiteDatabase db, int id){
		Cursor c = db.query("emails", new String[]{"sender", "subject", "body", "date"}, "id = ?", new String[]{String.valueOf(id)}, null, null, null, null);
		c.moveToFirst();
		String sender = c.getString(0);
		String subject = c.getString(1);
        String body = c.getString(2);
        String date = c.getString(3);
		Email e = new Email(id, sender, subject, body, date);
		return e;
	}
	
	public int id(){ return id; }
	public String sender(){ if(sender.trim().length() > 0) return sender; else return "No sender…"; }
	public String subject(){ if(subject.trim().length() > 0) return subject; else return "No subject…"; }
    public String body(){ return body; }
    public String date(){ return date; }
	
	public void fetch(){
		String html = SpaceEmailAPI.getEmail(id);
		Document doc = Jsoup.parse(html);
        subject = doc.getElementById("msgSubject").text();
        sender = doc.getElementById("msgSender").text();
        body = doc.getElementById("msgBody").html();
        date = doc.getElementById("msgDate").text();
    }
	
	public static Email fromListing(String html){
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
		ContentValues row = new ContentValues(5);
		row.put("id", id);
		row.put("sender", sender);
		row.put("subject", subject);
        row.put("body", body);
        row.put("date", date);
		return db.insert("emails", null, row) != -1;
	}
	
	public Boolean save(SQLiteDatabase db){
		ContentValues row = new ContentValues(5);
		row.put("id", id);
		row.put("sender", sender);
		row.put("subject", subject);
        row.put("body", body);
        row.put("date", date);
		return db.update("emails", row, "id = ?", new String[]{String.format("%d", id)}) != -1;
	}
}
