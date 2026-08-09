package org.example.api.service;

import org.example.api.exception.ExtractionException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KioskService {
    private static final Logger logger = LoggerFactory.getLogger(KioskService.class);

    public List<String> getKioskIds(int serviceId) throws ExtractionException {
        try {
            logger.info("Extracting kiosk IDs for serviceId: {}", serviceId);
            return List.copyOf(NewPipe.getService(serviceId)
                    .getKioskList()
                    .getAvailableKiosks());
        } catch (Exception e) {
            logger.error("Failed to extract kiosk IDs for serviceId: {}", serviceId, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    public KioskInfo getKioskInfo(int serviceId, String kioskId) throws ExtractionException {
        try {
            logger.info("Extracting kiosk info for kioskId: {}, serviceId: {}", kioskId, serviceId);

            StreamingService service = NewPipe.getService(serviceId);

            KioskExtractor<?> extractor = service.getKioskList().getExtractorById(kioskId, null);
            extractor.fetchPage();

            return KioskInfo.getInfo(extractor);
        } catch (Exception e) {
            logger.error("Failed to extract kiosk info for kioskId: {}, serviceId: {}",
                    kioskId, serviceId, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }
}