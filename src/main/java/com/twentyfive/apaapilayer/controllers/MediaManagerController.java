package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.DownloadMedia;
import com.twentyfive.apaapilayer.services.MediaManagerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
@CrossOrigin("*")
public class MediaManagerController {

    public final MediaManagerService mediaManagerService;

    @GetMapping("/download/**")
    public ResponseEntity<byte[]> getMedia(HttpServletRequest request) {

        DownloadMedia downloadMedia = mediaManagerService.downloadMedia(request);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + mediaManagerService.getPath(request) + "\"")
                .contentType(downloadMedia.getMediaType())
                .body(downloadMedia.getBytes());
    }
}
