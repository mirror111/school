package cn.ruc.gyf.db;

public class WeiboInfo {
	private int id;
	private String uid;
	private String wbid;
	private int time;
	private String oritime;
	private String content;
	private String repostUid;
	private String repostWbid;
	private int repostTime;
	private String orirepostTime;
	private String repostContent;
	private int reposts_count;
	private int comments_count;
	private int attitudes_count;
	
	@Override
	public String toString() {
		return "WeiboInfo [id=" + id + ", uid=" + uid + ", wbid=" + wbid + ", time=" + time + ", oritime=" + oritime
				+ ", content=" + content + ", repostUid=" + repostUid + ", repostWbid=" + repostWbid + ", repostTime="
				+ repostTime + ", orirepostTime=" + orirepostTime + ", repostContent=" + repostContent
				+ ", reposts_count=" + reposts_count + ", comments_count=" + comments_count + ", attitudes_count="
				+ attitudes_count + "]";
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getWbid() {
		return wbid;
	}
	public void setWbid(String wbid) {
		this.wbid = wbid;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getOritime() {
		return oritime;
	}
	public void setOritime(String oritime) {
		this.oritime = oritime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getRepostUid() {
		return repostUid;
	}
	public void setRepostUid(String repostUid) {
		this.repostUid = repostUid;
	}
	public String getRepostWbid() {
		return repostWbid;
	}
	public void setRepostWbid(String repostWbid) {
		this.repostWbid = repostWbid;
	}
	public int getRepostTime() {
		return repostTime;
	}
	public void setRepostTime(int repostTime) {
		this.repostTime = repostTime;
	}
	public String getOrirepostTime() {
		return orirepostTime;
	}
	public void setOrirepostTime(String orirepostTime) {
		this.orirepostTime = orirepostTime;
	}
	public String getRepostContent() {
		return repostContent;
	}
	public void setRepostContent(String repostContent) {
		this.repostContent = repostContent;
	}
	public int getReposts_count() {
		return reposts_count;
	}
	public void setReposts_count(int reposts_count) {
		this.reposts_count = reposts_count;
	}
	public int getComments_count() {
		return comments_count;
	}
	public void setComments_count(int comments_count) {
		this.comments_count = comments_count;
	}
	public int getAttitudes_count() {
		return attitudes_count;
	}
	public void setAttitudes_count(int attitudes_count) {
		this.attitudes_count = attitudes_count;
	}

	

}
