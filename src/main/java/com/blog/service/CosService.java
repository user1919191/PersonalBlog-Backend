package main.java.com.blog.service;

import com.blog.config.CosProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class CosService {

    private final COSClient cosClient;
    private final CosProperties cosProperties;

    public CosService(COSClient cosClient, CosProperties cosProperties) {
        this.cosClient = cosClient;
        this.cosProperties = cosProperties;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf(".")) : "";
        String key = "images/" + UUID.randomUUID().toString().replace("-", "") + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        PutObjectRequest putRequest = new PutObjectRequest(
                cosProperties.getBucketName(), key, file.getInputStream(), metadata);
        cosClient.putObject(putRequest);

        return cosProperties.getUrlPrefix() + "/" + key;
    }
}
