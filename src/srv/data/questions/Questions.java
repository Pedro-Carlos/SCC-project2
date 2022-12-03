package srv.data.questions;

import java.util.Arrays;

public class Questions {
    private String id;
    private String userId;
    private String auctionId;
    private String message;


    public Questions(){
        
    }

    public Questions(String id, String userId, String auctionId, String message) {
        super();
        this.id = id;
        this.userId = userId;
        this.auctionId = auctionId;
        this.message = message;

    }
    public Questions(QuestionsDAO questionsDAO) {
        super();
        this.id = questionsDAO.getId();
        this.userId = questionsDAO.getUserId();
        this.auctionId = questionsDAO.getAuctionId();
        this.message = questionsDAO.getMessage();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public String toString() {
        return "Questions [id=" + id + ", bidderId=" + userId + ", auctionId=" + auctionId
                + ", message=" + message + "]";
    }

}
