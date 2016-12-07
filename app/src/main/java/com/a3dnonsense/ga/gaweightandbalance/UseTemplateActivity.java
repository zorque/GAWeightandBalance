package com.a3dnonsense.ga.gaweightandbalance;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import static android.R.attr.max;
import static android.R.attr.onClick;
import static android.R.attr.resource;

public class UseTemplateActivity extends AppCompatActivity {
    public void updateTotals(ArrayList wtIds, ArrayList mmtIds, HashMap<String, Integer> nameToId, HashMap<Integer, Double> wtIdToWt, HashMap<Integer, Double> mmIdToMmt, HashMap<Double, String> envWtToRng, HashMap<String, String> dataMap) {
        Double totalWt = 0.0;
        Double totalMmt = 0.0;
        //add up our weights and moments
        for (int i=0; i < wtIds.size(); i++) {
            totalWt += wtIdToWt.get(wtIds.get(i));
        }
        for (int i=0; i < mmtIds.size(); i++) {
            totalMmt += mmIdToMmt.get(mmtIds.get(i));
        }
        //update appropriate text fields
        int totalWtId = nameToId.get("totalWeight");
        int totalMmtId = nameToId.get("totalMoment");
        TextView viewToChangeTotalWeight = (TextView) findViewById(totalWtId);
        if (!(viewToChangeTotalWeight == null)) {
            viewToChangeTotalWeight.setText(new DecimalFormat("#.##").format(totalWt));
        }
        TextView viewToChangeTotalMoment = (TextView) findViewById(totalMmtId);
        if (!(viewToChangeTotalMoment == null)) {
            viewToChangeTotalMoment.setText(new DecimalFormat("#.##").format(totalMmt));
        }
        //find acceptable range for the given weight.
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
        //find nearest 2 weight data points
        Boolean weightEqRefPoint = false;
        SortedSet<Double> envKeys = new TreeSet<Double>(envWtToRng.keySet());
        for (Double w : envKeys) {
            String range = envWtToRng.get(w);
            String[] rangeStrings = range.split("<>");
            Double minMoment = Double.parseDouble(rangeStrings[0]);
            Double maxMoment = Double.parseDouble(rangeStrings[1]);
            if (w < totalWt) {
                if (nearestLowerWeight.equals(-10.0) || w > nearestLowerWeight) {
                    nearestLowerWeight = w;
                    nearestLowerMinMoment = minMoment;
                    nearestLowerMaxMoment = maxMoment;
                }
            }
            if (w > totalWt) {
                if (nearestHigherWeight.equals(-10.0) || w < nearestHigherWeight) {
                    nearestHigherWeight = w;
                    nearestHigherMinMoment = minMoment;
                    nearestHigherMaxMoment = maxMoment;
                }
            }
            if (w.equals(totalWt)) {
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
        //if our weight values are still default, we are out of range.
        Boolean weightTooHigh = false;
        Boolean weightTooLow = false;
        Boolean weightTooFwd = false;
        Boolean weightTooAft = false;
        if (!weightEqRefPoint) {
            if (nearestLowerWeight.equals(-10.0)) {
                weightTooLow = true;
            }
            if (nearestHigherWeight.equals(-10.0)) {
                weightTooHigh = true;
            }
        }
        //also, if we're over our maxGross, we're too high.
        Double maxGross = Double.parseDouble(dataMap.get("MaxGross"));
        if (totalWt > maxGross) {
            weightTooHigh = true;
        }
        if (!weightEqRefPoint && !weightTooHigh && !weightTooLow) {
            //find % of range between 2 weights
            Double wtToLowWt = totalWt - nearestLowerWeight;
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
            if (totalMmt < weightMinMoment) {
                weightTooFwd = true;
            }
            if (totalMmt > weightMaxMoment) {
                weightTooAft = true;
            }
        }
        //now we can layout our representation of our results.
        Integer lPosition = 5;
        Integer hPosition = 16;
        int positionShiftInt = 0;
        int finalPosition = 0;
        //update text line: Weight: <in-range|out-of-range-<heavy|light>>
        TextView wtViewToChange = (TextView) findViewById(nameToId.get("WeightResult"));
        if (weightTooLow) {
            wtViewToChange.setText(R.string.label_weight_result_light);
        } else if (weightTooHigh) {
            wtViewToChange.setText(R.string.label_weight_result_heavy);
        }  else {
            wtViewToChange.setText(R.string.label_weight_result_in_range);
        }

        //update graph line: light-----L---|------H-----heavy
        Double weightRange = maxGross - lowestWeight;
        StringBuilder wtRngBaseString = new StringBuilder(getResources().getString(R.string.graph_weight));
        Double wtDifference = 0.0;
        Double wtPercent = 0.0;
        if (weightTooLow) {
            wtDifference = lowestWeight - totalWt;
            wtPercent = wtDifference / weightRange;
            positionShiftInt = (int) Math.round(wtPercent * 10);
            finalPosition = lPosition - 1 - positionShiftInt;
            if (finalPosition < 0) {
                finalPosition = 0;
            }
        } else if (weightTooHigh) {
            wtDifference = totalWt - maxGross;
            wtPercent = wtDifference / weightRange;
            positionShiftInt = (int) Math.round(wtPercent * 10);
            finalPosition = hPosition + 1 + positionShiftInt;
            if (finalPosition > wtRngBaseString.length() - 1) {
                finalPosition = wtRngBaseString.length() - 1;
            }
        } else {
            wtDifference = totalWt - lowestWeight;
            wtPercent = wtDifference / weightRange;
            positionShiftInt = (int) Math.round(wtPercent * 10);
            finalPosition = lPosition + 1 + positionShiftInt;
            if (finalPosition < lPosition + 1) {
                finalPosition = lPosition + 1;
            } else if (finalPosition > hPosition - 1) {
                finalPosition = hPosition - 1;
            }
        }
        wtRngBaseString.setCharAt(finalPosition, '|');
        TextView wtGraphToChange = (TextView) findViewById(nameToId.get("WeightGraph"));
        wtGraphToChange.setText(wtRngBaseString.toString());

        //update text line: Moment: <in-range|out-of-range-<nose|tail>>
        TextView mmtViewToChange = (TextView) findViewById(nameToId.get("MomentResult"));
        if (weightTooLow || weightTooHigh) {
            mmtViewToChange.setText(R.string.label_moment_result_bad_weight);
        } else {
            if (weightTooFwd) {
                mmtViewToChange.setText(R.string.label_moment_result_nose);
            } else if (weightTooAft) {
                mmtViewToChange.setText(R.string.label_moment_result_tail);
            } else {
                mmtViewToChange.setText(R.string.label_moment_result_in_range);
            }
        }

        //update graph line labels
        TextView mmtGraphToChange = (TextView) findViewById(nameToId.get("MomentGraph"));
        if (weightTooLow || weightTooHigh) {
            mmtGraphToChange.setText(R.string.graph_moment_bad_weight);
        } else {
            TextView mmtGraphLeftLegend = (TextView) findViewById(nameToId.get("MomentGraphLeft"));
            String momentLegentLeft = getResources().getString(R.string.label_moment_graph_low) + " - " + getResources().getString(R.string.nose) + "=" + new DecimalFormat("#.##").format(weightMinMoment) + " -";
            mmtGraphLeftLegend.setText(momentLegentLeft);
            TextView mmtGraphRightLegend = (TextView) findViewById(nameToId.get("MomentGraphRight"));
            String momentLegendRight = "- " + getResources().getString(R.string.tail) + "=" + new DecimalFormat("#.##").format(weightMaxMoment) + " - " + getResources().getString(R.string.label_moment_graph_high);
            mmtGraphRightLegend.setText(momentLegendRight);
            //update graph line: nose-----L----|-----H-----tail
            positionShiftInt = 0;
            finalPosition = 0;
            Double momentRange = weightMaxMoment - weightMinMoment;
            StringBuilder mmtRngBaseString = new StringBuilder(getResources().getString(R.string.graph_moment));
            Double mmtDifference = 0.0;
            Double mmtPercent = 0.0;
            if (weightTooFwd) {
                mmtDifference = weightMinMoment - totalMmt;
                mmtPercent = mmtDifference / momentRange;
                positionShiftInt = (int) Math.round(mmtPercent * 10);
                finalPosition = lPosition - 1 - positionShiftInt;
                if (finalPosition < 0) {
                    finalPosition = 0;
                }
            } else if (weightTooAft) {
                mmtDifference = totalMmt - weightMaxMoment;
                mmtPercent = mmtDifference / momentRange;
                positionShiftInt = (int) Math.round(mmtPercent * 10);
                finalPosition = hPosition + 1 + positionShiftInt;
                if (finalPosition > wtRngBaseString.length() - 1) {
                    finalPosition = wtRngBaseString.length() - 1;
                }
            } else {
                mmtDifference = totalMmt - weightMinMoment;
                mmtPercent = mmtDifference / momentRange;
                positionShiftInt = (int) Math.round(mmtPercent * 10);
                finalPosition = lPosition + 1 + positionShiftInt;
                if (finalPosition < lPosition + 1) {
                    finalPosition = lPosition + 1;
                } else if (finalPosition > hPosition - 1) {
                    finalPosition = hPosition - 1;
                }
            }
            mmtRngBaseString.setCharAt(finalPosition, '|');
            mmtGraphToChange.setText(mmtRngBaseString.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getResources();
        Intent intent = getIntent();
        String fileName = intent.getStringExtra(MainActivity.EXTRA_FILENAME);
        Boolean fileVerified = false;

        ScrollView sv = new ScrollView(this);
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);
        sv.addView(llMain);

        // Read in our file and verify it's format
        File dir = getFilesDir();
        ArrayList lines = new ArrayList<String>();
        String[] keyLines = res.getStringArray(R.array.key_lines);
        final HashMap<String, String> dataMap = new HashMap<String, String>();
        Boolean maxGrossFound = false;
        Boolean momentDivideFound = false;
        try {
            File file = new File(dir,fileName);
            FileInputStream inFile = new FileInputStream(file);
            //InputStream inFile = this.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inFile));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
            inFile.close();
            // Verify file starts/ends with START/END
            if (!lines.get(0).toString().equals("START") || !lines.get(lines.size()-1).toString().equals("END")) {
                Exception e = new Exception("File format not correct (missing START/END)");
                throw e;
            }
            //create arraylists to keep track of names to make sure they're unique
            ArrayList namesWT = new ArrayList<String>();
            ArrayList namesBAG = new ArrayList<String>();
            ArrayList namesROW = new ArrayList<String>();
            for (int i=0; i < lines.size(); i++) {
                String l = lines.get(i).toString();
                //ignore our KEY lines
                if (Arrays.asList(keyLines).contains(l)) {
                    continue;
                }
                String[] lineElements = l.split(",",-1);
                if (lineElements.length < 1) {
                    Exception e = new Exception("File format not correct (found empty line)");
                    throw e;
                }
                switch (lineElements[0]) {
                    case "WT":
                        //if line begins with WT it needs 4 elements (string, string, double, double)
                        if (lineElements.length != 4) {
                            Exception e = new Exception("File format not correct (found WT without 4 elements)");
                            throw e;
                        }
                        if (!lineElements[2].toString().matches("-?\\d+(\\.\\d+)?") || !lineElements[3].toString().matches("-?\\d+(\\.\\d+)?")) {
                            Exception e = new Exception("File format not correct (found WT without numbers in last 2 columns.)");
                            throw e;
                        }
                        //and the name must be unique
                        if (namesWT.contains(lineElements[1].toString())) {
                            Exception e = new Exception("Data error: Duplicate WT name.");
                            throw e;
                        } else {
                            namesWT.add(lineElements[1].toString());
                        }
                        break;
                    case "BAG":
                        //if line begins with BAG it needs 4 elements (string, string, double, double)
                        if (lineElements.length != 4) {
                            Exception e = new Exception("File format not correct (found BAG without 4 elements)");
                            throw e;
                        }
                        if (!lineElements[2].toString().matches("-?\\d+(\\.\\d+)?") || !lineElements[3].toString().matches("-?\\d+(\\.\\d+)?")) {
                            Exception e = new Exception("File format not correct (found BAG without numbers in last 2 columns.)");
                            throw e;
                        }
                        //and the name must be unique
                        if (namesBAG.contains(lineElements[1].toString())) {
                            Exception e = new Exception("Data error: Duplicate BAG name.");
                            throw e;
                        } else {
                            namesBAG.add(lineElements[1].toString());
                        }
                        break;
                    case "ROW":
                        //if line begins with ROW it needs 4 elements (string, string, double, int)
                        if (lineElements.length != 4) {
                            Exception e = new Exception("File format not correct (found ROW without 4 elements)");
                            throw e;
                        }
                        if (!lineElements[2].toString().matches("-?\\d+(\\.\\d+)?")) {
                            Exception e = new Exception("File format not correct (found ROW without number in 3rd column.)");
                            throw e;
                        }
                        if (!lineElements[3].toString().matches("\\d+")) {
                            Exception e = new Exception("File format not correct (found ROW without positive int in 4th column.)");
                            throw e;
                        }
                        //and the name must be unique
                        if (namesROW.contains(lineElements[1].toString())) {
                            Exception e = new Exception("Data error: Duplicate ROW name.");
                            throw e;
                        } else {
                            namesROW.add(lineElements[1].toString());
                        }
                        break;
                    case "DATA":
                        //Data lines must be 3 elements (String, String, String)
                        if (lineElements.length != 3) {
                            Exception e = new Exception("File format not correct (found DATA without 3 elements)");
                            throw e;
                        }
                        //MaxGross must have a valid Double.
                        if (lineElements[1].equals("MaxGross")) {
                            if (!(lineElements[2]).matches("-?\\d+(\\.\\d+)?")) {
                                Exception e = new Exception("Data error: MaxGross must be a ##.# number.");
                                throw e;
                            }
                            maxGrossFound = true;
                        }
                        //MomentDivide must have a valid Double and not be 0
                        if (lineElements[1].equals("MomentDivide")) {
                            if (!(lineElements[2]).matches("\\d+(\\.\\d+)?")) {
                                Exception e = new Exception("Data error: MomentDivide must be a  positive number.");
                                throw e;
                            }
                            if (!(Double.parseDouble(lineElements[2]) >= 1.0)) {
                                Exception e = new Exception("Data error: MomentDivide must be a positive number >= 1.");
                                throw e;
                            }
                            momentDivideFound = true;
                        }
                        dataMap.put(lineElements[1], lineElements[2]);
                        break;
                    case "ENV":
                        //ENVelope lines must have 4 elements (String, Double, Double, Double)
                        if (lineElements.length != 4) {
                            Exception e = new Exception("File format not correct (found ENV without 4 elements)");
                            throw e;
                        }
                        if (!(lineElements[1]).matches("-?\\d+(\\.\\d+)?")) {
                            Exception e = new Exception("Data error: ENV plots must be numbers");
                            throw e;
                        }
                        if (!(lineElements[2]).matches("-?\\d+(\\.\\d+)?")) {
                            Exception e = new Exception("Data error: ENV plots must be numbers");
                            throw e;
                        }
                        if (!(lineElements[3]).matches("-?\\d+(\\.\\d+)?")) {
                            Exception e = new Exception("Data error: ENV plots must be numbers");
                            throw e;
                        }
                        if (Double.parseDouble(lineElements[3]) - Double.parseDouble(lineElements[2]) < 0) {
                            Exception e = new Exception("Data error: ENV plots - low moment must be on left.");
                            throw e;
                        }
                        break;
                    default:
                        Exception e = new Exception("File format not correct (found unexpected line)");
                        throw e;
                }
            }
            if (!maxGrossFound) {
                Exception e = new Exception("Missing Data: MaxGross not found.");
                throw e;
            }
            if (!momentDivideFound) {
                Exception e = new Exception("Missing Data: MomentDivide not found.");
                throw e;
            }
            fileVerified = true;
        } catch (Exception e) {
            Log.e("File Error", "Error opening file '" + fileName + "'", e);
            Toast.makeText(this, "Error opening file '" + fileName + "'", Toast.LENGTH_LONG).show();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            TextView tvError = new TextView(this);
            tvError.setText("Error opening file '" + fileName + "'");
            llMain.addView(tvError);
            TextView tvError2 = new TextView(this);
            tvError2.setText(e.toString());
            llMain.addView(tvError2);
        }

        //Process our file and lay out our values.
        LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        int currentId = 1000;
        final ArrayList weightIds = new ArrayList<Integer>();
        final ArrayList momentIds = new ArrayList<Integer>();
        final HashMap<String, Integer> nameToId = new HashMap<String, Integer>();
        final HashMap<Integer, Integer> weightIdToMomentIdMap = new HashMap<Integer, Integer>();
        final HashMap<Integer, Double> weightIdToWeightMap = new HashMap<Integer, Double>();
        final HashMap<Integer, Double> weightIdToArmMap = new HashMap<Integer, Double>();
        final HashMap<Integer, Double> momentIdToMomentMap = new HashMap<Integer, Double>();
        final HashMap<Integer, ArrayList<Integer>> rowTotalWeightIdToPaxWeightIds = new HashMap<Integer, ArrayList<Integer>>();
        final HashMap<Double,String> envWeightToRange = new HashMap<Double,String>();
        if (fileVerified) {
            Boolean focusMoved = false;
            final Double momentDivide = Double.parseDouble(dataMap.get("MomentDivide"));
            String momentDivideString =  "/" + dataMap.get("MomentDivide");
            if (momentDivide.equals(Double.parseDouble("1.0"))) {
                momentDivideString = "";
            }
            //create our weight / arm / moment view.
            //title area
            LinearLayout llTitle = new LinearLayout(this);
            llTitle.setOrientation(LinearLayout.HORIZONTAL);
            llTitle.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
            String templateName = fileName.replace(".csv","").replace("ga_wb_","");
            TextView tvTemplateName = new TextView(this);
            tvTemplateName.setText(templateName);
            tvTemplateName.setLayoutParams(rowLayoutParams);
            llTitle.addView(tvTemplateName);
            llMain.addView(llTitle);
            //weight tables
            LinearLayout llWeightArmMoment = new LinearLayout(this);
            llWeightArmMoment.setOrientation(LinearLayout.VERTICAL);
            LinearLayout llColumnTitles = new LinearLayout(this);
            llColumnTitles.setOrientation(LinearLayout.HORIZONTAL);
            llColumnTitles.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
            //set up our column names
            TextView tvNameLabel = new TextView(this);
            tvNameLabel.setText(res.getString(R.string.label_name));
            tvNameLabel.setLayoutParams(rowLayoutParams);
            llColumnTitles.addView(tvNameLabel);
            TextView tvWeightLabel = new TextView(this);
            tvWeightLabel.setText(res.getString(R.string.label_weight));
            tvWeightLabel.setLayoutParams(rowLayoutParams);
            llColumnTitles.addView(tvWeightLabel);
            TextView tvArmLabel = new TextView(this);
            tvArmLabel.setText(res.getString(R.string.label_arm));
            tvArmLabel.setLayoutParams(rowLayoutParams);
            llColumnTitles.addView(tvArmLabel);
            TextView tvMomentLabel = new TextView(this);
            tvMomentLabel.setText(res.getString(R.string.label_moment) + momentDivideString);
            tvMomentLabel.setLayoutParams(rowLayoutParams);
            llColumnTitles.addView(tvMomentLabel);
            llWeightArmMoment.addView(llColumnTitles);
            //loop through our lines and parse our data, laying out our weight tables.
            for (int i=0; i < lines.size(); i++) {
                String l = lines.get(i).toString();
                //ignore our KEY lines
                if (Arrays.asList(keyLines).contains(l)) {
                    continue;
                }
                String[] lineElements = l.split(",",-1);
                switch (lineElements[0]) {
                    case "WT":
                    case "BAG":
                        String name = lineElements[1];
                        Double weight = Double.parseDouble(lineElements[2]);
                        Double arm = Double.parseDouble(lineElements[3]);
                        Double moment = weight * arm / momentDivide;
                        //set up our ids so we can keep track of which weight is multiplied by which arm, etc.
                        final Integer idWeight = currentId++;
                        Integer idArm = currentId++;
                        Integer idMoment = currentId++;
                        weightIdToMomentIdMap.put(idWeight, idMoment);
                        //set up our data storage (for easier calculation later)
                        weightIds.add(idWeight);
                        momentIds.add(idMoment);
                        weightIdToWeightMap.put(idWeight, weight);
                        weightIdToArmMap.put(idWeight, arm);
                        momentIdToMomentMap.put(idMoment, moment);
                        //lay out our row
                        LinearLayout llWeightRow = new LinearLayout(this);
                        llWeightRow.setOrientation(LinearLayout.HORIZONTAL);
                        llWeightRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
                        //name
                        TextView tvName = new TextView(this);
                        tvName.setText(name);
                        tvName.setLayoutParams(rowLayoutParams);
                        llWeightRow.addView(tvName);
                        //weight
                        final EditText etWeight = new EditText(this);
                        etWeight.setId(idWeight);
                        if (nameToId.get("firstWeightId") == null && !(name.equals("Empty"))) {
                            nameToId.put("firstWeightId", etWeight.getId());
                        }
                        nameToId.put("lastWeightId", etWeight.getId());
                        etWeight.setText(weight.toString());
                        etWeight.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        etWeight.setSelectAllOnFocus(true);
                        etWeight.setLayoutParams(rowLayoutParams);
                        if (name.equals("Empty")) {
                            etWeight.setEnabled(false);
                        } else {
                            if (!focusMoved) {
                                focusMoved = true;
                                etWeight.requestFocus();
                            }
                        }
                        //any time the weight changes, update our moment (weight * arm) and our total weight and moment.
                        etWeight.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                //update moment and totals after weight is changed
                                int currentWeightId = etWeight.getId();
                                String currentWeightString = etWeight.getText().toString();
                                Double currentWeight = 99999.9;
                                if (currentWeightString.matches("-?\\d+(\\.\\d+)?")) {
                                    currentWeight = Double.parseDouble(currentWeightString);
                                }
                                weightIdToWeightMap.put(currentWeightId, currentWeight);
                                Double currentArm = (Double) weightIdToArmMap.get(currentWeightId);
                                Double currentMoment = currentWeight * currentArm / momentDivide;
                                TextView viewToChangeMoment = (TextView) findViewById(weightIdToMomentIdMap.get(currentWeightId));
                                momentIdToMomentMap.put(viewToChangeMoment.getId(), currentMoment);
                                viewToChangeMoment.setText(new DecimalFormat("#.##").format(currentMoment));
                                updateTotals(weightIds, momentIds, nameToId, weightIdToWeightMap, momentIdToMomentMap, envWeightToRange, dataMap);
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });
                        llWeightRow.addView(etWeight);
                        //arm
                        TextView tvArm = new TextView(this);
                        tvArm.setId(idArm);
                        tvArm.setText(arm.toString());
                        tvArm.setLayoutParams(rowLayoutParams);
                        llWeightRow.addView(tvArm);
                        //moment
                        TextView tvMoment = new TextView(this);
                        tvMoment.setId(idMoment);
                        tvMoment.setText(new DecimalFormat("#.##").format(moment));
                        tvMoment.setLayoutParams(rowLayoutParams);
                        llWeightRow.addView(tvMoment);
                        //add to Weight/Arm/Moment view.
                        llWeightArmMoment.addView(llWeightRow);
                        break;
                    case "ROW":
                        //make vertical set of fields for weights (on per pax), 1 arm text, and 1 moment text.
                        String rowName = lineElements[1].toString();
                        Double rowArm = Double.parseDouble(lineElements[2]);
                        Integer numSeats = Integer.parseInt(lineElements[3]);
                        //set up our data storage (for easier calculation later)
                        //set up our ids so we can keep track of which weight is multiplied by which arm, etc.
                        final Integer idPaxrowTotalWeight = currentId++;
                        rowTotalWeightIdToPaxWeightIds.put(idPaxrowTotalWeight, new ArrayList<Integer>());
                        Integer idPaxrowArm = currentId++;
                        Integer idPaxrowMoment = currentId++;
                        //set up our data storage (for easier calculation later)
                        weightIds.add(idPaxrowTotalWeight);
                        momentIds.add(idPaxrowMoment);
                        weightIdToWeightMap.put(idPaxrowTotalWeight, 0.0);
                        weightIdToArmMap.put(idPaxrowTotalWeight, rowArm);
                        weightIdToMomentIdMap.put(idPaxrowTotalWeight, idPaxrowMoment);
                        momentIdToMomentMap.put(idPaxrowMoment, 0.0);
                        //create our row
                        LinearLayout llPaxrowWeightRow = new LinearLayout(this);
                        llPaxrowWeightRow.setOrientation(LinearLayout.HORIZONTAL);
                        llPaxrowWeightRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
                        //name
                        TextView tvPaxrowName = new TextView(this);
                        tvPaxrowName.setText(rowName);
                        tvPaxrowName.setLayoutParams(rowLayoutParams);
                        llPaxrowWeightRow.addView(tvPaxrowName);
                        //weights
                        LinearLayout llPaxrowWeightColumn = new LinearLayout(this);
                        llPaxrowWeightColumn.setLayoutParams(rowLayoutParams);
                        llPaxrowWeightColumn.setOrientation(LinearLayout.VERTICAL);
                        for (int j=0; j < numSeats; j++) {
                            final EditText etPaxWeight = new EditText(this);
                            Integer idPaxWeight = currentId++;
                            etPaxWeight.setId(idPaxWeight);
                            weightIdToWeightMap.put(idPaxWeight, 0.0);
                            nameToId.put("lastWeightId", etPaxWeight.getId());
                            etPaxWeight.setText("0.0");
                            etPaxWeight.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            etPaxWeight.setSelectAllOnFocus(true);
                            etPaxWeight.setLayoutParams(rowLayoutParams);
                            //add individual pax weight to totalpaxweight ID to paxweight IDs map.
                            ArrayList<Integer> tmpArrayList = rowTotalWeightIdToPaxWeightIds.get(idPaxrowTotalWeight);
                            tmpArrayList.add(idPaxWeight);
                            rowTotalWeightIdToPaxWeightIds.put(idPaxrowTotalWeight, tmpArrayList);
                            //any time the weight changes, update our moment (weight * arm) and our total weight and moment.
                            etPaxWeight.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    //update total weight with all pax from this row
                                    int currentWeightId = etPaxWeight.getId();
                                    Double tmpTotalWeight = 0.0;
                                    String currentWeightString = etPaxWeight.getText().toString();
                                    if (currentWeightString.matches("-?\\d+(\\.\\d+)?")) {
                                        tmpTotalWeight += Double.parseDouble(currentWeightString);
                                    } else {
                                        tmpTotalWeight = 99999.9;
                                    }
                                    ArrayList<Integer> tmpWeightIds = rowTotalWeightIdToPaxWeightIds.get(idPaxrowTotalWeight);
                                    for (int j=0; j < tmpWeightIds.size(); j++) {
                                        if (tmpWeightIds.get(j).equals(currentWeightId)) {
                                            continue;
                                        }
                                        EditText otherWeightTxtBox = (EditText)findViewById(tmpWeightIds.get(j));
                                        String otherWeightString = otherWeightTxtBox.getText().toString();
                                        if (otherWeightString.matches("-?\\d+(\\.\\d+)?")) {
                                            tmpTotalWeight += Double.parseDouble(otherWeightString);
                                        } else {
                                            tmpTotalWeight = 99999.9;
                                        }
                                    }
                                    weightIdToWeightMap.put(idPaxrowTotalWeight, tmpTotalWeight);
                                    //update moment and totals.
                                    Double currentArm = (Double) weightIdToArmMap.get(idPaxrowTotalWeight);
                                    Double currentMoment = tmpTotalWeight * currentArm / momentDivide;
                                    TextView viewToChangeMoment = (TextView) findViewById(weightIdToMomentIdMap.get(idPaxrowTotalWeight));
                                    momentIdToMomentMap.put(viewToChangeMoment.getId(), currentMoment);
                                    viewToChangeMoment.setText(new DecimalFormat("#.##").format(currentMoment));
                                    updateTotals(weightIds, momentIds, nameToId, weightIdToWeightMap, momentIdToMomentMap, envWeightToRange, dataMap);
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {
                                }
                            });
                            llPaxrowWeightColumn.addView(etPaxWeight);
                        }
                        llPaxrowWeightRow.addView(llPaxrowWeightColumn);
                        //arm
                        TextView tvPaxrowArm = new TextView(this);
                        tvPaxrowArm.setId(idPaxrowArm);
                        tvPaxrowArm.setText(rowArm.toString());
                        tvPaxrowArm.setLayoutParams(rowLayoutParams);
                        llPaxrowWeightRow.addView(tvPaxrowArm);
                        //moment
                        TextView tvPaxrowMoment = new TextView(this);
                        tvPaxrowMoment.setId(idPaxrowMoment);
                        tvPaxrowMoment.setText("0.0");
                        tvPaxrowMoment.setLayoutParams(rowLayoutParams);
                        llPaxrowWeightRow.addView(tvPaxrowMoment);
                        //add to weight/arm/moment view
                        llWeightArmMoment.addView(llPaxrowWeightRow);
                        break;
                    case "DATA":
                        dataMap.put(lineElements[1], lineElements[2]);
                        break;
                    case "ENV":
                        //build our envelope data set
                        envWeightToRange.put(Double.parseDouble(lineElements[1]), lineElements[2] + "<>" + lineElements[3]);
                        break;
                }
            }
            //add weight section to main view.
            llMain.addView(llWeightArmMoment);

            //calculate our total weight and moment and add to view.
            Integer idTotalWeight = currentId++;
            Integer idTotalMoment = currentId++;
            nameToId.put("totalWeight", idTotalWeight);
            nameToId.put("totalMoment", idTotalMoment);
            LinearLayout llTotalRow = new LinearLayout(this);
            llTotalRow.setOrientation(LinearLayout.HORIZONTAL);
            llTotalRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
            TextView tvTotalWeightLabel = new TextView(this);
            tvTotalWeightLabel.setLayoutParams(rowLayoutParams);
            tvTotalWeightLabel.setText(R.string.total_weight_label);
            llTotalRow.addView(tvTotalWeightLabel);
            TextView tvTotalWeight = new TextView(this);
            tvTotalWeight.setId(idTotalWeight);
            tvTotalWeight.setLayoutParams(rowLayoutParams);
            tvTotalWeight.setText("0000");
            llTotalRow.addView(tvTotalWeight);
            TextView tvTotalMomentLabel = new TextView(this);
            tvTotalMomentLabel.setLayoutParams(rowLayoutParams);
            String tvTotalMomentLabelString = res.getString(R.string.total_moment_label) + momentDivideString + ":";
            tvTotalMomentLabel.setText(tvTotalMomentLabelString);
            llTotalRow.addView(tvTotalMomentLabel);
            TextView tvTotalMoment = new TextView(this);
            tvTotalMoment.setId(idTotalMoment);
            tvTotalMoment.setLayoutParams(rowLayoutParams);
            tvTotalMoment.setText("00000");
            llTotalRow.addView(tvTotalMoment);
            llMain.addView(llTotalRow);

            //add rows for representing our weight result and graph.
            SortedSet<Double> envKeys = new TreeSet<Double>(envWeightToRange.keySet());
            Double lowestWeight = -10.0;
            for (Double w : envKeys) {
                if (lowestWeight.equals(-10.0) || w < lowestWeight) {
                    lowestWeight = w;
                }
            }

            LinearLayout llWeightResultRow = new LinearLayout(this);
            llWeightResultRow.setOrientation(LinearLayout.HORIZONTAL);
            llWeightResultRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            TextView tvWeightResultLabel = new TextView(this);
            tvWeightResultLabel.setText(R.string.label_weight_result);
            llWeightResultRow.addView(tvWeightResultLabel);

            TextView tvWeightResultStatus = new TextView(this);
            tvWeightResultStatus.setId(currentId++);
            tvWeightResultStatus.setLayoutParams(rowLayoutParams);
            nameToId.put("WeightResult", tvWeightResultStatus.getId());
            tvWeightResultStatus.setText(R.string.label_weight_result_light);
            llWeightResultRow.addView(tvWeightResultStatus);

            llMain.addView(llWeightResultRow);

            LinearLayout llWeightResultGraph = new LinearLayout(this);
            llWeightResultGraph.setOrientation(LinearLayout.VERTICAL);
            llWeightResultGraph.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            LinearLayout llWeightResultGraphRow1 = new LinearLayout(this);
            llWeightResultGraphRow1.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvWeightGraphLabelLeft = new TextView(this);
            tvWeightGraphLabelLeft.setLayoutParams(rowLayoutParams);
            tvWeightGraphLabelLeft.setGravity(Gravity.RIGHT);
            tvWeightGraphLabelLeft.setText("<");
            llWeightResultGraphRow1.addView(tvWeightGraphLabelLeft);

            TextView tvWeightGraph = new TextView(this);
            tvWeightGraph.setId(currentId++);
            nameToId.put("WeightGraph", tvWeightGraph.getId());
            tvWeightGraph.setTypeface(Typeface.MONOSPACE);
            tvWeightGraph.setText(R.string.graph_weight);
            llWeightResultGraphRow1.addView(tvWeightGraph);

            TextView tvWeightGraphLabelRight = new TextView(this);
            tvWeightGraphLabelRight.setLayoutParams(rowLayoutParams);
            tvWeightGraphLabelRight.setGravity(Gravity.LEFT);
            tvWeightGraphLabelRight.setText(">");
            llWeightResultGraphRow1.addView(tvWeightGraphLabelRight);

            llWeightResultGraph.addView(llWeightResultGraphRow1);

            LinearLayout llWeightResultGraphRow2 = new LinearLayout(this);
            llWeightResultGraphRow2.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvWeightGraphLegendLeft = new TextView(this);
            tvWeightGraphLegendLeft.setLayoutParams(rowLayoutParams);
            tvWeightGraphLegendLeft.setGravity(Gravity.RIGHT);
            String weightLegendLeft = res.getString(R.string.label_weight_graph_low) + " - " + res.getString(R.string.light) + "=" + lowestWeight.toString() + " -";
            tvWeightGraphLegendLeft.setText(weightLegendLeft);
            llWeightResultGraphRow2.addView(tvWeightGraphLegendLeft);

            TextView tvWeightGraphLegendRight = new TextView(this);
            tvWeightGraphLegendRight.setLayoutParams(rowLayoutParams);
            tvWeightGraphLegendRight.setGravity(Gravity.LEFT);
            String weightLegendRight = "- " + res.getString(R.string.heavy) + "=" + dataMap.get("MaxGross").toString() + " - " + res.getString(R.string.label_weight_graph_high);
            tvWeightGraphLegendRight.setText(weightLegendRight);
            llWeightResultGraphRow2.addView(tvWeightGraphLegendRight);

            llWeightResultGraph.addView(llWeightResultGraphRow2);

            llMain.addView(llWeightResultGraph);

            //add rows for representing our moment result and graph
            LinearLayout llMomentResultRow = new LinearLayout(this);
            llMomentResultRow.setOrientation(LinearLayout.HORIZONTAL);
            llMomentResultRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            TextView tvMomentResultLabel = new TextView(this);
            tvMomentResultLabel.setText(R.string.label_moment_result);
            llMomentResultRow.addView(tvMomentResultLabel);

            TextView tvMomentResultStatus = new TextView(this);
            tvMomentResultStatus.setId(currentId++);
            tvMomentResultStatus.setLayoutParams(rowLayoutParams);
            nameToId.put("MomentResult", tvMomentResultStatus.getId());
            tvMomentResultStatus.setText(R.string.label_moment_result_nose);
            llMomentResultRow.addView(tvMomentResultStatus);

            llMain.addView(llMomentResultRow);

            LinearLayout llMomentResultGraph = new LinearLayout(this);
            llMomentResultGraph.setOrientation(LinearLayout.VERTICAL);
            llMomentResultGraph.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

            LinearLayout llMomentResultGraphRow1 = new LinearLayout(this);
            llMomentResultGraphRow1.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvMomentGraphLabelLeft = new TextView(this);
            tvMomentGraphLabelLeft.setLayoutParams(rowLayoutParams);
            tvMomentGraphLabelLeft.setGravity(Gravity.RIGHT);
            tvMomentGraphLabelLeft.setText("<");
            llMomentResultGraphRow1.addView(tvMomentGraphLabelLeft);

            TextView tvMomentGraph = new TextView(this);
            tvMomentGraph.setId(currentId++);
            nameToId.put("MomentGraph", tvMomentGraph.getId());
            tvMomentGraph.setTypeface(Typeface.MONOSPACE);
            tvMomentGraph.setText(R.string.graph_moment);
            llMomentResultGraphRow1.addView(tvMomentGraph);

            TextView tvMomentGraphLabelRight = new TextView(this);
            tvMomentGraphLabelRight.setLayoutParams(rowLayoutParams);
            tvMomentGraphLabelRight.setGravity(Gravity.LEFT);
            tvMomentGraphLabelRight.setText(">");
            llMomentResultGraphRow1.addView(tvMomentGraphLabelRight);

            llMomentResultGraph.addView(llMomentResultGraphRow1);

            LinearLayout llMomentResultGraphRow2 = new LinearLayout(this);
            llMomentResultGraphRow2.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvMomentGraphLegendLeft = new TextView(this);
            tvMomentGraphLegendLeft.setId(currentId++);
            nameToId.put("MomentGraphLeft", tvMomentGraphLegendLeft.getId());
            tvMomentGraphLegendLeft.setLayoutParams(rowLayoutParams);
            tvMomentGraphLegendLeft.setGravity(Gravity.RIGHT);
            tvMomentGraphLegendLeft.setText(R.string.label_moment_graph_low);
            llMomentResultGraphRow2.addView(tvMomentGraphLegendLeft);

            TextView tvMomentGraphlegendRight = new TextView(this);
            tvMomentGraphlegendRight.setId(currentId++);
            nameToId.put("MomentGraphRight", tvMomentGraphlegendRight.getId());
            tvMomentGraphlegendRight.setLayoutParams(rowLayoutParams);
            tvMomentGraphlegendRight.setGravity(Gravity.LEFT);
            tvMomentGraphlegendRight.setText(R.string.label_moment_graph_high);
            llMomentResultGraphRow2.addView(tvMomentGraphlegendRight);

            llMomentResultGraph.addView(llMomentResultGraphRow2);

            llMain.addView(llMomentResultGraph);

        }

        // Now we show our main view.
        setContentView(sv);
        //lastly, run our first calc to update totals.
        if (fileVerified) {
            //I also want to change my last "weight" field to send focus to the first weight field vs just doing nothing.
            EditText viewToChangeNextFocus = (EditText) findViewById(nameToId.get("lastWeightId"));
            viewToChangeNextFocus.setNextFocusDownId(nameToId.get("firstWeightId"));
            //and we'll update our totals so the user starts with accurate calculations.
            updateTotals(weightIds, momentIds, nameToId, weightIdToWeightMap, momentIdToMomentMap, envWeightToRange, dataMap);
        }
    }


}
