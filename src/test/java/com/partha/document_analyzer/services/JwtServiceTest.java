package com.partha.document_analyzer.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey", "cGFydGhhc2FydGhpMDkzMDIxNjU2ODNkb2NhbmFseXplcnByb2plY3Q=");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
    }

    @Test
    void shouldGenerateToken() {

        // Arrange
        String email = "partha@email.com";
        Long userId = 4L;
        String role = "USER";

        // Act
        String token = jwtService.generateToken(email, userId, role);

        //Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractEmailFromToken() {

        // Arrange
        String email = "partha@email.com";
        String token = jwtService.generateToken(email, 4L, "USER");

        // Act
        String extractedEmail = jwtService.extractEmail(token);

        // Assert
        assertEquals(extractedEmail, email);
    }

    @Test
    void shouldExtractUserIdFromToken() {

        // Arrange
        Long userId = 4L;
        String token = jwtService.generateToken("partha@email.com", userId, "USER");

        // Act
        Long extractedUserId = jwtService.extractUserId(token);

        // Assert
        assertEquals(extractedUserId, userId);
    }

    @Test
    void shouldReturnTrueForValidToken() {
        // Arrange
        String token = jwtService.generateToken("partha@email.com", 4L, "USER");

        // Act
        boolean isValid = jwtService.isTokenValid(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        // Arrange
        String invalidToken = "this.is.not.a.valid.token";

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseForTamperedToken() {
        // Arrange
        String token = jwtService.generateToken("partha@email.com", 4L, "USER");
        String tamperedToken = token + "tampered";

        // Act
        boolean isValid = jwtService.isTokenValid(tamperedToken);

        // Assert
        assertFalse(isValid);
    }
}
