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
    public ResponseEntity<byte[]> getMedia(@PathVariable("path") String path) {

        DownloadMedia downloadMedia = mediaManagerService.downloadMedia(path);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + path + "\"")
                .contentType(downloadMedia.getMediaType())
                .body(downloadMedia.getBytes());
    }
}
