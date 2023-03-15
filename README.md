# TestBlockchainWallet
BlockchainWalletDemo 
如何使用Web3j开发一个以太坊钱包APP（Android ）

Web3j是一个Java库，它提供了与以太坊区块链网络进行交互的工具和API。
使用这个库，你可以开发一个安卓钱包APP，与以太坊网络进行交互，包括发送和接收以太币和其他代币。
Web3j的Github地址：https://github.com/web3j/web3j
操作步骤：
1.创建一个Android项目，并在项目的build.gradle文件中添加以下依赖项：
dependencies {
    implementation 'org.web3j:core:4.8.7-android'
    implementation 'com.android.support:appcompat-v7:28.0.0'
}
2.创建一个Web3j实例：
使用Web3j.build()方法创建一个Web3j实例，该实例与以太坊主网进行通信。
//创建一个Web3j实例
Web3j web3j = Web3j.build(new HttpService("https://goerli.infura.io/<your-infura-api-key>"));
注：将 <your-infura-api-key> 替换为你自己的Infura API密钥，Infura API的密钥可以在Infura 官网注册获取，官网地址： https://app.infura.io/
3.创建一个以太坊钱包：
使用WalletUtils.generateNewWalletFile()方法创建一个新的以太坊钱包，并将其保存在指定的目录中。
//创建一个新的以太坊钱包
String walletFileName = WalletUtils.generateNewWalletFile("password", new File("/path/to/keystore/directory"), false);
注：需要将 "password" 和 "/path/to/keystore/directory" 替换为使用的密码和钱包文件目录。
4.获取以太坊钱包的余额：
使用web3j.ethGetBalance()方法获取指定钱包地址的余额。
//获取"目标钱包地址"的余额。
EthGetBalance balance = web3j.ethGetBalance("目标钱包地址", DefaultBlockParameterName.LATEST).send();
BigInteger wei = balance.getBalance();
注：将 "目标钱包地址" 替换为需要查询余额的以太坊钱包地址。
5.发送以太币：
先用WalletUtils.loadCredentials()方法加载一个以太坊钱包文件，再使用Transfer.sendFunds()方法发送1个以太币到  "目标钱包地址"。
//加载以太坊钱包文件
Credentials credentials = WalletUtils.loadCredentials("password", "/path/to/wallet/file");
//发送1个以太币到 "目标钱包地址"
TransactionReceipt receipt = Transfer.sendFunds(web3j, credentials, "目标钱包地址", BigDecimal.valueOf(1.0), Convert.Unit.ETHER).send();

注：要将 "password" 和 "/path/to/wallet/file" 替换为要使用的以太坊钱包文件的密码和路径，"目标钱包地址" 替换为需要查询余额的以太坊钱包地址。
6.接收以太币：
使用以太坊智能合约来接收以太币，需要在应用程序中部署一个智能合约，并向用户提供一个收款地址。当用户将以太币发送到该地址时，智能合约会自动将它们存储在合约帐户中，然后使用    web3j.ethGetBalance()方法查询合约帐户的余额。
7.与以太坊智能合约交互：
使用Web3j库与以太坊智能合约进行交互，用MyContract.deploy()方法部署一个智能合约，使用contract.myMethod()方法调用一个合约方法，并使用contract.myEventEventObservable()方法监听一个事件。
// 部署合约
MyContract contract = MyContract.deploy(web3j, credentials, GAS_PRICE, GAS_LIMIT, BigInteger.ZERO).send();
// 调用合约方法
TransactionReceipt receipt = contract.myMethod(arg1, arg2).send();
// 监听事件
contract.myEventEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
    .subscribe(event -> {
        // 处理事件
});

注：要将 "MyContract" 替换为要交互的智能合约类名，并根据该智能合约定义自定义方法和事件。
8.实现用户界面：
这点就不用讲解了，贴下实现的界面：
