package org.example;

import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASSWORD = "12345678";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Database Connected Successfully!");

            insertOrders(con);          // Q1(a)
            displayOrdersByStatus(con);  // Q1(b)
            increaseQuantity(con);       // Q1(c)
            updateOrderStatus(con);      // Q1(d)
            totalValueAndDelete(con);    // Q1(e)

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Q1(a): Insert 3 orders using PreparedStatement
    static void insertOrders(Connection con) throws SQLException {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter Order " + i);

                System.out.print("Order ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Customer Name: ");
                String customer = sc.nextLine();

                System.out.print("Product Name: ");
                String product = sc.nextLine();

                System.out.print("Quantity: ");
                int qty = sc.nextInt();

                System.out.print("Unit Price: ");
                double price = sc.nextDouble();
                sc.nextLine();

                System.out.print("Status: ");
                String status = sc.nextLine();

                ps.setInt(1, id);
                ps.setString(2, customer);
                ps.setString(3, product);
                ps.setInt(4, qty);
                ps.setDouble(5, price);
                ps.setString(6, status);

                try {
                    ps.executeUpdate();
                    System.out.println("Order Inserted Successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID!");
                }
            }
        }
    }

    // Q1(b): Display orders by status, quantity > 2
    static void displayOrdersByStatus(Connection con) throws SQLException {

        System.out.print("\nEnter order status to search: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {

                System.out.println("\nOrder ID | Customer | Product | Quantity | Price | Status");
                System.out.println("---------------------------------------------------------");

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    System.out.println(
                            rs.getInt("order_id") + " | " +
                                    rs.getString("customer_name") + " | " +
                                    rs.getString("product_name") + " | " +
                                    rs.getInt("quantity") + " | " +
                                    rs.getDouble("unit_price") + " | " +
                                    rs.getString("status")
                    );
                }

                if (!found) {
                    System.out.println("No matching orders found!");
                }
            }
        }
    }

    // Q1(c): Increase existing order quantity
    static void increaseQuantity(Connection con) throws SQLException {

        System.out.print("\nEnter Order ID: ");
        int id = sc.nextInt();

        System.out.print("Enter additional quantity: ");
        int additionalQty = sc.nextInt();
        sc.nextLine();

        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, additionalQty);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("Order quantity updated successfully!");

                String selectSQL = "SELECT * FROM Orders WHERE order_id = ?";

                try (PreparedStatement selectPS = con.prepareStatement(selectSQL)) {

                    selectPS.setInt(1, id);

                    try (ResultSet rs = selectPS.executeQuery()) {

                        if (rs.next()) {

                            System.out.println("\nUpdated Order Details:");
                            System.out.println("Order ID: " + rs.getInt("order_id"));
                            System.out.println("Customer: " + rs.getString("customer_name"));
                            System.out.println("Product: " + rs.getString("product_name"));
                            System.out.println("Quantity: " + rs.getInt("quantity"));
                            System.out.println("Price: " + rs.getDouble("unit_price"));
                            System.out.println("Status: " + rs.getString("status"));
                        }
                    }
                }

            } else {
                System.out.println("Order ID not found!");
            }
        }
    }

    // Q1(d): Update order status with validation
    static void updateOrderStatus(Connection con) throws SQLException {

        System.out.print("\nEnter Order ID to update status: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter new status (Pending/Shipped/Delivered): ");
        String status = sc.nextLine();

        if (!status.equalsIgnoreCase("Pending") &&
                !status.equalsIgnoreCase("Shipped") &&
                !status.equalsIgnoreCase("Delivered")) {

            System.out.println("Invalid Status!");
            return;
        }

        if (status.equalsIgnoreCase("pending")) {
            status = "Pending";
        } else if (status.equalsIgnoreCase("shipped")) {
            status = "Shipped";
        } else {
            status = "Delivered";
        }

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("Order Status Updated Successfully!");

                String selectSQL = "SELECT * FROM Orders WHERE order_id = ?";

                try (PreparedStatement selectPS = con.prepareStatement(selectSQL)) {

                    selectPS.setInt(1, id);

                    try (ResultSet rs = selectPS.executeQuery()) {

                        if (rs.next()) {

                            System.out.println("\nUpdated Order Details:");
                            System.out.println("Order ID: " + rs.getInt("order_id"));
                            System.out.println("Customer: " + rs.getString("customer_name"));
                            System.out.println("Product: " + rs.getString("product_name"));
                            System.out.println("Quantity: " + rs.getInt("quantity"));
                            System.out.println("Price: " + rs.getDouble("unit_price"));
                            System.out.println("Status: " + rs.getString("status"));
                        }
                    }
                }

            } else {
                System.out.println("Order ID not found!");
            }
        }
    }

    // Q1(e): Display total order value and delete Cancelled order
    static void totalValueAndDelete(Connection con) throws SQLException {

        // Calculate total order value
        String sumSQL = "SELECT SUM(quantity * unit_price) AS total_value FROM Orders";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sumSQL)) {

            if (rs.next()) {
                System.out.println("\nTotal Order Value: " + rs.getDouble("total_value"));
            }
        }

        // Delete only Cancelled order
        System.out.print("\nEnter Order ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        String sql = "DELETE FROM Orders WHERE order_id = ? AND LOWER(status) = 'cancelled'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Cancelled Order Deleted Successfully!");
            } else {
                System.out.println("Order not found or order is not Cancelled!");
            }
        }
    }
}