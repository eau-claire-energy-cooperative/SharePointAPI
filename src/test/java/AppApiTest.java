import com.dustinredmond.sharepoint.SharePointAPI;
import com.dustinredmond.sharepoint.Token;
import com.dustinredmond.sharepoint.TokenFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class AppApiTest {

	/*
	 * Takes 6 args
	 * 1. app id - get from the SharePoint App Setup
	 * 2. app secret
	 * 3. domain - example "contoso"
	 * 4. tenant id
	 * 5. site - site string /sites/SiteName
	 * 6. command to run - example GetFolderByServerRelativeUrl('/doclib')
	 */
	public static void main(String[] args) {
		String appId = args[0]; // e.g. admin@myCompany.com
        String appSecret = args[1];
        
        // This is the subdomain of SharePoint, if your SharePoint url were
        //      https://myCompany.sharepoint.com/
        // You would supply the domain as 'myCompany'
        String domain = args[2];

        // We have to create our Token for authentication
        Token token = TokenFactory.getAppToken(appId, appSecret, domain, args[3]);
        // We can now access API methods through the SharePointAPI class
        SharePointAPI api = new SharePointAPI(token);

        // We can now make a GET request use our SharePointAPI object
        // The below will get Invoice with ID 130 from the
        // InvoiceRetention SharePoint site's Invoices list
        final String site = args[4];
        JsonObject invoice = api.get(site+"/_api/web/" +  args[5]);

        // We can use Google's Gson library to make our JSON print prettily
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(invoice);
        System.out.println(prettyJson);
	}

}