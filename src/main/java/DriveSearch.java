import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;

import java.lang.InterruptedException;

import java.util.LinkedList;

public class DriveSearch {
  private static Drive service;
  private static DriveDirectory root;

  public DriveSearch(Drive serve, DriveDirectory rt) {
    service = serve;
    root = rt;
  }

  /**
  *  Attempts to find a given local file with a list of parents in
  *  Google Drive.
  *
  *  @param find - Filename to find
  *  @param parents - Parent folders of the file 'find'
  *  @return Whether the file was found
  */
  public Boolean inDrive(String find, String[] parents) {
    Boolean found = false;

    LinkedList<DriveDirectory> children = root.getChildren();
    DriveDirectory temp = null;

    int k = 0;
    while(!found) {
      if(children != null && k < parents.length + 1) {
        for(DriveDirectory child : children) {
          // Parent folder was found
          if(k < parents.length && child.getName().equals(parents[k])) {
            temp = child;
            break;
          }
          // Child was found in the last parent folder
          else if(child.getName().equals(find))
            return true;
        }
      }
      else
        return false;

      // If parent folder found, gets its children
      //  and searches for the next parent folder
      //  or file on the next iteration
      if(temp != null) {
        k++;
        children = temp.getChildren();
        temp = null;
      }
      else 
        return false;
    }

    return false;
  }

  /**
  *  Indexes the user provided Drive folder by getting all files and folders
  *  recursively, going through each folder. Once found adds a parent child
  *  relationship.
  *
  *  @param parents - Folders still left to index
  *  @return LinkedList of parents for the last searched file/folder
  *  @throws IOException - If API was unable to gather file/folder information 
  */
public static LinkedList<DriveDirectory> addChildren(LinkedList<DriveDirectory> parents) throws IOException, InterruptedException {
    if(parents.size() == 0)
      return parents;

    DriveDirectory parent = parents.removeFirst();

    // Search through folder and get all children
    FileList result = null;

    try {
        result = service.files().list()
            .setQ("'" + parent.getID() + "' in parents and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name, mimeType)")
            .execute();
    } catch (IOException e) {
        LinkedList code = DriveCommand.getErrorCode(e);

        if(code != null)
            DriveCommand.handleError(code, e);
        else
            e.printStackTrace();
    }
    for(File file : result.getFiles()) {
      DriveDirectory temp = null;
      
      // Only search folders
      if(file.getMimeType().equals("application/vnd.google-apps.folder")) {
        temp = new DriveDirectory(file.getName(), file.getId(), true);

        DriveDirectory.addChild(parent, temp);
        parents.addLast(temp);
      }
      
      temp = new DriveDirectory(file.getName(), file.getId(), false);

      DriveDirectory.addChild(parent, temp);
    }

    return addChildren(parents);
  }
}
