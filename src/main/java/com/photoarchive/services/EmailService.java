package com.photoarchive.services;

import com.photoarchive.domain.Token;
import com.photoarchive.repositories.TokenRepository;
import com.photoarchive.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {

    private TokenRepository tokenRepository;

    @Autowired
    public EmailService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void sendVerificationEmail(User user) {
    }

    private Token createToken(User user){
        Token token = new Token();
        String value = UUID.randomUUID().toString();
        token.setValue(value);
        token.setUser(user);
        return token;
    }
}
