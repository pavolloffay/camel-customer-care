package at.tu.beans;

import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.*;

public abstract class BasicBean {

	private int id;
	private String subject;
	private String body;
	private int parentId;
	private String sender;

	//TODO STILL HAS TO BE TESTED
	
	public BasicBean() {
		
	}
	
	@JsonIgnore
	public String getSubject() {
		return subject;
	}

	@JsonIgnore
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@JsonIgnore
	public String getBody() {
		return body;
	}

	@JsonIgnore
	public void setBody(String body) {
		this.body = body;
	}

	@JsonProperty("parent_id")
	public int getParentId() {
		return parentId;
	}

	@JsonProperty("parent_id")
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	@JsonIgnore
	public String getSender() {
		return sender;
	}

	@JsonIgnore
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	@ObjectId
	@JsonProperty("_id")
	public final int getId() {
		return id;
	}

	@ObjectId
	@JsonProperty("_id")
	public final void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return "ID: "+id+", Parent ID: "+parentId+", Sender: "+sender+", Subject: "+subject+", Body: "+body;
	}
}
