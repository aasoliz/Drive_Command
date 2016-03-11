import com.google.api.services.drive.model.*;

import java.io.IOException;

import java.lang.InterruptedException;

import java.util.LinkedList;

public class DriveDirectory {
  LinkedList<DriveDirectory> children;
  LinkedList<DriveDirectory> parents;
  String name;
  String id;
  Boolean folder;

  public DriveDirectory(String nme, String identification, Boolean flag) {
    children = null;
    parents = null;
    name = nme;
    id = identification;
    folder = flag;
  }

  /** Name of Drive file @return Name of Drive file */
  public String getName() { return this.name; }

  /** Id of Drive file @return ID of Drive file */
  public String getID() { return this.id; }

  /** Children (files/folders) of folder @return Children of folder */
  public LinkedList<DriveDirectory> getChildren() { return this.children; }

  /** Parent folders of file @return List of parent folder for file */
  public LinkedList<DriveDirectory> getParents() { return this.parents; }

  /**
  *  Creates a parent to child relationship.
  *  
  *  @param parent Parent folder in Drive
  *  @param child  Child folder/file contained in parent
  */
  public static void addChild(DriveDirectory parent, DriveDirectory child) {
    if(parent.children == null)
      parent.children = new LinkedList<DriveDirectory>();
    if(child.parents == null)
      child.parents = new LinkedList<DriveDirectory>();
    
    if(parent.parents != null)
      child.parents.addAll(parent.parents);
  
    child.parents.addLast(parent);

    parent.children.add(child);
  }

  /**
  *  Adds the given Drive file to the tree of previously indexed files/folders
  *  from Google Drive. Used after a local file/folder has been uploaded to Drive.
  *
  *  @param d - Newly uploaded file
  *  @param f - Local file and information
  *  @param folder - Boolean specifying if 'f' is a file or folder 
  */
public void addDir(File d, IOFile f, Boolean folder) throws IOException, InterruptedException {
    String[] parents = IOFile.parentFolders(f.getOriginal(), f);

    DriveDirectory nw = new DriveDirectory(f.getName(), d.getId(), folder);
    DriveDirectory parent = getDriveParent(parents);

    addChild(parent, nw);
  }

  /**
  *  Gets the last parent folder, which can be used to 
  *  get the id for uploading. Or can be used to update
  *  the indexed Drive tree. The method assumes it will 
  *  find all parent folders.
  *
  *  @param parents - List of parent folders
  *  @return Last parent folder (destination folder)
  */
  public DriveDirectory getDriveParent(String[] parents) {
    // If the folder is the top level folder
    if(parents.length == 0)
      return this;

    DriveDirectory temp = null;
    Boolean found = false;
    
    LinkedList<DriveDirectory> children = getChildren();

    int k = 0;
    while(!found) {
      for(DriveDirectory child : children) {
        if(k < parents.length && child.getName().equals(parents[k])) {
          temp = child;
          break;
        }
      }
      if(temp != null) {
        k++;
        children = temp.getChildren();
      }
      // Used to prevent an infinite loop
      // in case a parent is not found.
      // Should not get here.
      else {
        found = true;
      }
      // Found all parents
      if(k == parents.length)
        return temp;
      
      temp = null;
    }

    return null;
  }
}
