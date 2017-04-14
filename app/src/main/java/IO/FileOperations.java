package IO;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    public FileOperations() {
        this.in = null;
        this.out = null;
        this.opStatus = false;
    }

    /**
     * Copies the inputFile from the inputPath to the outputPath
     *
     * @param inputPath  The input path
     * @param inputFile  The input file
     * @param outputPath The output path
     * @return True if copy was successful, false otherwise
     */
    public boolean copyFile(String inputPath, String inputFile, String outputPath) {

        this.opStatus = false;

        try {
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            out.flush();
            out.close();
            this.opStatus = true;

        } catch (FileNotFoundException fnfe1) {
            Log.e("File Not Found", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("General Exception", e.getMessage());
        }

        return opStatus;

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

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

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
     * Deletes a file
     *
     * @param inputPath The input path
     * @param inputFile The input file
     * @return True if file was deleted correctly.
     */
    public boolean deleteFile(String inputPath, String inputFile) {

        File file = new File(inputPath, inputFile);
        boolean didItWork = false;

        if (file.exists()) {

            try {
                didItWork = file.delete();

            } catch (SecurityException exception) {
                Log.e(TAG, "File could not be deleted.");
            }

            if (didItWork) {
                Log.d(TAG, "File deleted");
            }
        }

        return didItWork;
    }

}
