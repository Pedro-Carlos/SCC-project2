package srv.api.service.rest.authentication;

public class Login {

    private String id;
	private String pwd;

    public Login(){
        
    }
    
    public Login(String id, String pwd){
        super();
        this.id = id;
        this.pwd = pwd;
    }

    public Login(LoginDAO login){
        super();
        this.id = login.getId();
        this.pwd = login.getPwd();
    }

    public String getId() {
		return id;
	}

    public void setId(String id) {
		this.id = id;
	}

    public String getPwd() {
		return pwd;
	}

    public void setPwd(String pwd) {
		this.pwd = pwd;
	}

}
