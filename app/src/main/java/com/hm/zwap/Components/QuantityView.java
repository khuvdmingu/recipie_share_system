package com.hm.zwap.Components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hm.zwap.R;

import java.util.Locale;

public class QuantityView extends LinearLayout {

    private TextView tv_qty;
    private FloatingActionButton fb_add;
    private FloatingActionButton fb_sub;

    public QuantityView(Context context) {
        super(context);

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.component_quantity, this, true);
        tv_qty = (TextView) findViewById(R.id.tv_qty);
        fb_sub = (FloatingActionButton) findViewById(R.id.fab_qty_sub);
        fb_add = (FloatingActionButton) findViewById(R.id.fab_qty_add);

        fb_sub.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(tv_qty.getText().toString());
                if (qty > 0) {
                    qty--;
                    tv_qty.setText(qty + "");
                }
            }
        });

        fb_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(tv_qty.getText().toString());
                if (qty < 10) {
                    qty++;
                    tv_qty.setText(qty + "");
                }
            }
        });

    }

}