package com.dustinredmond.sharepoint.test;
import java.io.File;

import com.dustinredmond.sharepoint.SharePointSite;
import com.dustinredmond.sharepoint.Token;
import com.dustinredmond.sharepoint.TokenFactory;
import com.google.gson.JsonObject;

public class DownloadFileTest {

	/*
	 * Takes 6 args
	 * 1. app id - get from the SharePoint App Setup
	 * 2. app secret
	 * 3. domain - example "contoso"
	 * 4. tenant id
	 * 5. site - site string /sites/SiteName
	 * 6. file to download - /doclibrary/Word.docx
	 * 7. file location to download to - C:\folder\
	 */
	public static void main(String[] args) {
		String appId = args[0]; 
        String appSecret = args[1];
        
        // This is the subdomain of SharePoint, if your SharePoint url were
        //      https://myCompany.sharepoint.com/
        // You would supply the domain as 'myCompany'
        String domain = args[2];

        // We have to create our Token for authentication
        Token token = TokenFactory.getAppToken(appId, appSecret, domain, args[3]);
        // We can now access API methods through the SharePointAPI class
        SharePointSite api = new SharePointSite(token, args[4]);

        File folder = new File(args[6]);
        
        if(folder.isDirectory() && folder.exists())
        {
            //first get the file metadata
        	JsonObject fileMeta = api.getFile(args[5]);
        	
        	System.out.println("Found file " + fileMeta.get("Name").getAsString());
        	
        	File downloadFile = new File(folder.getAbsoluteFile() + "/" + fileMeta.get("Name").getAsString());
        	System.out.println("Writing to " + downloadFile.getAbsolutePath());
        	
        	if(api.downloadFile(args[5], downloadFile))
        	{
        		System.out.println("Download complete");
        	}
        	else
        	{
        		System.out.println("Download failed");
        	}
        }
        else
        {
        	System.out.println("Folder " + args[6] + " doesn't exist");
        }

	}

}
