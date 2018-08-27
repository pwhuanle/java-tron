package stest.tron.wallet.onlineStress;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class TestStorageAndCpu {
  //testng001、testng002、testng003、testng004
  private String testAccount1Key =
      "FC8BF0238748587B9617EB6D15D47A66C0E07C1A1959033CF249C6532DC29FE6";
  //"BC70ADC5A0971BA3F7871FBB7249E345D84CE7E5458828BE1E28BF8F98F2795B";
  private byte[] testAccount1Address = PublicMethed.getFinalAddress(testAccount1Key);
  private String testAccount2Key     =
      "6815B367FDDE637E53E9ADC8E69424E07724333C9A2B973CFA469975E20753FC";
  private byte[] testAccount2Address = PublicMethed.getFinalAddress(testAccount2Key);

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);


  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  @BeforeClass(enabled = false)
  public void beforeClass() {
    PublicMethed.printAddress(testAccount1Key);
    PublicMethed.printAddress(testAccount2Key);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = false)
  public void storageAndCpu() {
    Integer i = 0;
    String accountString = "THph9K2M2nLvkianrMGswRhz5hjSA9fuH7";
    Assert.assertTrue(PublicMethed
        .freezeBalance(testAccount1Address,10000000L,3,testAccount1Key,blockingStubFull));
    Assert.assertTrue(PublicMethed
        .freezeBalanceGetEnergy(testAccount1Address,1000000L,3,1,testAccount1Key,blockingStubFull));
    while (1 < 1000) {


      Long maxFeeLimit = 3900000000L;
      String contractName = "StorageAndCpu";
      String code = "60c0604052600660808190527f464f4d4f3344000000000000000000000000000000000000000000000000000060a0908152620000409160009190620000b3565b506040805180820190915260038082527f463344000000000000000000000000000000000000000000000000000000000060209092019182526200008791600191620000b3565b506305f5e1006002556000600855600b805460ff19169055348015620000ac57600080fd5b5062000158565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620000f657805160ff191683800117855562000126565b8280016001018555821562000126579182015b828111156200012657825182559160200191906001019062000109565b506200013492915062000138565b5090565b6200015591905b808211156200013457600081556001016200013f565b90565b61165980620001686000396000f3006080604052600436106101685763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166265318b811461017657806306fdde03146101a957806310d0ffdd1461023357806318160ddd1461024b578063226093731461026057806327defa1f14610278578063313ce567146102a1578063392efb52146102cc5780633ccfd60b146102e45780634b750334146102fb57806356d399e814610310578063688abbf7146103255780636b2f46321461033f57806370a08231146103545780638328b610146103755780638620410b1461038d57806389135ae9146103a25780638fea64bd146103bf578063949e8acd146103d457806395d89b41146103e9578063a8e04f34146103fe578063a9059cbb14610413578063b84c824614610437578063c47f002714610490578063e4849b32146104e9578063e9fad8ee14610501578063f088d54714610516578063fdb5a03e1461052a575b61017334600061053f565b50005b34801561018257600080fd5b50610197600160a060020a0360043516610b15565b60408051918252519081900360200190f35b3480156101b557600080fd5b506101be610b50565b6040805160208082528351818301528351919283929083019185019080838360005b838110156101f85781810151838201526020016101e0565b50505050905090810190601f1680156102255780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561023f57600080fd5b50610197600435610bde565b34801561025757600080fd5b50610197610c0e565b34801561026c57600080fd5b50610197600435610c15565b34801561028457600080fd5b5061028d610c4e565b604080519115158252519081900360200190f35b3480156102ad57600080fd5b506102b6610c57565b6040805160ff9092168252519081900360200190f35b3480156102d857600080fd5b5061028d600435610c5c565b3480156102f057600080fd5b506102f9610c71565b005b34801561030757600080fd5b50610197610d44565b34801561031c57600080fd5b50610197610d93565b34801561033157600080fd5b506101976004351515610d99565b34801561034b57600080fd5b50610197610ddc565b34801561036057600080fd5b50610197600160a060020a0360043516610de1565b34801561038157600080fd5b506102f9600435610dfc565b34801561039957600080fd5b50610197610e43565b3480156103ae57600080fd5b506102f96004356024351515610e87565b3480156103cb57600080fd5b506102f9610ee9565b3480156103e057600080fd5b50610197610eeb565b3480156103f557600080fd5b506101be610efe565b34801561040a57600080fd5b506102f9610f58565b34801561041f57600080fd5b5061028d600160a060020a0360043516602435610fa6565b34801561044357600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526102f99436949293602493928401919081908401838280828437509497506111609650505050505050565b34801561049c57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526102f99436949293602493928401919081908401838280828437509497506111b99650505050505050565b3480156104f557600080fd5b506102f960043561120d565b34801561050d57600080fd5b506102f961135e565b610197600160a060020a036004351661138b565b34801561053657600080fd5b506102f9611397565b60008060008060008060008060008a6000339050600b60009054906101000a900460ff16801561058157506801158e460913d000008261057d610ddc565b0311155b1561088e57600160a060020a03811660009081526003602052604090205460ff16151560011480156105d65750600160a060020a038116600090815260076020526040902054670de0b6b3a764000090830111155b15156105e157600080fd5b600160a060020a038116600090815260076020526040902054610604908361144d565b600160a060020a03821660009081526007602052604090205533995061062b8d6005611463565b9850610638896003611463565b9750610644898961147a565b96506106508d8a61147a565b955061065b8661148c565b945068010000000000000000870293506000851180156106855750600854610683868261144d565b115b151561069057600080fd5b600160a060020a038c16158015906106ba575089600160a060020a03168c600160a060020a031614155b80156106e05750600254600160a060020a038d1660009081526004602052604090205410155b1561072657600160a060020a038c16600090815260056020526040902054610708908961144d565b600160a060020a038d16600090815260056020526040902055610741565b610730878961144d565b965068010000000000000000870293505b600060085411156107a5576107586008548661144d565b600881905568010000000000000000880281151561077257fe5b6009805492909104909101905560085468010000000000000000880281151561079757fe5b0485028403840393506107ab565b60088590555b600160a060020a038a166000908152600460205260409020546107ce908661144d565b600460008c600160a060020a0316600160a060020a031681526020019081526020016000208190555083856009540203925082600660008c600160a060020a0316600160a060020a03168152602001908152602001600020600082825401925050819055508b600160a060020a03168a600160a060020a03167f022c0d992e4d873a3748436d960d5140c1f9721cf73f7ca5ec679d3d9f4fe2d58f88604051808381526020018281526020019250505060405180910390a3849a50610b05565b600b805460ff191690553399506108a68d6005611463565b98506108b3896003611463565b97506108bf898961147a565b96506108cb8d8a61147a565b95506108d68661148c565b9450680100000000000000008702935060008511801561090057506008546108fe868261144d565b115b151561090b57600080fd5b600160a060020a038c1615801590610935575089600160a060020a03168c600160a060020a031614155b801561095b5750600254600160a060020a038d1660009081526004602052604090205410155b156109a157600160a060020a038c16600090815260056020526040902054610983908961144d565b600160a060020a038d166000908152600560205260409020556109bc565b6109ab878961144d565b965068010000000000000000870293505b60006008541115610a20576109d36008548661144d565b60088190556801000000000000000088028115156109ed57fe5b60098054929091049091019055600854680100000000000000008802811515610a1257fe5b048502840384039350610a26565b60088590555b600160a060020a038a16600090815260046020526040902054610a49908661144d565b600460008c600160a060020a0316600160a060020a031681526020019081526020016000208190555083856009540203925082600660008c600160a060020a0316600160a060020a03168152602001908152602001600020600082825401925050819055508b600160a060020a03168a600160a060020a03167f022c0d992e4d873a3748436d960d5140c1f9721cf73f7ca5ec679d3d9f4fe2d58f88604051808381526020018281526020019250505060405180910390a3849a505b5050505050505050505092915050565b600160a060020a0316600090815260066020908152604080832054600490925290912054600954680100000000000000009102919091030490565b6000805460408051602060026001851615610100026000190190941693909304601f81018490048402820184019092528181529291830182828015610bd65780601f10610bab57610100808354040283529160200191610bd6565b820191906000526020600020905b815481529060010190602001808311610bb957829003601f168201915b505050505081565b6000808080610bee856005611463565b9250610bfa858461147a565b9150610c058261148c565b95945050505050565b6008545b90565b6000806000806008548511151515610c2c57600080fd5b610c3585611508565b9250610c42836005611463565b9150610c05838361147a565b600b5460ff1681565b600681565b600a6020526000908152604090205460ff1681565b6000806000610c806001610d99565b11610c8a57600080fd5b339150610c976000610d99565b600160a060020a038316600081815260066020908152604080832080546801000000000000000087020190556005909152808220805490839055905193019350909183156108fc0291849190818181858888f19350505050158015610d00573d6000803e3d6000fd5b50604080518281529051600160a060020a038416917fccad973dcd043c7d680389db4378bd6b9775db7124092e9e0422c9e46d7985dc919081900360200190a25050565b60008060008060085460001415610d625764d18c2e28009350610d8d565b610d6e620f4240611508565b9250610d7b836005611463565b9150610d87838361147a565b90508093505b50505090565b60025481565b60003382610daf57610daa81610b15565b610dd3565b600160a060020a038116600090815260056020526040902054610dd182610b15565b015b91505b50919050565b303190565b600160a060020a031660009081526004602052604090205490565b604080516c010000000000000000000000003390810282528251918290036014019091206000908152600a602052919091205460ff161515610e3d57600080fd5b50600255565b60008060008060085460001415610e62576501001d1bf8009350610d8d565b610e6e620f4240611508565b9250610e7b836005611463565b9150610d87838361144d565b604080516c010000000000000000000000003390810282528251918290036014019091206000908152600a602052919091205460ff161515610ec857600080fd5b506000918252600a6020526040909120805460ff1916911515919091179055565b565b600033610ef781610de1565b91505b5090565b60018054604080516020600284861615610100026000190190941693909304601f81018490048402820184019092528181529291830182828015610bd65780601f10610bab57610100808354040283529160200191610bd6565b604080516c010000000000000000000000003390810282528251918290036014019091206000908152600a602052919091205460ff161515610f9957600080fd5b50600b805460ff19169055565b600080600080600080610fb7610eeb565b11610fc157600080fd5b600b5433945060ff16158015610fef5750600160a060020a0384166000908152600460205260409020548611155b1515610ffa57600080fd5b60006110066001610d99565b111561101457611014610c71565b61101f866005611463565b925061102b868461147a565b915061103683611508565b90506110446008548461147a565b600855600160a060020a03841660009081526004602052604090205461106a908761147a565b600160a060020a038086166000908152600460205260408082209390935590891681522054611099908361144d565b600160a060020a0388811660008181526004602090815260408083209590955560098054948a16835260069091528482208054948c0290940390935582549181529290922080549285029092019091555460085461110d919068010000000000000000840281151561110757fe5b0461144d565b600955604080518381529051600160a060020a03808a1692908716917fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9181900360200190a35060019695505050505050565b604080516c010000000000000000000000003390810282528251918290036014019091206000908152600a602052919091205460ff1615156111a157600080fd5b81516111b490600190602085019061159f565b505050565b604080516c010000000000000000000000003390810282528251918290036014019091206000908152600a602052919091205460ff1615156111fa57600080fd5b81516111b490600090602085019061159f565b6000806000806000806000611220610eeb565b1161122a57600080fd5b3360008181526004602052604090205490965087111561124957600080fd5b86945061125585611508565b9350611262846005611463565b925061126e848461147a565b915061127c6008548661147a565b600855600160a060020a0386166000908152600460205260409020546112a2908661147a565b600160a060020a038716600090815260046020908152604080832093909355600954600690915291812080549288026801000000000000000086020192839003905560085491925010156113125761130e60095460085468010000000000000000860281151561110757fe5b6009555b60408051868152602081018490528151600160a060020a038916927fc4823739c5787d2ca17e404aa47d5569ae71dfb49cbf21b3f6152ed238a31139928290030190a250505050505050565b336000818152600460205260408120549081111561137f5761137f8161120d565b611387610c71565b5050565b6000610dd6348361053f565b6000806000806113a76001610d99565b116113b157600080fd5b6113bb6000610d99565b336000818152600660209081526040808320805468010000000000000000870201905560059091528120805490829055909201945092506113fd90849061053f565b905081600160a060020a03167fbe339fc14b041c2b0e0f3dd2cd325d0c3668b78378001e53160eab36153264588483604051808381526020018281526020019250505060405180910390a2505050565b60008282018381101561145c57fe5b9392505050565b600080828481151561147157fe5b04949350505050565b60008282111561148657fe5b50900390565b600854600090670de0b6b3a764000090829064174876e8006114f56114ef692a5a058fc295ed000000880269021e19e0c9bab24000006002860a02016c02863c1f5cdae42f95400000008502016ec097ce7bc90715b34b9f10000000000161156a565b8561147a565b8115156114fe57fe5b0403949350505050565b600854600090620f424083810191810190839061155764d18c2e280082850464174876e80002018702600283620f423f1982890a8b9003010464174876e8000281151561155157fe5b0461147a565b81151561156057fe5b0495945050505050565b80600260018201045b81811015610dd657809150600281828581151561158c57fe5b040181151561159757fe5b049050611573565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106115e057805160ff191683800117855561160d565b8280016001018555821561160d579182015b8281111561160d5782518255916020019190600101906115f2565b50610efa92610c129250905b80821115610efa57600081556001016116195600a165627a7a72305820bf757ace6c45263e75b0a8eb6c539065f0ca610f7bf4378df93a0eff2d0551060029";
      String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"_customerAddress\",\"type\":\"address\"}],\"name\":\"dividendsOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_ethereumToSpend\",\"type\":\"uint256\"}],\"name\":\"calculateTokensReceived\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_tokensToSell\",\"type\":\"uint256\"}],\"name\":\"calculateEthereumReceived\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"onlyAmbassadors\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint8\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"administrators\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"withdraw\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"sellPrice\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stakingRequirement\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_includeReferralBonus\",\"type\":\"bool\"}],\"name\":\"myDividends\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalEthereumBalance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_customerAddress\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_amountOfTokens\",\"type\":\"uint256\"}],\"name\":\"setStakingRequirement\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"buyPrice\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_identifier\",\"type\":\"bytes32\"},{\"name\":\"_status\",\"type\":\"bool\"}],\"name\":\"setAdministrator\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"Hourglass\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"myTokens\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"disableInitialStage\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_toAddress\",\"type\":\"address\"},{\"name\":\"_amountOfTokens\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_symbol\",\"type\":\"string\"}],\"name\":\"setSymbol\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"}],\"name\":\"setName\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_amountOfTokens\",\"type\":\"uint256\"}],\"name\":\"sell\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"exit\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_referredBy\",\"type\":\"address\"}],\"name\":\"buy\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"reinvest\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"fallback\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"customerAddress\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"incomingEthereum\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"tokensMinted\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"referredBy\",\"type\":\"address\"}],\"name\":\"onTokenPurchase\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"customerAddress\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"tokensBurned\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"ethereumEarned\",\"type\":\"uint256\"}],\"name\":\"onTokenSell\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"customerAddress\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"ethereumReinvested\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"tokensMinted\",\"type\":\"uint256\"}],\"name\":\"onReinvestment\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"customerAddress\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"ethereumWithdrawn\",\"type\":\"uint256\"}],\"name\":\"onWithdraw\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"tokens\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"}]";

      String txid = PublicMethed.deployContractAndGetTransactionInfoById(contractName,abi,code,
          "",maxFeeLimit,
          0L, 20,null,testAccount1Key,testAccount1Address,blockingStubFull);
      logger.info(txid);
      /*      byte[] contractAddress = PublicMethed.deployContract(contractName,abi,code,"",
      maxFeeLimit,
          0L, 20,null,testAccount1Key,testAccount1Address,blockingStubFull);
      SmartContract smartContract = PublicMethed.getContract(contractAddress,blockingStubFull);
      logger.info("contract name is " + smartContract.getName());*/
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      Optional<TransactionInfo> infoById = PublicMethed.getTransactionInfoById(txid,
          blockingStubFull);
      /*      logger.info("fee " + infoById.get().getFee());
      logger.info("CpuFee " + infoById.get().getReceipt().getCpuFee());
      logger.info("cpuUsage " + infoById.get().getReceipt().getCpuUsage());
      logger.info("Storagefee " + infoById.get().getReceipt().getStorageFee());
      logger.info("storageDelta " + infoById.get().getReceipt().getStorageDelta());*/
      String feeString = Long.toString(infoById.get().getFee());
      String cpuFeeString = Long.toString(infoById.get().getReceipt().getEnergyFee());
      String cpuUsageString = Long.toString(infoById.get().getReceipt().getEnergyUsage());
      //String storageFeeString = Long.toString(infoById.get().getReceipt().getStorageFee());
      //String storageDeltaString  = Long.toString(infoById.get().getReceipt().getStorageDelta());
      String netUsageString = Long.toString(infoById.get().getReceipt().getNetUsage());
      String netFeeString = Long.toString(infoById.get().getReceipt().getNetFee());
      String energyTotal = Long.toString(infoById.get().getReceipt().getEnergyUsageTotal());
      writeCsv(feeString,cpuFeeString,cpuUsageString, netUsageString,netFeeString,txid,energyTotal);

      //PublicMethed.buyStorage(10000000000L,testAccount2Address,testAccount2Key,blockingStubFull);


      //SmartContract smartContract = PublicMethed.getContract(contractAddress,blockingStubFull);

      /*      accountResource1 = PublicMethed.getAccountResource(testAccount1Address,
      blockingStubFull);
      Long afterCpuUsage = accountResource1.getCpuUsed();
      Long afterStorageUsed = accountResource1.getStorageUsed();
      account = PublicMethed.queryAccount(testAccount1Key,blockingStubFull);
      Long afterBalance = account.getBalance();
      writeCsv(accountString,beforeCpuUsage,beforeStorageUsage,beforeBalance,afterCpuUsage,
          afterStorageUsed,afterBalance);*/


      i++;
    }
  }

  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  public static void writeCsv(String feeString,String cpuFeeString,String cpuUsageString,
      String netUsageString,
      String netFeeString,String txid,String energyTotalString) {
    try {
      File csv = new File("/Users/wangzihe/Documents/test.csv");
      String time = Long.toString(System.currentTimeMillis());
      BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));
      /*      bw.write(time + "," + feeString + "," + cpuFeeString + "," + cpuUsageString + ","
          + netUsageString + ","
          + netFeeString + "," + txid);*/
      bw.write(time + "," + energyTotalString + "," + txid);
      bw.newLine();
      bw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}


