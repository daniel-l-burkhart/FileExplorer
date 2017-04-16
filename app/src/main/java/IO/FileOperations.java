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

    public boolean copyFile(String inputPath, String inputFile, String outputPath, String outputFile) {


        this.opStatus = false;
        try {
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + outputFile);

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
    */

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

    public boolean moveFolder(String inputPath, String inputFile, String outputPath){
        return this.moveCopyFolder(inputPath, inputFile, outputPath, null);
    }

    public boolean copyFolder(String inputPath, String inputFile, String outputPath, String outputFile){
        return this.moveCopyFolder(inputPath, inputFile, outputPath, outputFile);
    }

    private boolean moveCopyFolder(String intputPath, String inputFile, String outputPath, String outputFile){

        this.opStatus = false;

        if(outputFile == null){
            File from = new File(intputPath + File.separator + inputFile);
            File to = new File(outputPath + File.separator + inputFile);
            from.renameTo(to);
        } else{
            File to = new File(outputPath + File.separator+ outputFile);
            this.opStatus = to.mkdir();
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

        if(file.isDirectory()){
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

    public boolean renameFile(File from, File to) {

        return from.renameTo(to);
    }

    public boolean renameFolder(File from, File to){

        System.out.println(from.toString());
        System.out.println(to.toString());
        if(!from.isDirectory()){

            this.opStatus = this.renameFile(from, to);

        } else{
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

    public boolean copyDirectory(File sourceLocation, File targetLocation) {

        if(!sourceLocation.isDirectory()){

             this.opStatus = copyFile(sourceLocation, targetLocation);

        } else {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }
            String[] children = sourceLocation.list();
            for (String aChildren : children) {
                copyDirectory(new File(sourceLocation, aChildren), new File(
                        targetLocation, aChildren));
            }
        }

        return this.opStatus;
    }

    /**
     * @param sourceLocation
     * @param targetLocation
     * @throws FileNotFoundException
     * @throws IOException
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

        }catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            System.out.println(e.getMessage());
        }

        return this.opStatus;
    }



}
