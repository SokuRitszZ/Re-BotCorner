package com.soku.rebotcorner.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class HttpClientUtil {
  public static String get(String url, List<NameValuePair> params) {
    URIBuilder uriBuilder = null;
    try {
      uriBuilder = new URIBuilder(url);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
    uriBuilder.setParameters(params);

    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpGet httpGet = new HttpGet(uriBuilder.build());
      CloseableHttpResponse response = client.execute(httpGet);
      HttpEntity entity = response.getEntity();
      return EntityUtils.toString(entity);
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }
}