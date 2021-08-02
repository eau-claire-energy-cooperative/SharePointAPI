package com.dustinredmond.sharepoint;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.JsonObject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TokenFactory {
    private static final String MICROSOFT_STS = "https://login.microsoftonline.com/extSTS.srf";
    private static final String MICROSOFT_OATH = "https://accounts.accesscontrol.windows.net/%s/tokens/OAuth/2";
    
    public static Token getUserToken(String username, String password, String domain) {
        username = StringEscapeUtils.escapeXml11(username);
        password = StringEscapeUtils.escapeXml11(password);
        Token authToken;
        try {
            authToken = submitTokenToDomain(domain, doRequestUserToken(domain,username,password));
            return authToken;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Token getAppToken(String appClientId, String appClientSecret, String domain, String directoryId) {
    	Token result = null;
    	
    	String response = doRequestOauthToken(appClientId, appClientSecret, domain, directoryId);
    	
    	if(response != null)
    	{
    		//convert to JSON and get the bearer
    		JsonObject json = Utils.parseJson(response);
    		
    		System.out.println(json.get("access_token").getAsString());
    		result = new AppToken(json.get("access_token").getAsString(), domain);
    	}
    	
    	return result;
    }
    
    private static String doRequestUserToken(String domain, String username, String password) throws XPathExpressionException, SAXException, ParserConfigurationException, IOException {
        String envelope = getEnvelopeString(domain, username, password);
        URLConnection uc = new URL(MICROSOFT_STS).openConnection();
        HttpURLConnection connection = (HttpURLConnection) uc;

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-Type", "text/xml; charset=utf-8");

        try (OutputStream out = connection.getOutputStream(); Writer writer = new OutputStreamWriter(out)) {
            writer.write(envelope);
        }

        StringBuilder sb = new StringBuilder();
        try (InputStream in = connection.getInputStream()) {
            int charCode;
            while ((charCode = in.read()) != -1) {
                sb.append((char) (charCode));
            }
        }
        String token = extractTokenFromString(sb.toString());
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Login failed. Received null token");
        }
        return token;
    }

    private static String getEnvelopeString(String domain, String username, String password) {
        String envelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
                + "   <s:Header>\n"
                + "      <a:Action s:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue</a:Action>\n"
                + "      <a:ReplyTo>\n"
                + "         <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>\n"
                + "      </a:ReplyTo>\n"
                + "      <a:To s:mustUnderstand=\"1\">https://login.microsoftonline.com/extSTS.srf</a:To>\n"
                + "      <o:Security xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" s:mustUnderstand=\"1\">\n"
                + "         <o:UsernameToken>\n"
                + "            <o:Username>[[username]]</o:Username>\n"
                + "            <o:Password>[[password]]</o:Password>\n"
                + "         </o:UsernameToken>\n"
                + "      </o:Security>\n"
                + "   </s:Header>\n"
                + "   <s:Body>\n"
                + "      <t:RequestSecurityToken xmlns:t=\"http://schemas.xmlsoap.org/ws/2005/02/trust\">\n"
                + "         <wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n"
                + "            <a:EndpointReference>\n"
                + "               <a:Address>[[endpoint]]</a:Address>\n"
                + "            </a:EndpointReference>\n"
                + "         </wsp:AppliesTo>\n"
                + "         <t:KeyType>http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey</t:KeyType>\n"
                + "         <t:RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</t:RequestType>\n"
                + "         <t:TokenType>urn:oasis:names:tc:SAML:1.0:assertion</t:TokenType>\n"
                + "      </t:RequestSecurityToken>\n"
                + "   </s:Body>\n"
                + "</s:Envelope>";
        return envelope.replace("[[username]]", username)
                .replace("[[password]]", password)
                .replace("[[endpoint]]",
                        String.format("https://%s.sharepoint.com/_forms/default.aspx?wa=wsignin1.0", domain));
    }

    private static String extractTokenFromString(String result) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(result)));
        XPath xp = XPathFactory.newInstance().newXPath();
        return xp.evaluate("//BinarySecurityToken/text()", document.getDocumentElement());
    }

    private static Token submitTokenToDomain(String domain, String token) throws IOException {
        String loginContextPath = "/_forms/default.aspx?wa=wsignin1.0";
        String url = String.format("https://%s.sharepoint.com%s", domain, loginContextPath);
        CookieHandler.setDefault(null);
        URL u = new URL(url);
        URLConnection uc = u.openConnection();
        HttpURLConnection connection = (HttpURLConnection) uc;
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/x-www-form-urlencoded");
        connection.addRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setInstanceFollowRedirects(false);

        try (OutputStream out = connection.getOutputStream(); Writer writer = new OutputStreamWriter(out)) {
            writer.write(token);
        }

        String rtFa = null;
        String fedAuth = null;
        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> cookies = headers.get("Set-Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith("rtFa=")) {
                    rtFa = "rtFa=" + HttpCookie.parse(cookie).get(0).getValue();
                } else if (cookie.startsWith("FedAuth=")) {
                    fedAuth = "FedAuth=" + HttpCookie.parse(cookie).get(0).getValue();
                }
            }
        }
        return new UserToken(rtFa, fedAuth, domain);
    }
    
    private static String doRequestOauthToken(String appClientId, String appClientSecret, String domain, String directoryId) {
    	String result = null;
    	
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(String.format(TokenFactory.MICROSOFT_OATH, directoryId));
            post.addHeader("accept", "application/json");

            List<NameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("resource", "00000003-0000-0ff1-ce00-000000000000/" + domain + ".sharepoint.com@" + directoryId));
            form.add(new BasicNameValuePair("client_id", appClientId + "@" + directoryId));
            form.add(new BasicNameValuePair("client_secret", appClientSecret));
            form.add(new BasicNameValuePair("grant_type", "	client_credentials"));
            HttpEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
            
            post.setEntity(entity);
            
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204) {
            	throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
            
            if (response.getEntity() != null && response.getEntity().getContent() != null) {
                result = Utils.inStreamToString(response.getEntity().getContent());
            }
            
        } catch (IOException e) {
           	e.printStackTrace();
        }
        
        return result;
    }
}
