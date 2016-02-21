import com.google.api.client.http.FileContent;

import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Path;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

public class DriveUpload {
  private static HashMap<String, String> mimeType;
  private static Drive service;
  private static DriveDirectory root;

  public DriveUpload(Drive serve, DriveDirectory rt) {
    service = serve;
    root = rt;
  }

  public static Boolean types() throws IOException {
    mimeType = new HashMap<String, String>();
    try(BufferedReader reader = new BufferedReader(new FileReader("/home/aasoliz/Documents/Other/Commands/Drive_Command/src/main/resources/mimeTypeMapping.txt"))) {
      String curr;
      String[] parsed;
      while((curr = reader.readLine()) != null) {
        parsed = curr.split("[,\\s]");

        for(int i = 0; i < parsed.length-1; i++)
          mimeType.put(parsed[i], parsed[parsed.length-1]);
      }

    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public static String fileType(Path path) throws IOException {
    // TODO: Need to change to Google's mimeType?
    String ext = java.nio.file.Files.probeContentType(path);
    String test = mimeType.get(ext);

    if(test == null)
      return ext;

    return test;
  }

  public static void uploadFile(IOFile file, String mimeType) throws IOException {
    // TODO: Multipart upload?

    System.out.println("Files : " + file.getName());

    File meta = new File();
    meta.setName(file.getName());
    meta.setMimeType(mimeType);

    String parentId = root.getDriveParent(IOFile.parentFolders(file.getOriginal(), file)).getID();
    meta.setParents(Collections.singletonList(parentId));
      
    meta.setWritersCanShare(true);
    meta.setViewersCanCopyContent(true);

    if(!mimeType.equals("application/vnd.google-apps.folder")) {
      FileContent mediaContent = new FileContent(mimeType, file.getOriginal());

      try {
        File nw = service.files().create(meta, mediaContent)
             .setFields("id")
             .execute();

        // Add newly created drive folder to the Drive directory
        root.addDir(nw, file, false);

      } catch (IOException e) {
        System.out.println("Name: " + file.getName() + " " + e);
      }
    }
    else {
      try {
        File nw = service.files().create(meta)
             .setFields("id")
             .execute();
        
        // Add newly created drive folder to the Drive directory
        root.addDir(nw, file, true);

      } catch (IOException e) {
        System.out.println("Name: " + file.getName() + " " + e);
      }
    }
  }
}