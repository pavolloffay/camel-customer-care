package at.tu.wmpm.model;

import java.util.ArrayList;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.mongodb.morphia.annotations.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BusinessCase {
	
	public BusinessCase() {
	    this._id = UUID.randomUUID().toString();
	    this.comments = new ArrayList<Comment>();
    }

	@Id
    @ObjectId
    @JsonProperty("_id")
    private String _id;
    private String subject;
    @JsonProperty("parent_id")
    private String parentId;
    private String sender;
    private String incomingDate;
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
        return _id;
    }

    public final void setId(String id) {
        this._id = id;
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
        return "ID: "+_id+", Parent ID: "+parentId+", Sender: "+sender+", Subject: "+subject;
    }

    /**
     * @return the incomingDate
     */
    public String getIncomingDate() {
        return incomingDate;
    }

    /**
     * @param incomingDate the incomingDate to set
     */
    public void setIncomingDate(String incomingDate) {
        this.incomingDate = incomingDate;
    }
    
    public boolean hasCommentWithId(String id){
        for(Comment c: this.comments){
            if(c.getId().equals(id)){
            	return true;
            }
        }
        return false;
    }
}
