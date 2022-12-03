package srv.api.service.rest.authentication;

import srv.data.user.UserDAO;

public class Session {

    private String uid;
    private UserDAO user;

    public Session(){

    }
    
    public Session(String uid, UserDAO user){
        this.uid = uid;
        this.user = user;
    }

    public String getUid() {
		return uid;
	}

    public void setUid(String uid) {
		this.uid = uid;
	}

    public UserDAO getUser() {
		return user;
	}

    public void setUser(UserDAO user) {
		this.user = user;
	}

    
}
