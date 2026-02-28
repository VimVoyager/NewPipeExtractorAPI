package org.example.api.service;

import org.example.api.dto.ChannelTabDTO;
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
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for extracting paginated content from YouTube channel tabs via
 * NewPipe's {@link ChannelTabInfo} API.
 *
 * <p>Supported tab types (use the string constants from {@link ChannelTabs}):
 * <ul>
 *   <li>{@code "videos"}      — regular uploads</li>
 *   <li>{@code "shorts"}      — YouTube Shorts</li>
 *   <li>{@code "livestreams"} — past and upcoming live streams</li>
 *   <li>{@code "playlists"}   — channel playlists (items are PlaylistInfoItems)</li>
 * </ul>
 * </p>
 */
@Service
public class ChannelTabService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelTabService.class);

    /**
     * Fetches the first page of items from a given channel tab.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Resolve the channel URL → {@link ChannelInfo} to get its {@code tabs} list.</li>
     *   <li>Find the {@link ListLinkHandler} whose {@code contentFilters} match the tab type.</li>
     *   <li>Get a {@link ChannelTabExtractor} from the service, call {@code fetchPage()},
     *       then pass it to {@link ChannelTabInfo#getInfo(ChannelTabExtractor)}.</li>
     * </ol>
     * </p>
     *
     * @param channelUrl full YouTube channel URL (e.g. {@code https://www.youtube.com/@LinusTechTips})
     * @param tab        tab type string — use {@link ChannelTabs} constants
     *                   ({@code "videos"}, {@code "shorts"}, {@code "livestreams"}, {@code "playlists"})
     * @param channelId  channel ID used for the response DTO (e.g. {@code @LinusTechTips})
     * @return paginated {@link ChannelTabDTO}
     * @throws ExtractionException if extraction or tab lookup fails
     */
    public ChannelTabDTO getChannelTab(String channelUrl, String tab, String channelId) throws ExtractionException {
        try {
            logger.info("Fetching channel tab '{}' for URL: {}", tab, channelUrl);

            // Get channel info to access its tab link handlers
            StreamingService service = NewPipe.getServiceByUrl(channelUrl);
            ChannelInfo channelInfo = ChannelInfo.getInfo(channelUrl);

            // Find the matching tab handler
            ListLinkHandler tabHandler = findTabHandler(channelInfo.getTabs(), tab)
                    .orElseThrow(() -> new ExtractionException(
                            "Tab '%s' not found for channel '%s'. Available tabs: %s"
                                    .formatted(tab, channelUrl, describeAvailableTabs(channelInfo.getTabs()))
                    ));

            // Extract the tab's initial page
            ChannelTabExtractor extractor = service.getChannelTabExtractor(tabHandler);
            extractor.fetchPage();
            ChannelTabInfo tabInfo = ChannelTabInfo.getInfo(extractor);

            logger.info("Fetched {} items from tab '{}' (hasNextPage={})",
                    tabInfo.getRelatedItems().size(), tab, tabInfo.hasNextPage());

            return ChannelTabDTO.from(tabInfo, tab, channelId);

        } catch (ExtractionException e) {
            throw e; // re-throw as-is so controller can handle it cleanly
        } catch (Exception e) {
            logger.error("Failed to fetch channel tab '{}' for URL: {}", tab, channelUrl, e);
            throw new ExtractionException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Fetches a subsequent page of channel tab items using a pagination cursor
     * returned by a previous call.
     *
     * <p>Unlike the initial fetch, this does <em>not</em> perform any network calls to
     * reconstruct context. It mirrors what the NewPipe Android app does in
     * {@code ChannelTabFragment}: the {@code tabHandler} is reconstructed from the
     * service's {@link ListLinkHandlerFactory} using only the channel ID and tab type,
     * then passed directly to {@link ChannelTabInfo#getMoreItems(StreamingService, ListLinkHandler, Page)}.
     * No {@code ChannelInfo} or {@code ChannelTabInfo} pre-fetch is needed.</p>
     *
     * @param channelUrl  full YouTube channel URL (used only to resolve the service)
     * @param channelId   the raw channel ID (e.g. {@code UCXuqSBlHAE6Xw-yeJA0Tunw}) — must be
     *                    the stable UC… ID, not a handle like {@code @LinusTechTips}, since the
     *                    factory uses it to build the tab URL without a network call
     * @param tab         tab type string (must match the tab from the initial request)
     * @param nextPageUrl the {@code nextPage.nextPage} cursor from a previous response
     * @return next page of items as a {@link ChannelTabDTO}
     * @throws ExtractionException if extraction fails
     */
    public ChannelTabDTO getChannelTabPage(String channelUrl, String channelId, String tab, String nextPageUrl)
            throws ExtractionException {
        try {
            logger.info("Fetching next page for channel tab '{}', channelId: {}", tab, channelId);

            StreamingService service = NewPipe.getServiceByUrl(channelUrl);

            // Reconstruct the tab handler from the factory — no network call required.
            // This is exactly what NewPipe Android does: it stores the tabHandler in
            // fragment state and passes it directly to getMoreChannelTabItems().
            ListLinkHandlerFactory tabLHFactory = service.getChannelTabLHFactory();
            if (tabLHFactory == null) {
                throw new ExtractionException("Service does not support channel tab link handler factory");
            }

            ListLinkHandler tabHandler = tabLHFactory.fromQuery(
                    channelId,          // channel ID (e.g. UCXuqSBlHAE6Xw-yeJA0Tunw)
                    List.of(tab),       // contentFilters — the tab type
                    ""                  // sortFilter — empty for YouTube
            );

            Page pageInstance = new Page(nextPageUrl);
            InfoItemsPage<InfoItem> page = ChannelTabInfo.getMoreItems(service, tabHandler, pageInstance);

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

    /**
     * Finds the first tab handler whose {@code contentFilters} list contains
     * the requested tab string (case-insensitive).
     */
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