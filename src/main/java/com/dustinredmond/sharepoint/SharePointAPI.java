package com.dustinredmond.sharepoint;

import java.io.IOException;
import java.io.InputStream;
import com.google.gson.JsonObject;

/**
 * Constructs an instance of the SharePointAPI,
 * all public SharePoint API calls are available through instance methods of this class
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
     * Executes a HTTP GET request at the given path.
     * https://youDomain.sharepoint.com/${path goes here}
     * @param path The API endpoint path
     * @return The response as a String
     */
    public JsonObject get(String path) {
    	JsonObject result = null;
    	
        try {
			result = Utils.parseJson(requests.doGet(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return result;
    }

    /**
     * Executes a HTTP POST request to the given path
     * the result is the binary of the response, useful for downloading files
     * 
     * @param path the path to the file
     * @return the file as an InputStream
     */
    public InputStream getFile(String path) {
    	InputStream result = null;
    	
    	try {
			result = requests.doGetStream(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return result;
    }
    
    /**
     * Executes a HTTP POST request at the given path.
     * @param path The API endpoint path
     * @param data The data as an InputStream
     * @return The response as a JsonObject
     */
    protected JsonObject post(String path, InputStream data) {
    	JsonObject result = null;
    	
    	try {
			result = Utils.parseJson(requests.doPost(path, data));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return result;
    }

    
    /**
     * Executes a HTTP POST request at the given path.
     * @param path The API endpoint path
     * @param data The data as a String
     * @return The response as a JsonObject
     */
    protected JsonObject post(String path, String data) {
    	JsonObject result = null;
    	
    	try {
			result = Utils.parseJson(requests.doPost(path, data));
		} catch (IOException e) {
    	
			e.printStackTrace();
		}
    	
    	return result;
    }
    
    /**
     * Executes a HTTP DELETE request at the given path.
     * @param path The API endpoint path
     * @param formDigestValue The X-RequestDigest value
     * @return boolean if this was successful
     */
    protected boolean delete(String path) {
    	boolean result = false;
    	
        try {
        	//this doesn't return a response, if no error assume true
			requests.doDelete(path);
			result = true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return result;
    }

}
