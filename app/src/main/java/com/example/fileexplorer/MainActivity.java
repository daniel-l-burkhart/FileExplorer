package com.example.fileexplorer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import IO.FileOperations;
import model.FolderItem;
import model.FolderItemList;
import model.Item;

/**
 * Main Activity class that runs app
 *
 * @author Daniel Burkhart
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "F_PATH";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    private ListView listView;
    private ListAdapter adapter;
    private GridView gridView;
    private FloatingActionButton newFolderButton;
    private Typeface tf;

    private ArrayList<String> str = new ArrayList<>();
    private ArrayList<File> myFolders = new ArrayList<>();
    private FolderItemList myFolderItems = new FolderItemList();
    private Boolean firstLvl = true;
    private Item[] fileList;
    private File path = new File(Environment.getExternalStorageDirectory() + "");
    private File basePath = new File(Environment.getExternalStorageDirectory() + "");

    private String chosenFile;
    private FileOperations fileOps;
    private boolean wasMoveClicked;
    private boolean wasCopyClicked;

    /**
     * On create method that begins app
     *
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.fileOps = new FileOperations();
        setContentView(R.layout.activity_main);

        this.listView = (ListView) findViewById(R.id.listView);
        this.gridView = (GridView) findViewById(R.id.gridView);
        this.newFolderButton = (FloatingActionButton) findViewById(R.id.newFolderButton);
        this.tf = Typeface.createFromAsset(getAssets(), "helvetica.ttf");

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            String[] permissionsWanted = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(MainActivity.this, permissionsWanted, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        } else {
            onStart();
            Log.d(TAG, path.getAbsolutePath());
        }
    }

    /**
     * Requests permissions for reading and writing storage directory
     *
     * @param requestCode  The request code
     * @param permissions  The array of permissions
     * @param grantResults The results if the user granted or didn't grant permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    loadFileList();
                    onStart();
                    Log.d(TAG, path.getAbsolutePath());

                } else {
                    finish();
                }

            }
        }
    }

    /**
     * On start method that occurs after onCreate
     */
    @Override
    protected void onStart() {
        super.onStart();

        loadFileList();
        this.myFolders = new ArrayList<>();
        getFolders(basePath);

        this.setUpListViewEventHandlers();

        this.setUpGridView();

        this.newFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeFolder();
            }
        });
    }

    /**
     * Sets up the grid view.
     */
    private void setUpGridView() {
        ArrayAdapter<File> folderAdapter = new ArrayAdapter<File>(this,
                android.R.layout.simple_list_item_activated_1, android.R.id.text1, this.myFolders) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);

                textView.setText(myFolderItems.getFolderItemFromFile(myFolders.get(position)).getDisplayName());
                textView.setTypeface(tf);

                return view;
            }
        };

        this.gridView.setAdapter(folderAdapter);

        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String fromPath = path.getPath() + File.separator;
                String toPath = myFolders.get(position).getPath() + File.separator;
                boolean pass = false;

                if (wasMoveClicked) {
                    pass = fileOps.moveFile(fromPath, chosenFile, toPath);
                } else if (wasCopyClicked) {

                    if(new File(toPath+chosenFile).exists()){
                        Toast.makeText(getApplicationContext(), "File already exists in this directory.", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        pass = fileOps.copyFile(fromPath, chosenFile, toPath, chosenFile);
                    }
                }

                if (pass) {
                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                }

                wasMoveClicked = false;
                wasCopyClicked = false;

                onRestart();
                onStart();
                hideMoveView();
            }

        });
    }

    /**
     * Sets up event handlers for list view.
     */
    private void setUpListViewEventHandlers() {
        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenFile = fileList[position].toString();
                File sel = new File(path + "/" + chosenFile);
                if (sel.isDirectory()) {
                    showFolderOptions(sel);
                } else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {
                    String s = str.remove(str.size() - 1);
                    path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));
                    fileList = null;
                    if (str.isEmpty()) {
                        firstLvl = true;
                    }
                    loadFileList();

                    onRestart();
                    onStart();
                    Log.d(TAG, path.getAbsolutePath());

                } else {

                    showFileOptions();
                    loadFileList();
                    onRestart();
                    onStart();
                    Log.d(TAG, path.getAbsolutePath());
                }
            }
        });
    }

    /**
     * Loads files and sets up view in current directory
     */
    private void loadFileList() {

        boolean pass = false;
        try {
            pass = path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }
        if (pass) {
            Log.d(TAG, "pass mkDirs");
        }
        if (!path.exists()) {
            Log.e(TAG, "path does not exist");
            return;
        }
        String[] fList = path.list();
        if (fList == null) {
            return;
        }

        this.setIcons(fList);

        this.checkIfTopLevel();

        this.setUpListView();
    }

    /**
     * Sets icons for files
     *
     * @param fList The list of files.
     */
    private void setIcons(String[] fList) {
        fileList = new Item[fList.length];

        for (int i = 0; i < fList.length; i++) {
            fileList[i] = new Item(fList[i], 0);
            File currFile = new File(path, fList[i]);

            if (currFile.isDirectory()) {
                fileList[i].setIcon(R.drawable.folder_np);
            } else {
                fileList[i].setIcon(R.drawable.file_np);
            }
        }
    }

    /**
     * Checks if the current level is top-level or not, if not adds back button
     */
    private void checkIfTopLevel() {
        if (!firstLvl) {
            Item temp[] = new Item[fileList.length + 1];
            System.arraycopy(fileList, 0, temp, 1, fileList.length);
            temp[0] = new Item("Up", R.drawable.up_one_level_np);
            fileList = temp;
        }
    }

    /**
     * Sets up the list view
     */
    private void setUpListView() {
        adapter = new ArrayAdapter<Item>(this,
                android.R.layout.select_dialog_item, android.R.id.text1,
                fileList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        fileList[position].getIcon(), 0, 0, 0);
                textView.setTypeface(tf);
                int drawablePadding = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(drawablePadding);

                return view;
            }
        };
    }

    /**
     * Opens and sets up view of current directory
     *
     * @param sel The current directory
     */
    private void openDirectory(File sel) {
        firstLvl = false;

        str.add(chosenFile);
        fileList = null;
        path = new File(sel + "");

        loadFileList();

        onRestart();
        onStart();
        Log.d(TAG, path.getAbsolutePath());
    }

    /**
     * Gets all the folders currently in system for moving files
     *
     * @param root The base folder path
     */
    private void getFolders(File root) {

        File[] list = root.listFiles();

        for (File f : list) {

            if (f.isDirectory() && !f.getAbsolutePath().contains(Environment.getExternalStorageDirectory() + "/Android")) {

                this.myFolders.add(f);
                String displayName = f.toString().replace("/storage/emulated/0/", "");
                FolderItem currItem = new FolderItem(f, displayName);
                this.myFolderItems.add(currItem);

                getFolders(f);
            }
        }

    }

    /**
     * Makes a folder in current directory
     */
    private void makeFolder() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Make New Folder");
        alert.setMessage("Type in name of new folder.");

        final EditText input = new EditText(this);
        input.setSingleLine();
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                if(value.trim().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Invalid name", Toast.LENGTH_LONG).show();
                } else {

                    File newDirectory = new File(path, value);
                    if (!newDirectory.exists()) {
                        if (newDirectory.mkdir()) {
                            Toast.makeText(getApplicationContext(), "Folder successfully made!", Toast.LENGTH_LONG).show();
                            onRestart();
                            onStart();
                        } else {
                            Toast.makeText(getApplicationContext(), "Folder was not made. Try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();

    }

    /**
     * Deletes file or folder
     */
    private void deleteFileFolder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete");
        alert.setMessage("Are you sure you want to delete?\n(You cannot delete system folders).");
        alert.setIcon(R.drawable.delete);

        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                boolean pass = fileOps.deleteFile(path.getPath(), chosenFile);
                if (pass) {
                    Toast.makeText(getApplicationContext(), "Successfully deleted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
                loadFileList();
                onRestart();
                onStart();

                dialog.dismiss();
            }

        });

        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /**
     * Shows the options that can be performed on folder
     *
     * @param sel Current file
     */
    private void showFolderOptions(File sel) {

        final File folder = sel;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Folder Operations");

        builder.setItems(new CharSequence[]{"Open", "Move", "Copy", "Delete", "Rename"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                openDirectory(folder);
                                break;
                            case 1:
                                wasMoveClicked = true;
                                showMoveView();
                                break;
                            case 2:
                                wasCopyClicked = true;
                                showMoveView();
                                break;
                            case 3:
                                deleteFileFolder();
                                break;
                            case 4:
                                renameFileFolder();
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * Shows the options to be performed on file.
     */
    private void showFileOptions() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File Operations");

        builder.setItems(new CharSequence[]{"Move", "Copy", "Delete", "Rename"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                wasMoveClicked = true;
                                showMoveView();
                                break;
                            case 1:
                                wasCopyClicked = true;
                                showMoveView();
                                break;
                            case 2:
                                deleteFileFolder();
                                break;
                            case 3:
                                renameFileFolder();
                                break;
                        }
                    }
                });

        builder.create().show();
    }

    /**
     * Renames a file or folder
     */
    private void renameFileFolder(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Rename file");
        alert.setMessage("Type in new name.");

        final EditText input = new EditText(this);
        input.setSingleLine();
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                boolean pass = false;
                String value = input.getText().toString();

                if (value.trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Invalid name", Toast.LENGTH_LONG).show();
                } else {

                    if (new File(path + File.separator + value).exists()) {
                        Toast.makeText(getApplicationContext(), "Name already exists.", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        pass = fileOps.renameFile(path.getPath(), chosenFile, value);
                    }

                    if (pass) {
                        Toast.makeText(getApplicationContext(), "File renamed", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Rename unsuccessful", Toast.LENGTH_LONG).show();
                    }

                    loadFileList();
                    onRestart();
                    onStart();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    /**
     * Shows move view and hides main view
     */
    private void showMoveView() {
        this.newFolderButton.setVisibility(View.GONE);
        this.listView.setVisibility(View.GONE);
        this.gridView.setVisibility(View.VISIBLE);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Move/Copy");
        alert.setMessage("Select the output directory");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.dismiss();
            }

        });

        alert.show();
    }

    /**
     * Hides move view.
     */
    private void hideMoveView() {
        this.gridView.setVisibility(View.GONE);
        this.listView.setVisibility(View.VISIBLE);
        this.newFolderButton.setVisibility(View.VISIBLE);
    }

}
