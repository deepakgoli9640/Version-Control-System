package org.example;
import com.google.gson.*;


import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.JSON;

public class Groot {

    private final Path repoPath;
    private final Path objectsPath;
    private final Path headPath;
    private final Path indexPath;

    public Groot(Path repoPath) {
        this.repoPath = Paths.get(String.valueOf(repoPath), ".groot");
        this.objectsPath = Paths.get(String.valueOf(this.repoPath), "objects");  //  .groot/objects
        this.headPath = Paths.get(String.valueOf(this.repoPath), "HEAD"); // .groot/HEAD
        this.indexPath = Paths.get(String.valueOf(this.repoPath), "index");
        this.init();
    }

    public void init() {
        File nestedDirs = this.objectsPath.toFile();
        boolean created = nestedDirs.mkdirs();
        if (created)
            System.out.println("Directory created Successfully");
        else
            System.out.println("failed to created Directory or Directory already exists");

        try {
            Files.write(
                    this.headPath,
                    "".getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
            Files.write(
                    this.indexPath
                    , "[]".getBytes(StandardCharsets.UTF_8)
                    , StandardOpenOption.CREATE_NEW
                    , StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            System.out.print("ALready initialized the groot folder");
        }

    }

    public String hashObject(String content) throws NoSuchAlgorithmException {
        byte[] bytes = MessageDigest.getInstance("SHA-1").digest(content.getBytes());
        StringBuilder hexstring = new StringBuilder();
        for (byte b : bytes)
            hexstring.append(String.format("%02x", b));
        return hexstring.toString();
    }

    public void add(String fileToBeAdded) throws IOException {
        String fileHash;
        try {
            String fileData = Files.readString(Path.of((fileToBeAdded)));
            fileHash = (this.hashObject(fileData));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Path newFileHashedObjectPath = Paths.get(String.valueOf(this.objectsPath), fileHash);
        try {
            Files.write(newFileHashedObjectPath,
                    fileHash.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (Exception e) {

            Files.write(newFileHashedObjectPath,
                    fileHash.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );

        }

        System.out.println("Added");
        this.updateStagingArea((fileToBeAdded), fileHash);

    }

    public void updateStagingArea(String filePath, String fileHash) throws IOException {
        String content = Files.readString(this.indexPath);
        Gson gson = new Gson();
        JsonArray index = gson.fromJson(content, JsonArray.class);
        JsonObject newFile = new JsonObject();
        newFile.addProperty("path", filePath);
        newFile.addProperty("hash", fileHash);
        index.add(newFile);
        Files.writeString(indexPath, gson.toJson(index), StandardOpenOption.TRUNCATE_EXISTING);

    }

    public void commit(String message) throws IOException, NoSuchAlgorithmException {
        String content = Files.readString(this.indexPath);
        Gson gson = new Gson();
        JsonArray index = gson.fromJson(content, JsonArray.class);
        String parentCommit = this.getCurrentHead();
        Map<String, String> commitData = new HashMap<>();
        commitData.put("timeStamp", new Date().toString());
        commitData.put("message", message);
        commitData.put("files", index.toString());
        commitData.put("parent", parentCommit);

        String commitHash = this.hashObject(commitData.toString());
        Path commitPath = Paths.get(String.valueOf(this.objectsPath), commitHash);
        String commitDataString = gson.toJson(commitData);
        Files.write(commitPath, commitDataString.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        Files.write(this.headPath, commitHash.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);

        Files.write(
                this.indexPath
                , "[]".getBytes(StandardCharsets.UTF_8)
                , StandardOpenOption.TRUNCATE_EXISTING
                , StandardOpenOption.WRITE
        );
        System.out.print("commit successful");
        this.showCommitDiff(commitHash);
    }

    private String getCurrentHead() {
        try {
            return Files.readString(this.headPath);
        } catch (IOException e) {
            return null;
        }
    }

    public void log() throws IOException {
        String currentCommitHash = this.getCurrentHead();
        while (!currentCommitHash.isBlank()) {
            Path commitDataPath = Paths.get(String.valueOf(this.objectsPath), currentCommitHash);
            String commitData = Files.readString(commitDataPath);
            Gson gson = new Gson();

                JsonObject jsonObject = JsonParser.parseString(commitData).getAsJsonObject();
                String parent = jsonObject.get("parent").getAsString();
                String timestamp=jsonObject.get("timeStamp").getAsString();
                System.out.println("parent :"+parent);
                System.out.println("timeStamp: "+timestamp);
                System.out.println("-------------------------------------------------");
                currentCommitHash=parent;
        }
    }

    public void showCommitDiff(String commitHash) throws IOException {
        String commitData = this.getCommitData(commitHash);
        Gson gson = new Gson();
        JsonObject jsonObject = JsonParser.parseString(commitData).getAsJsonObject();
        String files = jsonObject.get("files").getAsString();
        String parent=jsonObject.get("parent").getAsString();
        JsonArray filesArray = JsonParser.parseString(files).getAsJsonArray();
        for (JsonElement element : filesArray) {
            JsonObject fileObj = element.getAsJsonObject();
            String path = fileObj.get("path").getAsString();
            String hash = fileObj.get("hash").getAsString();

            String content = this.getFileContent(hash);
            System.out.println("content" + content);
            if (parent != null) {
                String parentCommitData = this.getCommitData(parent);
                String getParentFileContent=this.getParentFileContent(parentCommitData, Path.of(path));
            }
        }

    }
    public String getParentFileContent(String parentCommitData,Path path)
    {
        JsonObject jsonObject=JsonParser.parseString(parentCommitData).getAsJsonObject();
        String parentPath=jsonObject.get("path").getAsString();

    }
    public String getCommitData(String commitHash){
        Path commitPath=Paths.get(String.valueOf(this.objectsPath),commitHash);
        try{
            return Files.readString(commitPath);
        } catch (IOException e) {
            System.out.println("Failed to read the commit Data");
        }
        return null;
    }

    public String getFileContent(String filehash) throws IOException {
        Path objectpath=Paths.get(String.valueOf(this.objectsPath),filehash);
        return Files.readString(objectpath);
    }

}



