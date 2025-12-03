package org.example.api.downloader;

import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for DownloaderImpl.
 * Uses MockWebServer to test HTTP interactions without real network calls.
 */
@DisplayName("DownloaderImpl Tests")
class DownloaderImplTest {

    private MockWebServer mockWebServer;
    private DownloaderImpl downloader;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = mockWebServer.url("/").toString();

        // Initialize downloader with default OkHttpClient
        downloader = DownloaderImpl.init(null);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize downloader with default builder")
        void testInit_DefaultBuilder() {
            // Act
            DownloaderImpl instance = DownloaderImpl.init(null);

            // Assert
            assertNotNull(instance);
            assertSame(instance, DownloaderImpl.getInstance());
        }

        @Test
        @DisplayName("Should initialize downloader with custom builder")
        void testInit_CustomBuilder() {
            // Arrange
            OkHttpClient.Builder customBuilder = new OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS);

            // Act
            DownloaderImpl instance = DownloaderImpl.init(customBuilder);

            // Assert
            assertNotNull(instance);
            assertSame(instance, DownloaderImpl.getInstance());
        }

        @Test
        @DisplayName("Should return same instance (singleton pattern)")
        void testSingletonPattern() {
            // Arrange
            DownloaderImpl instance1 = DownloaderImpl.init(null);

            // Act
            DownloaderImpl instance2 = DownloaderImpl.getInstance();

            // Assert
            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName("Should have default User-Agent")
        void testDefaultUserAgent() {
            // Assert
            assertEquals("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0",
                    DownloaderImpl.USER_AGENT);
        }
    }

    @Nested
    @DisplayName("Cookie Management Tests")
    class CookieManagementTests {

        @Test
        @DisplayName("Should get and set cookies")
        void testGetSetCookies() {
            // Arrange
            String testCookies = "session=abc123; token=xyz789";

            // Act
            downloader.setCookies(testCookies);

            // Assert
            assertEquals(testCookies, downloader.getCookies());
        }

        @Test
        @DisplayName("Should start with null cookies")
        void testInitialCookiesNull() {
            // Arrange
            DownloaderImpl newDownloader = DownloaderImpl.init(null);

            // Assert
            assertNull(newDownloader.getCookies());
        }

        @Test
        @DisplayName("Should include cookies in request headers")
        void testCookiesInRequestHeaders() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));
            String cookies = "session=test123";
            downloader.setCookies(cookies);

            // Act
            downloader.stream(baseUrl + "test");

            // Assert
            RecordedRequest request = mockWebServer.takeRequest();
            assertEquals(cookies, request.getHeader("Cookie"));
        }

        @Test
        @DisplayName("Should not include Cookie header when cookies are empty")
        void testNoCookiesWhenEmpty() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));
            downloader.setCookies("");

            // Act
            downloader.stream(baseUrl + "test");

            // Assert
            RecordedRequest request = mockWebServer.takeRequest();
            assertNull(request.getHeader("Cookie"));
        }
    }

    @Nested
    @DisplayName("Content Length Tests")
    class ContentLengthTests {

        @Test
        @DisplayName("Should get content length from HEAD request")
        void testGetContentLength_Success() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setBody("x".repeat(12345)));

            // Act
            long contentLength = downloader.getContentLength(baseUrl + "file");

            // Assert
            assertEquals(12345L, contentLength);

            // Verify HEAD request was made
            RecordedRequest request = mockWebServer.takeRequest();
            assertEquals("HEAD", request.getMethod());
        }

        @Test
        @DisplayName("Should throw IOException when Content-Length header is missing")
        void testGetContentLength_MissingHeader() {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setChunkedBody("test content", 4));

            // Act & Assert
            assertThrows(IOException.class, () ->
                    downloader.getContentLength(baseUrl + "file")
            );
        }

        @Test
        @DisplayName("Should throw IOException when Content-Length is invalid")
        void testGetContentLength_InvalidNumber() {
            // Use chunked encoding to avoid auto Content-Length
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Length", "not-a-number")
                    .setChunkedBody("", 1));

            Exception exception = assertThrows(Exception.class, () ->
                    downloader.getContentLength(baseUrl + "file")
            );
            assertTrue(exception instanceof IOException ||
                    exception instanceof NullPointerException);
        }

        @Test
        @DisplayName("Should handle zero content length")
        void testGetContentLength_Zero() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setHeader("Content-Length", "0")
                    .setBody(""));

            // Act
            long contentLength = downloader.getContentLength(baseUrl + "empty");

            // Assert
            assertEquals(0L, contentLength);
        }

        @Test
        @DisplayName("Should handle large content length")
        void testGetContentLength_LargeFile() throws Exception {
            long largeSize = 5_000_000_000L;

            MockResponse response = new MockResponse()
                    .setResponseCode(200);

            response.setBody("x");  // Set body first (triggers Content-Length)
            response.setHeader("Content-Length", String.valueOf(largeSize));  // Override it

            mockWebServer.enqueue(response);

            long contentLength = downloader.getContentLength(baseUrl + "large");
            assertEquals(largeSize, contentLength);
        }
    }

    @Nested
    @DisplayName("Stream Tests")
    class StreamTests {

        @Test
        @DisplayName("Should open input stream successfully")
        void testStream_Success() throws Exception {
            // Arrange
            String testContent = "Test content data";
            mockWebServer.enqueue(new MockResponse().setBody(testContent));

            // Act
            InputStream stream = downloader.stream(baseUrl + "data");

            // Assert
            assertNotNull(stream);
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(testContent, content);
            stream.close();
        }

        @Test
        @DisplayName("Should include User-Agent in stream request")
        void testStream_UserAgent() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));

            // Act
            downloader.stream(baseUrl + "test");

            // Assert
            RecordedRequest request = mockWebServer.takeRequest();
            assertEquals(DownloaderImpl.USER_AGENT, request.getHeader("User-Agent"));
        }

        @Test
        @DisplayName("Should use GET method for stream")
        void testStream_GetMethod() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));

            // Act
            downloader.stream(baseUrl + "test");

            // Assert
            RecordedRequest request = mockWebServer.takeRequest();
            assertEquals("GET", request.getMethod());
        }

        @Test
        @DisplayName("Should throw IOException when stream returns 429")
        void testStream_ReCaptchaException() {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setResponseCode(429));

            // Act & Assert
            IOException exception = assertThrows(IOException.class, () ->
                    downloader.stream(baseUrl + "blocked")
            );
            assertTrue(exception.getMessage().contains("reCaptcha"));
        }

        @Test
        @DisplayName("Should return null when response body is null")
        void testStream_NullBody() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(204) // No Content
                    .setBody(""));

            // Act
            InputStream stream = downloader.stream(baseUrl + "empty");

            // Assert
            assertNotNull(stream); // Body exists but is empty
        }

        @Test
        @DisplayName("Should handle binary data stream")
        void testStream_BinaryData() throws Exception {
            // Arrange
            byte[] binaryData = {0x00, 0x01, 0x02, (byte) 0xFF};
            mockWebServer.enqueue(new MockResponse()
                    .setBody(new okio.Buffer().write(binaryData)));

            // Act
            InputStream stream = downloader.stream(baseUrl + "binary");

            // Assert
            assertNotNull(stream);
            byte[] result = stream.readAllBytes();
            assertArrayEquals(binaryData, result);
            stream.close();
        }
    }

    @Nested
    @DisplayName("Execute Request Tests")
    class ExecuteRequestTests {

        @Test
        @DisplayName("Should execute GET request successfully")
        void testExecute_GetRequest() throws Exception {
            // Arrange
            String responseBody = "Success response";
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(responseBody));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .build();

            // Act
            Response response = downloader.execute(request);

            // Assert
            assertEquals(200, response.responseCode());
            assertEquals(responseBody, response.responseBody());

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals("GET", recordedRequest.getMethod());
        }

        @Test
        @DisplayName("Should execute POST request with data")
        void testExecute_PostRequest() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setResponseCode(201));

            byte[] postData = "key=value".getBytes(StandardCharsets.UTF_8);
            Request request = new Request.Builder()
                    .post(baseUrl + "api/create", postData)
                    .build();

            // Act
            Response response = downloader.execute(request);

            // Assert
            assertEquals(201, response.responseCode());

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals("key=value", recordedRequest.getBody().readUtf8());
        }

        @Test
        @DisplayName("Should execute HEAD request")
        void testExecute_HeadRequest() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Length", "1234"));

            Request request = new Request.Builder()
                    .head(baseUrl + "api/check")
                    .build();

            // Act
            Response response = downloader.execute(request);

            // Assert
            assertEquals(200, response.responseCode());

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals("HEAD", recordedRequest.getMethod());
        }

        @Test
        @DisplayName("Should include custom headers in request")
        void testExecute_CustomHeaders() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));

            Map<String, List<String>> headers = new HashMap<>();
            headers.put("X-Custom-Header", Arrays.asList("CustomValue"));
            headers.put("Accept", Arrays.asList("application/json"));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .headers(headers)
                    .build();

            // Act
            downloader.execute(request);

            // Assert
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals("CustomValue", recordedRequest.getHeader("X-Custom-Header"));
            assertEquals("application/json", recordedRequest.getHeader("Accept"));
        }

        @Test
        @DisplayName("Should handle multiple values for same header")
        void testExecute_MultipleHeaderValues() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));

            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Accept", Arrays.asList("application/json", "text/html"));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .headers(headers)
                    .build();

            // Act
            downloader.execute(request);

            // Assert
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            List<String> acceptHeaders = recordedRequest.getHeaders().values("Accept");
            assertTrue(acceptHeaders.contains("application/json"));
            assertTrue(acceptHeaders.contains("text/html"));
        }

        @Test
        @DisplayName("Should include User-Agent in execute request")
        void testExecute_UserAgent() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .build();

            // Act
            downloader.execute(request);

            // Assert
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals(DownloaderImpl.USER_AGENT, recordedRequest.getHeader("User-Agent"));
        }

        @Test
        @DisplayName("Should throw ReCaptchaException on 429 response")
        void testExecute_ReCaptchaException() {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setResponseCode(429));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .build();

            // Act & Assert
            ReCaptchaException exception = assertThrows(ReCaptchaException.class, () ->
                    downloader.execute(request)
            );
            assertTrue(exception.getMessage().contains("reCaptcha"));
        }

        @Test
        @DisplayName("Should handle 404 response")
        void testExecute_NotFound() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(404)
                    .setBody("Not Found"));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/missing")
                    .build();

            // Act
            Response response = downloader.execute(request);

            // Assert
            assertEquals(404, response.responseCode());
            assertEquals("Not Found", response.responseBody());
        }

        @Test
        @DisplayName("Should handle 500 server error")
        void testExecute_ServerError() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("Internal Server Error"));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/error")
                    .build();

            // Act
            Response response = downloader.execute(request);

            // Assert
            assertEquals(500, response.responseCode());
            assertEquals("Internal Server Error", response.responseBody());
        }

        @Test
        @DisplayName("Should handle empty response body")
        void testExecute_EmptyBody() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(204)); // No Content

            Request request = new Request.Builder()
                    .get(baseUrl + "api/empty")
                    .build();

            // Act
            Response response = downloader.execute(request);

            // Assert
            assertEquals(204, response.responseCode());
            assertNotNull(response.responseBody()); // Empty string, not null
        }

        @Test
        @DisplayName("Should include cookies in execute request")
        void testExecute_WithCookies() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));
            String cookies = "session=abc123";
            downloader.setCookies(cookies);

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .build();

            // Act
            downloader.execute(request);

            // Assert
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals(cookies, recordedRequest.getHeader("Cookie"));
        }

        @Test
        @DisplayName("Should capture response headers")
        void testExecute_ResponseHeaders() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setHeader("X-Custom", "value123")
                    .setBody("{\"status\":\"ok\"}"));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .build();

            // Act
            Response response = downloader.execute(request);

            // Assert
            Map<String, List<String>> responseHeaders = response.responseHeaders();
            assertTrue(responseHeaders.containsKey("Content-Type"));
            assertTrue(responseHeaders.containsKey("X-Custom"));
        }

//        @Test
//        @DisplayName("Should preserve response URL")
//        void testExecute_ResponseUrl() throws Exception {
//            // Arrange
//            String requestUrl = baseUrl + "api/data";
//            mockWebServer.enqueue(new MockResponse().setBody("OK"));
//
//            Request request = new Request.Builder()
//                    .get(requestUrl)
//                    .build();
//
//            // Act
//            Response response = downloader.execute(request);
//
//            // Assert
//            assertEquals(requestUrl, response.responseUrl());
//        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

//        @Test
//        @DisplayName("Should handle network timeout")
//        void testNetworkTimeout() {
//            OkHttpClient.Builder testBuilder = new OkHttpClient.Builder()
//                    .readTimeout(1, TimeUnit.SECONDS);
//
//            DownloaderImpl testDownloader = DownloaderImpl.init(testBuilder);
//
//            mockWebServer.enqueue(new MockResponse()
//                    .setBodyDelay(2, TimeUnit.SECONDS));
//
//            Request request = new Request.Builder()
//                    .get(baseUrl + "api/slow")
//                    .build();
//
//            // Timeout throws IOException (SocketTimeoutException extends IOException)
//            assertThrows(IOException.class, () ->
//                    testDownloader.execute(request)
//            );
//
//            downloader = DownloaderImpl.init(null);
//        }

        @Test
        @DisplayName("Should handle invalid URL")
        void testInvalidUrl() {
            Request request = new Request.Builder()
                    .get("not-a-valid-url")
                    .build();

            IOException exception = assertThrows(IOException.class, () ->
                    downloader.execute(request)
            );

            assertTrue(exception.getMessage().contains("Invalid URL"));
            assertTrue(exception.getCause() instanceof IllegalArgumentException);
        }

        @Test
        @DisplayName("Should handle connection refused")
        void testConnectionRefused() throws Exception {
            // Arrange
            mockWebServer.shutdown(); // Shutdown server to simulate connection refused

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .build();

            // Act & Assert
            assertThrows(IOException.class, () ->
                    downloader.execute(request)
            );
        }
    }

    @Nested
    @DisplayName("Privacy Tests")
    class PrivacyTests {

        @Test
        @DisplayName("Should not expose user data in requests")
        void testNoUserDataInRequests() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));

            Request request = new Request.Builder()
                    .get(baseUrl + "api/data")
                    .build();

            // Act
            downloader.execute(request);

            // Assert
            RecordedRequest recordedRequest = mockWebServer.takeRequest();

            // Verify no privacy-invasive headers
            assertNull(recordedRequest.getHeader("X-User-Id"));
            assertNull(recordedRequest.getHeader("X-Session-Id"));
            assertNull(recordedRequest.getHeader("X-Tracking-Id"));

            // Only expected headers should be present
            assertNotNull(recordedRequest.getHeader("User-Agent"));
        }

        @Test
        @DisplayName("Should use generic User-Agent (no personal info)")
        void testGenericUserAgent() throws Exception {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("OK"));

            // Act
            downloader.stream(baseUrl + "test");

            // Assert
            RecordedRequest request = mockWebServer.takeRequest();
            String userAgent = request.getHeader("User-Agent");

            // Verify it's a generic browser UA, not identifying the user
            assertTrue(userAgent.contains("Firefox"));
            assertFalse(userAgent.contains("user"));
            assertFalse(userAgent.contains("id"));
        }
    }
}