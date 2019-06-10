package com.rafagire.orderapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyOrder extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private DBAccess dbAccess;
    private Map<Integer, Integer> orderedProducts;
    private Map<Integer, Integer> orderProducts;
    private List<Product> productsOrderedList;
    private List<Product> productsOrderList;
    protected ItemAdapter itemAdapterOrdered;
    protected ItemAdapter itemAdapterOrder;
    protected ListView listViewOrdered;
    protected ListView listViewOrder;
    private TextView tView_totalCountNumber;
    private Button button_order;
    private Button button_remove;

    private float totalCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView in onResume()
        //setContentView(R.layout.activity_my_order);

        dbAccess = new DBLocal(this, "DB_OrderApp", null, 1);
    }

    protected void onResume(){
        setContentView(R.layout.activity_my_order);
        orderProducts = null;
        orderedProducts = null;
        productsOrderList = null;
        productsOrderedList = null;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(pref.contains(getString(R.string.order_products_key)) || pref.contains(getString(R.string.ordered_products_key))){
            totalCount = 0;

            if(pref.contains(getString(R.string.ordered_products_key))) {
                Set<String> setOrdered = pref.getStringSet(getString(R.string.ordered_products_key), new HashSet<String>());
                if (!setOrdered.isEmpty()) {
                    orderedProducts = new HashMap<Integer, Integer>();
                    productsOrderedList = new ArrayList<Product>();
                    for (String st : setOrdered) {
                        int id = Integer.parseInt(st);
                        int number = pref.getInt((getString(R.string.general_number_product_key) + "_already_ordered" + st), 0);

                        orderedProducts.put(id, number);
                        productsOrderedList.add(dbAccess.findById(id));

                        totalCount += (dbAccess.findById(id).price * number);
                    }
                    listViewOrdered = (ListView) findViewById(R.id.listView_ordered);
                    itemAdapterOrdered = new ItemAdapter(this, productsOrderedList, true);
                    listViewOrdered.setAdapter(itemAdapterOrdered);

                    listViewOrdered.setOnItemClickListener(this);
                }
            }

            if (pref.contains(getString(R.string.order_products_key))) {
                Set<String> setCurrentOrder = pref.getStringSet(getString(R.string.order_products_key), new HashSet<String>());
                if (!setCurrentOrder.isEmpty()) {
                    orderProducts = new HashMap<Integer, Integer>();
                    productsOrderList = new ArrayList<Product>();
                    for (String st : setCurrentOrder) {
                        int id = Integer.parseInt(st);
                        int number = pref.getInt((getString(R.string.general_number_product_key) + st), 0);

                        orderProducts.put(id, number);
                        productsOrderList.add(dbAccess.findById(id));

                        totalCount += (dbAccess.findById(id).price * number);
                    }
                    listViewOrder = (ListView) findViewById(R.id.listView_order);
                    itemAdapterOrder = new ItemAdapter(this, productsOrderList, false);
                    listViewOrder.setAdapter(itemAdapterOrder);

                    listViewOrder.setOnItemClickListener(this);
                }
            }

            tView_totalCountNumber = (TextView) findViewById(R.id.tView_totalCountNumber);
            tView_totalCountNumber.setText(String.format("%.2f", totalCount) + getString(R.string.PriceUnit));

            button_order = (Button) findViewById(R.id.button_order);
            button_order.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    executeOrder();
                }
            });

            button_remove = (Button) findViewById(R.id.button_remove);
            button_remove.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    removeWholeOrder();
                }
            });

        }
        else{
            LinearLayout lLayout = (LinearLayout) findViewById(R.id.lLayout_orded);

            lLayout.removeView(((ScrollView) findViewById(R.id.scrollView)));
            lLayout.removeView((View) findViewById(R.id.divider));
            lLayout.removeView(((LinearLayout) findViewById(R.id.lLayout_totalPrice)));
            lLayout.removeView(((LinearLayout) findViewById(R.id.lLayout_buttons)));

            final TextView tView_noProducts = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            tView_noProducts.setLayoutParams(lp);
            tView_noProducts.setText(getString(R.string.MyOrder_tView_noProductsFound));
            tView_noProducts.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_SearchResult_Subtitle);
            tView_noProducts.setGravity(Gravity.CENTER);

            lLayout.addView(tView_noProducts);
        }

        super.onResume();
    }

    public void executeOrder(){
        if((productsOrderList == null) || productsOrderList.isEmpty()){
            Toast.makeText(this, getString(R.string.MyOrder_toast_orderNoNewProducts), Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            String productStringList = "";
            for(Product p : productsOrderList){
                productStringList = productStringList + "\n" + p.name + " x" + String.valueOf(orderProducts.get(p.id));
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getString(R.string.MyOrder_buttonOrder));
            alertDialog.setMessage(getString(R.string.MyOrder_dialogOrder) + "\n" + productStringList);

            alertDialog.setPositiveButton(getString(R.string.MyOrder_dialogOrder_positiveButton),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = pref.edit();

                            for (Product p : productsOrderList) {
                                if ((orderedProducts == null) || !orderedProducts.containsKey(p.id)) {
                                    Set<String> set = pref.getStringSet(getString(R.string.ordered_products_key), new HashSet<String>());
                                    set.add(Integer.toString(p.id));
                                    editor.putStringSet(getString(R.string.ordered_products_key), set);
                                }
                                int number = pref.getInt((getString(R.string.general_number_product_key) + "_already_ordered" + Integer.toString(p.id)), 0);
                                number += pref.getInt((getString(R.string.general_number_product_key) + Integer.toString(p.id)), 0);

                                editor.putInt((getString(R.string.general_number_product_key) + "_already_ordered" + Integer.toString(p.id)), number);

                                Set<String> set = pref.getStringSet(getString(R.string.order_products_key), new HashSet<String>());
                                set.remove(Integer.toString(p.id));
                                editor.putStringSet(getString(R.string.order_products_key), set);
                                editor.remove((getString(R.string.general_number_product_key) + Integer.toString(p.id)));

                                editor.commit();
                            }
                            onResume();
                        }
                    });

            alertDialog.setNegativeButton(getString(R.string.MyOrder_dialogOrder_negativeButton),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();
        }
    }

    public void removeWholeOrder(){
        if((productsOrderList == null) || productsOrderList.isEmpty()){
            Toast.makeText(this, getString(R.string.MyOrder_toast_deleteNoNewProducts), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.MyOrder_buttonRemove));
        alertDialog.setMessage(getString(R.string.MyOrder_dialogRemove_text));

        alertDialog.setPositiveButton(getString(R.string.MyOrder_dialogRemove_positiveButton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = pref.edit();

                        //for(Product p : productsOrderedList){
                            //editor.remove((getString(R.string.general_number_product_key) + "_already_ordered" + Integer.toString(p.id)));
                        //}
                        //editor.remove(getString(R.string.ordered_products_key));
                        for(Product p : productsOrderList){
                            editor.remove((getString(R.string.general_number_product_key) + Integer.toString(p.id)));
                        }
                        editor.remove(getString(R.string.order_products_key));
                        editor.commit();

                        Toast.makeText(MyOrder.this, getString(R.string.MyOrder_toast_orderRemoved), Toast.LENGTH_SHORT).show();

                        if(productsOrderedList != null)
                            recreate();
                        else
                            finish();

                    }
                });
        alertDialog.setNegativeButton(getString(R.string.MyOrder_dialogRemove_negativeButton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int pos = position;

        switch(parent.getId()) {
            case (R.id.listView_ordered):
                Toast.makeText(this, getString(R.string.MyOrder_toast_productOrderedSelected), Toast.LENGTH_SHORT).show();
                break;

            case (R.id.listView_order):
                Product product = (Product) itemAdapterOrder.getItem(position);

                Intent intent = new Intent(this, MenuProduct.class);

                Bundle bundle = new Bundle();
                bundle.putInt("product_id", product.id);
                intent.putExtras(bundle);

                startActivity(intent);
                recreate();
                break;
        }
    }

    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class ItemAdapter extends BaseAdapter {

        protected Activity activity;
        protected List<Product> items;
        protected boolean alreadyOrdered;


        public ItemAdapter (Activity activity, List<Product> items, boolean alreadyOrdered) {
            this.activity = activity;
            this.items = items;
            this.alreadyOrdered = alreadyOrdered;
        }

        public int getCount() {
            return items.size();
        }

        public void addAll(List<Product> products) {
            for (int i = 0; i < products.size(); i++) {
                items.add(products.get(i));
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
                v = inf.inflate(R.layout.item_category_orded, null);
            }

            Product product = items.get(position);

            TextView tView_name = (TextView) v.findViewById(R.id.tView_name);
            TextView tView_price = (TextView) v.findViewById(R.id.tView_price);
            TextView tView_number = (TextView) v.findViewById(R.id.tView_number);

            tView_name.setText(product.name);
            tView_price.setText((String.format ("%.2f", product.price) + getString(R.string.PriceUnit)));
            if(alreadyOrdered)
                tView_number.setText("x" + String.valueOf(orderedProducts.get(product.id)));
            else
                tView_number.setText("x" + String.valueOf(orderProducts.get(product.id)));

            if(alreadyOrdered){
                tView_name.setTextColor(getResources().getColor(R.color.colorAlreadyOrdered));
                tView_price.setTextColor(getResources().getColor(R.color.colorAlreadyOrdered));
                tView_number.setTextColor(getResources().getColor(R.color.colorAlreadyOrdered));
            }

            return v;
        }
    }
}
