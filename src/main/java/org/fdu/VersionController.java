package org.fdu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


//https://www.logicbig.com/tutorials/spring-framework/spring-boot/maven-resource-filtering.html

@RestController
@RequestMapping("/")
class VersionController {
    @Value("${project-version}")
    private String version;
    @GetMapping("/version")
    public String getVersion() {
        return version;
    }
}
