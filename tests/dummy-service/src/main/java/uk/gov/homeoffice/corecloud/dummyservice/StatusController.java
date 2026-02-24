package uk.gov.homeoffice.corecloud.dummyservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * CCL-6306 – Minimal REST controller.
 *
 * Provides a /api/status endpoint used to confirm the built image is
 * functional during pipeline validation.
 */
@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(
            "status",  "UP",
            "service", "dummy-service",
            "team",    "core-cloud-platform",
            "ticket",  "CCL-6306"
        ));
    }
}
