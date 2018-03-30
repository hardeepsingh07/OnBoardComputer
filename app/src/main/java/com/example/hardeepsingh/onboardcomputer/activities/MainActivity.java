package com.example.hardeepsingh.onboardcomputer.activities;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hardeepsingh.onboardcomputer.R;
import com.example.hardeepsingh.onboardcomputer.adapters.BuildingAdapter;
import com.example.hardeepsingh.onboardcomputer.handlers.OnItemClickListener;
import com.example.hardeepsingh.onboardcomputer.models.Building;
import com.example.hardeepsingh.onboardcomputer.speech.ConversionDelegate;
import com.example.hardeepsingh.onboardcomputer.speech.SpeechDialogType;
import com.example.hardeepsingh.onboardcomputer.speech.SpeechToTextConverter;
import com.example.hardeepsingh.onboardcomputer.speech.TextToSpeechConverter;
import com.example.hardeepsingh.onboardcomputer.utils.OnBoardUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OnItemClickListener, ConversionDelegate {

    private static final int REQ_CODE_SPEECH_INPUT_W_DIALOG = 200;
    private static final String BUILDING_FILE = "buildings.xml";
    private RecyclerView recycleView;
    private TextView s_buildingName;
    private TextView s_buildingDescription;
    private ProgressBar progressBar;

    private ArrayList<Building> buildings;
    private BuildingAdapter buildingAdapter;
    private GoogleMap mMap;
    private AlertDialog alert;

    private Building selectedBuilding;
    private SpeechToTextConverter speechToTextConverter;
    private TextToSpeechConverter textToSpeechConverter;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //Hide Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Welcome to Cal Poly Pomona Transit");

        //Get Buildings Data from XML
        readBuildingsData();

        //Fill up the Recycler View List
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanCount(2);
        recycleView = findViewById(R.id.building_recycle_view);
        s_buildingName = findViewById(R.id.main_building_name);
        s_buildingDescription = findViewById(R.id.main_building_description);
        progressBar = findViewById(R.id.progress_bar_main);
        recycleView.setLayoutManager(gridLayoutManager);
        buildingAdapter = new BuildingAdapter(buildings, this);
        recycleView.setAdapter(buildingAdapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mainMap);
        mapFragment.getMapAsync(this);

        //Speech Recognition is Required to be used only from Main Thread so it has to be initialized here along with helper classes
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        SpeechToTextConverter.CustomRecognitionListener listener = new SpeechToTextConverter.CustomRecognitionListener();
        speechRecognizer.setRecognitionListener(listener);
        speechToTextConverter = new SpeechToTextConverter(this);
        textToSpeechConverter = new TextToSpeechConverter(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        selectedBuilding = null;
        updateSelectedBuildingInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * User Actions
     */
    public void startOver(View view) {
        s_buildingName.setText("");
        s_buildingDescription.setText("");
    }

    public void start(View view) {
        showActivityDialog();
    }


    /**
     * Handle Result from Activity Start
     * For Google Maps, Speech Recognition Engine
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT_W_DIALOG: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    cipherSpeechInput(result.get(0));
                }
                break;
            }

        }
    }

    public void showActivityDialog() {
        if(selectedBuilding != null) {
            CharSequence colors[] = new CharSequence[]{"Location Update with Google Path Generator",
                    "Simulate Location Updates with Google Path Generator",
                    "Location Update with WayPoints provided by File",
                    "Simulate Location Update with WayPoints provide by File"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick a Transit Type");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            startActivity(WaypointRoute.createIntent(MainActivity.this, selectedBuilding, null, false));
                            break;
                        case 1:
                            startActivity(WaypointRoute.createIntent(MainActivity.this, selectedBuilding, null, true));
                            break;
                        case 2:
                            startActivity(WaypointRoute.createIntent(MainActivity.this, selectedBuilding, "blg8_to_blg9.txt", false));
                            break;
                        case 3:
                            startActivity(WaypointRoute.createIntent(MainActivity.this, selectedBuilding, "blg8_to_blg9.txt", true));
                            break;
                    }
                }
            });
            builder.show();
        } else {
            Toast.makeText(this, "Please make a building selection!!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle Click on Building within RecyclerView
     *
     * @param building
     */
    @Override
    public void onClick(Building building) {
        this.selectedBuilding = building;
        makeConfirmAlertDialog(this, "Did you choose " + selectedBuilding.getName() + "?");
    }

    /**
     * Menu Initialization and Delegations
     * Handle Search Engine in Menu Items and Update RecyclerView Accordingly
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);

        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setSubmitButtonEnabled(true);

        //Custom EditText for SearchView
        EditText searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.colorIcons));
        searchEditText.setHintTextColor(getResources().getColor(R.color.colorIcons));
        searchEditText.setTextSize(30f);
        searchEditText.setTypeface(Typeface.SERIF);

        // Search Manager for Search View
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Query Search View
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buildingAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                buildingAdapter.filter(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_mic:
                //Deliver message then listen for speech input by starting a speech dialog
                showProgressBar();
                textToSpeechConverter.speakMessage("Speak Desired Building Number", SpeechDialogType.DIALOG);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Animate Camera To Cal Poly Pomona
        LatLng calPolyPomona = new LatLng(34.0565, -117.8215);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(calPolyPomona, 17));
    }

    public void placeMarkerOnSelection() {
        mMap.clear();
        LatLng calPolyPomona = new LatLng(selectedBuilding.getLatitude(), selectedBuilding.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(calPolyPomona)
                .title(selectedBuilding.getName()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(calPolyPomona, 17));
    }


    public void makeConfirmAlertDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                updateSelectedBuildingInfo();
                placeMarkerOnSelection();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (speechToTextConverter != null) {
                    hideProgressBar();
                    speechToTextConverter.stopListening(speechRecognizer);
                }
            }
        });

        alert = builder.create();
        alert.show();
        alert.getWindow().getAttributes();

        TextView textViewMessage = alert.findViewById(android.R.id.message);
        Button buttonPositive = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        Button buttonNegative = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        textViewMessage.setTypeface(Typeface.SERIF);
        textViewMessage.setGravity(Gravity.CENTER);
        textViewMessage.setTextSize(30);

        buttonPositive.setTypeface(Typeface.SERIF);
        buttonPositive.setTextSize(25);

        buttonNegative.setTypeface(Typeface.SERIF);
        buttonNegative.setTextSize(25);

        //Speak Dialog Message and start listening when message delivered
        showProgressBar();
        textToSpeechConverter.speakMessage(message, SpeechDialogType.WITHOUT_DIALOG);
    }

    public void readBuildingsData() {
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(BUILDING_FILE);
        } catch (IOException e) {
            Log.e("Get XML Assets", e.getMessage());
        }
        buildings = OnBoardUtil.parseXML(inputStream);
    }

    public void updateSelectedBuildingInfo() {
        if (selectedBuilding != null) {
            s_buildingName.setText(selectedBuilding.getName());
            s_buildingDescription.setText(selectedBuilding.getDescription());
        } else {
            s_buildingName.setText("");
            s_buildingDescription.setText("");
        }
    }

    /**
     * Handle Speech Result with Google Dialog sent by onActivityResult
     * @param speechResult
     */
    public void cipherSpeechInput(String speechResult) {
        for (Building b : buildings) {
            if (b.getName().toLowerCase().contains(speechResult) || b.getFullName().toLowerCase().contains(speechResult)) {
                selectedBuilding = b;
                makeConfirmAlertDialog(this, "Did you say " + b.getName() + "?");
                return;
            } else {
                showProgressBar();
                textToSpeechConverter.speakMessage("I am sorry I didn't get that! Try again!", SpeechDialogType.DIALOG);
            }
        }
    }

    /**
     * Handle Without Dialog Speech Results for Alert Dialog
     * @param results
     */
    public void handleAlertDialogSpeechResults(ArrayList<String> results) {
        textToSpeechConverter.finish();
        if (results != null) {
            if (results.contains("yes") || results.contains("Yes") || results.contains("YES")) {
                updateSelectedBuildingInfo();
                placeMarkerOnSelection();
                alert.dismiss();
            } else if (results.contains("no") || results.contains("No") || results.contains("NO")) {
                if (alert != null) {
                    alert.dismiss();
                }
            } else {
                showProgressBar();
                textToSpeechConverter.speakMessage("I am sorry I didn't get that! Try again!", SpeechDialogType.WITHOUT_DIALOG);
            }
        }
    }

    /**
     * Handle Result for Custom Speech Recognition
     * Text To Speech Return Result on onCompletion()
     * Speech To Text Return Result on onSuccess()
     *
     * @param results
     */
    @Override
    public void onSuccess(ArrayList<String> results) {
        handleAlertDialogSpeechResults(results);
    }

    @Override
    public void onCompletion(SpeechDialogType speechDialogType) {
        hideProgressBar();
        switch (speechDialogType) {
            case DIALOG:
                speechToTextConverter.speechWithDialog(REQ_CODE_SPEECH_INPUT_W_DIALOG);
                break;
            case WITHOUT_DIALOG:
                speechToTextConverter.startListening(speechRecognizer);
                break;
        }
    }

    @Override
    public void onErrorOccurred(String errorMessage) {
        Log.e("SpeechRecognition: ", errorMessage);
    }
}