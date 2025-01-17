package com.dustinredmond.sharepoint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * 
 * This class does all the low level HTTP requests with the given SharePoint site. 
 * 
 */
public class SharePointHttpRequests {
    private final Token authToken;
    
	public SharePointHttpRequests(Token authToken) {
	    this.authToken = authToken;
	}
	
    protected InputStream doGetStream(String url) throws IOException {
        final String urlPath = "https://" + authToken.getDomain() + ".sharepoint.com" + url;
        
        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(urlPath);
        httpGet.addHeader(authToken.getHeader());
        httpGet.addHeader("accept", "application/json;odata=verbose");
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 200) {
            return new ByteArrayInputStream(response.getEntity().getContent().readAllBytes());
        } else {
        	throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        }
       
    }

    protected String doGet(String url) throws IOException {
    	return Utils.inStreamToString(this.doGetStream(url));
    }

    protected String doPost(String path, InputStream data) throws IOException {
    	CloseableHttpClient client = HttpClients.createDefault();
        
        HttpPost post = new HttpPost("https://" + authToken.getDomain() + ".sharepoint.com/" + path);
        post.addHeader(authToken.getHeader());
        post.addHeader("accept", "application/json;odata=verbose");
        post.addHeader("X-RequestDigest", getFormDigestValue());
        post.addHeader("IF-MATCH", "*");

        if (data != null) {
            ByteArrayEntity input = new ByteArrayEntity(data.readAllBytes());
            post.setEntity(input);
        }

        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204) {
        	throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        }
        
        if (response.getEntity() == null || response.getEntity().getContent() == null) {
            return null;
        } else {
            return Utils.inStreamToString(response.getEntity().getContent());
        }
        
    }

    protected String doPost(String path, String data) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        
        HttpPost post = new HttpPost("https://" + authToken.getDomain() + ".sharepoint.com/" + path);
        post.addHeader(authToken.getHeader());
        post.addHeader("accept", "application/json;odata=verbose");
        post.addHeader("Content-Type", "application/json");
        post.addHeader("X-RequestDigest", getFormDigestValue());
        post.addHeader("IF-MATCH", "*");

        if (data != null) {
            StringEntity input = new StringEntity(data);
            post.setEntity(input);
        }

        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204) {
        	throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        }
        if (response.getEntity() == null || response.getEntity().getContent() == null) {
            return null;
        } else {
            return Utils.inStreamToString(response.getEntity().getContent());
        }
       
    }
    
    protected String doDelete(String path) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        
        HttpDelete del = new HttpDelete("https://" + authToken.getDomain() + ".sharepoint.com/" + path);
        del.addHeader(authToken.getHeader());
        del.addHeader("accept", "application/json;odata=verbose");
        del.addHeader("content-type", "application/json;odata=verbose");
        del.addHeader("X-RequestDigest", getFormDigestValue());
        del.addHeader("IF-MATCH", "*");
        HttpResponse response = client.execute(del);

        if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204) {
        	throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        }
        if (response.getEntity() == null || response.getEntity().getContent() == null) {
            return null;
        } else {
            return Utils.inStreamToString(response.getEntity().getContent());
        }
    }
    
    private String getFormDigestValue() {
    	String result = null;
    	
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://" + authToken.getDomain() + ".sharepoint.com/_api/contextinfo");
            post.addHeader(authToken.getHeader());
            post.addHeader("accept", "application/json;odata=verbose");
            post.addHeader("content-type", "application/json;odata=verbose");

            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204) {
            	throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
            
            if (response.getEntity() != null && response.getEntity().getContent() != null) {
                String json = Utils.inStreamToString(response.getEntity().getContent());
                int indexOfDigest = json.indexOf("\"FormDigestValue\":\"");
                String digestStart =  json.substring(indexOfDigest+19);
                
                result =  digestStart.substring(0, digestStart.indexOf("\""));
            }
            
        } catch (IOException e) {
           	e.printStackTrace();
        }
        
        return result;
    }
}
