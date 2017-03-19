package cn.ruc.gyf.db;

import java.util.Date;

public class Floor {
	
	private int id;
	private long user_id;
	private String user_name;
	private Date date;
	private String content;
	private String 	theme;
	private long theme_id;
	private String ba_name;
	
	
	@Override
	public String toString() {
		return "Floor [id=" + id + ", user_id=" + user_id + ", user_name=" + user_name + ", date=" + date + ", content="
				+ content + ", theme=" + theme + ", theme_id=" + theme_id + ", ba_name=" + ba_name + "]";
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getUser_id() {
		return user_id;
	}
	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public long getTheme_id() {
		return theme_id;
	}
	public void setTheme_id(long theme_id) {
		this.theme_id = theme_id;
	}
	public String getBa_name() {
		return ba_name;
	}
	public void setBa_name(String ba_name) {
		this.ba_name = ba_name;
	}
	

}
