package org.example.api.service;

import org.example.api.dto.channels.ChannelTabDTO;
import org.example.api.exception.ExtractionException;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service for extracting paginated content from YouTube channel tabs via
 * NewPipe's {@link ChannelTabInfo} API.
 *
 * <p>Supported tab types (use the string constants from {@link ChannelTabs}):</p>
 * <ul>
 *   <li>{@code "videos"}      — regular uploads</li>
 *   <li>{@code "shorts"}      — YouTube Shorts</li>
 *   <li>{@code "livestreams"} — past and upcoming live streams</li>
 *   <li>{@code "playlists"}   — channel playlists</li>
 * </ul>
 */
@Service
public class ChannelTabService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelTabService.class);

    /**
     * Fetches the first page of items from a given channel tab.
     */
    public ChannelTabDTO getChannelTab(String channelUrl, String tab, String channelId)
            throws ExtractionException {
        try {
            logger.info("Fetching channel tab '{}' for URL: {}", tab, channelUrl);

            StreamingService service = NewPipe.getServiceByUrl(channelUrl);
            ChannelInfo channelInfo = ChannelInfo.getInfo(channelUrl);

            ListLinkHandler tabHandler = findTabHandler(channelInfo.getTabs(), tab)
                    .orElseThrow(() -> new ExtractionException(
                            "Tab '%s' not found for channel '%s'. Available tabs: %s"
                                    .formatted(tab, channelUrl, describeAvailableTabs(channelInfo.getTabs()))
                    ));

            ChannelTabExtractor extractor = service.getChannelTabExtractor(tabHandler);
            extractor.fetchPage();
            ChannelTabInfo tabInfo = ChannelTabInfo.getInfo(extractor);

            logger.info("Fetched {} items from tab '{}' (hasNextPage={})",
                    tabInfo.getRelatedItems().size(), tab, tabInfo.hasNextPage());

            return ChannelTabDTO.from(tabInfo, tab, channelId);

        } catch (ExtractionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch channel tab '{}' for URL: {}", tab, channelUrl, e);
            throw new ExtractionException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Fetches a subsequent page of channel tab items.
     *
     * <p><b>Root cause of the previous NPE:</b> {@code YoutubeChannelTabExtractor.getPage()}
     * reads two things from the {@link Page} object it receives:</p>
     * <ol>
     *   <li>{@code page.getBody()} — a JSON POST body containing the continuation token.
     *       Without it, the InnerTube browse request has no continuation and YouTube
     *       returns page 1 again.</li>
     *   <li>{@code page.getIds()} — a {@code List<String>} of
     *       {@code ["channelName", "channelUrl", "verifiedStatus"]} used to annotate
     *       returned items with uploader metadata. Without it, the local variable
     *       {@code channelIds} is null, causing the NPE in {@code collectItemsFrom}.</li>
     * </ol>
     *
     * @param channelId   channel ID — for the response DTO
     * @param tab         tab type string matching the initial request
     * @param pageUrl     from {@code nextPage.url} in the previous response
     * @param pageBody    from {@code nextPage.body} in the previous response (Base64)
     * @param pageIds     from {@code nextPage.ids} in the previous response
     */
    public ChannelTabDTO getChannelTabPage(String channelId, String tab,
                                           String pageUrl, String pageBody, List<String> pageIds)
            throws ExtractionException {
        try {
            logger.info("Fetching next page for channel tab '{}', channelId: {}", tab, channelId);

            // Reconstruct the full Page with url + body + ids — all three are required.
            byte[] bodyBytes = pageBody != null ? Base64.getDecoder().decode(pageBody) : null;
            Page pageInstance = new Page(pageUrl, null, pageIds, null, bodyBytes);

            String channelUrl = pageIds != null && pageIds.size() >= 2
                    ? pageIds.get(1)
                    : null;

            if (channelUrl == null) {
                throw new ExtractionException(
                        "Cannot reconstruct extractor: pageIds missing channelUrl (index 1). " +
                                "Ensure nextPage.ids is passed correctly from the previous response.");
            }

            StreamingService service = NewPipe.getServiceByUrl(channelUrl);
            ChannelInfo channelInfo = ChannelInfo.getInfo(channelUrl);
            ListLinkHandler tabHandler = findTabHandler(channelInfo.getTabs(), tab)
                    .orElseThrow(() -> new ExtractionException(
                            "Tab '%s' not found for channel '%s'".formatted(tab, channelUrl)
                    ));

            ChannelTabExtractor extractor = service.getChannelTabExtractor(tabHandler);
            extractor.fetchPage();

            InfoItemsPage<InfoItem> page = extractor.getPage(pageInstance);

            logger.info("Fetched {} items from tab '{}' page (hasNextPage={})",
                    page.getItems().size(), tab, page.hasNextPage());

            return ChannelTabDTO.fromPage(page, tab, channelId);

        } catch (ExtractionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch channel tab page for tab '{}', channelId: {}", tab, channelId, e);
            throw new ExtractionException(e.getMessage(), e.getCause());
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private Optional<ListLinkHandler> findTabHandler(List<ListLinkHandler> tabs, String tab) {
        return tabs.stream()
                .filter(t -> t.getContentFilters().stream()
                        .anyMatch(f -> f.equalsIgnoreCase(tab)))
                .findFirst();
    }

    private String describeAvailableTabs(List<ListLinkHandler> tabs) {
        return tabs.stream()
                .flatMap(t -> t.getContentFilters().stream())
                .toList()
                .toString();
    }
}