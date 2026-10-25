package com.core.G_O.service;

import com.core.G_O.Exception.AtencIAException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
public class EvidenceStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(EvidenceStorageService.class);

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "application/pdf", "pdf"
    );

    private final Path root;

    public EvidenceStorageService(@Value("${app.storage.evidence-dir:./uploads/evidence}") String evidenceDir) {
        this.root = Paths.get(evidenceDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(root);
            LOG.info("@EvidenceStorageService > Evidence directory ready at {}", root);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo crear el directorio de evidencias: " + root, exception);
        }
    }

    public StoredEvidence save(MultipartFile file, Long businessId) {
        if (file == null || file.isEmpty()) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST, "La evidencia del pago es obligatoria");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST, "La evidencia supera el tamaño máximo de 10 MB");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        String extension = ALLOWED_TYPES.get(contentType);

        if (extension == null) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST,
                    "Formato de evidencia no permitido. Usa JPG, PNG, WEBP o PDF");
        }

        try {
            Path businessDirectory = root.resolve(String.valueOf(businessId)).normalize();

            if (!businessDirectory.startsWith(root)) {
                throw new AtencIAException(HttpStatus.BAD_REQUEST, "Ruta de evidencia inválida");
            }

            Files.createDirectories(businessDirectory);

            String fileName = UUID.randomUUID() + "." + extension;
            Path destination = businessDirectory.resolve(fileName).normalize();

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = businessId + "/" + fileName;

            LOG.info("@EvidenceStorageService > Evidence saved at {}", destination);

            return new StoredEvidence(relativePath, contentType, file.getSize());
        } catch (IOException exception) {
            throw new AtencIAException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la evidencia del pago");
        }
    }

    public Resource load(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new AtencIAException(HttpStatus.NOT_FOUND, "La evidencia no existe");
        }

        Path filePath = root.resolve(relativePath).normalize();

        if (!filePath.startsWith(root) || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new AtencIAException(HttpStatus.NOT_FOUND, "La evidencia no existe");
        }

        return new FileSystemResource(filePath);
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
            LOG.warn("@EvidenceStorageService > Could not delete evidence {}", relativePath, exception);
        }
    }

    public record StoredEvidence(String relativePath, String contentType, long size) {
    }
}
