# Drive_Command

Drive Command is a utility that lets the user upload a specific (on the local system) to Google Drive. It is meant
to keep Google Drive up to date with the local folder adding any new files that were added locally. It is useful 
the tree structure of a folder is complex and new files are added at different places in the tree.

Constraints

    The base/root folder must be present in Drive and have the same name as the local folder

    Does not keep local folder up to date with Drive folder

Running

     Run the provided 'jar' file and select the folder to be uploaded

     Run the 'jar' through the command line providing the full folder path

     Compile source with gradle
         To create jar with dependencies
             'gradle fatJar'
             run jar