package com.core.G_O.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String technicianPhotoLocation;

    public StaticResourceConfig(
            @Value("${app.storage.technician-photo-dir:./uploads/technician-photo}") String technicianPhotoDir) {
        Path root = Paths.get(technicianPhotoDir).toAbsolutePath().normalize();
        String location = root.toUri().toString();

        this.technicianPhotoLocation = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/technician-photo/**")
                .addResourceLocations(technicianPhotoLocation);
    }
}
