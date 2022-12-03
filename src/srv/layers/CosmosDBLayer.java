package srv.layers;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import srv.data.auction.Auction;
import srv.data.auction.AuctionDAO;
import srv.data.bid.Bid;
import srv.data.bid.BidDAO;
import srv.data.questions.Questions;
import srv.data.questions.QuestionsDAO;
import srv.data.user.User;
import srv.data.user.UserDAO;
import utils.AzureKeys;

import java.util.Iterator;

public class CosmosDBLayer {

    public static final String USERS = "users";
    public static final String AUCTIONS = "auctions";
    public static final String BIDS = "bids";
    public static final String QUESTIONS = "questions";
    public static final String GARBAGE = "garbage";

    public static final String URL = AzureKeys.getInstance().getCosmosDbUrl();
    public static final String KEY = AzureKeys.getInstance().getCosmosDbKey();
    public static final String DB_NAME = AzureKeys.getInstance().getCosmosDB();



    private static CosmosDBLayer instance;

    public static synchronized CosmosDBLayer getInstance() {
        if (instance != null)
            return instance;

        CosmosClient client = new CosmosClientBuilder()
                .endpoint(URL)
                .key(KEY)
                .gatewayMode()                 //.gatewayMode()// replace by .directMode() for better performance
                .consistencyLevel(ConsistencyLevel.SESSION)
                .connectionSharingAcrossClientsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .buildClient();
        instance = new CosmosDBLayer(client);
        return instance;

    }

    private CosmosClient client;
    private CosmosDatabase db;
    private CosmosContainer container;

    public CosmosDBLayer(CosmosClient client) {
        this.client = client;
    }

    private synchronized void init() {
        if (db != null)
            return;
        db = client.getDatabase(DB_NAME);

    }

    /*User methods*/
    public CosmosItemResponse<Object> delById(String id, String container) {
        init();
        PartitionKey key = new PartitionKey(id);
        return db.getContainer(container).deleteItem(id, key, new CosmosItemRequestOptions());
    }

    public <T> CosmosItemResponse<T> put(String container, T object) {
        init();
        return db.getContainer(container).createItem(object);
    }

    public <T> CosmosItemResponse<T> replace(T object, String id, String container) {
        init();
        PartitionKey key = new PartitionKey(id);
        CosmosContainer cosmosContainer = db.getContainer(container);
        cosmosContainer.deleteItem(id, key, new CosmosItemRequestOptions());
        return cosmosContainer.createItem(object);
    }

    public <T> T getById(String id, String containerType, Class<T> objectClass) {
        init();
        CosmosPagedIterable<T> response = db.getContainer(containerType).queryItems("SELECT * FROM " + containerType + " WHERE " + containerType + ".id=\"" + id + "\"", new CosmosQueryRequestOptions(), objectClass);
        Iterator<T> it = response.iterator();
        if (it.hasNext())
            return it.next();
        return null;
    }

    public <T> CosmosPagedIterable<T> getList(String containerType, Class<T> objectClass) {
        init();
        return db.getContainer(containerType).queryItems("SELECT * FROM " + containerType, new CosmosQueryRequestOptions(), objectClass);
    }

    public <T> CosmosPagedIterable<T> getElementsFromObject(String parentId, String containerType, Class<T> objectClass) {
        init();
        return db.getContainer(containerType).queryItems("SELECT * FROM " + containerType + " WHERE " + containerType + getElementsFromObject(containerType) + parentId + "\"", new CosmosQueryRequestOptions(), objectClass);
    }

    private String getElementsFromObject(String containerType) {
        if (AUCTIONS.equals(containerType))
            return ".ownerId=\"";
        return ".auctionId=\"";
    }

    public CosmosPagedIterable<BidDAO> getBidsFromUser(String bidderId) {
        init();
        return db.getContainer(BIDS).queryItems("SELECT * FROM " + BIDS + " WHERE " + BIDS + ".bidderId=\"" + bidderId + "\"", new CosmosQueryRequestOptions(), BidDAO.class);
    }

    public CosmosPagedIterable<AuctionDAO> getClosedAuctions() {
        init();
        return db.getContainer(AUCTIONS).queryItems("SELECT * FROM " + AUCTIONS + " WHERE " + AUCTIONS + ".status=\"" + "closed" + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
    }

    public CosmosPagedIterable<AuctionDAO> getDeletedAuctions() {
        init();
        return db.getContainer(AUCTIONS).queryItems("SELECT * FROM " + AUCTIONS + " WHERE " + AUCTIONS + ".status=\"" + "deleted" + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
    }
    public void close() {
        client.close();
    }

	/*
	public CosmosPagedIterable<AuctionDAO> getAuctionsFromUser(String ownerId) {
		init();
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.ownerId=\"" + ownerId + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}



	public CosmosItemResponse<AuctionDAO> putAuction(AuctionDAO auction) {
		init();
		return auctions.createItem(auction);
		}

	public CosmosItemResponse<AuctionDAO> replaceAuction(AuctionDAO auction) {
		init();
		return auctions.replaceItem(auction, auction.getId(), null, new CosmosItemRequestOptions());
		}

	public AuctionDAO getAuctionById( String id) {
		init();
		CosmosPagedIterable<AuctionDAO> response =  auctions.queryItems("SELECT * FROM auctions WHERE auctions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
		Iterator<AuctionDAO> it = response.iterator();
		if (it.hasNext())
			return it.next();
		return null;
	}

	public CosmosPagedIterable<AuctionDAO> getAuctions() {
		init();
		return auctions.queryItems("SELECT * FROM auctions ", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}


	public CosmosItemResponse<BidDAO> putBid(BidDAO bid) {
		init();
		return bids.createItem(bid);
	}

	public CosmosPagedIterable<BidDAO> getBids() {
		init();
		return bids.queryItems("SELECT * FROM bids ", new CosmosQueryRequestOptions(),BidDAO.class);
	}

	public BidDAO getBidById(String id) {
		init();
		CosmosPagedIterable<BidDAO> response =  bids.queryItems("SELECT * FROM bids WHERE bids.id=\"" + id + "\"", new CosmosQueryRequestOptions(), BidDAO.class);
		Iterator<BidDAO> it = response.iterator();
		if (it.hasNext())
			return it.next();
		return null;
	}

	public CosmosPagedIterable<BidDAO> getBidsFromAuction(String auctionId) {
		init();
		return bids.queryItems("SELECT * FROM bids WHERE bids.auctionId=\"" + auctionId + "\"", new CosmosQueryRequestOptions(), BidDAO.class);
	}


	public CosmosItemResponse<QuestionsDAO> putQuestion(QuestionsDAO question) {
		init();
		return questions.createItem(question);
	}

	public QuestionsDAO getQuestionById(String id) {
		init();
		CosmosPagedIterable<QuestionsDAO> response =  questions.queryItems("SELECT * FROM questions WHERE questions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionsDAO.class);
		Iterator<QuestionsDAO> it = response.iterator();
		if (it.hasNext())
			return it.next();
		return null;
	}

	public CosmosPagedIterable<QuestionsDAO> getQuestionsFromAuction(String auctionId) {
		init();
		return questions.queryItems("SELECT * FROM questions WHERE questions.auctionId=\"" + auctionId + "\"", new CosmosQueryRequestOptions(), QuestionsDAO.class);
	}

	public CosmosPagedIterable<QuestionsDAO> getQuestions() {
		init();
		return bids.queryItems("SELECT * FROM questions ", new CosmosQueryRequestOptions(),QuestionsDAO.class);
	}

	 */


}
