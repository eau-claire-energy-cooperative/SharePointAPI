import com.dustinredmond.sharepoint.SharePointAPI;
import com.dustinredmond.sharepoint.TokenFactory;
import com.dustinredmond.sharepoint.Token;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;

public class Test {

	/*
	 * Takes 5 args
	 * 1. username - username@mycompany.com
	 * 2. password
	 * 3. domain - example "contoso"
	 * 4. site - site string /sites/SiteName
	 * 5. command to run - example GetFolderByServerRelativeUrl('/doclib')
	 */
    public static void main(String[] args) {
    	
        String username = args[0]; // e.g. admin@myCompany.com
        String password = args[1];
        // This is the subdomain of SharePoint, if your SharePoint url were
        //      https://myCompany.sharepoint.com/
        // You would supply the domain as 'myCompany'
        String domain = args[2];

        // We have to create our Token for authentication
        Token token = TokenFactory.getToken(username, password, domain);
        // We can now access API methods through the SharePointAPI class
        SharePointAPI api = new SharePointAPI(token);

        // We can now make a GET request use our SharePointAPI object
        // The below will get Invoice with ID 130 from the
        // InvoiceRetention SharePoint site's Invoices list
        final String site = args[3];
        String invoice = api.get(site+"/_api/web/" +  args[4]);

        // We can use Google's Gson library to make our JSON print prettily
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(JsonParser.parseString(invoice));
        System.out.println(prettyJson);

        // Using Gson's JsonParser class, we can get our Invoice as an "object" of sorts
        JsonObject rootElement = JsonParser.parseString(invoice).getAsJsonObject();
    }
}
