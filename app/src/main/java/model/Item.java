package model;

/**
 * Item class that holds file and icon
 *
 * @author Daniel Burkhart.
 */
public class Item {

    private String file;
    private int icon;

    /**
     * Constructor that makes file and icon
     *
     * @param file The current file
     * @param icon That file's icon
     */
    public Item(String file, Integer icon) {
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
        return this.file;
    }

    /**
     * Gets the id for the icon
     *
     * @return The int id for icon
     */
    public int getIcon() {
        return this.icon;
    }

    /**
     * Sets the icon
     *
     * @param icon The id of the icon
     */
    public void setIcon(int icon) {
        this.icon = icon;
    }
}
