package IO;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

/**
 * Operations that can be done on files
 *
 * @author Daniel Burkhart.
 */
public class FileOperations {

    private InputStream in;
    private OutputStream out;
    private boolean opStatus;

    /**
     * Constructor of class
     */
    public FileOperations() {
        this.in = null;
        this.out = null;
        this.opStatus = false;
    }

    /**
     * Moves folder
     *
     * @param inputPath  The input path
     * @param inputFile  The input file
     * @param outputPath The output path
     * @return True if successful, false otherwise
     */
    public boolean moveFolder(String inputPath, String inputFile, String outputPath) {

        this.opStatus = false;

        File from = new File(inputPath + File.separator + inputFile);
        File to = new File(outputPath + File.separator + inputFile);
        this.opStatus = from.renameTo(to);

        return this.opStatus;
    }

    /**
     * Moves the inputFile from the inputPath to the outpuPath
     *
     * @param inputPath  Input path
     * @param inputFile  Input file
     * @param outputPath Output path
     * @return True if move was successful, false otherwise
     */
    public boolean moveFile(String inputPath, String inputFile, String outputPath) {

        this.opStatus = false;

        if (inputPath.equals(outputPath)) {
            this.opStatus = true;
            return this.opStatus;
        }

        try {
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            this.in = new FileInputStream(inputPath + inputFile);
            this.out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = this.in.read(buffer)) != -1) {
                this.out.write(buffer, 0, read);
            }
            this.in.close();
            this.in = null;

            this.out.flush();
            this.out.close();
            this.out = null;

            new File(inputPath + inputFile).delete();

            this.opStatus = true;

        } catch (FileNotFoundException fnfe1) {
            Log.e("File Not Found", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("General Exception", e.getMessage());
        }

        return this.opStatus;
    }

    /**
     * Renames a folder and updates all of its files paths.
     *
     * @param from The from folder name
     * @param to   The new folder name.
     * @return True if successful, false otherwise.
     */
    public boolean renameFolder(File from, File to) {

        if (!from.isDirectory()) {

            this.opStatus = this.renameFile(from, to);

        } else {
            if (!to.exists()) {
                to.mkdirs();
            }
            String[] children = from.list();
            for (String aChildren : children) {
                renameFolder(new File(from, aChildren), new File(to, aChildren));
            }
            this.opStatus = from.delete();
        }

        return this.opStatus;
    }

    /**
     * Renames file to new name
     *
     * @param from The from name.
     * @param to   The to name
     * @return True if successful, false otherwise.
     */
    public boolean renameFile(File from, File to) {

        return from.renameTo(to);
    }

    /**
     * Copies directory.
     *
     * @param sourceLocation The source location
     * @param targetLocation The output directory
     * @return True if succesfull, false otherwise
     */
    public boolean copyDirectory(File sourceLocation, File targetLocation) {

        if (!sourceLocation.isDirectory()) {

            this.opStatus = copyFile(sourceLocation, targetLocation);

        } else if (sourceLocation.equals(targetLocation)) {
            this.opStatus = false;
        } else {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }
            String[] children = sourceLocation.list();
            for (String aChildren : children) {
                this.copyDirectory(new File(sourceLocation, aChildren), new File(targetLocation, aChildren));
            }
        }
        return this.opStatus;
    }

    /**
     * @param sourceLocation The source location of the file
     * @param targetLocation The destination location
     * @return True if successful, false otherwise.
     */
    public boolean copyFile(File sourceLocation, File targetLocation) {

        this.opStatus = false;

        try {
            this.in = new FileInputStream(sourceLocation);
            this.out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = this.in.read(buf)) > 0) {
                this.out.write(buf, 0, len);
            }
            this.in.close();
            this.out.close();
            this.opStatus = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return this.opStatus;
    }

    /**
     * Deletes a directory
     *
     * @param path The path to delete
     * @return True if successful, false otherwise.
     */
    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }

    /**
     * Deletes a file
     *
     * @param inputPath The input path
     * @param inputFile The input file
     * @return True if file was deleted correctly.
     */
    public boolean deleteFile(String inputPath, String inputFile) {

        File file = new File(inputPath, inputFile);

        if (file.isDirectory()) {
            this.opStatus = FileOperations.deleteDirectory(file);
        }

        if (file.exists()) {
            try {
                this.opStatus = file.delete();
            } catch (SecurityException exception) {
                Log.e(TAG, "File could not be deleted.");
            }
            if (this.opStatus) {
                Log.d(TAG, "File deleted");
            }
        }
        return this.opStatus;
    }
}
