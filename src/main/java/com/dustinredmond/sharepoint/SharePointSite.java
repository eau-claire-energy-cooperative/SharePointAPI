package com.dustinredmond.sharepoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 
 * Represents a specific SharePoint site as referenced by it's base url
 * 
 * This allows for higher level functions to be called on a site specifically without crafting individual SharePoint commands
 *
 */
public class SharePointSite {
	private final String API_URL = "_api/web/";
	
	private final SharePointAPI api;
	private final String baseSite;
	
	public SharePointSite(SharePointAPI api) {
		this(api, "/");
	}
	
	public SharePointSite(SharePointAPI api, String site) {
		this.api = api;
		this.baseSite = site;
	}
	
	private String urlEncodePath(String s) {
		//replace spaces but leave other characters alone
		return s.replaceAll(" ", "%20");
	}
	
	public JsonArray listFiles(String path){
    	JsonArray result = null;
    	
    	//construct url to get files for this path
    	String listUrl = baseSite + API_URL + String.format("GetFolderByServerRelativeUrl('%s')", this.urlEncodePath(path));
    	
    	//get the files and folders (two different commands)
    	JsonObject files = api.get(listUrl + "/Files");
    	JsonObject folders = api.get(listUrl + "/Folders");

    	if(files != null && folders != null)
    	{
    		//key is "results" within each object
    		result = files.getAsJsonArray("results");
    		
    		result.addAll(folders.getAsJsonArray("results"));
    	}
    	
    	return result;
    }
}
