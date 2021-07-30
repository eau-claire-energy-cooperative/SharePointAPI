package com.dustinredmond.sharepoint;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Constructs an instance of the SharePointAPI,
 * all public API is available through instance methods of this class
 */
public class SharePointAPI {
	private final SharePointHttpRequests requests;
	
    /**
     * Takes in a token as a parameter and constructs
     * a class through which we access the SharePoint API. Use the
     * SharePointTokenFactory to get an instance of Token
     * @param authToken com.dustinredmond.sharepoint.Token with which to authenticate to SharePoint
     */
    public SharePointAPI(Token authToken) {
        requests = new SharePointHttpRequests(authToken);
    }

    /**
     * Provided as a convenience method, to avoid having to create
     * a token via SharePointTokenFactory.getToken()
     * @param username The SharePoint username e.g. person@example.com
     * @param password The SharePoint user's password
     * @param domain The subdomain of SharePoint
     * @return An instance of the SharePointAPI for making requests to SharePoint
     */
    public SharePointAPI(String username, String password, String domain) {
        this(TokenFactory.getToken(username, password, domain));
    }
    
    /**
     * Executes a HTTP GET request at the given path.
     * https://youDomain.sharepoint.com/${path goes here}
     * @param path The API endpoint path
     * @return The response as a String
     * @throws RuntimeException If the response's status code is other than 200
     */
    public String get(String path) {
        return requests.doGet(path);
    }

    public InputStream getFile(String path) {
    	return requests.doGetStream(path);
    }
    
    /**
     * Executes a HTTP POST request at the given path.
     * @param path The API endpoint path
     * @param data The data
     * @return The response as a String
     * @throws RuntimeException If the response's status code is other than 200
     */
    @SuppressWarnings("unused")
    protected JSONObject post(String path, InputStream data) {
    	JSONObject result = null;
    	
    	String postResult = requests.doPost(path, data);
    	
    	if(postResult != null) 
    	{
    		result = (JSONObject)JSONValue.parse(postResult);
    		
    		if(result.containsKey("d"))
    		{
    			result = (JSONObject)result.get("d");
    		}
    	}
    	
        return result;
    }

    protected JSONObject post(String path, String data) {
    	JSONObject result = null;
    	
    	String postResult = requests.doPost(path, data);
    	
    	if(postResult != null) 
    	{
    		result = (JSONObject)JSONValue.parse(postResult);
    		
    		if(result.containsKey("d"))
    		{
    			result = (JSONObject)result.get("d");
    		}
    	}
    	
        return result;
    }
    
    /**
     * Executes a HTTP DELETE request at the given path.
     * @param path The API endpoint path
     * @param formDigestValue The X-RequestDigest value
     * @return The response as a String
     * @throws RuntimeException If the response's status code is other than 200
     */
    @SuppressWarnings("unused")
    protected String delete(String path) {
        return requests.doDelete(path);
    }
    
    public JSONArray listFiles(String path){
    	JSONArray result = null;
    	
    	JSONObject response = (JSONObject)JSONValue.parse(this.get(path));

    	if(response != null && response.containsKey("d"))
    	{
    		result = (JSONArray)((JSONObject)response.get("d")).get("results");
    	}
    	
    	return result;
    }

}
