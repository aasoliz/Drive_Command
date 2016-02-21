import com.google.api.client.http.FileContent;

import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Collections;
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

  /**
  *  Reads and keeps track of a file that contains the 
  *  conversion mappings of files to drive files.
  *
  *  @return Whether the method was successful
  *  @throws IOException - If the file was not found or could not be read
  */
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

  /**
  *  Determines the mimeType of the provided file and if
  *  able sets it to the special Google file.
  *
  *  @param path - Absolute path to local file
  *  @return mimeType of provided file
  *  @throws IOException
  */
  public static String fileType(Path path) throws IOException {
    // TODO: Need to change to Google's mimeType?
    String ext = java.nio.file.Files.probeContentType(path);
    String test = mimeType.get(ext);

    if(test == null)
      return ext;

    return test;
  }

  /**
  *  Uploads the given file/folder into Google Drive. Uses the local parent folder
  *  to determine the parent folder in Drive. Assumes the uploader intends to allow
  *  writers to share the file and viewers to copy the file.
  *
  *  @param file - Local file to upload
  *  @param mimeType - Type of file that is to be uploaded
  *  @throws IOException - If upload was unsuccessful
  */
  public static void uploadFile(IOFile file, String mimeType) throws IOException {
    // TODO: Multipart upload?

    System.out.println("Files : " + file.getName());

    File meta = new File();
    meta.setName(file.getName());
    meta.setMimeType(mimeType);

    // Get the id of the parent folder in drive
    String parentId = root.getDriveParent(IOFile.parentFolders(file.getOriginal(), file)).getID();
    meta.setParents(Collections.singletonList(parentId));
      
    meta.setWritersCanShare(true);
    meta.setViewersCanCopyContent(true);

      try {
        // Upload a file
        if(!mimeType.equals("application/vnd.google-apps.folder")) {
          
          // Actual content of the file
          FileContent mediaContent = new FileContent(mimeType, file.getOriginal());
          
          File nw = service.files().create(meta, mediaContent)
               .setFields("id")
               .execute();

          // Add newly created drive folder to the Drive directory
          root.addDir(nw, file, false);
        }
        else {
          File nw = service.files().create(meta)
             .setFields("id")
             .execute();
        
          // Add newly created drive folder to the Drive directory
          root.addDir(nw, file, true);
        }
      } catch (IOException e) {
        LinkedList code = DriveCommand.getErrorCode(e);

        if(code != null)
          DriveCommand.handleError(code, e);
        else
          e.printStackTrace();
      }

  }
}