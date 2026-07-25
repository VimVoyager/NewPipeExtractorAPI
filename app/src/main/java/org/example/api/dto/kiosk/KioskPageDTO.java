package org.example.api.dto.kiosk;

import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

/**
 * A subsequent page of kiosk items, returned by {@code GET /api/v1/kiosks/{kioskId}/page}.
 */
public record KioskPageDTO(
        List<KioskDTO.KioskStreamItemDTO> items,
        KioskDTO.NextPageDTO nextPage
) {
    public static KioskPageDTO from(InfoItemsPage<StreamInfoItem> page) {
        return new KioskPageDTO(
                page.getItems().stream().map(KioskDTO.KioskStreamItemDTO::from).toList(),
                KioskDTO.NextPageDTO.from(page.getNextPage())
        );
    }
}