import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.LinkedList;

public class DriveSearch {
  // TODO: Save in hidden app folder, so don't have to read over and over
  private static String folderOrigin = "/home/aasoliz/Documents/Other/Commands/Drive_Command/src/main/resources/FolderMapping.txt";
  private static HashMap<String, String> folders;
  private static Drive service;
  private static DriveDirectory root;

  public DriveSearch(Drive serve, DriveDirectory rt) {
    service = serve;
    root = rt;
  }

  public static Boolean inDrive(String find, String[] parents) throws IOException {
    // FileList result = service.files().list()
    //         .setQ("name='" + find + "'")
    //         .setSpaces("drive")
    //         .setFields("files(id, name, parents)")
    //         .execute();

    Boolean found = false;

    LinkedList<DriveDirectory> children = root.getChildren();
    DriveDirectory temp = null;

    System.out.println(find);

    int k = 0;
    while(!found) {
      if(k < parents.length) {
        for(DriveDirectory child : children) {
          System.out.println(child.getName() + " " + parents[k]);
          if(child.getName().equals(parents[k])) {
            temp = child;
            break;
          }

          else if(child.getName().equals(find)) {
            System.out.println("yay " + find);
            return true;
          }
        }
      }
      else
        return false;

      if(temp != null) {
        System.out.println("found parent");
        k++;

        // Children was probably null
        children = temp.getChildren();
        if(children == null)
          return false;

        temp = null;
      }
      else 
        return false;
    }

    return false;
        
    // for(File file : result.getFiles()) {
    //   if(file.getName().equals(find))
    //     if(folders.containsKey(file.getParents().get(0)))
    //       if(folders.get(file.getParents().get(0)).equals(parent))
    //         return true;
    //  }
    // retu.rn false;
  }

  // May not need anymore
  public static Boolean readKnown() {
    folders = new HashMap<String, String>();

    try(BufferedReader reader = new BufferedReader(new FileReader(folderOrigin))) {
      String curr;
      String[] parsed;

      while((curr = reader.readLine()) != null) {
        parsed = curr.split("\\s+");
        folders.put(parsed[0], parsed[1]);
      }

      reader.close();

    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public static String getParentId(String parent) {
    return folders.get(parent);
  }

  public static void updateFolders(IOFile file, String id) throws IOException {
    try(BufferedWriter writer = new BufferedWriter(new FileWriter(folderOrigin, true))) {
      writer.newLine();
      
      writer.write(DriveSearch.getParentId(
        IOFile.parentFolder(
          file.getOriginal()
          )
        ) +
        "      " + IOFile.parentFolder(file.getOriginal())
      );

      writer.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public static LinkedList<DriveDirectory> addChildren(LinkedList<DriveDirectory> parents) throws IOException {
    if(parents.size() == 0)
      return parents;

    DriveDirectory parent = parents.removeFirst();

    FileList result = service.files().list()
            .setQ("'" + parent.getID() + "' in parents")
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
    return service.files().get(parentID)
            .setFields("id, name, parents")
            .execute();
  }
}