package org.example.gavebackend.controllers;

import jakarta.validation.constraints.NotNull;
import org.example.gavebackend.dtos.ProductImageDTO;
import org.example.gavebackend.entities.product;
import org.example.gavebackend.repositories.productRepository;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class productimageuploadcontroller {
    @Value("${app.upload.dir}")
    private String uploadDir;
    @Autowired
    private productRepository productRepo;
    @Autowired
    private serviceproducts service;



    @PostMapping(value = "/{productId}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductImageDTO upload(
            @PathVariable Long productId,
            @RequestPart("file") @NotNull MultipartFile file,
            @RequestParam(value="altText", required = false) String altText,
            @RequestParam(value="sortOrder", required = false, defaultValue = "0") Integer sortOrder
    ) throws IOException {

        product p = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        // crear carpeta si no existe
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        // nombre único conservando extensión
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext != null ? ("." + ext) : "");
        Path target = dir.resolve(filename);

        // guardar archivo
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // URL pública que servimos con /files/**
        String publicUrl = "/files/" + filename;

        // crear registro en DB usando tu service existente
        var dto = new ProductImageDTO();
        dto.setProductId(productId);
        dto.setUrl(publicUrl);
        dto.setAltText(altText);
        dto.setSortOrder(sortOrder);

        return service.addImage(dto);
    }

    @PostMapping(value = "/{productId}/images/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ProductImageDTO> uploadMultiple(
            @PathVariable Long productId,
            @RequestPart("files") MultipartFile[] files,
            @RequestParam(value="altText", required=false) String altText,         // opcional (mismo para todas)
            @RequestParam(value="sortStart", required=false, defaultValue="0") Integer sortStart
    ) throws IOException {

        var prod = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        List<ProductImageDTO> results = new ArrayList<>();
        int sort = sortStart;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            // validación rápida de content-type
            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                continue; // o lanzar excepción si preferís
            }
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + (ext != null ? ("." + ext) : "");
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String publicUrl = "/files/" + filename;

            var dto = new ProductImageDTO();
            dto.setProductId(productId);
            dto.setUrl(publicUrl);
            dto.setAltText(altText);
            dto.setSortOrder(sort++);

            results.add(service.addImage(dto));
        }

        return results;
    }
}