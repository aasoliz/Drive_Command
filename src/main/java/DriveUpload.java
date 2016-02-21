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

  public static void uploadFile(IOFile file, String mimeType, Drive service, DriveDirectory root) throws IOException {
    // TODO: Check if file is folder, add to "foldermappings"
    // TODO: Multipart upload?

    System.out.println("Files : " + file.getName());

    // LinkedList<String> parents = new LinkedList<String>();
    String [] par = IOFile.parentFolders(file.getOriginal(), file);

    for(String g : par)
      System.out.println(g + "/");

    // for(int i = 0; i < path.length; i++) {
    //   System.out.println(path[i]);
    //   parents.add(path[i]);
    // }

    // for(int i = 0; i < par.length; i++) {
    //   parents.add(par[i]);
    // }
    // parent.add(DriveSearch.getParentId(
    //   IOFile.parentFolder(
    //     file.getOriginal()
    //     )
    //   )
    // );
    File meta = new File();
    meta.setName(file.getName());
    System.out.println(file.getName());
    meta.setMimeType(mimeType);

    String parentId = root.getDriveParent(IOFile.parentFolders(file.getOriginal(), file)).getID();
    System.out.println(parentId);

    meta.setParents(Collections.singletonList(parentId));
      
    meta.setWritersCanShare(true);
    meta.setViewersCanCopyContent(true);

    if(!mimeType.equals("application/vnd.google-apps.folder")) {
      java.io.File f = file.getOriginal();

      FileContent mediaContent = new FileContent(mimeType, f);

      try {
        File nw = service.files().create(meta, mediaContent)
             .setFields("id")
             .execute();

        // Add newly created drive folder to the Drive directory
        root.addDir(nw, file, false);

        System.out.printf("ew file created %s", nw.getId());

      } catch (IOException e) {
        System.out.println("Name: " + file.getName() + " " + e);
      }
    }
    else {
      try {
        File nw = service.files().create(meta)
             .setFields("id")
             .execute();
        System.out.printf("ew file created %s\n", nw.getId());
        
        // Add newly created drive folder to the Drive directory
        root.addDir(nw, file, true);
        
        //DriveSearch.updateFolders(file, nw.getId());
      
      } catch (IOException e) {
        System.out.println("Name: " + file.getName() + " " + e);
      }
    }
  }
}