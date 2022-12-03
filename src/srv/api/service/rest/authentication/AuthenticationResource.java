package srv.api.service.rest.authentication;

import cache.RedisCache;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import srv.data.user.User;
import srv.data.user.UserDAO;
import srv.layers.CosmosDBLayer;

import javax.ws.rs.*;
import java.util.UUID;

@Path("/auth")
public class AuthenticationResource {

    private CosmosDBLayer db = CosmosDBLayer.getInstance();
    private RedisCache cache = RedisCache.getInstance();

    public AuthenticationResource() {
    }

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login user) {

        UserDAO u = db.getById(user.getId(), CosmosDBLayer.USERS, UserDAO.class);

        if (user.getPwd().equals(u.getPwd())) {
            String uid = UUID.randomUUID().toString();
            NewCookie cookie = new NewCookie.Builder("scc:session")
                    .value(uid)
                    .path("/")
                    .comment("sessionid")
                    .maxAge(3600)
                    .secure(false)
                    .httpOnly(true)
                    .build();
            cache.setSession(new Session(uid, u));
            return Response.ok().cookie(cookie).build();
        } else
            throw new WebApplicationException(javax.ws.rs.core.Response.Status.FORBIDDEN);
    }

    public Session checkCookieUser(Cookie session, String id) throws jakarta.ws.rs.NotAuthorizedException {

        if (session == null || session.getValue() == null)
            throw new NotAuthorizedException("No session initialized");

        Session s;
        try {
            s = cache.getSession(session.getValue());
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("No valid session initialized");
        }
        if (s == null || s.getUser() == null)
            throw new NotAuthorizedException("No valid session initialized");
        if (!s.getUser().getId().equals(id))
            throw new NotAuthorizedException("Invalid user : " + s.getUser());
        return s;
    }

    public void deleteCookie(Cookie session){
        cache.deleteSession(session.getValue());
    }

}