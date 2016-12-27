package com.a3dnonsense.ga.gaweightandbalance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_FILENAME = "com.a3dnonsense.ga.gaweightandbalance.FILENAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Resources res = getResources();

        //list our files, if any
        File[] internalFileList = getFilesDir().listFiles();
        ArrayList<String> templates = new ArrayList<>();
        for (File f : internalFileList) {
            if (f.getName().startsWith(res.getString(R.string.template_file_prefix))) {
                templates.add(convertFileNameToTemplate(f.getName()));
            }
        }
        if (templates.size()< 1) {
            //if nothing is in our file list, we'll create a sample template to play with
            createSampleFile();
        }
        for (final String t : templates) {  //make open and delete buttons for each template
            if (t.startsWith(res.getString(R.string.sample_template_prefix))) {
                //if our sample is present we can disable the "create sample" button
                Button bCreateSample = (Button) findViewById(R.id.bCreateSample);
                bCreateSample.setEnabled(false);
            }
            final String fileName = convertTemplateToFileName(t);
            //layout row
            LinearLayout llTemplateButtonRow = new LinearLayout(this);
            llTemplateButtonRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout llTemplateButtonRowParent = (LinearLayout) findViewById(R.id.fileList);
            llTemplateButtonRowParent.addView(llTemplateButtonRow);

            Button bOpenTemplate = new Button(this);
            bOpenTemplate.setText(t);
            bOpenTemplate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setEnabled(false);
                    Intent templateIntent = new Intent(view.getContext(), OpenTemplateActivity.class);
                    templateIntent.putExtra(EXTRA_FILENAME, fileName);
                    startActivity(templateIntent);
                    view.setEnabled(true);
                }
            });
            llTemplateButtonRow.addView(bOpenTemplate);

            Button bDeleteTemplate = new Button(this);
            bDeleteTemplate.setText(R.string.delete_template);
            bDeleteTemplate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    view.setEnabled(false);
                    //create alert dialogue to verify delete
                    AlertDialog.Builder adDeleteConfirm = new AlertDialog.Builder(view.getContext());
                    adDeleteConfirm.setTitle(res.getString(R.string.delete_alert_dialogue_title));
                    adDeleteConfirm.setMessage(String.format(res.getString(R.string.delete_alert_dialogue_msg), t));
                    adDeleteConfirm.setPositiveButton(
                            res.getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //delete the file
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
                                    } catch (Exception e) {
                                        fatalAlert(res.getString(R.string.file_error_title), res.getString(R.string.error_deleting_file) + " '" + fileName + "' : " + e.toString());
                                    }
                                    dialogInterface.cancel();
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                }
                            }
                    );
                    adDeleteConfirm.setNegativeButton(
                            res.getString(R.string.no),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                    view.setEnabled(true);
                                }
                            }
                    );
                    AlertDialog ad1 = adDeleteConfirm.create();
                    ad1.show();
                }
            });
            llTemplateButtonRow.addView(bDeleteTemplate);
        }
    }

    public void createSampleFile() {
        AircraftClass a = new AircraftClass().makeSample(getResources().getString(R.string.sample_template_prefix), getResources());
        try {
            FileOutputStream fos = this.openFileOutput(convertTemplateToFileName(a.getTemplateName()), Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(a);
            oos.close();
            fos.close();
        } catch (Exception e) {
            fatalAlert(getResources().getString(R.string.file_error_title), getResources().getString(R.string.error_creating_file) + ": " + e.toString());
        }
        restartAlert(getResources().getText(R.string.sample_created_alert_title).toString(), getResources().getText(R.string.sample_created_alert_message).toString());
    }

    public void createSampleXml(View view) {
        createSampleFile();
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

    private void restartAlert(String title, String msg) {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle(title);
        adBuilder.setMessage(msg);
        adBuilder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        AlertDialog ad = adBuilder.create();
        ad.show();
    }

    private String convertTemplateToFileName(String s) {
        return getResources().getString(R.string.template_file_prefix) + s;
    }

    private String convertFileNameToTemplate(String s) {
        return s.replace(getResources().getString(R.string.template_file_prefix), "");
    }

    public void openCreateTemplateActivity(View view) {
        Intent templateIntent = new Intent(view.getContext(), CreateTemplateActivity.class);
        finish();
        startActivity(templateIntent);
    }
}
