package srv.data.questions;

import java.util.Arrays;

public class QuestionsDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String userId;
    private String auctionId;
    private String message;


    public QuestionsDAO() {
    }
    public QuestionsDAO( Questions q) {
        this(q.getId(), q.getUserId(), q.getAuctionId(), q.getMessage());
    }

    public QuestionsDAO(String id, String userId, String auctionId, String message) {
        super();
        this.id = id;
        this.userId = userId;
        this.auctionId = auctionId;
        this.message = message;
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
        return "QuestionsDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", bidderId=" + userId + ", auctionId=" + auctionId
                + ", message=" + message + "]";
    }

}

