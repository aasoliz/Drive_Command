import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;

import java.lang.InterruptedException;

import java.util.ArrayList;
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
  *  @param find       - Filename to find
  *  @param parents    - Parent folders of the file 'find'
  *  @return The file that was found
  */
  public DriveDirectory inDrive(String find, String[] parents, 
                                Boolean folder, Boolean parent) {
    return inDr(find, parents, 0, null, folder, parent);
  }

  private DriveDirectory inDr(String find, String[] parents, int k, 
                              DriveDirectory rt, Boolean folder, Boolean parent) {

    DriveDirectory temp = null;

    ArrayList<DriveDirectory> children = null;

    rt = (rt == null) ? root : rt;
    children = rt.getChildren();

    int stop = (children == null) ? 0 : children.size();
    for(int i = 0; i < stop; i++) {
      if(children.get(i).getName().equals(find) && k == parents.length) {
        if(folder)
          return rt;

        return children.get(i);
      }

      else if(k < parents.length && children.get(i).getName().equals(parents[k])) 
        return inDr(find, parents, ++k, children.get(i), folder, parent);
    }

    if(k == parents.length && parent)
      return rt;

    return null;
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
public static LinkedList<DriveDirectory> addChildren(LinkedList<DriveDirectory> parents) 
  throws IOException, InterruptedException {

    if(parents.size() == 0)
      return parents;

    DriveDirectory parent = parents.removeFirst();

    // Search through folder and get all children
    FileList result = null;

    try {
        result = service.files().list()
            .setQ("'" + parent.getID() + "' in parents and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name, mimeType, modifiedTime)")
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
        temp = new DriveDirectory(file.getName(), file.getId(), file.getModifiedTime(), true);

        parents.addLast(temp);
      }
      else
        temp = new DriveDirectory(file.getName(), file.getId(), file.getModifiedTime(), false);

      parent.children.add(temp);
    }

    return addChildren(parents);
  }
}
