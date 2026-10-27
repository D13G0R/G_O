package com.core.G_O.service;

import com.core.G_O.Exception.AtencIAException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class TechnicianPhotoStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnicianPhotoStorageService.class);

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path root;

    public TechnicianPhotoStorageService(
            @Value("${app.storage.technician-photo-dir:./uploads/technician-photo}") String photoDir) {
        this.root = Paths.get(photoDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(root);
            LOG.info("@TechnicianPhotoStorageService > Technician photo directory ready at {}", root);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo crear el directorio de fotos de técnicos: " + root, exception);
        }
    }

    public String save(MultipartFile file, Long businessId) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST, "La foto supera el tamaño máximo de 5 MB");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        String extension = ALLOWED_TYPES.get(contentType);

        if (extension == null) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST,
                    "Formato de foto no permitido. Usa JPG, PNG o WEBP");
        }

        try {
            Path businessDirectory = root.resolve(String.valueOf(businessId)).normalize();

            if (!businessDirectory.startsWith(root)) {
                throw new AtencIAException(HttpStatus.BAD_REQUEST, "Ruta de foto inválida");
            }

            Files.createDirectories(businessDirectory);

            String fileName = UUID.randomUUID() + "." + extension;
            Path destination = businessDirectory.resolve(fileName).normalize();

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = businessId + "/" + fileName;

            LOG.info("@TechnicianPhotoStorageService > Technician photo saved at {}", destination);

            return relativePath;
        } catch (IOException exception) {
            throw new AtencIAException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la foto del técnico");
        }
    }

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        try {
            Path filePath = root.resolve(relativePath).normalize();

            if (filePath.startsWith(root)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException exception) {
            LOG.warn("@TechnicianPhotoStorageService > Could not delete technician photo {}", relativePath,
                    exception);
        }
    }
}
