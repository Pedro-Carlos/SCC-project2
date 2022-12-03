package srv.data.user;


/**
 * Represents a User, as returned to the clients
 */
public class User {
	private String id;
	private String name;
	private String nickName;
	private String pwd;
	private String photoId;

	public User(){
		
	}

	public User(String id, String name, String nickName, String pwd, String photoId) {
		super();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.nickName = nickName;

	}
	public User(UserDAO user) {
		super();
		this.id = user.getId();
		this.name = user.getName();
		this.pwd = user.getPwd();
		this.photoId = user.getPwd();
		this.nickName = user.getNickName();

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

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name +", nickName=" + nickName +", pwd=" + pwd + ", photoId=" + photoId + "]";
	}

}
