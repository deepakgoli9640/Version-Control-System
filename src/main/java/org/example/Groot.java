package org.example;
import com.google.gson.JsonArray;
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

import static com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.JSON;

public class Groot {

     private final Path repoPath;
     private final Path objectsPath;
     private final Path headPath;
     private final Path indexPath;

    public Groot(Path repoPath)
    {
        this.repoPath= Paths.get(String.valueOf(repoPath),".groot");
        this.objectsPath=Paths.get(String.valueOf(this.repoPath),"objects");  //  .groot/objects
        this.headPath=Paths.get(String.valueOf(this.repoPath),"HEAD"); // .groot/HEAD
        this.indexPath=Paths.get(String.valueOf(this.repoPath),"index");
        this.init();
    }
    public void init(){
        File nestedDirs=this.objectsPath.toFile();
        boolean created= nestedDirs.mkdirs();
        if(created)
            System.out.println("Directory created Successfully");
        else
            System.out.println("failed to created Directory or Directory already exists");

        try{
            Files.write(
                        this.headPath,
                    "".getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
            Files.write(
                    this.indexPath
                    ,"[]".getBytes(StandardCharsets.UTF_8)
                    ,StandardOpenOption.CREATE_NEW
                    ,StandardOpenOption.WRITE
            );
        }
        catch(IOException e)
        {
            System.out.print("ALready initialized the groot folder");
        }

    }
    public byte[] hashObject(String content) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-1").digest(content.getBytes());
    }


}



