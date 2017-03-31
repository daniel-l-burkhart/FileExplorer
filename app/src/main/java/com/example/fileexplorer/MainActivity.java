package com.example.fileexplorer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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

import static android.widget.Toast.makeText;

/**
 * Main Activity class that runs app
 *
 * @author Daniel Burkhart
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "F_PATH";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    ArrayList<String> str = new ArrayList<>();
    private ListView listView;
    private GridView gridView;
    private FloatingActionButton newFolderButton;
    private ArrayList<File> myFolders = new ArrayList<>();
    private Boolean firstLvl = true;
    private Item[] fileList;
    private File path = new File(Environment.getExternalStorageDirectory() + "");
    private File basePath = new File(Environment.getExternalStorageDirectory() + "");
    private String chosenFile;
    private ListAdapter adapter;

    /**
     * On create method that begins app
     *
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.listView = (ListView) findViewById(R.id.listView);
        this.gridView = (GridView) findViewById(R.id.gridView);

        this.newFolderButton = (FloatingActionButton) findViewById(R.id.newFolderButton);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        } else {
            onStart();
            Log.d(TAG, path.getAbsolutePath());
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

        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenFile = fileList[position].file;

                File sel = new File(path + "/" + chosenFile);

                if (sel.isDirectory()) {

                    showFolderOptions(sel);

                } else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

                    String s = str.remove(str.size() - 1);

                    path = new File(path.toString().substring(0,
                            path.toString().lastIndexOf(s)));
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


        ArrayAdapter<File> folderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, this.myFolders);


        this.gridView.setAdapter(folderAdapter);

        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                File from = new File(path + File.separator + chosenFile);
                File to = new File(myFolders.get(position) + File.separator + chosenFile);
                boolean pass = from.renameTo(to);

                if (pass) {
                    gridView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);

                    onRestart();
                    onStart();

                    Toast toast = Toast.makeText(getApplicationContext(), "File successfully moved.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    gridView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    onRestart();
                    onStart();
                }

            }

        });

        this.newFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeFolder();
            }
        });
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

        // Checks whether path exists
        if (!path.exists()) {
            Log.e(TAG, "path does not exist");
        } else {

            /*
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory())
                            && !sel.isHidden();

                }
            };

            */


            String[] fList = path.list();

            if (fList == null) {
                return;
            }

            fileList = new Item[fList.length];

            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.directory_icon;
                    Log.d("DIRECTORY", fileList[i].file);
                } else {
                    Log.d("FILE", fileList[i].file);
                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                System.arraycopy(fileList, 0, temp, 1, fileList.length);
                temp[0] = new Item("Up", R.drawable.directory_up);
                fileList = temp;
            }


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
                            fileList[position].icon, 0, 0, 0);


                    int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                    textView.setCompoundDrawablePadding(dp5);

                    return view;
                }
            };
        }

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
                getFolders(f);
            }
        }

    }

    /**
     * Makes a folder in current directory
     */
    private void makeFolder() {

        // Dialog dialog

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Make New Folder");
        alert.setMessage("Type in name of new folder.");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                File newDirectory = new File(path, value);
                if (!newDirectory.exists()) {
                    if (newDirectory.mkdir()) {
                        Context context = getApplicationContext();
                        CharSequence text = "Folder successfully made!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = makeText(context, text, duration);
                        toast.show();
                        onRestart();
                        onStart();
                    } else {
                        Context context = getApplicationContext();
                        CharSequence text = "Folder was not made. Try again.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = makeText(context, text, duration);
                        toast.show();
                    }
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

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
        alert.setMessage("Do you want to Delete");
        alert.setIcon(R.drawable.delete);

        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                File file = new File(path, chosenFile);

                boolean didItWork = false;

                System.out.println(path);
                System.out.println(chosenFile);
                System.out.println(file);

                if (file.exists()) {

                    try {
                        didItWork = file.delete();

                        loadFileList();
                        onRestart();
                        onStart();
                    } catch (SecurityException exception) {
                        System.out.println("File could not be deleted.");
                    }

                    if (didItWork) {
                        Log.d(TAG, "File deleted");
                    }

                }

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

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Folder Options");
        alert.setMessage("Open, Move, or Delete?");
        alert.setIcon(R.drawable.directory_icon);
        alert.setCancelable(true);


        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                deleteFileFolder();
            }

        });
        alert.setNegativeButton("Move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
            }
        });

        alert.setNeutralButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openDirectory(folder);
            }
        });


        alert.show();

    }

    /**
     * Shows the options to be performed on file.
     */
    private void showFileOptions() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("File Options");
        alert.setMessage("You can move or delete files");
        alert.setIcon(R.drawable.file_icon);
        alert.setCancelable(true);

        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                deleteFileFolder();
            }

        });

        alert.setNegativeButton("Move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                System.out.println(myFolders.size());
                listView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
            }
        });


        alert.show();

    }

}
