package com.a3dnonsense.ga.gaweightandbalance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class OpenTemplateActivity extends AppCompatActivity {
    private int screenWidth;
    private int screenHeight;
    private String screenOrientation;
    private String screenSize;
    private float textViewModifier;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_template);

        int currentId = 1000; //used to set dynamically unique Ids in loops.

        //we'll need to know our general screen size
        //Determine screen size
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            this.screenSize = "Large";
            textViewModifier = 1.0f;
        }
        else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            this.screenSize = "Normal";
            textViewModifier = 0.5f;
        }
        else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            screenSize = "Small";
            textViewModifier = 0.5f;
        }
        else {
            screenSize = "Unknown";
            textViewModifier = 1.0f;
        }

        //ease-ification variables
        final Resources res = getResources();
        Intent intent = getIntent();
        String fileName = intent.getStringExtra(MainActivity.EXTRA_FILENAME);
        final NamedViews namedViews = new NamedViews();

        LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);

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
                    setLocation(namedViews.tvWeightBarLabelGross, 6.5f, 34.5f, "LC");
                    setLocation(namedViews.tvWeightBarLabelEmpty, 6.5f, 76f, "LC");
                    setLocation(namedViews.tvWeightBarLabelUnits, 0f, 76.5f, "UL");
                    setLocation(namedViews.tvMomentBarLabelNoseMin, 93.8f, 33f, "RC");
                    setLocation(namedViews.tvMomentBarLabelTailMax, 93.8f, 67.6f, "RC");
                    setLocation(namedViews.tvMomentBarLabelDivide, 100f, 77f, "UR");
                    namedViews.svDataInput.getLayoutParams().width = Math.round(screenWidth * 0.6f);
                    namedViews.svDataInput.getLayoutParams().height = Math.round(screenHeight * 0.7f);
                    setLayoutLocation(namedViews.svDataInput, 50f, 50f, "CN");
                } else {
                    setLocation(namedViews.tvTemplateName, 49f, 0f, "UR");
                    setLocation(namedViews.tvTitleLabelUnits, 51f, 0f, "UL");
                    setLocation(namedViews.tvWeightBarLabelGross, 96f, 26f, "RC");
                    setLocation(namedViews.tvWeightBarLabelEmpty, 96f, 90.8f, "RC");
                    setLocation(namedViews.tvWeightBarLabelUnits, 100f, 91.5f, "UR");
                    setLocation(namedViews.tvMomentBarLabelNoseMin, 33.3f, 94f, "BC");
                    setLocation(namedViews.tvMomentBarLabelTailMax, 67.9f, 94f, "BC");
                    setLocation(namedViews.tvMomentBarLabelDivide, 77.2f, 100f, "LL");
                    //center our data scrollview
                    namedViews.svDataInput.getLayoutParams().width = Math.round(screenWidth * 0.4f);
                    namedViews.svDataInput.getLayoutParams().height = Math.round(screenHeight * 0.7f);
                    setLayoutLocation(namedViews.svDataInput, 62f, 50f, "CN");
                }
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

        //read in template object
        AircraftClass aircraft = new AircraftClass();
        try {
            FileInputStream fis = this.openFileInput(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            aircraft = (AircraftClass) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            fatalAlert(getResources().getString(R.string.file_error_title), getResources().getString(R.string.error_opening_file) + " (" + getResources().getString(R.string.try_recreating_file) + "): " + e.toString());
        }

        //set up our text labels
        TextView tvTemplateName = new TextView(this);
        tvTemplateName.setLayoutParams(rowLayoutParams);
        tvTemplateName.setText(aircraft.getTemplateName());
        tvTemplateName.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvTemplateName.getTextSize() * textViewModifier * 3);
        rlMain.addView(tvTemplateName);
        namedViews.tvTemplateName = tvTemplateName;

        TextView tvTitleLabelUnits = new TextView(this);
        tvTitleLabelUnits.setLayoutParams(rowLayoutParams);
        String tvTitleLabelUnitsString = res.getString(R.string.label_weight) + "=" + aircraft.weightUnits + ", " + res.getString(R.string.label_arm) + "=" + aircraft.armUnits;
        tvTitleLabelUnits.setText(tvTitleLabelUnitsString);
        rlMain.addView(tvTitleLabelUnits);
        namedViews.tvTitleLabelUnits = tvTitleLabelUnits;

        TextView tvWeightBarLabelGross = new TextView(this);
        tvWeightBarLabelGross.setLayoutParams(rowLayoutParams);
        tvWeightBarLabelGross.setText(new DecimalFormat("#.##").format(aircraft.maxGross));
        tvWeightBarLabelGross.setTextSize(tvWeightBarLabelGross.getTextSize() * textViewModifier);
        tvWeightBarLabelGross.setEnabled(false);
        rlMain.addView(tvWeightBarLabelGross);
        namedViews.tvWeightBarLabelGross = tvWeightBarLabelGross;

        TextView tvWeightBarLabelEmpty = new TextView(this);
        tvWeightBarLabelEmpty.setLayoutParams(rowLayoutParams);
        tvWeightBarLabelEmpty.setText(new DecimalFormat("#.##").format(aircraft.emptyWeight));
        tvWeightBarLabelEmpty.setTextSize(tvWeightBarLabelEmpty.getTextSize() * textViewModifier);
        tvWeightBarLabelEmpty.setEnabled(false);
        rlMain.addView(tvWeightBarLabelEmpty);
        namedViews.tvWeightBarLabelEmpty = tvWeightBarLabelEmpty;

        TextView tvWeightBarLabelUnits = new TextView(this);
        tvWeightBarLabelUnits.setLayoutParams(rowLayoutParams);
        tvWeightBarLabelUnits.setText(aircraft.weightUnits);
        tvWeightBarLabelUnits.setTextSize(tvWeightBarLabelUnits.getTextSize() * textViewModifier);
        rlMain.addView(tvWeightBarLabelUnits);
        namedViews.tvWeightBarLabelUnits = tvWeightBarLabelUnits;

        TextView tvMomentBarLabelNoseMin = new TextView(this);
        tvMomentBarLabelNoseMin.setLayoutParams(rowLayoutParams);
        tvMomentBarLabelNoseMin.setText(R.string.unset);
        tvMomentBarLabelNoseMin.setTextSize(tvMomentBarLabelNoseMin.getTextSize() * textViewModifier);
        tvMomentBarLabelNoseMin.setEnabled(false);
        rlMain.addView(tvMomentBarLabelNoseMin);
        namedViews.tvMomentBarLabelNoseMin = tvMomentBarLabelNoseMin;

        TextView tvMomentBarLabelTailMax = new TextView(this);
        tvMomentBarLabelTailMax.setLayoutParams(rowLayoutParams);
        tvMomentBarLabelTailMax.setText(R.string.unset);
        tvMomentBarLabelTailMax.setTextSize(tvMomentBarLabelTailMax.getTextSize() * textViewModifier);
        tvMomentBarLabelTailMax.setEnabled(false);
        rlMain.addView(tvMomentBarLabelTailMax);
        namedViews.tvMomentBarLabelTailMax = tvMomentBarLabelTailMax;

        TextView tvMomentBarLabelDivide = new TextView(this);
        tvMomentBarLabelDivide.setLayoutParams(rowLayoutParams);
        String weightDivideString = "/" + String.valueOf(aircraft.momentDivide);
        tvMomentBarLabelDivide.setText(weightDivideString);
        tvMomentBarLabelDivide.setTextSize(tvMomentBarLabelDivide.getTextSize() * textViewModifier);
        rlMain.addView(tvMomentBarLabelDivide);
        namedViews.tvMomentBarLabelDivide = tvMomentBarLabelDivide;

        //set up a base container for our input fields.
        ScrollView svDataInput = new ScrollView(this);
        namedViews.svDataInput = svDataInput;
        rlMain.addView(svDataInput);

        LinearLayout llDataInput = new LinearLayout(this);
        llDataInput.setOrientation(LinearLayout.VERTICAL);
        svDataInput.addView(llDataInput);

        //create table rows to hold data (we'll store them in a hashmap so we can sort by arm before adding to our main table
        HashMap<Double, TableRow> hmArmToTblRow = new HashMap<>();
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);

        //mechanical weight inputs
        for (AircraftClass.mechanicalWeight weight : aircraft.mechanicalWeights) {
            TableRow tblrSingleWeight = new TableRow(this);
            tblrSingleWeight.setLayoutParams(tableParams);
            tblrSingleWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            //layout our name, weight, arm, and moment fields.
            TextView tvSingleWeightName = new TextView(this);
            tvSingleWeightName.setLayoutParams(tableRowParams);
            tvSingleWeightName.setText(weight.name);
            tvSingleWeightName.setTextSize(tvSingleWeightName.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightName);

            TextView tvSingleWeightWeight = new TextView(this);
            tvSingleWeightWeight.setLayoutParams(tableRowParams);
            tvSingleWeightWeight.setGravity(Gravity.END);
            tvSingleWeightWeight.setText(new DecimalFormat("#.##").format(weight.weight));
            tvSingleWeightWeight.setTextSize(tvSingleWeightWeight.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightWeight);

            TextView tvSingleWeightArm = new TextView(this);
            tvSingleWeightArm.setLayoutParams(tableRowParams);
            tvSingleWeightArm.setGravity(Gravity.END);
            tvSingleWeightArm.setText(new DecimalFormat("#.##").format(weight.arm));
            tvSingleWeightArm.setTextSize(tvSingleWeightArm.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightArm);

            TextView tvSingleWeightMoment = new TextView(this);
            tvSingleWeightMoment.setLayoutParams(tableRowParams);
            tvSingleWeightMoment.setGravity(Gravity.END);
            tvSingleWeightMoment.setText(new DecimalFormat("#.##").format((weight.weight * weight.arm) / aircraft.momentDivide));
            tvSingleWeightMoment.setTextSize(tvSingleWeightMoment.getTextSize() * textViewModifier);
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
            tblrSingleWeight.setLayoutParams(tableParams);
            tblrSingleWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            //layout our name, weight, arm, and moment fields.
            TextView tvSingleWeightName = new TextView(this);
            tvSingleWeightName.setLayoutParams(tableRowParams);
            tvSingleWeightName.setText(weight.name);
            tvSingleWeightName.setTextSize(tvSingleWeightName.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightName);

            TextView tvSingleWeightWeight = new TextView(this);
            tvSingleWeightWeight.setLayoutParams(tableRowParams);
            tvSingleWeightWeight.setGravity(Gravity.END);
            tvSingleWeightWeight.setText(new DecimalFormat("#.##").format(weight.weight));
            tvSingleWeightWeight.setTextSize(tvSingleWeightWeight.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightWeight);

            TextView tvSingleWeightArm = new TextView(this);
            tvSingleWeightArm.setLayoutParams(tableRowParams);
            tvSingleWeightArm.setGravity(Gravity.END);
            tvSingleWeightArm.setText(new DecimalFormat("#.##").format(weight.arm));
            tvSingleWeightArm.setTextSize(tvSingleWeightArm.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightArm);

            TextView tvSingleWeightMoment = new TextView(this);
            tvSingleWeightMoment.setLayoutParams(tableRowParams);
            tvSingleWeightMoment.setGravity(Gravity.END);
            tvSingleWeightMoment.setText(new DecimalFormat("#.##").format((weight.weight * weight.arm) / aircraft.momentDivide));
            tvSingleWeightMoment.setTextSize(tvSingleWeightMoment.getTextSize() * textViewModifier);
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

        //TODO: Add pax rows.
        //passenger row weight inputs
        for (AircraftClass.passengerRow row : aircraft.passengerRows) {
            TableRow tblrSingleWeight = new TableRow(this);
            tblrSingleWeight.setLayoutParams(tableParams);
            tblrSingleWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            //name, weight * numSeats, arm, and moment fields
            TextView tvRowName = new TextView(this);
            tvRowName.setLayoutParams(tableRowParams);
            tvRowName.setText(row.name);
            tvRowName.setTextSize(tvRowName.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvRowName);

            LinearLayout llPaxSeatWts = new LinearLayout(this);
            llPaxSeatWts.setOrientation(LinearLayout.VERTICAL);
            llPaxSeatWts.setHorizontalGravity(Gravity.END);
            tblrSingleWeight.addView(llPaxSeatWts);

            for (int i=0; i < row.numseats; i++) {
                TextView tvSinglePaxWeight = new TextView(this);
                tvSinglePaxWeight.setLayoutParams(tableRowParams);
                tvSinglePaxWeight.setGravity(Gravity.END);
                tvSinglePaxWeight.setText(new DecimalFormat("#.##").format(0.0));
                tvSinglePaxWeight.setTextSize(tvSinglePaxWeight.getTextSize() * textViewModifier);
                llPaxSeatWts.addView(tvSinglePaxWeight);
            }

            TextView tvSingleWeightArm = new TextView(this);
            tvSingleWeightArm.setLayoutParams(tableRowParams);
            tvSingleWeightArm.setGravity(Gravity.END);
            tvSingleWeightArm.setText(new DecimalFormat("#.##").format(row.arm));
            tvSingleWeightArm.setTextSize(tvSingleWeightArm.getTextSize() * textViewModifier);
            tblrSingleWeight.addView(tvSingleWeightArm);

            TextView tvSingleWeightMoment = new TextView(this);
            tvSingleWeightMoment.setLayoutParams(tableRowParams);
            tvSingleWeightMoment.setGravity(Gravity.END);
            tvSingleWeightMoment.setText(new DecimalFormat("#.##").format(0.0));
            tvSingleWeightMoment.setTextSize(tvSingleWeightMoment.getTextSize() * textViewModifier);
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
        tblrLegend.setLayoutParams(tableParams);
        tblrLegend.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tblDataInput.addView(tblrLegend);

        TextView tvNameLabel = new TextView(this);
        tvNameLabel.setLayoutParams(tableRowParams);
        tvNameLabel.setText(R.string.label_name);
        tvNameLabel.setTextSize(tvNameLabel.getTextSize() * textViewModifier);
        tblrLegend.addView(tvNameLabel);

        TextView tvWeightLabel = new TextView(this);
        tvWeightLabel.setLayoutParams(tableRowParams);
        tvWeightLabel.setGravity(Gravity.END);
        String weightLabel = res.getString(R.string.label_weight) + "\n" + "(" + aircraft.weightUnits + ")";
        tvWeightLabel.setText(weightLabel);
        tvWeightLabel.setLines(2);
        tvWeightLabel.setTextSize(tvWeightLabel.getTextSize() * textViewModifier);
        tblrLegend.addView(tvWeightLabel);

        TextView tvArmLabel = new TextView(this);
        tvArmLabel.setLayoutParams(tableRowParams);
        tvArmLabel.setGravity(Gravity.END);
        String armLabel = res.getString(R.string.label_arm) + "\n" + "(" + aircraft.armUnits + ")";
        tvArmLabel.setText(armLabel);
        tvArmLabel.setLines(2);
        tvArmLabel.setTextSize(tvArmLabel.getTextSize() * textViewModifier);
        tblrLegend.addView(tvArmLabel);

        TextView tvMomentLabel = new TextView(this);
        tvMomentLabel.setLayoutParams(tableRowParams);
        tvMomentLabel.setGravity(Gravity.END);
        String momentLabel = res.getString(R.string.label_moment) + "\n" + "(/" + aircraft.momentDivide + ")";
        tvMomentLabel.setText(momentLabel);
        tvMomentLabel.setLines(2);
        tvMomentLabel.setTextSize(tvMomentLabel.getTextSize() * textViewModifier);
        tblrLegend.addView(tvMomentLabel);

        //DEBUG: Add lots of rows to test scroll.
        /*for (int i = 0; i < 10; i++) {
            TableRow tblrLegend2 = new TableRow(this);
            tblrLegend2.setLayoutParams(tableParams);
            tblrLegend2.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));


            TextView tvNameLabel2 = new TextView(this);
            tvNameLabel2.setLayoutParams(tableRowParams);
            tvNameLabel2.setText(R.string.unset);
            tvNameLabel2.setTextSize(tvNameLabel2.getTextSize() * textViewModifier);
            tblrLegend2.addView(tvNameLabel2);

            TextView tvWeightLabel2 = new TextView(this);
            tvWeightLabel2.setLayoutParams(tableRowParams);
            tvWeightLabel2.setText(R.string.unset);
            tvWeightLabel2.setTextSize(tvWeightLabel2.getTextSize() * textViewModifier);
            tblrLegend2.addView(tvWeightLabel2);

            TextView tvArmLabel2 = new TextView(this);
            tvArmLabel2.setLayoutParams(tableRowParams);
            tvArmLabel2.setText(R.string.unset);
            tvArmLabel2.setTextSize(tvArmLabel2.getTextSize() * textViewModifier);
            tblrLegend2.addView(tvArmLabel2);

            TextView tvMomentLabel2 = new TextView(this);
            tvMomentLabel2.setLayoutParams(tableRowParams);
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
        view.setX(xPosition);
        view.setY(yPosition);
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
}
