package srv.api.service.rest;

import srv.layers.PersistentVolume;
import utils.Hash;

import java.util.List;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/media")
public class MediaResource {

    PersistentVolume blob = PersistentVolume.getInstance();

    /**
     * Uploads content
     *
     * @param contents - content to be uploaded
     * @return the generated id
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(byte[] contents) {
        String id = Hash.of(contents);
        blob.upload(contents, id);
        return id;
    }

    /**
     * Downloads the content
     *
     * @param id - id of the content to be downloaded
     * @return the content
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(@PathParam("id") String id) {
        return blob.download(id);
    }

    /**
     * Lists the ids of images stored.
     *
     * @return the list
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list() {
        return blob.list();
    }
}