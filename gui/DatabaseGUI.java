import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DatabaseGUI extends JFrame{
    private JTextArea textArea;
    private JTextField queryField;
    private JButton executeButton;

    public void SQLOracleGUI() {
        setTitle("SoccerLeagueDatabase");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        queryField = new JTextField("Enter Query");
        executeButton = new JButton("Execute Query");
        textArea = new JTextArea();
        textArea.setEditable(false);
        
        // Add components to the JFrame
        add(queryField, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(executeButton, BorderLayout.SOUTH);
        
        // Add action listener to the button
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryField.getText();
                //executeQuery(query);
            }
        });
        setVisible(true);
    }

    public static void main(String[] args){
        DatabaseGUI GUI = new DatabaseGUI();
        GUI.SQLOracleGUI();
    }
}