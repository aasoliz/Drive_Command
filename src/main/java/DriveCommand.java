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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

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
    Arrays.asList(DriveScopes.DRIVE);

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

  public static void main(String[] args) throws IOException, InterruptedException {
    // Build a new authorized API client service.
    // TODO: Make sure to catch if not authenticated
    Drive service = getDriveService();

    // Name given has to be unique or it could get the wrong folder
    // Name also must be the same in both drive folder and locally
    // Maybe add parent folder so that it can be more precise
    FileList result = service.files().list()
            .setQ("mimeType='application/vnd.google-apps.folder' and name='spring2016' and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name, parents)")
            .execute();

    File file = result.getFiles().get(0);
    
    DriveDirectory dir = new DriveDirectory(file.getName(), file.getId(), true);

    LinkedList<DriveDirectory> root = new LinkedList<DriveDirectory>();
    root.add(dir);

    DriveSearch ds = new DriveSearch(service);
    ds.addChildren(root);

    String[] path = DriveDirectory.getSuperPath(ds, dir, file.getParents().get(0));

    // DriveSearch ds = new DriveSearch(service);
    // ds.readKnown();

    // IOFile root = new IOFile("/home/aasoliz/Documents/Classes/spring2016");

    // DriveUpload up = new DriveUpload();
    // up.types();

    // LinkedHashMap<IOFile, String> adding = IOFile.deep(root, ds, up);

    // Boolean flag = true;
    // for(Map.Entry<IOFile, String> entry : adding.entrySet())
    //   if(flag) {
    //     System.out.println(IOFile.getName(entry.getKey()) + " " + entry.getValue());
    //     DriveUpload.uploadFile(entry.getKey(), entry.getValue(), service);
    //     flag = false; 
    //   }
  }
}
