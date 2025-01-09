import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OracleSwingApp {

    private static void printTableStructure() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet columns = meta.getColumns(
                    null,
                    null,
                    "NEW_PLAYER",
                    null
                );

                System.out.println("Table structure:");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String dataType = columns.getString("TYPE_NAME");
                    System.out.println(columnName + " - " + dataType);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Replace the existing getConnection method with this one
    public static Connection getConnection() {
        try {
            // Load Oracle JDBC Driver
            Class.forName("oracle.jdbc.OracleDriver");
            // Define the connection URL
            String dbURL1 =
                "jdbc:oracle:thin:mcconnol/02084278@oracle.scs.ryerson.ca:1521:orcl";
            // Establish and return the connection
            return DriverManager.getConnection(dbURL1);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "Failed to connect to the database."
            );
            return null;
        }
    }

    private static void dropTable() {
        try (Connection conn = getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(null, "Drop table error.");
                return;
            }

            try (Statement stmt = conn.createStatement()) {
                String dropQuery = "DROP TABLE new_player";
                stmt.execute(dropQuery);
                System.out.println("Dropped Table.");
            } catch (SQLException e) {
                if (e.getErrorCode() != 942) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //To create Player Table on the database. Table won't be saved
    private static void createTable() {
        try (Connection conn = getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(
                    null,
                    "Failed to create table - Connection error."
                );
            }

            boolean tableExists = false;

            try {
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet rs = metaData.getTables(
                    null,
                    null,
                    "NEW_PLAYER",
                    new String[] { "TABLE" }
                );
                tableExists = rs.next();
            } catch (SQLException error) {
                error.printStackTrace();
            }

            if (!tableExists) {
                try (Statement smtm = conn.createStatement()) {
                    String createTableQuery =
                        """

                            CREATE TABLE new_player (

                            player_id NUMBER PRIMARY KEY,
                            player_name VARCHAR2(25) NOT NULL,
                            player_age NUMBER CHECK (player_age BETWEEN 18 AND 65) NOT NULL,
                            player_jersey NUMBER CHECK (player_jersey BETWEEN 0 AND 99) NOT NULL,
                            player_position VARCHAR2(25) NOT NULL,
                            nationality VARCHAR2(4) NOT NULL

                            )

                        """;
                    smtm.execute(createTableQuery);
                    JOptionPane.showMessageDialog(
                        null,
                        "Created new_player table."
                    );
                } catch (SQLException error) {
                    error.printStackTrace();
                    JOptionPane.showMessageDialog(
                        null,
                        "Error creating new_player table."
                    );
                }
            }
        } catch (SQLException error) {
            error.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "Cannot create new_player table"
            );
        }
    }

    //main function
    public static void main(String[] args) {
        printTableStructure();
        //dropping table for fresh start
        dropTable();
        //creating table afterwards
        createTable();

        JFrame frame = new JFrame("Soccer League DBMS");
        frame.setSize(800, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Table to display player data
        DefaultTableModel model = new DefaultTableModel(
            new String[] {
                "PlayerID",
                "PlayerName",
                "PlayerAge",
                "PlayerJersey",
                "PlayerPosition",
                "PlayerNationality",
            },
            0
        );
        JTable table = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(table);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Input fields and buttons initialization
        JPanel controlPanel = new JPanel();
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JTextField idField = new JTextField(10);
        JTextField nameField = new JTextField(10);
        JTextField ageField = new JTextField(10);
        JTextField jerseyField = new JTextField(10);
        JTextField positionField = new JTextField(10);
        JTextField nationalityField = new JTextField(10);
        JTextField teamField = new JTextField(10);

        JButton insertButton = new JButton("Insert");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");

        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Age:"));
        inputPanel.add(ageField);
        inputPanel.add(new JLabel("Jersey:"));
        inputPanel.add(jerseyField);
        inputPanel.add(new JLabel("Position:"));
        inputPanel.add(positionField);
        inputPanel.add(new JLabel("Nationality:"));
        inputPanel.add(nationalityField);

        buttonPanel.add(insertButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        controlPanel.add(inputPanel);
        controlPanel.add(buttonPanel);

        panel.add(controlPanel, BorderLayout.SOUTH);

        // Button actions
        insertButton.addActionListener(e -> {
            try {
                int playerId = Integer.parseInt(idField.getText());
                String playerName = nameField.getText();
                int playerAge = Integer.parseInt(ageField.getText());
                int jerseyNumber = Integer.parseInt(jerseyField.getText());
                String position = positionField.getText();
                String nationality = nationalityField.getText();

                Player player = new Player(
                    playerId,
                    playerName,
                    playerAge,
                    jerseyNumber,
                    position,
                    nationality
                );

                try (Connection conn = getConnection()) {
                    if (conn == null) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Connection is null. Cannot insert data."
                        );
                        return;
                    }

                    String query =
                        "INSERT INTO NEW_PLAYER (player_id, player_name, player_age, player_jersey, player_position, nationality) VALUES (?, ?, ?, ?, ?, ?)";
                    try (
                        PreparedStatement pstmt = conn.prepareStatement(query)
                    ) {
                        pstmt.setInt(1, player.playerID);
                        pstmt.setString(2, player.playerName);
                        pstmt.setInt(3, player.playerAge);
                        pstmt.setInt(4, player.playerJersey);
                        pstmt.setString(5, player.playerPosition);
                        pstmt.setString(6, player.nationality);

                        pstmt.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "Player added.");

                        idField.setText("");
                        nameField.setText("");
                        ageField.setText("");
                        jerseyField.setText("");
                        positionField.setText("");
                        nationalityField.setText("");
                        teamField.setText("");
                    } catch (SQLException error) {
                        error.printStackTrace();
                        JOptionPane.showMessageDialog(
                            frame,
                            "Cannot insert into new_player table."
                        );
                    }
                } catch (SQLException error) {
                    error.printStackTrace();
                    JOptionPane.showMessageDialog(
                        frame,
                        "Cannot connect to database."
                    );
                }
            } catch (NumberFormatException error) {
                error.printStackTrace();
                JOptionPane.showMessageDialog(
                    frame,
                    "PlayerAge and/or JerseyNumber in invalid format"
                );
            }
        });

        updateButton.addActionListener(e -> {
            try (Connection conn = getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "Connection is null. Cannot fetch data."
                    );
                    return;
                }

                String query =
                    "SELECT player_id, player_name, player_age, player_jersey, player_position, nationality FROM NEW_PLAYER";
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(query);
                    model.setRowCount(0); // Clear table
                    while (rs.next()) {
                        model.addRow(
                            new Object[] {
                                rs.getInt("player_id"),
                                rs.getString("player_name"),
                                rs.getInt("player_age"),
                                rs.getInt("player_jersey"),
                                rs.getString("player_position"),
                                rs.getString("nationality"),
                            }
                        );
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error fetching data!");
            }
        });

        deleteButton.addActionListener(e -> {
            try {
                int playerId = Integer.parseInt(idField.getText());

                try (Connection conn = getConnection()) {
                    if (conn == null) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Error deleting data."
                        );
                        return;
                    }
                    String query = "DELETE FROM NEW_PLAYER WHERE player_id = ?";
                    try (
                        PreparedStatement pstmt = conn.prepareStatement(query)
                    ) {
                        pstmt.setInt(1, playerId);
                        int rowsAffected = pstmt.executeUpdate();
                        conn.commit();

                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(
                                frame,
                                "Deleted player."
                            );
                            idField.setText("");
                            updateButton.doClick();
                        } else {
                            JOptionPane.showMessageDialog(
                                frame,
                                "Player not found."
                            );
                        }
                    }
                }
            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Player ID format invalid."
                );
            } catch (SQLException error) {
                error.printStackTrace();
                JOptionPane.showMessageDialog(frame, error.getMessage());
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }
}
