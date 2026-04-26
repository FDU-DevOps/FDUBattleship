package org.fdu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the application version via HTTP.
 */
@RestController
class VersionController {

    /**
     * The application version, injected from the {@code project-version} property.
     */
    @Value("${project-version}")
    private String version;

    /**
     * Returns the current application version.
     *
     * @return the version string defined in {@code project-version}
     */
    @GetMapping("/api/version")
    public String getVersion() {
        return version;
    }
}
