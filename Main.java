import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;

public class Main extends JFrame {
    private JButton openButton;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton exitButton;
    private JToggleButton darkModeButton;
    
    private JLabel fileName;
    private JPanel mainPanel;
    private JPanel controlPanel;
    private JPanel utilityPanel;
    private JPanel panel;
    
    private Clip clip;
    private AudioInputStream audioStream;
    private File audioFile;
    private long clipPosition = 0;
    
    // Color scheme
    private Color lightBackground = new Color(245, 245, 250);
    private Color darkBackground = new Color(30, 30, 35);
    private Color lightForeground = new Color(10, 10, 10);
    private Color darkForeground = new Color(220, 220, 225);
    
    // Button colors
    private final Color playPauseColor = new Color(50, 205, 50);
    private final Color stopColor = new Color(220, 20, 60);
    private final Color darkButtonColor = new Color(60, 60, 70);
    private final Color exitColor = new Color(100, 100, 100);
    
    private boolean isDarkMode = false;
    
    public Main() {
        super("AudioWave Player");
        setSize(500, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10));
        
        fileName = new JLabel("No file opened", SwingConstants.CENTER);
        fileName.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(fileName);
        
        openButton = createButton("Open File", null);
        openButton.setFont(new Font("Arial", Font.PLAIN, 16));
        openButton.addActionListener(e -> openAudioFile());
        panel.add(openButton);
        
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
        
        playButton = createButton("▶", playPauseColor);
        playButton.setPreferredSize(new Dimension(50, 30));
        playButton.addActionListener(e -> playAudio());
        controlPanel.add(playButton);
        
        pauseButton = createButton("⏸", playPauseColor);
        pauseButton.setPreferredSize(new Dimension(50, 30));
        pauseButton.addActionListener(e -> pauseAudio());
        controlPanel.add(pauseButton);
        
        stopButton = createButton("⏹", stopColor);
        stopButton.setPreferredSize(new Dimension(50, 30));
        stopButton.addActionListener(e -> stopAudio());
        controlPanel.add(stopButton);
        
        panel.add(controlPanel);
        
        utilityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        
        darkModeButton = new JToggleButton("Dark Mode");
        darkModeButton.setFocusPainted(false);
        darkModeButton.setPreferredSize(new Dimension(120, 30));
        darkModeButton.addActionListener(e -> toggleDarkMode());
        utilityPanel.add(darkModeButton);
        
        exitButton = createButton("Exit", exitColor);
        exitButton.setPreferredSize(new Dimension(80, 30));
        exitButton.addActionListener(e -> exitApplication());
        utilityPanel.add(exitButton);
        
        panel.add(utilityPanel);
        
        mainPanel.add(panel, BorderLayout.CENTER);
        add(mainPanel);
        
        // Set initial appearance
        updateComponentStyles(false);
        
        setVisible(true);
    }
    
    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        if (bgColor != null) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
        }
        button.setFocusPainted(false);
        return button;
    }
    
    private void updateComponentStyles(boolean darkMode) {
        this.isDarkMode = darkMode;
        
        // Set background colors
        Color bgColor = darkMode ? darkBackground : lightBackground;
        Color textColor = darkMode ? darkForeground : lightForeground;
        
        // Update container backgrounds
        mainPanel.setBackground(bgColor);
        panel.setBackground(bgColor);
        controlPanel.setBackground(bgColor);
        utilityPanel.setBackground(bgColor);
        getContentPane().setBackground(bgColor);
        
        // Update text colors
        fileName.setForeground(textColor);
        
        // Update regular buttons that should change with theme
        if (darkMode) {
            // Open button
            openButton.setBackground(darkButtonColor);
            openButton.setForeground(darkForeground);
            
            // Dark mode toggle
            darkModeButton.setText("Light Mode");
            darkModeButton.setBackground(darkButtonColor);
            darkModeButton.setForeground(darkForeground);
        } else {
            // Open button
            openButton.setBackground(UIManager.getColor("Button.background"));
            openButton.setForeground(lightForeground);
            
            // Dark mode toggle
            darkModeButton.setText("Dark Mode");
            darkModeButton.setBackground(UIManager.getColor("ToggleButton.background"));
            darkModeButton.setForeground(lightForeground);
        }
        
        // Always ensure colored buttons maintain their colors
        playButton.setBackground(playPauseColor);
        pauseButton.setBackground(playPauseColor);
        stopButton.setBackground(stopColor);
        exitButton.setBackground(exitColor);
        
        // Ensure white text on colored buttons
        playButton.setForeground(Color.WHITE);
        pauseButton.setForeground(Color.WHITE);
        stopButton.setForeground(Color.WHITE);
        exitButton.setForeground(Color.WHITE);
        
        // Fix button borders if needed
        if (darkMode) {
            fixButtonBorders();
        }
        
        repaint();
        revalidate();
    }
    
    private void fixButtonBorders() {
        // Simple flat border for dark mode
        javax.swing.border.Border flatBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 80), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        );
        
        // Apply to all buttons
        openButton.setBorder(flatBorder);
        playButton.setBorder(flatBorder);
        pauseButton.setBorder(flatBorder);
        stopButton.setBorder(flatBorder);
        exitButton.setBorder(flatBorder);
        darkModeButton.setBorder(flatBorder);
    }
    
    private void openAudioFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            audioFile = fileChooser.getSelectedFile();
            fileName.setText("Selected file: " + audioFile.getName());
            
            try {
                if (clip != null && clip.isOpen()) clip.close();
                audioStream = AudioSystem.getAudioInputStream(audioFile);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                clipPosition = 0;
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + exception.getMessage());
            }
        }
    }
    
    private void playAudio() {
        if (clip != null) {
            clip.setMicrosecondPosition(clipPosition);
            clip.start();
        }
    }
    
    private void pauseAudio() {
        if (clip != null && clip.isRunning()) {
            clipPosition = clip.getMicrosecondPosition();
            clip.stop();
        }
    }
    
    private void stopAudio() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
            clipPosition = 0;
        }
    }
    
    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            dispose();
            System.exit(0);
        }
    }
    
    private void toggleDarkMode() {
        updateComponentStyles(darkModeButton.isSelected());
    }
    
    public static void main(String[] args) {
        try {
            // Try to use system look and feel by default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new Main());
    }
}