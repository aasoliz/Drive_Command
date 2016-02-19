import com.google.api.client.http.FileContent;

import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

public class DriveUpload {
  private static HashMap<String, String> mimeType;

  public DriveUpload() {}

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

  public static void uploadFile(IOFile file, String mimeType, Drive service) throws IOException {
    // TODO: Check if file is folder, add to "foldermappings"
    // TODO: Multipart upload?

    System.out.println("Files : " + IOFile.getName(file));

    LinkedList<String> parent = new LinkedList<String>();
    parent.add(DriveSearch.getParentId(
      IOFile.parentFolder(
        IOFile.getOriginal(file)
        )
      )
    );

    File meta = new File();
    meta.setName(IOFile.getNameExt(file));
    meta.setMimeType(mimeType);
    meta.setParents(parent);
    meta.setWritersCanShare(true);
    meta.setViewersCanCopyContent(true);

    if(!mimeType.equals("application/vnd.google-apps.folder")) {
      FileContent mediaContent = new FileContent(mimeType, IOFile.getOriginal(file));

      try {
        File nw = service.files().create(meta, mediaContent)
             .setFields("id")
             .execute();
        System.out.printf("ew file created %s", nw.getId());

      } catch (IOException e) {
        System.out.println("Name: " + IOFile.getName(file) + " " + e);
      }
    }
    else {
      try {
        System.out.println("hey folder");
        File nw = service.files().create(meta)
             .setFields("id")
             .execute();
        System.out.printf("ew file created %s", nw.getId());
        
        DriveSearch.updateFolders(file, nw.getId());
      
      } catch (IOException e) {
        System.out.println("Name: " + IOFile.getName(file) + " " + e);
      }
    }
  }
}