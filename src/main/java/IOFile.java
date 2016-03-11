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

  /** Name of user provided folder @return Name of inital folder */
  public String getOriginalFolder() {
    return this.originalFolder;
  }

  /** Local file @return java.io.File of local file */
  public File getOriginal() {
    return this.original;
  }

  /** Name of local file @return Name of local file */
  public String getName() {
    return this.name;
  }

  /** Immediate parent folder @return Parent folder of file */
  public File getParent() {
    return this.parent;
  }

  /** Is the IOFile a folder or file @return Whether the local file is a folder or file */
  public Boolean getDirectory() {
    return this.isDirectory;
  }

  /** List of children files/folders @return List of children */
  public LinkedList<File> getSubFiles() {
    return this.subFiles;
  }

  /**
  *  Returns parent folders from user given folder.
  *
  *  @param file - Local file
  *  @param fi - IOFile used to get the user provided folder
  *  @return Portion of the absolute path starting from the previously provided folder
  */
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

  /**
  *  Gets the initial list of folders to search
  *  
  *  @param fi - Starting folder to search
  *  @param ds - Drive searching
  *  @param up - File type getter for files
  *  @return LinkedHashMap of the local files not in Drive
  *  @throws IOException
  */
  public static LinkedHashMap<IOFile, String> deep(IOFile fi, DriveSearch ds, DriveUpload up) throws IOException {
    LinkedHashMap<IOFile, String> adding = new LinkedHashMap<IOFile, String>();

    // Initial list of directories to search
    File[] initial = fi.getOriginal().listFiles();
    
    for(File file : initial)
      fi.getSubFiles().add(file);

    int size = fi.getSubFiles().size();
    while(size > 0) {
      deeper(fi, fi.getSubFiles().removeFirst(), adding, ds, up);
      size = fi.getSubFiles().size();
    }

    return adding;
  }

  /**
  *  Recursively processes sub folders and files. Checks if they are in Drive and if not are added
  *  to the list of files to add. Excludes any temporary local files.
  *
  *  @param fi - Current folder that is being indexed
  *  @param numSize - Number of folders left to go through
  *  @param adding - List of files that need to be uploaded to Drive
  *  @param ds - Drive searching
  *  @param up - File type getter for files
  *  @return  LinkedHashMap of the local files not in Drive
  *  @throws IOException
  */
  private static LinkedHashMap<IOFile, String> deeper(IOFile fi, File temp, LinkedHashMap<IOFile, String> adding, DriveSearch ds, DriveUpload up) throws IOException {
      //if(numSize == 0)
      //return adding;

      //File temp = fi.getSubFiles().removeFirst();
    //numSize--;

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
      //numSize = fi.getSubFiles().size();
    }
    
    return adding;
    //return deeper(fi, numSize, adding, ds, up);
  }
}
