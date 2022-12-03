package srv.data.bid;

import java.util.Arrays;

public class Bid {
    private String id;
    private String bidderId;
    private String auctionId;
    private long value;

    public Bid(){
        
    }

    public Bid(String id, String bidderId, String auctionId, long value) {
        super();
        this.id = id;
        this.bidderId = bidderId;
        this.auctionId = auctionId;
        this.value = value;
    }
    public Bid(BidDAO bidDAO) {
        super();
        this.id = bidDAO.getId();
        this.bidderId = bidDAO.getBidderId();
        this.auctionId = bidDAO.getAuctionId();
        this.value = bidDAO.getValue();
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
        return "Bid [id=" + id + ", bidderId=" + bidderId + ", auctionId=" + auctionId
                + ", value=" + value + "]";
    }

}
