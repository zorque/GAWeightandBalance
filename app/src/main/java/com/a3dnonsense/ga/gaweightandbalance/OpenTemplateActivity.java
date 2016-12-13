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
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
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

        LinearLayout.LayoutParams colLayoutParams = new LinearLayout.LayoutParams(
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
                } else {
                    setLocation(namedViews.tvTemplateName, 49f, 0f, "UR");
                    setLocation(namedViews.tvTitleLabelUnits, 51f, 0f, "UL");
                    setLocation(namedViews.tvWeightBarLabelGross, 96f, 26f, "RC");
                    setLocation(namedViews.tvWeightBarLabelEmpty, 96f, 90.8f, "RC");
                    setLocation(namedViews.tvWeightBarLabelUnits, 100f, 91.5f, "UR");
                    setLocation(namedViews.tvMomentBarLabelNoseMin, 33.3f, 94f, "BC");
                    setLocation(namedViews.tvMomentBarLabelTailMax, 67.9f, 94f, "BC");
                    setLocation(namedViews.tvMomentBarLabelDivide, 77.2f, 100f, "LL");
                }
                //center our data scrollview
                namedViews.svDataInput.getLayoutParams().width = Math.round(screenWidth * 0.6f);
                namedViews.svDataInput.getLayoutParams().height = Math.round(screenHeight * 0.7f);
                setLayoutLocation(namedViews.svDataInput, 50f, 50f, "CN");
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

        //set up our input fields.
        ScrollView svDataInput = new ScrollView(this);
        namedViews.svDataInput = svDataInput;
        rlMain.addView(svDataInput);
        LinearLayout llDataInput = new LinearLayout(this);
        llDataInput.setOrientation(LinearLayout.VERTICAL);
        svDataInput.addView(llDataInput);

        HashMap<Double, LinearLayout> hmArmToLayout = new HashMap<Double, LinearLayout>();

        //mechanical weight inputs
        for (AircraftClass.mechanicalWeight m : aircraft.mechanicalWeights) {
            LinearLayout llMechWeight = new LinearLayout(this);
            llMechWeight.setOrientation(LinearLayout.HORIZONTAL);
            llMechWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            //layout our name, weight, arm, and moment fields.
            TextView tvMechWeightName = new TextView(this);
            tvMechWeightName.setLayoutParams(rowLayoutParams);
            tvMechWeightName.setText(m.name);
            tvMechWeightName.setTextSize(tvMechWeightName.getTextSize() * textViewModifier);
            llMechWeight.addView(tvMechWeightName);

            TextView tvMechWeightWeight = new TextView(this);
            tvMechWeightWeight.setLayoutParams(rowLayoutParams);
            tvMechWeightWeight.setText(new DecimalFormat("#.##").format(m.weight));
            tvMechWeightWeight.setTextSize(tvMechWeightWeight.getTextSize() * textViewModifier);
            llMechWeight.addView(tvMechWeightWeight);
            if (m.name.equals(res.getString(R.string.empty))) {
                llMechWeight.setEnabled(false);
            }

            TextView tvMechWeightArm = new TextView(this);
            tvMechWeightArm.setLayoutParams(rowLayoutParams);
            tvMechWeightArm.setText(new DecimalFormat("#.##").format(m.arm));
            tvMechWeightArm.setTextSize(tvMechWeightArm.getTextSize() * textViewModifier);
            llMechWeight.addView(tvMechWeightArm);

            TextView tvMechWeightMoment = new TextView(this);
            tvMechWeightMoment.setLayoutParams(rowLayoutParams);
            tvMechWeightMoment.setText(new DecimalFormat("#.##").format((m.weight * m.arm) / aircraft.momentDivide));
            tvMechWeightMoment.setTextSize(tvMechWeightMoment.getTextSize() * textViewModifier);
            llMechWeight.addView(tvMechWeightMoment);

            //Add to ArmToLayout HashMap, as we'll sort by Arm before adding to ScrollView
            hmArmToLayout.put(m.arm, llMechWeight);
        }

        //baggage weight inputs
        for (AircraftClass.baggageArea b : aircraft.baggageAreas) {
            LinearLayout llBagWeight = new LinearLayout(this);
            llBagWeight.setOrientation(LinearLayout.HORIZONTAL);
            llBagWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            //layout our name, weight, arm, and moment fields.
            TextView tvBagWeightName = new TextView(this);
            tvBagWeightName.setLayoutParams(rowLayoutParams);
            tvBagWeightName.setText(b.name);
            tvBagWeightName.setTextSize(tvBagWeightName.getTextSize() * textViewModifier);
            llBagWeight.addView(tvBagWeightName);

            TextView tvBagWeightWeight = new TextView(this);
            tvBagWeightWeight.setLayoutParams(rowLayoutParams);
            tvBagWeightWeight.setText(new DecimalFormat("#.##").format(b.weight));
            tvBagWeightWeight.setTextSize(tvBagWeightWeight.getTextSize() * textViewModifier);
            llBagWeight.addView(tvBagWeightWeight);
            if (b.name.equals(res.getString(R.string.empty))) {
                llBagWeight.setEnabled(false);
            }

            TextView tvBagWeightArm = new TextView(this);
            tvBagWeightArm.setLayoutParams(rowLayoutParams);
            tvBagWeightArm.setText(new DecimalFormat("#.##").format(b.arm));
            tvBagWeightArm.setTextSize(tvBagWeightArm.getTextSize() * textViewModifier);
            llBagWeight.addView(tvBagWeightArm);

            TextView tvBagWeightMoment = new TextView(this);
            tvBagWeightMoment.setLayoutParams(rowLayoutParams);
            tvBagWeightMoment.setText(new DecimalFormat("#.##").format((b.weight * b.arm) / aircraft.momentDivide));
            tvBagWeightMoment.setTextSize(tvBagWeightMoment.getTextSize() * textViewModifier);
            llBagWeight.addView(tvBagWeightMoment);

            //Add to ArmToLayout HashMap, as we'll sort by Arm before adding to ScrollView
            hmArmToLayout.put(b.arm, llBagWeight);
        }

        //passenger row inputs.

        //sort LinearLayouts by the arm key and add to scrollview.
        SortedSet<Double> sortedArms = new TreeSet<Double>(hmArmToLayout.keySet());
        for (Double a : sortedArms) {
            llDataInput.addView(hmArmToLayout.get(a));
        }
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
