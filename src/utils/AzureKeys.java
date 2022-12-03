package utils;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import srv.layers.BlobStorageLayer;

public class AzureKeys {
    private static AzureKeys instance;
    //blobKeys
    private String blobKey;
    private String replicationBlobKey;
    //cosmosKeys
    private String cosmosDbKey;
    private String cosmosDbUrl;
    private String cosmosDB;
    //cacheKeys
    private String redisHostname;
    private String redisKey;
    //cognitiveSearchKeys
    private String searchServiceQueryKey;
    private String searchServiceUrl;
    private String indexName;

    public static AzureKeys getInstance() {

        if (instance != null)
            return instance;

        instance = new AzureKeys();
        return instance;
    }
    public AzureKeys(){
        this.blobKey = "DefaultEndpointsProtocol=https;AccountName=scc57778;AccountKey=/bn5jEszh5Jll9p+CkbH5xEu1xOcR0fjmzUiX800SmBCNaFz8wp0AhoLINB/bQsedmUKSL5FBTmF+AStcxLBQQ==;EndpointSuffix=core.windows.net";
        this.replicationBlobKey = "";
        //cosmosKeys
        this.cosmosDbKey = "jVsddXd9vKjNBHm8WJybVpiE0NvVslwnHPHm0pvWMI6tW6QShILWbzmkRe4o86EcvdFMhTrgz2hZACDb6ab5Cg==";
        this.cosmosDbUrl = "https://scc2257778.documents.azure.com:443/";
        this.cosmosDB = "scc2223db";
        //cacheKeys
        this.redisHostname = "scc2223cache57778.redis.cache.windows.net";
        this.redisKey = "EgQUitxwZisoDmpjdfQZ3EI163xkbVXbhAzCaPFB4ME=";
        //cognitiveSearchKeys
        this.searchServiceQueryKey = "Nt8MJDoj1hMLUNGDsiNL77GfOxWiiqptFjfXtuVZeSAzSeAiTrQ1";
        this.searchServiceUrl = "https://cg57778.search.windows.net";
        this.indexName ="cosmosdb-index" ;
    }


    public String getBlobKey() {
        return blobKey;
    }

    public void setBlobKey(String blobKey) {
        this.blobKey = blobKey;
    }

    public String getReplicationBlobKey() {
        return replicationBlobKey;
    }

    public void setReplicationBlobKey(String replicationBlobKey) {
        this.replicationBlobKey = replicationBlobKey;
    }

    public String getCosmosDbKey() {
        return cosmosDbKey;
    }

    public void setCosmosDbKey(String cosmosDbKey) {
        this.cosmosDbKey = cosmosDbKey;
    }

    public String getCosmosDbUrl() {
        return cosmosDbUrl;
    }

    public void setCosmosDbUrl(String cosmosDbUrl) {
        this.cosmosDbUrl = cosmosDbUrl;
    }

    public String getCosmosDB() {
        return cosmosDB;
    }

    public void setCosmosDB(String cosmosDB) {
        this.cosmosDB = cosmosDB;
    }

    public String getRedisHostname() {
        return redisHostname;
    }

    public void setRedisHostname(String redisHostname) {
        this.redisHostname = redisHostname;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public String getSearchServiceQueryKey() {
        return searchServiceQueryKey;
    }

    public void setSearchServiceQueryKey(String searchServiceQueryKey) {
        this.searchServiceQueryKey = searchServiceQueryKey;
    }

    public String getSearchServiceUrl() {
        return searchServiceUrl;
    }

    public void setSearchServiceUrl(String searchServiceUrl) {
        this.searchServiceUrl = searchServiceUrl;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }


}
