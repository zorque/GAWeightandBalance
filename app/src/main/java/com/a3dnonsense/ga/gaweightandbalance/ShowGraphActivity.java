package com.a3dnonsense.ga.gaweightandbalance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class ShowGraphActivity extends AppCompatActivity {
    private class NamedViews {
        ImageView drawView;
    }
    private int screenWidth;
    private int screenHeight;
    private String screenOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);

        Intent intent = getIntent();
        final String fileName = intent.getStringExtra(MainActivity.EXTRA_FILENAME);

        final NamedViews namedViews = new NamedViews();

        //get our screen orientation for later use
        Display display = ((WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) { //Portrait Mode
            this.screenOrientation = "portrait";
        } else {
            this.screenOrientation = "landscape";
        }

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
        //grab our main layout for sizing
        final RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.activity_open_template);

        //figure out our max and min X/Y points so we can scale our grid accordingly. We'll also sort our datapoints for later.
        HashMap<Double, AircraftClass.envelopeData> weightToDatapoint = new HashMap<>();
        for (AircraftClass.envelopeData dataPoint : aircraft.envelopeDataSet) {
            weightToDatapoint.put(dataPoint.weight, dataPoint);
        }
        SortedSet<Double> dpWeights = new TreeSet<>(weightToDatapoint.keySet());
        Double minWeight = -10.0;
        Double maxWeight = -10.0;
        Double minMoment = -10.0;
        Double maxMoment = -10.0;
        for (Double w : dpWeights) {
            if (minWeight.equals(-10.0) || w < minWeight) {
                minWeight = w;
            }
            if (maxWeight.equals(-10.0) || w > maxWeight) {
                maxWeight = w;
            }
            if (minMoment.equals(-10.0) || weightToDatapoint.get(w).lowMoment < minMoment) {
                minMoment = weightToDatapoint.get(w).lowMoment;
            }
            if (maxMoment.equals(-10.0) || weightToDatapoint.get(w).highMoment > maxMoment) {
                maxMoment = weightToDatapoint.get(w).highMoment;
            }
        }
        //if we haven't found all of our range limits, something's wrong.
        if (minWeight.equals(-10.0) || maxWeight.equals(-10.0) || minMoment.equals(-10.0) || maxMoment.equals(-10.0)) {
            fatalAlert(getResources().getString(R.string.file_error_title), getResources().getString(R.string.bad_weight_envelope));
        }

        //our envelope should start at the bottom, and the top should be about 95% up the page.
        //the moment line should have about 5% free on each side.
        Double xDataRange = maxWeight - minWeight;
        Double yDataRange = maxMoment - minMoment;
        Double xAxisMin = minWeight - (xDataRange / 20);
        Double xAxisMax = maxWeight + (xDataRange / 20);
        Double yAxisMin = minMoment - (yDataRange / 20);
        Double yAxisMax = maxMoment + (yDataRange / 20);
        Double xAxisRange = xAxisMax - xAxisMin;
        Double yAxisRange = yAxisMax - yAxisMin;
        //we want about 10-15 reference points on each side, so let's figure our our scaling
        int xRoundTo = 10;
        int yRoundTo = 100;
        int[] roundPossibilities = new int[] {10, 20, 50, 100};
        for (int i : roundPossibilities) {
            if (xAxisRange / i > 5 && xAxisRange / i < 20) {
                xRoundTo = i;
            }
            if (yAxisRange / i > 5 && yAxisRange / i < 20) {
                yRoundTo = i;
            }
        }
        //reset axis min/max/ranges to accommodate rounding
        xAxisMin = roundedDouble(xAxisMin, "down", xRoundTo);
        xAxisMax = roundedDouble(xAxisMax, "up", xRoundTo);
        xAxisRange = xAxisMax - xAxisMin;
        yAxisMin = roundedDouble(yAxisMin, "down", yRoundTo);
        yAxisMax = roundedDouble(yAxisMax, "up", yRoundTo);
        yAxisRange = yAxisMax - yAxisMin;

        //now we can set up our grid.
        ImageView drawView = (ImageView)findViewById(R.id.DrawingImageView);
        Bitmap bitmap = Bitmap.createBitmap((int) getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawView.setImageBitmap(bitmap);
        namedViews.drawView = drawView;
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1f);
        canvas.drawLine(0, 0, 0, 100, paint);
        canvas.drawLine(0, 100, 100, 100, paint);
        canvas.drawLine(100, 100, 100, 0, paint);
        canvas.drawLine(100, 0, 0, 0, paint);

        //relative placement needs to happen after the layout has been painted
        rlMain.post(new Runnable() {
            @Override
            public void run() {
                screenWidth = rlMain.getWidth();
                screenHeight = rlMain.getHeight();
                if (screenOrientation.equals("portrait")) {
                    namedViews.drawView.getLayoutParams().width = Math.round(screenWidth * 0.6f);
                    namedViews.drawView.getLayoutParams().height = Math.round(screenWidth * 0.7f);
                    setLayoutLocation(namedViews.drawView, 50f, 50f, "CN");
                } else {
                    namedViews.drawView.getLayoutParams().width = Math.round(screenWidth * 0.6f);
                    namedViews.drawView.getLayoutParams().height = Math.round(screenWidth * 0.7f);
                    setLayoutLocation(namedViews.drawView, 50f, 50f, "CN");
                }
            }
        });
    }

    private Double roundedDouble (Double d, String direction, int place) {
        switch (direction) {
            case "up":
                return (Math.ceil(d/place)* place);
            case "down":
                return (Math.floor(d/place)* place);
            default:
                return (double)(Math.round(d/place)* place);
        }
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

    private String convertTemplateToFileName(String s) {
        return getResources().getString(R.string.template_file_prefix) + s;
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
}
