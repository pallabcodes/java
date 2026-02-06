package com.backend.designpatterns.realworld.cloud_storage;

public class CloudStorageDemo {

    public static void main(String[] args) {
        
        CloudStorageService service = new CloudStorageService();

        // Upload to AWS
        service.upload("image.png", "aws");

        System.out.println();

        // Upload to Google Cloud
        service.upload("video.mp4", "gcp");
    }
}
