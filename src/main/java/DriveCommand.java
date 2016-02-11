import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.TreeMap;

public class DriveCommand {
  /** Application name. */
  private static final String APPLICATION_NAME =
    "Drive Command";

  /** Directory to store user credentials for this application. */
  private static final java.io.File DATA_STORE_DIR = new java.io.File(
    System.getProperty("user.home"), ".credentials/drive-command.json");

  /** Global instance of the {@link FileDataStoreFactory}. */
  private static FileDataStoreFactory DATA_STORE_FACTORY;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY =
    JacksonFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  private static HttpTransport HTTP_TRANSPORT;

  /** Global instance of the scopes required by this quickstart.
   *
   * If modifying these scopes, delete your previously saved credentials
   * at ~/.credentials/drive-java-quickstart.json
   */
  private static final List<String> SCOPES =
    Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY);

  static {
    try {
          HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
          DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
          t.printStackTrace();
          System.exit(1);
      }
  }

  /**
   * Creates an authorized Credential object.
   * @return an authorized Credential object.
   * @throws IOException
   */
  public static Credential authorize() throws IOException {
      // Load client secrets.
    InputStream in =
              DriveCommand.class.getResourceAsStream("/client_secret.json");
    GoogleClientSecrets clientSecrets =
              GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

      // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow =
    new GoogleAuthorizationCodeFlow.Builder(
              HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
              .setDataStoreFactory(DATA_STORE_FACTORY)
              .setAccessType("offline")
              .build();
    Credential credential = new AuthorizationCodeInstalledApp(
              flow, new LocalServerReceiver()).authorize("user");
    System.out.println(
              "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
    return credential;
  }

  /**
   * Build and return an authorized Drive client service.
   * @return an authorized Drive client service
   * @throws IOException
   */
  public static Drive getDriveService() throws IOException {
    Credential credential = authorize();
    return new Drive.Builder(
              HTTP_TRANSPORT, JSON_FACTORY, credential)
              .setApplicationName(APPLICATION_NAME)
              .build();
  }

  public static Boolean inDrive(Drive service, String find) throws IOException {
    FileList result = service.files().list()
            .setQ("name='" + find + "'")
            .setSpaces("drive")
            .setFields("files(id, name, parents)")
            .execute();

    for(File file : result.getFiles())
      System.out.printf("Name %s, Id %s, Parents %s\n", file.getName(), file.getId(), file.getParents());

    return true;
  }

  public static void main(String[] args) throws IOException {
    // Build a new authorized API client service.
    Drive service = getDriveService();

    String pageToken = null;

    // TODO: Save in hidden app folder, so don't have to read over and over
    HashMap<String, String> folders = new HashMap<String, String>();

    try(BufferedReader reader = new BufferedReader(new FileReader("/home/aasoliz/Documents/Other/Commands/Drive_Command/src/main/resources/FolderMapping.txt"))) {
      String curr;
      String[] parsed;

      while((curr = reader.readLine()) != null) {
        parsed = curr.split("\\s+");
        folders.put(parsed[0], parsed[1]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    IOFile root = new IOFile("/home/aasoliz/Documents/Classes/spring2016");

    TreeMap<IOFile, String> adding = IOFile.deep(root, service);

/*
    File[] temp = null;

    for(int i = 0; i < files.length; i++) {
      if(!folders.containsKey(files[i]))
        // Add folders/files (Drive API)
      else
        temp = files[i].listFiles();

      for(int j = 0; j < temp.length; j++) {
        if(temp[j].isDirectory())
          if(!folders.containsKey(temp[j]))
            // ADD
        else
          // Check if in parent folder (Drive API)
      }
    }*/
      

  }
}