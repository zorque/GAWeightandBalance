package com.a3dnonsense.ga.gaweightandbalance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class CreateTemplateActivity extends AppCompatActivity {
    private class NamedViews {
        ArrayList<TextView> weightUnitLabels;
        ArrayList<TextView> armUnitLabels;
        EditText etEmptyWeight;
        EditText etEmptyArm;
        EditText etFuelWeight;
        EditText etFuelArm;
        EditText etOilWeight;
        EditText etOilArm;
        Boolean lastEtErrored = false;
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

        final LinearLayout llMain = (LinearLayout)findViewById(R.id.create_template_main_vertical_layout);

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

        //create sparse arrays to hold info for our dynamically-generated row info
        final SparseArray<String> spaPaxRowIdToName = new SparseArray<>();
        final SparseArray<Double> spaPaxRowIdToArm = new SparseArray<>();
        final SparseIntArray spaPaxRowIdToNumSeats = new SparseIntArray();

        final SparseArray<String> spaBagAreaIdToName = new SparseArray<>();
        final SparseArray<Double> spaBagAreaIdToArm = new SparseArray<>();

        final SparseArray<Double> spaDataIdToWeight = new SparseArray<>();
        final SparseArray<Double> spaDataIdToLowMoment = new SparseArray<>();
        final SparseArray<Double> spaDataIdToHighMoment = new SparseArray<>();

        //tail number / model instructions
        LinearLayout llNameInstruct = new LinearLayout(this);
        llNameInstruct.setLayoutParams(llParamsHorizontal);
        llMain.addView(llNameInstruct);

        TextView tvNameSectionLabel = new TextView(this);
        tvNameSectionLabel.setLayoutParams(tvParamsWeight1);
        tvNameSectionLabel.setText(R.string.create_title_name);
        llNameInstruct.addView(tvNameSectionLabel);

        final int colorOriginal = tvNameSectionLabel.getCurrentTextColor();

        TextView tvNameSectionHelp = new TextView(this);
        tvNameSectionHelp.setLayoutParams(tvParamsWeight0);
        tvNameSectionHelp.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvNameSectionHelp.setText(R.string.help_icon);
        tvNameSectionHelp.setTextColor(colorClickable);
        tvNameSectionHelp.setClickable(true);
        tvNameSectionHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nonFatalAlert(res.getString(R.string.create_title_name), res.getString(R.string.instructions_name));
            }
        });
        llNameInstruct.addView(tvNameSectionHelp);

        //tail number / model entry
        LinearLayout llNameEntry = new LinearLayout((this));
        llNameEntry.setLayoutParams(llParamsHorizontal);
        llNameEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(llNameEntry);

        TextView tvTailNumLabel = new TextView(this);
        tvTailNumLabel.setLayoutParams(tvParamsWeight1);
        tvTailNumLabel.setText(R.string.label_tail_num);
        llNameEntry.addView(tvTailNumLabel);

        final EditText etTailNum = new EditText(this);
        etTailNum.setLayoutParams(tvParamsWeight1);
        etTailNum.setHint("N12345");
        etTailNum.setSelectAllOnFocus(true);
        etTailNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etTailNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {return;}
                aircraft.tailNumber = ((EditText)view).getText().toString();
            }
        });
        llNameEntry.addView(etTailNum);
        aircraft.tailNumber = "";

        TextView tvModelLabel = new TextView(this);
        tvModelLabel.setLayoutParams(tvParamsWeight1);
        tvModelLabel.setText(R.string.label_model);
        llNameEntry.addView(tvModelLabel);

        EditText etModel = new EditText(this);
        etModel.setLayoutParams(tvParamsWeight1);
        etModel.setHint("c172");
        etModel.setSelectAllOnFocus(true);
        etModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etModel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {return;}
                aircraft.model = ((EditText)view).getText().toString();
            }
        });
        llNameEntry.addView(etModel);
        aircraft.model = "";

        LinearLayout llNameDivider = new LinearLayout(this);
        llNameDivider.setLayoutParams(fourP);
        llNameDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llNameDivider);

        //unit instructions
        LinearLayout llUnitInstruct = new LinearLayout(this);
        llUnitInstruct.setLayoutParams(llParamsHorizontal);
        llMain.addView(llUnitInstruct);

        TextView tvUnitSectionLabel = new TextView(this);
        tvUnitSectionLabel.setLayoutParams(tvParamsWeight1);
        tvUnitSectionLabel.setText(R.string.create_title_units);
        llUnitInstruct.addView(tvUnitSectionLabel);

        TextView tvUnitSectionHelp = new TextView(this);
        tvUnitSectionHelp.setLayoutParams(tvParamsWeight0);
        tvUnitSectionHelp.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvUnitSectionHelp.setText(R.string.help_icon);
        tvUnitSectionHelp.setTextColor(colorClickable);
        tvUnitSectionHelp.setClickable(true);
        tvUnitSectionHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nonFatalAlert(res.getString(R.string.create_title_units), res.getString(R.string.instructions_units));
            }
        });
        llUnitInstruct.addView(tvUnitSectionHelp);

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
        aircraft.weightUnits = spWeightUnits.getSelectedItem().toString();
        spWeightUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for (TextView tv : namedViews.weightUnitLabels) {
                    tv.setText(adapterView.getSelectedItem().toString());
                }
                aircraft.weightUnits = adapterView.getSelectedItem().toString();
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
        aircraft.armUnits = spArmUnits.getSelectedItem().toString();
        spArmUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for (TextView tv : namedViews.armUnitLabels) {
                    tv.setText(adapterView.getSelectedItem().toString());
                }
                aircraft.armUnits = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        LinearLayout llUnitDivider = new LinearLayout(this);
        llUnitDivider.setLayoutParams(fourP);
        llUnitDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llUnitDivider);

        //mechanical weight instructions
        LinearLayout llMechInstruct = new LinearLayout(this);
        llMechInstruct.setLayoutParams(llParamsHorizontal);
        llMain.addView(llMechInstruct);

        TextView tvMechSectionLabel = new TextView(this);
        tvMechSectionLabel.setLayoutParams(tvParamsWeight1);
        tvMechSectionLabel.setText(R.string.create_title_mechanical_weights);
        llMechInstruct.addView(tvMechSectionLabel);

        TextView tvMechSectionHelp = new TextView(this);
        tvMechSectionHelp.setLayoutParams(tvParamsWeight0);
        tvMechSectionHelp.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvMechSectionHelp.setText(R.string.help_icon);
        tvMechSectionHelp.setTextColor(colorClickable);
        tvMechSectionHelp.setClickable(true);
        tvMechSectionHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nonFatalAlert(res.getString(R.string.create_title_mechanical_weights), res.getString(R.string.instructions_nechanical_weights));
            }
        });
        llMechInstruct.addView(tvMechSectionHelp);

        //Max Gross entry
        LinearLayout llGrossEntry = new LinearLayout((this));
        llGrossEntry.setLayoutParams(llParamsHorizontal);
        llGrossEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(llGrossEntry);

        TextView tvGrossWeightLabel = new TextView(this);
        tvGrossWeightLabel.setLayoutParams(tvParamsWeight0);
        String grossWeightLabel = res.getString(R.string.label_gross) + " " + res.getString(R.string.label_weight) + " (" + res.getString(R.string.max) + "):";
        tvGrossWeightLabel.setText(grossWeightLabel);
        llGrossEntry.addView(tvGrossWeightLabel);

        final EditText etGrossWeight = new EditText(this);
        etGrossWeight.setLayoutParams(tvParamsWeight1);
        etGrossWeight.setText("0.0");
        etGrossWeight.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        etGrossWeight.setSelectAllOnFocus(true);
        etGrossWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etGrossWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {return;}
                try {
                    aircraft.maxGross = Double.parseDouble(((EditText)view).getText().toString());
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etGrossWeight.setText("0.0");
                    aircraft.maxGross = 0.0;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getCurrentFocus() != null) {if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}}
                            view.requestFocus();
                            namedViews.lastEtErrored = false;
                        }
                    }, 100);
                    namedViews.lastEtErrored = true;
                }
            }
        });
        llGrossEntry.addView(etGrossWeight);
        aircraft.maxGross = 0.0;

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
        etEmptyWeight.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        etEmptyWeight.setSelectAllOnFocus(true);
        etEmptyWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etEmptyWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {return;}
                try {
                    aircraft.emptyWeight = Double.parseDouble(((EditText)view).getText().toString());
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etEmptyWeight.setText("0.0");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                            view.requestFocus();
                            namedViews.lastEtErrored = false;
                        }
                    }, 100);
                    namedViews.lastEtErrored = true;
                }
            }
        });
        llEmptyEntry.addView(etEmptyWeight);
        namedViews.etEmptyWeight = etEmptyWeight;

        TextView tvEmptyWeightUnitLabel = new TextView(this);
        tvEmptyWeightUnitLabel.setLayoutParams(tvParamsWeight0);
        tvEmptyWeightUnitLabel.setText(aircraft.weightUnits);
        llEmptyEntry.addView(tvEmptyWeightUnitLabel);
        namedViews.weightUnitLabels.add(tvEmptyWeightUnitLabel);

        TextView tvEmptyArmLabel = new TextView(this);
        tvEmptyArmLabel.setLayoutParams(tvParamsWeight0);
        String emptyArmLabel = " / " + res.getString(R.string.label_arm) + ": ";
        tvEmptyArmLabel.setText(emptyArmLabel);
        llEmptyEntry.addView(tvEmptyArmLabel);

        final EditText etEmptyArm = new EditText(this);
        etEmptyArm.setLayoutParams(tvParamsWeight1);
        etEmptyArm.setText("0.0");
        etEmptyArm.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        etEmptyArm.setSelectAllOnFocus(true);
        etEmptyArm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etEmptyArm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {return;}
                try {
                    Double emptyArm = Double.parseDouble(((EditText)view).getText().toString());
                    etEmptyArm.setText(new DecimalFormat("#.##").format(emptyArm));
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etEmptyArm.setText("0.0");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                            view.requestFocus();
                            namedViews.lastEtErrored = false;
                        }
                    }, 100);
                    namedViews.lastEtErrored = true;
                }
            }
        });
        llEmptyEntry.addView(etEmptyArm);
        namedViews.etEmptyArm = etEmptyArm;

        TextView tvEmptyArmUnitLabel = new TextView(this);
        tvEmptyArmUnitLabel.setLayoutParams(tvParamsWeight0);
        tvEmptyArmUnitLabel.setText(aircraft.armUnits);
        llEmptyEntry.addView(tvEmptyArmUnitLabel);
        namedViews.armUnitLabels.add(tvEmptyArmUnitLabel);

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
        etFuelWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etFuelWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {return;}
                try {
                    Double fuelWeight = Double.parseDouble(((EditText)view).getText().toString());
                    etFuelWeight.setText(new DecimalFormat("#.##").format(fuelWeight));
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etFuelWeight.setText("0.0");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                            view.requestFocus();
                            namedViews.lastEtErrored = false;
                        }
                    }, 100);
                    namedViews.lastEtErrored = true;
                }
            }
        });
        llFuelEntry.addView(etFuelWeight);
        namedViews.etFuelWeight = etFuelWeight;

        TextView tvFuelWeightUnitLabel = new TextView(this);
        tvFuelWeightUnitLabel.setLayoutParams(tvParamsWeight0);
        tvFuelWeightUnitLabel.setText(aircraft.weightUnits);
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
        etFuelArm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etFuelArm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {return;}
                try {
                    Double fuelArm = Double.parseDouble(((EditText)view).getText().toString());
                    etFuelArm.setText(new DecimalFormat("#.##").format(fuelArm));
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etFuelArm.setText("0.0");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                            view.requestFocus();
                            namedViews.lastEtErrored = false;
                        }
                    }, 100);
                    namedViews.lastEtErrored = true;
                }
            }
        });
        llFuelEntry.addView(etFuelArm);
        namedViews.etFuelArm = etFuelArm;

        TextView tvFuelArmUnitLabel = new TextView(this);
        tvFuelArmUnitLabel.setLayoutParams(tvParamsWeight0);
        tvFuelArmUnitLabel.setText(aircraft.armUnits);
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
        etOilWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etOilWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {return;}
                try {
                    Double oilWeight = Double.parseDouble(((EditText)view).getText().toString());
                    etOilWeight.setText(new DecimalFormat("#.##").format(oilWeight));
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etOilWeight.setText("0.0");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                            view.requestFocus();
                            namedViews.lastEtErrored = false;
                        }
                    }, 100);
                    namedViews.lastEtErrored = true;
                }
            }
        });
        llOilEntry.addView(etOilWeight);
        namedViews.etOilWeight = etOilWeight;

        TextView tvOilWeightUnitLabel = new TextView(this);
        tvOilWeightUnitLabel.setLayoutParams(tvParamsWeight0);
        tvOilWeightUnitLabel.setText(aircraft.weightUnits);
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
        etOilArm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etOilArm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {return;}
                try {
                    Double oilArm = Double.parseDouble(((EditText)view).getText().toString());
                    etOilArm.setText(new DecimalFormat("#.##").format(oilArm));
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etOilArm.setText("0.0");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                            view.requestFocus();
                            namedViews.lastEtErrored = false;
                        }
                    }, 100);
                    namedViews.lastEtErrored = true;
                }
            }
        });
        llOilEntry.addView(etOilArm);
        namedViews.etOilArm = etOilArm;

        TextView tvOilArmUnitLabel = new TextView(this);
        tvOilArmUnitLabel.setLayoutParams(tvParamsWeight0);
        tvOilArmUnitLabel.setText(aircraft.armUnits);
        llOilEntry.addView(tvOilArmUnitLabel);
        namedViews.armUnitLabels.add(tvOilArmUnitLabel);

        LinearLayout llFuelOilDivider = new LinearLayout(this);
        llFuelOilDivider.setLayoutParams(fourP);
        llFuelOilDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llFuelOilDivider);

        //passenger row instructions
        LinearLayout llPaxInstruct = new LinearLayout(this);
        llPaxInstruct.setLayoutParams(llParamsHorizontal);
        llMain.addView(llPaxInstruct);

        TextView tvPaxSectionLabel = new TextView(this);
        tvPaxSectionLabel.setLayoutParams(tvParamsWeight0);
        tvPaxSectionLabel.setText(R.string.create_title_pax_rows);
        llPaxInstruct.addView(tvPaxSectionLabel);

        final TextView tvPaxSectionCount = new TextView(this);
        tvPaxSectionCount.setLayoutParams(tvParamsWeight1);
        String strPaxSectionCount = " (" + String.valueOf(spaPaxRowIdToName.size()) + ")";
        tvPaxSectionCount.setText(strPaxSectionCount);
        llPaxInstruct.addView(tvPaxSectionCount);

        TextView tvPaxSectionHelp = new TextView(this);
        tvPaxSectionHelp.setLayoutParams(tvParamsWeight0);
        tvPaxSectionHelp.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvPaxSectionHelp.setText(R.string.help_icon);
        tvPaxSectionHelp.setTextColor(colorClickable);
        tvPaxSectionHelp.setClickable(true);
        tvPaxSectionHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nonFatalAlert(res.getString(R.string.create_title_pax_rows), res.getString(R.string.instructions_pax));
            }
        });
        llPaxInstruct.addView(tvPaxSectionHelp);

        //passenger row entry
        final TableLayout tblPaxRows = new TableLayout(this);
        tblPaxRows.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        llMain.addView(tblPaxRows);

        TextView tvAddPaxRow = new TextView(this);
        tvAddPaxRow.setLayoutParams(tvParamsWeight0);
        tvAddPaxRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvAddPaxRow.setText("+");
        tvAddPaxRow.setTextColor(colorClickable);
        tvAddPaxRow.setClickable(true);
        tvAddPaxRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add passenger row.
                final int currentRowId = View.generateViewId();

                final TableRow tblrPaxRowEntry = new TableRow(getBaseContext());
                tblrPaxRowEntry.setLayoutParams(tableParamsWrapContent);
                tblrPaxRowEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
                tblrPaxRowEntry.setId(currentRowId);
                tblPaxRows.addView(tblrPaxRowEntry);

                //name label
                TextView tvPaxRowNameLabel = new TextView(getBaseContext());
                tvPaxRowNameLabel.setLayoutParams(tableRowParamsWeight0);
                String strPaxRowNameLabel = res.getString(R.string.label_name) + ": ";
                tvPaxRowNameLabel.setText(strPaxRowNameLabel);
                tvPaxRowNameLabel.setTextColor(colorOriginal);
                tblrPaxRowEntry.addView(tvPaxRowNameLabel);

                //name entry
                final EditText etPaxRowName = new EditText(getBaseContext());
                etPaxRowName.setLayoutParams(tableRowParamsWeight1);
                int rowNum = spaPaxRowIdToName.size() + 1;
                String paxRowName = res.getString(R.string.row) + String.valueOf(rowNum);
                while (true) {
                    Boolean nameExists = false;
                    for (int i=0; i < spaPaxRowIdToName.size(); i++) {
                        int k = spaPaxRowIdToName.keyAt(i);
                        if (spaPaxRowIdToName.get(k).equals(paxRowName) && !(currentRowId == k)) {
                            nameExists = true;
                        }
                    }
                    if (nameExists) {
                        rowNum += 1;
                        paxRowName = res.getString(R.string.row) + String.valueOf(rowNum);
                    } else {
                        break;
                    }
                }
                etPaxRowName.setText(paxRowName);
                etPaxRowName.setTextColor(colorOriginal);
                etPaxRowName.setSelectAllOnFocus(true);
                etPaxRowName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearFocus();
                        view.requestFocus();
                    }
                });
                etPaxRowName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View view, boolean b) {
                        if (b) {return;}
                        Boolean dupeName = false;
                        String enteredName = etPaxRowName.getText().toString();
                        for (int i=0; i < spaPaxRowIdToName.size(); i++) {
                            int k = spaPaxRowIdToName.keyAt(i);
                            if (spaPaxRowIdToName.get(k).equals(enteredName) && k != currentRowId) {
                                dupeName = true;
                            }
                        }
                        if (dupeName) {
                            nonFatalAlert(res.getString(R.string.duplicate_name_title), res.getString(R.string.duplicate_name_msg));
                            int newRowNum = spaPaxRowIdToName.size() + 1;
                            String newPaxRowName = res.getString(R.string.row) + String.valueOf(newRowNum);
                            while (true) {
                                Boolean nameExists = false;
                                for (int i=0; i < spaPaxRowIdToName.size(); i++) {
                                    int k = spaPaxRowIdToName.keyAt(i);
                                    if (spaPaxRowIdToName.get(k).equals(newPaxRowName) && k != currentRowId) {
                                        nameExists = true;
                                    }
                                }
                                if (nameExists) {
                                    newRowNum += 1;
                                    newPaxRowName = res.getString(R.string.row) + String.valueOf(newRowNum);
                                } else {
                                    break;
                                }
                            }
                            etPaxRowName.setText(newPaxRowName);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                        } else {
                            //make sure our sparse arrays are in sync
                            spaPaxRowIdToName.put(currentRowId, enteredName);
                        }

                    }
                });
                tblrPaxRowEntry.addView(etPaxRowName);

                //make sure our sparse arrays are in sync
                spaPaxRowIdToName.put(currentRowId, paxRowName);
                spaPaxRowIdToArm.put(currentRowId, 0.0);
                spaPaxRowIdToNumSeats.put(currentRowId,1);

                //arm label
                TextView tvPaxRowArmLabel = new TextView(getBaseContext());
                tvPaxRowArmLabel.setLayoutParams(tableRowParamsWeight0);
                String strPaxRowArmLabel = res.getString(R.string.label_arm) + ": ";
                tvPaxRowArmLabel.setText(strPaxRowArmLabel);
                tvPaxRowArmLabel.setTextColor(colorOriginal);
                tblrPaxRowEntry.addView(tvPaxRowArmLabel);

                //arm entry
                final EditText etPaxRowArm = new EditText(getBaseContext());
                etPaxRowArm.setLayoutParams(tableRowParamsWeight1);
                String strLongArm = "00.00";
                etPaxRowArm.setText(strLongArm);
                etPaxRowArm.setTextColor(colorOriginal);
                etPaxRowArm.setKeyListener(DigitsKeyListener.getInstance("0123456789-."));
                etPaxRowArm.setSelectAllOnFocus(true);
                etPaxRowArm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearFocus();
                        view.requestFocus();
                    }
                });
                etPaxRowArm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View view, boolean b) {
                        if (b) {return;}
                        try {
                            Double paxRowArm = Double.parseDouble(((EditText)view).getText().toString());
                            spaPaxRowIdToArm.put(currentRowId, paxRowArm);
                        } catch (Exception e) {
                            nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                            etPaxRowArm.setText("0.0");
                            spaPaxRowIdToArm.put(currentRowId, 0.0);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                        }
                    }
                });
                tblrPaxRowEntry.addView(etPaxRowArm);

                //num_seats label
                TextView tvPaxRowSeatsLabel = new TextView(getBaseContext());
                tvPaxRowSeatsLabel.setLayoutParams(tableRowParamsWeight0);
                String strPaxRowSeatsLabel = res.getString(R.string.label_seats) + ": ";
                tvPaxRowSeatsLabel.setText(strPaxRowSeatsLabel);
                tvPaxRowSeatsLabel.setTextColor(colorOriginal);
                tblrPaxRowEntry.addView(tvPaxRowSeatsLabel);

                //num_seats entry
                final EditText etPaxRowSeats = new EditText(getBaseContext());
                etPaxRowSeats.setLayoutParams(tableRowParamsWeight1);
                etPaxRowSeats.setText("1");
                etPaxRowSeats.setTextColor(colorOriginal);
                etPaxRowSeats.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                etPaxRowSeats.setSelectAllOnFocus(true);
                etPaxRowSeats.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearFocus();
                        view.requestFocus();
                    }
                });
                etPaxRowSeats.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View view, boolean b) {
                        if (b) {return;}
                        try {
                            int numSeats = Integer.parseInt(((EditText)view).getText().toString());
                            if (numSeats < 1) {
                                throw new Exception();
                            }
                            spaPaxRowIdToNumSeats.put(currentRowId, numSeats);
                        } catch (Exception e) {
                            nonFatalAlert(res.getString(R.string.invalid_entry_title), res.getString(R.string.invalid_integer));
                            etPaxRowSeats.setText("1");
                            spaPaxRowIdToNumSeats.put(currentRowId, 1);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                        }
                    }
                });
                tblrPaxRowEntry.addView(etPaxRowSeats);

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
                        spaPaxRowIdToName.remove(currentRowId);
                        spaPaxRowIdToArm.remove(currentRowId);
                        spaPaxRowIdToNumSeats.delete(currentRowId);
                        String tmpPaxSectionCount2 = " (" + String.valueOf(spaPaxRowIdToName.size()) + ")";
                        tvPaxSectionCount.setText(tmpPaxSectionCount2);
                    }
                });
                tblrPaxRowEntry.addView(tvRemovePaxRow);

                String tmpPaxSectionCount = " (" + String.valueOf(spaPaxRowIdToName.size()) + ")";
                tvPaxSectionCount.setText(tmpPaxSectionCount);
            }
        });
        llPaxInstruct.addView(tvAddPaxRow);

        LinearLayout llPaxTblDivider = new LinearLayout(this);
        llPaxTblDivider.setLayoutParams(fourP);
        llPaxTblDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llPaxTblDivider);

        //baggage area instructions
        LinearLayout llBagInstruct = new LinearLayout(this);
        llBagInstruct.setLayoutParams(llParamsHorizontal);
        llMain.addView(llBagInstruct);

        TextView tvBagSectionLabel = new TextView(this);
        tvBagSectionLabel.setLayoutParams(tvParamsWeight0);
        tvBagSectionLabel.setText(R.string.create_title_bag);
        llBagInstruct.addView(tvBagSectionLabel);

        final TextView tvBagAreaCount = new TextView(this);
        tvBagAreaCount.setLayoutParams(tvParamsWeight1);
        String strBagSectionCount = " (" + String.valueOf(spaBagAreaIdToName.size()) + ")";
        tvBagAreaCount.setText(strBagSectionCount);
        llBagInstruct.addView(tvBagAreaCount);

        TextView tvBagSectionHelp = new TextView(this);
        tvBagSectionHelp.setLayoutParams(tvParamsWeight0);
        tvBagSectionHelp.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvBagSectionHelp.setText(R.string.help_icon);
        tvBagSectionHelp.setTextColor(colorClickable);
        tvBagSectionHelp.setClickable(true);
        tvBagSectionHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nonFatalAlert(res.getString(R.string.create_title_bag), res.getString(R.string.instructions_bag));
            }
        });
        llBagInstruct.addView(tvBagSectionHelp);

        //baggage area entry
        final TableLayout tblBagAreas = new TableLayout(this);
        tblBagAreas.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        llMain.addView(tblBagAreas);

        TextView tvAddBagArea = new TextView(this);
        tvAddBagArea.setLayoutParams(tvParamsWeight0);
        tvAddBagArea.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvAddBagArea.setText("+");
        tvAddBagArea.setTextColor(colorClickable);
        tvAddBagArea.setClickable(true);
        tvAddBagArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int currentRowId = View.generateViewId();

                final TableRow tblrBagAreaEntry = new TableRow(getBaseContext());
                tblrBagAreaEntry.setLayoutParams(tableParamsWrapContent);
                tblrBagAreaEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
                tblrBagAreaEntry.setId(currentRowId);
                tblBagAreas.addView(tblrBagAreaEntry);

                //name label
                TextView tvBagAreaNameLabel = new TextView(getBaseContext());
                tvBagAreaNameLabel.setLayoutParams(tableRowParamsWeight0);
                String strBagAreaNameLabel = res.getString(R.string.label_name) + ": ";
                tvBagAreaNameLabel.setText(strBagAreaNameLabel);
                tvBagAreaNameLabel.setTextColor(colorOriginal);
                tblrBagAreaEntry.addView(tvBagAreaNameLabel);

                //name entry
                final EditText etBagAreaName = new EditText(getBaseContext());
                etBagAreaName.setLayoutParams(tableRowParamsWeight1);
                int rowNum = spaBagAreaIdToName.size() + 1;
                String BagAreaName = res.getString(R.string.baggage) + String.valueOf(rowNum);
                while (true) {
                    Boolean nameExists = false;
                    for (int i=0; i < spaBagAreaIdToName.size(); i++) {
                        int k = spaBagAreaIdToName.keyAt(i);
                        if (spaBagAreaIdToName.get(k).equals(BagAreaName) && !(currentRowId == k)) {
                            nameExists = true;
                        }
                    }
                    if (nameExists) {
                        rowNum += 1;
                        BagAreaName = res.getString(R.string.baggage) + String.valueOf(rowNum);
                    } else {
                        break;
                    }
                }
                etBagAreaName.setText(BagAreaName);
                etBagAreaName.setTextColor(colorOriginal);
                etBagAreaName.setSelectAllOnFocus(true);
                etBagAreaName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearFocus();
                        view.requestFocus();
                    }
                });
                etBagAreaName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View view, boolean b) {
                        if (b) {return;}
                        Boolean dupeName = false;
                        String enteredName = etBagAreaName.getText().toString();
                        for (int i=0; i < spaBagAreaIdToName.size(); i++) {
                            int k = spaBagAreaIdToName.keyAt(i);
                            if (spaBagAreaIdToName.get(k).equals(enteredName) && k != currentRowId) {
                                dupeName = true;
                            }
                        }
                        if (dupeName) {
                            nonFatalAlert(res.getString(R.string.duplicate_name_title), res.getString(R.string.duplicate_name_msg));
                            int newRowNum = spaBagAreaIdToName.size() + 1;
                            String newBagAreaName = res.getString(R.string.baggage) + String.valueOf(newRowNum);
                            while (true) {
                                Boolean nameExists = false;
                                for (int i=0; i < spaBagAreaIdToName.size(); i++) {
                                    int k = spaBagAreaIdToName.keyAt(i);
                                    if (spaBagAreaIdToName.get(k).equals(newBagAreaName) && k != currentRowId) {
                                        nameExists = true;
                                    }
                                }
                                if (nameExists) {
                                    newRowNum += 1;
                                    newBagAreaName = res.getString(R.string.baggage) + String.valueOf(newRowNum);
                                } else {
                                    break;
                                }
                            }
                            etBagAreaName.setText(newBagAreaName);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                        } else {
                            //make sure our sparse arrays are in sync
                            spaBagAreaIdToName.put(currentRowId, enteredName);
                        }

                    }
                });
                tblrBagAreaEntry.addView(etBagAreaName);

                //make sure our sparse arrays are in sync
                spaBagAreaIdToName.put(currentRowId, BagAreaName);
                spaBagAreaIdToArm.put(currentRowId, 0.0);

                //arm label
                TextView tvBagAreaArmLabel = new TextView(getBaseContext());
                tvBagAreaArmLabel.setLayoutParams(tableRowParamsWeight0);
                String strBagAreaArmLabel = res.getString(R.string.label_arm) + ": ";
                tvBagAreaArmLabel.setText(strBagAreaArmLabel);
                tvBagAreaArmLabel.setTextColor(colorOriginal);
                tblrBagAreaEntry.addView(tvBagAreaArmLabel);

                //arm entry
                final EditText etBagAreaArm = new EditText(getBaseContext());
                etBagAreaArm.setLayoutParams(tableRowParamsWeight1);
                etBagAreaArm.setText("0.0");
                etBagAreaArm.setTextColor(colorOriginal);
                etBagAreaArm.setKeyListener(DigitsKeyListener.getInstance("0123456789-."));
                etBagAreaArm.setSelectAllOnFocus(true);
                etBagAreaArm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearFocus();
                        view.requestFocus();
                    }
                });
                etBagAreaArm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View view, boolean b) {
                        if (b) {return;}
                        try {
                            Double BagAreaArm = Double.parseDouble(((EditText)view).getText().toString());
                            spaBagAreaIdToArm.put(currentRowId, BagAreaArm);
                        } catch (Exception e) {
                            nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                            etBagAreaArm.setText("0.0");
                            spaBagAreaIdToArm.put(currentRowId, 0.0);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                        }
                    }
                });
                tblrBagAreaEntry.addView(etBagAreaArm);

                //remove row button
                TextView tvRemoveBagArea = new TextView(getBaseContext());
                tvRemoveBagArea.setLayoutParams(tableRowParamsWeight0);
                tvRemoveBagArea.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
                tvRemoveBagArea.setText("-");
                tvRemoveBagArea.setTextColor(colorClickable);
                tvRemoveBagArea.setClickable(true);
                tvRemoveBagArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tblBagAreas.removeView(tblrBagAreaEntry);
                        spaBagAreaIdToName.remove(currentRowId);
                        spaBagAreaIdToArm.remove(currentRowId);
                        String tmpBagSectionCount2 = " (" + String.valueOf(spaBagAreaIdToName.size()) + ")";
                        tvBagAreaCount.setText(tmpBagSectionCount2);
                    }
                });
                tblrBagAreaEntry.addView(tvRemoveBagArea);

                String tmpBagSectionCount = " (" + String.valueOf(spaBagAreaIdToName.size()) + ")";
                tvBagAreaCount.setText(tmpBagSectionCount);
            }
        });
        llBagInstruct.addView(tvAddBagArea);

        LinearLayout llBagTblDivider = new LinearLayout(this);
        llBagTblDivider.setLayoutParams(fourP);
        llBagTblDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llBagTblDivider);

        //envelope data instructions
        LinearLayout llDataInstruct = new LinearLayout(this);
        llDataInstruct.setLayoutParams(llParamsHorizontal);
        llMain.addView(llDataInstruct);

        TextView tvDataSectionLabel = new TextView(this);
        tvDataSectionLabel.setLayoutParams(tvParamsWeight0);
        tvDataSectionLabel.setText(R.string.create_title_data);
        llDataInstruct.addView(tvDataSectionLabel);

        final TextView tvDataSectionCount = new TextView(this);
        tvDataSectionCount.setLayoutParams(tvParamsWeight1);
        String strDataSectionCount = " (" + String.valueOf(spaDataIdToWeight.size()) + ")";
        tvDataSectionCount.setText(strDataSectionCount);
        llDataInstruct.addView(tvDataSectionCount);

        TextView tvDataSectionHelp = new TextView(this);
        tvDataSectionHelp.setLayoutParams(tvParamsWeight0);
        tvDataSectionHelp.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvDataSectionHelp.setText(R.string.help_icon);
        tvDataSectionHelp.setTextColor(colorClickable);
        tvDataSectionHelp.setClickable(true);
        tvDataSectionHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nonFatalAlert(res.getString(R.string.create_title_data), res.getString(R.string.instructions_data));
            }
        });
        llDataInstruct.addView(tvDataSectionHelp);

        //moment divide entry
        LinearLayout llMomentDivideEntry = new LinearLayout(this);
        llMomentDivideEntry.setLayoutParams(llParamsHorizontal);
        llMomentDivideEntry.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(llMomentDivideEntry);

        TextView tvMomentDivideLabel = new TextView(this);
        tvMomentDivideLabel.setLayoutParams(tvParamsWeight0);
        tvMomentDivideLabel.setText(R.string.moment_divide);
        llMomentDivideEntry.addView(tvMomentDivideLabel);

        final EditText etMomentDivide = new EditText(this);
        etMomentDivide.setLayoutParams(tvParamsWeight1);
        etMomentDivide.setText("1.0");
        etMomentDivide.setKeyListener(DigitsKeyListener.getInstance("0123456789-."));
        etMomentDivide.setSelectAllOnFocus(true);
        etMomentDivide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                view.requestFocus();
            }
        });
        etMomentDivide.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean b) {
                if (b) {return;}
                try {
                    aircraft.momentDivide = Double.parseDouble(((EditText)view).getText().toString());
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                    etMomentDivide.setText("1.0");
                    aircraft.momentDivide = 1.0;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                            view.requestFocus();
                            namedViews.lastEtErrored = false;
                        }
                    }, 100);
                    namedViews.lastEtErrored = true;
                }
            }
        });
        llMomentDivideEntry.addView(etMomentDivide);
        aircraft.momentDivide = 1.0;

        TextView tvMomentDivideHelp = new TextView(this);
        tvMomentDivideHelp.setLayoutParams(tvParamsWeight0);
        tvMomentDivideHelp.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvMomentDivideHelp.setText(R.string.help_icon);
        tvMomentDivideHelp.setTextColor(colorClickable);
        tvMomentDivideHelp.setClickable(true);
        tvMomentDivideHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nonFatalAlert(res.getString(R.string.create_title_data), res.getString(R.string.instructions_moment_divide));
            }
        });
        llMomentDivideEntry.addView(tvMomentDivideHelp);

        //envelop data entry
        final TableLayout tblDataPoints = new TableLayout(this);
        tblDataPoints.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        tblDataPoints.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        llMain.addView(tblDataPoints);

        TableRow tblrLegend = new TableRow(this);
        tblrLegend.setLayoutParams(tableParamsWrapContent);
        tblDataPoints.addView(tblrLegend);

        TextView tvLegendWeight = new TextView(this);
        tvLegendWeight.setLayoutParams(tableRowParamsWeight1);
        tvLegendWeight.setLines(2);
        String strLegendWeight = res.getString(R.string.label_weight) + "\n" + res.getString(R.string.zero_removes);
        tvLegendWeight.setText(strLegendWeight);
        tvLegendWeight.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        tvLegendWeight.setTextColor(colorOriginal);
        tvLegendWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tblrLegend.addView(tvLegendWeight);

        TextView tvLegendLow = new TextView(this);
        tvLegendLow.setLayoutParams(tableRowParamsWeight1);
        tvLegendLow.setLines(2);
        String strLegendLow = res.getString(R.string.label_moment) + "\n" + res.getString(R.string.low);
        tvLegendLow.setText(strLegendLow);
        tvLegendLow.setGravity(Gravity.CENTER_HORIZONTAL);
        tvLegendLow.setTextColor(colorOriginal);
        tvLegendLow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tblrLegend.addView(tvLegendLow);

        TextView tvLegendHigh = new TextView(this);
        tvLegendHigh.setLayoutParams(tableRowParamsWeight1);
        tvLegendHigh.setLines(2);
        String strLegendHigh = res.getString(R.string.label_moment) + "\n" + res.getString(R.string.high);
        tvLegendHigh.setText(strLegendHigh);
        tvLegendHigh.setGravity(Gravity.CENTER_HORIZONTAL);
        tvLegendHigh.setTextColor(colorOriginal);
        tvLegendHigh.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
        tblrLegend.addView(tvLegendHigh);

        TextView tvAddDataPoint = new TextView(this);
        tvAddDataPoint.setLayoutParams(tableRowParamsWeight0);
        tvAddDataPoint.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvAddDataPoint.setText("+");
        tvAddDataPoint.setTextColor(colorClickable);
        tvAddDataPoint.setClickable(true);
        tvAddDataPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add data point
                final int currentRowId = View.generateViewId();

                final TableRow tblrDataPointEntry = new TableRow(getBaseContext());
                tblrDataPointEntry.setLayoutParams(tableParamsWrapContent);
                tblrDataPointEntry.setId(currentRowId);
                tblDataPoints.addView(tblrDataPointEntry);

                //weight entry
                final EditText etDataWeight = new EditText(getBaseContext());
                etDataWeight.setLayoutParams(tableRowParamsWeight1);
                etDataWeight.setText("0.0");
                etDataWeight.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                etDataWeight.setTextColor(colorOriginal);
                etDataWeight.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
                etDataWeight.setGravity(Gravity.END);
                etDataWeight.setSelectAllOnFocus(true);
                etDataWeight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearFocus();
                        view.requestFocus();
                    }
                });
                etDataWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View view, boolean b) {
                        if (b) {return;}
                        Double weight;
                        try {
                            weight = Double.parseDouble(((EditText)view).getText().toString());
                            spaDataIdToWeight.put(currentRowId, weight);
                        } catch (Exception e) {
                            nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                            etDataWeight.setText("0.0");
                            spaDataIdToWeight.put(currentRowId, 0.0);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                            return;
                        }
                        //check for dupes
                        Boolean dupeWeight = false;
                        for (int i=0; i < spaDataIdToWeight.size(); i++) {
                            int k = spaDataIdToWeight.keyAt(i);
                            if (!(weight.equals(0.0)) && spaDataIdToWeight.get(k).equals(weight) && k != currentRowId) {
                                dupeWeight = true;
                            }
                        }
                        if (dupeWeight) {
                            nonFatalAlert(res.getString(R.string.duplicate_weight_title), res.getString(R.string.duplicate_weight_msg));
                            etDataWeight.setText("0.0");
                            spaDataIdToWeight.put(currentRowId, 0.0);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                        }
                    }


                });
                tblrDataPointEntry.addView(etDataWeight);

                //make sure our sparse arrays are in sync
                spaDataIdToWeight.put(currentRowId, 0.0);
                spaDataIdToLowMoment.put(currentRowId, 0.0);
                spaDataIdToHighMoment.put(currentRowId, 0.0);

                //low moment entry
                final EditText etDataLowMoment = new EditText(getBaseContext());
                etDataLowMoment.setLayoutParams(tableRowParamsWeight1);
                etDataLowMoment.setText("0.0");
                etDataLowMoment.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                etDataLowMoment.setTextColor(colorOriginal);
                etDataLowMoment.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
                etDataLowMoment.setGravity(Gravity.END);
                etDataLowMoment.setSelectAllOnFocus(true);
                etDataLowMoment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearFocus();
                        view.requestFocus();
                    }
                });
                etDataLowMoment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View view, boolean b) {
                        if (b) {return;}
                        Double lowMoment;
                        try {
                            lowMoment = Double.parseDouble(((EditText)view).getText().toString());
                            spaDataIdToLowMoment.put(currentRowId, lowMoment);
                        } catch (Exception e) {
                            nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                            etDataLowMoment.setText("0.0");
                            spaDataIdToLowMoment.put(currentRowId, 0.0);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                            return;
                        }
                        Boolean dupeLowMoment = false;
                        for (int i=0; i < spaDataIdToLowMoment.size(); i++) {
                            int k = spaDataIdToLowMoment.keyAt(i);
                            if (!(lowMoment.equals(0.0)) && spaDataIdToLowMoment.get(k).equals(lowMoment) && k != currentRowId) {
                                dupeLowMoment = true;
                            }
                        }
                        if (dupeLowMoment) {
                            nonFatalAlert(res.getString(R.string.duplicate_moment_title), res.getString(R.string.duplicate_moment_msg));
                            etDataLowMoment.setText("0.0");
                            spaDataIdToLowMoment.put(currentRowId, 0.0);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                        }
                    }
                });
                tblrDataPointEntry.addView(etDataLowMoment);

                //high moment entry
                final EditText etDataHighMoment = new EditText(getBaseContext());
                etDataHighMoment.setLayoutParams(tableRowParamsWeight1);
                etDataHighMoment.setText("0.0");
                etDataHighMoment.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                etDataHighMoment.setTextColor(colorOriginal);
                etDataHighMoment.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));
                etDataHighMoment.setGravity(Gravity.END);
                etDataHighMoment.setSelectAllOnFocus(true);
                etDataHighMoment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearFocus();
                        view.requestFocus();
                    }
                });
                etDataHighMoment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View view, boolean b) {
                        if (b) {return;}
                        Double highMoment;
                        try {
                            highMoment = Double.parseDouble(((EditText)view).getText().toString());
                            spaDataIdToHighMoment.put(currentRowId, highMoment);
                        } catch (Exception e) {
                            nonFatalAlert(res.getString(R.string.invalid_double), res.getString(R.string.invalid_entry_msg));
                            etDataHighMoment.setText("0.0");
                            spaDataIdToHighMoment.put(currentRowId, 0.0);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                            return;
                        }
                        Boolean dupeHighMoment = false;
                        for (int i=0; i < spaDataIdToHighMoment.size(); i++) {
                            int k = spaDataIdToHighMoment.keyAt(i);
                            if (!(highMoment.equals(0.0)) && spaDataIdToHighMoment.get(k).equals(highMoment) && k != currentRowId) {
                                dupeHighMoment = true;
                            }
                        }
                        if (dupeHighMoment) {
                            nonFatalAlert(res.getString(R.string.duplicate_moment_title), res.getString(R.string.duplicate_moment_msg));
                            etDataHighMoment.setText("0.0");
                            spaDataIdToHighMoment.put(currentRowId, 0.0);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();}
                                    view.requestFocus();
                                    namedViews.lastEtErrored = false;
                                }
                            }, 100);
                            namedViews.lastEtErrored = true;
                        }
                    }
                });
                tblrDataPointEntry.addView(etDataHighMoment);

                //remove row button
                TextView tvRemoveMomentRow = new TextView(getBaseContext());
                tvRemoveMomentRow.setLayoutParams(tableRowParamsWeight0);
                tvRemoveMomentRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
                tvRemoveMomentRow.setText("-");
                tvRemoveMomentRow.setTextColor(colorClickable);
                tvRemoveMomentRow.setClickable(true);
                tvRemoveMomentRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tblDataPoints.removeView(tblrDataPointEntry);
                        spaDataIdToWeight.remove(currentRowId);
                        spaDataIdToLowMoment.remove(currentRowId);
                        spaDataIdToHighMoment.remove(currentRowId);
                        String tmpDataSectionCount2 = " (" + String.valueOf(spaDataIdToWeight.size()) + ")";
                        tvDataSectionCount.setText(tmpDataSectionCount2);
                    }
                });
                tblrDataPointEntry.addView(tvRemoveMomentRow);

                String tmpDataSectionCount = " (" + String.valueOf(spaDataIdToWeight.size()) + ")";
                tvDataSectionCount.setText(tmpDataSectionCount);
            }
        });
        tblrLegend.addView(tvAddDataPoint);

        LinearLayout llEnvelopeDivider = new LinearLayout(this);
        llEnvelopeDivider.setLayoutParams(fourP);
        llEnvelopeDivider.setBackgroundColor(Color.BLACK);
        llMain.addView(llEnvelopeDivider);

        //submit / verify
        LinearLayout llSubmit = new LinearLayout(this);
        llSubmit.setLayoutParams(llParamsHorizontal);
        llSubmit.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        llMain.addView(llSubmit);

        TextView tvSubmit = new TextView(this);
        tvSubmit.setLayoutParams(tvParamsWeight0);
        tvSubmit.setGravity(Gravity.CENTER_HORIZONTAL);
        tvSubmit.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborderentry, null));
        tvSubmit.setText(R.string.submit);
        tvSubmit.setTextColor(colorClickable);
        tvSubmit.setClickable(true);
        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentFocus() != null) {getCurrentFocus().clearFocus();} //force our last entered EditText to update desired value.

                if (namedViews.lastEtErrored) {return;}

                AircraftClass a = new AircraftClass(); //create blank aircraft each time to avoid duplicate list entries

                //verify tail number and model filled in and not a dupe
                if (aircraft.tailNumber.equals("") || aircraft.model.equals("")) {
                    nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_tail_or_model_blank));
                    return;
                }
                a.tailNumber = aircraft.tailNumber;
                a.model = aircraft.model;

                //check for existing template
                File[] internalFileList = getFilesDir().listFiles();
                ArrayList<String> templates = new ArrayList<>();
                for (File f : internalFileList) {
                    if (f.getName().startsWith(res.getString(R.string.template_file_prefix))) {
                        templates.add(convertFileNameToTemplate(f.getName()));
                    }
                }
                for (String t : templates) {
                    if (t.equals(a.getTemplateName())) {
                        nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_duplicate_template));
                        return;
                    }
                }

                //verify weight and arm units not ""
                if (aircraft.weightUnits.equals("") || aircraft.armUnits.equals("")) {
                    nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_weight_or_arm_units_blank));
                    return;
                }
                a.weightUnits = aircraft.weightUnits;
                a.armUnits = aircraft.armUnits;

                //verify empty > 0
                if (!(aircraft.emptyWeight > 0)) {
                    nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_empty_weight));
                    return;
                }
                a.emptyWeight = aircraft.emptyWeight;

                //verify maxGross numeric and > empty
                if (!(aircraft.maxGross > aircraft.emptyWeight)) {
                    nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_gross_weight));
                    return;
                }
                a.maxGross = aircraft.maxGross;

                //verify momentDivide > 0
                if (!(aircraft.momentDivide > 0)) {
                    nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_moment_divide));
                    return;
                }
                a.momentDivide = aircraft.momentDivide;

                //verify empty, fuel, and oil arms and weights are numbers, and input to aircraft class.
                try {
                    AircraftClass.mechanicalWeight empty = new AircraftClass().new mechanicalWeight();
                    AircraftClass.mechanicalWeight fuel = new AircraftClass().new mechanicalWeight();
                    AircraftClass.mechanicalWeight oil = new AircraftClass().new mechanicalWeight();

                    empty.name = res.getString(R.string.empty);
                    fuel.name = res.getString(R.string.fuel);
                    oil.name = res.getString(R.string.oil);

                    empty.weight = Double.parseDouble(namedViews.etEmptyWeight.getText().toString());
                    fuel.weight = Double.parseDouble(namedViews.etFuelWeight.getText().toString());
                    oil.weight = Double.parseDouble(namedViews.etOilWeight.getText().toString());

                    empty.arm = Double.parseDouble(namedViews.etEmptyArm.getText().toString());
                    fuel.arm = Double.parseDouble(namedViews.etFuelArm.getText().toString());
                    oil.arm = Double.parseDouble(namedViews.etOilArm.getText().toString());

                    a.mechanicalWeights.add(empty);
                    a.mechanicalWeights.add(fuel);
                    a.mechanicalWeights.add(oil);
                } catch (Exception e) {
                    nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_mechanical_weights));
                    return;
                }

                //verify passenger rows exist
                if (spaPaxRowIdToName.size() < 1) {
                    nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_no_pax_rows));
                    return;
                }

                //verify passenger row names are unique, seats are integers > 0, and input to aircraft class.
                ArrayList<String> existingPaxNames = new ArrayList<>();
                for (int i=0; i < spaPaxRowIdToName.size(); i++) {
                    int k = spaPaxRowIdToName.keyAt(i);

                    if (existingPaxNames.contains(spaPaxRowIdToName.get(k))) {
                        nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_pax_row_unique_names));
                        return;
                    }
                    existingPaxNames.add(spaPaxRowIdToName.get(k));

                    if (!(spaPaxRowIdToNumSeats.get(k) > 0)) {
                        nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_pax_row_num_seats));
                        return;
                    }

                    AircraftClass.passengerRow paxRow = new AircraftClass().new passengerRow();
                    paxRow.name = spaPaxRowIdToName.get(k);
                    paxRow.arm = spaPaxRowIdToArm.get(k);
                    paxRow.numseats = spaPaxRowIdToNumSeats.get(k);
                    a.passengerRows.add(paxRow);
                }

                //verify baggage areas have unique names, and input to aircraft class.
                ArrayList<String> existingBagNames = new ArrayList<>();
                for (int i=0; i < spaBagAreaIdToName.size(); i++) {
                    int k = spaBagAreaIdToName.keyAt(i);

                    if (existingBagNames.contains(spaBagAreaIdToName.get(k))) {
                        nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_bag_area_unique_names));
                        return;
                    }
                    existingBagNames.add(spaBagAreaIdToName.get(k));

                    AircraftClass.baggageArea bagArea = new AircraftClass().new baggageArea();
                    bagArea.name = spaBagAreaIdToName.get(k);
                    bagArea.arm = spaBagAreaIdToArm.get(k);
                    a.baggageAreas.add(bagArea);
                }

                //verify envelope data numeric, not duplicated, and non-zero, and input to aircraft class.
                if (spaDataIdToWeight.size() < 2) {
                    nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_need_two_envelope_weights));
                    return;
                }

                ArrayList<Double> existingDataWeights = new ArrayList<>();
                ArrayList<Double> existingDataLowMoments = new ArrayList<>();
                ArrayList<Double> existingDataHighMoments = new ArrayList<>();
                for (int i=0; i < spaDataIdToWeight.size(); i++) {
                    int k = spaDataIdToWeight.keyAt(i);
                    Double weight = spaDataIdToWeight.get(k);
                    Double lowMoment = spaDataIdToLowMoment.get(k);
                    Double highMoment = spaDataIdToHighMoment.get(k);

                    if (existingDataWeights.contains(weight)) {
                        nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_data_weight_duplicate));
                        return;
                    }
                    existingDataWeights.add(weight);

                    if (existingDataLowMoments.contains(lowMoment) || existingDataHighMoments.contains(highMoment)) {
                        nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.duplicate_moment_msg));
                        return;
                    }
                    existingDataLowMoments.add(lowMoment);
                    existingDataHighMoments.add(highMoment);

                    if (lowMoment.equals(highMoment)) {
                        nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_low_high_moment_same));
                        return;
                    }

                    if (lowMoment > highMoment) {
                        nonFatalAlert(res.getString(R.string.verification_failure), res.getString(R.string.verification_high_moment_below_low_moment) + " ("
                                + res.getString(R.string.label_weight) + ":" + weight.toString() + ", "
                                + res.getString(R.string.low) + ":" + lowMoment.toString() + ", "
                                + res.getString(R.string.high) + ":" + highMoment.toString()
                                + ")");
                        return;
                    }

                    AircraftClass.envelopeData dataPoint = new AircraftClass().new envelopeData();
                    dataPoint.weight = weight;
                    dataPoint.lowMoment = lowMoment;
                    dataPoint.highMoment = highMoment;
                    a.envelopeDataSet.add(dataPoint);
                }

                //write out file
                writeAircraftToFile(a);
                Intent mainIntent = new Intent(view.getContext(), MainActivity.class);
                finish();
                startActivity(mainIntent);
            }
        });
        llSubmit.addView(tvSubmit);
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(mainIntent);
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

    private void writeAircraftToFile(AircraftClass a) {
        try {
            FileOutputStream fos = this.openFileOutput(getResources().getString(R.string.template_file_prefix) + a.getTemplateName(), Context.MODE_PRIVATE);
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

    private String convertFileNameToTemplate(String s) {
        return s.replace(getResources().getString(R.string.template_file_prefix), "");
    }
}
