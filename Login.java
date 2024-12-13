import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

// Main class for user login system
public class Login {
    // Path to the file where user details are stored
    private static final String FILE_PATH = "userdetails.txt";
    private static final Scanner scanner = new Scanner(System.in);
    // Main method to handle sign up or sign in
    public static void main(String[] args) throws Exception {
        
        String choice;
        // Loop until the user provides a valid choice
        do {
            System.out.print("Please choose an option: 'Sign up' or 'Sign in': ");
            choice = scanner.nextLine().trim().toLowerCase();
            if (!choice.equals("sign up") && !choice.equals("sign in")) {
                System.out.println("Invalid choice, try again");
            }

        } while (!choice.equals("sign up") && !choice.equals("sign in"));

        // Call corresponding method based on user choice
        if (choice.equals("sign up")) {
            signUp();
        } else {
            signIn();
        }
        scanner.close();
    }

    // Method to handle user account creation
    public static void signUp() throws Exception {
        String username;
        String password;

        // Loop until the user provides a unique username
        do {
            System.out.println("Enter username");
            username = scanner.nextLine().trim();
            // Check file to see if username is already used
            if (usernameExists(username)) {
                System.out.println("An account with this username already exists.");
            }
        } while (usernameExists(username));

        // Loop until the user provides a valid password
        String valid;
        do {
            System.out.print("Please enter password: ");
            password = scanner.nextLine();
            // Check if password follows rules
            valid = checkPassword(password, username);
            // Explain how password breaks rules
            if (!valid.equals("Valid")) {
                System.out.print(valid + "\n");
            }
            else {
                System.out.println("Valid password");
            }
        } while (!valid.equals("Valid"));
        
        // Save user details to file
        saveDetails(username, password);
        System.out.println("Sign up successful. You can now log in.");
        signIn();
    }

    //  Method to allow users to sign in by validating details from the file
    public static void signIn() throws Exception {
        String username;
        System.out.println("Enter username");
        username = scanner.nextLine().trim();
        // Check file to see if username is attached to an existing account
        if (!usernameExists(username)) {
            System.out.println("An account with this username doesn't exist.");
            // Restart program
            main(null);
        }
        
        int attempts = 0;
        // Allow up to 3 attempts for the user to enter the correct password
        while (attempts < 3)  {
            System.out.println("Enter password");
            String password = scanner.nextLine();
            // Check file to see if password matches the username
            if (validateDetails(username, password)) {
                System.out.println("Correct password. Logging in...");
                // End program
                System.exit(0);
            }
            else {
                System.out.println("Incorrect password.");
                attempts++;
            }   
            // Lock out user for 15 seconds after 3 failed attempts
            if (attempts == 3) {
                System.out.println("You have entered the wrong password too many times. Please wait 15 seconds.\n");
                // 15 second time out 
                TimeUnit.SECONDS.sleep(15);   
                attempts = 0;    
                // Allow user to restart sign in process
                System.out.print("Do you want to try signing in with a different username instead?\n");
                String changeUsername = scanner.nextLine().trim().toLowerCase();
                if (changeUsername.equals("yes")) {
                    signIn();
                }    
            }
        }

    }

    // Check if username already exists in file
    private static boolean usernameExists(String username) throws IOException {
        // Create a File object for the user details file
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            // Return false if the file does not exist 
            return false;
        }

        // Create a Scanner object to read the file
        Scanner fileScanner = new Scanner(file);
        // Read each line of the file until the end
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            // Return true if username is found in file
            if (line.split(",")[0].equals(username)) {
                fileScanner.close();              
                return true;
            }
        }
        fileScanner.close();
        // Return false if username is not found in file
        return false;
    }

    // Save user details to the file
    private static void saveDetails(String username, String password) throws IOException {
        FileWriter writer = new FileWriter(FILE_PATH, true);
        // Write username and password to the file separated by a comma
        writer.write(username + "," + password + "\n");
        writer.close();
    }

    // // Check if the provided username and password match any record in the file
    private static boolean validateDetails(String username, String password) throws FileNotFoundException {
        // Create a File object for the user details file
        File file = new File(FILE_PATH);
        if (!file.exists())
            // Return false if file does not exist
            return false;
        // Create a Scanner object to read the file
        Scanner fileScanner = new Scanner(file);
        // Read each line of the file until the end
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            // Split the line into username and password and store in details array
            String[] details = line.split(",");
            // If any of rows in the array match the given username and password
            if (details[0].equals(username) && details[1].equals(password)) {
                fileScanner.close();
                // Return true if username and password match
                return true;
            }
        }
        fileScanner.close();
        // Return false if username and password do not match
        return false;
    }

    // Check if the password meets all requirements
    public static String checkPassword(String password, String user) throws Exception {
        // Check if length of string is at least 8 characters
        if (password.length() < 8) {
            return "Invalid, password must be at least 8 characters";
        }
        // Check for uppercase letters
        if (!password.matches(".*[A-Z].*")) {
            return "Invalid, password must contain at least one uppercase letter";
        }
        // Check for lowercase letters
        if (!password.matches(".*[a-z].*")) {
            return "Invalid, password must contain at least one lowercase letter";
        }
        // Check for numbers
        if (!password.matches(".*\\d.*")) {
            return "Invalid, password must contain a number";
        }
        // Check for listed special characters
        if (!password.matches(".*[!@#$€£%^&*()\\-_=+\\[\\]{};:'\",.<>?/\\\\|`~].*")) {
            return "Invalid, password must contain a special character"; 
        }
        // Check if the password has 3 or more consecutive characters
        if (password.matches(".*(.)\\1{2,}.*")) {
            return "Invalid, password must not contain 3 or more repeating characters in a row";
        }
        // Check if string contains username
        if (password.toLowerCase().contains(user.toLowerCase())) {
            return "Invalid, password must not contain the username";
        }
        // Check if password is listed in Have I Been Pwned data breach list
        if (isPasswordPwned(password)) {
            return "Invalid, this password has been found in a data breach. Please use a different password.";
        }
        // Return valid if all checks pass
        return "Valid";
    }


    // Check if the password has been found in a data breach
    private static boolean isPasswordPwned(String password) throws Exception {
        // Generate SHA-1 hash of the password and convert it to uppercase
        String sha1Hash = sha1(password).toUpperCase();
        String prefix = sha1Hash.substring(0, 5);
        String suffix = sha1Hash.substring(5);

        // Create the URL to check the hash prefix
        URL url = new URL("https://api.pwnedpasswords.com/range/" + prefix);
        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Set the request method to GET
        connection.setRequestMethod("GET");
        // Create a scanner to read the API response
        Scanner apiScanner = new Scanner(connection.getInputStream());
        while (apiScanner.hasNextLine()) {
            // Get the current line of response being read
            String line = apiScanner.nextLine();
            // Check if the line contains the suffix of the hash
            if (line.contains(suffix)) {
                apiScanner.close();
                // Return true if the suffix is found, indicating the password is pwned
                return true;
            }
        }
        apiScanner.close();
        // Return false if the suffix is not found, indicating the password is not pwned
        return false;

    }

    // Generate SHA-1 hash of the given input
    private static String sha1(String input) throws NoSuchAlgorithmException {
        // Get an instance of the SHA-1 message digest
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        // Compute the hash of the input bytes
        byte[] hashBytes = md.digest(input.getBytes());
        // Create a StringBuilder to hold the hash
        StringBuilder sb = new StringBuilder();
        // Iterate over the hash bytes
        for (byte b : hashBytes) {
        // Convert each byte to a two-digit hexadecimal string 
            sb.append(String.format("%02x", b));
        }
        // Return the resulting hash string
        return sb.toString();
    }
}


