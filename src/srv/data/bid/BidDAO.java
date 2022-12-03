package srv.data.bid;

import java.util.Arrays;

public class BidDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String bidderId;
    private String auctionId;
    private long value;


    public BidDAO() {
    }
    public BidDAO( Bid b) {
        this(b.getId(), b.getBidderId(), b.getAuctionId(), b.getValue());
    }

    public BidDAO(String id, String bidderId, String auctionId, long value) {
        super();
        this.id = id;
        this.bidderId = bidderId;
        this.auctionId = auctionId;
        this.value = value;
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

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }



    @Override
    public String toString() {
        return "BidDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", bidderId=" + bidderId + ", auctionId=" + auctionId
                + ", value=" + value + "]";
    }

}
