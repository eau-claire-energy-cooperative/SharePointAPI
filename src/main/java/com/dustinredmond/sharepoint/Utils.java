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

public final class Utils {

	
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
