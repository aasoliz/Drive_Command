import com.google.api.client.util.DateTime;

import com.google.api.services.drive.model.*;

import java.io.IOException;

import java.lang.InterruptedException;

import java.util.ArrayList;
import java.util.LinkedList;

public class DriveDirectory {
  public ArrayList<DriveDirectory> children;
  private String name;
  private String id;
  private long modifiedTime;
  private Boolean folder;

  public DriveDirectory(String nme, String identification, DateTime time, Boolean flag) {
    children = new ArrayList<DriveDirectory>();
    name = nme;
    id = identification;
    modifiedTime = time.getValue();
    folder = flag;
  }

  public DriveDirectory(String nme, String identification, long time, Boolean flag) {
    children = new ArrayList<DriveDirectory>();
    name = nme;
    id = identification;
    modifiedTime = time;
    folder = flag;
  }

  /** Name of Drive file @return Name of Drive file */
  public String getName() { return this.name; }

  /** Id of Drive file @return ID of Drive file */
  public String getID() { return this.id; }

  /** Modified time of Drive rile @return Modified Time of drive file */
  public long getModTime() { return this.modifiedTime; }

  /** Children (files/folders) of folder @return Children of folder */
  public ArrayList<DriveDirectory> getChildren() { return this.children; }

  /**
  *  Adds the given Drive file to the tree of previously indexed files/folders
  *  from Google Drive. Used after a local file/folder has been uploaded to Drive.
  *
  *  @param d - Newly uploaded file
  *  @param f - Local file and information
  *  @param folder - Boolean specifying if 'f' is a file or folder 
  */
  public void addDir(IOFile f, Boolean folder, String pID, long modTime, DriveDirectory parent) {
    String[] parents = IOFile.parentFolders(f.getOriginal(), f);

    DriveDirectory nw = new DriveDirectory(f.getName(), pID, modTime, folder);

       // TODO: find better way
    if(folder)
       parent.children.add(0, nw);
    else 
      parent.children.add(nw);
  }
}
