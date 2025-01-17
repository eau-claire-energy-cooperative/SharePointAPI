package com.dustinredmond.sharepoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	
	public SharePointSite(Token token) {
		this(token, "/");
	}
	
	public SharePointSite(SharePointAPI api) {
		this(api, "/");
	}
	
	public SharePointSite(Token token, String site) {
		this(new SharePointAPI(token), site);
	}
	
	public SharePointSite(SharePointAPI api, String site) {
		this.api = api;
		this.baseSite = site;
	}
	
	private String urlEncodePath(String s) {
		
		// if not in the root site, you must use the full path to the file
		if(this.baseSite.startsWith("/sites"))
		{
			s = this.baseSite.substring(0, this.baseSite.length() - 1) + s;
		}
		
		//replace spaces but leave other characters alone
		return s.replaceAll(" ", "%20");
	}
	
	/**
	 * @param path absolute SharePoint path "/doc lib/" to a file folder
	 * @return an array containing all the file/folder metadata from this folder
	 * 
	 * Queries both the /Files and /Folders endpoints to return a combined array of both
	 * 
	 */
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
	
	/**
	 * @param path path to the folder
	 * @return the folder metadata as a JsonObject
	 * 
	 * returns NULL if the folder doesn't exist
	 */
	public JsonObject getFolder(String path) {
		String url = baseSite + API_URL + String.format("GetFolderByServerRelativeUrl('%s')", this.urlEncodePath(path));
		
		return api.get(url);
	}
	
	/**
	 * @param path path to the file
	 * @return the folder metadata as a JsonObject
	 * 
	 * Returns NULL if the file doesn't exist. 
	 * 
	 */
	public JsonObject getFile(String path) {
		String url = baseSite + API_URL + String.format("getfilebyserverrelativeurl('%s')", this.urlEncodePath(path));
		
		return api.get(url);
	}
	
	/**
	 * @param folderPath the path to the folder in SharePoint to upload the file to
	 * @param filename the name you want the file to have in SharePoint
	 * @param fileData the stream of data representing the file
	 * @return true if success, false if doesn't
	 * 
	 * Uploads the data given by the InputStream to the specified folder in SharePoint with the given filename
	 */
	public boolean uploadFile(String folderPath, String filename, InputStream fileData) {
		String url = baseSite + API_URL + String.format("GetFolderByServerRelativeUrl('%s')/Files/add(url='%s',overwrite=true)", urlEncodePath(folderPath), urlEncodePath(filename));
		
		JsonObject result = api.post(url, fileData);
		
		return result != null && result.get("Exists").getAsString().equals("true");
	}
	
	/**
	 * @param path full path to the file to download
	 * @return an InputStream representing the file data
	 * 
	 * Download the binary data from SharePoint for the given file and returns an InputStream for use elsewhere. 
	 */
	public InputStream downloadFile(String path) {
		// attempt to get the file contents from online
		String url = baseSite + API_URL + String.format("getfilebyserverrelativeurl('%s')/$value", this.urlEncodePath(path));
		
		return api.getFile(url);
	}
	
	/**
	 * @param path full path to the file to download
	 * @param dest the destination file to write the file to
	 * @return true if success, false if not
	 * 
	 *  Download the file specified at the given SharePoint path to the File location specified
	 */
	public boolean downloadFile(String path, File dest) {
		boolean result = false; //assume we'll fail
		
		InputStream fileStream = this.downloadFile(path);
		
		if(fileStream != null)
		{
			//we have the file, save to disk
			try{
				OutputStream outstream = new FileOutputStream(dest);
		
				byte[] buf = new byte[1024];
				int len;
				
				while ((len = fileStream.read(buf)) > 0)
				{
					outstream.write(buf, 0, len);
				}
				
				//close the stream here
				fileStream.close();
				outstream.close();
				
				//made it!
				result = true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * @param path path of the file to delete
	 * @return true if success, false if not
	 */
	public boolean deleteFile(String path) {
		String url = baseSite + API_URL + String.format("getfilebyserverrelativeurl('%s')", this.urlEncodePath(path));
		
		return api.delete(url);
	}
}
