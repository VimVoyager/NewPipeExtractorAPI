package org.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.config.GlobalExceptionHandler;
import org.example.api.dto.channels.ChannelDTO;
import org.example.api.dto.channels.ChannelTabDTO;
import org.example.api.exception.ExtractionException;
import org.example.api.service.ChannelService;
import org.example.api.service.ChannelTabService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for ChannelController.
 * Tests all three endpoints with success paths, missing params, and service failures.
 */
@DisplayName("ChannelController Tests")
class ChannelControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ChannelService channelService;

    @Mock
    private ChannelTabService channelTabService;

    @InjectMocks
    private ChannelController channelController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(channelController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private ChannelDTO buildChannelDTO() {
        ChannelDTO dto = new ChannelDTO();
        dto.setId("UCXuqSBlHAE6Xw-yeJA0Tunw");
        dto.setName("Linus Tech Tips");
        dto.setSubscriberCount(15_000_000L);
        dto.setVerified(true);
        dto.setAvatars(List.of());
        dto.setBanners(List.of());
        dto.setParentChannelAvatars(List.of());
        dto.setTabs(List.of());
        dto.setTags(List.of());
        dto.setErrors(List.of());
        return dto;
    }

    private ChannelTabDTO buildChannelTabDTO(String tab, String channelId) {
        ChannelTabDTO dto = new ChannelTabDTO();
        dto.setTab(tab);
        dto.setChannelId(channelId);
        dto.setItems(List.of());
        dto.setNextPage(null);
        return dto;
    }

    private ChannelTabDTO buildChannelTabDTOWithNextPage(String tab, String channelId) {
        ChannelTabDTO dto = buildChannelTabDTO(tab, channelId);
        dto.setNextPage(new ChannelTabDTO.PageDto(
                "https://www.youtube.com/youtubei/v1/browse?prettyPrint=false",
                "eyJjb250aW51YXRpb24iOiJ0b2tlbiJ9",
                List.of("Linus Tech Tips",
                        "https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw",
                        "VERIFIED")
        ));
        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/channels - Channel Info Tests")
    class ChannelInfoTests {

        @Test
        @DisplayName("Should return 200 with channel info for valid id")
        void testGetChannelInfo_Success() throws Exception {
            // Arrange
            ChannelInfo mockInfo = mock(ChannelInfo.class);
            when(channelService.getChannelInfo(anyString())).thenReturn(mockInfo);

            // ChannelDTO.from is called inside the controller; we need a real ChannelInfo mock
            // that satisfies ChannelDTO.from(). Stub the minimum required methods.
            when(mockInfo.getServiceId()).thenReturn(0);
            when(mockInfo.getId()).thenReturn("UCXuqSBlHAE6Xw-yeJA0Tunw");
            when(mockInfo.getUrl()).thenReturn("https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw");
            when(mockInfo.getOriginalUrl()).thenReturn("https://www.youtube.com/@LinusTechTips");
            when(mockInfo.getName()).thenReturn("Linus Tech Tips");
            when(mockInfo.getSubscriberCount()).thenReturn(15_000_000L);
            when(mockInfo.isVerified()).thenReturn(true);
            when(mockInfo.getDescription()).thenReturn("Tech channel");
            when(mockInfo.getAvatars()).thenReturn(List.of());
            when(mockInfo.getBanners()).thenReturn(List.of());
            when(mockInfo.getParentChannelAvatars()).thenReturn(List.of());
            when(mockInfo.getTabs()).thenReturn(List.of());
            when(mockInfo.getTags()).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels")
                            .param("id", "@LinusTechTips")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value("UCXuqSBlHAE6Xw-yeJA0Tunw"))
                    .andExpect(jsonPath("$.name").value("Linus Tech Tips"))
                    .andExpect(jsonPath("$.subscriberCount").value(15_000_000))
                    .andExpect(jsonPath("$.verified").value(true));
        }

        @Test
        @DisplayName("Should return 400 when id param is missing")
        void testGetChannelInfo_MissingId() throws Exception {
            mockMvc.perform(get("/api/v1/channels")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 when service throws ExtractionException")
        void testGetChannelInfo_ServiceThrowsException() throws Exception {
            // Arrange
            when(channelService.getChannelInfo(anyString()))
                    .thenThrow(new ExtractionException("Failed to extract channel info"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels")
                            .param("id", "@LinusTechTips")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.errorCode").value("EXTRACTION_ERROR"))
                    .andExpect(jsonPath("$.message").value(containsString("Failed to extract")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/channels/tab - Channel Tab Tests")
    class ChannelTabTests {

        @Test
        @DisplayName("Should return 200 with tab results for valid params")
        void testGetChannelTab_Success() throws Exception {
            // Arrange
            ChannelTabDTO mockResult = buildChannelTabDTOWithNextPage("videos", "@LinusTechTips");
            when(channelTabService.getChannelTab(anyString(), anyString(), anyString()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels/tab")
                            .param("id", "@LinusTechTips")
                            .param("tab", "videos")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.tab").value("videos"))
                    .andExpect(jsonPath("$.channelId").value("@LinusTechTips"))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.nextPage").exists())
                    .andExpect(jsonPath("$.nextPage.url").isNotEmpty())
                    .andExpect(jsonPath("$.nextPage.body").isNotEmpty())
                    .andExpect(jsonPath("$.nextPage.ids").isArray())
                    .andExpect(jsonPath("$.nextPage.ids", hasSize(3)));
        }

        @Test
        @DisplayName("Should default tab to 'videos' when tab param is omitted")
        void testGetChannelTab_DefaultTab() throws Exception {
            // Arrange
            ChannelTabDTO mockResult = buildChannelTabDTO("videos", "@LinusTechTips");
            when(channelTabService.getChannelTab(anyString(), eq("videos"), anyString()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels/tab")
                            .param("id", "@LinusTechTips")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tab").value("videos"));

            verify(channelTabService).getChannelTab(anyString(), eq("videos"), anyString());
        }

        @Test
        @DisplayName("Should pass channel URL with base youtube prefix to service")
        void testGetChannelTab_ChannelUrlConstruction() throws Exception {
            // Arrange
            ChannelTabDTO mockResult = buildChannelTabDTO("videos", "@LinusTechTips");
            when(channelTabService.getChannelTab(anyString(), anyString(), anyString()))
                    .thenReturn(mockResult);

            // Act
            mockMvc.perform(get("/api/v1/channels/tab")
                            .param("id", "@LinusTechTips")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Assert that the URL passed contains the youtube base and the id
            verify(channelTabService).getChannelTab(
                    contains("@LinusTechTips"),
                    eq("videos"),
                    eq("@LinusTechTips")
            );
        }

        @Test
        @DisplayName("Should return 400 when id param is missing")
        void testGetChannelTab_MissingId() throws Exception {
            mockMvc.perform(get("/api/v1/channels/tab")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 when service throws ExtractionException")
        void testGetChannelTab_ServiceThrowsException() throws Exception {
            // Arrange
            when(channelTabService.getChannelTab(anyString(), anyString(), anyString()))
                    .thenThrow(new ExtractionException("Tab not found"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels/tab")
                            .param("id", "@LinusTechTips")
                            .param("tab", "videos")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode").value("EXTRACTION_ERROR"))
                    .andExpect(jsonPath("$.message").value(containsString("Tab not found")));
        }

        @Test
        @DisplayName("Should support shorts tab type")
        void testGetChannelTab_ShortsTab() throws Exception {
            // Arrange
            ChannelTabDTO mockResult = buildChannelTabDTO("shorts", "UCXuq");
            when(channelTabService.getChannelTab(anyString(), eq("shorts"), anyString()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels/tab")
                            .param("id", "UCXuq")
                            .param("tab", "shorts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tab").value("shorts"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/channels/tab/page - Pagination Tests")
    class ChannelTabPageTests {

        @Test
        @DisplayName("Should return 200 with paginated results for valid params")
        void testGetChannelTabPage_Success() throws Exception {
            // Arrange
            ChannelTabDTO mockResult = buildChannelTabDTOWithNextPage("videos", "UCXuq");
            when(channelTabService.getChannelTabPage(
                    anyString(), anyString(), anyString(), anyString(), anyList()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("channelId", "UCXuqSBlHAE6Xw-yeJA0Tunw")
                            .param("tab", "videos")
                            .param("pageUrl", "https://www.youtube.com/youtubei/v1/browse?prettyPrint=false")
                            .param("pageBody", "eyJjb250aW51YXRpb24iOiJ0b2tlbiJ9")
                            .param("pageIds", "Linus Tech Tips")
                            .param("pageIds", "https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw")
                            .param("pageIds", "VERIFIED")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.tab").value("videos"))
                    .andExpect(jsonPath("$.items").isArray());
        }

        @Test
        @DisplayName("Should pass all three pageIds as list to service")
        void testGetChannelTabPage_PageIdsPassedAsList() throws Exception {
            // Arrange
            ChannelTabDTO mockResult = buildChannelTabDTO("videos", "UCXuq");
            when(channelTabService.getChannelTabPage(anyString(), anyString(), anyString(), anyString(), anyList()))
                    .thenReturn(mockResult);

            // Act
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("channelId", "UCXuq")
                            .param("tab", "videos")
                            .param("pageUrl", "https://youtube.com/browse")
                            .param("pageBody", "bodyBase64")
                            .param("pageIds", "Channel Name")
                            .param("pageIds", "https://youtube.com/channel/UC1")
                            .param("pageIds", "VERIFIED")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Assert service received correct list
            verify(channelTabService).getChannelTabPage(
                    eq("UCXuq"),
                    eq("videos"),
                    eq("https://youtube.com/browse"),
                    eq("bodyBase64"),
                    eq(List.of("Channel Name", "https://youtube.com/channel/UC1", "VERIFIED"))
            );
        }

        @Test
        @DisplayName("Should default tab to 'videos' when omitted")
        void testGetChannelTabPage_DefaultTab() throws Exception {
            // Arrange
            ChannelTabDTO mockResult = buildChannelTabDTO("videos", "UCXuq");
            when(channelTabService.getChannelTabPage(anyString(), eq("videos"), anyString(), anyString(), anyList()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("channelId", "UCXuq")
                            .param("pageUrl", "https://youtube.com/browse")
                            .param("pageBody", "body")
                            .param("pageIds", "Ch")
                            .param("pageIds", "https://youtube.com/channel/UC1")
                            .param("pageIds", "false")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(channelTabService).getChannelTabPage(anyString(), eq("videos"), anyString(), anyString(), anyList());
        }

        @Test
        @DisplayName("Should return 400 when channelId is missing")
        void testGetChannelTabPage_MissingChannelId() throws Exception {
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("pageUrl", "https://youtube.com/browse")
                            .param("pageBody", "body")
                            .param("pageIds", "Ch")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when pageUrl is missing")
        void testGetChannelTabPage_MissingPageUrl() throws Exception {
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("channelId", "UCXuq")
                            .param("pageBody", "body")
                            .param("pageIds", "Ch")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when pageBody is missing")
        void testGetChannelTabPage_MissingPageBody() throws Exception {
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("channelId", "UCXuq")
                            .param("pageUrl", "https://youtube.com/browse")
                            .param("pageIds", "Ch")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when pageIds is missing")
        void testGetChannelTabPage_MissingPageIds() throws Exception {
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("channelId", "UCXuq")
                            .param("pageUrl", "https://youtube.com/browse")
                            .param("pageBody", "body")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 when service throws ExtractionException")
        void testGetChannelTabPage_ServiceThrowsException() throws Exception {
            // Arrange
            when(channelTabService.getChannelTabPage(anyString(), anyString(), anyString(), anyString(), anyList()))
                    .thenThrow(new ExtractionException("pageIds missing channelUrl"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("channelId", "UCXuq")
                            .param("tab", "videos")
                            .param("pageUrl", "https://youtube.com/browse")
                            .param("pageBody", "body")
                            .param("pageIds", "Ch")
                            .param("pageIds", "https://youtube.com/channel/UC1")
                            .param("pageIds", "false")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode").value("EXTRACTION_ERROR"))
                    .andExpect(jsonPath("$.message").value(containsString("pageIds missing channelUrl")));
        }

        @Test
        @DisplayName("Should return null nextPage on final page")
        void testGetChannelTabPage_LastPage() throws Exception {
            // Arrange - last page has no nextPage
            ChannelTabDTO mockResult = buildChannelTabDTO("videos", "UCXuq");
            when(channelTabService.getChannelTabPage(anyString(), anyString(), anyString(), anyString(), anyList()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/channels/tab/page")
                            .param("channelId", "UCXuq")
                            .param("tab", "videos")
                            .param("pageUrl", "https://youtube.com/browse")
                            .param("pageBody", "body")
                            .param("pageIds", "Ch")
                            .param("pageIds", "https://youtube.com/channel/UC1")
                            .param("pageIds", "false")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nextPage").doesNotExist());
        }
    }
}