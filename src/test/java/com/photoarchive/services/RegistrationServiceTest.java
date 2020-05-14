package com.photoarchive.services;

import com.photoarchive.domain.Token;
import com.photoarchive.domain.User;
import com.photoarchive.exceptions.UserAlreadyExistsException;
import com.photoarchive.managers.TokenManager;
import com.photoarchive.managers.UserManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@SpringBootTest
class RegistrationServiceTest {
    @MockBean
    private UserManager userManager;
    @MockBean
    private TokenManager tokenManager;
    @MockBean
    private EmailService emailService;
    @Captor
    ArgumentCaptor<User> captorUser;
    @Captor
    ArgumentCaptor<Token> captorToken;

    @Autowired
    private RegistrationService registrationService;

    @Test
    void shouldThrowWhenTryingToRegisterAlreadyExistingUser() throws UserAlreadyExistsException {
        final User user = new User();
        doThrow(UserAlreadyExistsException.class).when(userManager).saveUser(user);

        assertThatExceptionOfType(UserAlreadyExistsException.class)
                .isThrownBy(() -> registrationService.register(user));
        verify(userManager, times(1)).saveUser(user);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldRegisterNewUser() throws UserAlreadyExistsException {
        final User user = new User();
        final Token token = new Token();
        doNothing().when(userManager).saveUser(user);
        doNothing().when(emailService).sendEmail(any());
        when(tokenManager.createToken()).thenReturn(token);

        registrationService.register(user);

        verify(userManager).saveUser(captorUser.capture());
        verify(tokenManager).saveToken(captorToken.capture());
        assertThat(token).isEqualTo(captorToken.getValue());
        assertThat(user)
                .isEqualTo(captorUser.getValue())
                .isEqualTo(captorToken.getValue().getUser());
        verify(userManager, times(1)).saveUser(user);
        verify(tokenManager, times(1)).saveToken(token);
        verify(emailService, times(1)).sendEmail(any());
    }

}