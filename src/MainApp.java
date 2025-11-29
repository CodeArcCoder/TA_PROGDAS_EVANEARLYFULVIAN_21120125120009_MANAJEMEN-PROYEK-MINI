import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

class Project {
    protected String name;
    protected String deadline;
    protected String status;

    public Project(String name, String deadline, String status) {
        this.name = name;
        this.deadline = deadline;
        this.status = status;
    }

    public String[] getData() {
        return new String[]{name, deadline, status};
    }

    public void update(String name, String deadline, String status) {
        this.name = name;
        this.deadline = deadline;
        this.status = status;
    }
}

class ImportantProject extends Project {
    public ImportantProject(String name, String deadline, String status) {
        super(name, deadline, status);
    }

    @Override
    public String[] getData() {
        return new String[]{name + " (PRIORITAS)", deadline, status};
    }
}

class RoundedButton extends JButton {
    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;
    private Color normalBackgroundColor;

    public RoundedButton(String text, Color bg) {
        super(text);
        super.setContentAreaFilled(false);
        this.normalBackgroundColor = bg;
        this.hoverBackgroundColor = bg.brighter();
        this.pressedBackgroundColor = bg.darker();

        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setFocusPainted(false);
        setBorder(new EmptyBorder(10, 20, 10, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2.setColor(pressedBackgroundColor);
        } else if (getModel().isRollover()) {
            g2.setColor(hoverBackgroundColor);
        } else {
            g2.setColor(normalBackgroundColor);
        }

        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
        g2.dispose();

        super.paintComponent(g);
    }
}

class ModernComboBoxUI extends BasicComboBoxUI {

    private final Border defaultComboBoxBorder = new CompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(0, 5, 0, 0)
    );

    @Override
    protected ComboPopup createPopup() {
        BasicComboPopup popup = new BasicComboPopup(comboBox);
        popup.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        return popup;
    }

    @Override
    protected JButton createArrowButton() {
        JButton button = new BasicArrowButton(
                BasicArrowButton.SOUTH,
                new Color(240, 240, 240),
                new Color(240, 240, 240),
                Color.GRAY,
                new Color(240, 240, 240)
        );
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return button;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setBorder(defaultComboBoxBorder);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (comboBox.hasFocus()) {
            c.setBorder(new CompoundBorder(
                    new LineBorder(new Color(52, 152, 219), 1),
                    new EmptyBorder(0, 5, 0, 0)
            ));
        } else {
            c.setBorder(defaultComboBoxBorder);
        }
        super.paint(g, c);
    }
}

class CustomTableRenderer extends DefaultTableCellRenderer {

    private final Color COLOR_EVEN = new Color(255, 255, 255);
    private final Color COLOR_ODD = new Color(248, 249, 250);
    private final Color COLOR_SELECTED = new Color(232, 240, 254);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBorder(new EmptyBorder(5, 10, 5, 10));

        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? COLOR_EVEN : COLOR_ODD);
            c.setForeground(Color.DARK_GRAY);
        } else {
            c.setBackground(COLOR_SELECTED);
            c.setForeground(new Color(13, 71, 161));
        }

        if (column == 2) {
            String status = (String) value;
            setFont(getFont().deriveFont(Font.BOLD));
            if ("Selesai".equalsIgnoreCase(status)) {
                c.setForeground(new Color(39, 174, 96));
            } else if ("Berjalan".equalsIgnoreCase(status)) {
                c.setForeground(new Color(41, 128, 185));
            } else {
                c.setForeground(new Color(127, 140, 141));
            }
        } else if (column == 0) {
            if (value.toString().contains("(PRIORITAS)")) {
                c.setForeground(new Color(192, 57, 43));
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
        } else {
            setFont(getFont().deriveFont(Font.PLAIN));
        }

        return c;
    }
}

public class MainApp extends JFrame {

    private final Color COLOR_PRIMARY = new Color(52, 152, 219);
    private final Color COLOR_DANGER = new Color(231, 76, 60);
    private final Color COLOR_SUCCESS = new Color(46, 204, 113);
    private final Color COLOR_WARNING = new Color(243, 156, 18);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_HEADER = new Color(44, 62, 80);
    private final Color COLOR_WHITE = Color.WHITE;

    private final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private DefaultTableModel model;
    private JTable table;
    private JScrollPane tableScrollPane;
    private final ArrayList<Project> projectList = new ArrayList<>();
    private final List<Integer> visibleIndexes = new ArrayList<>();

    private JTextField tfNama, tfSearch;
    private JComboBox<String> cbStatus, cbJenis, cbFilterStatus;
    private JComboBox<Integer> cbHari, cbTahun;
    private JComboBox<String> cbBulan;
    private JButton btnTambah, btnEdit, btnHapus, btnSimpan, btnBatalEdit;
    private int selectedProjectIndex = -1;

    public MainApp() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignore) {}

        setTitle("Project Management Dashboard");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);
        setContentPane(mainPanel);

        JPanel headerPanel = createHeader();
        JPanel contentPanel = createContentTable();
        JPanel formContainer = createFormCard();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(formContainer, BorderLayout.SOUTH);


        updateTableFiltered();

        setupListeners(mainPanel, headerPanel, contentPanel, formContainer);

        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(new EmptyBorder(25, 40, 25, 40));

        JLabel title = new JLabel("Manajemen Proyek Mini");
        title.setFont(FONT_HEADER);
        title.setForeground(COLOR_WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        searchPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Status:");
        lblFilter.setForeground(new Color(200, 200, 200));
        lblFilter.setFont(FONT_NORMAL);

        cbFilterStatus = styleComboBox(new String[]{"Semua", "Belum Mulai", "Berjalan", "Selesai"});
        cbFilterStatus.setPreferredSize(new Dimension(130, 30));
        cbFilterStatus.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(0, 5, 0, 0)));


        JLabel lblSearch = new JLabel("Cari:");
        lblSearch.setForeground(new Color(200, 200, 200));
        lblSearch.setFont(FONT_NORMAL);

        tfSearch = styleTextField();
        tfSearch.setPreferredSize(new Dimension(180, 30));

        searchPanel.add(lblFilter);
        searchPanel.add(cbFilterStatus);
        searchPanel.add(lblSearch);
        searchPanel.add(tfSearch);

        headerPanel.add(searchPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createContentTable() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(20, 40, 10, 40));

        model = new DefaultTableModel(new String[]{"Nama Proyek", "Deadline", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setFont(FONT_NORMAL);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new CustomTableRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader theader = table.getTableHeader();
        theader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        theader.setBackground(COLOR_WHITE);
        theader.setForeground(new Color(100, 100, 100));
        theader.setBorder(new MatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
        theader.setPreferredSize(new Dimension(100, 45));

        theader.setReorderingAllowed(false);

        ((DefaultTableCellRenderer) theader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(COLOR_WHITE);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(COLOR_WHITE);
        tableCard.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        tableCard.add(tableScrollPane);

        contentPanel.add(tableCard, BorderLayout.CENTER);
        return contentPanel;
    }

    private JPanel createFormCard() {
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(COLOR_BG);
        formContainer.setBorder(new EmptyBorder(10, 40, 30, 40));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(COLOR_WHITE);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(25, 25, 25, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        formCard.add(createLabel("Nama Proyek"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.9;
        tfNama = styleTextField();
        tfNama.setText("");
        formCard.add(tfNama, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        formCard.add(createLabel("Deadline"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.9;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        datePanel.setOpaque(false);

        cbHari = styleComboBox(null);
        cbHari.setPreferredSize(new Dimension(80, 35));
        for (int i = 1; i <= 31; i++) cbHari.addItem(i);
        cbHari.setSelectedIndex(0);

        cbBulan = styleComboBox(new String[]{
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        });
        cbBulan.setPreferredSize(new Dimension(130, 35));
        cbBulan.setSelectedIndex(0);

        cbTahun = styleComboBox(null);
        cbTahun.setPreferredSize(new Dimension(100, 35));
        for (int i = 2025; i <= 2030; i++) cbTahun.addItem(i);
        cbTahun.setSelectedIndex(0);

        datePanel.add(cbHari);
        datePanel.add(cbBulan);
        datePanel.add(cbTahun);

        formCard.add(datePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.1;
        formCard.add(createLabel("Status"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.9;
        JPanel row3 = new JPanel(new GridBagLayout());
        row3.setOpaque(false);
        GridBagConstraints gbcIn = new GridBagConstraints();
        gbcIn.fill = GridBagConstraints.HORIZONTAL;
        gbcIn.insets = new Insets(0, 0, 0, 15);

        gbcIn.weightx = 0.4;
        cbStatus = styleComboBox(new String[]{"Belum Mulai", "Berjalan", "Selesai"});
        cbStatus.setPreferredSize(new Dimension(150, 35));
        cbStatus.setSelectedIndex(0);
        row3.add(cbStatus, gbcIn);

        gbcIn.weightx = 0.1;
        row3.add(createLabel("Prioritas:"), gbcIn);

        gbcIn.weightx = 0.4;
        cbJenis = styleComboBox(new String[]{"Biasa", "Penting"});
        cbJenis.setPreferredSize(new Dimension(150, 35));
        cbJenis.setSelectedIndex(0);
        row3.add(cbJenis, gbcIn);

        formCard.add(row3, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 15, 5, 15);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        btnTambah = new RoundedButton("Tambah Data", COLOR_SUCCESS);
        btnSimpan = new RoundedButton("Simpan Perubahan", COLOR_PRIMARY);
        btnEdit = new RoundedButton("Edit", COLOR_WARNING);
        btnHapus = new RoundedButton("Hapus", COLOR_DANGER);
        btnBatalEdit = new RoundedButton("Batal", Color.GRAY);

        btnTambah.setVisible(true);
        btnEdit.setVisible(false);
        btnHapus.setVisible(false);
        btnSimpan.setVisible(false);
        btnBatalEdit.setVisible(false);

        enableForm(true);

        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBatalEdit);
        btnPanel.add(btnSimpan);

        formCard.add(btnPanel, gbc);
        formContainer.add(formCard, BorderLayout.CENTER);

        return formContainer;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField styleTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_NORMAL);
        Border defaultBorder = new CompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 10, 8, 10));

        tf.setBorder(defaultBorder);
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                        new LineBorder(COLOR_PRIMARY, 1),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(defaultBorder);
            }
        });
        return tf;
    }

    private <T> JComboBox<T> styleComboBox(T[] data) {
        JComboBox<T> cb = (data != null) ? new JComboBox<>(data) : new JComboBox<>();
        cb.setFont(FONT_NORMAL);
        cb.setBackground(COLOR_WHITE);
        cb.setUI(new ModernComboBoxUI());

        cb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(7, 10, 7, 10));
                if (isSelected) {
                    setBackground(new Color(235, 245, 251));
                    setForeground(COLOR_PRIMARY);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.DARK_GRAY);
                }
                return this;
            }
        });
        return cb;
    }

    private void addGlobalCancelListener(Container container) {
        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedProjectIndex != -1) {
                    Component deepest = SwingUtilities.getDeepestComponentAt(e.getComponent(), e.getX(), e.getY());

                    boolean isInteractiveComponent = deepest instanceof AbstractButton ||
                            deepest instanceof JTextComponent ||
                            deepest instanceof JComboBox ||
                            deepest instanceof JTable ||
                            (deepest instanceof JComponent && deepest.getParent() instanceof JTableHeader) ||
                            (deepest == tableScrollPane.getViewport());

                    if (!isInteractiveComponent) {
                        if (table.getSelectedRow() != -1) {
                            table.clearSelection();
                        } else {
                            clearForm();
                        }
                    }
                }
            }
        });
    }

    private void setupListeners(JPanel mainPanel, JPanel headerPanel, JPanel contentPanel, JPanel formContainer) {
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTableFiltered(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTableFiltered(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTableFiltered(); }
        });

        cbFilterStatus.addActionListener(e -> updateTableFiltered());

        table.getSelectionModel().addListSelectionListener(e -> pilihBaris());

        btnTambah.addActionListener(e -> tambahProyek());
        btnEdit.addActionListener(e -> masukModeEdit());
        btnHapus.addActionListener(e -> hapusProyek());
        btnSimpan.addActionListener(e -> simpanPerubahan());
        btnBatalEdit.addActionListener(e -> batalkanEdit());

        tableScrollPane.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row == -1 && selectedProjectIndex != -1) {
                    table.clearSelection();
                }
            }
        });

        addGlobalCancelListener(headerPanel);
        addGlobalCancelListener(mainPanel);
        addGlobalCancelListener(contentPanel);
        addGlobalCancelListener(formContainer);

        JPanel formCard = (JPanel) formContainer.getComponent(0);
        addGlobalCancelListener(formCard);

        Component[] formCardComponents = formCard.getComponents();
        for (Component c : formCardComponents) {
            if (c instanceof Container) {
                addGlobalCancelListener((Container) c);
            }
        }
    }

    private void enableForm(boolean enabled) {
        tfNama.setEnabled(enabled);
        cbHari.setEnabled(enabled);
        cbBulan.setEnabled(enabled);
        cbTahun.setEnabled(enabled);
        cbStatus.setEnabled(enabled);
        cbJenis.setEnabled(enabled);
    }

    private void pilihBaris() {
        if (table.getSelectionModel().getValueIsAdjusting()) {
            return;
        }

        int tableRow = table.getSelectedRow();
        if (tableRow == -1 || tableRow >= visibleIndexes.size()) {
            clearForm();
            return;
        }

        selectedProjectIndex = visibleIndexes.get(tableRow);
        Project p = projectList.get(selectedProjectIndex);

        tfNama.setText(p.name.replace(" (PRIORITAS)", ""));
        String[] comps = parseDeadlineComponents(p.deadline);

        if (comps != null) {
            cbHari.setSelectedItem(Integer.parseInt(comps[0]));
            cbBulan.setSelectedItem(comps[1]);
            cbTahun.setSelectedItem(Integer.parseInt(comps[2]));
        }

        cbStatus.setSelectedItem(p.status);
        cbJenis.setSelectedItem(p instanceof ImportantProject ? "Penting" : "Biasa");

        btnEdit.setVisible(true);
        btnHapus.setVisible(true);
        btnTambah.setVisible(false);
        btnSimpan.setVisible(false);
        btnBatalEdit.setVisible(false);
        enableForm(false);
    }

    private void tambahProyek() {
        if (tfNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String d = createDeadlineString();
        Project p = (cbJenis.getSelectedItem().equals("Penting"))
                ? new ImportantProject(tfNama.getText().trim(), d, cbStatus.getSelectedItem().toString())
                : new Project(tfNama.getText().trim(), d, cbStatus.getSelectedItem().toString());

        projectList.add(p);
        updateTableFiltered();

        clearForm();

        tfNama.requestFocusInWindow();
    }

    private void masukModeEdit() {
        enableForm(true);
        btnEdit.setVisible(false);
        btnHapus.setVisible(false);
        btnTambah.setVisible(false);
        btnSimpan.setVisible(true);
        btnBatalEdit.setVisible(true);
        tfNama.requestFocus();
    }

    private void simpanPerubahan() {
        if (selectedProjectIndex == -1) return;
        if (tfNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Project p = projectList.get(selectedProjectIndex);
        String d = createDeadlineString();
        String newName = tfNama.getText().trim();
        String newStatus = cbStatus.getSelectedItem().toString();

        boolean isImportantNow = cbJenis.getSelectedItem().equals("Penting");
        boolean wasImportant = p instanceof ImportantProject;

        if (isImportantNow != wasImportant) {
            Project newProject = isImportantNow
                    ? new ImportantProject(newName, d, newStatus)
                    : new Project(newName, d, newStatus);
            projectList.set(selectedProjectIndex, newProject);
        } else {
            p.update(newName, d, newStatus);
        }

        updateTableFiltered();

        int selectedRow = -1;
        for (int i = 0; i < visibleIndexes.size(); i++) {
            if (visibleIndexes.get(i) == selectedProjectIndex) {
                selectedRow = i;
                break;
            }
        }

        clearForm();
        if(selectedRow != -1) {
            final int finalSelectedRow = selectedRow;
            SwingUtilities.invokeLater(() -> table.getSelectionModel().setSelectionInterval(finalSelectedRow, finalSelectedRow));
        }
    }

    private void hapusProyek() {
        if (selectedProjectIndex == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus proyek ini?", "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            projectList.remove(selectedProjectIndex);
            updateTableFiltered();
            clearForm();
        }
    }

    private void batalkanEdit() {
        table.clearSelection();
    }

    private void updateTableFiltered() {
        model.setRowCount(0);
        visibleIndexes.clear();

        String s = tfSearch.getText().toLowerCase().trim();
        String f = cbFilterStatus.getSelectedItem().toString();

        for (int i = 0; i < projectList.size(); i++) {
            Project p = projectList.get(i);
            boolean matchSearch = p.name.toLowerCase().contains(s);
            boolean matchFilter = f.equals("Semua") || p.status.equals(f);

            if (matchSearch && matchFilter) {
                model.addRow(p.getData());
                visibleIndexes.add(i);
            }
        }

        if (table.getRowCount() == 0) {
            if (selectedProjectIndex != -1) {
                clearForm();
            }
        }
    }

    private void clearForm() {
        tfNama.setText("");
        cbHari.setSelectedIndex(0);
        cbBulan.setSelectedIndex(0);
        cbTahun.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
        cbJenis.setSelectedIndex(0);

        enableForm(true);
        table.clearSelection();
        selectedProjectIndex = -1;

        btnEdit.setVisible(false);
        btnHapus.setVisible(false);
        btnSimpan.setVisible(false);
        btnBatalEdit.setVisible(false);
        btnTambah.setVisible(true);
    }

    private String createDeadlineString() {
        return cbHari.getSelectedItem() + " " + cbBulan.getSelectedItem() + " " + cbTahun.getSelectedItem();
    }

    private String[] parseDeadlineComponents(String deadline) {
        String[] parts = deadline.split(" ");
        if (parts.length != 3) return null;
        try {
            Integer.parseInt(parts[0]);
            Integer.parseInt(parts[2]);
            return parts;
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}