package com.dustinredmond.sharepoint;

import java.io.InputStream;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
     */
    public SharePointAPI(String username, String password, String domain) {
        this(TokenFactory.getToken(username, password, domain));
    }
    
    private JsonObject parseJson(String json) {
    	JsonObject result = null;
    	
    	if(json != null)
    	{
    		result = JsonParser.parseString(json).getAsJsonObject();
    		
    		// if the data exists extract it
    		if(result.has("d"))
    		{
    			result = result.getAsJsonObject("d");
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Executes a HTTP GET request at the given path.
     * https://youDomain.sharepoint.com/${path goes here}
     * @param path The API endpoint path
     * @return The response as a String
     * @throws RuntimeException If the response's status code is other than 200
     */
    public JsonObject get(String path) {
        return parseJson(requests.doGet(path));
    }

    /**
     * Executes a HTTP POST request to the given path
     * the result is the binary of the response, useful for downloading files
     * 
     * @param path the path to the file
     * @return the file as an InputStream
     */
    public InputStream getFile(String path) {
    	return requests.doGetStream(path);
    }
    
    /**
     * Executes a HTTP POST request at the given path.
     * @param path The API endpoint path
     * @param data The data as an InputStream
     * @return The response as a JsonObject
     * @throws RuntimeException If the response's status code is other than 200
     */
    protected JsonObject post(String path, InputStream data) {
    	return parseJson(requests.doPost(path, data));
    }

    
    /**
     * Executes a HTTP POST request at the given path.
     * @param path The API endpoint path
     * @param data The data as a String
     * @return The response as a JsonObject
     * @throws RuntimeException If the response's status code is other than 200
     */
    protected JsonObject post(String path, String data) {
    	return parseJson(requests.doPost(path, data));
    }
    
    /**
     * Executes a HTTP DELETE request at the given path.
     * @param path The API endpoint path
     * @param formDigestValue The X-RequestDigest value
     * @return The response as a JsonObject
     * @throws RuntimeException If the response's status code is other than 200
     */
    protected JsonObject delete(String path) {
        return parseJson(requests.doDelete(path));
    }
    
    public JsonArray listFiles(String path){
    	JsonArray result = null;
    	
    	JsonObject response = this.get(path);

    	if(response != null)
    	{
    		//key is "results"
    		result = response.getAsJsonArray("results");
    	}
    	
    	return result;
    }

}
