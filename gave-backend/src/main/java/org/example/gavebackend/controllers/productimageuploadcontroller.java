package org.example.gavebackend.controllers;

import jakarta.validation.constraints.NotNull;
import org.example.gavebackend.dtos.ProductImageDTO;
import org.example.gavebackend.entities.product;
import org.example.gavebackend.repositories.productRepository;
import org.example.gavebackend.services.impl.CloudinaryService;
import org.example.gavebackend.services.serviceproducts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class productimageuploadcontroller {
    @Autowired private productRepository productRepo;
    @Autowired private serviceproducts service;
    @Autowired private CloudinaryService cloudinaryService;

    @PostMapping(value = "/{productId}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductImageDTO upload(
            @PathVariable Long productId,
            @RequestPart("file") @NotNull MultipartFile file,
            @RequestParam(value="altText", required = false) String altText,
            @RequestParam(value="sortOrder", required = false, defaultValue = "0") Integer sortOrder
    ) throws IOException {

        productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        Map res = cloudinaryService.upload(file);
        String secureUrl = (String) res.get("secure_url");
        String publicId  = (String) res.get("public_id");

        var dto = new ProductImageDTO();
        dto.setProductId(productId);
        dto.setUrl(secureUrl);
        dto.setAltText(altText);
        dto.setSortOrder(sortOrder);
        dto.setCloudPublicId(publicId);

        return service.addImage(dto);
    }

    @PostMapping(value = "/{productId}/images/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ProductImageDTO> uploadMultiple(
            @PathVariable Long productId,
            @RequestPart("files") MultipartFile[] files,
            @RequestParam(value="altText", required=false) String altText,
            @RequestParam(value="sortStart", required=false, defaultValue="0") Integer sortStart
    ) throws IOException {

        productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        List<ProductImageDTO> results = new ArrayList<>();
        int sort = sortStart;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) continue;

            Map res = cloudinaryService.upload(file);
            String secureUrl = (String) res.get("secure_url");
            String publicId  = (String) res.get("public_id");

            var dto = new ProductImageDTO();
            dto.setProductId(productId);
            dto.setUrl(secureUrl);
            dto.setAltText(altText);
            dto.setSortOrder(sort++);
            dto.setCloudPublicId(publicId);

            results.add(service.addImage(dto));
        }
        return results;
    }
}