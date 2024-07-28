package org.snapgram.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.snapgram.model.response.ResponseObject;
import org.snapgram.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
class UserControllerTest {
    @InjectMocks
    UserController userController;
    @Autowired
    private MockMvc mockMvc;
    @Mock
    IUserService userService;
    @Value("${API_PREFIX}")
    private String apiPrefix;

    @BeforeEach
    public void setup() {

    }

    // Begin testing the emailExists method
    @Test
    void emailExists_ReturnBadRequest_WhenInvalidEmail() throws Exception {
        mockMvc.perform(get(apiPrefix + "/users/email-exists")
                        .param("email", "vriusaggedu.com")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.data.error").value("Invalid Data"))
                .andExpect(jsonPath("$.data.path").value(apiPrefix + "/users/email-exists"));
    }

    @Test
    void emailExists_ReturnBadRequest_WhenEmailBlank() throws Exception {
        mockMvc.perform(get(apiPrefix + "/users/email-exists")
                        .param("email", "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.data.error").value("Invalid Data"))
                .andExpect(jsonPath("$.data.path").value(apiPrefix + "/users/email-exists"));
    }

    @Test
    void emailExists_ReturnTrue_WhenEmailExists() {
        when(userService.isEmailExists(any(String.class))).thenReturn(true);

        ResponseObject<Boolean> response = userController.emailExists("");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().data());

    }

    @Test
    void emailExists_ReturnFalse_WhenEmailDoesNotExist() {
        when(userService.isEmailExists(any(String.class))).thenReturn(false);

        ResponseObject<Boolean> response = userController.emailExists("");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody().data());

    }
    // End testing the emailExists method
}
