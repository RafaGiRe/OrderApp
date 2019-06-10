package com.rafagire.orderapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MenuGeneral extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private DBAccess dbAccess;
    protected ItemAdapter itemAdapter;
    protected ListView listView;
    private Button button_seeOrder;

    private String code;
    private boolean correctCode = false;
    //private String label;
    private List<String> types;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_general);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null) {
            code = bundle.getString("code");
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.current_code_key), code);
            editor.commit();
        }
        else{
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            code = pref.getString(getString(R.string.current_code_key), null);
        }

        interpretCode(code);

        if(correctCode) {
            dbAccess = new DBLocal(this, "DB_OrderApp", null, 1);
            List<Product> all = dbAccess.findAll();
            if (all != null) {
                types = new ArrayList<String>();
                for (Product p : all) {
                    if (!types.contains(p.type))
                        types.add(p.type);
                }

            }

            if (types != null) {
                listView = (ListView) findViewById(R.id.listView_general);
                itemAdapter = new ItemAdapter(this, types);
                listView.setAdapter(itemAdapter);
                listView.setOnItemClickListener(this);

                button_seeOrder = (Button) findViewById(R.id.button_seeOrder);
                button_seeOrder.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(MenuGeneral.this, MyOrder.class);
                        startActivity(intent);
                    }
                });
            } else {
                LinearLayout lLayout = (LinearLayout) findViewById(R.id.lLayout_general);

                lLayout.removeView(findViewById(R.id.lLayout_listView));
                lLayout.removeView(findViewById(R.id.button_seeOrder));

                final TextView tView_noTypes = new TextView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                tView_noTypes.setLayoutParams(lp);
                tView_noTypes.setText(getString(R.string.MenuGeneral_noTypesFound));
                tView_noTypes.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
                tView_noTypes.setGravity(Gravity.CENTER);

                lLayout.addView(tView_noTypes);
            }
        }
        else{
            removeClientData();

            LinearLayout lLayout = (LinearLayout) findViewById(R.id.lLayout_general);

            lLayout.removeView(findViewById(R.id.lLayout_listView));
            lLayout.removeView(findViewById(R.id.button_seeOrder));

            final TextView tView_noTypes = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            tView_noTypes.setLayoutParams(lp);
            tView_noTypes.setText(getString(R.string.MenuGeneral_codeNotRecognised));
            tView_noTypes.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_SearchResult_Subtitle);
            tView_noTypes.setGravity(Gravity.CENTER);

            lLayout.addView(tView_noTypes);
        }


    }


    private void interpretCode(String code){
        //Set the table number to the label
        //setTitle(getString(R.string.MenuGeneral_label));
        String[] words = code.split("/");
        if(words.length == 2){
            setTitle(words[0] + " (" + getString(R.string.MenuGeneral_label_table) + " " + words[1] + ")") ;
            correctCode = true;
        }
        else
            correctCode = false;
    }

    private void removeClientData(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();

        if(pref.contains(getString(R.string.order_products_key))){
            Set<String> set = pref.getStringSet(getString(R.string.order_products_key), new HashSet<String>());
            if(!set.isEmpty()) {
                for (String st : set) {
                    editor.remove((getString(R.string.general_number_product_key) + st));
                }
            }
            editor.remove(getString(R.string.order_products_key));
        }
        if(pref.contains(getString(R.string.ordered_products_key))){
            Set<String> set = pref.getStringSet(getString(R.string.ordered_products_key), new HashSet<String>());
            if(!set.isEmpty()) {
                for (String st : set) {
                    editor.remove((getString(R.string.general_number_product_key) + "_already_ordered" + st));
                }
            }
            editor.remove(getString(R.string.ordered_products_key));
        }
        editor.remove(getString(R.string.current_code_key));

        editor.commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed(){
        if(correctCode) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getString(R.string.MenuGeneral_dialog_title));
            alertDialog.setMessage(getString(R.string.MenuGeneral_dialog_description));

            alertDialog.setPositiveButton(getString(R.string.MenuGeneral_dialog_positiveButton),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            removeClientData();
                            MenuGeneral.super.onBackPressed();
                        }
                    });

            alertDialog.setNegativeButton(getString(R.string.MenuGeneral_dialog_negativeButton),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();
        }
        else{
            removeClientData();
            MenuGeneral.super.onBackPressed();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int pos = position;
        String type = (String)itemAdapter.getItem(position);

        Intent intent = new Intent(this, MenuType.class);

        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class ItemAdapter extends BaseAdapter {

        protected Activity activity;
        protected List<String> items;


        public ItemAdapter (Activity activity, List<String> items) {
            this.activity = activity;
            this.items = items;
        }

        public int getCount() {
            return items.size();
        }

        public void addAll(List<String> sites) {
            for (int i = 0; i < sites.size(); i++) {
                items.add(sites.get(i));
            }
        }

        public Object getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (convertView == null) {
                LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inf.inflate(R.layout.item_category_type, null);
            }

            String type = items.get(position);
            TextView tView_name = (TextView) v.findViewById(R.id.tView_typeName);
            tView_name.setText(type);

            return v;
        }
    }
}
