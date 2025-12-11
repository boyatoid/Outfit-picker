package com.vision.maven.maven_vision_jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Desktop;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
// Credit: Matthew Price

public class DatabaseConnector {
    private static String url = "jdbc:mysql://127.0.0.1:3306/groupproject?allowPublicKeyRetrieval=true&useSSL=false";
    private static String user = "root";
    private static String password = "********"; // no pass for u
    private static String db_image;
    
    /**
     * Inserts clothing data and image into the database
     * 
     * @param color Detected color
     * @param item Detected clothing item
     * @param imagePath Path to the image file
     * @throws IOException If there's an error reading the image
     */
    public static void insertQuery(String color, String item, String imagePath) throws IOException {
        String sql = "INSERT INTO PIAP (itemPIAP, colorPIAP, image) VALUES (?, ?, ?);";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             FileInputStream fis = new FileInputStream(new File(imagePath))) {
            
            ensurePIAPTableExists(conn);
            pstmt.setString(1, item);
            pstmt.setString(2, color);
            pstmt.setBinaryStream(3, fis);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted.");
            System.out.println("Image inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves all clothing items from the database
     * 
     * @return List of strings with database entries
     */
    public static List<String> selectQuery() {
        List<String> results = new ArrayList<>();
        String query = "SELECT * FROM PIAP";
        
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                int id = resultSet.getInt("idPIAP");
                String item = resultSet.getString("itemPIAP");
                String color = resultSet.getString("colorPIAP");
                results.add("ID: " + id + ", Item: " + item + ", Color: " + color);
                System.out.println("ID: " + id + ", Item: " + item + ", Color: " + color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * Retrieves the latest image from the database
     * 
     * @return Path to the saved image file
     */
    public static String selectImage() {
        String imagePath = null;
        String query = "SELECT idPIAP, image FROM PIAP ORDER BY idPIAP DESC LIMIT 1";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("idPIAP");
                InputStream input = rs.getBinaryStream("image");
                FileOutputStream output = new FileOutputStream("retrieved_image_"+id+".jpg");
                db_image = "retrieved_image_"+id+".jpg";
                imagePath = db_image;
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.close();
                input.close();
                System.out.println("Image retrieved successfully... Opening " + db_image);
            } else {
                System.out.println("No image found with the given ID.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return imagePath;
    }
    

    public static void openImage() {
        File imageFile = new File(db_image);
        
        if (!imageFile.exists()) {
            System.out.println("File not found!");
            return;
        }
        
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(imageFile);
            } else {
                System.out.println("Open action is not supported.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Ensures the PIAP table exists in the database
     * 
     * @param conn Database connection
     * @return true if table was created, false if already exists
     * @throws SQLException If there's an error with the database
     */
    public static boolean ensurePIAPTableExists(Connection conn) throws SQLException {
        ResultSet tables = conn.getMetaData().getTables(null, null, "PIAP", new String[] {"TABLE"});
        boolean tableExists = tables.next();
        tables.close();
       
        if (!tableExists) {
            String sql = "CREATE TABLE IF NOT EXISTS PIAP (" +
                "idPIAP INT PRIMARY KEY AUTO_INCREMENT, " +
                "itemPIAP VARCHAR(40) NOT NULL, " +
                "colorPIAP VARCHAR(50), " +
                "graphicPIAP VARCHAR(30), " +
                "image BLOB " +
                ")";
            conn.createStatement().execute(sql);
            return true;
        }
        return false;
    }
}
