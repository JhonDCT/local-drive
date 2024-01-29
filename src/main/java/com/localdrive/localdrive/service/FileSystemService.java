package com.localdrive.localdrive.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.localdrive.localdrive.storage.StorageException;
import com.localdrive.localdrive.storage.StorageProperties;

@Service
public class FileSystemService {
    private final Path rootLocation;

    public FileSystemService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    public void createDir(String nameDir) throws IOException {
        try {
            Path destinationDir = this.rootLocation
                    .resolve(Paths.get(nameDir))
                    .normalize()
                    .toAbsolutePath();

            if (!destinationDir.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new IOException("Cannot create a directory.");
            }

            Files.createDirectory(destinationDir);
        } catch (IOException e) {
            throw new IOException("Failed to create directory");
        }
    }

    public Stream<Path> readDir(String nameDir) throws IOException {
        try {
            String[] dirs = nameDir.split("-");
            String finalPath = String.join("/", dirs);

            Path destinationDir = this.rootLocation
                    .resolve(Paths.get(finalPath))
                    .normalize()
                    .toAbsolutePath();

            return Files.list(destinationDir);
        } catch (IOException e) {
            throw new IOException("Failed to read directory");
        }
    }

    public void store(MultipartFile file, String nameDir) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation
                    .resolve(Paths.get(nameDir))
                    .normalize()
                    .toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }
}
