package srv.data.auction;

import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents an Auction, as returned to the clients
 */
public class Auction {
	private String id;
	private String description;
	private String ownerId;
	private String imageId;
	private long price;
	private String winnerBidId;
	private String status;
	
	@JsonDeserialize(as = Date.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date endDateTime;


	public Auction(){

	}

	public Auction(String id, String description, String ownerId, String imageId, long price, String winnerBidId, String status, Date endDateTime) {
		super();
		this.id = id;
		this.description = description;
		this.ownerId = ownerId;
		this.imageId = imageId;
		this.price = price;
		this.winnerBidId = winnerBidId;
		this.status = status;
		this.endDateTime = endDateTime;
	}
	public Auction(AuctionDAO auctionDAO) {
		super();
		this.id = auctionDAO.getId();
		this.description = auctionDAO.getDescription();
		this.ownerId = auctionDAO.getOwnerId();
		this.imageId = auctionDAO.getImageId();
		this.price = auctionDAO.getPrice();
		this.winnerBidId = auctionDAO.getWinnerBidId();
		this.status = auctionDAO.getStatus();
		this.endDateTime = auctionDAO.getEndDateTime();

	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}
	
	public String getWinnerBidId() {
		return winnerBidId;
	}

	public void setWinnerBidId(String winnerBidId) {
		this.winnerBidId = winnerBidId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
	public Date getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}



	@Override
	public String toString() {
		return "Auction [id=" + id + ", description=" + description + ", ownerId=" + ownerId + ", imageId=" + imageId +
		"price=" + price + ", winnerBidId=" + winnerBidId + ", status=" + status + ", endDateTime=" + endDateTime + "]";
	}

}
