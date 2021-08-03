package com.dustinredmond.sharepoint.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.dustinredmond.sharepoint.SharePointSite;
import com.dustinredmond.sharepoint.Token;
import com.dustinredmond.sharepoint.TokenFactory;
import com.google.gson.JsonObject;

public class UploadFileTest {

	/*
	 * Takes 6 args
	 * 1. app id - get from the SharePoint App Setup
	 * 2. app secret
	 * 3. domain - example "contoso"
	 * 4. tenant id
	 * 5. site - site string /sites/SiteName
	 * 6. file to upload - C:\doclibrary\Word.docx
	 * 7. file location to upload to - /doclibrary/
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

        File file = new File(args[5]);
        
        if(file.exists() && file.isFile())
        {
            //check that the folder exists
        	JsonObject folder = api.getFolder(args[6]);
        	
        	if(folder != null)
        	{
        		try {
					api.uploadFile(args[6], file.getName(), new FileInputStream(file));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	else
        	{
        		System.out.println("Folder " +  args[6] + " was not found");
        	}
        }
        else
        {
        	System.out.println("File " + args[5] + " doesn't exist");
        }
	}

}
