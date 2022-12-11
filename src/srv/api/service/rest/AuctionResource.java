package srv.api.service.rest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.mongodb.client.FindIterable;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedIterable;

import cache.RedisCache;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;

import srv.data.user.User;
import srv.data.user.UserDAO;
import srv.layers.PersistentVolume;
import srv.data.auction.Auction;
import srv.data.auction.AuctionDAO;
import srv.data.bid.Bid;
import srv.data.bid.BidDAO;
import srv.data.questions.Questions;
import srv.data.questions.QuestionsDAO;
import srv.api.service.rest.authentication.AuthenticationResource;
import srv.layers.MongoDBLayer;

@Path("/auction")
public class AuctionResource {
    private PersistentVolume blob = PersistentVolume.getInstance();
    private MongoDBLayer db = MongoDBLayer.getInstance();
    private RedisCache cache = RedisCache.getInstance();

    private AuthenticationResource auth = new AuthenticationResource();




/*    public static final String ABOUT_TO_CLOSE_AUCTIONS = "AboutToCloseAuctions";
    public static final String AUCTIONS_FROM = "AuctionsFrom";
    public static final String AUCTION_BIDS = "AuctionBids";
    public static final String AUCTION_QUESTIONS = "AuctionQuestions";*/

    /*
     * public static final String ABOUT_TO_CLOSE_AUCTIONS = "AboutToCloseAuctions";
     * public static final String AUCTIONS_FROM = "AuctionsFrom";
     * public static final String AUCTION_BIDS = "AuctionBids";
     * public static final String AUCTION_QUESTIONS = "AuctionQuestions";
     */

    /*
     * NOTE: make sure that it is easy to turn off the advanced features, so that
     * you can
     * execute tests that compare both – suggestion: have flags that control access
     * to the
     * additional code needed for the advanced feature. THAT'S WHY CREATED FLAG
     * cacheIsActive
     */
    private boolean cacheIsActive = true;
    private boolean autheticationIsActive = false;

    /**
     * Post a new auction.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Auction createAuction(@CookieParam("scc:session") Cookie session, Auction auction) {

        try {
            String auctionOwner = auction.getOwnerId();

            if(autheticationIsActive) auth.checkCookieUser(session, auctionOwner);

            String id = auction.getId();

            if (id == null || 
                auction.getImageId() == null || 
                auction.getPrice() <= 0 ||
                Date.from(LocalDateTime.now().toInstant(ZoneOffset.of("+00:00"))).compareTo(auction.getEndDateTime()) >= 0 || 
                !auction.getStatus().equals("open")) throw new WebApplicationException(Status.BAD_REQUEST);

            if (!blob.blobExists(auction.getImageId())) throw new WebApplicationException(Status.NOT_FOUND);

            User u = null;
            if (cacheIsActive) { // if it's in cache it was already created
                Auction a = cache.get(id, Auction.class);
                if (a != null) {
                    throw new WebApplicationException(Status.CONFLICT);
                }
                u = cache.get(auctionOwner, User.class);

            }
            UserDAO userDAO = null;
            if (u == null) {
                userDAO = db.getById(auctionOwner, MongoDBLayer.USERS, UserDAO.class);
            }
            if (u == null && userDAO == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }

            try {
                // Adds auction to database
                db.put(MongoDBLayer.AUCTIONS, new AuctionDAO(auction));

                // adds to cache in function
                if (cacheIsActive) {
                    AuctionDAO createdAuction = db.getById(id, MongoDBLayer.AUCTIONS, AuctionDAO.class);
                    if (createdAuction != null) {
                        cache.set(id, auction);
                        // update time of user in cache
                        // user with most auctions always in cache
                        cache.delete(auctionOwner, User.class);
                        cache.set(auctionOwner, u != null ? u : new User(userDAO));
                    }

                }

            } catch (CosmosException e) {
                throw new WebApplicationException(e.getStatusCode());
            }

            return auction;
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("Invalid user : " + auction.getOwnerId());
        }
    }

    /**
     * Update an existing auction.
     */
    @PUT
    @Path("/{TITLE}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateAuction(@CookieParam("scc:session") Cookie session, @PathParam("TITLE") String id,
            Auction auction) {

        try {

            if(autheticationIsActive) auth.checkCookieUser(session, auction.getOwnerId());

            try {

                if (id == null || (auction.getImageId() != null && !blob.blobExists(auction.getImageId())) ||
                        Date.from(LocalDateTime.now().toInstant(ZoneOffset.of("+00:00")))
                                .compareTo(auction.getEndDateTime()) >= 0
                        ||
                        !auction.getStatus().equals("open"))
                    throw new WebApplicationException(Status.BAD_REQUEST);

                User user = null;
                Auction a = null;
                if (cacheIsActive) {
                    user = cache.get(auction.getOwnerId(), User.class);
                    a = cache.get(id, Auction.class);
                }

                UserDAO userDAO = null;
                if (user == null) {
                    userDAO = db.getById(auction.getOwnerId(), MongoDBLayer.USERS, UserDAO.class);
                }

                AuctionDAO auctionDAO = null;
                if (a == null) {
                    auctionDAO = db.getById(id, MongoDBLayer.AUCTIONS, AuctionDAO.class);
                }

                if ((userDAO == null && user == null) || (auctionDAO == null && a == null)) {
                    throw new WebApplicationException(Status.NOT_FOUND);
                }
                // Replaces the auction in the database with a new auction with the updated info
                // in the db and cache
                // delete in cache
                if(!id.equals(auction.getId())){
                    throw new WebApplicationException(Status.FORBIDDEN);
                }
                if(a != null){
                    if (!a.getOwnerId().equals(auction.getOwnerId())) {
                        throw new WebApplicationException(Status.FORBIDDEN);
                    }
                }else{
                    if (!auctionDAO.getOwnerId().equals(auction.getOwnerId())) {
                        throw new WebApplicationException(Status.FORBIDDEN);
                    }
                }

                if (cacheIsActive) {
                    cache.delete(id, Auction.class);
                    cache.delete(auction.getOwnerId(), User.class);
                }

                db.replace(new AuctionDAO(auction), id, MongoDBLayer.AUCTIONS);
                // insert in cache in function
                if (cacheIsActive) {
                    cache.set(id, auction);
                    cache.set(auction.getOwnerId(), user != null ? user : new User(userDAO));
                    // replaces in cache list occur by azure functions

                }
            } catch (CosmosException e) {
                throw new WebApplicationException(e.getStatusCode());
            }
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("Invalid user : " + auction.getOwnerId());
        }

    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Auction> listAuctions() {
        FindIterable<AuctionDAO> auctions = db.getList(MongoDBLayer.AUCTIONS, AuctionDAO.class);
        List<Auction> l = new ArrayList<>();
        for (AuctionDAO o : auctions) {
            l.add(new Auction(o));
        }

        return l;
    }

    // TEST WITHOUT AUCTIONS IN SYSTEM
    @GET
    @Path("/AboutToClose")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Auction> getAuctionsAboutToClose() {
        List<Auction> l = new ArrayList<>();

        FindIterable<AuctionDAO> auctions = db.getList(MongoDBLayer.AUCTIONS, AuctionDAO.class);
        for (AuctionDAO auction : auctions) {
            long diff = ChronoUnit.MINUTES.between(LocalDateTime.now().toInstant(ZoneOffset.of("+00:00")),
                    auction.getEndDateTime().toInstant());

            // Check if the auction closes in 5 or less minutes
            if (diff <= 5) {
                l.add(new Auction(auction));
            }

        }
        return l;
    }

    /**
     * Post a new bid.
     */
    @POST
    @Path("/{AUCTION_ID}/bid/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Bid createBid(@CookieParam("scc:session") Cookie session, @PathParam("AUCTION_ID") String auctionId,
            Bid bid) {

        try {

            if(autheticationIsActive) auth.checkCookieUser(session, bid.getBidderId());

            String id = bid.getId();
            if (id == null)
                throw new WebApplicationException(Status.BAD_REQUEST);


            if (cacheIsActive) { //if it's in cache it was already created
                Bid b = cache.get(id, Bid.class);
                if (b != null) {
                    throw new WebApplicationException(Status.CONFLICT);
                }
            }
            if(db.getById(id, MongoDBLayer.BIDS, BidDAO.class) != null){
                throw new WebApplicationException(Status.CONFLICT);
            }

            User user = null;
            Auction a = null;
            if (cacheIsActive) {
                user = cache.get(id, User.class);
                a = cache.get(auctionId, Auction.class);
            }

            UserDAO userDAO = null;
            if (user == null) {
                userDAO = db.getById(bid.getBidderId(), MongoDBLayer.USERS, UserDAO.class);
            }

            AuctionDAO auctionDAO = null;
            if (a == null) {
                auctionDAO = db.getById(auctionId, MongoDBLayer.AUCTIONS, AuctionDAO.class);
            }

            if ((userDAO == null && user == null) || (auctionDAO == null && a == null)) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            if (a != null) {
                if (!a.getStatus().equals("open")) {
                    throw new WebApplicationException(Status.FORBIDDEN);
                }
            } else {
                if (!auctionDAO.getStatus().equals("open")) {
                    throw new WebApplicationException(Status.FORBIDDEN);
                }
            }

            try {
                db.put(MongoDBLayer.BIDS, new BidDAO(bid));
                // adds to cache in function
                if (cacheIsActive) {
                    BidDAO createdBid = db.getById(id, MongoDBLayer.BIDS, BidDAO.class);
                    if (createdBid != null) {
                        cache.set(id, bid);
                        // update time of auction in cache
                        // most bidded auctions always in cache
                        cache.delete(auctionId, Auction.class);
                        cache.set(auctionId, a != null ? a : new Auction(auctionDAO));
                        cache.delete(bid.getBidderId(), User.class);
                        cache.set(bid.getBidderId(), user != null ? user : new User(userDAO));
                    }
                }

            } catch (CosmosException e) {
                throw new WebApplicationException(e.getStatusCode());
            }

            return bid;

        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("Invalid user : " + bid.getBidderId());
        }
    }

    @GET
    @Path("/{AUCTION_ID}/listBids")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Bid> listBids(@PathParam("AUCTION_ID") String auctionId) {

        try {

            Auction a = null;
            if (cacheIsActive) {
                a = cache.get(auctionId, Auction.class);
            }

            AuctionDAO auctionDAO = null;
            if (a == null) {
                auctionDAO = db.getById(auctionId, MongoDBLayer.AUCTIONS, AuctionDAO.class);
            }
            if (a == null && auctionDAO == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }

            List<Bid> l = new ArrayList<>();

            FindIterable<BidDAO> bids = db.getElementsFromObject(auctionId, MongoDBLayer.BIDS, BidDAO.class);

            for (BidDAO o : bids) {
                l.add(new Bid(o));
            }
            return l;

        } catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }

    }

    /**
     * Post a new question.
     */
    @POST
    @Path("/{AUCTION_ID}/question/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Questions createQuestion(@CookieParam("scc:session") Cookie session,
            @PathParam("AUCTION_ID") String auctionId, Questions question) {

        try {
            if(autheticationIsActive) auth.checkCookieUser(session, question.getUserId());

            String id = question.getId();
            if (id == null || auctionId == null)
                throw new WebApplicationException(Status.BAD_REQUEST);

            if (cacheIsActive) { //if it's in cache it was already created
                Questions q = cache.get(id, Questions.class);
                if (q != null) {
                    throw new WebApplicationException(Status.CONFLICT);
                }
            }
            if(db.getById(id, MongoDBLayer.QUESTIONS, QuestionsDAO.class) != null){
                throw new WebApplicationException(Status.CONFLICT);
            }

            User user = null;
            Auction a = null;
            if (cacheIsActive) {
                user = cache.get(question.getUserId(), User.class);
                a = cache.get(auctionId, Auction.class);
            }

            UserDAO userDAO = null;
            if (user == null) {
                userDAO = db.getById(question.getUserId(), MongoDBLayer.USERS, UserDAO.class);
            }

            AuctionDAO auctionDAO = null;
            if (a == null) {
                auctionDAO = db.getById(auctionId, MongoDBLayer.AUCTIONS, AuctionDAO.class);
            }

            if ((userDAO == null && user == null) || (auctionDAO == null && a == null)) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            if (a != null) {
                if (!a.getStatus().equals("open")) {
                    throw new WebApplicationException(Status.FORBIDDEN);
                }
            } else {
                if (!auctionDAO.getStatus().equals("open")) {
                    throw new WebApplicationException(Status.FORBIDDEN);
                }
            }

            try {
                db.put(MongoDBLayer.QUESTIONS, new QuestionsDAO(question));

                if (cacheIsActive) {
                    QuestionsDAO createdQuestion = db.getById(id, MongoDBLayer.QUESTIONS, QuestionsDAO.class);
                    if (createdQuestion != null) {
                        cache.set(id, question);
                        // update time of auction in cache
                        // most questioned/replied auctions always in cache
                        cache.delete(auctionId, Auction.class);
                        cache.set(auctionId, a != null ? a : new Auction(auctionDAO));
                        cache.delete(question.getUserId(), User.class);
                        cache.set(question.getUserId(), user != null ? user : new User(userDAO));
                    }
                }
            } catch (CosmosException e) {
                throw new WebApplicationException(e.getStatusCode());
            }

            return question;

        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("Invalid user : " + question.getUserId());
        }
    }

    @GET
    @Path("/{AUCTION_ID}/listQuestions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Questions> listQuestions(@PathParam("AUCTION_ID") String auctionId) {
        try {
            Auction a = null;
            if (cacheIsActive) {
                a = cache.get(auctionId, Auction.class);
            }

            AuctionDAO auctionDAO = null;
            if (a == null) {
                auctionDAO = db.getById(auctionId, MongoDBLayer.AUCTIONS, AuctionDAO.class);
            }
            if (a == null && auctionDAO == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }

            List<Questions> l = new ArrayList<>();

            FindIterable<QuestionsDAO> questions = db.getElementsFromObject(auctionId, MongoDBLayer.QUESTIONS, QuestionsDAO.class);

            for (QuestionsDAO o : questions) {
                l.add(new Questions(o));
            }
            return l;

        } catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
    }


}
