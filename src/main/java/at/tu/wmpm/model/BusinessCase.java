package at.tu.wmpm.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import org.mongodb.morphia.annotations.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BusinessCase {
	
	@Id
    @ObjectId
    @JsonProperty("_id")
    private String id;
    private String subject;
    private String body;
    @JsonProperty("parent_id")
    private String parentId;
    private String sender;
    @JsonIgnore
    private boolean isNew;
    private ArrayList<Comment> comments;

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

    public String getParentId() {
        return parentId;
    }

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
