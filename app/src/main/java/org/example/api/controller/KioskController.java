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
    private static final int YOUTUBE_SERVICE_ID = 0;
    private final KioskService kioskService;


    @Autowired
    public KioskController(KioskService kioskService) {
        this.kioskService = kioskService;
    }

    /**
     * Returns the kiosk IDs available for a service,
     */
    @GetMapping
    public ResponseEntity<List<String>> getKioskIds() throws ExtractionException {
        logger.info("Retrieving kiosk IDs");

        List<String> kioskIds = kioskService.getKioskIds(YOUTUBE_SERVICE_ID);
        return ResponseEntity.ok(kioskIds);
    }

    /**
     * Returns kiosk metadata and the initial page of streams.
     */
    @GetMapping("/{kioskId}")
    public ResponseEntity<KioskDTO> getKioskInfo(
            @PathVariable(name = "kioskId") String kioskId
    ) throws ExtractionException {
        logger.info("Retrieving kiosk info for kioskId: {}", kioskId);

        KioskInfo info = kioskService.getKioskInfo(YOUTUBE_SERVICE_ID, kioskId);
        return ResponseEntity.ok(KioskDTO.from(info));
    }

    /**
     * Returns a subsequent page of kiosk items.
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
//            @RequestParam(name = "pageUrl") String pageUrl,
//            @RequestParam(name = "pageId", required = false) String pageId,
//            @RequestParam(name = "pageIds", required = false) List<String> pageIds,
//            @RequestParam(name = "pageBody", required = false) String pageBody
//    ) throws ExtractionException {
//        logger.info("Retrieving kiosk page for kioskId: {}", kioskId);
//
//        Page page = KioskDTO.NextPageDTO.toPage(pageUrl, pageId, pageIds, pageBody);
//        InfoItemsPage<StreamInfoItem> result = kioskService.getKioskPage(YOUTUBE_SERVICE_ID, kioskId, page);
//
//        return ResponseEntity.ok(KioskPageDTO.from(result));
//    }
}