package valor.agent;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ValorLauncher extends JFrame {
    private final JTable processTable;
    private final DefaultTableModel tableModel;
    private final JTextField jarPathField;
    private final JTextArea logArea;
    private final JButton injectButton;
    private final JButton refreshButton;
    private final JLabel statusLabel;
    private final List<ValorAgent.MinecraftProcess> currentProcesses = new ArrayList<ValorAgent.MinecraftProcess>();
    private String agentJarPath;

    public ValorLauncher(String initialJarPath) {
        super("Open Valor Client Launcher & Injector");
        this.agentJarPath = initialJarPath;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 560);
        setMinimumSize(new Dimension(680, 480));
        setLocationRelativeTo(null);

        Color bgDark = new Color(24, 24, 37);
        Color bgCard = new Color(30, 30, 46);
        Color bgCardLighter = new Color(49, 50, 68);
        Color accentColor = new Color(137, 180, 250);
        Color textColor = new Color(205, 214, 244);
        Color textMuted = new Color(166, 173, 200);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(bgDark);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(bgDark);

        JLabel titleLabel = new JLabel("OPEN VALOR INJECTOR");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(accentColor);

        JLabel subtitleLabel = new JLabel("Select the Minecraft instance to inject Open Valor into");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(textMuted);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(bgDark);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        String[] columns = {"PID", "Client / Launcher", "Player", "Version"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        processTable = new JTable(tableModel);
        processTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        processTable.setRowHeight(28);
        processTable.setBackground(bgCard);
        processTable.setForeground(textColor);
        processTable.setGridColor(bgCardLighter);
        processTable.setSelectionBackground(new Color(69, 71, 90));
        processTable.setSelectionForeground(Color.WHITE);
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        processTable.getTableHeader().setBackground(bgCardLighter);
        processTable.getTableHeader().setForeground(textColor);

        processTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        processTable.getColumnModel().getColumn(0).setMaxWidth(100);
        processTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        processTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        processTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        processTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        processTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(processTable);
        tableScroll.setBorder(new LineBorder(bgCardLighter, 1, true));
        tableScroll.getViewport().setBackground(bgCard);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.55;
        centerPanel.add(tableScroll, gbc);

        JPanel jarBar = new JPanel(new BorderLayout(8, 0));
        jarBar.setBackground(bgDark);
        jarBar.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel jarLabel = new JLabel("Agent JAR:");
        jarLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jarLabel.setForeground(textMuted);
        jarBar.add(jarLabel, BorderLayout.WEST);

        jarPathField = new JTextField(agentJarPath != null ? agentJarPath : "");
        jarPathField.setFont(new Font("Consolas", Font.PLAIN, 12));
        jarPathField.setBackground(bgCard);
        jarPathField.setForeground(textColor);
        jarPathField.setCaretColor(Color.WHITE);
        jarPathField.setBorder(new CompoundBorder(new LineBorder(bgCardLighter, 1), new EmptyBorder(4, 6, 4, 6)));
        jarBar.add(jarPathField, BorderLayout.CENTER);

        JButton browseJarBtn = new JButton("Browse...");
        browseJarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        browseJarBtn.setFocusPainted(false);
        browseJarBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(new File(jarPathField.getText()).getParentFile());
            chooser.setDialogTitle("Select Open Valor JAR");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                jarPathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        jarBar.add(browseJarBtn, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        centerPanel.add(jarBar, gbc);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(17, 17, 27));
        logArea.setForeground(new Color(166, 227, 161));
        logArea.setCaretColor(Color.WHITE);
        logArea.setMargin(new Insets(6, 8, 6, 8));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new LineBorder(bgCardLighter, 1, true));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.45;
        centerPanel.add(logScroll, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(bgDark);
        bottomPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(textMuted);
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(bgDark);

        refreshButton = new JButton("Refresh Instances");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshProcesses());
        buttonPanel.add(refreshButton);

        injectButton = new JButton("Inject Open Valor");
        injectButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        injectButton.setBackground(accentColor);
        injectButton.setForeground(new Color(17, 17, 27));
        injectButton.setFocusPainted(false);
        injectButton.setOpaque(true);
        injectButton.addActionListener(e -> performInject());
        buttonPanel.add(injectButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        redirectSystemStreams();

        refreshProcesses();
    }

    private void redirectSystemStreams() {
        OutputStream outStream = new OutputStream() {
            @Override
            public void write(int b) {
                appendLog(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                appendLog(new String(b, off, len));
            }
        };
        PrintStream printStream = new PrintStream(outStream, true);
        System.setOut(printStream);
        System.setErr(printStream);
    }

    private void appendLog(final String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void refreshProcesses() {
        statusLabel.setText("Scanning for Minecraft processes...");
        refreshButton.setEnabled(false);

        new SwingWorker<List<ValorAgent.MinecraftProcess>, Void>() {
            @Override
            protected List<ValorAgent.MinecraftProcess> doInBackground() {
                return ValorAgent.findMinecraftProcesses();
            }

            @Override
            protected void done() {
                try {
                    currentProcesses.clear();
                    tableModel.setRowCount(0);
                    List<ValorAgent.MinecraftProcess> procs = get();
                    currentProcesses.addAll(procs);

                    for (ValorAgent.MinecraftProcess p : procs) {
                        tableModel.addRow(new Object[]{
                                p.pid,
                                p.clientType,
                                p.username != null ? p.username : "-",
                                p.version != null ? p.version : "1.8.9"
                        });
                    }

                    if (procs.isEmpty()) {
                        statusLabel.setText("No Minecraft processes detected. Start the game first.");
                        injectButton.setEnabled(false);
                    } else {
                        statusLabel.setText("Found " + procs.size() + " Minecraft instance(s). Select one and click Inject.");
                        processTable.setRowSelectionInterval(0, 0);
                        injectButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error querying processes: " + ex.getMessage());
                } finally {
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void performInject() {
        int selectedRow = processTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentProcesses.size()) {
            JOptionPane.showMessageDialog(this, "Please select a Minecraft instance from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final ValorAgent.MinecraftProcess target = currentProcesses.get(selectedRow);
        final String jarPath = jarPathField.getText().trim();

        if (jarPath.isEmpty() || !new File(jarPath).exists()) {
            JOptionPane.showMessageDialog(this, "The specified JAR file does not exist:\n" + jarPath, "Invalid JAR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        injectButton.setEnabled(false);
        refreshButton.setEnabled(false);
        statusLabel.setText("Injecting into PID " + target.pid + " (" + target.clientType + ")...");
        logArea.append("\n[Launcher] >>> Attempting standard attach on PID " + target.pid + "...\n");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return ValorAgent.inject(target.pid, jarPath);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        statusLabel.setText("Injection succeeded for PID " + target.pid + "!");
                        logArea.append("[Launcher] >>> SUCCESS: Open Valor agent injected successfully!\n");
                    } else {
                        statusLabel.setText("Injection failed. Check log for details.");
                        logArea.append("[Launcher] >>> FAILED: All injection vectors failed.\n");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Injection error: " + e.getMessage());
                    logArea.append("[Launcher] >>> ERROR: " + e.getMessage() + "\n");
                } finally {
                    injectButton.setEnabled(true);
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }

    public static void showGui(final String jarPath) {
        SwingUtilities.invokeLater(() -> {
            ValorLauncher launcher = new ValorLauncher(jarPath);
            launcher.setVisible(true);
        });
    }

    public static void main(String[] args) {
        String jarPath = null;
        if (args.length > 0) {
            jarPath = args[0];
        }
        showGui(jarPath);
    }
}
