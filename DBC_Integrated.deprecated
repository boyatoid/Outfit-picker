package com.vision.maven.maven_vision_jar;
// PREVIOUS VERSION OF "DatabaseConnector" THIS IS NO LONGER IN USE
// DO NOT USE !!!!!!!!!!!!!!!!!
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
import java.util.Scanner;

public class DBC_Integrated {
    private static String url = "jdbc:mysql://127.0.0.1:3306/groupproject?allowPublicKeyRetrieval=true&useSSL=false";
    private static String user = "root";
    private static String password = "******";
    // docker run --name project_database -e MYSQL_ROOT_PASSWORD=****** -e MYSQL_DATABASE=groupproject -p 3306:3306 -d mysql:8.0
    // get it up and running ^ 
    // !*for first run, open docker and spin up container if already created*!
    
    //static String imagePath = "/Users/thepwn3r/Pictures/plaid.jpeg";
    static String imagePath;
    static String serviceAccountPath = "/Users/thepwn3r/Desktop/key.json";
    static String db_image;
    
    public static void main(String[] args) throws IOException {
    	@SuppressWarnings("resource")
		Scanner user_input = new Scanner(System.in);
        System.out.println("Select picture for processing: ");
        String pic = user_input.next();
        imagePath = "/Users/thepwn3r/Pictures/project/" + pic;
        
        // Perform AI-based clothing analysis
        String shirtColor = App.detectShirtColor(imagePath, serviceAccountPath);
        System.out.println("Detected Shirt Color: " + shirtColor);
        
        String clothingType = App.detectClothingType(imagePath, serviceAccountPath);
        System.out.println("Detected Clothing Type: " + clothingType);
        
        // Insert the analyzed data into the database
        insertQuery(shirtColor, clothingType);
        selectQuery();
        selectImage();
        openImage();
    }
    
    public static void insertQuery(String color, String item) throws IOException {
        
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
    
    public static void selectQuery() {
        String query = "SELECT * FROM PIAP";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                int id = resultSet.getInt("idPIAP");
                String item = resultSet.getString("itemPIAP");
                String color = resultSet.getString("colorPIAP");
                System.out.println("ID: " + id + ", Item: " + item + ", Color: " + color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void selectImage() {
        //String query = "SELECT image FROM PIAP WHERE idPIAP = ?";
    	String query = "SELECT idPIAP, image FROM PIAP ORDER BY idPIAP DESC LIMIT 1";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            //stmt.setInt(1, 5);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
            	int id = rs.getInt("idPIAP");
                InputStream input = rs.getBinaryStream("image");
                FileOutputStream output = new FileOutputStream("retrieved_image_"+id+".jpg");
                db_image = "retrieved_image_"+id+".jpg";
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
    }
    
    public static void openImage() {
        //String imagePlacement = "retrieved_image.jpg";
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
    
    public static String cmdtest() {
    	
    	
    	return "lmao";
    }
    
    private static boolean ensurePIAPTableExists(Connection conn) throws SQLException {
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
