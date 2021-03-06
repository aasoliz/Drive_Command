import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.lang.InterruptedException;

import java.sql.Timestamp;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DriveCommand {
  /** Application name. */
  private static final String APPLICATION_NAME = "Drive Command";

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

  /**
  *  Parse JSON that was received from the Drive API
  *  because of an error.
  *
  *  @param e - Exception that was thrown
  *  @return LinkedList with the error code and message
  */
  public static LinkedList getErrorCode(Exception e) throws IOException {
    JsonReader reader = new JsonReader(new StringReader(e.getMessage()));
    reader.setLenient(true);

    // Go past header
    reader.nextString();
    reader.nextString();

    JsonObject o = new JsonParser().parse(reader).getAsJsonObject();

    LinkedList error = new LinkedList();
    error.add(o.getAsJsonPrimitive("code").getAsInt());
    error.add(o.getAsJsonPrimitive("message").getAsString());

    // Convert stacktrace to String
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));

    String[] stack = sw.toString().split("}");
    error.add(stack[stack.length-1]);

    return error;
  }

  /**
  *  (For now) Writes out caught errors to a log file.
  *
  *  @param error - Parsed JSON error code and message
  *  @param e - Exception that was thrown
  *  @throws IOException - If the file was not found or created
  */
  public static void handleError(LinkedList error, Exception e) throws IOException, InterruptedException {
    PrintWriter writer = new PrintWriter(new FileWriter(new java.io.File("log"), true));

    Integer code = (Integer) error.removeFirst();
    String msg = (String) error.removeFirst();
    String stck = (String) error.removeFirst();
    
    // Time error occured
    writer.println((new Timestamp(Calendar.getInstance().getTime().getTime())));
   
    // TODO: Take actions to resolve errors
    switch(code) {
        case 403:
          writer.println("code: " + code + "\nmessage: " + msg + "\nstack trace:");
          writer.println(stck);
          Thread.sleep(1000);
          break;
        case 400:
        case 401:
        case 404:
        case 500:
          writer.println("code: " + code + "\nmessage: " + msg + "\nstack trace:");
          writer.println(stck);
          break;
        default:
          writer.println("code: " + code + "\nmessage: " + msg + "\nstack trace:");
          writer.println(stck);
    }

    writer.flush();
    writer.close();
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO: Make sure to catch if not authenticated
    Drive service = null;

    String drive = null;
    String local = null;

    Boolean commandLine = false;

    if(args.length == 1) {
      local = args[0];
      
      String[] temp = local.split("[\\/]");
      drive = temp[temp.length-1];

      commandLine = true;
    }
    else {
      int option = JOptionPane.showConfirmDialog(null, "Enter a folder path. \nThe folder must have the same name as a folder in your Google Drive folder.", 
                                                 "Information", JOptionPane.OK_CANCEL_OPTION);
      
      if(option == JOptionPane.YES_OPTION) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          local = chooser.getSelectedFile().getAbsolutePath();
          drive = chooser.getSelectedFile().getName();
        }
        else
          System.exit(1);
      }
      else
        System.exit(1);
    }

    // Build a new authorized API client service.
    try {
      service = getDriveService();
    } catch (IOException e) {
      LinkedList code = DriveCommand.getErrorCode(e);

      if(code != null)
        DriveCommand.handleError(code, e);
      else
        e.printStackTrace();
    }

    // Name given has to be unique or it could get the wrong folder
    // Name also must be the same in both drive folder and locally
    // Maybe add parent folder so that it can be more precise?
    FileList result = service.files().list()
            .setQ("mimeType='application/vnd.google-apps.folder' and name='" + drive + "' and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name, parents, modifiedTime)")
            .execute();

    File file = null;
    if(result.getFiles().size() > 0)
      file = result.getFiles().get(0);
    else {

      if(commandLine)
        System.out.println(drive + " was not found in Drive");
      else 
        JOptionPane.showMessageDialog(null, drive + " was not found in Drive", "Information", JOptionPane.OK_OPTION);

      System.exit(2);
    }

    // Initializes a structure that will hold what files are in
    //  the given drive folder
    DriveDirectory dir = new DriveDirectory(file.getName(), file.getId(), file.getModifiedTime(), true);
    LinkedList<DriveDirectory> root = new LinkedList<DriveDirectory>();
    root.add(dir);

    // Indexes the provided Google Drive folder
    DriveSearch ds = new DriveSearch(service, dir);
    ds.addChildren(root);

    DriveUpload up = new DriveUpload(service, dir, commandLine);
    DriveUpload.types(up);

    java.io.File loc = new java.io.File(local);
    if(!loc.exists()) {

      if(commandLine)
        System.out.println("Inputed path was not valid");
      else
        JOptionPane.showMessageDialog(null, "Inputed path was not valid", "Information", JOptionPane.OK_OPTION);

      System.exit(3);
    }

    // Indexes the local folder, checking which files are in Drive
    IOFile rootIO = new IOFile(loc, drive);
    LinkedHashMap<IOFile, String> adding = IOFile.deep(rootIO, ds, up);

    // Upload all the local files that were not in Drive
    for(Map.Entry<IOFile, String> entry : adding.entrySet())
      DriveUpload.uploadFile(entry.getKey(), entry.getValue(), ds);
  }
}
