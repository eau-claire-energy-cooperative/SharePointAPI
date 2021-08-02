package com.dustinredmond.sharepoint;

import org.apache.http.Header;

public abstract class Token {
    private final String domain;

    Token(String domain) {
        this.domain = domain;
    }

    public abstract Header getHeader();
    
    public String getDomain() {
        return this.domain;
    }

}
