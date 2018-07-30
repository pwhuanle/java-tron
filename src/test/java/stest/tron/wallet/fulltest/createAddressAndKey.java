package stest.tron.wallet.fulltest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.io.*;
import java.io.File;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Base64;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.Base58;
import java.io.BufferedWriter;
import java.io.FileWriter;
import stest.tron.wallet.common.client.utils.PublicMethed;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;



@Slf4j
public class createAddressAndKey {
  //testng001、testng002、testng003、testng004
  private final String testKey002 =
      "FC8BF0238748587B9617EB6D15D47A66C0E07C1A1959033CF249C6532DC29FE6";
  private final String testKey003 =
      "6815B367FDDE637E53E9ADC8E69424E07724333C9A2B973CFA469975E20753FC";

  private final byte[] fromAddress = PublicMethed.getFinalAddress(testKey002);
  private final byte[] toAddress   = PublicMethed.getFinalAddress(testKey003);


  private static String path = "/Users/wangzihe/Documents/";
  private static String filename = "/Users/wangzihe/Sites/postmanUsedKeyandAddress";
  private static String filenameTemp;

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  @BeforeClass(enabled = true)
  public void beforeClass() {

  }

  @Test(enabled = false)
  public void createAddressAndKey() {
    Integer i = 0;
    String accountIndex;
    String keyIndex;
    JsonObject jsonobject = new JsonObject();
    JsonArray jsonarray = new JsonArray();
    HashMap<String, String> addressAndKey = new HashMap<String, String>();
    while (i++ < 600) {
      ECKey ecKey1 = new ECKey(Utils.getRandom());
      byte[] address = ecKey1.getAddress();
      String addressString = ByteArray.toHexString(address);
      byte[] key = ecKey1.getPrivKeyBytes();
      String keyString = ByteArray.toHexString(key);

      logger.info(ByteArray.toStr(Base64.encode(key)));
      logger.info(ByteArray.toStr(Base64.encode(address)));
      JsonObject userBaseObj2=new JsonObject();
      userBaseObj2.addProperty("address",addressString);
      userBaseObj2.addProperty("key",keyString);
      //userBaseObj2.addProperty("address",ByteArray.toStr(Base64.encode(address)));
      //userBaseObj2.addProperty("key", ByteArray.toStr(Base64.encode(key)));
      jsonarray.add(userBaseObj2);
    }
    Gson gson = new Gson();
    String jsonMap = gson.toJson(addressAndKey);
    //createFile(filename,jsonobject.toString());
    createFile(filename,jsonarray.toString());

  }

  @AfterClass(enabled = true)
  public void shutdown() throws InterruptedException {
  }



  public static boolean createFile(String fileName,String filecontent){
    Boolean bool = false;
    filenameTemp = fileName;//文件路径+名称+文件类型
    File file = new File(filenameTemp);
    try {
      //如果文件不存在，则创建新的文件
      if(!file.exists()){
        file.createNewFile();
        bool = true;
        System.out.println("success create file,the file is "+filenameTemp);
        //创建文件成功后，写入内容到文件里
        writeFileContent(filenameTemp, filecontent);
      }
      else {
        clearInfoForFile(filenameTemp);
        writeFileContent(filenameTemp, filecontent);

      }
    } catch (Exception e) {
      e.printStackTrace();

    }

    return bool;
  }



  public static void clearInfoForFile(String fileName) {
    File file =new File(fileName);
    try {
      if(!file.exists()) {
        file.createNewFile();
      }
      FileWriter fileWriter =new FileWriter(file);
      fileWriter.write("");
      fileWriter.flush();
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * 向文件中写入内容
   * @param filepath 文件路径与名称
   * @param newstr  写入的内容
   * @return
   * @throws IOException
   */
  public static boolean writeFileContent(String filepath,String newstr) throws IOException{
    Boolean bool = false;
    String filein = newstr+"\r\n";//新写入的行，换行
    String temp  = "";

    FileInputStream fis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    FileOutputStream fos  = null;
    PrintWriter pw = null;
    try {
      File file = new File(filepath);//文件路径(包括文件名称)
      //将文件读入输入流
      fis = new FileInputStream(file);
      isr = new InputStreamReader(fis);
      br = new BufferedReader(isr);
      StringBuffer buffer = new StringBuffer();

      //文件原有内容
      for(int i=0;(temp =br.readLine())!=null;i++){
        buffer.append(temp);
        // 行与行之间的分隔符 相当于“\n”
        buffer = buffer.append(System.getProperty("line.separator"));
      }
      buffer.append(filein);

      fos = new FileOutputStream(file);
      pw = new PrintWriter(fos);
      pw.write(buffer.toString().toCharArray());
      pw.flush();
      bool = true;
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }finally {
      //不要忘记关闭
      if (pw != null) {
        pw.close();
      }
      if (fos != null) {
        fos.close();
      }
      if (br != null) {
        br.close();
      }
      if (isr != null) {
        isr.close();
      }
      if (fis != null) {
        fis.close();
      }
    }
    return bool;
  }

  /**
   * 删除文件
   * @param fileName 文件名称
   * @return
   */
  public static boolean delFile(String fileName){
    Boolean bool = false;
    filenameTemp = path+fileName+".txt";
    File file  = new File(filenameTemp);
    try {
      if(file.exists()){
        file.delete();
        bool = true;
      }
    } catch (Exception e) {
      // TODO: handle exception
    }
    return bool;
  }
}

