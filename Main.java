package com.vision.maven.maven_vision_jar;

import java.util.Scanner;
import java.io.IOException;
// Credit: With Love from the whole team :)

public class Main {
    
    /**
     * Main method to start the application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--cli")) {
            runCLI();
        } else {
            javax.swing.SwingUtilities.invokeLater(() -> new MainGUI());
        }
    }
    
    /**
     * Runs the application in command-line interface mode
     */
    private static void runCLI() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Enter service account path: ");
        String serviceAccountPath = scanner.nextLine();
        
        System.out.println("Select picture for processing: ");
        String imagePath = scanner.nextLine();
        
        try {
            System.out.println("Analyzing image...");
            String shirtColor = App.detectShirtColor(imagePath, serviceAccountPath);
            System.out.println("Detected Shirt Color: " + shirtColor);
            
            String clothingType = App.detectClothingType(imagePath, serviceAccountPath);
            System.out.println("Detected Clothing Type: " + clothingType);
            
            System.out.println("Saving to database...");
            DatabaseConnector.insertQuery(shirtColor, clothingType, imagePath);
            
            System.out.println("\nDatabase contents:");
            DatabaseConnector.selectQuery();
            
            System.out.println("\nRetrieving latest image...");
            DatabaseConnector.selectImage();
            DatabaseConnector.openImage();
            
            System.out.println("\nGenerating outfit suggestions...");
            App.ClothingAnalysis analysis = new App.ClothingAnalysis(shirtColor, clothingType);
            WardrobeAlgorithm.WardrobeItem item = WardrobeAlgorithm.createFromAnalysis(analysis);
            
            java.util.List<WardrobeAlgorithm.WardrobeItem> wardrobe = WardrobeAlgorithm.createDefaultWardrobe();
            wardrobe.add(item);
            
            WardrobeAlgorithm.OutfitSelector selector = new WardrobeAlgorithm.OutfitSelector(wardrobe);
            for (int i = 0; i < 3; i++) {
                System.out.println("\nOutfit " + (i + 1) + ":");
                java.util.List<WardrobeAlgorithm.WardrobeItem> outfit = selector.generateOutfit();
                for (WardrobeAlgorithm.WardrobeItem outfitItem : outfit) {
                    if (outfitItem != null) {
                        System.out.println("- " + outfitItem);
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}