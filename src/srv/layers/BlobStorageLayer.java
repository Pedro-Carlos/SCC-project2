package srv.layers;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobItem;
import utils.AzureKeys;

public class BlobStorageLayer {
    private static BlobStorageLayer instance;

    private BlobContainerClient containerClient;

    private static boolean isReplica = false;
    public static final String KEY = AzureKeys.getInstance().getBlobKey();
    public static final String KEY_REPLICA = AzureKeys.getInstance().getReplicationBlobKey();


    public static synchronized BlobStorageLayer getInstance(boolean isReplication) {
        if (instance != null && isReplica == isReplication)
            return instance;

        BlobContainerClient containerClient;
        if(!isReplication){
            containerClient = new BlobContainerClientBuilder()
                    .connectionString(KEY)
                    .containerName("images")
                    .buildClient();

        } else {
            containerClient = new BlobContainerClientBuilder()
                    .connectionString(KEY_REPLICA)
                    .containerName("images")
                    .buildClient();
        }
        instance = new BlobStorageLayer(containerClient);


        return instance;
    }

    public BlobStorageLayer(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    public void upload(byte[] media, String id) {

        BlobClient blob = containerClient.getBlobClient(id);
        if (!blob.exists()) {
            blob.upload(BinaryData.fromBytes(media));
        }

    }

    public byte[] download(String id) {

        BlobClient blob = containerClient.getBlobClient(id);
        if (!blob.exists()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        BinaryData data = blob.downloadContent();

        return data.toBytes();
    }

    public List<String> list() {
        List<String> list = new ArrayList<>();
        for (BlobItem image : containerClient.listBlobs()) {
            list.add(image.getName());
        }

        return list;
    }

    public boolean blobExists(String id) {
        BlobClient blob = containerClient.getBlobClient(id);
        return blob.exists();
    }

}
