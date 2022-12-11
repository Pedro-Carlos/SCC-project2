package srv.layers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PersistentVolume {
    private static PersistentVolume instance;

    private static String path = "/mnt/vol";

    public static synchronized PersistentVolume getInstance() {
        if( instance != null)
            return instance;

        instance = new PersistentVolume(path);

        return instance;
    }

    public PersistentVolume(String path ) {
        this.path = path;
    }

    public void upload(byte[] media, String id) {
        try {
            Path path = Paths.get(buildPath(id));
            Files.write(path, media);
        } catch (IOException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] download(String id) {
        try {
            Path path = Paths.get(buildPath(id));
            if(!Files.exists(path)) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
    public List<String> list() {
        Stream<Path> files = null;
        ArrayList<String> listOfFiles = new ArrayList<>();
        try {
            files = Files.list(Paths.get(path));
            files.forEach(file -> listOfFiles.add(file.toString()));
        } catch (IOException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        return listOfFiles;

    }




    public boolean blobExists(String id) {
        File blob = new File(buildPath(id));
        return blob.isFile();
    }

    private String buildPath(String id) {
        return path + "/" + id;
    }

}
