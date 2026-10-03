package com.multi.shop.media.images;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

@Component
public class ImageCleanup {
    private static final Logger log = LoggerFactory.getLogger(ImageCleanup.class);

    private final ImageRepository repository;
    private final ImageService service;
    private final Clock clock;
    private final Duration pendingAge;

    public ImageCleanup(
        ImageRepository repository,
        ImageService service,
        Clock clock,
        @Value("${media.cleanup.pending-age}") Duration pendingAge
    ) {
        this.repository = repository;
        this.service = service;
        this.clock = clock;
        this.pendingAge = pendingAge;
    }

    @Scheduled(cron = "${media.cleanup.cron}")
    public void cleanUp() {
        List<Image> abandoned = repository.findByStatusAndCreatedAtBefore(ImageStatus.PENDING, clock.instant().minus(pendingAge));
        List<Image> unfinished = repository.findByStatus(ImageStatus.DELETING);

        abandoned.forEach(service::delete);
        unfinished.forEach(service::delete);

        if (!abandoned.isEmpty() || !unfinished.isEmpty())
            log.info("Cleanup: {} abandoned and {} unfinished images processed", abandoned.size(), unfinished.size());
    }
}
