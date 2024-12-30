package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.clients.MediaManagerClientController;
import com.twentyfive.apaapilayer.dtos.DownloadMedia;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MediaManagerService {

    @Value("${layer.url}")
    private String mediaUrl;

    private final MediaManagerClientController mediaManagerClientController;

    public DownloadMedia downloadMedia(HttpServletRequest request) {
        try {
            String path = getPath(request);
            DownloadMedia downloadMedia = new DownloadMedia();
            ResponseEntity<byte[]> response = mediaManagerClientController.downloadMedia(path);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                downloadMedia.setBytes(response.getBody());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Immagine non trovata: " + path);
            }
            downloadMedia.setMediaType(response.getHeaders().getContentType());
            return downloadMedia;
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nel servizio remoto: " + e.contentUTF8());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore inatteso durante il download: " + e.getMessage());
        }
    }

    public String getPath(HttpServletRequest request) {
        String fullPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        String basePath = contextPath + "/download/";
        return fullPath.substring(basePath.length()); // Estrai il path relativo
    }
}
