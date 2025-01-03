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

    public byte[] downloadMedia(String path) {
        try {
            ResponseEntity<byte[]> response = mediaManagerClientController.downloadMedia(path.substring(1));
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Immagine non trovata: " + path);
            }
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
        String basePath = contextPath + "/twentyfiveserver/downloadkkk/";
        return fullPath.substring(basePath.length()); // Estrai il path relativo
    }
}
