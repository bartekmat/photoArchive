package com.photoarchive.controllers;

import com.photoarchive.domain.User;
import com.photoarchive.managers.TokenManager;
import com.photoarchive.managers.UserManager;
import com.photoarchive.managers.ResetCodeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChangePasswordController.class)
class ChangePasswordControllerTest {

    @MockBean
    private UserManager userManager;
    @MockBean
    private TokenManager tokenManager;
    @MockBean
    private ResetCodeManager resetCodeManager;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private String resetCode;
    private String tokenValue;
    private LocalDateTime creationDate;
    private User user;
    private String newPassword;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        resetCode = "resetCode";
        tokenValue = "tokenValue";
        creationDate = LocalDateTime.now();
        user = new User();
        newPassword = "newPassword1";
    }

    @Test
    void shouldNotProcessResetWhenResetCodeIsWrong() throws Exception {
        when(resetCodeManager.extractTokenValue(resetCode)).thenReturn(null);
        when(resetCodeManager.extractCreationDate(resetCode)).thenThrow(DateTimeException.class);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/change")
                .param("value", resetCode))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", ReflectionTestUtils.getField(ChangePasswordController.class, "INVALID_LINK_MESSAGE")))
                .andExpect(view().name("email-input"));

        verify(resetCodeManager, times(1)).extractTokenValue(resetCode);
        verify(resetCodeManager, times(1)).extractCreationDate(resetCode);
        verifyNoInteractions(userManager, tokenManager);
    }

    @Test
    void shouldNotProcessWhenTokenDoesntExist() throws Exception {
        when(resetCodeManager.extractTokenValue(resetCode)).thenReturn(tokenValue);
        when(resetCodeManager.extractCreationDate(resetCode)).thenReturn(creationDate);
        when(tokenManager.existsByValue(tokenValue)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/change")
                .param("value", resetCode))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", ReflectionTestUtils.getField(ChangePasswordController.class, "INVALID_LINK_MESSAGE")))
                .andExpect(view().name("email-input"));

        verify(resetCodeManager, times(1)).extractTokenValue(resetCode);
        verify(resetCodeManager, times(1)).extractCreationDate(resetCode);
        verify(tokenManager, times(1)).existsByValue(tokenValue);
        verifyNoInteractions(userManager);
    }

    @Test
    void shouldNotProcessWhenTokenHasExpired() throws Exception {
        when(resetCodeManager.extractTokenValue(resetCode)).thenReturn(tokenValue);
        when(resetCodeManager.extractCreationDate(resetCode)).thenReturn(creationDate);
        when(tokenManager.existsByValue(tokenValue)).thenReturn(true);
        when(tokenManager.hasExpired(creationDate)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/change")
                .param("value", resetCode))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", ReflectionTestUtils.getField(ChangePasswordController.class, "EXPIRED_LINK_MESSAGE")))
                .andExpect(view().name("email-input"));
        verify(resetCodeManager, times(1)).extractTokenValue(resetCode);
        verify(resetCodeManager, times(1)).extractCreationDate(resetCode);
        verify(tokenManager, times(1)).existsByValue(tokenValue);
        verify(tokenManager, times(1)).hasExpired(creationDate);
        verifyNoInteractions(userManager);
    }

    @Test
    void shouldProcessPasswordReset() throws Exception {
        when(resetCodeManager.extractTokenValue(resetCode)).thenReturn(tokenValue);
        when(resetCodeManager.extractCreationDate(resetCode)).thenReturn(creationDate);
        when(tokenManager.existsByValue(tokenValue)).thenReturn(true);
        when(tokenManager.hasExpired(creationDate)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/change")
                .param("value", resetCode))
                .andExpect(status().isOk())
                .andExpect(model().attribute("resetCode", resetCode))
                .andExpect(view().name("new-password-input"));
        verify(resetCodeManager, times(1)).extractTokenValue(resetCode);
        verify(resetCodeManager, times(1)).extractCreationDate(resetCode);
        verify(tokenManager, times(1)).existsByValue(tokenValue);
        verify(tokenManager, times(1)).hasExpired(creationDate);
        verifyNoInteractions(userManager);
    }

    @Test
    void shouldProcessPasswordChange() throws Exception {
        when(resetCodeManager.extractTokenValue(resetCode)).thenReturn(tokenValue);
        when(userManager.loadUserByToken(tokenValue)).thenReturn(Optional.of(user));
        doNothing().when(userManager).setNewPassword(user, newPassword);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/change")
                .param("password", newPassword)
                .param("matchingPassword", newPassword)
                .flashAttr("resetCode", resetCode))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", ReflectionTestUtils.getField(ChangePasswordController.class, "PASSWORD_CHANGED_MESSAGE")))
                .andExpect(view().name("login"));

        verify(resetCodeManager, times(1)).extractTokenValue(resetCode);
        verify(userManager, times(1)).loadUserByToken(tokenValue);
        verify(userManager, times(1)).setNewPassword(user, newPassword);
    }

    @Test
    void shouldNotProcessPasswordChange() throws Exception {
        when(resetCodeManager.extractTokenValue(resetCode)).thenReturn(tokenValue);
        when(userManager.loadUserByToken(tokenValue)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/change")
                .param("password", newPassword)
                .param("matchingPassword", newPassword)
                .flashAttr("resetCode", resetCode))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("message"))
                .andExpect(view().name("login"));

        verify(resetCodeManager, times(1)).extractTokenValue(resetCode);
        verify(userManager, times(1)).loadUserByToken(tokenValue);
        verifyNoMoreInteractions(userManager);
    }
}