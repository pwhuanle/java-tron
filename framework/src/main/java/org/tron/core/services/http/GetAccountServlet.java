package org.tron.core.services.http;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;


@Component
@Slf4j(topic = "API")
public class GetAccountServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      String address = request.getParameter("address");
      Account.Builder build = Account.newBuilder();
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("address", address);
      JsonFormat.merge(jsonObject.toJSONString(), build, visible);
      fillResponse(visible, build.build(), response);
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String account = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(account);
      boolean visible = Util.getVisiblePost(account);
      Account.Builder build = Account.newBuilder();
      JsonFormat.merge(account, build, visible);
      fillResponse(visible, build.build(), response);
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  private void fillResponse(boolean visible, Account account, HttpServletResponse response)
      throws Exception {
    Account reply = wallet.getAccount(account);
    Util.printAccount(reply, response, visible);
  }
}
