package com.example.fileexplorer;

/**
 * Item class that holds file and icon
 *
 * @author Daniel Burkhart.
 */
class Item {

    public String file;
    int icon;

    /**
     * Constructor that makes file and icon
     *
     * @param file The current file
     * @param icon That file's icon
     */
    Item(String file, Integer icon) {
        this.file = file;
        this.icon = icon;
    }

    /**
     * To string method of file
     *
     * @return A string representation of file.
     */
    @Override
    public String toString() {
        return file;
    }
}
