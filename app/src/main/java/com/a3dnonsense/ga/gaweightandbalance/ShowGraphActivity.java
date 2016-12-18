package com.a3dnonsense.ga.gaweightandbalance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class ShowGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);

        Intent intent = getIntent();
        final String fileName = intent.getStringExtra(MainActivity.EXTRA_FILENAME);

        //get our screen orientation for later use
        Display display = ((WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

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
        Double xDataRange = maxMoment - minMoment;
        Double yDataRange = maxWeight - minWeight;
        Double xAxisMin = minMoment - (xDataRange / 20);
        Double xAxisMax = maxMoment + (xDataRange / 20);
        Double yAxisMin = minWeight - (yDataRange / 20);
        Double yAxisMax = maxWeight + (yDataRange / 20);
        Double xAxisRangeTmp = xAxisMax - xAxisMin;
        Double yAxisRangeTmp = yAxisMax - yAxisMin;
        //we want about 10-15 reference points on each side, so let's figure our our scaling
        int xRoundTo = 10;
        int yRoundTo = 100;
        int[] roundPossibilities = new int[] {100, 50, 20, 10}; //reverse order because we want the most ref points possible.
        for (int i : roundPossibilities) {
            if (xAxisRangeTmp / i > 5 && xAxisRangeTmp / i < 20) {
                xRoundTo = i;
            }
            if (yAxisRangeTmp / i > 5 && yAxisRangeTmp / i < 20) {
                yRoundTo = i;
            }
        }
        //reset axis min/max/ranges to accommodate rounding
        xAxisMin = roundedDouble(xAxisMin, "down", xRoundTo);
        xAxisMax = roundedDouble(xAxisMax, "up", xRoundTo);
        final Double xAxisRange = xAxisMax - xAxisMin;
        yAxisMin = roundedDouble(yAxisMin, "down", yRoundTo);
        yAxisMax = roundedDouble(yAxisMax, "up", yRoundTo);
        final Double yAxisRange = yAxisMax - yAxisMin;

        //set up our drawing canvas
        final ImageView drawView = (ImageView)findViewById(R.id.DrawingImageView);
        final Point screenSize = new Point();
        display.getSize(screenSize);
        Bitmap bitmapTmp = Bitmap.createBitmap(Math.round(screenSize.x * 1.1f), screenSize.y, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmapTmp);
        drawView.setImageBitmap(bitmapTmp);
        drawView.setBackgroundColor(Color.WHITE);
        drawView.measure(0,0);
        final int sizeX = drawView.getMeasuredWidth();
        final int sizeY = drawView.getMeasuredHeight();
        Bitmap bitmap = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawView.setImageBitmap(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1f);

        //set up our base grid
        for (double d = xAxisMin; d <= xAxisMax; d += xRoundTo) {
            double xAmount = d - xAxisMin;
            double percentX = xAmount / xAxisRange;
            float x = (float)(sizeX * percentX);
            canvas.drawLine(x, 0, x, sizeY, paint);
            canvas.drawText(new DecimalFormat("#.##").format(d), x, sizeY, paint);
        }
        for (double d = yAxisMin; d <= yAxisMax; d += yRoundTo) {
            double yAmount = yAxisMax - d;
            double percentY = yAmount / yAxisRange;
            float y = (float)(sizeY * percentY);
            canvas.drawLine(0, y, sizeX, y, paint);
            canvas.drawText(new DecimalFormat("#.##").format(d), 0, y, paint);
        }
        //draw our envelope
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2f);
        Boolean firstPointSkipped = false;
        AircraftClass.envelopeData lastDp = aircraft.envelopeDataSet.get(0);
        for (Double w : dpWeights) {
            AircraftClass.envelopeData currentDp = weightToDatapoint.get(w);
            if (firstPointSkipped) {
                //connect to the last point min and max
                double xAmountStart1 = lastDp.lowMoment - xAxisMin;
                double xAmountStart2 = lastDp.highMoment - xAxisMin;
                double xAmountFinish1 = currentDp.lowMoment - xAxisMin;
                double xAmountFinish2 = currentDp.highMoment - xAxisMin;

                double yAmountStart = yAxisMax - lastDp.weight;
                double yAmountFinish = yAxisMax - currentDp.weight;

                double xPercentStart1 = xAmountStart1 / xAxisRange;
                double xPercentStart2 = xAmountStart2 / xAxisRange;
                double xPercentFinish1 = xAmountFinish1 / xAxisRange;
                double xPercentFinish2 = xAmountFinish2 / xAxisRange;
                double yPercentStart = yAmountStart / yAxisRange;
                double yPercentFinish = yAmountFinish / yAxisRange;

                float xStart1 = (float)(sizeX * xPercentStart1);
                float xStart2 = (float)(sizeX * xPercentStart2);
                float xFinish1 = (float)(sizeX * xPercentFinish1);
                float xFinish2 = (float)(sizeX * xPercentFinish2);
                float yStart = (float)(sizeY * yPercentStart);
                float yFinish = (float)(sizeY * yPercentFinish);

                canvas.drawLine(xStart1, yStart, xFinish1, yFinish, paint);
                canvas.drawLine(xStart2, yStart, xFinish2, yFinish, paint);
            } else {
                //connect our bottom points
                double xAmountStart = currentDp.lowMoment - xAxisMin;
                double xAmountFinish = currentDp.highMoment - xAxisMin;
                double yAmount = yAxisMax - currentDp.weight;

                double xPercentStart = xAmountStart / xAxisRange;
                double xPercentFinish = xAmountFinish / xAxisRange;
                double yPercent = yAmount / yAxisRange;

                float xStart = (float)(sizeX * xPercentStart);
                float xFinish = (float)(sizeX * xPercentFinish);
                float y = (float)(sizeY * yPercent);

                canvas.drawLine(xStart, y, xFinish, y, paint);
            }
            lastDp = currentDp;
            firstPointSkipped = true;
        }
        if (firstPointSkipped) {
            //cap our envelope
            double xAmountStart = lastDp.lowMoment - xAxisMin;
            double xAmountFinish = lastDp.highMoment - xAxisMin;
            double yAmount = yAxisMax - lastDp.weight;

            double xPercentStart = xAmountStart / xAxisRange;
            double xPercentFinish = xAmountFinish / xAxisRange;
            double yPercent = yAmount / yAxisRange;

            float xStart = (float)(sizeX * xPercentStart);
            float xFinish = (float)(sizeX * xPercentFinish);
            float y = (float)(sizeY * yPercent);

            canvas.drawLine(xStart, y, xFinish, y, paint);
        }
        //plot our point
        double pointXAmount = transientData.totalMoment - xAxisMin;
        double pointYAmount = yAxisMax - transientData.totalWeight;

        double pointXPercent = pointXAmount / xAxisRange;
        double pointYPercent = pointYAmount / yAxisRange;

        float pointX = (float)(sizeX * pointXPercent);
        float pointY = (float)(sizeY * pointYPercent);


        //if our point is in-bounds, draw a circle. Otherwise, draw an arrow pointing to it
        Log.d("POINTX", Float.valueOf(pointX).toString());
        Log.d("POINTY", Float.valueOf(pointY).toString());
        Log.d("SIZEX", Float.valueOf(sizeX).toString());
        Log.d("SIZEY", Float.valueOf(sizeY).toString());
        if (pointX >= 0 && pointX <= sizeX && pointY >= 0 && pointY <= sizeY) {
            paint.setColor(Color.BLUE);
            Log.d("STATUS", "INBOUNDS");
            canvas.drawCircle(pointX, pointY, 3f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            canvas.drawCircle(pointX, pointY, 10f, paint);
        } else if (pointX < 0 && pointY < 0) {
            //off upper left corner
            Log.d("STATUS", "OOBUL");
            paint.setStrokeWidth(2f);
            paint.setColor(Color.RED);
            canvas.drawLine(0,0,0,10,paint);
            canvas.drawLine(0,0,10,0,paint);
            canvas.drawLine(0,0,15,15,paint);
        } else if (pointX > sizeX && pointY < 0) {
            //off upper right corner
            Log.d("STATUS", "OOBUR");
            paint.setStrokeWidth(2f);
            paint.setColor(Color.RED);
            canvas.drawLine(sizeX,0,sizeX-10,0,paint);
            canvas.drawLine(sizeX,0,sizeX,10,paint);
            canvas.drawLine(sizeX, 0, sizeX - 15, 15,paint);
        } else if (pointX > sizeX && pointY > sizeY) {
            //off bottom right corner
            Log.d("STATUS", "OOBLR");
            paint.setStrokeWidth(2f);
            paint.setColor(Color.RED);
            canvas.drawLine(sizeX, sizeY, sizeX, sizeY - 10,paint);
            canvas.drawLine(sizeX, sizeY, sizeX - 10, sizeY,paint);
            canvas.drawLine(sizeX, sizeY, sizeX - 15, sizeY - 15,paint);
        } else if (pointX < 0 && pointY > sizeY) {
            //off bottom left corner
            Log.d("STATUS", "OOBLL");
            paint.setStrokeWidth(2f);
            paint.setColor(Color.RED);
            canvas.drawLine(0, sizeY, 0, sizeY - 10,paint);
            canvas.drawLine(0, sizeY,10, sizeY,paint);
            canvas.drawLine(0, sizeY, 15, sizeY - 15,paint);
        } else if (pointX < 0) {
            //off left side
            Log.d("STATUS", "OOBLEFT");
            paint.setColor(Color.RED);
            paint.setStrokeWidth(2f);
            canvas.drawLine(0,pointY,10, pointY - 10,paint);
            canvas.drawLine(0,pointY, 10, pointY + 10,paint);
            canvas.drawLine(0, pointY, 15, pointY,paint);
        } else if (pointY < 0) {
            //off top
            Log.d("STATUS", "OOBTOP");
            paint.setStrokeWidth(2f);
            paint.setColor(Color.RED);
            canvas.drawLine(pointX, 0, pointX - 10, 10,paint);
            canvas.drawLine(pointX, 0, pointX + 10, 10,paint);
            canvas.drawLine(pointX, 0, pointX, 15,paint);
        } else if (pointX > sizeX) {
            //off right side
            Log.d("STATUS", "OOBRIGHT");
            paint.setStrokeWidth(2f);
            paint.setColor(Color.RED);
            canvas.drawLine(sizeX, pointY, sizeX - 10, pointY - 10,paint);
            canvas.drawLine(sizeX, pointY, sizeX - 10, pointY + 10,paint);
            canvas.drawLine(sizeX, pointY, sizeX - 15, pointY,paint);
        } else if (pointY > sizeY) {
            //off bottom
            Log.d("STATUS", "OOBBOTTOM");
            paint.setStrokeWidth(2f);
            paint.setColor(Color.RED);
            canvas.drawLine(pointX, sizeY, pointX - 10, sizeY - 10,paint);
            canvas.drawLine(pointX, sizeY, pointX + 10, sizeY - 10,paint);
            canvas.drawLine(pointX, sizeY, pointX, sizeY - 15,paint);
        }
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
}
