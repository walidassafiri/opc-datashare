package com.example.integration;

import com.example.file.FileMetadata;
import com.example.file.FileMetadataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthUploadDownloadIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    private final List<String> uploadedTokens = new ArrayList<>();

    @AfterEach
    void cleanupUploadedFiles() throws Exception {
        for (String token : uploadedTokens) {
            FileMetadata meta = fileMetadataRepository.findById(token).orElse(null);
            if (meta != null) {
                String safeName = meta.getFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
                Path filePath = Path.of("uploads").resolve(token + "_" + safeName);
                Files.deleteIfExists(filePath);
                fileMetadataRepository.delete(meta);
            }
        }
        uploadedTokens.clear();
    }

    @Test
    void fullFlowShouldRegisterLoginUploadAndDownload() throws Exception {
        String email = "it-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        String registerPayload = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        String loginPayload = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        MvcResult loginResult = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();

        byte[] payload = "hello integration".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "integration.txt",
                MediaType.TEXT_PLAIN_VALUE,
                payload
        );

        MvcResult uploadResult = mvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("expirationDays", "1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode uploadJson = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        String fileToken = uploadJson.get("token").asText();
        uploadedTokens.add(fileToken);

        mvc.perform(get("/api/files/info/{token}", fileToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("integration.txt"));

        mvc.perform(get("/api/files/download/{token}", fileToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("integration.txt")))
                .andExpect(content().bytes(payload));
    }
}
