package com.liar.testwallet;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * 以太坊钱包主页
 */
public class EthWalletActivity extends AppCompatActivity {

    private static final String TAG = "EthWalletActivity";
    //以太坊钱包文件
    private WalletFile mWalletFile;
    //当前网络名称
    private TextView mNetworkTitleText;
    //当前钱包地址
    private EditText mWalletAddressText;
    //当前钱包余额
    private TextView mWalletBalanceText;
    //目标钱包地址
    private EditText mToAddressEdit;
    //目标钱包余额
    private TextView mToAddressBalanceText;
    //发送数额
    private EditText mAmountEdit;
    //钱包信息
    private EditText mKeyStoreEdit;
    //钱包私钥
    private EditText mPrivateKeyEdit;

    //创建Web3j实例
    private Web3j mWeb3j = Web3j.build(new HttpService(Constants.ETHEREUM_GOERLI_URL));
    //当前钱包地址
    private String mAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ethereum_wallet);
        initUi();
        initEthWalletData();
    }

    /**
     * 加载UI控件
     */
    private void initUi(){
        mWalletAddressText = findViewById(R.id.address);
        mWalletBalanceText = findViewById(R.id.balance);
        mToAddressEdit = findViewById(R.id.to_address);
        mAmountEdit = findViewById(R.id.amount);
        mKeyStoreEdit = findViewById(R.id.key_store);
        mPrivateKeyEdit = findViewById(R.id.private_key);
        mNetworkTitleText = findViewById(R.id.network_title);
        mToAddressBalanceText = findViewById(R.id.to_address_balance);
    }

    /**
     * 加载钱包数据
     */
    private void initEthWalletData(){
        EthWalletController.getInstance().loadWallet(this, new EthWalletController.OnWalletLoadedListener() {
            @Override
            public void onWalletLoaded(WalletFile w) {
                mWalletFile = w;
                Log.d(TAG, "onWalletLoaded::::: " + mWalletFile.getAddress().length());
                mAddress = Constants.HEX_PREFIX + mWalletFile.getAddress();
                Log.d(TAG, "mAddress当前钱包地址 ::::: " +mAddress);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mWalletAddressText.setText(mAddress);
                        updateBalance(mAddress, mWalletBalanceText);

                        mToAddressEdit.setText(Constants.LIA_ADDRESS);
                        updateBalance(mToAddressEdit.getText().toString(),mToAddressBalanceText);
                    }
                });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //切换网络
        getMenuInflater().inflate(R.menu.eth_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_goerli:
                //切换Goerli测试网络
                refurbishUI("Goerli测试网络",Constants.ETHEREUM_GOERLI_URL);
                break;
            case R.id.switch_mainnet:
                //切换Mainnet测试网络
                refurbishUI("Mainnet测试网络",Constants.ETHEREUM_MAINNET_URL);
                break;
            case R.id.switch_sepolia:
                //切换sepolia测试网络
                refurbishUI("Sepolia测试网络",Constants.ETHEREUM_SEPOLIA_URL);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 刷新UI
     *  @param title 标题
     *  @param ethUrl 切换的网络链接
     */
    private void refurbishUI(String title,String ethUrl){
        mNetworkTitleText.setText(title);
        mWeb3j = Web3j.build(new HttpService(ethUrl));

        if (TextUtils.isEmpty(mToAddressEdit.getText())) {
            mToAddressEdit.setText("");
            mToAddressBalanceText.setText("");
        }else {
            updateBalance(mToAddressEdit.getText().toString(),mToAddressBalanceText);
        }
        if (TextUtils.isEmpty(mAddress) || TextUtils.isEmpty(mWalletAddressText.getText())) {
            mWalletAddressText.setText("");
            mWalletBalanceText.setText("");
            return;
        }else {
            updateBalance(mAddress, mWalletBalanceText);
        }
    }

    /**
     * 更新钱包余额
     *  @param owner 查询的钱包地址
     *  @param view  查询的钱包余额TextView
     */
    private void updateBalance(String owner,TextView view) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "updateBalance-当前查询地址：：：" + owner);
                    final BigInteger balance = mWeb3j.ethGetBalance(owner, DefaultBlockParameterName.LATEST).send().getBalance();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BigDecimal bigDecimal = Convert.fromWei(balance.toString(), Convert.Unit.ETHER);
                            String balanceString = bigDecimal.setScale(8, RoundingMode.FLOOR).toPlainString() + " eth";
                            Log.d(TAG, "updateBalance-当前查询地址的余额：：：" + balanceString);
                            view.setText(balanceString);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "updateBalance-查询该地址的余额失败,IOException::::"+e.getMessage().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "updateBalance-查询该地址的余额失败,Exception::::"+e.getMessage().toString());
                }
            }
        });
    }


    /**
     * 展示钱包信息
     */
    public void showKeyStore(View view) {
        if (mWalletFile == null) {
            return;
        }
        if(TextUtils.isEmpty(mKeyStoreEdit.getText().toString().trim())){
            mKeyStoreEdit.setText(EthWalletController.getInstance().exportKeyStore(mWalletFile));
        }else {
            mKeyStoreEdit.setText("");
        }

    }


    /**
     * 展示钱包私钥
     */
    public void showPrivateKey(View view) {
        if (mWalletFile == null) {
            return;
        }
        if(TextUtils.isEmpty(mPrivateKeyEdit.getText().toString().trim())){
            mPrivateKeyEdit.setText(EthWalletController.getInstance().exportPrivateKey(mWalletFile));
        }else {
            mPrivateKeyEdit.setText("");
        }
    }

    /**
     * 更新当前钱包余额
     */
    public void updateAddressBalance(View view) {
        if (TextUtils.isEmpty(mAddress)
                || TextUtils.isEmpty(mWalletAddressText.getText())) {
            Toast.makeText(this,"当前钱包为空！",Toast.LENGTH_SHORT).show();
            return;
        }
        updateBalance(mAddress, mWalletBalanceText);
    }

    /**
     * 更新目标钱包余额
     */
    public void updateToAddressBalance(View view) {
        if (TextUtils.isEmpty(mToAddressEdit.getText())) {
            Toast.makeText(this,"目标钱包地址不能为空！",Toast.LENGTH_SHORT).show();
            return;
        }
        updateBalance(mToAddressEdit.getText().toString(),mToAddressBalanceText);
    }


    /**
     * 发送ETH
     */
    public void onSendEth(View view) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (mAddress == null || TextUtils.isEmpty(mToAddressEdit.getText().toString())
                        || TextUtils.isEmpty(mAmountEdit.getText().toString())) return;
                try {
                    BigInteger transactionCount = mWeb3j.ethGetTransactionCount(mAddress, DefaultBlockParameterName.LATEST).send().getTransactionCount();
                    BigInteger gasPrice = mWeb3j.ethGasPrice().send().getGasPrice();
                    Log.d(TAG, "run: onSendEth:::: " + transactionCount + ", " + gasPrice);
                    BigInteger gasLimit = new BigInteger("200000");
                    BigDecimal value = Convert.toWei(mAmountEdit.getText().toString().trim(), Convert.Unit.ETHER);
                    Log.d(TAG, "run: onSendEth:::: value wei" + value.toPlainString());
                    String to = mToAddressEdit.getText().toString().trim();
                    RawTransaction etherTransaction = RawTransaction.createEtherTransaction(transactionCount, gasPrice, gasLimit, to, value.toBigInteger());
                    //获取私钥，进行签名
                    ECKeyPair ecKeyPair = Wallet.decrypt("a12345678", mWalletFile);
                    Credentials credentials = Credentials.create(ecKeyPair);

                    byte[] bytes = TransactionEncoder.signMessage(etherTransaction, credentials);
                    String hexValue = Numeric.toHexString(bytes);
                    final String transactionHash = mWeb3j.ethSendRawTransaction(hexValue).send().getTransactionHash();
                    Log.d(TAG, "run: onSendEth:::: transactionHash :::" + transactionHash);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EthWalletActivity.this, "Send success!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: onSendEth.IOException：：： " + e.getMessage());
                } catch (CipherException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: onSendEth.CipherException：：： " + e.getMessage());
                }
            }
        });
    }

}
