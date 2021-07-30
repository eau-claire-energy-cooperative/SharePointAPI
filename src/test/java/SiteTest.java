import com.dustinredmond.sharepoint.SharePointAPI;
import com.dustinredmond.sharepoint.SharePointSite;
import com.dustinredmond.sharepoint.TokenFactory;
import com.dustinredmond.sharepoint.Token;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

public class SiteTest {

	/*
	 * Takes 5 args
	 * 1. username - username@mycompany.com
	 * 2. password
	 * 3. domain - example "contoso"
	 * 4. site - site string /sites/SiteName
	 * 5. path to a document library - example "/SiteLibrary"
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

        SharePointSite api = new SharePointSite(new SharePointAPI(token), args[3]);

        JsonArray files = api.listFiles(args[4]);

        // We can use Google's Gson library to make our JSON print prettily
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(files);
        System.out.println(prettyJson);
    }
}
