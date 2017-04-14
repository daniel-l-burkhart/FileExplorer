package model;

import java.io.File;

/**
 * Folder Item
 */
public class FolderItem {

    private File file;
    private String displayName;

    /**
     * Constructor
     *
     * @param file        The File object
     * @param displayName The displayName
     */
    public FolderItem(File file, String displayName) {
        if (file == null || displayName == null) {
            throw new IllegalArgumentException("Something is null.");
        }
        this.file = file;
        this.displayName = displayName;
    }

    /**
     * The File of the item.
     *
     * @return The file
     */
    public File getFile() {
        return this.file;
    }

    /**
     * The display name
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return this.displayName;
    }

}
