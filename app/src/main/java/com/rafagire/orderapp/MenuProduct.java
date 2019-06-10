package com.rafagire.orderapp;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MenuProduct extends AppCompatActivity implements View.OnClickListener {

    private DBAccess dbAccess;

    //private String label;
    private Product product;
    private int number;
    private TextView tView_name;
    private TextView tView_price;
    private TextView tView_number;
    private Button button_plus;
    private Button button_minus;
    private Button button_add;
    private Button button_cancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_product);

        Bundle bundle = this.getIntent().getExtras();
        int id = bundle.getInt("product_id");

        dbAccess = new DBLocal(this, "DB_OrderApp", null, 1);
        product = dbAccess.findById(id);
        setTitle(product.name);

        number = 0;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(pref.contains(getString(R.string.general_number_product_key) + Integer.toString(product.id))){
            number = pref.getInt((getString(R.string.general_number_product_key) + Integer.toString(product.id)), 0);
        }

        tView_name = (TextView) findViewById(R.id.tView_name);
        tView_price = (TextView) findViewById(R.id.tView_price);
        tView_number = (TextView) findViewById(R.id.tView_number);
        button_plus = (Button) findViewById(R.id.button_plus);
        button_minus = (Button) findViewById(R.id.button_minus);
        button_add = (Button) findViewById(R.id.button_add);
        button_cancel = (Button) findViewById(R.id.button_cancel);

        tView_name.setText(product.name);
        tView_price.setText((String.format ("%.2f", product.price) + getString(R.string.PriceUnit)));
        tView_number.setText(Integer.toString(number));
        if(number == 0)
            button_add.setText(getString(R.string.MenuProduct_buttonAdd));
        else
            button_add.setText(getString(R.string.MenuProduct_buttonModify));

        button_plus.setOnClickListener(this);
        button_minus.setOnClickListener(this);
        button_add.setOnClickListener(this);
        button_cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_plus:
                if(number < 99){
                    number++;
                    tView_number.setText(Integer.toString(number));
                }
                else{
                    Toast.makeText(this, getString(R.string.MenuProduct_toast_noMoreProducts), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_minus:
                if(number > 0){
                    number--;
                    tView_number.setText(Integer.toString(number));
                }
                break;
            case R.id.button_cancel:
                finish();
                break;
            case R.id.button_add:
                if(number > 0) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = pref.edit();

                    String toastMessage;

                    //Check the products currently ordering
                    Set<String> set;
                    if(pref.contains(getString(R.string.order_products_key))){
                        set = pref.getStringSet(getString(R.string.order_products_key), new HashSet<String>());
                        if(set.contains(Integer.toString(product.id))) {
                            toastMessage = getString(R.string.MenuProduct_toast_productsModified);
                        }
                        else{
                            set.add(Integer.toString(product.id));
                            toastMessage = getString(R.string.MenuProduct_toast_productsAdded);
                        }
                    }
                    else{
                        set = new HashSet<String>();
                        set.add(Integer.toString(product.id));
                        toastMessage = getString(R.string.MenuProduct_toast_productsAdded);
                    }

                    editor.putStringSet(getString(R.string.order_products_key), set);
                    editor.putInt((getString(R.string.general_number_product_key) + Integer.toString(product.id)), number);
                    editor.commit();

                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

                    finish();
                }
                else{
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = pref.edit();

                    //Check if there was a previous order of the current product, and in this case, remove it
                    Set<String> set;
                    if(pref.contains(getString(R.string.order_products_key))){
                        set = pref.getStringSet(getString(R.string.order_products_key), new HashSet<String>());
                        if(set.contains(Integer.toString(product.id))){
                            set.remove(Integer.toString(product.id));
                            if(set.isEmpty()){
                                editor.remove(getString(R.string.order_products_key));
                            }
                            else{
                                editor.putStringSet(getString(R.string.order_products_key), set);
                            }
                            editor.remove((getString(R.string.general_number_product_key) + Integer.toString(product.id)));

                            editor.commit();

                            Toast.makeText(this, getString(R.string.MenuProduct_toast_removeProductOrded), Toast.LENGTH_SHORT).show();

                            finish();
                        }
                        else{
                            Toast.makeText(this, getString(R.string.MenuProduct_toast_add0Products), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(this, getString(R.string.MenuProduct_toast_add0Products), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
