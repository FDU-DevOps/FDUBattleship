package org.fdu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the application version via HTTP.
 */
@RestController
@PropertySource("classpath:version.properties")
class VersionController {

    /**
     * The application version, injected from the {@code project-version} property.
     */
    @Value("${project-version:unknown}")
    private String version;

    /**
     * Returns the current application version.
     *
     * @return the version string defined in {@code project-version}
     */
    @GetMapping("/api/version")
    public String getVersion() {
        return version; //literally impossible to be null, so I can just return the value as it will default to unknown.
    }
}
