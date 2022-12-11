package srv.api.service.rest;

import com.mongodb.MongoException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import cache.RedisCache;
import com.mongodb.MongoException;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.mongodb.client.FindIterable;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import srv.api.service.rest.authentication.AuthenticationResource;
import srv.layers.PersistentVolume;
import srv.layers.MongoDBLayer;
import srv.layers.MongoDBLayer;
import utils.Hash;
import srv.data.auction.Auction;
import srv.data.auction.AuctionDAO;
import srv.api.service.rest.authentication.Login;
import srv.api.service.rest.authentication.Session;
import srv.data.user.User;
import srv.data.user.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*import static srv.api.service.rest.AuctionResource.AUCTIONS_FROM;*/

@Path("/user")
public class UserResource {
    private PersistentVolume blob = PersistentVolume.getInstance();
    private MongoDBLayer db = MongoDBLayer.getInstance();
    private RedisCache cache = RedisCache.getInstance();

    private AuthenticationResource auth = new AuthenticationResource();

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
     * Post a new user given its username, name, password and photo.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User createUser(User user) {
        String id = user.getId();
        if (id == null || user.getPhotoId() == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        if (!blob.blobExists(user.getPhotoId()))
            throw new WebApplicationException(Status.NOT_FOUND);

        if (cacheIsActive) { // if it's in cache it was already created
            User u = cache.get(id, User.class);
            if (u != null) {
                throw new WebApplicationException(Status.CONFLICT);
            }
        }

        try {
            // Adds user to database
            user.setPwd(Hash.of(user.getPwd()));
            db.put(MongoDBLayer.USERS, new UserDAO(user));

            // adds to cache
            if (cacheIsActive) {
                UserDAO createdUser = db.getById(id, MongoDBLayer.USERS, UserDAO.class);
                if (createdUser != null)
                    cache.set(id, user);
            }
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode());
        }

        // Adds user to database
        return user;
    }

    /**
     * Delete a user given its username and password
     */
    @DELETE
    @Path("/{USER_ID}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteUser(@CookieParam("scc:session") Cookie session, @PathParam("USER_ID") String userId) {

        try {

            if(autheticationIsActive) auth.checkCookieUser(session, userId);
            UserDAO user = db.getById(userId, MongoDBLayer.USERS, UserDAO.class);
            if (user == null)
                throw new WebApplicationException(Status.NOT_FOUND);

            try {
                if (cacheIsActive) {
                    cache.delete(userId, User.class);
                    //cache.deleteSession(session.getValue());
                }

                db.delById(userId, MongoDBLayer.USERS, UserDAO.class);

                //put in garbage
                db.put(MongoDBLayer.GARBAGE, user);

            } catch (MongoException e) {
                throw new WebApplicationException(e.getCode());
            }
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("Invalid user : " + userId);
        }
    }

    /**
     * Given its password and id, update the user's information (Only the password,
     * image and name can be changed)
     */
    @PUT
    @Path("/{USER_ID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@CookieParam("scc:session") Cookie session, @PathParam("USER_ID") String userId, User user) {

        try {
            if(autheticationIsActive) auth.checkCookieUser(session, userId);
            try {

                // Replaces the user in the database with a new user with the updated info in
                // the db and cache
                // delete in cache
                User u = null;
                if (cacheIsActive) {
                    u = cache.get(userId, User.class);
                }
                UserDAO userDAO = null;
                if (u == null) {
                    userDAO = db.getById(userId, MongoDBLayer.USERS, UserDAO.class);
                }

                if (userDAO == null && u == null) {
                    throw new WebApplicationException(Status.NOT_FOUND);
                }

                if(u != null){
                    if (!user.getId().equals(u.getId())) {
                        throw new WebApplicationException(Status.FORBIDDEN);
                    }
                } else{
                    if (!user.getId().equals(userDAO.getId())) {
                        throw new WebApplicationException(Status.FORBIDDEN);
                    }
                }

                db.replace(user, userId, MongoDBLayer.USERS);

                // update cache
                if (cacheIsActive) {
                    cache.delete(userId, User.class);
                    cache.set(userId, user);
                }
            } catch (MongoException e) {
                throw new WebApplicationException(e.getCode());
            }

        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("Invalid user : " + userId);
        }

    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> listUsers() {

        FindIterable<UserDAO> users = db.getList(MongoDBLayer.USERS, UserDAO.class);

        List<User> l = new ArrayList<>();
        for (UserDAO o : users) {
            l.add(new User(o));
        }

        return l;
    }

    @GET
    @Path("/{USER_ID}/auctions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Auction> getAuctionsFromUser(@CookieParam("scc:session") Cookie session,
            @PathParam("USER_ID") String userId) {
        try {
            if(autheticationIsActive) auth.checkCookieUser(session, userId);
            try {


                User u = null;
                if (cacheIsActive) {
                    u = cache.get(userId, User.class);
                }
                UserDAO userDAO = null;
                if (u == null) {
                    userDAO = db.getById(userId, MongoDBLayer.USERS, UserDAO.class);
                }

                if (userDAO == null && u == null) {
                    throw new WebApplicationException(Status.NOT_FOUND);
                }

                List<Auction> l = new ArrayList<>();

                FindIterable<AuctionDAO> auctions = db.getElementsFromObject(userId, MongoDBLayer.AUCTIONS, AuctionDAO.class);
                for (AuctionDAO o : auctions) {
                    l.add(new Auction(o));
                }

                return l;


            } catch (MongoException e) {
                throw new WebApplicationException(e.getCode());
            }

        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("Invalid user : " + userId);
        }



    }
}

