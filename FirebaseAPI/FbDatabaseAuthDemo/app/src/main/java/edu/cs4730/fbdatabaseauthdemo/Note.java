package edu.cs4730.fbdatabaseauthdemo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Note {

    private String title;
    private String note;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public Note() {

    }

    public Note(String title, String note) {
        this.title = title;
        this.note = note;
    }

    public String getTitle() {
        return title == null ? "No Title" : title;
    }

    public String getNote() {
        return note == null ? "No Note" : note;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("note", note);
        result.put("title", title);

        return result;
    }


}
