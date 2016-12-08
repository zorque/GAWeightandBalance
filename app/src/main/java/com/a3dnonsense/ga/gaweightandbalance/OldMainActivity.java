package com.a3dnonsense.ga.gaweightandbalance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class OldMainActivity extends AppCompatActivity {
    // This activity will present a list of aircraft templates to choose from,
    // as well as options to add or remove templates.
    public final static String EXTRA_FILENAME = "com.a3dnonsense.ga.gaweightandbalance.FILENAME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getResources();

        ScrollView sv = new ScrollView(this);
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);

        File[] internalFileList = getFilesDir().listFiles();
        ArrayList<String> templateNames = new ArrayList<String>();

        // Create buttons at top to create new templates.
        LinearLayout llCreateButtons = new LinearLayout(this);
        llCreateButtons.setOrientation(LinearLayout.HORIZONTAL);
        final Button bCreateTemplate = new Button(this);
        bCreateTemplate.setText(R.string.create_template);
        bCreateTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bCreateTemplate.setEnabled(false);
                // restart the activity to refresh the file list.
                Intent intent = new Intent(view.getContext(), CreateTemplateAcitvity.class);
                startActivity(intent);
            }
        });
        llCreateButtons.addView(bCreateTemplate);
        Boolean sampleExists = false;
        final Button bMakeSample = new Button(this);
        bMakeSample.setText(R.string.make_sample);
        bMakeSample.setEnabled(false);
        bMakeSample.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //create sample template if it doesn't exist.
                bMakeSample.setEnabled(false);
                FileOutputStream outputStream;
                //create file / stream
                try {
                    String fileName = res.getString(R.string.template_file_prefix) + res.getString(R.string.sample_template_name) + ".csv";
                    outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                    //write out data
                    String[] sampleLines = res.getStringArray(R.array.sample_file_content);
                    for (int i = 0; i < sampleLines.length; i++) {
                        outputStream.write(sampleLines[i].toString().getBytes());
                        outputStream.write("\n".getBytes());
                    }
                    //close file
                    outputStream.close();
                    Toast.makeText(view.getContext(), "Sample Created.", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("File Error", "Error creating sample file", e);
                    Toast.makeText(view.getContext(), "Error Creating sample file.", Toast.LENGTH_LONG).show();
                    Toast.makeText(view.getContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
                // restart the activity to refresh the file list.
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        llCreateButtons.addView(bMakeSample);
        llMain.addView(llCreateButtons);

        // List our exiting template files, if any
        LinearLayout llTemplates = new LinearLayout(this);
        llTemplates.setOrientation(LinearLayout.VERTICAL);
        if (internalFileList.length < 1) {
            TextView tvNoFiles = new TextView(this);
            tvNoFiles.setText(R.string.no_files);
            llTemplates.addView(tvNoFiles);
        } else {
            boolean foundTemplate = false;
            for (int i = 0; i < internalFileList.length; i++) {
                //check each file against our template format.
                if (internalFileList[i].getName().startsWith(res.getString(R.string.template_file_prefix))) {
                    foundTemplate = true;
                    String bName = internalFileList[i].getName().replace(res.getString(R.string.template_file_prefix), "");
                    bName = bName.replace(".csv", "");
                    templateNames.add(bName);
                }
            }
            if (!foundTemplate) {
                TextView tvNoTemplates = new TextView(this);
                tvNoTemplates.setText(R.string.no_templates);
                llTemplates.addView(tvNoTemplates);
            } else {
                // List template names as buttons that will open that template.
                for (int i = 0; i < templateNames.size(); i++) {
                    final String template = templateNames.get(i);
                    final String fileName = res.getString(R.string.template_file_prefix) + template + ".csv";
                    LinearLayout llTemplateRow = new LinearLayout(this);
                    llTemplateRow.setOrientation(LinearLayout.HORIZONTAL);
                    Button bLoadTemplate = new Button(this);
                    bLoadTemplate.setText(template);
                    bLoadTemplate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(), UseTemplateActivity.class);
                            intent.putExtra(EXTRA_FILENAME, fileName);
                            startActivity(intent);
                        }
                    });
                    llTemplateRow.addView(bLoadTemplate);
                    final Button bDeleteTemplate = new Button(this);
                    bDeleteTemplate.setText(R.string.delete_template);
                    bDeleteTemplate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            view.setEnabled(false);
                            //create alert dialogue to verify delete.
                            AlertDialog.Builder adBuilder1 = new AlertDialog.Builder(view.getContext());
                            adBuilder1.setTitle(res.getString(R.string.delete_alert_dialogue_title));
                            adBuilder1.setMessage(String.format(res.getString(R.string.delete_alert_dialogue_msg), template));
                            adBuilder1.setPositiveButton(
                                    res.getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //delete the file
                                            try {
                                                File dir = getFilesDir();
                                                File file = new File(dir, fileName);
                                                if (!file.exists()) {
                                                    Exception e = new Exception("File doesn't exist.");
                                                    throw e;
                                                }
                                                Boolean deleted = file.delete();
                                                if (!deleted) {
                                                    Exception e = new Exception("Deleted returned false.");
                                                    throw e;
                                                }
                                            } catch (Exception e) {
                                                Log.e("File Error", "Error deleting file '" + fileName + "'", e);
                                                Toast.makeText(view.getContext(), "Error deleting file '" + fileName + "'", Toast.LENGTH_LONG).show();
                                                Toast.makeText(view.getContext(), e.toString(), Toast.LENGTH_LONG).show();
                                            }
                                            dialogInterface.cancel();
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                        }
                                    }
                            );
                            adBuilder1.setNegativeButton(
                                    res.getString(R.string.no),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            view.setEnabled(true);
                                            dialogInterface.cancel();
                                        }
                                    }
                            );
                            AlertDialog ad1 = adBuilder1.create();
                            ad1.show();
                            // restart the activity to refresh the file list.
                        }
                    });
                    llTemplateRow.addView(bDeleteTemplate);
                    llTemplates.addView(llTemplateRow);
                    if (template.equals(res.getString(R.string.sample_template_name))) {
                        //set "Make Sample" button to disabled if we already generated it.
                        sampleExists = true;
                    }
                }
            }
        }
        if (!sampleExists) {
            bMakeSample.setEnabled(true);
        }
        llMain.addView(llTemplates);
        sv.addView(llMain);
        setContentView(sv);
    }
}
