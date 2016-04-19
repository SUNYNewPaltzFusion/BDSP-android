//TODO have datatypes option for unique fields
//TODO autoincrement field
//TODO section function - auto increment by date on field
//TODO flagged fields - allow binary
//TODO manual or incremental field - trigger = field, reset = how you want to reset (daily or no reset)
//TODO housekeeping, separate manual and automatically collected
//TODO chase down bug that causes multiple entries when save is pressed
package me.sunyfusion.fuzion;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import android.view.ViewGroup.LayoutParams;

import cz.msebera.android.httpclient.Header;

import android.widget.RelativeLayout;
import android.widget.Button;
import android.graphics.Color;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String SUBMIT_URL = "http://www.sunyfusion.me/sub_test/index.php";
    Uri imgUri;
    boolean sendGPS = false;
    double latitude = -1;
    double longitude = -1;
    double gps_acc = -1000;
    int GPS_FREQ = 2000;
    LocationManager locationManager;
    ArrayList<View> fields;
    LinearLayout.LayoutParams buttonDetails;
    LinearLayout.LayoutParams editTextParams;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    EditText mText;
    ContentValues values;
    private static LinearLayout layout;
    private static LinearLayout a_view;


    Boolean cameraInUse = false;
    Boolean gpsLocationInUse = false;
    ImageButton camera;
    Button gpsLocation;
    ArrayList<ImageButton> uniqueButtonsReferences = new ArrayList<ImageButton>();
    private final int MenuItem_EditId = 1, MenuItem_DeleteId = 0;
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add("HELLO");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return false;
        }
    }
*/
    /**
     * Runs on startup, creates the layout when the activity is created.
     * This is essentially the "main" method.
     *
     * @param savedInstanceState restores previous state on entry
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar a = getSupportActionBar();
        a_view = new LinearLayout(this);
        a_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        a_view.setOrientation(LinearLayout.HORIZONTAL);
        a_view.setGravity(Gravity.RIGHT);
        ShapeDrawable rectangle = new ShapeDrawable ();
        rectangle.setShape(new RectShape());
        rectangle.setIntrinsicHeight(100);
        rectangle.setIntrinsicWidth(100);
        rectangle.getPaint().setColor(Color.GREEN);
        TextView menu_title = new TextView(this);
        a.setCustomView(a_view);
        a.setDisplayShowCustomEnabled(true);
        values = new ContentValues();
        layout = new LinearLayout(this);
        fields = new ArrayList<View>();
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(Color.CYAN);
        layout.setBackgroundColor(Color.CYAN);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.VERTICAL);


        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        scroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT));
        scroll.addView(layout);


        setContentView(scroll);


        buttonDetails = new
        LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonDetails.setMargins(0, 10, 0, 10);

        editTextParams = new
                LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.setMargins(0, 10, 0, 10);

        dbHelper = new DatabaseHelper(this);
        buildSubmit();
        buildSave();
        dispatch();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    /**
     * Receives all intents returned by activities when returning to this activity.
     * Right now, this only processes the intent returned by the getImage() method
     * below
     *
     * @param requestCode integer that identifies the type of activity that the intent belongs to
     * @param resultCode  status code returned by the exiting activity
     * @param data        the intent that is being returned by the exiting activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Img saved to:\n" +
                    data.getData(), Toast.LENGTH_LONG).show();
            imgUri = data.getData();
        }
    }

    /**
     * Creates a file in the application's directory on the device, assigns a timestamped file
     * name, creates a new Image Capture intent, and starts it. the overridden method
     * "onActivityResult" handles the data returned by the camera intent.
     *
     * @return void
     */
    private void getImage() {
        File f = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File s = new File(f.getPath() + File.separator + timeStamp + ".jpg");
        Uri uri = Uri.fromFile(s);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void dispatch() {
        String Type;
        String Name;
        Scanner infile = null;
        db = dbHelper.getWritableDatabase();

        try {
            infile = new Scanner(this.getAssets().open("buildApp.txt"));   // scans File
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReadFromInput readFile = new ReadFromInput(infile);

        do {
            try {
                readFile.getNextLine();
                readFile.ReadLineCollectInfo();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Type = readFile.getType();

            switch (Type) {
                case "camera":
                    if (readFile.getAnswer() == 1) {
                        buildCamera(readFile.getArgs());
                        System.out.println(Type + " " + readFile.getAnswer());
                    }
                    break;

                case "gpsLoc":
                    if (readFile.getAnswer() == 1) {
                        buildGpsLoc(readFile.getArgs());
                        System.out.println(Type + " " + readFile.getAnswer());
                    }
                    break;

                case "gpsTracker":
                    if (readFile.getAnswer() == 1) {
                        buildGpsTracker();
                        System.out.println(Type + " " + readFile.getAnswer());
                    }
                    break;

                case "unique":
                    Name = readFile.getUniqueName();
                    buildUniqueName(Name);
                    System.out.println(Type + " " + readFile.getUniqueName());
                    break;

            }
        }
        while (!Type.equals("endFile"));
        Cursor dbCursor = db.query("tasksTable", null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();
        for(String s : columnNames){
            System.out.println(s);
        }
    }

    public void buildCamera(String[] args) {
        // build button
        // add column to SQLite table
        String name = args[2];
        final ImageButton cameraButton = new ImageButton(this);
        dbHelper.addColumn(db, name, "TEXT");
        camera = cameraButton;
        cameraInUse = true;

        //cameraButton.setBackgroundColor(Color.BLACK);
        //cameraButton.setTextColor(Color.WHITE);
        //cameraButton.setText("Camera");
        cameraButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
              LayoutParams.WRAP_CONTENT));
        //cameraButton.setGravity(Gravity.CENTER);
        cameraButton.setImageResource(android.R.drawable.ic_menu_camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getImage();
                cameraButton.setBackgroundColor(Color.YELLOW);
                //cameraButton.setTextColor(Color.BLACK);
            }
        });
        a_view.addView(cameraButton);

    }

    public void buildGpsLoc(String[] args) {
        // build button
        // add column to SQLite table
        if (args[2] != null) {
            GPS_FREQ = Integer.parseInt(args[2]);
        }
        final Button buildGPSLocButton = new Button(this);

        gpsLocation = buildGPSLocButton;
        gpsLocationInUse = true;

        buildGPSLocButton.setText("GPS Location");
        //buildGPSLocButton.setBackgroundColor(Color.BLACK);
        //buildGPSLocButton.setGravity(Gravity.CENTER);

        buildGPSLocButton.setTextColor(Color.WHITE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_FREQ, 0, locationListener);
        }
        catch(SecurityException e){

        }
        //buildGPSLocButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        //        LayoutParams.WRAP_CONTENT));

        buildGPSLocButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buildGPSLocButton.setBackgroundColor(Color.YELLOW);
                buildGPSLocButton.setText("REDO GPS LOCATION");
                buildGPSLocButton.setTextColor(Color.BLACK);
                Toast toast = Toast.makeText(getApplicationContext(),"Long:" + longitude + " Lat:" + latitude,Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        a_view.addView(buildGPSLocButton);
    }

    public void buildGpsTracker() {
        sendGPS = true;
    }

    public void buildUniqueName(final String name) {
        // build unique button
        // add column to SQLite table
       // Button uniqueButton = (Button) findViewById(R.id.inputButtons);

        dbHelper.addColumn(db, name, "TEXT");

        LinearLayout box = new LinearLayout(this);   // layout to wrap the whole thing
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.BLACK);    // Can use transparent if you want for the background color.
        box.setPadding(0, 10, 0, 10);

        LinearLayout l = new LinearLayout(this);   // layout for the text entry and the enter button
        l.setOrientation(LinearLayout.HORIZONTAL);
        //l.setPadding(10, 10, 10, 10);

        final EditText t = new EditText(this);    // makes the edit text field

        final ImageButton enterButton = new ImageButton(this);    // Enter Button creation

        uniqueButtonsReferences.add(enterButton);

        enterButton.setImageResource(android.R.drawable.ic_input_add);
        //enterButton.setBackgroundColor(Color.GREEN);

        enterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // action
                //enterButton.setBackgroundColor(Color.YELLOW);
                enterButton.setImageResource(android.R.drawable.ic_menu_edit);

                // test to write to ContentsValue object
                values.put(name, t.getText().toString());
                System.out.println(name + " " + values.get(name).toString());

                t.getText().clear();
            }
        });

        TextView uniqueText = new TextView(this);   // Unique label


        uniqueText.setText(name);
        uniqueText.setBackgroundColor(Color.TRANSPARENT);
        uniqueText.setTextColor(Color.WHITE);
        uniqueText.setPadding(15, 0, 0, 0);


        l.addView(t);
        t.setBackgroundColor(Color.WHITE);
        t.setSingleLine();
        t.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f));


        //fields.add(uniqueText);



        box.addView(uniqueText);
        box.addView(l);
        l.addView(enterButton);


        layout.addView(box, editTextParams);
    }

    public void buildSubmit() {
        // Button uniqueButton = (Button) findViewById(R.id.inputButtons);
        Button submitButton = new Button(this);
        submitButton.setText("Submit All Data");
        submitButton.setBackgroundColor(Color.BLACK);
        submitButton.setTextColor(Color.WHITE);
       // submitButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
       //         LayoutParams.WRAP_CONTENT));

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                Cursor c = dbHelper.queueAll(db);
                c.moveToNext();
                String[] cNames = c.getColumnNames();
                for(String s : cNames){
                    System.out.print(s + ", ");
                }
                System.out.println();
                int cCount = c.getColumnCount();
                while(!c.isAfterLast()){
                    params = new RequestParams();
                    for(int i = 0; i < cCount; i++) {
                        params.add(cNames[i],c.getString(i));
                        System.out.print(c.getString(i) + ", ");
                    }
                    System.out.println();
                    HTTPFunc.doHTTPpost(getApplicationContext(),SUBMIT_URL,params,imgUri);
                    //TODO NOT A SAFE WAY TO DELETE, LOOK TO REVISE
                    db.delete("tasksTable","ID=" + c.getString(0),null);
                    c.moveToNext();
                }


                //TODO add code to write fields and fields.value to cursor
            }
        });

        layout.addView(submitButton, buttonDetails);
    }
    //GPS CODE
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            makeUseOfNewLocation(location);
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    private void makeUseOfNewLocation(Location l){
        latitude = l.getLatitude();
        longitude = l.getLongitude();
        gps_acc = l.getAccuracy();

        if(sendGPS) {
            System.out.printf("Accuracy=%f,Longitude=%f,Latitude=%f\n",gps_acc,longitude,latitude); //Debug
        }
    }
    //END GPS CODE

    public void buildSave() {
        // Button uniqueButton = (Button) findViewById(R.id.inputButtons);
        Button saveButton = new Button(this);
        saveButton.setText("Save");
        saveButton.setBackgroundColor(Color.GREEN);
        saveButton.setTextColor(Color.BLACK);
        // submitButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        //         LayoutParams.WRAP_CONTENT));

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // submit to SQLITE database
                resetButtonsAfterSave();
            }
        });

        layout.addView(saveButton, buttonDetails);
    }

    public void resetButtonsAfterSave()
    {
        db.insert("tasksTable", null, values);
        if (cameraInUse == true)
        {
            camera.setBackgroundColor(Color.BLACK);
            //camera.setTextColor(Color.WHITE);
            camera.setImageResource(android.R.drawable.ic_menu_camera);
        }

        if (gpsLocationInUse == true)
        {
            gpsLocation.setBackgroundColor(Color.BLACK);
            gpsLocation.setTextColor(Color.WHITE);
            gpsLocation.setText("GPS LOCATION");
        }

        for (int i = 0; i < uniqueButtonsReferences.size(); i++)
        {
            //uniqueButtonsReferences.get(i).setBackgroundColor(Color.GREEN);
            uniqueButtonsReferences.get(i).setImageResource(android.R.drawable.ic_input_add);
        }

    }
}


/*
final Button button = (Button) findViewById(R.id.button_id);
button.setOnClickListener(new View.OnClickListener() {
public void onClick(View v) {
*/