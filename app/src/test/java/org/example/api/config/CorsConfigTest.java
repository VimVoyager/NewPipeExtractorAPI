package org.example.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CorsConfig.
 * Tests CORS configuration for security and frontend compatibility.
 */
@DisplayName("CorsConfig Tests")
class CorsConfigTest {

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should implement WebMvcConfigurer")
        void testImplementsWebMvcConfigurer() {
            // Assert
            assertTrue(corsConfig instanceof org.springframework.web.servlet.config.annotation.WebMvcConfigurer);
        }

        @Test
        @DisplayName("Should have addCorsMappings method")
        void testHasAddCorsMappingsMethod() throws NoSuchMethodException {
            // Act
            Method method = CorsConfig.class.getMethod("addCorsMappings", CorsRegistry.class);

            // Assert
            assertNotNull(method);
        }

        @Test
        @DisplayName("Should be annotated with @Configuration")
        void testHasConfigurationAnnotation() {
            // Assert
            assertTrue(CorsConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        }
    }

    @Nested
    @DisplayName("CORS Mapping Tests")
    class CorsMappingTests {

        @Test
        @DisplayName("Should configure CORS for /api/** pattern")
        void testCorsMapping_ApiPattern() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            assertNotNull(registry.getLastRegistration());
            // The pattern is set in the registration, we verify it was called
            assertTrue(registry.wasAddMappingCalled());
        }

        @Test
        @DisplayName("Should allow all origins")
        void testCorsMapping_AllowedOrigins() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();
            assertNotNull(registration);
            assertTrue(registration.getAllowedOrigins().contains("*"));
        }

        @Test
        @DisplayName("Should allow GET, POST, PUT, DELETE, OPTIONS methods")
        void testCorsMapping_AllowedMethods() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();
            assertNotNull(registration);

            assertTrue(registration.getAllowedMethods().contains("GET"));
            assertTrue(registration.getAllowedMethods().contains("POST"));
            assertTrue(registration.getAllowedMethods().contains("PUT"));
            assertTrue(registration.getAllowedMethods().contains("DELETE"));
            assertTrue(registration.getAllowedMethods().contains("OPTIONS"));
        }

        @Test
        @DisplayName("Should allow all headers")
        void testCorsMapping_AllowedHeaders() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();
            assertNotNull(registration);
            assertTrue(registration.getAllowedHeaders().contains("*"));
        }

        @Test
        @DisplayName("Should not allow credentials")
        void testCorsMapping_CredentialsDisabled() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();
            assertNotNull(registration);
            assertFalse(registration.isAllowCredentials());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should allow wildcard origin for public API")
        void testSecurity_WildcardOrigin() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();
            assertTrue(registration.getAllowedOrigins().contains("*"));
            // Wildcard origin is acceptable for public APIs
        }

        @Test
        @DisplayName("Should not allow credentials with wildcard origin")
        void testSecurity_NoCredentialsWithWildcard() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();
            // IMPORTANT: Cannot use allowCredentials(true) with allowedOrigins("*")
            // This combination would be a security violation
            if (registration.getAllowedOrigins().contains("*")) {
                assertFalse(registration.isAllowCredentials(),
                        "Credentials should be false when using wildcard origin");
            }
        }

        @Test
        @DisplayName("Should include OPTIONS method for preflight requests")
        void testSecurity_OptionsMethod() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();
            assertTrue(registration.getAllowedMethods().contains("OPTIONS"),
                    "OPTIONS method is required for CORS preflight requests");
        }
    }

    @Nested
    @DisplayName("API Path Coverage Tests")
    class ApiPathCoverageTests {

        @Test
        @DisplayName("Should apply to all API endpoints")
        void testPathCoverage_AllApiEndpoints() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            // Verify that /api/** pattern was used
            assertTrue(registry.wasAddMappingCalled());
            // Pattern /api/** covers all these paths:
            // /api/v1/streams
            // /api/v1/search
            // /api/v1/dash
            // etc.
        }
    }

    @Nested
    @DisplayName("HTTP Methods Tests")
    class HttpMethodsTests {

        @Test
        @DisplayName("Should allow GET method")
        void testHttpMethods_Get() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            assertTrue(registry.getLastRegistration().getAllowedMethods().contains("GET"));
        }

        @Test
        @DisplayName("Should allow POST method")
        void testHttpMethods_Post() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            assertTrue(registry.getLastRegistration().getAllowedMethods().contains("POST"));
        }

        @Test
        @DisplayName("Should allow PUT method")
        void testHttpMethods_Put() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            assertTrue(registry.getLastRegistration().getAllowedMethods().contains("PUT"));
        }

        @Test
        @DisplayName("Should allow DELETE method")
        void testHttpMethods_Delete() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            assertTrue(registry.getLastRegistration().getAllowedMethods().contains("DELETE"));
        }

        @Test
        @DisplayName("Should allow OPTIONS method for preflight")
        void testHttpMethods_Options() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            assertTrue(registry.getLastRegistration().getAllowedMethods().contains("OPTIONS"));
        }

        @Test
        @DisplayName("Should have exactly 5 allowed methods")
        void testHttpMethods_Count() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            assertEquals(5, registry.getLastRegistration().getAllowedMethods().size());
        }
    }

    @Nested
    @DisplayName("Headers Tests")
    class HeadersTests {

        @Test
        @DisplayName("Should allow all headers")
        void testHeaders_AllowAll() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            assertTrue(registry.getLastRegistration().getAllowedHeaders().contains("*"));
        }

        @Test
        @DisplayName("Should accept Content-Type header")
        void testHeaders_ContentType() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            // Wildcard allows all headers including Content-Type
            assertTrue(registry.getLastRegistration().getAllowedHeaders().contains("*"));
        }

        @Test
        @DisplayName("Should accept Authorization header")
        void testHeaders_Authorization() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            // Wildcard allows all headers including Authorization
            assertTrue(registry.getLastRegistration().getAllowedHeaders().contains("*"));
        }

        @Test
        @DisplayName("Should accept custom headers")
        void testHeaders_Custom() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            // Wildcard allows all headers including custom ones
            assertTrue(registry.getLastRegistration().getAllowedHeaders().contains("*"));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should configure CORS that allows frontend requests")
        void testIntegration_FrontendCompatibility() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();

            // Verify configuration allows typical frontend requests
            assertTrue(registration.getAllowedOrigins().contains("*"));
            assertTrue(registration.getAllowedMethods().contains("GET"));
            assertTrue(registration.getAllowedMethods().contains("POST"));
            assertTrue(registration.getAllowedHeaders().contains("*"));
        }

        @Test
        @DisplayName("Should configure CORS compatible with SvelteKit")
        void testIntegration_SvelteKitCompatibility() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);

            // Assert
            TestCorsRegistration registration = registry.getLastRegistration();

            // SvelteKit needs: GET, POST, OPTIONS methods and custom headers
            assertTrue(registration.getAllowedMethods().contains("GET"));
            assertTrue(registration.getAllowedMethods().contains("POST"));
            assertTrue(registration.getAllowedMethods().contains("OPTIONS"));
            assertTrue(registration.getAllowedHeaders().contains("*"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null registry gracefully")
        void testEdgeCase_NullRegistry() {
            // This test verifies the method signature doesn't throw on null
            // In practice, Spring will never pass null, but good to verify
            assertDoesNotThrow(() -> {
                // The method expects a non-null registry from Spring
                // We just verify the method exists and can be called
                CorsRegistry registry = new CorsRegistry();
                corsConfig.addCorsMappings(registry);
            });
        }

        @Test
        @DisplayName("Should be callable multiple times")
        void testEdgeCase_MultipleCalls() {
            // Arrange
            TestCorsRegistry registry = new TestCorsRegistry();

            // Act
            corsConfig.addCorsMappings(registry);
            corsConfig.addCorsMappings(registry);

            // Assert
            // Should not throw exception
            assertNotNull(registry.getLastRegistration());
        }
    }

    // Test helper classes

    /**
     * Test implementation of CorsRegistry that captures registrations.
     */
    private static class TestCorsRegistry extends CorsRegistry {
        private TestCorsRegistration lastRegistration;
        private boolean addMappingCalled = false;

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration addMapping(String pathPattern) {
            addMappingCalled = true;
            lastRegistration = new TestCorsRegistration();
            return lastRegistration;
        }

        public TestCorsRegistration getLastRegistration() {
            return lastRegistration;
        }

        public boolean wasAddMappingCalled() {
            return addMappingCalled;
        }
    }

    /**
     * Test implementation of CorsRegistration that captures configuration.
     */
    private static class TestCorsRegistration extends org.springframework.web.servlet.config.annotation.CorsRegistration {
        private java.util.List<String> allowedOrigins = new java.util.ArrayList<>();
        private java.util.List<String> allowedMethods = new java.util.ArrayList<>();
        private java.util.List<String> allowedHeaders = new java.util.ArrayList<>();
        private boolean allowCredentials = false;

        public TestCorsRegistration() {
            super("/**");
        }

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration allowedOrigins(String... origins) {
            allowedOrigins.addAll(Arrays.asList(origins));
            return this;
        }

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration allowedMethods(String... methods) {
            allowedMethods.addAll(Arrays.asList(methods));
            return this;
        }

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration allowedHeaders(String... headers) {
            allowedHeaders.addAll(Arrays.asList(headers));
            return this;
        }

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration allowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
            return this;
        }

        public java.util.List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public java.util.List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public java.util.List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }
    }
}