package com.photoarchive.exceptions;

public class TokenNotFoundException extends Exception{
    public TokenNotFoundException() {
         super("Token not found");
    }
}
