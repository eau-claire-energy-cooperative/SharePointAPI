package com.dustinredmond.sharepoint;

import org.apache.http.Header;

/**
 *  Base token class that can be used to implement various Token types as returned via the various authentication methods. 
 */
public abstract class Token {
    private final String domain;

    Token(String domain) {
        this.domain = domain;
    }

    /**
     * @return the HTTP header added to the request that includes the token information
     */
    public abstract Header getHeader();
    
    public String getDomain() {
        return this.domain;
    }

}
