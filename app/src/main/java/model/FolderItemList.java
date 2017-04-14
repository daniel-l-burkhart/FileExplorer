package model;

import java.io.File;
import java.util.ArrayList;

/**
 * List of folder Items
 */
public class FolderItemList {

    private ArrayList<FolderItem> list;

    /**
     * Constructor
     */
    public FolderItemList() {
        this.list = new ArrayList<>();
    }

    /**
     * Adds an item to the list.
     *
     * @param item The FolderItem in question
     */
    public void add(FolderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item is null");
        }
        this.list.add(item);
    }

    /**
     * Gets the FoldetItem from File
     *
     * @param file The File
     * @return Returns the folderItem
     */
    public FolderItem getFolderItemFromFile(File file) {
        return this.findItem(file, "");
    }

    /**
     * Gets the FolderItem from displayName
     *
     * @param displayName The name of the file
     * @return The FolderItem object
     */
    public FolderItem getFolderFromDisplayName(String displayName) {
        return this.findItem(null, displayName);
    }

    private FolderItem findItem(File file, String displayName) {

        FolderItem returnItem = null;

        if (file == null) {
            for (FolderItem item : this.list) {
                if (item.getDisplayName().equals(displayName)) {
                    returnItem = item;
                }
            }
        } else if (displayName.equals("")) {
            for (FolderItem item : this.list) {
                if (item.getFile().equals(file)) {
                    returnItem = item;
                }
            }
        }

        return returnItem;
    }
}
