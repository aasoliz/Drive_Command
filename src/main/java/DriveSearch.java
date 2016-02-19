import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;

public class DriveSearch {
  // TODO: Save in hidden app folder, so don't have to read over and over
  private static String folderOrigin = "/home/aasoliz/Documents/Other/Commands/Drive_Command/src/main/resources/FolderMapping.txt";
  private static HashMap<String, String> folders;
  private static Drive service;

  public DriveSearch(Drive serve) {
    service = serve;
  }

  public static Boolean inDrive(String find, String parent) throws IOException {
    FileList result = service.files().list()
            .setQ("name='" + find + "'")
            .setSpaces("drive")
            .setFields("files(id, name, parents)")
            .execute();

    for(File file : result.getFiles()) {
      if(file.getName().equals(find))
        if(folders.containsKey(file.getParents().get(0)))
          if(folders.get(file.getParents().get(0)).equals(parent))
            return true;
    }

    return false;
  }

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

  public static void updateFolders(IOFile file) throws IOException {
    try(BufferedWriter writer = new BufferedWriter(new FileWriter(folderOrigin, true))) {
      writer.newLine();
      
      writer.write(DriveSearch.getParentId(
        IOFile.parentFolder(
          IOFile.getOriginal(file)
          )
        ) +
        "      " + IOFile.parentFolder(IOFile.getOriginal(file))
      );

      writer.close();
    }
  }
}