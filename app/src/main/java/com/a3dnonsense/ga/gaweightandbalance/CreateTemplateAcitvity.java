package com.a3dnonsense.ga.gaweightandbalance;

import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;

public class CreateTemplateAcitvity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources res = getResources();

        final HashMap<String, Integer> nameToId = new HashMap<String, Integer>();
        int currentId = 1000;

        LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);

        ScrollView sv = new ScrollView(this);
        LinearLayout llMain = new LinearLayout(this);

        LinearLayout llTemplateNameRow = new LinearLayout(this);
        llTemplateNameRow.setOrientation(LinearLayout.HORIZONTAL);
        llTemplateNameRow.setBackground(ResourcesCompat.getDrawable(res, R.drawable.customborder, null));

        TextView tvTailNum = new TextView(this);
        tvTailNum.setLayoutParams(rowLayoutParams);
        tvTailNum.setText(R.string.tail_number_label);
        llTemplateNameRow.addView(tvTailNum);

        EditText etTailNumber = new EditText(this);
        etTailNumber.setLayoutParams(rowLayoutParams);
        etTailNumber.setHint("N12345");
        llTemplateNameRow.addView(etTailNumber);

        llMain.addView(llTemplateNameRow);

        sv.addView(llMain);
        setContentView(sv);
    }
}
