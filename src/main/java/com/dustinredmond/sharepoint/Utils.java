package com.dustinredmond.sharepoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Util methods used throughout the program
 */
public final class Utils {

	
	/**
	 * @param json a JSON string 
	 * @return deserialized JSON value as an object
	 * 
	 * Microsoft formated JSON responses are an object starting containing one key "d". This function returns the "d" value directly when it exists. 
	 */
	public static JsonObject parseJson(String json) {
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
	 * @param in inputstream to convert to a string
	 * @return value converted to a String
	 * @throws IOException
	 * 
	 * Converts an input stream (usually from an HTTP request) to a string
	 */
	public static String inStreamToString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        Charset cs = Charset.forName(StandardCharsets.UTF_8.name());
        try (Reader reader = new BufferedReader(new InputStreamReader(in, cs))) {
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }
}
