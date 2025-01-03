package com.twentyfive.apaapilayer.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "MediaManagerController", url = "${twentyfive.media.url}")
public interface MediaManagerClientController {

    @PostMapping(value = "/uploadkkk/{path}", consumes = "multipart/form-data")
    String uploadMedia(@RequestPart("file") MultipartFile file,
                                             @PathVariable("path") String path);

    @DeleteMapping(value = "/deletekkk/{path}")
    String deleteMedia(@PathVariable("path") String path);

    @GetMapping(value = "/downloadkkk/{path}")
    ResponseEntity<byte[]> downloadMedia(@PathVariable("path") String path);
}

