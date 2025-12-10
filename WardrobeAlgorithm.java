package com.vision.maven.maven_vision_jar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
//Credit: Joanna John

public class WardrobeAlgorithm {
    
    /**
     * Represents a clothing item in the wardrobe
     */
    public static class WardrobeItem {
        String name;
        String type; 
        String color;
        String pattern; 

        public WardrobeItem(String name, String type, String color, String pattern) {
            this.name = name;
            this.type = type;
            this.color = color;
            this.pattern = pattern;
        }

        @Override
        public String toString() {
            return name + " (" + color + ", " + pattern + ")";
        }
    }

    public static class OutfitSelector {
        List<WardrobeItem> wardrobe;
        Random random = new Random();

        public OutfitSelector(List<WardrobeItem> wardrobe) {
            this.wardrobe = wardrobe;
        }

        /**
         * Generates a complete outfit (shirt, pants, shoes)
         * 
         * @return List of WardrobeItems making up the outfit
         */
        public List<WardrobeItem> generateOutfit() {
            WardrobeItem shirt = selectRandomItem("shirt");
            WardrobeItem pants = selectRandomItem(shirt);
            WardrobeItem shoes = selectRandomItem("shoes");

            return Arrays.asList(shirt, pants, shoes);
        }

        /**
         * Selects a random item of the specified type
         * 
         * @param type Type of clothing item to select
         * @return Selected wardrobe item
         */
        private WardrobeItem selectRandomItem(String type) {
            List<WardrobeItem> filtered = getItemsByType(type);
            return filtered.isEmpty() ? null : filtered.get(random.nextInt(filtered.size()));
        }

        /**
         * Selects a random item that doesn't clash with the provided item
         * 
         * @param avoidClashWith Item to avoid clashing with
         * @return Selected wardrobe item
         */
        private WardrobeItem selectRandomItem(WardrobeItem avoidClashWith) {
            List<WardrobeItem> filtered = getItemsByType("pants");
            filtered.removeIf(item -> clashes(item, avoidClashWith));

            return filtered.isEmpty() ? selectRandomItem("pants") : filtered.get(random.nextInt(filtered.size()));
        }

        /**
         * Gets all items of a specified type
         * 
         * @param type Type of clothing to filter for
         * @return List of items matching the type
         */
        private List<WardrobeItem> getItemsByType(String type) {
            List<WardrobeItem> filtered = new ArrayList<>();
            for (WardrobeItem item : wardrobe) {
                if (item.type.equals(type)) {
                    filtered.add(item);
                }
            }
            return filtered;
        }

        /**
         * Determines if two items clash with each other
         * 
         * @param a First item
         * @param b Second item
         * @return True if items clash, false otherwise
         */
        private boolean clashes(WardrobeItem a, WardrobeItem b) {
            return a != null && b != null && a.pattern.equals("striped") && b.pattern.equals("plaid");
        }
    }
    
    /**
     * Creates a default wardrobe with sample items
     * 
     * @return List of sample wardrobe items
     */
    public static List<WardrobeItem> createDefaultWardrobe() {
        return Arrays.asList(
            new WardrobeItem("Blue Striped Shirt", "shirt", "blue", "striped"),
            new WardrobeItem("Red Plaid Shirt", "shirt", "red", "plaid"),
            new WardrobeItem("Black Pants", "pants", "black", "solid"),
            new WardrobeItem("Red Plaid Pants", "pants", "red", "plaid"),
            new WardrobeItem("White Sneakers", "shoes", "white", "solid"),
            new WardrobeItem("Brown Boots", "shoes", "brown", "solid")
        );
    }
    
    /**
     * Creates a WardrobeItem from clothing analysis
     * 
     * @param analysis Clothing analysis from Vision API
     * @return WardrobeItem representation
     */
    public static WardrobeItem createFromAnalysis(App.ClothingAnalysis analysis) {
        String pattern = "solid"; // assumes pattern is soild, enough for poc
        
        String name = (analysis.getColor() != null ? analysis.getColor() : "Unknown color") + 
                      " " + 
                      (analysis.getType() != null ? analysis.getType() : "item");
        
        String color = analysis.getColor();
        if (color != null && color.startsWith("RGB")) {
            if (color.contains("255, 0, 0")) color = "red";
            else if (color.contains("0, 255, 0")) color = "green";
            else if (color.contains("0, 0, 255")) color = "blue";
            else if (color.contains("0, 0, 0")) color = "black";
            else if (color.contains("255, 255, 255")) color = "white";
            else color = "mixed";
        }
        
        return new WardrobeItem(
            name, 
            analysis.getType() != null ? analysis.getType() : "unknown",
            color != null ? color : "unknown",
            pattern
        );
    }
}