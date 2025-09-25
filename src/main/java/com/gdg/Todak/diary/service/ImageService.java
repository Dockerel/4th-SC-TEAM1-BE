package com.gdg.Todak.diary.service;


import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.diary.dto.UrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.gdg.Todak.common.exception.errors.DiaryError.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",   // JPG 이미지
            "image/png",    // PNG 이미지
            "image/gif",    // GIF 이미지
            "image/bmp",    // BMP 이미지
            "image/webp",   // WEBP 이미지
            "image/svg+xml" // SVG 이미지
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    @Value("${file.path}")
    private String uploadFolder;
    @Value("${image.url}")
    private String imageUrl;

    public UrlResponse uploadImage(MultipartFile file, String storageUUID, String userName) {
        if (file.isEmpty()) throw new TodakException(EMPTY_IMAGE_ERROR);
        if (file.getSize() > MAX_FILE_SIZE) throw new TodakException(TOO_BIG_IMAGE_ERROR);
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType()))
            throw new TodakException(INVALID_IMAGE_FORMAT_ERROR);

        String subDirectory = userName + "/" + storageUUID;

        try {
            Path directoryPath = Paths.get(uploadFolder + subDirectory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
        } catch (IOException e) {
            throw new TodakException(UPLOAD_FAILED_ERROR);
        }
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File destinationFile = new File(uploadFolder + subDirectory + "/" + fileName);

        try {
            file.transferTo(destinationFile);
            return new UrlResponse(imageUrl + subDirectory + "/" + fileName);
        } catch (IOException e) {
            throw new TodakException(UPLOAD_FAILED_ERROR);
        }
    }

    public void deleteImage(String url, String userName) {
        // url example: /backend/images/testUser/1234/Frame.png
        String[] parts = url.split("/");
        if (parts.length != 6) throw new TodakException(INVALID_URL_FORMAT_ERROR);
        String storageUUID = parts[4];
        String filename = parts[5];

        File targetFile = new File(uploadFolder + userName + "/" + storageUUID + "/" + filename);
        if (!targetFile.delete()) throw new TodakException(DELETE_FAILED_ERROR);
    }

    public void deleteAllImagesInStorageUUID(String userName, String storageUUID) {
        Path directoryPath = Paths.get(uploadFolder + userName + "/" + storageUUID);

        if (!Files.exists(directoryPath)) {
            return;
        }

        File directory = directoryPath.toFile();
        File[] images = directory.listFiles();
        if (images != null) {
            for (File image : images) {
                if (!image.delete()) {
                    throw new TodakException(DELETE_FAILED_ERROR);
                }
            }
        }
        if (!directory.delete()) throw new TodakException(DELETE_FAILED_ERROR);
    }
}
