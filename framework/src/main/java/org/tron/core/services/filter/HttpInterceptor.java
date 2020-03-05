package org.tron.core.services.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.tron.core.metrics.MetricsKey;
import org.tron.core.metrics.MetricsUtil;

@Slf4j(topic = "httpIntercetpor")
public class HttpInterceptor implements Filter {

  private static final Map<String, Set<String>> EndpointMeterNameList = new HashMap<>();
  private String endpoint;


  public static Map<String, Set<String>> getEndpointList() {
    return EndpointMeterNameList;
  }


  @Override public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    try {
      if (request instanceof HttpServletRequest) {
        endpoint = ((HttpServletRequest) request).getRequestURI();
        String endpointQPS = MetricsKey.NET_API_DETAIL_ENDPOINT_QPS + "." + endpoint;
        MetricsUtil.meterMark(MetricsKey.NET_API_QPS, 1);
        MetricsUtil.meterMark(endpointQPS, 1);

        CharResponseWrapper responseWrapper = new CharResponseWrapper(
            (HttpServletResponse) response);
        chain.doFilter(request, responseWrapper);

        int reposeContentSize = responseWrapper.getByteSize();
        String endpointOutTraffic = MetricsKey.NET_API_DETAIL_ENDPOINT_OUT_TRAFFIC + "." + endpoint;
        MetricsUtil.meterMark(MetricsKey.NET_API_TOTAL_OUT_TRAFFIC,
            reposeContentSize);

        MetricsUtil.meterMark(endpointOutTraffic, reposeContentSize);
        if (!EndpointMeterNameList.containsKey(endpointOutTraffic)) {
          Set<String> st = new HashSet<>();
          st.add(endpointQPS);
          st.add(endpointOutTraffic);
          EndpointMeterNameList.put(endpoint, st);
        }

        HttpServletResponse resp = (HttpServletResponse) response;
        if (resp.getStatus() != 200) {
          String endpointFailQPS = MetricsKey.NET_API_DETAIL_ENDPOINT_FAIL_QPS + "." + endpoint;
          MetricsUtil.meterMark(MetricsKey.NET_API_FAIL_QPS, 1);
          MetricsUtil.meterMark(endpointFailQPS, 1);
          Set<String> st = EndpointMeterNameList.get(endpoint);
          if (!st.contains(endpointFailQPS)) {
            st.add(endpointQPS);
            st.add(endpointOutTraffic);
            EndpointMeterNameList.put(endpoint, st);
          }
        }

      } else {
        chain.doFilter(request, response);
      }

    } catch (Exception e) {
      if (EndpointMeterNameList.containsKey(endpoint)) {
        MetricsUtil.meterMark(MetricsKey.NET_API_DETAIL_ENDPOINT_FAIL_QPS
            + "." + endpoint, 1);
        MetricsUtil.meterMark(MetricsKey.NET_API_DETAIL_ENDPOINT_QPS
            + "." + endpoint, 1);
      }
      MetricsUtil.meterMark(MetricsKey.NET_API_QPS, 1);
      MetricsUtil.meterMark(MetricsKey.NET_API_FAIL_QPS, 1);

    }

  }

  @Override public void destroy() {

  }

}



