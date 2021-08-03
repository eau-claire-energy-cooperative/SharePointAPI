package com.dustinredmond.sharepoint;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * The authorization token as returned via the SharePoint app authentication method. 
 */
public class AppToken extends Token {
	private final String accessToken;
	
	AppToken(String accessToken , String domain) {
		super(domain);
		
		this.accessToken = accessToken;
	}
	
	public String getAccessToken() {
		return this.accessToken;
	}

	@Override
	public Header getHeader() {
		return new BasicHeader("Authorization", "Bearer " + this.getAccessToken());
	}

}
