package srv.layers;

import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import utils.AzureKeys;


import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.Arrays;

import org.bson.Document;

public class MongoDBLayer {

    public static final String USERS = "users";
    public static final String AUCTIONS = "auctions";
    public static final String BIDS = "bids";
    public static final String QUESTIONS = "questions";
    public static final String GARBAGE = "garbage";

    //public static final String URL = AzureKeys.getInstance().getMongoDbUrl();
    public static final String DB_NAME ="admin";

    public MongoDBLayer(CodecRegistry codecRegis) {
        this.codecRegistry = codecRegis;
    }

    public static synchronized MongoDBLayer getInstance() {
        if (instance != null)
            return instance;

        CodecProvider codecProv = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry codecRegis = fromRegistries(getDefaultCodecRegistry(), fromProviders(codecProv));

        instance = new MongoDBLayer(codecRegis);

        return instance;
    }
    private static MongoDBLayer instance;
    MongoDatabase database;
    private CodecRegistry codecRegistry;

    private synchronized void init() {
        if (database != null)
            return;
        String mongoDB_URI = System.getenv("MONGODB");
        MongoClient mongoClient = MongoClients.create(mongoDB_URI);
        database = mongoClient.getDatabase(DB_NAME).withCodecRegistry(codecRegistry);

    }

    public <T> boolean delById(String id, String container) {
        init();
        DeleteResult res = database.getCollection(container).deleteOne(eq("_id", id));
        /*
        Returns:A document containing:
           A boolean acknowledged as true if the operation ran with write concern or false if write concern was disabled
           deletedCount containing the number of deleted documents
           need deleted count to check if the operation was successful
         */

        return res.getDeletedCount() == 1;
    }

    public <T> void put(String container, T object) {
        init();
        database.getCollection(container).insertOne(new Document()
        .append("_id", "a")
        .append("title", "Ski Bloopers")
        .append("genres", Arrays.asList("Documentary", "Comedy")));
    }

    public <T> T getById(String id, String containerType) {
        init();
        return (T) database.getCollection(containerType).find(eq("_id", id)).first();
    }

    public <T> void replace(T object, String id, String container) {
        if(delById(id, container)){
            database.getCollection(container).insertOne((Document) object);
        }
    }

    public <T> FindIterable<T> getList(String containerType) {
        init();
        return ((MongoCollection<T>) database.getCollection(containerType)).find();
    }

    public <T> FindIterable<T> getElementsFromObject(String parentId, String containerType) {
        init();
        return ((MongoCollection<T>) database.getCollection(containerType)).find(eq(getElementsFromObject(containerType), parentId));
    }

    private String getElementsFromObject(String containerType) {
        if (AUCTIONS.equals(containerType))
            return "ownerId";
        return "auctionId";
    }
    /**
     * USED IN FUNCTIONS
     * NO NEED TO CONVERT IF WE DO NOT DO THE OPTIONAL FOR THE FUNCTIONS
     */

    /*
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

     */
}
