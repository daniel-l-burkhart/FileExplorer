package com.example.fileexplorer;

class Item {

    public String file;
    int icon;

    Item(String file, Integer icon) {
        this.file = file;
        this.icon = icon;
    }

    @Override
    public String toString() {
        return file;
    }
}
