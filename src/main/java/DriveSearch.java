import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.IOException;

import java.util.LinkedList;

public class DriveSearch {
  private static Drive service;
  private static DriveDirectory root;

  public DriveSearch(Drive serve, DriveDirectory rt) {
    service = serve;
    root = rt;
  }

  public Boolean inDrive(String find, String[] parents) throws IOException {
    Boolean found = false;

    LinkedList<DriveDirectory> children = root.getChildren();
    DriveDirectory temp = null;

    int k = 0;
    while(!found) {
      if(k < parents.length + 1) {
        for(DriveDirectory child : children) {
          if(k < parents.length && child.getName().equals(parents[k])) {
            temp = child;
            break;
          }
          else if(child.getName().equals(find))
            return true;
        }
      }
      else
        return false;

      if(temp != null) {
        k++;

        children = temp.getChildren();
        if(children == null)
          return false;

        temp = null;
      }
      else 
        return false;
    }

    return false;
  }

  public static LinkedList<DriveDirectory> addChildren(LinkedList<DriveDirectory> parents) throws IOException {
    if(parents.size() == 0)
      return parents;

    DriveDirectory parent = parents.removeFirst();

    FileList result = service.files().list()
            .setQ("'" + parent.getID() + "' in parents and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name, mimeType)")
            .execute();
    
    for(File file : result.getFiles()) {
      DriveDirectory temp = null;
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

  public static File getParent(String parentID) throws IOException {
    File result;
    try {
      result = service.files().get(parentID)
              .setFields("id, name, parents")
              .execute();
    } catch (IOException e) {
      System.out.println("asdhf");
    }

    return result;
  }
}