package srv.layers;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;

@SuppressWarnings("ALL")
public class MongoDBLayer {

    public static final String USERS = "users";
    public static final String AUCTIONS = "auctions";
    public static final String BIDS = "bids";
    public static final String QUESTIONS = "questions";
    public static final String GARBAGE = "garbage";

    //public static final String URL = AzureKeys.getInstance().getMongoDbUrl();
    public static final String DB_NAME ="data";
    private static final String CONNECTION_STRING ="mongodb://mongodb:27017";


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

        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        database = mongoClient.getDatabase(DB_NAME).withCodecRegistry(codecRegistry);

    }

    public <T> boolean delById(String id, String container, Class<T> objectClass) {
        init();
        DeleteResult res = database.getCollection(container, objectClass).deleteOne(eq("_id", id));
        /*
        Returns:A document containing:
           A boolean acknowledged as true if the operation ran with write concern or false if write concern was disabled
           deletedCount containing the number of deleted documents
           need deleted count to check if the operation was successful
         */

        return res.getDeletedCount() == 1;
    }

    public <T> String put(String container, T object) {
        init();
        InsertOneResult res = ((MongoCollection<T>) database.getCollection(container, object.getClass())).insertOne(object);
        return res.getInsertedId().toString();

    }

    public <T> T getById(String id, String containerType, Class<T> objectClass) {
        init();
        return database.getCollection(containerType, objectClass).find(eq("_id", id)).first();
    }

    public <T> void replace(T object, String id, String container) {
        if(delById(id, container, object.getClass())){
            ((MongoCollection<T>) database.getCollection(container, object.getClass())).insertOne(object);
        }
    }

    public <T> FindIterable<T> getList(String containerType, Class<T> objectClass) {
        init();
        return database.getCollection(containerType, objectClass).find();
    }

    public <T> FindIterable<T> getElementsFromObject(String parentId, String containerType, Class<T> objectClass) {
        init();
        return database.getCollection(containerType, objectClass).find(eq(getElementsFromObject(containerType), parentId));
    }

    private String getElementsFromObject(String containerType) {
        if (AUCTIONS.equals(containerType))
            return "_ownerId";
        return "_auctionId";
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
