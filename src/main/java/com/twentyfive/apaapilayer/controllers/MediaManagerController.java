package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.DownloadMedia;
import com.twentyfive.apaapilayer.services.MediaManagerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
@CrossOrigin("*")
public class MediaManagerController {

    public final MediaManagerService mediaManagerService;

    @GetMapping("/download/{*path}")
    public ResponseEntity<byte[]> downloadMedia(@PathVariable("path") String path) {

        byte[] imageBytes = mediaManagerService.downloadMedia(path);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + path + "\"")
                .contentType(determineContentType(path))
                .body(imageBytes);
    }

    // Determina il tipo di file in base all'estensione
    private MediaType determineContentType(String path) {
        if (path.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (path.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (path.endsWith(".svg")) {
            return MediaType.valueOf("image/svg+xml");  // Tipo MIME per SVG
        } else{
            return MediaType.APPLICATION_OCTET_STREAM;  // Default se il tipo non è noto
        }
    }
}
