package com.kollect.etl.service.email.impl;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.kollect.etl.util.PropertiesUtils;
import com.kollect.etl.util.dataconnector.Regex;
import com.kollect.etl.util.dataconnector.TotalLoaded;

public class DataConnectorEmailSender {


  public static void main(String[] args) throws IOException {
    Properties emailProp = new PropertiesUtils().loadPropertiesFileResource("/server.properties");
    String user = emailProp.getProperty("email.user");
    String userAuth = emailProp.getProperty("email.userAuthentication");
    String password = emailProp.getProperty("email.pass");
    String host = emailProp.getProperty("email.host");
    String subject = emailProp.getProperty("email.subjectLoading");
    String emailMsg = emailProp.getProperty("email.msgLoading");
    int port = Integer.parseInt(emailProp.getProperty("email.port"));
    String[] recipient = emailProp.getProperty("email.recipient").split("\\s*,\\s*");
    DataConnectorEmailNotification notify = new DataConnectorEmailNotification();
    Regex r = new Regex();
    List<TotalLoaded> bodyList = r.getMatchedTokens(args[0], Integer.parseInt(emailProp.getProperty("email.daysAgo")));
    String htmlTemplate = notify.generateHtmlTemplate(emailMsg, bodyList);
    if (!bodyList.isEmpty()) notify.sendEmail(user, password, host, subject, htmlTemplate, userAuth, port, recipient);
  }

}
