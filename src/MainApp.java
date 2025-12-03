import javax.swing.*; // [Modul 8] GUI Programming
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
import java.util.ArrayList; // [Modul 1] Variabel & Tipe Data (Penggunaan List Dinamis)
import java.util.List;

// --- Model Data (Compact) ---

// [Modul 5] OOP 1: Class (Blueprint Objek)
class Project {
    protected String name, deadline, status; // [Modul 1] Variabel & Tipe Data

    // [Modul 5] OOP 1: Constructor
    public Project(String n, String d, String s) {
        this.name = n;
        this.deadline = d;
        this.status = s;
    }

    // [Modul 4] Function & Method: Getter untuk data tabel
    public String[] getData() {
        return new String[]{name, deadline, status};
    }

    // [Modul 4] Function & Method: Method untuk update data
    public void update(String n, String d, String s) {
        this.name = n;
        this.deadline = d;
        this.status = s;
    }
}

// [Modul 6] OOP 2: Inheritance (Mewarisi dari Project)
class ImportantProject extends Project {
    public ImportantProject(String n, String d, String s) {
        super(n, d, s);
    }

    // [Modul 6] OOP 2: Polymorphism (Overriding)
    @Override public String[] getData() {
        return new String[]{name + " (PRIORITAS)", deadline, status};
    }
}

// [Modul 6] OOP 2: Inheritance (Mewarisi dari JFrame)
public class MainApp extends JFrame {

    // [Modul 1] Variabel & Tipe Data: Deklarasi konstanta warna dan font
    private final Color C_PRI = new Color(52, 152, 219), C_DNG = new Color(231, 76, 60), C_SUC = new Color(46, 204, 113);
    private final Color C_WAR = new Color(243, 156, 18), C_BG = new Color(245, 247, 250), C_HDR = new Color(44, 62, 80);
    private final Font F_NOR = new Font("Segoe UI", Font.PLAIN, 14), F_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private final Border TF_DEF_BORDER = new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(8, 10, 8, 10));

    // --- Komponen & Data ---
    private DefaultTableModel model;
    private JTable table;
    private JScrollPane tableScrollPane;
    private JTextField tfNama, tfSearch;
    private JComboBox<String> cbStatus, cbJenis, cbFilterStatus, cbBulan;
    private JComboBox<Integer> cbHari, cbTahun;
    private JButton btnTambah, btnEdit, btnHapus, btnSimpan, btnBatalEdit;

    private final ArrayList<Project> projectList = new ArrayList<>(); // [Modul 5] OOP 1: Objek (List dari objek Project)
    private final List<Integer> visibleIndexes = new ArrayList<>();
    private int selectedProjectIndex = -1;

    // [Modul 5] OOP 1: Constructor
    public MainApp() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignore) {}
        setTitle("Project Manajemen Proyek Mini");
        setSize(1000, 800); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE); // [Modul 8] GUI Programming

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(C_BG);

        // [Modul 4] Function & Method: Memanggil fungsi pembangun UI
        JPanel headerPanel = createHeader();
        JPanel contentPanel = createContentTable();
        JPanel formContainer = createFormCard();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(formContainer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        updateTableFiltered();
        setupListeners(mainPanel, headerPanel, contentPanel, formContainer);
        setVisible(true);
    }

    // --- Komponen Kustom Inner Class ---

    private class RoundedButton extends JButton {
        private final Color hov, prs, norm;
        public RoundedButton(String text, Color bg) {
            super(text); super.setContentAreaFilled(false);
            this.norm = bg; this.hov = bg.brighter(); this.prs = bg.darker();
            setForeground(Color.WHITE); setFont(F_BOLD.deriveFont(13f));
            setFocusPainted(false); setBorder(new EmptyBorder(10, 20, 10, 20)); setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // [Modul 2] Pengkondisian (Ternary Operator)
            g2.setColor(getModel().isPressed() ? prs : getModel().isRollover() ? hov : norm);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class ModernComboBoxUI extends BasicComboBoxUI {
        private final Border CB_DEF_BORDER = new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(0, 5, 0, 0));

        @Override protected ComboPopup createPopup() {
            BasicComboPopup p = new BasicComboPopup(comboBox);
            p.setBorder(new LineBorder(new Color(200, 200, 200)));
            return p;
        }

        @Override protected JButton createArrowButton() {
            return new BasicArrowButton(SwingConstants.SOUTH, new Color(240, 240, 240), new Color(240, 240, 240), Color.GRAY, new Color(240, 240, 240));
        }

        @Override public void installUI(JComponent c) { super.installUI(c); c.setBorder(CB_DEF_BORDER); }
        @Override public void paint(Graphics g, JComponent c) {
            c.setBorder(comboBox.hasFocus() ? new CompoundBorder(new LineBorder(C_PRI), new EmptyBorder(0, 5, 0, 0)) : CB_DEF_BORDER);
            super.paint(g, c);
        }
    }

    private class CustomTableRenderer extends DefaultTableCellRenderer {
        private final Color C_SEL = new Color(232, 240, 254), C_ODD = new Color(248, 249, 250);
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
            setBorder(new EmptyBorder(5, 10, 5, 10));

            // [Modul 2] Pengkondisian: Warna baris ganjil/genap
            comp.setBackground(isSel ? C_SEL : (r % 2 == 0 ? Color.WHITE : C_ODD));
            comp.setForeground(isSel ? new Color(13, 71, 161) : Color.DARK_GRAY);
            setFont(F_NOR);

            // [Modul 2] Pengkondisian: Warna teks berdasarkan Status
            if (c == 2) {
                setFont(F_BOLD);
                String status = (String) v;
                if ("Selesai".equalsIgnoreCase(status)) comp.setForeground(new Color(39, 174, 96));
                else if ("Berjalan".equalsIgnoreCase(status)) comp.setForeground(new Color(41, 128, 185));
                else comp.setForeground(new Color(127, 140, 141));
                // [Modul 2] Pengkondisian: Warna teks berdasarkan Prioritas
            } else if (c == 0 && v.toString().contains("(PRIORITAS)")) {
                comp.setForeground(new Color(192, 57, 43));
                setFont(F_BOLD);
            } else { setFont(F_NOR); }
            return comp;
        }
    }

    // --- Struktur Panel UI ---

    // [Modul 4] Function & Method: Membuat header
    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(C_HDR); p.setBorder(new EmptyBorder(25, 40, 25, 40));
        JLabel title = new JLabel("Manajemen Proyek Mini"); title.setFont(F_BOLD.deriveFont(24f)); title.setForeground(Color.WHITE);
        p.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); searchPanel.setOpaque(false);
        JLabel lblFilter = new JLabel("Filter Status:"); lblFilter.setForeground(new Color(200, 200, 200)); lblFilter.setFont(F_NOR);

        cbFilterStatus = styleComboBox(new String[]{"Semua", "Belum Mulai", "Berjalan", "Selesai"}); cbFilterStatus.setPreferredSize(new Dimension(130, 30));
        cbFilterStatus.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(0, 5, 0, 0)));

        tfSearch = styleTextField(); tfSearch.setPreferredSize(new Dimension(180, 30));
        JLabel lblSearch = new JLabel("Cari:"); lblSearch.setForeground(new Color(200, 200, 200)); lblSearch.setFont(F_NOR);

        searchPanel.add(lblFilter); searchPanel.add(cbFilterStatus); searchPanel.add(lblSearch); searchPanel.add(tfSearch);
        p.add(searchPanel, BorderLayout.EAST);
        return p;
    }

    // [Modul 4] Function & Method: Membuat tabel
    private JPanel createContentTable() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(C_BG); p.setBorder(new EmptyBorder(20, 40, 10, 40));
        model = new DefaultTableModel(new String[]{"Nama Proyek", "Deadline", "Status"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model); table.setFont(F_NOR); table.setRowHeight(40);
        table.setShowVerticalLines(false); table.setShowHorizontalLines(true); table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new CustomTableRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader theader = table.getTableHeader(); theader.setFont(F_BOLD); theader.setPreferredSize(new Dimension(100, 45));
        theader.setReorderingAllowed(false); ((DefaultTableCellRenderer) theader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        theader.setBackground(Color.WHITE); theader.setForeground(new Color(100, 100, 100));
        theader.setBorder(new MatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));


        tableScrollPane = new JScrollPane(table); tableScrollPane.getViewport().setBackground(Color.WHITE); tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        JPanel tableCard = new JPanel(new BorderLayout()); tableCard.setBorder(new LineBorder(new Color(220, 220, 220)));
        tableCard.add(tableScrollPane);
        p.add(tableCard, BorderLayout.CENTER);
        return p;
    }

    // [Modul 4] Function & Method: Membuat form input
    private JPanel createFormCard() {
        JPanel formContainer = new JPanel(new BorderLayout()); formContainer.setBorder(new EmptyBorder(10, 40, 30, 40));
        JPanel formCard = new JPanel(new GridBagLayout()); formCard.setBackground(Color.WHITE);
        formCard.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(25, 25, 25, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Input Nama Proyek ---
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.05;
        formCard.add(createLabel("Nama Proyek"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.95;
        tfNama = styleTextField(); formCard.add(tfNama, gbc);

        // --- Input Deadline (Tanggal, Bulan, Tahun) ---
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.05;
        formCard.add(createLabel("Deadline"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.95;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.setOpaque(false);
        cbHari = styleComboBox(null); cbHari.setPreferredSize(new Dimension(80, 35));

        // [Modul 3] Perulangan: Mengisi Hari
        for (int i = 1; i <= 31; i++) cbHari.addItem(i);

        cbBulan = styleComboBox(new String[]{"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"}); cbBulan.setPreferredSize(new Dimension(130, 35));
        cbTahun = styleComboBox(null); cbTahun.setPreferredSize(new Dimension(100, 35));

        // [Modul 3] Perulangan: Mengisi Tahun
        for (int i = 2025; i <= 2030; i++) cbTahun.addItem(i);

        datePanel.add(cbHari); datePanel.add(cbBulan); datePanel.add(cbTahun); formCard.add(datePanel, gbc);

        // --- Input Status dan Jenis (Prioritas) ---
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.05;
        formCard.add(createLabel("Status"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.95;
        JPanel row3 = new JPanel(new GridBagLayout()); row3.setOpaque(false); GridBagConstraints gbcIn = new GridBagConstraints(); gbcIn.fill = GridBagConstraints.HORIZONTAL;
        gbcIn.insets = new Insets(0, 0, 0, 10);

        // Status ComboBox
        gbcIn.weightx = 0.4; cbStatus = styleComboBox(new String[]{"Belum Mulai", "Berjalan", "Selesai"}); cbStatus.setPreferredSize(new Dimension(150, 35)); row3.add(cbStatus, gbcIn);

        // Label Prioritas
        gbcIn.weightx = 0.05;
        row3.add(createLabel("Prioritas:"), gbcIn);

        // Jenis ComboBox
        gbcIn.weightx = 0.4; cbJenis = styleComboBox(new String[]{"Biasa", "Penting"}); cbJenis.setPreferredSize(new Dimension(150, 35)); row3.add(cbJenis, gbcIn);
        formCard.add(row3, gbc);

        // --- Tombol Aksi ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(25, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); btnPanel.setOpaque(false);

        btnTambah = new RoundedButton("Tambah Data", C_SUC);
        btnSimpan = new RoundedButton("Simpan Perubahan", C_PRI);
        btnEdit = new RoundedButton("Edit", C_WAR);
        btnHapus = new RoundedButton("Hapus", C_DNG);
        btnBatalEdit = new RoundedButton("Batal", Color.GRAY);

        btnEdit.setVisible(false); btnHapus.setVisible(false); btnSimpan.setVisible(false); btnBatalEdit.setVisible(false);
        btnTambah.setVisible(true);
        btnPanel.add(btnTambah); btnPanel.add(btnEdit); btnPanel.add(btnHapus); btnPanel.add(btnBatalEdit); btnPanel.add(btnSimpan);
        formCard.add(btnPanel, gbc);
        formContainer.add(formCard, BorderLayout.CENTER);
        return formContainer;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text); lbl.setFont(F_BOLD); lbl.setForeground(new Color(80, 80, 80)); return lbl;
    }

    private JTextField styleTextField() {
        JTextField tf = new JTextField(); tf.setFont(F_NOR); tf.setBorder(TF_DEF_BORDER);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { tf.setBorder(new CompoundBorder(new LineBorder(C_PRI), new EmptyBorder(8, 10, 8, 10))); }
            public void focusLost(FocusEvent e) { tf.setBorder(TF_DEF_BORDER); }
        });
        return tf;
    }

    private <T> JComboBox<T> styleComboBox(T[] data) {
        JComboBox<T> cb = (data != null) ? new JComboBox<>(data) : new JComboBox<>();
        cb.setFont(F_NOR); cb.setBackground(Color.WHITE);
        cb.setUI(new ModernComboBoxUI());
        cb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(7, 10, 7, 10));
                // [Modul 2] Pengkondisian
                if (isSelected) { setBackground(new Color(235, 245, 251)); setForeground(C_PRI); }
                else { setBackground(Color.WHITE); setForeground(Color.DARK_GRAY); }
                return this;
            }
        });
        return cb;
    }

    // --- Logika & Listener ---

    private void setupListeners(JPanel mainPanel, JPanel headerPanel, JPanel contentPanel, JPanel formContainer) {
        // [Modul 8] Event Handling
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
                if (table.rowAtPoint(e.getPoint()) == -1 && selectedProjectIndex != -1) {
                    table.clearSelection();
                }
            }
        });
    }

    private void enableForm(boolean enabled) {
        tfNama.setEnabled(enabled); cbHari.setEnabled(enabled); cbBulan.setEnabled(enabled);
        cbTahun.setEnabled(enabled); cbStatus.setEnabled(enabled); cbJenis.setEnabled(enabled);
    }

    private void clearForm() {
        tfNama.setText(""); cbHari.setSelectedIndex(0); cbBulan.setSelectedIndex(0); cbTahun.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0); cbJenis.setSelectedIndex(0);
        enableForm(true); table.clearSelection(); selectedProjectIndex = -1;
        btnEdit.setVisible(false); btnHapus.setVisible(false); btnSimpan.setVisible(false);
        btnBatalEdit.setVisible(false); btnTambah.setVisible(true);
    }

    private String createDeadlineString() { return cbHari.getSelectedItem() + " " + cbBulan.getSelectedItem() + " " + cbTahun.getSelectedItem(); }

    private String[] parseDeadlineComponents(String deadline) {
        String[] parts = deadline.split(" ");
        // [Modul 2] Pengkondisian
        if (parts.length != 3) return null;
        try { Integer.parseInt(parts[0]); Integer.parseInt(parts[2]); return parts; } catch (Exception e) { return null; }
    }

    private void pilihBaris() {
        if (table.getSelectionModel().getValueIsAdjusting()) return;
        int tableRow = table.getSelectedRow();
        // [Modul 2] Pengkondisian
        if (tableRow == -1 || tableRow >= visibleIndexes.size()) { clearForm(); return; }

        selectedProjectIndex = visibleIndexes.get(tableRow);
        Project p = projectList.get(selectedProjectIndex); // [Modul 5] OOP 1: Mengambil objek
        tfNama.setText(p.name.replace(" (PRIORITAS)", ""));
        String[] comps = parseDeadlineComponents(p.deadline);
        if (comps != null) {
            cbHari.setSelectedItem(Integer.parseInt(comps[0])); cbBulan.setSelectedItem(comps[1]); cbTahun.setSelectedItem(Integer.parseInt(comps[2]));
        }
        cbStatus.setSelectedItem(p.status);
        // [Modul 2] Pengkondisian (instanceof)
        cbJenis.setSelectedItem(p instanceof ImportantProject ? "Penting" : "Biasa");
        btnEdit.setVisible(true); btnHapus.setVisible(true); btnTambah.setVisible(false); btnSimpan.setVisible(false); btnBatalEdit.setVisible(false); enableForm(false);
    }

    // [Modul 4] Function & Method: Menambah proyek
    private void tambahProyek() {
        // [Modul 2] Pengkondisian (Validasi)
        if (tfNama.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Nama kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE); return; }
        String d = createDeadlineString();
        // [Modul 6] OOP 2: Polymorphism (Membuat objek berdasarkan kondisi)
        Project p = (cbJenis.getSelectedItem().equals("Penting")) ? new ImportantProject(tfNama.getText().trim(), d, cbStatus.getSelectedItem().toString()) : new Project(tfNama.getText().trim(), d, cbStatus.getSelectedItem().toString());
        projectList.add(p);
        updateTableFiltered();
        clearForm();
        tfNama.requestFocusInWindow();
    }

    private void masukModeEdit() {
        enableForm(true);
        btnEdit.setVisible(false); btnHapus.setVisible(false); btnSimpan.setVisible(true); btnBatalEdit.setVisible(true);
        tfNama.requestFocus();
    }

    private void simpanPerubahan() {
        if (selectedProjectIndex == -1) return;
        if (tfNama.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Nama kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE); return; }

        Project p = projectList.get(selectedProjectIndex);
        String newName = tfNama.getText().trim(), d = createDeadlineString(), newStatus = cbStatus.getSelectedItem().toString();
        boolean isImportantNow = cbJenis.getSelectedItem().equals("Penting"), wasImportant = p instanceof ImportantProject;

        // [Modul 2] Pengkondisian
        if (isImportantNow != wasImportant) {
            // [Modul 6] OOP 2: Polymorphism (Mengganti class objek)
            Project newProject = isImportantNow ? new ImportantProject(newName, d, newStatus) : new Project(newName, d, newStatus);
            projectList.set(selectedProjectIndex, newProject);
        } else {
            p.update(newName, d, newStatus);
        }

        updateTableFiltered();
        int selectedRow = -1;
        // [Modul 3] Perulangan: Mencari index baris
        for (int i = 0; i < visibleIndexes.size(); i++) { if (visibleIndexes.get(i) == selectedProjectIndex) { selectedRow = i; break; } }
        clearForm();
        if(selectedRow != -1) { final int finalSelectedRow = selectedRow; SwingUtilities.invokeLater(() -> table.getSelectionModel().setSelectionInterval(finalSelectedRow, finalSelectedRow)); }
    }

    private void hapusProyek() {
        if (selectedProjectIndex == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus proyek ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        // [Modul 2] Pengkondisian
        if (confirm == JOptionPane.YES_OPTION) { projectList.remove(selectedProjectIndex); updateTableFiltered(); clearForm(); }
    }

    private void batalkanEdit() { table.clearSelection(); }

    private void updateTableFiltered() {
        model.setRowCount(0); visibleIndexes.clear();
        String s = tfSearch.getText().toLowerCase().trim();
        String f = cbFilterStatus.getSelectedItem().toString();

        // [Modul 3] Perulangan: Iterasi seluruh List
        for (int i = 0; i < projectList.size(); i++) {
            Project p = projectList.get(i);
            boolean matchSearch = p.name.toLowerCase().contains(s);
            boolean matchFilter = f.equals("Semua") || p.status.equals(f);

            // [Modul 2] Pengkondisian: Logika Filter
            if (matchSearch && matchFilter) {
                model.addRow(p.getData());
                visibleIndexes.add(i);
            }
        }
        if (table.getRowCount() == 0 && selectedProjectIndex != -1) clearForm();
    }

    public static void main(String[] args) {
        // [Modul 8] GUI Programming: Menjalankan di EDT
        SwingUtilities.invokeLater(MainApp::new);
    }
}