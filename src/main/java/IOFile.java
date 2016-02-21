import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.IOException;

import java.util.LinkedList;
import java.util.LinkedHashMap;

public class IOFile {
  private String originalFolder;
  private File original;
  private String name;
  private File parent;
  private Boolean isDirectory;
  private LinkedList<File> subFiles = new LinkedList<File>();

  public IOFile(String root, String top) {
    originalFolder = top;
    original = new File(root);
    name = original.getName();
    parent = null;
    isDirectory = original.isDirectory();
  }

  public IOFile(File root, String top) {
    originalFolder = top;
    original = root;
    name = original.getName();
    parent = null;
    isDirectory = original.isDirectory();
  }

  public IOFile(File root, String top, IOFile par) {
    originalFolder = top;
    original = root;
    name = original.getName();
    parent = par.getOriginal();
    isDirectory = original.isDirectory();
  }

  public String getOriginalFolder() {
    return this.originalFolder;
  }

  public File getOriginal() {
    return this.original;
  }

  public String getName() {
    return this.name;
  }

  // public String getNameExt() {
  //   String[] path = this.getOriginal().toPath().toString().split("/");

  //   System.out.println(path[path.length-1]);
  //   return path[path.length-1];
  // }

  public File getParent() {
    return this.parent;
  }

  public Boolean getDirectory() {
    return this.isDirectory;
  }

  public LinkedList<File> getSubFiles() {
    return this.subFiles;
  }

  // May not need anymore
  public static String parentFolder(File file) {
    String[] path = file.getParent().split("/");

    int len = path.length;

    return path[len-1];
  }

  public static String[] parentFolders(File file, IOFile fi) {
    String originalFolder = fi.getOriginalFolder();

    String[] paths = file.getParent().split("/");

    int index = -1;
    for(int i = 0; i < paths.length; i++) {
      if(paths[i].equals(originalFolder)) {
        index = i + 1;
        break; 
      }
    }

    String[] fin_path = new String[paths.length - index];

    int j = 0;
    for(int i = index; i < paths.length; i++) {
      fin_path[j] = paths[i];
      j++;
    } 

    return fin_path;
  }

  public static LinkedHashMap<IOFile, String> deep(IOFile fi, DriveSearch ds, DriveUpload up) throws IOException, InterruptedException {
    // Initial list of directories to search
    File[] initial = fi.getOriginal().listFiles();
    
    for(File file : initial)
      fi.getSubFiles().add(file);

    return deeper(fi, fi.getSubFiles().size(), new LinkedHashMap<IOFile, String>(), ds, up);
  }

  private static LinkedHashMap<IOFile, String> deeper(IOFile fi, int numSize, LinkedHashMap<IOFile, String> adding, DriveSearch ds, DriveUpload up) throws IOException, InterruptedException {
    if(numSize == 0)
      return adding;

    File temp = fi.getSubFiles().removeFirst();
    numSize--;

    Boolean drive = ds.inDrive(temp.getName(), parentFolders(temp, fi));

    // Checks if the folder/file was in Drive folders
    if(!drive) {
      if(temp.getName().charAt(0) != '.' && temp.getName().charAt(0) != '#')
        adding.put(new IOFile(temp, fi.getOriginalFolder(), fi), up.fileType(temp.toPath()));
    }
    if(temp.isDirectory()) {
      File[] list = temp.listFiles();

      for(File file : list)
        fi.getSubFiles().addLast(file);

      // Keep track of how many subFolders are left
      numSize = fi.getSubFiles().size();
    }

    return deeper(fi, numSize, adding, ds, up);
  }
}