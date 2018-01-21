import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.Random;

/**
 * populate parent and child tables in an aws hosted mysql db with random strings
 */
public class App {
    public static void main(String [] args){
        Connection conn = null;
        String DBurl = "jdbc:mysql://mysql_db_address"; //Change for your specific DB address
        String DBuser = "user"; //Change for your specific DB's username. This should never be the root user
        String DBpass = "password"; //Change for your specific DB's password
        try {
            conn = DriverManager.getConnection(DBurl, DBuser, DBpass);
        } catch (SQLException ex) {// handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        Statement creator;
        try {
            creator = conn.createStatement();
            String selectDB = "USE thor_db"; //Select DB tables will be added to
            creator.executeQuery(selectDB);
            String dropC = "DROP TABLE IF EXISTS child"; //clear out old child table
            creator.executeUpdate(dropC);
            String dropP = "DROP TABLE IF EXISTS parent"; //clear out old parent table
            creator.executeUpdate(dropP);

            //recreate deleted tables
            String makeParent = "CREATE TABLE parent (id INT NOT NULL AUTO_INCREMENT, " +
                    "parent_str varchar(50) NOT NULL, " +
                    "PRIMARY KEY (id))";
            creator.executeUpdate(makeParent);
            String makeChild = "CREATE TABLE child (" +
                    "  id INT NOT NULL AUTO_INCREMENT," +
                    "  parent_id INT NOT NULL," +
                    "  child_str varchar(50) NOT NULL," +
                    "  INDEX par_ind (parent_id)," +
                    "  FOREIGN KEY (parent_id)" +
                    "  REFERENCES parent(id)" +
                    "  ON DELETE CASCADE" +
                    "  ON UPDATE CASCADE, PRIMARY KEY (id)" +
                    ")";
            creator.executeUpdate(makeChild);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            int numparents = 50; //Add 50 parent rows to parent DB
            int maxnumchildren = 4;
            String insertParentRand = "INSERT INTO parent (parent_str) VALUES (?)";
            PreparedStatement prepParent;
            for(int i = 0; i< numparents; i++){
                prepParent = conn.prepareStatement(insertParentRand);
                prepParent.setString(1, genRandomStr());
                prepParent.executeUpdate();
            }
            String insertChildRand = "INSERT INTO child (child_str, parent_id) VALUES (?, ?)";
            PreparedStatement prepChild;
            for(int i = 0; i< numparents; i++){ //Add randomly up to maxnumchildren of children to each parent
                Random r = new Random();
                int n = r.nextInt(maxnumchildren) + 1;
                for(int j = 0; j < n; j ++) {
                    prepChild = conn.prepareStatement(insertChildRand);
                    prepChild.setString(1, genRandomStr());
                    prepChild.setInt(2, i + 1);
                    prepChild.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Parent and Child Databases created and populated successfully");
    }

    private static String genRandomStr(){
        String randStr = "";
        Random rand = new Random();
        final String toRand = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnop1qrstuvwxyz";
        final int randLength = toRand.length();
        for (int i = 0; i < 50; i++) {
            randStr += toRand.charAt(rand.nextInt(randLength));
        }
        return randStr;
    }
}
