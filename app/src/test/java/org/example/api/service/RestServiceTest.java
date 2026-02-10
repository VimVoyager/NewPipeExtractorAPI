package org.example.api.service;

import org.example.api.exception.ExtractionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.schabi.newpipe.extractor.comments.CommentsInfo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@DisplayName("RestService Tests")
public class RestServiceTest {

    private RestService restService;
    private static final String TEST_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    @BeforeEach
    void setUp() {
        restService = new RestService();
    }

//    @DisplayName("Comments Info Tests")

    @Test
    @DisplayName("Should return comments info successfully")
    void testGetCommentsInfo_Success() throws Exception {
        CommentsInfo mockCommentsInfo = createMockCommentsInfo();

        try (MockedStatic<CommentsInfo> commentsInfoMock = mockStatic(CommentsInfo.class)) {
            commentsInfoMock.when(() -> CommentsInfo.getInfo(TEST_URL))
                    .thenReturn(mockCommentsInfo);

            CommentsInfo result = restService.getCommentsInfo(TEST_URL);

            assertNotNull(result);
            commentsInfoMock.verify(() -> CommentsInfo.getInfo(TEST_URL));
        }
    }

    @Test
    @DisplayName("Should throw ExtractionException when extraction fails")
    void testGetCommentsInfo_ThrowsException() {
        try (MockedStatic<CommentsInfo> commentsInfoMock = mockStatic(CommentsInfo.class)) {
            commentsInfoMock.when(() -> CommentsInfo.getInfo(TEST_URL))
                    .thenThrow(new RuntimeException("Network error"));

            ExtractionException exception = assertThrows(
                    ExtractionException.class,
                    () -> restService.getCommentsInfo(TEST_URL)
            );

            assertTrue(exception.getMessage().contains("Failed to retrieve comments information"));
            assertNotNull(exception.getCause());
        }
    }

    //Helper method to create mock objects
    private CommentsInfo createMockCommentsInfo() {
        return mock(CommentsInfo.class);
    }
}
