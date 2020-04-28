package com.photoarchive.security;

import com.photoarchive.exceptions.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private static final String USERNAME_ALREADY_EXISTS_MESSAGE = "User with this username already exists!";
    private static final String EMAIL_ALREADY_EXISTS_MESSAGE = "User with this email already exists!";


    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findByUsername(username);
        if (isNull(user)) {
            throw new UsernameNotFoundException("User " + username + " not found");
        }
        return user;
    }

    public void register(User user) throws UserAlreadyExistsException {
        if (usernameExists(user.getUsername())){
            log.warn("User "+user.getUsername()+" already exists");
            throw new UserAlreadyExistsException(USERNAME_ALREADY_EXISTS_MESSAGE);
        }
        if (emailExists(user.getEmail())){
            log.warn("User "+user.getEmail()+" already exists");
            throw new UserAlreadyExistsException(EMAIL_ALREADY_EXISTS_MESSAGE);
        }
        userRepository.save(user);
        log.info("User "+user.getUsername()+" saved to database");
    }

    private boolean usernameExists(String username){
        return userRepository.findByUsername(username) != null;
    }
    private boolean emailExists(String email){
        return userRepository.findByEmail(email) != null;
    }
}
