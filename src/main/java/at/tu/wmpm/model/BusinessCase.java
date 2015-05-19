package at.tu.wmpm.model;


import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.*;

import org.mongojack.ObjectId;


public abstract class BusinessCase {

    @ObjectId
    @JsonProperty("_id")
    private String id;
    private String subject;
    private String body;
    private String parentId;
    private String sender;
    private boolean isNew;
    private ArrayList<Comment> comments;

    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @JsonProperty("parent_id")
    public String getParentId() {
        return parentId;
    }

    @JsonProperty("parent_id")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }
    
    public ArrayList<Comment> getComments() {
		return comments;
	}

	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}
	
	public void addComment(Comment comment){
		this.comments.add(comment);
	}

	@Override
    public String toString() {
        return "ID: "+id+", Parent ID: "+parentId+", Sender: "+sender+", Subject: "+subject+", Body: "+body;
    }
}
