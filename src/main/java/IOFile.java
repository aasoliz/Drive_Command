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

  public static TreeMap<IOFile, String> deep(IOFile fi, Drive service) throws IOException {
    // Initial list of directories to search
    File[] initial = getOriginal(fi).listFiles();
    
    for(File file : initial)
      getSubFiles(fi).add(file);

    return deeper(fi, getSubFiles(fi).size(), new TreeMap<IOFile, String>(), service);
  }

  // TODO: Treeset?
  private static TreeMap<IOFile, String> deeper(IOFile fi, int numSize, TreeMap<IOFile, String> adding, Drive service) throws IOException {
    if(numSize == 0)
      return adding;

    File temp = getSubFiles(fi).removeFirst();

    // Checks if the folder/file was in Drive folders
    if(!DriveCommand.inDrive(service, getName(fi))) {
      System.out.println("Not in Drive");
      /* TODO: Add method in DriveQuickstart to
          Get "name", "parent", "mimeType" */
      //adding.put(new IOFile(nme, mimeType), parent.get(0));
    }
    if(temp.isDirectory()) {
      File[] list = temp.listFiles();
      System.out.println("\n" + temp.getName() + "\n");

      for(File file : list) {
        System.out.println(file.getName()); 
        getSubFiles(fi).addLast(file);
      }

      // Keep track of how many subFolders are left
      numSize = getSubFiles(fi).size();
    }

    return deeper(fi, numSize, adding, service);
  }
}