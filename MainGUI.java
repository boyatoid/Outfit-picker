package com.vision.maven.maven_vision_jar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatLightLaf;
import com.vision.maven.maven_vision_jar.WardrobeAlgorithm.WardrobeItem;
import com.vision.maven.maven_vision_jar.WardrobeAlgorithm.OutfitSelector;
// Credit: Aditya Mathur

public class MainGUI {
    private String imagePath;
    private String serviceAccountPath;
    private JFrame frame;
    private JLabel imageLabel;
    private JPanel suggestionsPanel;
    private JButton startButton;
    private JButton chooseFileButton;
    private JButton deleteButton;
    private JTextArea outputTextArea;
    
    private List<WardrobeItem> currentWardrobe = new ArrayList<>();
    
    public MainGUI() {
        initGUI();
        requestServiceAccountPath();
    }
    
    private void requestServiceAccountPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Google Cloud Service Account JSON File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            serviceAccountPath = fileChooser.getSelectedFile().getAbsolutePath();
            appendOutput("Service account configured: " + serviceAccountPath);
        } else {
            appendOutput("Warning: Service account not configured. Vision API functions will not work.");
        }
    }
    

    private void initGUI() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF...");
        }
        
        UIManager.put("Button.arc", 20);
        UIManager.put("Component.arc", 15);
        UIManager.put("TextComponent.arc", 10);

        frame = new JFrame("👗 Wardrobe Assistant");
        frame.setSize(800, 600);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(245, 248, 255)); 

        chooseFileButton = new JButton("Choose File");
        chooseFileButton.setBounds(30, 10, 220, 50);
        chooseFileButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chooseFileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseFileButton.setBackground(new Color(173, 216, 230)); 
        chooseFileButton.setForeground(Color.BLACK);
        chooseFileButton.setOpaque(true);
        chooseFileButton.setBorderPainted(false);
        frame.add(chooseFileButton);

        deleteButton = new JButton("Delete Picture");
        deleteButton.setBounds(260, 10, 150, 50);
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setBackground(new Color(255, 182, 193)); 
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);
        frame.add(deleteButton);

        imageLabel = new JLabel("Photo will appear here", SwingConstants.CENTER);
        imageLabel.setBounds(30, 80, 250, 200);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        frame.add(imageLabel);

    
        startButton = new JButton("Analyze Image");
        startButton.setBounds(30, 300, 250, 50);
        startButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.setBackground(new Color(144, 238, 144)); 
        startButton.setForeground(Color.BLACK);
        startButton.setOpaque(true);
        startButton.setBorderPainted(false);
        frame.add(startButton);
        
        JButton generateOutfitButton = new JButton("Generate Matching Outfit");
        generateOutfitButton.setBounds(30, 360, 250, 50);
        generateOutfitButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        generateOutfitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generateOutfitButton.setBackground(new Color(255, 215, 0)); 
        generateOutfitButton.setForeground(Color.BLACK);
        generateOutfitButton.setOpaque(true);
        generateOutfitButton.setBorderPainted(false);
        frame.add(generateOutfitButton);
        
        JButton viewDatabaseButton = new JButton("View Database");
        viewDatabaseButton.setBounds(30, 420, 250, 50);
        viewDatabaseButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        viewDatabaseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewDatabaseButton.setBackground(new Color(220, 220, 220));
        viewDatabaseButton.setForeground(Color.BLACK);
        viewDatabaseButton.setOpaque(true);
        viewDatabaseButton.setBorderPainted(false);
        frame.add(viewDatabaseButton);


        suggestionsPanel = new JPanel();
        suggestionsPanel.setLayout(new GridLayout(5, 1, 5, 5));
        suggestionsPanel.setBounds(300, 80, 450, 200);
        suggestionsPanel.setBorder(BorderFactory.createTitledBorder("AI Suggestions"));
        

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        outputTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        scrollPane.setBounds(300, 300, 450, 170);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));
        frame.add(scrollPane);
        
        for (int i = 0; i < 4; i++) {
            JTextField suggestion = new JTextField("Suggestion " + (i + 1));
            suggestion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            suggestionsPanel.add(suggestion);
        }
        frame.add(suggestionsPanel);

        chooseFileButton.addActionListener(e -> chooseImage());
        deleteButton.addActionListener(e -> {
            imageLabel.setIcon(null);
            imageLabel.setText("Photo will appear here");
            clearSuggestions();
            appendOutput("Image cleared");
        });

        startButton.addActionListener(e -> {
            if (imagePath == null || imagePath.isEmpty()) {
                appendOutput("Please select an image first");
                return;
            }
            
            if (serviceAccountPath == null || serviceAccountPath.isEmpty()) {
                appendOutput("Please configure service account first");
                requestServiceAccountPath();
                return;
            }
            
            analyzeImage();
        });
        
        generateOutfitButton.addActionListener(e -> generateMatchingOutfit());
        viewDatabaseButton.addActionListener(e -> viewDatabase());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        appendOutput("Application started. Please select a service account JSON file and an image to analyze.");
    }
    
    /**
     * Appends text to the output text area
     * 
     * @param message Message to append
     */
    private void appendOutput(String message) {
        outputTextArea.append(message + "\n");
        outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
    }
    

    private void clearSuggestions() {
        suggestionsPanel.removeAll();
        for (int i = 0; i < 4; i++) {
            JTextField suggestion = new JTextField("Suggestion " + (i + 1));
            suggestion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            suggestionsPanel.add(suggestion);
        }
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }

    /**
     * Opens file chooser to select an image
     */
    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));

        int option = fileChooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            imagePath = selectedFile.getAbsolutePath();
            ImageIcon image = new ImageIcon(imagePath);
            Image img = image.getImage().getScaledInstance(250, 200, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setText(""); // removes the placeholder text
            appendOutput("Selected image: " + imagePath);
        }
    }
    
    /**
     * Analyzes the selected image using Vision API
     */
    private void analyzeImage() {
        appendOutput("Analyzing image...");
        setButtonsEnabled(false);
        
        SwingWorker<App.ClothingAnalysis, Void> worker = new SwingWorker<App.ClothingAnalysis, Void>() {
            @Override
            protected App.ClothingAnalysis doInBackground() throws Exception {
                return App.analyzeClothing(imagePath, serviceAccountPath);
            }
            
            @Override
            protected void done() {
                try {
                    App.ClothingAnalysis analysis = get();
                    if (analysis != null) {
                        appendOutput("Analysis complete!");
                        appendOutput("Detected type: " + analysis.getType());
                        appendOutput("Detected color: " + analysis.getColor());
                        
                        updateSuggestions(analysis);
                        
                        try {
                            DatabaseConnector.insertQuery(analysis.getColor(), analysis.getType(), imagePath);
                            appendOutput("Saved to database successfully");
                        } catch (IOException e) {
                            appendOutput("Error saving to database: " + e.getMessage());
                            e.printStackTrace();
                        }
                        
                        WardrobeItem item = WardrobeAlgorithm.createFromAnalysis(analysis);
                        currentWardrobe.add(item);
                        appendOutput("Added to wardrobe: " + item);
                    } else {
                        appendOutput("Analysis failed or no clothing detected");
                    }
                } catch (Exception e) {
                    appendOutput("Error during analysis: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Enable/disable buttons during processing
     * 
     * @param enabled Enable/disable status
     */
    private void setButtonsEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
        chooseFileButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
    }
    
    /**
     * Updates the suggestions panel with analysis results
     * 
     * @param analysis Clothing analysis results
     */
    private void updateSuggestions(App.ClothingAnalysis analysis) {
        suggestionsPanel.removeAll();
        
        JTextField typeField = new JTextField("Type: " + (analysis.getType() != null ? analysis.getType() : "Unknown"));
        typeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionsPanel.add(typeField);
        
        JTextField colorField = new JTextField("Color: " + (analysis.getColor() != null ? analysis.getColor() : "Unknown"));
        colorField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionsPanel.add(colorField);
        
        JTextField suggestionField1 = new JTextField("Matching suggestion: Add complementary accessories");
        suggestionField1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionsPanel.add(suggestionField1);
        
        JTextField suggestionField2 = new JTextField("Style suggestion: Good for casual outfits");
        suggestionField2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionsPanel.add(suggestionField2);
        
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }
    
    /**
     * Generates matching outfit suggestions
     */
    private void generateMatchingOutfit() {
        if (currentWardrobe.isEmpty()) {
            appendOutput("No items in wardrobe. Please analyze some clothing first.");
            return;
        }
        
        appendOutput("Generating matching outfit...");
        
 
        if (currentWardrobe.size() < 3) {
            appendOutput("Adding some default items to wardrobe for better matching");
            currentWardrobe.addAll(WardrobeAlgorithm.createDefaultWardrobe());
        }
        
        OutfitSelector selector = new OutfitSelector(currentWardrobe);
       
        suggestionsPanel.removeAll();

        for (int i = 0; i < 3; i++) {
            List<WardrobeItem> outfit = selector.generateOutfit();
            
            StringBuilder outfitInfo = new StringBuilder();
            outfitInfo.append("Outfit ").append(i + 1).append(": ");
            
            for (WardrobeItem item : outfit) {
                if (item != null) {
                    outfitInfo.append(item.name).append(", ");
                }
            }
            
            if (outfitInfo.length() > 2) {
                outfitInfo.setLength(outfitInfo.length() - 2);
            }
            
            JTextField outfitField = new JTextField(outfitInfo.toString());
            outfitField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            suggestionsPanel.add(outfitField);
        }
        
        JTextField suggestionField = new JTextField("Add more items to get better matches!");
        suggestionField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionsPanel.add(suggestionField);
        
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
        
        appendOutput("Outfit suggestions generated successfully!");
    }
    
    private void viewDatabase() {
        appendOutput("Retrieving data from database...");
        
        List<String> dbResults = DatabaseConnector.selectQuery();
        
        if (dbResults.isEmpty()) {
            appendOutput("No items found in database");
        } else {
            appendOutput("Database contents:");
            for (String result : dbResults) {
                appendOutput(result);
            }
        }
        
        String imagePath = DatabaseConnector.selectImage();
        if (imagePath != null) {
            DatabaseConnector.openImage();
        }
    }
    
    /**
     * Main method to start the application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}