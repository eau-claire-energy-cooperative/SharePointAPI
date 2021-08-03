package com.dustinredmond.sharepoint;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * The authorization token as returned via the username/password authentication method
 */
public class UserToken extends Token {
	private final String rtFa;
    private final String fedAuth;
    
	public UserToken(String rtFa, String fedAuth, String domain) {
		super(domain);
		
        this.rtFa = rtFa;
        this.fedAuth = fedAuth;
	}
	
	public String getRtFa() {
        return this.rtFa;
    }
    public String getFedAuth() {
        return this.fedAuth;
    }

	@Override
	public Header getHeader() {
		return new BasicHeader("cookie", this.getRtFa() + ";" + this.getFedAuth());
	}
}
