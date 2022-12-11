package srv.data.user;


import org.bson.Document;

import java.util.Arrays;

/**
 * Represents a User, as stored in the database
 */
public class UserDAO {
	private String _rid;
	private String _ts;
	private String id;
	private String name;
	private String nickName;
	private String pwd;
	private String photoId;

	public UserDAO() {
	}
	public UserDAO( User u) {
		this(u.getId(), u.getName(), u.getNickName(), u.getPwd(), u.getPhotoId());
	}

	public UserDAO(String id, String name,String nickName, String pwd, String photoId) {
		super();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.nickName = nickName;
	}
	public String get_rid() {
		return _rid;
	}
	public void set_rid(String _rid) {
		this._rid = _rid;
	}
	public String get_ts() {
		return _ts;
	}
	public void set_ts(String _ts) {
		this._ts = _ts;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public User toUser() {
		return new User( id, name, pwd, photoId, nickName);
	}
	@Override
	public String toString() {
		return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", name=" + name +", nickName=" + nickName + ", pwd=" + pwd
				+ ", photoId=" + photoId + "]";
	}

	public Document toDocument() {
		Document doc = new Document();
		doc.put("_ts", this._ts);
		doc.put("id", this.id);
		doc.put("name", this.name);
		doc.put("nickName", this.nickName);
		doc.put("pwd", this.pwd);
		doc.put("photoId", this.photoId);
		return doc;
	}


}
