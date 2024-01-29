package com.localdrive.localdrive.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.localdrive.localdrive.service.FileSystemService;
import com.localdrive.localdrive.storage.StorageService;

@Controller
public class FileUploadController {
    private final StorageService storageService;
    private final FileSystemService fileSystemService;

    public FileUploadController(StorageService storageService, FileSystemService fileSystemService) {
        this.storageService = storageService;
        this.fileSystemService = fileSystemService;
    }

    @GetMapping("/directory/{nameDir}")
    public ResponseEntity<String> createDirectory(@PathVariable String nameDir) throws IOException {
        this.fileSystemService.createDir(nameDir);

        return ResponseEntity.ok().body("creado");
    }

    @GetMapping("/read/{name}")
    public ResponseEntity<List<String>> readDirectory(@PathVariable String name) throws IOException {
        Stream<String> map = this.fileSystemService.readDir(name).map(path -> {
            String filePath = path.getFileName().toString();
            UriComponentsBuilder serveFileMethod = MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                    "serveFile", filePath);

            return serveFileMethod.build().toUri().toString();
        });

        return ResponseEntity.ok().body(map.collect(Collectors.toList()));
    }

    @PostMapping("/upload/{name}")
    public ResponseEntity<String> fileUploadToDir(@RequestParam("file") MultipartFile file, @PathVariable String name) {
        fileSystemService.store(file, name);
        String message = "You successfully uploaded" + file.getOriginalFilename() + "!";

        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/")
    public ResponseEntity<List<String>> listUploadedFiles() throws IOException {
        Stream<String> map = storageService.loadAll().map(path -> {
            String serveFilePath = path.getFileName().toString();
            UriComponentsBuilder serveFileMethod = MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                    "serveFile", serveFilePath);

            return serveFileMethod.build().toUri().toString();
        });

        return ResponseEntity.ok().body(map.collect(Collectors.toList()));
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        String filePath = "attachment; filename=\"" + file.getFilename() + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, filePath)
                .body(file);
    }

    @PostMapping("/")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        storageService.store(file);
        String message = "You successfully uploaded" + file.getOriginalFilename() + "!";
        redirectAttributes.addFlashAttribute("message", message);

        return ResponseEntity.ok().body(message);
    }
}
