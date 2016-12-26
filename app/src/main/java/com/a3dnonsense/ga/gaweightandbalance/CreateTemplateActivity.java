package com.a3dnonsense.ga.gaweightandbalance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateTemplateActivity extends AppCompatActivity {
    private class NamedViews {
        EditText etTailNum;
        EditText etModel;
        EditText etEmptyWeight;
        String weightUnits;
        String armUnits;
        ArrayList<TextView> weightUnitLabels;
        ArrayList<TextView> armUnitLabels;
        EditText etFuelWeight;
        EditText etFuelArm;
        EditText etOilWeight;
        EditText etOilArm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_template_activity);

        final Resources res = getResources();

        final NamedViews namedViews = new NamedViews();
        namedViews.weightUnitLabels = new ArrayList<>();
        namedViews.armUnitLabels = new ArrayList<>();

        final int colorClickable = Color.rgb(0,0,180);

        final ScrollView svMain = (ScrollView)findViewById(R.id.activity_create_template_activity);
        final LinearLayout llMain = (LinearLayout)findViewById(R.id.create_template_main_vertical_layout);

        final LinearLayout.LayoutParams rlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        final LinearLayout.LayoutParams llParamsVertical = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.VERTICAL);

        final LinearLayout.LayoutParams llParamsHorizontal = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.HORIZONTAL);

        final LinearLayout.LayoutParams tvParamsWeight1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        final LinearLayout.LayoutParams tvParamsWeight0 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        final LinearLayout.LayoutParams fourP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 4);

        final TableLayout.LayoutParams tableParamsWrapContent = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        final TableRow.LayoutParams tableRowParamsWeight1 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f);
        final TableRow.LayoutParams tableRowParamsWeight0 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

        //create our aircraft class to hold our input data
        final AircraftClass aircraft = new AircraftClass();

        //create some hashmaps to hold data that could change before adding to aircraft class
        final HashMap<String, Double> hmNameToWeight = new HashMap<>();
        final HashMap<String, Double> hmNameToArm = new HashMap<>();

        //create sparse arrays to hold info for our dynamically-generated row info
        final SparseArray<String> spaPaxRowIdToName = new SparseArray<>();
        final SparseArray<Double> spaPaxRowIdToArm = new SparseArray<>();
        final SparseIntArray spaPaxRowIdToNumSeats = new SparseIntArray();

        final SparseArray<String> spaBagAreaIdToName = new SparseArray<>();
        final SparseArray<Double> spaBagAreaIdToArm = new SparseArray<>();

        //tail number / model instructions
        TextView tvNameInstruct = new TextView(this);
        tvNameInstruct.setLayoutParams( tvParamsWeight1);
        tvNameInstruct.setText(R.string.instructions_name);
        llMain.addView(tvNameInstruct);

        final int colorOriginal = tvNameInstruct.getCurrentTextColor();

        //tail number / model entry
        LinearLayout llNameEntry = new LinearLayout((this));
        llNameEntry.setLayoutParams(llParamsHorizontal);
        llNameEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(llNameEntry);

        TextView tvTailNumLabel = new TextView(this);
        tvTailNumLabel.setLayoutParams(tvParamsWeight1);
        tvTailNumLabel.setText(R.string.label_tail_num);
        llNameEntry.addView(tvTailNumLabel);

        EditText etTailNum = new EditText(this);
        etTailNum.setLayoutParams(tvParamsWeight1);
        etTailNum.setHint("N12345");
        etTailNum.setSelectAllOnFocus(true);
        etTailNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                aircraft.tailNumber = ((EditText)view).getText().toString();
            }
        });
        llNameEntry.addView(etTailNum);
        namedViews.etTailNum = etTailNum;

        TextView tvModelLabel = new TextView(this);
        tvModelLabel.setLayoutParams(tvParamsWeight1);
        tvModelLabel.setText(R.string.label_model);
        llNameEntry.addView(tvModelLabel);

        EditText etModel = new EditText(this);
        etModel.setLayoutParams(tvParamsWeight1);
        etModel.setHint("c172");
        etModel.setSelectAllOnFocus(true);
        etModel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                aircraft.model = ((EditText)view).getText().toString();
            }
        });
        llNameEntry.addView(etModel);
        namedViews.etModel = etModel;

        LinearLayout llNameDivider = new LinearLayout(this);
        llNameDivider.setLayoutParams(fourP);
        llNameDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llNameDivider);

        //unit instructions
        TextView tvUnitInstruct = new TextView(this);
        tvUnitInstruct.setLayoutParams( tvParamsWeight1);
        tvUnitInstruct.setText(R.string.instructions_units);
        llMain.addView(tvUnitInstruct);

        //unit entry
        LinearLayout llUnitEntry = new LinearLayout((this));
        llUnitEntry.setLayoutParams(llParamsHorizontal);
        llUnitEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(llUnitEntry);

        TextView tvWeightUnitLabel = new TextView(this);
        tvWeightUnitLabel.setLayoutParams(tvParamsWeight0);
        String weightUnitLabel = res.getString(R.string.label_weight) + ": ";
        tvWeightUnitLabel.setText(weightUnitLabel);
        llUnitEntry.addView(tvWeightUnitLabel);

        Spinner spWeightUnits = new Spinner(this);
        ArrayAdapter<String> spinnerWeightUnitsArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.weight_units));
        spWeightUnits.setAdapter(spinnerWeightUnitsArrayAdapter);
        spWeightUnits.setGravity(Gravity.START);
        llUnitEntry.addView(spWeightUnits);
        namedViews.weightUnits = spWeightUnits.getSelectedItem().toString();
        spWeightUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for (TextView tv : namedViews.weightUnitLabels) {
                    tv.setText(adapterView.getSelectedItem().toString());
                }
                namedViews.weightUnits = adapterView.getSelectedItem().toString();
                aircraft.weightUnits = namedViews.weightUnits;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        TextView tvUnitsSpacer = new TextView(this);
        tvUnitsSpacer.setLayoutParams(tvParamsWeight1);
        tvUnitsSpacer.setText("");
        llUnitEntry.addView(tvUnitsSpacer);

        TextView tvArmUnitLabel = new TextView(this);
        tvArmUnitLabel.setLayoutParams(tvParamsWeight0);
        String armUnitLabel = res.getString(R.string.label_arm) + ": ";
        tvArmUnitLabel.setText(armUnitLabel);
        llUnitEntry.addView(tvArmUnitLabel);

        Spinner spArmUnits = new Spinner(this);
        ArrayAdapter<String> spinnerArmUnitsArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.arm_units));
        spArmUnits.setAdapter(spinnerArmUnitsArrayAdapter);
        spArmUnits.setGravity(Gravity.START);
        llUnitEntry.addView(spArmUnits);
        namedViews.armUnits = spArmUnits.getSelectedItem().toString();
        spArmUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for (TextView tv : namedViews.armUnitLabels) {
                    tv.setText(adapterView.getSelectedItem().toString());
                }
                namedViews.armUnits = adapterView.getSelectedItem().toString();
                aircraft.armUnits = namedViews.armUnits;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        LinearLayout llUnitDivider = new LinearLayout(this);
        llUnitDivider.setLayoutParams(fourP);
        llUnitDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llUnitDivider);

        //empty weight instructions
        TextView tvEmptyInstruct = new TextView(this);
        tvEmptyInstruct.setLayoutParams(tvParamsWeight1);
        tvEmptyInstruct.setText(R.string.instructions_empty_weight);
        llMain.addView(tvEmptyInstruct);

        //empty weight entry
        LinearLayout llEmptyEntry = new LinearLayout((this));
        llEmptyEntry.setLayoutParams(llParamsHorizontal);
        llEmptyEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(llEmptyEntry);

        TextView tvEmptyWeightLabel = new TextView(this);
        tvEmptyWeightLabel.setLayoutParams(tvParamsWeight0);
        String emptyWeightLabel = res.getString(R.string.label_weight) + " (" + res.getString(R.string.empty) + "):";
        tvEmptyWeightLabel.setText(emptyWeightLabel);
        llEmptyEntry.addView(tvEmptyWeightLabel);

        final EditText etEmptyWeight = new EditText(this);
        etEmptyWeight.setLayoutParams(tvParamsWeight1);
        etEmptyWeight.setText("0.0");
        etEmptyWeight.setKeyListener(DigitsKeyListener.getInstance("0123456789-."));
        etEmptyWeight.setSelectAllOnFocus(true);
        etEmptyWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                try {
                    aircraft.emptyWeight = Double.parseDouble(((EditText)view).getText().toString());
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etEmptyWeight.setText("0.0");
                }
            }
        });
        llEmptyEntry.addView(etEmptyWeight);
        namedViews.etEmptyWeight = etEmptyWeight;

        TextView tvEmptyWeightUnitLabel = new TextView(this);
        tvEmptyWeightUnitLabel.setLayoutParams(tvParamsWeight0);
        tvEmptyWeightUnitLabel.setText(namedViews.weightUnits);
        llEmptyEntry.addView(tvEmptyWeightUnitLabel);
        namedViews.weightUnitLabels.add(tvEmptyWeightUnitLabel);

        LinearLayout llEmptyDivider = new LinearLayout(this);
        llEmptyDivider.setLayoutParams(fourP);
        llEmptyDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llEmptyDivider);

        //fuel / oil instructions
        TextView tvFuelOilInstruct = new TextView(this);
        tvFuelOilInstruct.setLayoutParams(tvParamsWeight1);
        tvFuelOilInstruct.setText(R.string.instructions_fuel_oil);
        llMain.addView(tvFuelOilInstruct);

        //fuel / oil entry
        LinearLayout llFuelEntry = new LinearLayout((this));
        llFuelEntry.setLayoutParams(llParamsHorizontal);
        llFuelEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(llFuelEntry);

        TextView tvFuelWeightLabel = new TextView(this);
        tvFuelWeightLabel.setLayoutParams(tvParamsWeight0);
        tvFuelWeightLabel.setText(R.string.label_fuel_weight);
        llFuelEntry.addView(tvFuelWeightLabel);

        final EditText etFuelWeight = new EditText(this);
        etFuelWeight.setLayoutParams(tvParamsWeight1);
        etFuelWeight.setText("0.0");
        etFuelWeight.setKeyListener(DigitsKeyListener.getInstance("0123456789-."));
        etFuelWeight.setSelectAllOnFocus(true);
        etFuelWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                try {
                    Double fuelWeight = Double.parseDouble(((EditText)view).getText().toString());
                    hmNameToWeight.put(res.getString(R.string.fuel), fuelWeight);
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etFuelWeight.setText("0.0");
                }
            }
        });
        llFuelEntry.addView(etFuelWeight);
        namedViews.etFuelWeight = etFuelWeight;

        TextView tvFuelWeightUnitLabel = new TextView(this);
        tvFuelWeightUnitLabel.setLayoutParams(tvParamsWeight0);
        tvFuelWeightUnitLabel.setText(namedViews.weightUnits);
        llFuelEntry.addView(tvFuelWeightUnitLabel);
        namedViews.weightUnitLabels.add(tvFuelWeightUnitLabel);

        TextView tvFuelArmLabel = new TextView(this);
        tvFuelArmLabel.setLayoutParams(tvParamsWeight0);
        String fuelArmLabel = " / " + res.getString(R.string.label_arm) + ": ";
        tvFuelArmLabel.setText(fuelArmLabel);
        llFuelEntry.addView(tvFuelArmLabel);

        final EditText etFuelArm = new EditText(this);
        etFuelArm.setLayoutParams(tvParamsWeight1);
        etFuelArm.setText("0.0");
        etFuelArm.setKeyListener(DigitsKeyListener.getInstance("0123456789-."));
        etFuelArm.setSelectAllOnFocus(true);
        etFuelArm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                try {
                    Double fuelArm = Double.parseDouble(((EditText)view).getText().toString());
                    hmNameToArm.put(res.getString(R.string.fuel), fuelArm);
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etFuelArm.setText("0.0");
                }
            }
        });
        llFuelEntry.addView(etFuelArm);
        namedViews.etFuelArm = etFuelArm;

        TextView tvFuelArmUnitLabel = new TextView(this);
        tvFuelArmUnitLabel.setLayoutParams(tvParamsWeight0);
        tvFuelArmUnitLabel.setText(namedViews.armUnits);
        llFuelEntry.addView(tvFuelArmUnitLabel);
        namedViews.armUnitLabels.add(tvFuelArmUnitLabel);

        LinearLayout llOilEntry = new LinearLayout((this));
        llOilEntry.setLayoutParams(llParamsHorizontal);
        llOilEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(llOilEntry);

        TextView tvOilWeightLabel = new TextView(this);
        tvOilWeightLabel.setLayoutParams(tvParamsWeight0);
        tvOilWeightLabel.setText(R.string.label_oil_weight);
        llOilEntry.addView(tvOilWeightLabel);

        final EditText etOilWeight = new EditText(this);
        etOilWeight.setLayoutParams(tvParamsWeight1);
        etOilWeight.setText("0.0");
        etOilWeight.setKeyListener(DigitsKeyListener.getInstance("0123456789-."));
        etOilWeight.setSelectAllOnFocus(true);
        etOilWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                try {
                    Double oilWeight = Double.parseDouble(((EditText)view).getText().toString());
                    hmNameToWeight.put(res.getString(R.string.oil), oilWeight);
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etOilWeight.setText("0.0");
                }
            }
        });
        llOilEntry.addView(etOilWeight);
        namedViews.etOilWeight = etOilWeight;

        TextView tvOilWeightUnitLabel = new TextView(this);
        tvOilWeightUnitLabel.setLayoutParams(tvParamsWeight0);
        tvOilWeightUnitLabel.setText(namedViews.weightUnits);
        llOilEntry.addView(tvOilWeightUnitLabel);
        namedViews.weightUnitLabels.add(tvOilWeightUnitLabel);

        TextView tvOilArmLabel = new TextView(this);
        tvOilArmLabel.setLayoutParams(tvParamsWeight0);
        String oilArmLabel = " / " + res.getString(R.string.label_arm) + ": ";
        tvOilArmLabel.setText(oilArmLabel);
        llOilEntry.addView(tvOilArmLabel);

        final EditText etOilArm = new EditText(this);
        etOilArm.setLayoutParams(tvParamsWeight1);
        etOilArm.setText("0.0");
        etOilArm.setKeyListener(DigitsKeyListener.getInstance("0123456789-."));
        etOilArm.setSelectAllOnFocus(true);
        etOilArm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                try {
                    Double oilArm = Double.parseDouble(((EditText)view).getText().toString());
                    hmNameToArm.put(res.getString(R.string.oil), oilArm);
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etOilArm.setText("0.0");
                }
            }
        });
        llOilEntry.addView(etOilArm);
        namedViews.etOilArm = etOilArm;

        TextView tvOilArmUnitLabel = new TextView(this);
        tvOilArmUnitLabel.setLayoutParams(tvParamsWeight0);
        tvOilArmUnitLabel.setText(namedViews.armUnits);
        llOilEntry.addView(tvOilArmUnitLabel);
        namedViews.armUnitLabels.add(tvOilArmUnitLabel);

        LinearLayout llFuelOilDivider = new LinearLayout(this);
        llFuelOilDivider.setLayoutParams(fourP);
        llFuelOilDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llFuelOilDivider);

        //passenger row instructions
        TextView tvPaxInstruct = new TextView(this);
        tvPaxInstruct.setLayoutParams(tvParamsWeight1);
        tvPaxInstruct.setText(R.string.instructions_pax);
        llMain.addView(tvPaxInstruct);

        //passenger row entry
        final TableLayout tblPaxRows = new TableLayout(this);
        tblPaxRows.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        llMain.addView(tblPaxRows);

        final TableRow tblrPaxRowSummary = new TableRow(this);
        tblrPaxRowSummary.setLayoutParams(tableParamsWrapContent);
        tblrPaxRowSummary.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tblPaxRows.addView(tblrPaxRowSummary);

        final TextView tvPaxRowSummary = new TextView(this);
        tvPaxRowSummary.setLayoutParams(tableRowParamsWeight1);
        String paxRowSummary = String.valueOf(spaPaxRowIdToName.size()) + " " + res.getString(R.string.passenger_rows);
        tvPaxRowSummary.setText(paxRowSummary);
        tblrPaxRowSummary.addView(tvPaxRowSummary);

        TextView tvAddPaxRow = new TextView(this);
        tvAddPaxRow.setLayoutParams(tableRowParamsWeight0);
        tvAddPaxRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvAddPaxRow.setText("+");
        tvAddPaxRow.setTextColor(colorClickable);
        tvAddPaxRow.setClickable(true);
        tvAddPaxRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add passenger row.
                int currentRowId = View.generateViewId();

                final TableRow tblrPaxRowEntry = new TableRow(getBaseContext());
                tblrPaxRowEntry.setLayoutParams(tableParamsWrapContent);
                tblrPaxRowEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
                tblrPaxRowEntry.setId(currentRowId);
                tblPaxRows.addView(tblrPaxRowEntry);

                //name label
                TextView tvPaxRowNameLabel = new TextView(getBaseContext());
                tvPaxRowNameLabel.setLayoutParams(tableRowParamsWeight1);
                String strPaxRowNameLabel = res.getString(R.string.label_name) + ": ";
                tvPaxRowNameLabel.setText(strPaxRowNameLabel);
                tvPaxRowNameLabel.setTextColor(colorOriginal);
                tblrPaxRowEntry.addView(tvPaxRowNameLabel);

                //name entry
                EditText etPaxRowName = new EditText(getBaseContext());
                etPaxRowName.setLayoutParams(tableRowParamsWeight1);
                int rowNum = spaPaxRowIdToName.size();
                //TODO: implement checking to see if name exists.
                String paxRowName = res.getString(R.string.row) + String.valueOf(spaPaxRowIdToName.size());
                etPaxRowName.setHint(paxRowName);
                etPaxRowName.setSelectAllOnFocus(true);
                etPaxRowName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {

                    }
                });
                tblrPaxRowEntry.addView(etPaxRowName);

                //arm label

                //arm entry

                //num_seats label

                //num_seats entry

                //remove row button
                TextView tvRemovePaxRow = new TextView(getBaseContext());
                tvRemovePaxRow.setLayoutParams(tableRowParamsWeight0);
                tvRemovePaxRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
                tvRemovePaxRow.setText("-");
                tvRemovePaxRow.setTextColor(colorClickable);
                tvRemovePaxRow.setClickable(true);
                tvRemovePaxRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tblPaxRows.removeView(tblrPaxRowEntry);
                    }
                });
                tblrPaxRowEntry.addView(tvRemovePaxRow);

                tblPaxRows.removeView(tblrPaxRowSummary);
                String tmpPaxRowSummary = String.valueOf(spaPaxRowIdToName.size()) + " " + res.getString(R.string.passenger_rows);
                tvPaxRowSummary.setText(tmpPaxRowSummary);
                tblPaxRows.addView(tblrPaxRowSummary);
            }
        });
        tblrPaxRowSummary.addView(tvAddPaxRow);

        LinearLayout llPaxTblDivider = new LinearLayout(this);
        llPaxTblDivider.setLayoutParams(fourP);
        llPaxTblDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llPaxTblDivider);

        //baggage area instructions
        
        //baggage area entry

        //envelope data instructions

        //envelop data entry

        //submit

    }

    private void storeTmpData(AircraftClass a) {
        try {
            FileOutputStream fos = this.openFileOutput("data4_ga_wb_create", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(a);
            oos.close();
            fos.close();
        } catch (Exception e) {
            fatalAlert(getResources().getString(R.string.file_error_title), getResources().getString(R.string.error_creating_file) + ": " + e.toString());
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
    private void nonFatalAlert(String title, String msg) {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle(title);
        adBuilder.setMessage(msg);
        adBuilder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog ad = adBuilder.create();
        ad.show();
    }
}
