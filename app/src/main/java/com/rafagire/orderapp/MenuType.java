package com.rafagire.orderapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MenuType extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private DBAccess dbAccess;
    protected ItemAdapter itemAdapter;
    protected ListView listView;
    private Button button_seeOrder;

    private String type;
    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_type);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null) {
            type = bundle.getString("type");
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.current_type_key), type);
            editor.commit();
        }
        else{
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            type = pref.getString(getString(R.string.current_type_key), null);
        }

        setTitle(type);

        dbAccess = new DBLocal(this, "DB_OrderApp", null, 1);
        products = dbAccess.findByType(type);

        listView = (ListView) findViewById(R.id.listView_type);
        itemAdapter = new ItemAdapter(this, products);
        listView.setAdapter(itemAdapter);

        listView.setOnItemClickListener(this);

        button_seeOrder = (Button) findViewById(R.id.button_seeOrder);
        button_seeOrder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MenuType.this, MyOrder.class);
                startActivity(intent);
            }
        });
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int pos = position;
        Product product = (Product)itemAdapter.getItem(position);

        Intent intent = new Intent(this, MenuProduct.class);

        Bundle bundle = new Bundle();
        bundle.putInt("product_id", product.id);
        //bundle.putString("product_name", product.name);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class ItemAdapter extends BaseAdapter {

        protected Activity activity;
        protected List<Product> items;


        public ItemAdapter (Activity activity, List<Product> items) {
            this.activity = activity;
            this.items = items;
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
                v = inf.inflate(R.layout.item_category_product, null);
            }

            Product product = items.get(position);

            TextView tView_name = (TextView) v.findViewById(R.id.tView_productName);
            tView_name.setText(product.name);
            TextView tView_price = (TextView) v.findViewById(R.id.tView_productPrice);
            tView_price.setText((String.format ("%.2f", product.price) + getString(R.string.PriceUnit)));

            return v;
        }
    }
}
