package com.example.file;

import com.example.security.CustomUserDetails;
import com.example.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(UploadController.class)
public class UploadControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    UploadService uploadService;

    private CustomUserDetails mockUser() {
        User user = new User("test@example.com", "hash");
        user.setId(1L);
        return new CustomUserDetails(user);
    }

    @Test
    public void unauthenticatedIsUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","a.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());
        mvc.perform(multipart("/api/files/upload").file(file).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    public void fileTooLargeReturnsBadRequest() throws Exception {
        byte[] big = new byte[10];
        MockMultipartFile file = new MockMultipartFile("file","a.txt", MediaType.TEXT_PLAIN_VALUE, big){
            @Override public long getSize() { return 2L * 1024 * 1024 * 1024; }
        };
        mvc.perform(multipart("/api/files/upload").file(file).with(user(mockUser())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    public void forbiddenExtensionReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","evil.exe", MediaType.APPLICATION_OCTET_STREAM_VALUE, "x".getBytes());
        mvc.perform(multipart("/api/files/upload").file(file).with(user(mockUser())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    public void passwordTooShortReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","a.txt", MediaType.TEXT_PLAIN_VALUE, "ok".getBytes());
        mvc.perform(multipart("/api/files/upload").file(file).param("password","123").with(user(mockUser())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    public void expirationTooLongReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","a.txt", MediaType.TEXT_PLAIN_VALUE, "ok".getBytes());
        mvc.perform(multipart("/api/files/upload").file(file).param("expirationDays","10").with(user(mockUser())).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    public void validUploadReturnsCreatedAndToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","a.txt", MediaType.TEXT_PLAIN_VALUE, "ok".getBytes());
        FileMetadata meta = new FileMetadata("tok123","a.txt", 2, LocalDateTime.now().plusDays(7), List.of("tag1"), 1L);
        when(uploadService.store(any(), any(), any())).thenReturn(meta);

        mvc.perform(multipart("/api/files/upload").file(file).with(user(mockUser())).with(csrf())).andExpect(status().isCreated()).andExpect(jsonPath("$.token").value("tok123"));
    }
}
