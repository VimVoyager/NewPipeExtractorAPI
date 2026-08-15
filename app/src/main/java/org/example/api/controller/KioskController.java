package org.example.api.controller;

import org.example.api.dto.kiosk.KioskDTO;
import org.example.api.service.KioskService;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
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
    public ResponseEntity<List<String>> getKioskIds() {

        List<String> kioskIds = kioskService.getKioskIds(YOUTUBE_SERVICE_ID);
        return ResponseEntity.ok(kioskIds);
    }

    /**
     * Returns kiosk metadata and the initial page of streams.
     */
    @GetMapping("/{kioskId}")
    public ResponseEntity<KioskDTO> getKioskInfo(
            @PathVariable(name = "kioskId") String kioskId
    ) {
        logger.debug("Retrieving kiosk info for kioskId: {}", kioskId);

        KioskInfo info = kioskService.getKioskInfo(YOUTUBE_SERVICE_ID, kioskId);
        return ResponseEntity.ok(KioskDTO.from(info));
    }
}