package com.example.smartcity.metier.service;

import com.example.smartcity.dao.PhotoRepository;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.entity.Photo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class PhotoService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final PhotoRepository photoRepository;

    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public void savePhotos(MultipartFile[] files, Incident incident) throws IOException {

        Path incidentDir = Paths.get(uploadDir + "/incident_" + incident.getId());
        Files.createDirectories(incidentDir);

        for (MultipartFile file : files) {

            String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = incidentDir.resolve(uniqueName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Photo photo = new Photo();
            photo.setNomFichier(uniqueName);
            photo.setChemin(filePath.toString());
            photo.setType(file.getContentType());
            photo.setTaille(file.getSize());
            photo.setIncident(incident);

            photoRepository.save(photo);
        }
    }
}
