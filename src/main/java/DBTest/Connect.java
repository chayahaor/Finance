package DBTest;

import java.sql.*;

public class Connect {

    public static void main(String[] args) {
        //TODO: When using this code, make sure to change the statement query
        String dbName = "finance";
        try
        {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/"+dbName, "root", "");
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Select * from dbTableName");
            while (resultSet.next())
            {
                System.out.println(resultSet.getString(1));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
