package com.a3dnonsense.ga.gaweightandbalance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class OpenTemplateActivity extends AppCompatActivity {
    public final static String EXTRA_FILENAME = "com.a3dnonsense.ga.gaweightandbalance.FILENAME";
    private int screenWidth;
    private int screenHeight;
    private String screenOrientation;
    private Boolean allowBackPress;

    private class NamedViews {
        TextView tvTemplateName;
        TextView tvTitleLabelUnits;
        TextView tvWeightBarLabelGross;
        TextView tvWeightBarLabelEmpty;
        TextView tvWeightBarLabelUnits;
        TextView tvMomentBarLabelNoseMin;
        TextView tvMomentBarLabelTailMax;
        TextView tvMomentBarLabelDivide;
        ScrollView svDataInput;
        ScrollView svKeypad;
        TextView tvEntryTotal;
        TextView tvEntryName;
        TextView tvCurrentTextView;
        String weightUnits;
        Boolean firstKeypadNumClicked;
        TextView tvTotalWeightLabel;
        TextView tvTotalWeight;
        TextView tvTotalMomentLabel;
        TextView tvTotalMoment;
        TextView tvGraphWeight;
        TextView tvGraphWeightDashes;
        TextView tvGraphMoment;
        TextView tvGraphMomentDashes;
        TextView tvGraphMomentPipe;
        TextView tvStatus;
        TextView tvReset;
        TextView tvShowGraph;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_template);

        //we'll need to know our general screen size
        //Determine screen size
        Float textViewModifier;
        //String screenSize;
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            //screenSize = "Large";
            textViewModifier = 1.0f;
        }
        else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            //screenSize = "Normal";
            textViewModifier = 0.5f;
        }
        else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            //screenSize = "Small";
            textViewModifier = 0.5f;
        }
        else {
            //screenSize = "Unknown";
            textViewModifier = 1.0f;
        }

        //ease-ification variables
        this.allowBackPress = true;
        final Resources res = getResources();
        Intent intent = getIntent();
        final String fileName = intent.getStringExtra(MainActivity.EXTRA_FILENAME);
        final NamedViews namedViews = new NamedViews();
        int colorClickable = Color.rgb(0,0,180);

        LinearLayout.LayoutParams rowLayoutParamsEvenSpacing = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        TableLayout.LayoutParams tableParamsWrapContent = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams tableRowParamsEvenSpacing = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f);

        //read in template object
        AircraftClass tmpAirCraft = new AircraftClass();
        try {
            FileInputStream fis = this.openFileInput(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            tmpAirCraft = (AircraftClass) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            fatalAlert(getResources().getString(R.string.file_error_title), getResources().getString(R.string.error_opening_file) + " (" + getResources().getString(R.string.try_recreating_file) + "): " + e.toString());
        }
        //read in data file if it exists
        TransientData tmpTransientData;
        try {
            FileInputStream fis = this.openFileInput("data4_" + convertTemplateToFileName(tmpAirCraft.getTemplateName()));
            ObjectInputStream ois = new ObjectInputStream(fis);
            tmpTransientData = (TransientData) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            tmpTransientData = new TransientData();
        }
        final TransientData transientData = tmpTransientData;
        //need to set up some "final" values to avoid errors.
        final AircraftClass aircraft = tmpAirCraft;
        namedViews.weightUnits = aircraft.weightUnits;

        final RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.activity_open_template);
        //relative placement needs to happen after the layout has been painted
        rlMain.post(new Runnable() {
            @Override
            public void run() {
                screenWidth = rlMain.getWidth();
                screenHeight = rlMain.getHeight();
                //move our text labels around to adjust for different screen sizes
                if (screenOrientation.equals("portrait")) {
                    setLocation(namedViews.tvTemplateName, 50f, 0f, "TC");
                    setLocation(namedViews.tvTitleLabelUnits, 50f, 5f, "TC");
                    setLocation(namedViews.tvReset, 3f, 3f, "");
                    setLocation(namedViews.tvShowGraph, 97f, 3f, "UR");
                    setLocation(namedViews.tvWeightBarLabelGross, 6.5f, 34.5f, "LC");
                    setLocation(namedViews.tvWeightBarLabelEmpty, 6.5f, 76f, "LC");
                    setLocation(namedViews.tvWeightBarLabelUnits, 0f, 76.5f, "UL");
                    setLocation(namedViews.tvMomentBarLabelNoseMin, 93.8f, 33f, "RC");
                    setLocation(namedViews.tvMomentBarLabelTailMax, 93.8f, 67.6f, "RC");
                    setLocation(namedViews.tvMomentBarLabelDivide, 100f, 77f, "UR");
                    setLocation(namedViews.tvTotalWeightLabel, 30f, 80f, "BC");
                    setLocation(namedViews.tvTotalWeight, 30f, 80f, "TC");
                    setLocation(namedViews.tvTotalMomentLabel, 70f, 80f, "BC");
                    setLocation(namedViews.tvTotalMoment, 70f, 80f, "TC");
                    setLocation(namedViews.tvGraphWeight, 6.5f, 23.2f, "LC");
                    setLocation(namedViews.tvGraphWeightDashes, 6.1f, 23.2f, "RC");
                    setLocation(namedViews.tvGraphMoment, 93.4f, 72f, "RC");
                    namedViews.tvGraphMomentPipe.setVisibility(View.GONE);
                    namedViews.tvGraphMomentDashes.setVisibility(View.VISIBLE);
                    setLocation(namedViews.tvGraphMomentDashes, 94f, 72f, "LC");
                    setLocation(namedViews.tvStatus, 50f, 95f, "BC");
                    //center our data scrollview and keypad
                    namedViews.svDataInput.getLayoutParams().width = Math.round(screenWidth * 0.6f);
                    namedViews.svDataInput.getLayoutParams().height = Math.round(screenHeight * 0.6f);
                    setLayoutLocation(namedViews.svDataInput, 50f, 45f, "CN");
                    namedViews.svKeypad.getLayoutParams().width = Math.round(screenWidth * 0.6f);
                    namedViews.svKeypad.getLayoutParams().height = Math.round(screenWidth * 0.7f);
                    setLayoutLocation(namedViews.svKeypad, 50f, 50f, "CN");
                    namedViews.svKeypad.setVisibility(View.GONE);
                } else {
                    setLocation(namedViews.tvTemplateName, 49f, 0f, "UR");
                    setLocation(namedViews.tvTitleLabelUnits, 51f, 0f, "UL");
                    setLocation(namedViews.tvReset, 3f, 3f, "");
                    setLocation(namedViews.tvShowGraph, 3f, 97f, "LL");
                    setLocation(namedViews.tvWeightBarLabelGross, 96f, 26f, "RC");
                    setLocation(namedViews.tvWeightBarLabelEmpty, 96f, 90.8f, "RC");
                    setLocation(namedViews.tvWeightBarLabelUnits, 100f, 91.5f, "UR");
                    setLocation(namedViews.tvMomentBarLabelNoseMin, 33.3f, 94f, "BC");
                    setLocation(namedViews.tvMomentBarLabelTailMax, 67.9f, 94f, "BC");
                    setLocation(namedViews.tvMomentBarLabelDivide, 77.2f, 100f, "LL");
                    setLocation(namedViews.tvTotalWeightLabel, 10f, 40f, "BC");
                    setLocation(namedViews.tvTotalWeight, 10f, 40f, "TC");
                    setLocation(namedViews.tvTotalMomentLabel, 30f, 40f, "BC");
                    setLocation(namedViews.tvTotalMoment, 30f, 40f, "TC");
                    setLocation(namedViews.tvGraphWeight, 96f, 8f, "RC");
                    setLocation(namedViews.tvGraphWeightDashes, 96f, 8f, "LC");
                    setLocation(namedViews.tvGraphMoment, 30f, 94f, "BC");
                    namedViews.tvGraphMomentDashes.setVisibility(View.GONE);
                    namedViews.tvGraphMomentPipe.setVisibility(View.VISIBLE);
                    setLocation(namedViews.tvGraphMomentPipe, 30f, 90f, "TC");
                    setLocation(namedViews.tvStatus, 20f, 80f, "BC");
                    //center our data scrollview and keypad
                    namedViews.svDataInput.getLayoutParams().width = Math.round(screenWidth * 0.4f);
                    namedViews.svDataInput.getLayoutParams().height = Math.round(screenHeight * 0.7f);
                    setLayoutLocation(namedViews.svDataInput, 62f, 50f, "CN");
                    namedViews.svKeypad.getLayoutParams().width = Math.round(screenWidth * 0.4f);
                    namedViews.svKeypad.getLayoutParams().height = Math.round(screenWidth * 0.7f);
                    setLayoutLocation(namedViews.svKeypad, 62f, 90f, "CN");
                    namedViews.svKeypad.setVisibility(View.GONE);
                }
                updateTotals(aircraft, namedViews, transientData);
            }
        });

        //get our screen orientation for later use
        Display display = ((WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) { //Portrait Mode
            this.screenOrientation = "portrait";
        } else {
            this.screenOrientation = "landscape";
        }

        //set up our text labels
        TextView tvTemplateName = new TextView(this);
        tvTemplateName.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvTemplateName.setText(aircraft.getTemplateName());
        tvTemplateName.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvTemplateName.getTextSize() * textViewModifier * 3);
        rlMain.addView(tvTemplateName);
        namedViews.tvTemplateName = tvTemplateName;

        TextView tvTitleLabelUnits = new TextView(this);
        tvTitleLabelUnits.setLayoutParams(rowLayoutParamsEvenSpacing);
        String tvTitleLabelUnitsString = res.getString(R.string.label_weight) + "=" + aircraft.weightUnits + ", " + res.getString(R.string.label_arm) + "=" + aircraft.armUnits;
        tvTitleLabelUnits.setText(tvTitleLabelUnitsString);
        rlMain.addView(tvTitleLabelUnits);
        namedViews.tvTitleLabelUnits = tvTitleLabelUnits;

        TextView tvWeightBarLabelGross = new TextView(this);
        tvWeightBarLabelGross.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvWeightBarLabelGross.setText(new DecimalFormat("#.##").format(aircraft.maxGross));
        tvWeightBarLabelGross.setTextSize(tvWeightBarLabelGross.getTextSize() * textViewModifier);
        tvWeightBarLabelGross.setEnabled(false);
        rlMain.addView(tvWeightBarLabelGross);
        namedViews.tvWeightBarLabelGross = tvWeightBarLabelGross;

        TextView tvWeightBarLabelEmpty = new TextView(this);
        tvWeightBarLabelEmpty.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvWeightBarLabelEmpty.setText(new DecimalFormat("#.##").format(aircraft.emptyWeight));
        tvWeightBarLabelEmpty.setTextSize(tvWeightBarLabelEmpty.getTextSize() * textViewModifier);
        tvWeightBarLabelEmpty.setEnabled(false);
        rlMain.addView(tvWeightBarLabelEmpty);
        namedViews.tvWeightBarLabelEmpty = tvWeightBarLabelEmpty;

        TextView tvWeightBarLabelUnits = new TextView(this);
        tvWeightBarLabelUnits.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvWeightBarLabelUnits.setText(aircraft.weightUnits);
        tvWeightBarLabelUnits.setTextSize(tvWeightBarLabelUnits.getTextSize() * textViewModifier);
        rlMain.addView(tvWeightBarLabelUnits);
        namedViews.tvWeightBarLabelUnits = tvWeightBarLabelUnits;

        TextView tvMomentBarLabelNoseMin = new TextView(this);
        tvMomentBarLabelNoseMin.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvMomentBarLabelNoseMin.setText(R.string.unset);
        tvMomentBarLabelNoseMin.setTextSize(tvMomentBarLabelNoseMin.getTextSize() * textViewModifier);
        tvMomentBarLabelNoseMin.setEnabled(false);
        rlMain.addView(tvMomentBarLabelNoseMin);
        namedViews.tvMomentBarLabelNoseMin = tvMomentBarLabelNoseMin;

        TextView tvMomentBarLabelTailMax = new TextView(this);
        tvMomentBarLabelTailMax.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvMomentBarLabelTailMax.setText(R.string.unset);
        tvMomentBarLabelTailMax.setTextSize(tvMomentBarLabelTailMax.getTextSize() * textViewModifier);
        tvMomentBarLabelTailMax.setEnabled(false);
        rlMain.addView(tvMomentBarLabelTailMax);
        namedViews.tvMomentBarLabelTailMax = tvMomentBarLabelTailMax;

        TextView tvMomentBarLabelDivide = new TextView(this);
        tvMomentBarLabelDivide.setLayoutParams(rowLayoutParamsEvenSpacing);
        String weightDivideString = "/" + String.valueOf(aircraft.momentDivide);
        tvMomentBarLabelDivide.setText(weightDivideString);
        tvMomentBarLabelDivide.setTextSize(tvMomentBarLabelDivide.getTextSize() * textViewModifier);
        rlMain.addView(tvMomentBarLabelDivide);
        namedViews.tvMomentBarLabelDivide = tvMomentBarLabelDivide;

        TextView tvTotalWeightLabel = new TextView(this);
        tvTotalWeightLabel.setLayoutParams(rowLayoutParamsEvenSpacing);
        String totalWeightLabel = res.getString(R.string.label_weight) + "(" + aircraft.weightUnits + ")";
        tvTotalWeightLabel.setText(totalWeightLabel);
        tvTotalWeightLabel.setTextSize(tvTotalWeightLabel.getTextSize() * textViewModifier);
        rlMain.addView(tvTotalWeightLabel);
        namedViews.tvTotalWeightLabel = tvTotalWeightLabel;

        TextView tvTotalWeight = new TextView(this);
        tvTotalWeight.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvTotalWeight.setText(R.string.unset);
        tvTotalWeight.setTextSize(tvTotalWeight.getTextSize() * textViewModifier * 2);
        rlMain.addView(tvTotalWeight);
        namedViews.tvTotalWeight = tvTotalWeight;

        TextView tvTotalMomentLabel = new TextView(this);
        tvTotalMomentLabel.setLayoutParams(rowLayoutParamsEvenSpacing);
        String totalMomentLabel = res.getString(R.string.label_moment) + "/" + aircraft.momentDivide;
        tvTotalMomentLabel.setText(totalMomentLabel);
        tvTotalMomentLabel.setTextSize(tvTotalMomentLabel.getTextSize() * textViewModifier);
        rlMain.addView(tvTotalMomentLabel);
        namedViews.tvTotalMomentLabel = tvTotalMomentLabel;

        TextView tvTotalMoment = new TextView(this);
        tvTotalMoment.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvTotalMoment.setText(R.string.unset);
        tvTotalMoment.setTextSize(tvTotalMoment.getTextSize() * textViewModifier * 2);
        rlMain.addView(tvTotalMoment);
        namedViews.tvTotalMoment = tvTotalMoment;
        
        TextView tvGraphWeight = new TextView(this);
        tvGraphWeight.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvGraphWeight.setText(R.string.unset);
        tvGraphWeight.setTextSize(tvGraphWeight.getTextSize() * textViewModifier);
        rlMain.addView(tvGraphWeight);
        namedViews.tvGraphWeight = tvGraphWeight;

        TextView tvGraphWeightDashes = new TextView(this);
        tvGraphWeightDashes.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvGraphWeightDashes.setText("---");
        tvGraphWeightDashes.setTextColor(Color.BLUE);
        tvGraphWeightDashes.setTextSize(tvGraphWeightDashes.getTextSize() * textViewModifier * 2);
        rlMain.addView(tvGraphWeightDashes);
        namedViews.tvGraphWeightDashes = tvGraphWeightDashes;
        
        TextView tvGraphMoment = new TextView(this);
        tvGraphMoment.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvGraphMoment.setText(R.string.unset);
        tvGraphMoment.setTextSize(tvGraphMoment.getTextSize() * textViewModifier);
        rlMain.addView(tvGraphMoment);
        namedViews.tvGraphMoment = tvGraphMoment;

        TextView tvGraphMomentDashes = new TextView(this);
        tvGraphMomentDashes.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvGraphMomentDashes.setText("---");
        tvGraphMomentDashes.setTextColor(Color.BLUE);
        tvGraphMomentDashes.setTextSize(tvGraphMomentDashes.getTextSize() * textViewModifier * 2);
        rlMain.addView(tvGraphMomentDashes);
        namedViews.tvGraphMomentDashes = tvGraphMomentDashes;

        TextView tvGraphMomentPipe = new TextView(this);
        tvGraphMomentPipe.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvGraphMomentPipe.setText("|");
        tvGraphMomentPipe.setTextColor(Color.BLUE);
        tvGraphMomentPipe.setTextSize(tvGraphMomentDashes.getTextSize() * textViewModifier * 2);
        rlMain.addView(tvGraphMomentPipe);
        namedViews.tvGraphMomentPipe = tvGraphMomentPipe;

        TextView tvStatus = new TextView(this);
        tvStatus.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvStatus.setText(R.string.unset);
        tvStatus.setLines(2);
        tvStatus.setGravity(Gravity.CENTER_HORIZONTAL);
        tvStatus.setTextSize(tvStatus.getTextSize() * textViewModifier * 1.3f);
        rlMain.addView(tvStatus);
        namedViews.tvStatus = tvStatus;

        TextView tvReset = new TextView(this);
        tvReset.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvReset.setText(R.string.reset);
        tvReset.setLines(2);
        tvReset.setGravity(Gravity.CENTER_HORIZONTAL);
        tvReset.setTextSize(tvReset.getTextSize() * textViewModifier * 1.3f);
        tvReset.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvReset.setClickable(true);
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delete the file
                String fileName = "data4_" + convertTemplateToFileName(aircraft.getTemplateName());
                try {
                    File dir = getFilesDir();
                    File file = new File(dir, fileName);
                    if (!file.exists()) {
                        throw new Exception("File doesn't exist.");
                    }
                    Boolean deleted = file.delete();
                    if (!deleted) {
                        throw new Exception("Deleted returned false.");
                    }
                    Intent intent1 = getIntent();
                    finish();
                    startActivity(intent1);
                } catch (Exception e) {
                    fatalAlert(res.getString(R.string.file_error_title), res.getString(R.string.error_deleting_file) + " '" + fileName + "' : " + e.toString());
                }
            }
        });
        rlMain.addView(tvReset);
        namedViews.tvReset = tvReset;

        TextView tvShowGraph = new TextView(this);
        tvShowGraph.setLayoutParams(rowLayoutParamsEvenSpacing);
        tvShowGraph.setText(R.string.show_graph);
        tvShowGraph.setLines(2);
        tvShowGraph.setGravity(Gravity.CENTER_HORIZONTAL);
        tvShowGraph.setTextSize(tvShowGraph.getTextSize() * textViewModifier * 1.3f);
        tvShowGraph.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvShowGraph.setClickable(true);
        tvShowGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open our graph activity
                view.setEnabled(false);
                Intent templateIntent = new Intent(view.getContext(), ShowGraphActivity.class);
                templateIntent.putExtra(EXTRA_FILENAME, fileName);
                startActivity(templateIntent);
                view.setEnabled(true);
            }
        });
        rlMain.addView(tvShowGraph);
        namedViews.tvShowGraph = tvShowGraph;

        //set up a base container for our input fields.
        ScrollView svDataInput = new ScrollView(this);
        namedViews.svDataInput = svDataInput;
        rlMain.addView(svDataInput);

        LinearLayout llDataInput = new LinearLayout(this);
        llDataInput.setOrientation(LinearLayout.VERTICAL);
        svDataInput.addView(llDataInput);

        //create table rows to hold data (we'll store them in a hashmap so we can sort by arm before adding to our main table
        HashMap<Double, TableRow> hmArmToTblRow = new HashMap<>();

        //mechanical weight inputs
        for (final AircraftClass.mechanicalWeight weight : aircraft.mechanicalWeights) {
            TableRow tblrSingleWeight = new TableRow(this);
            tblrSingleWeight.setLayoutParams(tableParamsWrapContent);
            tblrSingleWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            //layout our name, weight, arm, and moment fields.
            TextView tvSingleWeightName = new TextView(this);
            tvSingleWeightName.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightName.setText(weight.name);
            final String weightName = weight.name;
            tvSingleWeightName.setTextSize(tvSingleWeightName.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightName);

            LinearLayout llSingleWeightWeight = new LinearLayout(this);
            llSingleWeightWeight.setOrientation(LinearLayout.VERTICAL);
            llSingleWeightWeight.setHorizontalGravity(Gravity.END);
            tblrSingleWeight.addView(llSingleWeightWeight);

            TextView tvSingleWeightWeight = new TextView(this);
            tvSingleWeightWeight.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightWeight.setGravity(Gravity.END);
            if (transientData.WtNameToWtDouble.get(weight.name) != null) {
                weight.weight = transientData.WtNameToWtDouble.get(weight.name);
            }
            tvSingleWeightWeight.setText(new DecimalFormat("#.##").format(weight.weight));
            tvSingleWeightWeight.setTextSize(tvSingleWeightWeight.getTextSize() * textViewModifier);
            tvSingleWeightWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
            if (!weightName.equals(res.getString(R.string.empty))) {
                tvSingleWeightWeight.setTextColor(colorClickable);
                tvSingleWeightWeight.setClickable(true);
                tvSingleWeightWeight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickedWeight(view, namedViews, weightName);
                    }
                });
            } else {
                tvSingleWeightWeight.setTextColor(Color.GRAY);
            }
            weight.tvWeight = tvSingleWeightWeight;
            llSingleWeightWeight.addView(tvSingleWeightWeight);

            TextView tvSingleWeightArm = new TextView(this);
            tvSingleWeightArm.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightArm.setGravity(Gravity.END);
            tvSingleWeightArm.setText(new DecimalFormat("#.##").format(weight.arm));
            tvSingleWeightArm.setTextSize(tvSingleWeightArm.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightArm);

            TextView tvSingleWeightMoment = new TextView(this);
            tvSingleWeightMoment.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightMoment.setGravity(Gravity.END);
            tvSingleWeightMoment.setText(new DecimalFormat("#.##").format((weight.weight * weight.arm) / aircraft.momentDivide));
            tvSingleWeightMoment.setTextSize(tvSingleWeightMoment.getTextSize() * textViewModifier);
            weight.tvMoment = tvSingleWeightMoment;
            tblrSingleWeight.addView(tvSingleWeightMoment);

            //Add to HashMap, as we'll sort by Arm before adding to Parent View
            Double armSortValue = weight.arm;
            while (true) {
                if (hmArmToTblRow.containsKey(armSortValue)) {
                    armSortValue += 0.001;
                } else {
                    break;
                }
            }
            hmArmToTblRow.put(armSortValue, tblrSingleWeight);
        }

        //baggage weight inputs
        for (AircraftClass.baggageArea weight : aircraft.baggageAreas) {
            TableRow tblrSingleWeight = new TableRow(this);
            tblrSingleWeight.setLayoutParams(tableParamsWrapContent);
            tblrSingleWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            //layout our name, weight, arm, and moment fields.
            TextView tvSingleWeightName = new TextView(this);
            tvSingleWeightName.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightName.setText(weight.name);
            final String weightName = weight.name;
            tvSingleWeightName.setTextSize(tvSingleWeightName.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightName);

            LinearLayout llSingleWeightWeight = new LinearLayout(this);
            llSingleWeightWeight.setOrientation(LinearLayout.VERTICAL);
            llSingleWeightWeight.setHorizontalGravity(Gravity.END);
            tblrSingleWeight.addView(llSingleWeightWeight);

            TextView tvSingleWeightWeight = new TextView(this);
            tvSingleWeightWeight.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightWeight.setGravity(Gravity.END);
            if (transientData.WtNameToWtDouble.get(weight.name) != null) {
                weight.weight = transientData.WtNameToWtDouble.get(weight.name);
            }
            tvSingleWeightWeight.setText(new DecimalFormat("#.##").format(weight.weight));
            tvSingleWeightWeight.setTextSize(tvSingleWeightWeight.getTextSize() * textViewModifier);
            tvSingleWeightWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
            tvSingleWeightWeight.setTextColor(colorClickable);
            tvSingleWeightWeight.setClickable(true);
            tvSingleWeightWeight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickedWeight(view, namedViews, weightName);
                }
            });
            weight.tvWeight = tvSingleWeightWeight;
            llSingleWeightWeight.addView(tvSingleWeightWeight);

            TextView tvSingleWeightArm = new TextView(this);
            tvSingleWeightArm.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightArm.setGravity(Gravity.END);
            tvSingleWeightArm.setText(new DecimalFormat("#.##").format(weight.arm));
            tvSingleWeightArm.setTextSize(tvSingleWeightArm.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightArm);

            TextView tvSingleWeightMoment = new TextView(this);
            tvSingleWeightMoment.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightMoment.setGravity(Gravity.END);
            tvSingleWeightMoment.setText(new DecimalFormat("#.##").format((weight.weight * weight.arm) / aircraft.momentDivide));
            tvSingleWeightMoment.setTextSize(tvSingleWeightMoment.getTextSize() * textViewModifier);
            weight.tvMoment = tvSingleWeightMoment;
            tblrSingleWeight.addView(tvSingleWeightMoment);

            //Add to HashMap, as we'll sort by Arm before adding to Parent View
            Double armSortValue = weight.arm;
            while (true) {
                if (hmArmToTblRow.containsKey(armSortValue)) {
                    armSortValue += 0.001;
                } else {
                    break;
                }
            }
            hmArmToTblRow.put(armSortValue, tblrSingleWeight);
        }

        //passenger row weight inputs
        for (AircraftClass.passengerRow row : aircraft.passengerRows) {
            TableRow tblrSingleWeight = new TableRow(this);
            tblrSingleWeight.setLayoutParams(tableParamsWrapContent);
            tblrSingleWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            //name, weight * numSeats, arm, and moment fields
            TextView tvRowName = new TextView(this);
            tvRowName.setLayoutParams(tableRowParamsEvenSpacing);
            tvRowName.setText(row.name);
            tvRowName.setTextSize(tvRowName.getTextSize() * textViewModifier);
            tvRowName.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            tblrSingleWeight.addView(tvRowName);

            LinearLayout llPaxSeatWts = new LinearLayout(this);
            llPaxSeatWts.setOrientation(LinearLayout.VERTICAL);
            tblrSingleWeight.addView(llPaxSeatWts);

            row.tvWeights = new TextView[row.numseats];

            for (int i=0; i < row.numseats; i++) {
                TextView tvSinglePaxWeight = new TextView(this);
                tvSinglePaxWeight.setLayoutParams(tableRowParamsEvenSpacing);
                tvSinglePaxWeight.setGravity(Gravity.END);
                Double weight = 0.0;
                if (transientData.WtNameToWtDouble.get(row.name + String.valueOf(i)) != null) {
                    weight = transientData.WtNameToWtDouble.get(row.name + String.valueOf(i));
                }
                tvSinglePaxWeight.setText(new DecimalFormat("#.##").format(weight));
                tvSinglePaxWeight.setTextSize(tvSinglePaxWeight.getTextSize() * textViewModifier);
                tvSinglePaxWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
                final String weightName = row.name + "-" + String.valueOf(i+1);
                tvSinglePaxWeight.setTextColor(colorClickable);
                tvSinglePaxWeight.setClickable(true);
                tvSinglePaxWeight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickedWeight(view, namedViews, weightName);
                    }
                });
                row.tvWeights[i] = tvSinglePaxWeight;
                llPaxSeatWts.addView(tvSinglePaxWeight);
            }

            TextView tvSingleWeightArm = new TextView(this);
            tvSingleWeightArm.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightArm.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            tvSingleWeightArm.setText(new DecimalFormat("#.##").format(row.arm));
            tvSingleWeightArm.setTextSize(tvSingleWeightArm.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightArm);

            TextView tvSingleWeightMoment = new TextView(this);
            tvSingleWeightMoment.setLayoutParams(tableRowParamsEvenSpacing);
            tvSingleWeightMoment.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            tvSingleWeightMoment.setText(new DecimalFormat("#.##").format(0.0));
            tvSingleWeightMoment.setTextSize(tvSingleWeightMoment.getTextSize() * textViewModifier);
            row.tvMoment = tvSingleWeightMoment;
            tblrSingleWeight.addView(tvSingleWeightMoment);

            //Add to HashMap, as we'll sort by Arm before adding to Parent View
            Double armSortValue = row.arm;
            while (true) {
                if (hmArmToTblRow.containsKey(armSortValue)) {
                    armSortValue += 0.001;
                } else {
                    break;
                }
            }
            hmArmToTblRow.put(armSortValue, tblrSingleWeight);
        }

        //Make our first table row, our legend.
        TableLayout tblDataInput = new TableLayout(this);
        tblDataInput.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        TableRow tblrLegend = new TableRow(this);
        tblrLegend.setLayoutParams(tableParamsWrapContent);
        tblrLegend.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tblDataInput.addView(tblrLegend);

        TextView tvNameLabel = new TextView(this);
        tvNameLabel.setLayoutParams(tableRowParamsEvenSpacing);
        tvNameLabel.setText(R.string.label_name);
        tvNameLabel.setTextSize(tvNameLabel.getTextSize() * textViewModifier);
        tblrLegend.addView(tvNameLabel);

        TextView tvWeightLabel = new TextView(this);
        tvWeightLabel.setLayoutParams(tableRowParamsEvenSpacing);
        tvWeightLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        String weightLabel = res.getString(R.string.label_weight) + "\n" + "(" + aircraft.weightUnits + ")";
        tvWeightLabel.setText(weightLabel);
        tvWeightLabel.setLines(2);
        tvWeightLabel.setTextSize(tvWeightLabel.getTextSize() * textViewModifier);
        tblrLegend.addView(tvWeightLabel);

        TextView tvArmLabel = new TextView(this);
        tvArmLabel.setLayoutParams(tableRowParamsEvenSpacing);
        tvArmLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        String armLabel = res.getString(R.string.label_arm) + "\n" + "(" + aircraft.armUnits + ")";
        tvArmLabel.setText(armLabel);
        tvArmLabel.setLines(2);
        tvArmLabel.setTextSize(tvArmLabel.getTextSize() * textViewModifier);
        tblrLegend.addView(tvArmLabel);

        TextView tvMomentLabel = new TextView(this);
        tvMomentLabel.setLayoutParams(tableRowParamsEvenSpacing);
        tvMomentLabel.setGravity(Gravity.END);
        String momentLabel = res.getString(R.string.label_moment) + "\n" + "(/" + aircraft.momentDivide + ")";
        tvMomentLabel.setText(momentLabel);
        tvMomentLabel.setLines(2);
        tvMomentLabel.setTextSize(tvMomentLabel.getTextSize() * textViewModifier);
        tblrLegend.addView(tvMomentLabel);

        //DEBUG: Add lots of rows to test scroll.
        /*for (int i = 0; i < 10; i++) {
            TableRow tblrLegend2 = new TableRow(this);
            tblrLegend2.setLayoutParams(tableParamsWrapContent);
            tblrLegend2.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));


            TextView tvNameLabel2 = new TextView(this);
            tvNameLabel2.setLayoutParams(tableRowParamsEvenSpacing);
            tvNameLabel2.setText(R.string.unset);
            tvNameLabel2.setTextSize(tvNameLabel2.getTextSize() * textViewModifier);
            tblrLegend2.addView(tvNameLabel2);

            TextView tvWeightLabel2 = new TextView(this);
            tvWeightLabel2.setLayoutParams(tableRowParamsEvenSpacing);
            tvWeightLabel2.setText(R.string.unset);
            tvWeightLabel2.setTextSize(tvWeightLabel2.getTextSize() * textViewModifier);
            tblrLegend2.addView(tvWeightLabel2);

            TextView tvArmLabel2 = new TextView(this);
            tvArmLabel2.setLayoutParams(tableRowParamsEvenSpacing);
            tvArmLabel2.setText(R.string.unset);
            tvArmLabel2.setTextSize(tvArmLabel2.getTextSize() * textViewModifier);
            tblrLegend2.addView(tvArmLabel2);

            TextView tvMomentLabel2 = new TextView(this);
            tvMomentLabel2.setLayoutParams(tableRowParamsEvenSpacing);
            tvMomentLabel2.setText(R.string.unset);
            tvMomentLabel2.setTextSize(tvMomentLabel2.getTextSize() * textViewModifier);
            tblrLegend2.addView(tvMomentLabel2);

            Double armSortValue = 1000.0;
            while (true) {
                if (hmArmToTblRow.containsKey(armSortValue)) {
                    armSortValue += 0.001;
                } else {
                    break;
                }
            }
            hmArmToTblRow.put(armSortValue, tblrLegend2);
        }*/

        //sort, then add the table rows we created and stashed above to our table.
        SortedSet<Double> sortedArms = new TreeSet<>(hmArmToTblRow.keySet());
        for (Double a : sortedArms) {
            tblDataInput.addView(hmArmToTblRow.get(a));
        }
        llDataInput.addView(tblDataInput);

        //add pop-up keypad to enter tvWeights
        ScrollView svKeypad = new ScrollView(this);
        namedViews.svKeypad = svKeypad;
        rlMain.addView(svKeypad);

        LinearLayout llKeypad = new LinearLayout(this);
        llKeypad.setOrientation(LinearLayout.VERTICAL);
        svKeypad.addView(llKeypad);

        LinearLayout llEntryTotal = new LinearLayout(this);
        llEntryTotal.setOrientation(LinearLayout.HORIZONTAL);
        llKeypad.addView(llEntryTotal);

        TextView tvEntryName = new TextView(this);
        tvEntryName.setText(R.string.unset);
        tvEntryName.setTextSize(tvEntryName.getTextSize() * textViewModifier * 1.5f);
        tvEntryName.setGravity(Gravity.START);
        tvEntryName.setLayoutParams(rowLayoutParamsEvenSpacing);
        namedViews.tvEntryName = tvEntryName;
        llEntryTotal.addView(tvEntryName);

        TextView tvEntryTotal = new TextView(this);
        String entryTotal = "0.0 " + aircraft.weightUnits;
        tvEntryTotal.setText(entryTotal);
        tvEntryTotal.setTextSize(tvEntryTotal.getTextSize() * textViewModifier * 1.5f);
        tvEntryTotal.setGravity(Gravity.END);
        tvEntryTotal.setLayoutParams(rowLayoutParamsEvenSpacing);
        namedViews.tvEntryTotal = tvEntryTotal;
        llEntryTotal.addView(tvEntryTotal);

        TableLayout tblKeypad = new TableLayout(this);
        tblKeypad.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        TableRow tblrOne = new TableRow(this);
        tblrOne.setLayoutParams(tableParamsWrapContent);

        TextView tvSeven = new TextView(this);
        tvSeven.setLayoutParams(tableRowParamsEvenSpacing);
        tvSeven.setText("7");
        tvSeven.setTextSize(tvSeven.getTextSize() * textViewModifier * 2f);
        tvSeven.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvSeven.setClickable(true);
        tvSeven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrOne.addView(tvSeven);

        TextView tvEight = new TextView(this);
        tvEight.setLayoutParams(tableRowParamsEvenSpacing);
        tvEight.setText("8");
        tvEight.setTextSize(tvEight.getTextSize() * textViewModifier * 2f);
        tvEight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvEight.setClickable(true);
        tvEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrOne.addView(tvEight);

        TextView tvNine = new TextView(this);
        tvNine.setLayoutParams(tableRowParamsEvenSpacing);
        tvNine.setText("9");
        tvNine.setTextSize(tvNine.getTextSize() * textViewModifier * 2f);
        tvNine.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvNine.setClickable(true);
        tvNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrOne.addView(tvNine);

        tblKeypad.addView(tblrOne);

        TableRow tblrTwo = new TableRow(this);
        tblrTwo.setLayoutParams(tableParamsWrapContent);

        TextView tvFour = new TextView(this);
        tvFour.setLayoutParams(tableRowParamsEvenSpacing);
        tvFour.setText("4");
        tvFour.setTextSize(tvFour.getTextSize() * textViewModifier * 2f);
        tvFour.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvFour.setClickable(true);
        tvFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrTwo.addView(tvFour);

        TextView tvFive = new TextView(this);
        tvFive.setLayoutParams(tableRowParamsEvenSpacing);
        tvFive.setText("5");
        tvFive.setTextSize(tvFive.getTextSize() * textViewModifier * 2f);
        tvFive.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvFive.setClickable(true);
        tvFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrTwo.addView(tvFive);

        TextView tvSix = new TextView(this);
        tvSix.setLayoutParams(tableRowParamsEvenSpacing);
        tvSix.setText("6");
        tvSix.setTextSize(tvSix.getTextSize() * textViewModifier * 2f);
        tvSix.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvSix.setClickable(true);
        tvSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrTwo.addView(tvSix);

        tblKeypad.addView(tblrTwo);

        TableRow tblrThree = new TableRow(this);
        tblrThree.setLayoutParams(tableParamsWrapContent);

        TextView tvOne = new TextView(this);
        tvOne.setLayoutParams(tableRowParamsEvenSpacing);
        tvOne.setText("1");
        tvOne.setTextSize(tvOne.getTextSize() * textViewModifier * 2f);
        tvOne.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvOne.setClickable(true);
        tvOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrThree.addView(tvOne);

        TextView tvTwo = new TextView(this);
        tvTwo.setLayoutParams(tableRowParamsEvenSpacing);
        tvTwo.setText("2");
        tvTwo.setTextSize(tvTwo.getTextSize() * textViewModifier * 2f);
        tvTwo.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvTwo.setClickable(true);
        tvTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrThree.addView(tvTwo);

        TextView tvThree = new TextView(this);
        tvThree.setLayoutParams(tableRowParamsEvenSpacing);
        tvThree.setText("3");
        tvThree.setTextSize(tvThree.getTextSize() * textViewModifier * 2f);
        tvThree.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvThree.setClickable(true);
        tvThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrThree.addView(tvThree);

        tblKeypad.addView(tblrThree);

        TableRow tblrFour = new TableRow(this);
        tblrFour.setLayoutParams(tableParamsWrapContent);

        TextView tvClear = new TextView(this);
        tvClear.setLayoutParams(tableRowParamsEvenSpacing);
        tvClear.setText("C");
        tvClear.setTextSize(tvClear.getTextSize() * textViewModifier * 2f);
        tvClear.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvClear.setClickable(true);
        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                namedViews.tvEntryTotal.setText(namedViews.tvCurrentTextView.getText() + " " + namedViews.weightUnits);
                namedViews.firstKeypadNumClicked = false;
            }
        });
        tblrFour.addView(tvClear);

        TextView tvDecimal = new TextView(this);
        tvDecimal.setLayoutParams(tableRowParamsEvenSpacing);
        tvDecimal.setText(".");
        tvDecimal.setTextSize(tvDecimal.getTextSize() * textViewModifier * 2f);
        tvDecimal.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvDecimal.setClickable(true);
        tvDecimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrFour.addView(tvDecimal);

        TextView tvZero = new TextView(this);
        tvZero.setLayoutParams(tableRowParamsEvenSpacing);
        tvZero.setText("0");
        tvZero.setTextSize(tvZero.getTextSize() * textViewModifier * 2f);
        tvZero.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvZero.setClickable(true);
        tvZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedNumber((TextView)view, namedViews);
            }
        });
        tblrFour.addView(tvZero);

        TextView tvEnter = new TextView(this);
        tvEnter.setLayoutParams(tableRowParamsEvenSpacing);
        tvEnter.setText(R.string.ok);
        tvEnter.setTextSize(tvEnter.getTextSize() * textViewModifier * 2f);
        tvEnter.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tvEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check our entered weight is a simple decimal
                String weightString = namedViews.tvEntryTotal.getText().toString().replace(" " + namedViews.weightUnits, "");
                try {
                    Double weightDouble = Double.parseDouble(weightString);
                    namedViews.tvCurrentTextView.setText((new DecimalFormat("#.##").format(weightDouble)));
                } catch (Exception e) {
                    AlertDialog.Builder adbBadEntry = new AlertDialog.Builder(view.getContext());
                    adbBadEntry.setTitle(res.getString(R.string.invalid_entry_title));
                    adbBadEntry.setMessage(res.getString(R.string.invalid_entry_msg));
                    adbBadEntry.setNegativeButton(res.getString(R.string.close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog adBadEntry = adbBadEntry.create();
                    adBadEntry.show();
                    return;
                }
                namedViews.svKeypad.setVisibility(View.GONE);
                namedViews.svDataInput.setVisibility(View.VISIBLE);
                allowBackPress = true;
                updateTotals(aircraft, namedViews, transientData);
            }
        });
        tblrFour.addView(tvEnter);

        tblKeypad.addView(tblrFour);

        llKeypad.addView(tblKeypad);
    }

    private void updateTotals(AircraftClass a, NamedViews namedViews,  TransientData transientData) {
        Double totalWeight = 0.0;
        Double totalMoment = 0.0;
        //mechanical weight moment calcs
        for (AircraftClass.mechanicalWeight weight : a.mechanicalWeights) {
            try {
                weight.weight = Double.parseDouble(weight.tvWeight.getText().toString());
                transientData.WtNameToWtDouble.put(weight.name, weight.weight);
                Double moment = (weight.weight * weight.arm) / a.momentDivide;
                totalWeight += weight.weight;
                totalMoment += moment;
                weight.tvMoment.setText(new DecimalFormat("#.##").format(moment));
            } catch (Exception e) {
                Resources res = getResources();
                AlertDialog.Builder adbBadEntry = new AlertDialog.Builder(this);
                adbBadEntry.setTitle(res.getString(R.string.invalid_entry_title));
                adbBadEntry.setMessage(res.getString(R.string.invalid_double) + e.toString());
                adbBadEntry.setNegativeButton(res.getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        finish();
                    }
                });
                AlertDialog adBadEntry = adbBadEntry.create();
                adBadEntry.show();
            }
        }
        //baggage weight moment calcs
        for (AircraftClass.baggageArea weight : a.baggageAreas) {
            try {
                weight.weight = Double.parseDouble(weight.tvWeight.getText().toString());
                transientData.WtNameToWtDouble.put(weight.name, weight.weight);
                Double moment = (weight.weight * weight.arm) / a.momentDivide;
                totalWeight += weight.weight;
                totalMoment += moment;
                weight.tvMoment.setText(new DecimalFormat("#.##").format(moment));
            } catch (Exception e) {
                Resources res = getResources();
                AlertDialog.Builder adbBadEntry = new AlertDialog.Builder(this);
                adbBadEntry.setTitle(res.getString(R.string.invalid_entry_title));
                adbBadEntry.setMessage(res.getString(R.string.invalid_double) + e.toString());
                adbBadEntry.setNegativeButton(res.getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        finish();
                    }
                });
                AlertDialog adBadEntry = adbBadEntry.create();
                adBadEntry.show();
            }
        }
        //passenger row moment calcs
        for (AircraftClass.passengerRow row : a.passengerRows) {
            Double rowWeight = 0.0;
            int currentPax = 0;
            for (TextView tvWeight : row.tvWeights) {
                try {
                    Double w = Double.parseDouble(tvWeight.getText().toString());
                    transientData.WtNameToWtDouble.put(row.name + String.valueOf(currentPax++), w);
                    rowWeight += w;
                } catch (Exception e) {
                    Resources res = getResources();
                    AlertDialog.Builder adbBadEntry = new AlertDialog.Builder(this);
                    adbBadEntry.setTitle(res.getString(R.string.invalid_entry_title));
                    adbBadEntry.setMessage(res.getString(R.string.invalid_double) + e.toString());
                    adbBadEntry.setNegativeButton(res.getString(R.string.close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            finish();
                        }
                    });
                    AlertDialog adBadEntry = adbBadEntry.create();
                    adBadEntry.show();
                }
            }
            Double moment = (rowWeight * row.arm) / a.momentDivide;
            totalWeight += rowWeight;
            totalMoment += moment;
            row.tvMoment.setText(new DecimalFormat("#.##").format(moment));
        }
        //update our weight and moment text views
        namedViews.tvTotalWeight.setText(new DecimalFormat("#.##").format(totalWeight));
        namedViews.tvGraphWeight.setText(new DecimalFormat("#.##").format(totalWeight));
        namedViews.tvTotalMoment.setText(new DecimalFormat("#.##").format(totalMoment));
        namedViews.tvGraphMoment.setText(new DecimalFormat("#.##").format(totalMoment));
        //update weight graph
        Boolean overWeight = false;
        Boolean closeToLimitsWeight = false;
        Float weightMinY;
        Float weightMaxY;
        Float weightCap;
        String graphWeightAnchor;
        String graphWeightDashesAnchor;
        Float weightXposition;
        if (this.screenOrientation.equals("portrait")) {
            weightMinY = 75.5f;
            weightMaxY = 34.3f;
            weightCap = 23.2f;
            graphWeightAnchor = "LC";
            graphWeightDashesAnchor = "RC";
            weightXposition = 6.5f;
        } else {
            weightMinY = 89.8f;
            weightMaxY = 25.5f;
            weightCap = 8f;
            graphWeightAnchor = "RC";
            graphWeightDashesAnchor = "LC";
            weightXposition = 96f;
        }
        Double weightRange = a.maxGross - a.emptyWeight;
        if (totalWeight > a.maxGross) {
            overWeight = true;
            Double weightDifference = totalWeight - a.maxGross;
            Double weightPercent = weightDifference / weightRange;
            Float yRange = weightMinY - weightMaxY; //Y values are backwards
            Float yOverMax = yRange * weightPercent.floatValue();
            Float newYPosition = weightMaxY - yOverMax; //Up is actually a lower Y value
            if (newYPosition < weightCap) {
                newYPosition = weightCap;
            }
            setLocation(namedViews.tvGraphWeight, weightXposition, newYPosition, graphWeightAnchor);
            setLocation(namedViews.tvGraphWeightDashes, -1f, newYPosition, graphWeightDashesAnchor);
        } else {
            Double weightDifference = a.maxGross - totalWeight;
            Double weightPercent = weightDifference / weightRange;
            if (weightPercent < 0.03 || weightPercent > 0.97) {
                closeToLimitsWeight = true;
            }
            Float yRange = weightMinY - weightMaxY; //Y values are backwards
            Float yUnderMax = yRange * weightPercent.floatValue();
            Float newYPosition = weightMaxY + yUnderMax; //Down is actually a higher Y value
            if (newYPosition > weightMinY) {
                newYPosition = weightMinY;
            }
            setLocation(namedViews.tvGraphWeight, -1f, newYPosition, graphWeightAnchor);
            setLocation(namedViews.tvGraphWeightDashes, -1f, newYPosition, graphWeightDashesAnchor);
        }
        //calculate our min and max moment for this weight
        Double lowestWeight = -10.0;
        Double highestWeight = -10.0;
        Double nearestLowerWeight = -10.0;
        Double nearestHigherWeight = -10.0;
        Double nearestLowerMinMoment = -10.0;
        Double nearestLowerMaxMoment = -10.0;
        Double nearestHigherMinMoment = -10.0;
        Double nearestHigherMaxMoment = -10.0;
        Double weightMinMoment = -10.0;
        Double weightMaxMoment = -10.0;
        Boolean weightEqRefPoint = false;
        HashMap<Double, AircraftClass.envelopeData> weightToDatapoint = new HashMap<>();
        for (AircraftClass.envelopeData dataPoint : a.envelopeDataSet) {
            weightToDatapoint.put(dataPoint.weight, dataPoint);
        }
        SortedSet<Double> dpWeights = new TreeSet<>(weightToDatapoint.keySet());
        for (Double w : dpWeights) {
            Double minMoment = weightToDatapoint.get(w).lowMoment;
            Double maxMoment = weightToDatapoint.get(w).highMoment;
            if (w < totalWeight) {
                if (nearestLowerWeight.equals(-10.0) || w > nearestLowerWeight) {
                    nearestLowerWeight = w;
                    nearestLowerMinMoment = minMoment;
                    nearestLowerMaxMoment = maxMoment;
                }
            } else if (w > totalWeight) {
                if (nearestHigherWeight.equals(-10.0) || w < nearestHigherWeight) {
                    nearestHigherWeight = w;
                    nearestHigherMinMoment = minMoment;
                    nearestHigherMaxMoment = maxMoment;
                }
            } else {
                //if we're exactly on our weight data point, we don't need anything fancy
                weightMinMoment = minMoment;
                weightMaxMoment = maxMoment;
                weightEqRefPoint = true;
            }
            if (lowestWeight.equals(-10.0) || w < lowestWeight) {
                lowestWeight = w;
            }
            if (highestWeight.equals(-10.0) || w > highestWeight) {
                highestWeight = w;
            }
        }
        Boolean weightTooHigh = false;
        Boolean weightTooLow = false;
        Boolean weightTooFwd = false;
        Boolean weightTooAft = false;
        Boolean closeToLimitsMoment = false;
        if (!weightEqRefPoint) {
            if (nearestLowerWeight.equals(-10.0)) {
                weightTooLow = true;
            }
            if (nearestHigherWeight.equals(-10.0)) {
                weightTooHigh = true;
            }
        }
        if (overWeight) {
            weightTooHigh = true;
        }
        if (!weightEqRefPoint && !weightTooHigh && !weightTooLow) {
            //find % of range between 2 tvWeights
            Double wtToLowWt = totalWeight - nearestLowerWeight;
            Double lowToHighWt = nearestHigherWeight - nearestLowerWeight;
            Double percentOfRange = wtToLowWt / lowToHighWt;
            //find same % range for low and high moments
            Double lowToHighMinMoment = nearestHigherMinMoment - nearestLowerMinMoment;
            Double lowToHighmaxMoment = nearestHigherMaxMoment - nearestLowerMaxMoment;
            Double addToLowMinMoment = lowToHighMinMoment * percentOfRange;
            Double addToLowMaxMoment = lowToHighmaxMoment * percentOfRange;
            weightMinMoment = nearestLowerMinMoment + addToLowMinMoment;
            weightMaxMoment = nearestLowerMaxMoment + addToLowMaxMoment;
        }
        if (!weightTooHigh && !weightTooLow) {
            if (totalMoment < weightMinMoment) {
                weightTooFwd = true;
            }
            if (totalMoment > weightMaxMoment) {
                weightTooAft = true;
            }
        }
        //update our moment graph
        Float newPositionY;
        Float momentCapLowY = 24f;
        Float momentMinY = 32.6f;
        Float momentMaxY = 67.2f;
        Float momentCapHighY = 76f;
        Float momentRangeY = momentMaxY - momentMinY;
        Float newPositionX;
        Float momentCapLowX = 24f;
        Float momentMinX = 33.3f;
        Float momentMaxX = 67.9f;
        Float momentCapHighX = 77f;
        Float momentRangeX = momentMaxX - momentMinX;
        Double momentRange = weightMaxMoment - weightMinMoment;
        Double percentToMoveMoment = 0.0;
        if (this.screenOrientation.equals("portrait")) {
            if (weightTooHigh || weightTooLow) {
                namedViews.tvMomentBarLabelNoseMin.setText(R.string.v_o_i_d);
                namedViews.tvMomentBarLabelTailMax.setText(R.string.v_o_i_d);
                setLocation(namedViews.tvGraphMoment, 93.9f, momentCapLowY, "RC");
                namedViews.tvGraphMomentPipe.setVisibility(View.GONE);
                namedViews.tvGraphMomentDashes.setVisibility(View.VISIBLE);
                setLocation(namedViews.tvGraphMomentDashes, -1f, momentCapLowY, "LC");
            } else {
                namedViews.tvMomentBarLabelNoseMin.setText(new DecimalFormat("#.##").format(weightMinMoment));
                namedViews.tvMomentBarLabelTailMax.setText(new DecimalFormat("#.##").format(weightMaxMoment));
                if (totalMoment > weightMaxMoment) {
                    percentToMoveMoment = (totalMoment - weightMaxMoment) / momentRange;
                    Float distToMoveMomentY = percentToMoveMoment.floatValue() * momentRangeY;
                    newPositionY = momentMaxY + distToMoveMomentY;
                    if (newPositionY > momentCapHighY) {
                        newPositionY = momentCapHighY;
                    }
                } else if (totalMoment < weightMinMoment) {
                    percentToMoveMoment = (weightMinMoment - totalMoment) / weightRange;
                    Float distToMoveMomentY = percentToMoveMoment.floatValue() * momentRangeY;
                    newPositionY = momentMinY - distToMoveMomentY;
                    if (newPositionY < momentCapLowY) {
                        newPositionY = momentCapLowY;
                    }
                } else {
                    percentToMoveMoment = (weightMaxMoment - totalMoment) / momentRange;
                    Float distToMoveMomentY = percentToMoveMoment.floatValue() * momentRangeY;
                    newPositionY = momentMaxY - distToMoveMomentY;
                }
                setLocation(namedViews.tvGraphMoment, 93.9f, newPositionY, "RC");
                namedViews.tvGraphMomentPipe.setVisibility(View.GONE);
                namedViews.tvGraphMomentDashes.setVisibility(View.VISIBLE);
                setLocation(namedViews.tvGraphMomentDashes, -1f, newPositionY, "LC");
            }
            setLocation(namedViews.tvMomentBarLabelNoseMin, 93.8f, 33f, "RC");
            setLocation(namedViews.tvMomentBarLabelTailMax, 93.8f, 67.6f, "RC");
        } else {
            if (weightTooHigh || weightTooLow) {
                namedViews.tvMomentBarLabelNoseMin.setText(R.string.v_o_i_d);
                namedViews.tvMomentBarLabelTailMax.setText(R.string.v_o_i_d);
                setLocation(namedViews.tvGraphMoment, momentCapLowX, -1f, "BC");
                namedViews.tvGraphMomentDashes.setVisibility(View.GONE);
                namedViews.tvGraphMomentPipe.setVisibility(View.VISIBLE);
                setLocation(namedViews.tvGraphMomentPipe, momentCapLowX, -1f, "TC");
            } else {
                namedViews.tvMomentBarLabelNoseMin.setText(new DecimalFormat("#.##").format(weightMinMoment));
                namedViews.tvMomentBarLabelTailMax.setText(new DecimalFormat("#.##").format(weightMaxMoment));
                if (totalMoment > weightMaxMoment) {
                    percentToMoveMoment = (totalMoment - weightMaxMoment) / momentRange;
                    Float distToMoveMomentX = percentToMoveMoment.floatValue() * momentRangeX;
                    newPositionX = momentMaxX + distToMoveMomentX;
                    if (newPositionX > momentCapHighX) {
                        newPositionX = momentCapHighX;
                    }
                } else if (totalMoment < weightMinMoment) {
                    percentToMoveMoment = (weightMinMoment - totalMoment) / weightRange;
                    Float distToMoveMomentX = percentToMoveMoment.floatValue() * momentRangeX;
                    newPositionX = momentMinX - distToMoveMomentX;
                    if (newPositionX < momentCapLowX) {
                        newPositionX = momentCapLowX;
                    }
                } else {
                    percentToMoveMoment = (weightMaxMoment - totalMoment) / momentRange;
                    Float distToMoveMomentX = percentToMoveMoment.floatValue() * momentRangeX;
                    newPositionX = momentMaxX - distToMoveMomentX;
                }
                setLocation(namedViews.tvGraphMoment, newPositionX, -1f, "BC");
                namedViews.tvGraphMomentDashes.setVisibility(View.GONE);
                namedViews.tvGraphMomentPipe.setVisibility(View.VISIBLE);
                setLocation(namedViews.tvGraphMomentPipe, newPositionX, -1f, "TC");
            }
            setLocation(namedViews.tvMomentBarLabelNoseMin, 33.3f, 94f, "BC");
            setLocation(namedViews.tvMomentBarLabelTailMax, 67.9f, 94f, "BC");
        }
        if (percentToMoveMoment < 0.04 || percentToMoveMoment > 0.96) {
            closeToLimitsMoment = true;
        }
        //update our overall Status
        String statusString = getResources().getString(R.string.label_weight) + ": ";
        if (overWeight) {
            statusString += "<font color=#cc0000>" + getResources().getString(R.string.heavy) + "</font>";
        } else {
            statusString += "<font color=#009933>" + getResources().getString(R.string.in_range) + "</font>";
            if (closeToLimitsWeight) {
                statusString += " - <font color=#e6b800>" + getResources().getString(R.string.near_limits) + "</font>";
            }
        }
        statusString += "<br>" + getResources().getString(R.string.label_moment) + ": ";
        if (weightTooHigh || weightTooLow) {
            statusString += "<font color=#cc0000>" + getResources().getString(R.string.bad_weight_envelope) + "</font>";
        } else if (weightTooFwd) {
            statusString += "<font color=#cc0000>" + getResources().getString(R.string.nose_heavy) + "</font>";
        } else if (weightTooAft) {
            statusString += "<font color=#cc0000>" + getResources().getString(R.string.tail_heavy) + "</font>";
        } else {
            statusString += "<font color=#009933>" + getResources().getString(R.string.in_range) + "</font>";
            if (closeToLimitsMoment) {
                statusString += " - <font color=#e6b800>" + getResources().getString(R.string.near_limits) + "</font>";
            }
        }
        namedViews.tvStatus.setText(fromHtml(statusString));
        if (this.screenOrientation.equals("portrait")) {
            setLocation(namedViews.tvStatus, 50f, 95f, "BC");
        } else {
            setLocation(namedViews.tvStatus, 20f, 70f, "BC");
        }

        //lastly, we'll store a copy of our transient data (just weights for now) in the current state to pull when rotating the screen or resuming.
        transientData.totalWeight = totalWeight;
        transientData.totalMoment = totalMoment;
        try {
            FileOutputStream fos = this.openFileOutput("data4_" + convertTemplateToFileName(a.getTemplateName()), Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(transientData);
            oos.close();
            fos.close();
        } catch (Exception e) {
            fatalAlert(getResources().getString(R.string.file_error_title), getResources().getString(R.string.error_creating_file) + ": " + e.toString());
        }
    }

    private String convertTemplateToFileName(String s) {
        return getResources().getString(R.string.template_file_prefix) + s;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    private void clickedNumber (TextView view, NamedViews namedViews) {
        String currentTotalWIthUnits = namedViews.tvEntryTotal.getText().toString();
        String currentTotal = currentTotalWIthUnits.replace(" " + namedViews.weightUnits, "");
        if (currentTotal.startsWith("0") || !namedViews.firstKeypadNumClicked) {
            namedViews.firstKeypadNumClicked = true;
            currentTotal = view.getText().toString();
        } else {
            currentTotal += view.getText().toString();
        }
        namedViews.tvEntryTotal.setText(currentTotal + " " + namedViews.weightUnits);
    }

    private void clickedWeight (View view, NamedViews namedViews, String name) {
        allowBackPress = false;
        namedViews.tvCurrentTextView = (TextView) view;
        namedViews.svDataInput.setVisibility(View.GONE);
        namedViews.svKeypad.setVisibility(View.VISIBLE);
        namedViews.tvEntryName.setText(name);
        namedViews.tvEntryTotal.setText(((TextView) view).getText() + " " + namedViews.weightUnits);
        namedViews.firstKeypadNumClicked = false;
    }

    private void setLocation (View view, float xPercent, float yPercent, String anchorPoint) {
        //get our base position as a percentage of screen size
        float xPosition = this.screenWidth * (xPercent/100);
        float yPosition = this.screenHeight * (yPercent/100);
        //adjust for the desired anchor point.
        view.measure(0,0);
        switch (anchorPoint) {
            case "UR":
                xPosition -= view.getMeasuredWidth();
                break;
            case "LL":
                yPosition -= view.getMeasuredHeight();
                break;
            case "LR":
                xPosition -= view.getMeasuredWidth();
                yPosition -= view.getMeasuredHeight();
                break;
            case "CN":
                xPosition -= view.getMeasuredWidth() / 2;
                yPosition -= view.getMeasuredHeight() / 2;
                break;
            case "TC":
                xPosition -= view.getMeasuredWidth() / 2;
                break;
            case "BC":
                xPosition -= view.getMeasuredWidth() / 2;
                yPosition -= view.getMeasuredHeight();
                break;
            case "LC":
                yPosition -= view.getMeasuredHeight() / 2;
                break;
            case "RC":
                xPosition -= view.getMeasuredWidth();
                yPosition -= view.getMeasuredHeight() / 2;
                break;
            default: //UL is the default
                break;
        }
        //set view position
        if (xPosition > 0 ) {
            view.setX(xPosition);
        }
        if (yPosition > 0) {
            view.setY(yPosition);
        }
    }

    private void setLayoutLocation(View view, float xPercent, float yPercent, String anchorPoint) {
        //get our base position as a percentage of screen size
        float xPosition = this.screenWidth * (xPercent/100);
        float yPosition = this.screenHeight * (yPercent/100);
        //adjust for the desired anchor point.
        int w = view.getLayoutParams().width;
        int h = view.getLayoutParams().height;
        switch (anchorPoint) {
            case "UR":
                xPosition -= w;
                break;
            case "LL":
                yPosition -= h;
                break;
            case "LR":
                xPosition -= w;
                yPosition -= h;
                break;
            case "CN":
                xPosition -= w / 2;
                yPosition -= h / 2;
                break;
            case "TC":
                xPosition -= w / 2;
                break;
            case "BC":
                xPosition -= w / 2;
                yPosition -= h;
                break;
            case "LC":
                yPosition -= h / 2;
                break;
            case "RC":
                xPosition -= w;
                yPosition -= h / 2;
                break;
            default: //UL is the default
                break;
        }
        //set view position
        view.setX(xPosition);
        view.setY(yPosition);
    }

    private void fatalAlert(String title, String msg) {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle(title);
        adBuilder.setMessage(msg);
        adBuilder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                finish();
            }
        });
        AlertDialog ad = adBuilder.create();
        ad.show();
    }

    @Override
    public void onBackPressed () {
        if (this.allowBackPress) {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("unused")
    private void logAircraft(AircraftClass a) {
        Log.d("BEGINLOGGINGAIRCRAFT", a.getTemplateName());
        Log.d("TAILNUMBER",a.tailNumber);
        Log.d("MODEL",a.model);
        Log.d("WEIGHTUNITS",a.weightUnits);
        Log.d("ARMUNITS",a.armUnits);
        Log.d("MAXGROSS",a.maxGross.toString());
        Log.d("MOMENTDIVIDE",a.momentDivide.toString());
        Log.d("MECHANICALWEIGHTS","LISTING");
        for (AircraftClass.mechanicalWeight w : a.mechanicalWeights) {
            Log.d("MW-NAME",w.name);
            Log.d("MW-WEIGHT",w.weight.toString());
            Log.d("MW-ARM",w.arm.toString());
        }
        Log.d("PAXROWS","LISTING");
        for (AircraftClass.passengerRow p : a.passengerRows) {
            Log.d("PR-NAME",p.name);
            Log.d("PR-ARM",p.arm.toString());
            Log.d("PR-SEATS",String.valueOf(p.numseats));
        }
        Log.d("BAGAREAS","LISTING");
        for (AircraftClass.baggageArea b : a.baggageAreas) {
            Log.d("BA-NAME",b.name);
            Log.d("BA-ARM",b.arm.toString());
        }
        Log.d("ENVELOPEDATA","LISTING");
        for (AircraftClass.envelopeData e : a.envelopeDataSet) {
            Log.d("EN-WEIGHT",e.weight.toString());
            Log.d("EN-LOWMOMENT",e.lowMoment.toString());
            Log.d("EN-HIGHMOMENT",e.highMoment.toString());
        }
        Log.d("DONELISTINGAIRCRAFT",a.getTemplateName());
    }
}
