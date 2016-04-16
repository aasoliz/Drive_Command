import com.google.api.client.http.FileContent;

import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.InterruptedException;

import java.nio.file.Path;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JOptionPane;

public class DriveUpload {
  private static HashMap<String, String> mimeType;
  private static Drive service;
  private static DriveDirectory root;
  private InputStream is;

  public DriveUpload(Drive serve, DriveDirectory rt, Boolean commandLine) throws IOException {
    service = serve;
    root = rt;

    is = DriveUpload.class.getClassLoader().getResourceAsStream("mimeTypeMapping.txt");

    if(is == null) {
      if(commandLine)
        System.out.println("Resource was not found");
      else
        JOptionPane.showMessageDialog(null, "Resource was not found", "Information", JOptionPane.OK_OPTION);
    }
  }

  /**
  *  Reads and keeps track of a file that contains the 
  *  conversion mappings of files to drive files.
  *
  *  @return Whether the method was successful
  *  @throws IOException - If the file was not found or could not be read
  */
  public static Boolean types(DriveUpload up) throws IOException {
    if(up.is == null) {
      mimeType = null;
      return false;
    }

    mimeType = new HashMap<String, String>();
    try(BufferedReader reader = new BufferedReader(new InputStreamReader(up.is))) {
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
    // Convert to Google extension is possible
    String ext = java.nio.file.Files.probeContentType(path);

    String test = (mimeType == null) ? null : mimeType.get(ext);

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
  public static void uploadFile(IOFile file, String mimeType, DriveSearch ds) throws IOException, InterruptedException {
    // TODO: Multipart upload?

    System.out.println("Files : " + file.getName());

    File meta = new File();
    meta.setName(file.getName());
    meta.setMimeType(mimeType);

    // Get the id of the parent folder in drive
    String parentId = ds.inDrive(file.getName(), IOFile.parentFolders(file.getOriginal(), file)).getID();
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
          root.addDir(file, false, nw.getId(), System.currentTimeMillis(), ds);
        }
        else {
          File nw = service.files().create(meta)
             .setFields("id")
             .execute();
        
          // Add newly created drive folder to the Drive directory
          root.addDir(file, true, nw.getId(), System.currentTimeMillis(), ds);
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
