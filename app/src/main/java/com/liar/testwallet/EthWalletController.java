package com.liar.testwallet;


import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *以太坊钱包管理器
 */
public class EthWalletController {

    private static final String TAG = "EthWalletManager";

    private WalletFile wallet;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static EthWalletController mEthWalletController;

    private EthWalletController() {
    }


    public static EthWalletController getInstance() {
        if (mEthWalletController == null) {
            synchronized (EthWalletController.class) {
                if (mEthWalletController == null) {
                    mEthWalletController = new EthWalletController();
                }
            }
        }
        return mEthWalletController;
    }

    /**
     *加载钱包信息
     */
    public void loadWallet(final ContextWrapper contextWrapper, final OnWalletLoadedListener listener) {
        if (wallet != null && listener != null) {
            listener.onWalletLoaded(wallet);
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                    File walletDir = contextWrapper.getDir("eth", Context.MODE_PRIVATE);
                    if (walletDir.exists() && walletDir.listFiles().length > 0) {
                        //本地已有钱包,加载钱包文件
                        Log.d(TAG, "loadWallet：：： 本地已有钱包,加载钱包文件");
                        File[] files = walletDir.listFiles();
                        try {
                            wallet = objectMapper.readValue(files[0], WalletFile.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        //本地没有钱包,创建新钱包
                        Log.d(TAG, "loadWallet：：： 没有钱包,创建新钱包");
                        createNewWallet(walletDir);
                    }
                    if (listener != null && wallet != null) {
                        listener.onWalletLoaded(wallet);
                    }
            }
        });
    }

    /**
     *生成新钱包
     */
    public void createNewWallet(File walletDir){
        Log.d(TAG, "createNewWallet：：：：生成新钱包！");
        try {
            //生成密钥对
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            //WalletFile = KeyStore
            wallet  = Wallet.createLight(Constants.PASSWORD, ecKeyPair);
            String walletFileName = getWalletFileName(wallet);
            File destination = new File(walletDir, walletFileName);
            objectMapper.writeValue(destination, wallet);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *生成钱包文件名
     */
    private static String getWalletFileName(WalletFile walletFile) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
        return dateFormat.format(new Date()) + walletFile.getAddress() + ".json";
    }

    /**
     *导出钱包文件 KeyStore，KeyStore = 私钥 + 密码
     */
    public String exportKeyStore(WalletFile wallet) {
        try {
            return objectMapper.writeValueAsString(wallet);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *导出钱包私钥
     */
    public String exportPrivateKey(WalletFile wallet) {
        try {
            ECKeyPair ecKeyPair = Wallet.decrypt(Constants.PASSWORD, wallet);
            BigInteger privateKey = ecKeyPair.getPrivateKey();
            return  Numeric.toHexStringNoPrefixZeroPadded(privateKey, Keys.PRIVATE_KEY_LENGTH_IN_HEX);
        } catch (CipherException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static interface OnWalletLoadedListener {
        void onWalletLoaded(WalletFile wallet);
    }
}
