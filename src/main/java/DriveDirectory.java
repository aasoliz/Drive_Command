import com.google.api.services.drive.model.*;

import java.io.IOException;

import java.lang.InterruptedException;

import java.util.LinkedList;

public class DriveDirectory {
  DriveDirectory[] children;
  LinkedList<DriveDirectory> subFolders;
  String name;
  String id;
  Boolean folder;
  

  public DriveDirectory(String nme, String identification, Boolean flag) {
    children = null;
    subFolders = null;
    name = nme;
    id = identification;
    folder = flag;
  }

  /** Name of Drive file @return Name of Drive file */
  public String getName() { return this.name; }

  /** Id of Drive file @return ID of Drive file */
  public String getID() { return this.id; }

  /** Children (files/folders) of folder @return Children of folder */
  public DriveDirectory[] getChildren() { return this.children; }

  /** Parent folders of file @return List of parent folder for file */
  public LinkedList<DriveDirectory> getSubFolders() { return this.subFolders; }

  /**
  *  Creates a parent to child relationship.
  *  
  *  @param parent Parent folder in Drive
  *  @param subFolder  Child folder/file contained in parent
  */
  public static void addSubFolder(DriveDirectory parent, DriveDirectory subFolder) {
    if(parent.subFolders == null)
      parent.subFolders = new LinkedList<DriveDirectory>();
    
    parent.subFolders.addLast(subFolder);
  }

  /**
  *  Adds the given Drive file to the tree of previously indexed files/folders
  *  from Google Drive. Used after a local file/folder has been uploaded to Drive.
  *
  *  @param d - Newly uploaded file
  *  @param f - Local file and information
  *  @param folder - Boolean specifying if 'f' is a file or folder 
  */
  public void addDir(IOFile f, Boolean folder, String pID, DriveSearch ds) {
    String[] parents = IOFile.parentFolders(f.getOriginal(), f);

    DriveDirectory nw = new DriveDirectory(f.getName(), pID, folder);
    DriveDirectory parent = ds.inDrive(f.getName(), parents);

    addSubFolder(parent, nw);
  }
}
