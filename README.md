# SharePointAPI

Small java library to allow programmatically sending REST calls to SharePoint sites. It allows for authenticating with a valid username and password or creating a SharePoint app within the site you wish to perform operations on. 

---

## Background

You can interact with SharePoint programmatically using [a REST API](https://docs.microsoft.com/en-us/sharepoint/dev/sp-add-ins/get-to-know-the-sharepoint-rest-service?tabs=csom). The API defines a series of endpoints that you can use to interact with the file structure of a given SharePoint site. This library attempts to provide a wrapper around these web calls to provide higher level interaction via Java. 

There are two main entry points. The first is the lower level `SharePointAPI` class. This provides access to direct HTTP actions such as `GET` and `POST`. The programmer must provide [a valid REST command](https://docs.microsoft.com/en-us/sharepoint/dev/sp-add-ins/complete-basic-operations-using-sharepoint-rest-endpoints) and results are returned as JSON objects. 

The other entry point is the `SharePointSite` class. This class collects some higher level operations, utilizing the `SharePointAPI` class for the actual low levels calls. This allows you to perform operations like listing a document library's file contents or downloading a file without needing to know the exact structure of the REST commands. 

## Install

The easiest way to install is to add the JAR, and required dependencies, to a Maven project. Required libraries are: 

* [Apache HTTPClient](https://hc.apache.org/httpcomponents-client-5.1.x/)
* [Google GSON](https://github.com/google/gson)

The Javadoc is also available on [GitHub Pages](https://eau-claire-energy-cooperative.github.io/SharePointAPI/com/dustinredmond/sharepoint/package-summary.html). 

## Usage

Prior to using the library you'll need to make a choice on if you want to authenticate using a SharePoint username/password or by creating a SharePoint App for the given site. The SharePoint App is Microsoft's officially supported way of using the REST API; however it requires some additional setup setup steps (see below). There are some [example classes](https://github.com/eau-claire-energy-cooperative/SharePointAPI/tree/updates/src/test/java) in the test directory located under `src/`. These provide some basic examples for interacting with files and folders. 

### Authenticating

The `TokenFactory` class handles both authentication methods. Examples of each are given below. For reference the information needed for authentication is listed below. 

| Value | Description |
|--|--|
| Username | a valid SharePoint username, admin@mycompany.com |
| Password | the password for this user |
| Domain | the domain if your SharePoint tenant, typically part of the url such as _mycompany_.sharepoint.com | 
| App Id | the application id of your SharePoint App |
| App Secret | the secret for the given app id |
| Tenant Id | the id of the given O365 Tenant, found in the Azure Admin


```java
import com.dustinredmond.sharepoint.TokenFactory;
import com.dustinredmond.sharepoint.SharePointAPI;
    
class TestSharePoint {

    public static void main(String[] args) {
    
    String username = "admin@mycompany.com"
    String password = "password";
    String domain = "mycompany.com";
    
    // authenticate using a username
    token = TokenFactory.getUserToken(username, password, domain);

	// authenticate using a SharePoint app
	String appId = "appid";
	String appSecret = "appsecret";
	String tenantId = "tenantid";

	token = TokenFactory.getAppToken(appId, appSecret, domain, tenantId);

	// once a valid token exists, create the SharePointAPI class
	api = new SharePointAPI(token);
}
    
```

### Making Requests

Once a `SharePointAPI` or `SharePointSite` object is created it can be used to query the a SharePoint site. 

__Using SharePointAPI__

```java
    // using SharePointAPI variable made above
    JsonObject invoiceJson = api.get("/Sites/InvoiceRetention/_api/web/GetFolderByServerRelativeUrl('/Invoices/Invoice1.docx')");

	System.out.println(invoiceJson.get('Name').getAsString());
```

The above code get the file at location  `/Invoices/Invoice1.docx` and print out the file name. 

__Using SharePointSite__

```java
    // make a SharePointSite, need to give it an api object as well as the path to the site
    SharePointSite aSite = new SharePointSite(api, "/Sites/InvoiceRetention")
    
    JsonObject invoiceFile = aSite.getFile('/Invoices/Invoice1.docx');

	System.out.println(invoiceFile);
```

Similar to the first example, you can get a JsonObject representing the file in a given document library. Note the big difference in that you specify the site URL when you create the object and then don't need to craft the full command. This done for you with the `getFile` method. 

### Creating SharePoint App
Creating a SharePoint app has some additional setup steps, but doesn't tie you to a specific user. This is useful for when things like changing passwords or MFA prevent you from using username authentication. To setup the SharePoint app you need to perform a few steps as a user with Admin permissions on a given SharePoint site. 

__Create the App__

1. Go to [https://yourtenantname.sharepoint.com/_layouts/15/appregnew.aspx](https://ccdevanoop.sharepoint.com/sites/ModernTeamSite/_layouts/15/appregnew.aspx) 
2. On this page generate a new client id and client secret - save these for later use. For the App Domain use _localhost_ and for the Redirect URI use _http://localhost_ (these won't be used). 
3. Save the app
4. Go to [https://](https://ccdevanoop.sharepoint.com/sites/ModernTeamSite/_layouts/15/appinv.aspx)[yourtenantname](https://ccdevanoop.sharepoint.com/sites/ModernTeamSite/_layouts/15/appregnew.aspx)[.sharepoint.com/_layouts/15/appinv.aspx](https://ccdevanoop.sharepoint.com/sites/ModernTeamSite/_layouts/15/appinv.aspx)
5. On this page use the client id in the first box and hit "lookup" to find your app. 
6. In the permissions box paste the following, this will give your app full control over this site. For more info on permissions [read the MS article](https://docs.microsoft.com/en-us/sharepoint/dev/sp-add-ins/add-in-permissions-in-sharepoint). 
```
<AppPermissionRequests AllowAppOnlyPolicy="true">
  <AppPermissionRequest Scope="http://sharepoint/content/sitecollection/web" Right="FullControl"/>
</AppPermissionRequests>
```
7. Hit Save and hit _Trust It_ on the next screen. 
8. You'll also need your Tenant ID. This can be found in Azure AD. For exact instructions [read this article](https://docs.microsoft.com/en-us/azure/active-directory/fundamentals/active-directory-how-to-find-tenant). You'll need this to authenticate as well. 

Once complete you should have a client ID (app ID), a client secret, and your tenant ID for authentication. These are passed to the `TokenFactory` class to create an OAuth request and generate a token for use against the REST API. 
