package org.example.gavebackend.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    @Value("${cloudinary.folder:gave/products}")
    private String folder;

    /**
     * Sube un archivo a Cloudinary
     * @param file Archivo a subir
     * @return Mapa con los datos de la subida
     * @throws IOException Si ocurre un error al subir el archivo
     */
    public Map upload(MultipartFile file) throws IOException {
        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image",
                        "overwrite", true
                )
        );
    }
    /**
     * Elimina un archivo de Cloudinary
     * @param publicId ID público del archivo a eliminar
     * @return Mapa con los datos de la eliminación
     * @throws IOException Si ocurre un error al eliminar el archivo
     */
    public Map delete(String publicId) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
