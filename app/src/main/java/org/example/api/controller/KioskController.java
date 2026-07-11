package org.example.api.controller;

import org.example.api.dto.kiosk.KioskDTO;
import org.example.api.dto.kiosk.KioskPageDTO;
import org.example.api.exception.ExtractionException;
import org.example.api.service.KioskService;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kiosks")
public class KioskController {
    private static final Logger logger = LoggerFactory.getLogger(KioskController.class);

    private final KioskService kioskService;

    @Autowired
    public KioskController(KioskService kioskService) {
        this.kioskService = kioskService;
    }

    /**
     * Returns the kiosk IDs available for a service,
     * e.g. ["trending_gaming", "trending_music", ..., "live"].
     */
    @GetMapping
    public ResponseEntity<List<String>> getKioskIds(
            @RequestParam(name = "serviceId") int serviceId
    ) throws ExtractionException {
        logger.info("Retrieving kiosk IDs for serviceId: {}", serviceId);

        List<String> kioskIds = kioskService.getKioskIds(serviceId);
        return ResponseEntity.ok(kioskIds);
    }

    /**
     * Returns kiosk metadata and the initial page of streams.
     */
    @GetMapping("/{kioskId}")
    public ResponseEntity<KioskDTO> getKioskInfo(
            @PathVariable(name = "kioskId") String kioskId,
            @RequestParam(name = "serviceId") int serviceId
    ) throws ExtractionException {
        logger.info("Retrieving kiosk info for kioskId: {}, serviceId: {}", kioskId, serviceId);

        KioskInfo info = kioskService.getKioskInfo(serviceId, kioskId);
        return ResponseEntity.ok(KioskDTO.from(info));
    }

    /**
     * Returns a subsequent page of kiosk items.
     *
     * <p>All {@code nextPage.*} parameters must be taken verbatim from the
     * {@code nextPage} object in the previous response — same convention as
     * the channel tab paging endpoint.</p>
     *
     * @param kioskId   kiosk ID — must match the initial request
     * @param serviceId service ID — must match the initial request
     * @param pageUrl   from {@code nextPage.url}
     * @param pageId    from {@code nextPage.id} (optional)
     * @param pageIds   from {@code nextPage.ids} (optional)
     * @param pageBody  from {@code nextPage.body} (optional, Base64-encoded continuation token)
     */
//    @GetMapping("/{kioskId}/page")
//    public ResponseEntity<KioskPageDTO> getKioskPage(
//            @PathVariable(name = "kioskId") String kioskId,
//            @RequestParam(name = "serviceId") int serviceId,
//            @RequestParam(name = "pageUrl") String pageUrl,
//            @RequestParam(name = "pageId", required = false) String pageId,
//            @RequestParam(name = "pageIds", required = false) List<String> pageIds,
//            @RequestParam(name = "pageBody", required = false) String pageBody
//    ) throws ExtractionException {
//        logger.info("Retrieving kiosk page for kioskId: {}, serviceId: {}", kioskId, serviceId);
//
//        Page page = KioskDTO.NextPageDTO.toPage(pageUrl, pageId, pageIds, pageBody);
//        InfoItemsPage<StreamInfoItem> result = kioskService.getKioskPage(serviceId, kioskId, page);
//
//        return ResponseEntity.ok(KioskPageDTO.from(result));
//    }
}