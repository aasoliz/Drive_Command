import com.google.api.services.drive.model.*;

import java.io.IOException;

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

  public String getName() { return this.name; }

  public String getID() { return this.id; }

  public LinkedList<DriveDirectory> getChildren() { return this.children; }

  public LinkedList<DriveDirectory> getParents() { return this.parents; }

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

  public static String getPath(DriveDirectory getTo) {
    int len = getTo.getParents().size();

    String path = "";

    for(int i = 0; i < len; i++)
      path += getTo.parents.get(i).getID() + "/";

    return path;
  }

  public static String[] getSuperPath(DriveSearch ds, DriveDirectory folder, String parentID) throws IOException {
    String initial = "";

    File parent = null;
    DriveDirectory temp = null;

    do {
      if(parent != null)
        parentID = parent.getParents().get(0);

      parent = ds.getParent(parentID);

      temp = new DriveDirectory(parent.getName(), parent.getId(), true);
      addChild(temp, folder);

      initial += parent.getId() + "/";

      folder = temp;
    }
    while(parent.getParents() != null);

    String[] split = initial.split("/");
    String[] path = new String[split.length];

    int j = 0;
    for(int i = split.length-1; i > 0; i--) {
      path[j] = split[i];
      j++;
    }
    
    return path;
  }
}