import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.IOException;

import java.util.LinkedList;
import java.util.TreeMap;

public class IOFile {
  private File original;
  private String name;
  private String parents;
  private Boolean isDirectory;
  private LinkedList<File> subFiles = new LinkedList<File>();

  public IOFile(String root) {
    original = new File(root);
    name = original.getName();
    parents = original.getParent();
    isDirectory = original.isDirectory();
  }

  public IOFile(String nme, String mimeType) {
    original = null;
    parents = null;
    name = nme;

    if(mimeType.equals("application/vnd.google-api.folder"))
      isDirectory = true;
    else
      isDirectory = false;
  }

  public static File getOriginal(IOFile fi) {
    return fi.original;
  }

  public static String getName(IOFile fi) {
    return fi.name;
  }

  public static String getParents(IOFile fi) {
    return fi.parents;
  }

  public static Boolean getDirectory(IOFile fi) {
    return fi.isDirectory;
  }

  public static LinkedList<File> getSubFiles(IOFile fi) {
    return fi.subFiles;
  }

  private static String parentFolder(File file) {
    String[] path = file.getParent().split("/");

    int len = path.length;

    return path[len-1];
  }

  public static TreeMap<IOFile, String> deep(IOFile fi, DriveSearch ds) throws IOException, InterruptedException {
    // Initial list of directories to search
    File[] initial = getOriginal(fi).listFiles();
    
    for(File file : initial)
      getSubFiles(fi).add(file);

    return deeper(fi, getSubFiles(fi).size(), new TreeMap<IOFile, String>(), ds);
  }

  // TODO: Treeset?
  private static TreeMap<IOFile, String> deeper(IOFile fi, int numSize, TreeMap<IOFile, String> adding, DriveSearch ds) throws IOException, InterruptedException {
    if(numSize == 0)
      return adding;

    File temp = getSubFiles(fi).removeFirst();
    numSize--;

    Boolean drive = null;

    try {
      drive = ds.inDrive(temp.getName(), parentFolder(temp));
    } catch (Exception e) {

      Thread.sleep(1000);
      drive = ds.inDrive(temp.getName(), parentFolder(temp));
    }

    // Checks if the folder/file was in Drive folders
    System.out.println("\nDrive : " + drive);
    if(drive != null && !drive) {
      System.out.println("Not in Drive\n");
      /* TODO: Add method in DriveQuickstart to
          Get "name", "parent", "mimeType" */
      //adding.put(new IOFile(nme, mimeType), parent.get(0));
    }
    if(temp.isDirectory()) {
      File[] list = temp.listFiles();

      for(File file : list) {
        System.out.println(file.getName()); 
        getSubFiles(fi).addLast(file);
      }

      // Keep track of how many subFolders are left
      numSize = getSubFiles(fi).size();
    }

    return deeper(fi, numSize, adding, ds);
  }
}