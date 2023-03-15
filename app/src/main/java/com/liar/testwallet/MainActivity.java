package com.liar.testwallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void jumpEthWallet(View view) {
        //跳转以太坊
        Intent intent = new Intent(this, EthWalletActivity.class);
        startActivity(intent);
    }
}