package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends Thread implements ActionListener {

    private final JButton enter = new JButton("Send");           //Send button
    private final JButton exit = new JButton("Shutdown");        //Shutdown button
    private final Instructor thisInstructor;                      //Instructor object

    final JFrame frame = new JFrame();                                //JFrame object
    final JPanel panel = new JPanel();                                //JPanel object
    final JLabel label = new JLabel("Select a task length: ");        //JLabel object
    final JLabel label2 = new JLabel("Or enter a custom number: ");   //JLabel object
    final JTextField textField = new JTextField();                    //JTextField object

    /**
     * Constructor for the GUI class, sets properties about the buttons and adds them to the JPanel
     * Then adds the JPanel to the JFrame
     * @param sendingDevice - Instructor object used for sending messages
     */
    public GUI (Instructor sendingDevice){
        thisInstructor = sendingDevice;
        panel.setLayout(new GridLayout());
        JButton fiveSecondTask = new JButton("5 Second Task");
        fiveSecondTask.setActionCommand("5");
        fiveSecondTask.addActionListener(this);
        fiveSecondTask.setToolTipText("Send a job of 5 seconds to the Server");
        JButton tenSecondTask = new JButton("10 Second Task");
        tenSecondTask.setActionCommand("10");
        tenSecondTask.addActionListener(this);
        tenSecondTask.setToolTipText("Send a job of 10 seconds to the Server");
        JButton fifteenSecondTask = new JButton("15 Second Task");
        fifteenSecondTask.setActionCommand("15");
        fifteenSecondTask.addActionListener(this);
        fifteenSecondTask.setToolTipText("Send a job of 15 seconds to the Server");
        JButton twentySecondTask = new JButton("20 Second Task");
        twentySecondTask.setActionCommand("20");
        twentySecondTask.addActionListener(this);
        twentySecondTask.setToolTipText("Send a job of 20 seconds to the Server");
        enter.addActionListener(this);
        enter.setToolTipText("Send the job length in the custom field");
        exit.addActionListener(this);
        exit.setToolTipText("Shuts down the Server and closes the GUI");

        panel.add(label);
        panel.add(fiveSecondTask);
        panel.add(tenSecondTask);
        panel.add(fifteenSecondTask);
        panel.add(twentySecondTask);
        panel.add(label2);
        panel.add(textField);
        panel.add(enter);
        panel.add(exit);
        Dimension background = new Dimension(500, 500);
        panel.setMinimumSize(background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * used to send new work to the Server
     * @param duration - length of time in seconds for the job to be
     */
    private void newWork(int duration){
        thisInstructor.sendNewWork(duration);
    }

    /**
     * sends the SHUTDOWN command to the server to shut it down
     */
    private void shutdown() {
        thisInstructor.shutdown();
        System.exit(0);
    }

    /**
     * Overrides the default actionPerformed method - when a button is pressed, it will get the amount of time from the ActionCommand
     * If the button was Send
     * @param event - the event that occurs when a button is pressed
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == enter) {
            try {
                if (textField.getText().isEmpty()) {
                    textField.setBackground(Color.white);
                    textField.setText("");
                }
                else if (Integer.parseInt(textField.getText().trim()) > 0 && Integer.parseInt(textField.getText().trim()) < 2147483647) {
                    textField.setBackground(Color.green);
                    newWork(Integer.parseInt(textField.getText().trim()));
                    textField.setText("");
                } else {
                    textField.setBackground(Color.red);
                    textField.setText("");
                }
            } catch (NumberFormatException e) {
                System.out.println("That's not a valid number!");
                textField.setText("");
                textField.setBackground(Color.red);
            }

        } else if (event.getSource() == exit) {
            shutdown();
        } else {
            int duration = Integer.parseInt(event.getActionCommand().trim());
            newWork(duration);
        }
    }
}
