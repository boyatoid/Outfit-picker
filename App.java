package com.vision.maven.maven_vision_jar;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.type.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
// Credit: Andrew Boyer

public class App {
    /**
     * Create ImageAnnotatorClient using Service Account JSON
     * 
     * @param serviceAccountPath Path to the service account JSON file
     * @return Configured ImageAnnotatorClient
     * @throws IOException If there's an error reading the service account file
     */
    private static ImageAnnotatorClient createClientWithServiceAccount(String serviceAccountPath) throws IOException {
        // Loads json creds
        ServiceAccountCredentials credentials; // gonna have to have "String serviceAccountPath" var in main 
        try (FileInputStream serviceAccountStream = new FileInputStream(serviceAccountPath)) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
        }

        // Makes client with json creds
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build();
        return ImageAnnotatorClient.create(settings);
    }

    /**
     * Detect the color of a shirt in the given image
     * 
     * @param filePath Path to the image file
     * @param serviceAccountPath Path to the service account JSON file
     * @return String representing the dominant shirt color, or null if not found
     * @throws IOException If there's an error reading the image or service account
     */
    public static String detectShirtColor(String filePath, String serviceAccountPath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        // Pic Prep
        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath)); // and filePath var in main for pic
        Image img = Image.newBuilder().setContent(imgBytes).build();
        
        // Request creation
        Feature labelFeat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        Feature colorFeat = Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).build();
        
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
            .addFeatures(labelFeat)
            .addFeatures(colorFeat)
            .setImage(img)
            .build();
        requests.add(request);

        // Creates the Image Annotator client with the json file
        try (ImageAnnotatorClient client = createClientWithServiceAccount(serviceAccountPath)) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.err.format("Error: %s%n", res.getError().getMessage());
                    return null;
                }

                // Filters for shirt related labels
                List<String> shirtLabels = res.getLabelAnnotationsList().stream()
                    .map(EntityAnnotation::getDescription)
                    .filter(label -> label.toLowerCase().contains("shirt") || 
                                     label.toLowerCase().contains("t-shirt"))
                    .collect(Collectors.toList());

                // If a shirt is found, finds color
                if (!shirtLabels.isEmpty()) {
                    // Gets most dominate color, might rework
                    return res.getImagePropertiesAnnotation()
                        .getDominantColors()
                        .getColorsList()
                        .stream()
                        .findFirst()
                        .map(App::convertColorToString)
                        .orElse(null);
                }
            }
        }
        return null;
    }

    /**
     * Detect the type of clothing in the image
     * 
     * @param filePath Path to the image file
     * @param serviceAccountPath Path to the service account JSON file
     * @return String representing the clothing type (e.g., "t-shirt", "jeans", "shorts")
     * @throws IOException If there's an error reading the image or service account
     */
    public static String detectClothingType(String filePath, String serviceAccountPath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        // Pic prep part 2
        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));
        Image img = Image.newBuilder().setContent(imgBytes).build();
        
        // Request creation for labels
        Feature labelFeat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
            .addFeatures(labelFeat)
            .setImage(img)
            .build();
        requests.add(request);

        try (ImageAnnotatorClient client = createClientWithServiceAccount(serviceAccountPath)) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.err.format("Error: %s%n", res.getError().getMessage());
                    return null;
                }

                // clothing types, work needed
                String[] clothingTypes = {"t-shirt", "shirt", "jeans", "shorts", "pants", "dress"};

                // find type
                for (String type : clothingTypes) {
                    boolean typeFound = res.getLabelAnnotationsList().stream()
                        .anyMatch(label -> label.getDescription().toLowerCase().contains(type));
                    
                    if (typeFound) {
                        return type;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Comprehensive clothing analysis method
     * 
     * @param filePath Path to the image file
     * @param serviceAccountPath Path to the service account JSON file
     * @return ClothingAnalysis object containing detected clothing details
     * @throws IOException If there's an error reading the image or service account
     */
    public static ClothingAnalysis analyzeClothing(String filePath, String serviceAccountPath) throws IOException {
        String color = detectShirtColor(filePath, serviceAccountPath);
        String type = detectClothingType(filePath, serviceAccountPath);
        
        return new ClothingAnalysis(color, type);
    }

    /**
     * Convert Color to a readable string representation
     * 
     * @param colorInfo ColorInfo from Google Vision API
     * @return String representation of the color
     */
    private static String convertColorToString(ColorInfo colorInfo) {
        Color color = colorInfo.getColor();
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        
        return String.format("RGB(%d, %d, %d)", red, green, blue);
    }

   //clothing analysis info
    public static class ClothingAnalysis {
        private final String color;
        private final String type;

        public ClothingAnalysis(String color, String type) {
            this.color = color;
            this.type = type;
        }

        public String getColor() {
            return color;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Clothing Analysis: Color = " + color + ", Type = " + type;
        }
    }
}