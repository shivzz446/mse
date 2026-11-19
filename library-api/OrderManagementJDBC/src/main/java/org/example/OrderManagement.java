package org.example;

import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASSWORD = "12345678";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Database Connected Successfully!");

            while (true) {

                System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert 3 Orders");
                System.out.println("2. Retrieve Orders by Status");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Calculate Total Order Value");
                System.out.println("6. Delete Cancelled Order");
                System.out.println("7. Exit");

                System.out.print("Enter choice: ");
                int choice = sc.nextInt();

                switch (choice) {

                    case 1:

                        String insertQuery =
                                "INSERT INTO Orders " +
                                        "(order_id, customer_name, product_name, quantity, unit_price, status) " +
                                        "VALUES (?, ?, ?, ?, ?, ?)";

                        try (PreparedStatement ps = con.prepareStatement(insertQuery)) {

                            for (int i = 1; i <= 3; i++) {

                                System.out.println("\nEnter Order " + i + " Details:");

                                System.out.print("Order ID: ");
                                int id = sc.nextInt();
                                sc.nextLine();

                                System.out.print("Customer Name: ");
                                String customer = sc.nextLine();

                                System.out.print("Product Name: ");
                                String product = sc.nextLine();

                                System.out.print("Quantity: ");
                                int quantity = sc.nextInt();

                                System.out.print("Unit Price: ");
                                double price = sc.nextDouble();
                                sc.nextLine();

                                System.out.print("Status: ");
                                String status = sc.nextLine();

                                ps.setInt(1, id);
                                ps.setString(2, customer);
                                ps.setString(3, product);
                                ps.setInt(4, quantity);
                                ps.setDouble(5, price);
                                ps.setString(6, status);

                                try {
                                    ps.executeUpdate();
                                    System.out.println("Order Inserted Successfully!");

                                } catch (SQLException e) {
                                    if ("23000".equals(e.getSQLState())) {
                                        System.out.println("Duplicate Order ID!");
                                    } else {
                                        throw e;
                                    }
                                }
                            }
                        }
                        break;

                    case 2:

                        sc.nextLine();

                        System.out.print("Enter status: ");
                        String status = sc.nextLine();

                        String selectQuery =
                                "SELECT * FROM Orders " +
                                        "WHERE status = ? AND quantity > 2 " +
                                        "ORDER BY unit_price DESC";

                        try (PreparedStatement ps =
                                     con.prepareStatement(selectQuery)) {

                            ps.setString(1, status);

                            try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {
                                    System.out.println(
                                            rs.getInt("order_id") + " | " +
                                                    rs.getString("customer_name") + " | " +
                                                    rs.getString("product_name") + " | " +
                                                    rs.getInt("quantity") + " | " +
                                                    rs.getDouble("unit_price") + " | " +
                                                    rs.getString("status"));
                                }
                            }
                        }
                        break;

                    case 3:

                        System.out.print("Enter Order ID: ");
                        int orderId = sc.nextInt();

                        System.out.print("Enter quantity to increase: ");
                        int qty = sc.nextInt();

                        String updateQty =
                                "UPDATE Orders " +
                                        "SET quantity = quantity + ? " +
                                        "WHERE order_id = ?";

                        try (PreparedStatement ps =
                                     con.prepareStatement(updateQty)) {

                            ps.setInt(1, qty);
                            ps.setInt(2, orderId);

                            int rows = ps.executeUpdate();

                            if (rows > 0)
                                System.out.println("Quantity updated!");
                            else
                                System.out.println("Order not found!");
                        }
                        break;

                    case 4:

                        System.out.print("Enter Order ID: ");
                        int updateId = sc.nextInt();
                        sc.nextLine();

                        System.out.print("Enter new status: ");
                        String newStatus = sc.nextLine();

                        if (!newStatus.equalsIgnoreCase("Pending") &&
                                !newStatus.equalsIgnoreCase("Shipped") &&
                                !newStatus.equalsIgnoreCase("Cancelled")) {

                            System.out.println("Invalid Status!");
                            break;
                        }

                        String updateStatus =
                                "UPDATE Orders SET status = ? WHERE order_id = ?";

                        try (PreparedStatement ps =
                                     con.prepareStatement(updateStatus)) {

                            ps.setString(1, newStatus);
                            ps.setInt(2, updateId);

                            int rows = ps.executeUpdate();

                            if (rows > 0) {

                                System.out.println("Status updated!");

                                String showOrder =
                                        "SELECT * FROM Orders WHERE order_id = ?";

                                try (PreparedStatement showPs =
                                             con.prepareStatement(showOrder)) {

                                    showPs.setInt(1, updateId);

                                    try (ResultSet rs =
                                                 showPs.executeQuery()) {

                                        if (rs.next()) {
                                            System.out.println(
                                                    rs.getInt("order_id") + " | " +
                                                            rs.getString("customer_name") + " | " +
                                                            rs.getString("product_name") + " | " +
                                                            rs.getInt("quantity") + " | " +
                                                            rs.getDouble("unit_price") + " | " +
                                                            rs.getString("status"));
                                        }
                                    }
                                }

                            } else {
                                System.out.println("Order not found!");
                            }
                        }
                        break;

                    case 5:

                        String totalQuery =
                                "SELECT SUM(quantity * unit_price) " +
                                        "AS Total_Order_Value FROM Orders";

                        try (Statement st = con.createStatement();
                             ResultSet rs = st.executeQuery(totalQuery)) {

                            if (rs.next()) {
                                System.out.println(
                                        "Total Order Value = " +
                                                rs.getDouble("Total_Order_Value"));
                            }
                        }
                        break;


                    case 6:

                        System.out.print("Enter Order ID: ");
                        int deleteId = sc.nextInt();

                        String deleteQuery =
                                "DELETE FROM Orders " +
                                        "WHERE order_id = ? AND status = 'Cancelled'";

                        try (PreparedStatement ps =
                                     con.prepareStatement(deleteQuery)) {

                            ps.setInt(1, deleteId);

                            int rows = ps.executeUpdate();

                            if (rows > 0)
                                System.out.println("Cancelled Order Deleted!");
                            else
                                System.out.println(
                                        "Order not found or not Cancelled!");
                        }
                        break;


                    case 7:
                        System.out.println("Program Exited!");
                        sc.close();
                        return;

                    default:
                        System.out.println("Invalid Choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}