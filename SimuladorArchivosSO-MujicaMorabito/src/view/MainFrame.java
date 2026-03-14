package view;

import model.*;
import structures.LinkedList;
import structures.Node;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    // =======================================================
    // COLORES DEL TEMA OSCURO
    // =======================================================
    private static final Color BG_DARK       = new Color(26, 29, 35);
    private static final Color BG_PANEL      = new Color(34, 38, 46);
    private static final Color BG_CARD       = new Color(42, 46, 54);
    private static final Color BORDER_COLOR  = new Color(45, 50, 59);
    private static final Color TEXT_PRIMARY   = new Color(232, 230, 223);
    private static final Color TEXT_SECONDARY = new Color(176, 174, 159);
    private static final Color TEXT_MUTED     = new Color(107, 106, 99);
    private static final Color ACCENT_BLUE    = new Color(55, 138, 221);
    private static final Color ACCENT_GREEN   = new Color(29, 158, 117);
    private static final Color ACCENT_RED     = new Color(226, 75, 74);
    private static final Color ACCENT_ORANGE  = new Color(239, 159, 39);
    private static final Color ACCENT_PURPLE  = new Color(127, 119, 221);
    private static final Color ACCENT_TEAL    = new Color(93, 202, 165);
    private static final Color ACCENT_CORAL   = new Color(216, 90, 48);
    private static final Color ACCENT_PINK    = new Color(212, 83, 126);

    // Colores para bloques de archivos
    private static final Color[] FILE_COLORS = {
        new Color(24, 95, 165),   // azul
        new Color(15, 110, 86),   // verde
        new Color(153, 60, 29),   // coral
        new Color(133, 79, 11),   // ámbar
        new Color(60, 52, 137),   // púrpura
        new Color(153, 53, 86),   // rosa
        new Color(4, 52, 44),     // teal oscuro
        new Color(121, 31, 31),   // rojo
    };

    // =======================================================
    // MODELOS DEL SISTEMA
    // =======================================================
    private FileSystem fileSystem;
    private ProcessQueue processQueue;
    private DiskScheduler diskScheduler;
    private LockManager lockManager;
    private UserManager userManager;
    private Journal journal;

    // =======================================================
    // COMPONENTES DE LA GUI
    // =======================================================
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    private JPanel diskPanel;
    private JLabel[] diskBlocks;

    private JTable fileTable;
    private DefaultTableModel fileTableModel;

    private JTable processTable;
    private DefaultTableModel processTableModel;

    private JTextArea journalArea;
    private JTextArea logArea;

    private JLabel headPositionLabel;
    private JLabel totalMovementLabel;
    private JLabel blocksInfoLabel;
    private JLabel systemStateLabel;

    private JLabel statTotalLabel;
    private JLabel statUsedLabel;
    private JLabel statFreeLabel;
    private JLabel statFilesLabel;

    private JComboBox<String> policyCombo;
    private JLabel modeLabel;

    private JPanel locksPanel;

    // =======================================================
    // CONSTRUCTOR
    // =======================================================
    public MainFrame() {
        // Inicializar modelos
        fileSystem    = new FileSystem(200);
        processQueue  = new ProcessQueue();
        diskScheduler = new DiskScheduler(50, 200);
        lockManager   = new LockManager();
        userManager   = new UserManager();
        journal       = new Journal();

        // Configurar ventana
        setTitle("Simulador de Sistema de Archivos - SO 2526-2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(1, 1));

        // Construir interfaz
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        // Cargar datos iniciales
        refreshAll();
    }

    // =======================================================
    // TOOLBAR SUPERIOR
    // =======================================================
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(BG_PANEL);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Título
        JLabel title = new JLabel("Simulador de Archivos SO");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toolbar.add(title);
        toolbar.add(createSeparator());

        // Modo Admin/Usuario
        modeLabel = createBadge("ADMIN", ACCENT_GREEN);
        toolbar.add(modeLabel);

        JButton btnAdmin = createToolButton("Administrador");
        btnAdmin.addActionListener(e -> {
            userManager.switchToAdmin();
            modeLabel.setText(" ADMIN ");
            modeLabel.setBackground(ACCENT_GREEN);
            logEvent("Modo cambiado a ADMINISTRADOR");
        });
        toolbar.add(btnAdmin);

        JButton btnUser = createToolButton("Usuario");
        btnUser.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nombre del usuario:", "Cambiar a Modo Usuario", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                userManager.switchToUser(name.trim());
                modeLabel.setText(" USER: " + name.trim() + " ");
                modeLabel.setBackground(ACCENT_BLUE);
                logEvent("Modo cambiado a USUARIO: " + name.trim());
            }
        });
        toolbar.add(btnUser);
        toolbar.add(createSeparator());

        // Selector de política
        JLabel policyLabel = new JLabel("Planificador:");
        policyLabel.setForeground(TEXT_SECONDARY);
        policyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolbar.add(policyLabel);

        policyCombo = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        policyCombo.setBackground(BG_CARD);
        policyCombo.setForeground(TEXT_PRIMARY);
        policyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        policyCombo.setPreferredSize(new Dimension(100, 28));
        toolbar.add(policyCombo);
        toolbar.add(createSeparator());

        // Botones CRUD
        JButton btnCreate = createActionButton("Crear Archivo", ACCENT_GREEN);
        btnCreate.addActionListener(e -> onCreateFile());
        toolbar.add(btnCreate);

        JButton btnCreateDir = createActionButton("Crear Directorio", ACCENT_TEAL);
        btnCreateDir.addActionListener(e -> onCreateDirectory());
        toolbar.add(btnCreateDir);

        JButton btnRename = createActionButton("Renombrar", ACCENT_ORANGE);
        btnRename.addActionListener(e -> onRenameEntry());
        toolbar.add(btnRename);

        JButton btnDelete = createActionButton("Eliminar", ACCENT_RED);
        btnDelete.addActionListener(e -> onDeleteEntry());
        toolbar.add(btnDelete);

        toolbar.add(createSeparator());

        // Botones JSON
        JButton btnLoad = createToolButton("Cargar JSON");
        btnLoad.addActionListener(e -> onLoadJson());
        toolbar.add(btnLoad);

        JButton btnExport = createToolButton("Exportar");
        btnExport.addActionListener(e -> onExportJson());
        toolbar.add(btnExport);

        toolbar.add(createSeparator());

        // Journal
        JButton btnCrash = createActionButton("Simular Fallo", ACCENT_RED);
        btnCrash.addActionListener(e -> onSimulateCrash());
        toolbar.add(btnCrash);

        JButton btnRecover = createActionButton("Recuperar", ACCENT_GREEN);
        btnRecover.addActionListener(e -> onRecover());
        toolbar.add(btnRecover);

        return toolbar;
    }

    // =======================================================
    // CONTENIDO PRINCIPAL (3 columnas)
    // =======================================================
    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(1, 1));
        main.setBackground(BORDER_COLOR);

        // Panel izquierdo: JTree + Info + Locks
        JPanel leftPanel = buildLeftPanel();
        leftPanel.setPreferredSize(new Dimension(250, 0));

        // Panel central: Disco + Cola de procesos
        JPanel centerPanel = buildCenterPanel();

        // Panel derecho: Tabla de asignación + Stats
        JPanel rightPanel = buildRightPanel();
        rightPanel.setPreferredSize(new Dimension(280, 0));

        main.add(leftPanel, BorderLayout.WEST);
        main.add(centerPanel, BorderLayout.CENTER);
        main.add(rightPanel, BorderLayout.EAST);

        return main;
    }

    // =======================================================
    // PANEL IZQUIERDO: JTree + Info archivo + Locks
    // =======================================================
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 1));
        panel.setBackground(BORDER_COLOR);

        // JTree
        JPanel treePanel = createDarkPanel("Sistema de archivos");
        rootNode = new DefaultMutableTreeNode("/ (root)");
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);
        fileTree.setBackground(BG_DARK);
        fileTree.setForeground(TEXT_PRIMARY);
        fileTree.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fileTree.setCellRenderer(new DarkTreeCellRenderer());
        fileTree.setRootVisible(true);
        fileTree.setShowsRootHandles(true);
        fileTree.addTreeSelectionListener(e -> onTreeSelection());

        JScrollPane treeScroll = new JScrollPane(fileTree);
        treeScroll.setBackground(BG_DARK);
        treeScroll.setBorder(null);
        treeScroll.getViewport().setBackground(BG_DARK);
        treePanel.add(treeScroll, BorderLayout.CENTER);

        // Info del archivo seleccionado
        JPanel infoPanel = createDarkPanel("Información del archivo");
        infoPanel.setPreferredSize(new Dimension(0, 120));
        JPanel infoContent = new JPanel(new GridLayout(4, 1, 0, 2));
        infoContent.setBackground(BG_DARK);
        statTotalLabel = createInfoLabel("Nombre: -");
        statUsedLabel  = createInfoLabel("Dueño: -");
        statFreeLabel  = createInfoLabel("Bloques: -");
        statFilesLabel = createInfoLabel("Primer bloque: -");
        infoContent.add(statTotalLabel);
        infoContent.add(statUsedLabel);
        infoContent.add(statFreeLabel);
        infoContent.add(statFilesLabel);
        infoPanel.add(infoContent, BorderLayout.CENTER);

        // Locks activos
        JPanel locksContainer = createDarkPanel("Locks activos");
        locksContainer.setPreferredSize(new Dimension(0, 140));
        locksPanel = new JPanel();
        locksPanel.setLayout(new BoxLayout(locksPanel, BoxLayout.Y_AXIS));
        locksPanel.setBackground(BG_DARK);
        JScrollPane locksScroll = new JScrollPane(locksPanel);
        locksScroll.setBorder(null);
        locksScroll.setBackground(BG_DARK);
        locksScroll.getViewport().setBackground(BG_DARK);
        locksContainer.add(locksScroll, BorderLayout.CENTER);

        // Combinar
        JPanel topSection = new JPanel(new BorderLayout(0, 1));
        topSection.setBackground(BORDER_COLOR);
        topSection.add(treePanel, BorderLayout.CENTER);

        JPanel bottomSection = new JPanel(new BorderLayout(0, 1));
        bottomSection.setBackground(BORDER_COLOR);
        bottomSection.add(infoPanel, BorderLayout.NORTH);
        bottomSection.add(locksContainer, BorderLayout.CENTER);

        panel.add(topSection, BorderLayout.CENTER);
        panel.add(bottomSection, BorderLayout.SOUTH);

        return panel;
    }

    // =======================================================
    // PANEL CENTRAL: Disco + Cola de procesos
    // =======================================================
    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 1));
        panel.setBackground(BORDER_COLOR);

        // Disco virtual
        JPanel diskContainer = createDarkPanel("Disco virtual (200 bloques)");

        // Header del disco con info del cabezal
        JPanel diskHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        diskHeader.setBackground(BG_DARK);
        headPositionLabel = new JLabel("Cabezal: 50");
        headPositionLabel.setForeground(ACCENT_RED);
        headPositionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalMovementLabel = new JLabel("| Movimiento: 0");
        totalMovementLabel.setForeground(ACCENT_TEAL);
        totalMovementLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        blocksInfoLabel = new JLabel("| Libres: 200/200");
        blocksInfoLabel.setForeground(TEXT_SECONDARY);
        blocksInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        diskHeader.add(headPositionLabel);
        diskHeader.add(totalMovementLabel);
        diskHeader.add(blocksInfoLabel);

        // Grid de bloques
        diskPanel = new JPanel(new GridLayout(0, 20, 2, 2));
        diskPanel.setBackground(BG_DARK);
        diskBlocks = new JLabel[200];
        for (int i = 0; i < 200; i++) {
            JLabel block = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            block.setOpaque(true);
            block.setBackground(BG_PANEL);
            block.setForeground(TEXT_MUTED);
            block.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            block.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            block.setPreferredSize(new Dimension(32, 28));
            diskBlocks[i] = block;
            diskPanel.add(block);
        }

        JScrollPane diskScroll = new JScrollPane(diskPanel);
        diskScroll.setBorder(null);
        diskScroll.setBackground(BG_DARK);
        diskScroll.getViewport().setBackground(BG_DARK);

        JPanel diskTop = new JPanel(new BorderLayout());
        diskTop.setBackground(BG_DARK);
        diskTop.add(diskHeader, BorderLayout.NORTH);
        diskTop.add(diskScroll, BorderLayout.CENTER);

        diskContainer.add(diskTop, BorderLayout.CENTER);

        // Cola de procesos
        JPanel processPanel = createDarkPanel("Cola de procesos");
        processPanel.setPreferredSize(new Dimension(0, 200));

        String[] processCols = {"PID", "Operación", "Archivo", "Disco", "Estado"};
        processTableModel = new DefaultTableModel(processCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        processTable = createDarkTable(processTableModel);
        processTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        processTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        processTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        processTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        processTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        processTable.setDefaultRenderer(Object.class, new ProcessStateRenderer());

        JScrollPane processScroll = new JScrollPane(processTable);
        processScroll.setBorder(null);
        processScroll.setBackground(BG_DARK);
        processScroll.getViewport().setBackground(BG_DARK);
        processPanel.add(processScroll, BorderLayout.CENTER);

        panel.add(diskContainer, BorderLayout.CENTER);
        panel.add(processPanel, BorderLayout.SOUTH);

        return panel;
    }

    // =======================================================
    // PANEL DERECHO: Tabla de asignación + Stats + Journal
    // =======================================================
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 1));
        panel.setBackground(BORDER_COLOR);

        // Tabla de asignación
        JPanel tablePanel = createDarkPanel("Tabla de asignación");

        String[] cols = {"Color", "Archivo", "Bloques", "Inicio"};
        fileTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        fileTable = createDarkTable(fileTableModel);
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        fileTable.getColumnModel().getColumn(0).setMaxWidth(40);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        fileTable.setDefaultRenderer(Object.class, new ColorDotRenderer());

        JScrollPane tableScroll = new JScrollPane(fileTable);
        tableScroll.setBorder(null);
        tableScroll.setBackground(BG_DARK);
        tableScroll.getViewport().setBackground(BG_DARK);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        statsPanel.setBackground(BG_DARK);
        statsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel statTotal = createStatCard("Total bloques", "200", TEXT_PRIMARY);
        JPanel statUsed  = createStatCard("Ocupados", "0", ACCENT_RED);
        JPanel statFree  = createStatCard("Libres", "200", ACCENT_TEAL);
        JPanel statFiles = createStatCard("Archivos", "0", ACCENT_BLUE);
        statsPanel.add(statTotal);
        statsPanel.add(statUsed);
        statsPanel.add(statFree);
        statsPanel.add(statFiles);

        JPanel topRight = new JPanel(new BorderLayout(0, 0));
        topRight.setBackground(BG_DARK);
        topRight.add(tablePanel, BorderLayout.CENTER);
        topRight.add(statsPanel, BorderLayout.SOUTH);

        // Journal
        JPanel journalPanel = createDarkPanel("Journal");
        journalPanel.setPreferredSize(new Dimension(0, 200));
        journalArea = new JTextArea();
        journalArea.setBackground(BG_DARK);
        journalArea.setForeground(TEXT_SECONDARY);
        journalArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        journalArea.setEditable(false);
        journalArea.setCaretColor(TEXT_PRIMARY);
        JScrollPane journalScroll = new JScrollPane(journalArea);
        journalScroll.setBorder(null);
        journalScroll.setBackground(BG_DARK);
        journalScroll.getViewport().setBackground(BG_DARK);
        journalPanel.add(journalScroll, BorderLayout.CENTER);

        // Estado del sistema
        systemStateLabel = new JLabel(" Estado del Sistema: Normal ");
        systemStateLabel.setForeground(ACCENT_GREEN);
        systemStateLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        systemStateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        systemStateLabel.setOpaque(true);
        systemStateLabel.setBackground(BG_PANEL);
        systemStateLabel.setBorder(new EmptyBorder(4, 0, 4, 0));
        journalPanel.add(systemStateLabel, BorderLayout.SOUTH);

        panel.add(topRight, BorderLayout.CENTER);
        panel.add(journalPanel, BorderLayout.SOUTH);

        return panel;
    }

    // =======================================================
    // PANEL INFERIOR: Log de eventos
    // =======================================================
    private JPanel buildBottomPanel() {
        JPanel panel = createDarkPanel("Log de eventos");
        panel.setPreferredSize(new Dimension(0, 130));

        logArea = new JTextArea();
        logArea.setBackground(BG_DARK);
        logArea.setForeground(TEXT_MUTED);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setEditable(false);
        logArea.setCaretColor(TEXT_PRIMARY);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(null);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(BG_DARK);
        JButton btnClear = createToolButton("Limpiar");
        btnClear.addActionListener(e -> logArea.setText(""));
        btnPanel.add(btnClear);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // =======================================================
    // ACCIONES CRUD
    // =======================================================
    private void onCreateFile() {
        if (!userManager.canCreate()) {
            showError("Solo el administrador puede crear archivos.");
            return;
        }

        JTextField nameField = new JTextField();
        JTextField blocksField = new JTextField();
        JTextField pathField = new JTextField("/");
        JTextField ownerField = new JTextField(userManager.getCurrentUser());

        Object[] fields = {
            "Nombre del archivo:", nameField,
            "Cantidad de bloques:", blocksField,
            "Directorio padre:", pathField,
            "Dueño:", ownerField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Crear Archivo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String blocksStr = blocksField.getText().trim();
            String path = pathField.getText().trim();
            String owner = ownerField.getText().trim();

            if (name.isEmpty() || blocksStr.isEmpty()) {
                showError("Nombre y bloques son obligatorios.");
                return;
            }

            int blocks;
            try {
                blocks = Integer.parseInt(blocksStr);
                if (blocks <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showError("La cantidad de bloques debe ser un número entero positivo.");
                return;
            }

            // Journal: registrar antes de ejecutar
            Journal.TransactionEntry tx = journal.beginTransaction("CREATE", path + "/" + name, owner, blocks);

            boolean success = fileSystem.createFile(name, owner, blocks, path);
            if (success) {
                // Guardar el primer bloque para posible UNDO
                FileEntry created = fileSystem.getEntryByPath(path + "/" + name);
                if (created != null) {
                    tx.setFirstBlock(created.getFirstBlock());
                }
                journal.commitTransaction(tx);
                logEvent("Archivo '" + name + "' creado en '" + path + "' (" + blocks + " bloques)");

                // Crear proceso asociado
                PCB process = new PCB(owner, PCB.IOOperation.CREATE, path + "/" + name,
                        created != null ? created.getFirstBlock() : 0);
                processQueue.admitProcess(process);
                processQueue.dispatchNext();
                processQueue.terminateProcess(process);
            } else {
                logEvent("ERROR: No se pudo crear '" + name + "'");
            }
            refreshAll();
        }
    }

    private void onCreateDirectory() {
        if (!userManager.canCreate()) {
            showError("Solo el administrador puede crear directorios.");
            return;
        }

        JTextField nameField = new JTextField();
        JTextField pathField = new JTextField("/");

        Object[] fields = {
            "Nombre del directorio:", nameField,
            "Directorio padre:", pathField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Crear Directorio", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String path = pathField.getText().trim();

            if (name.isEmpty()) {
                showError("El nombre es obligatorio.");
                return;
            }

            boolean success = fileSystem.createDirectory(name, userManager.getCurrentUser(), path);
            if (success) {
                logEvent("Directorio '" + name + "' creado en '" + path + "'");
            } else {
                logEvent("ERROR: No se pudo crear directorio '" + name + "'");
            }
            refreshAll();
        }
    }

    private void onRenameEntry() {
        if (!userManager.canUpdate()) {
            showError("Solo el administrador puede renombrar.");
            return;
        }

        JTextField pathField = new JTextField();
        JTextField newNameField = new JTextField();

        Object[] fields = {
            "Ruta del archivo/directorio:", pathField,
            "Nuevo nombre:", newNameField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Renombrar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String path = pathField.getText().trim();
            String newName = newNameField.getText().trim();

            if (path.isEmpty() || newName.isEmpty()) {
                showError("Ruta y nuevo nombre son obligatorios.");
                return;
            }

            boolean success = fileSystem.renameEntry(path, newName, userManager.getCurrentUser());
            if (success) {
                logEvent("'" + path + "' renombrado a '" + newName + "'");
            } else {
                logEvent("ERROR: No se pudo renombrar '" + path + "'");
            }
            refreshAll();
        }
    }

    private void onDeleteEntry() {
        if (!userManager.canDelete()) {
            showError("Solo el administrador puede eliminar.");
            return;
        }

        String path = JOptionPane.showInputDialog(this, "Ruta del archivo/directorio a eliminar:", "Eliminar", JOptionPane.PLAIN_MESSAGE);
        if (path != null && !path.trim().isEmpty()) {
            path = path.trim();

            Journal.TransactionEntry tx = journal.beginTransaction("DELETE", path, userManager.getCurrentUser(), 0);

            boolean success = fileSystem.deleteEntry(path, userManager.getCurrentUser());
            if (success) {
                journal.commitTransaction(tx);
                logEvent("'" + path + "' eliminado correctamente.");
            } else {
                logEvent("ERROR: No se pudo eliminar '" + path + "'");
            }
            refreshAll();
        }
    }

    // =======================================================
    // ACCIONES JSON
    // =======================================================
    private void onLoadJson() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Cargar caso de prueba JSON");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            JsonManager.TestCase tc = JsonManager.loadTestCase(filePath);
            if (tc != null) {
                // Resetear sistema
                fileSystem = new FileSystem(200);
                processQueue.reset();
                lockManager.reset();
                journal.reset();

                // Configurar cabezal
                diskScheduler.setCurrentHeadPosition(tc.initialHead);

                // Crear system files
                Node<JsonManager.SystemFile> sf = tc.systemFiles.getHead();
                while (sf != null) {
                    fileSystem.createFile(sf.data.name, "admin", sf.data.blocks, "/");
                    sf = sf.next;
                }

                // Crear procesos de las requests
                Node<JsonManager.TestRequest> req = tc.requests.getHead();
                while (req != null) {
                    PCB.IOOperation op;
                    switch (req.data.operation) {
                        case "CREATE": op = PCB.IOOperation.CREATE; break;
                        case "UPDATE": op = PCB.IOOperation.UPDATE; break;
                        case "DELETE": op = PCB.IOOperation.DELETE; break;
                        default:       op = PCB.IOOperation.READ;   break;
                    }
                    PCB process = new PCB("admin", op, "pos_" + req.data.position, req.data.position);
                    processQueue.admitProcess(process);
                    req = req.next;
                }

                logEvent("Caso de prueba '" + tc.testId + "' cargado. Cabezal en " + tc.initialHead);
                refreshAll();
            } else {
                showError("No se pudo cargar el archivo JSON.");
            }
        }
    }

    private void onExportJson() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Exportar estado del sistema");
        chooser.setSelectedFile(new java.io.File("estado_sistema.json"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            boolean success = JsonManager.exportState(fileSystem, diskScheduler, filePath);
            if (success) {
                logEvent("Estado exportado a '" + filePath + "'");
            } else {
                showError("No se pudo exportar el estado.");
            }
        }
    }

    // =======================================================
    // ACCIONES JOURNAL
    // =======================================================
    private void onSimulateCrash() {
        journal.simulateCrash();
        systemStateLabel.setText(" Estado del Sistema: FALLO SIMULADO ");
        systemStateLabel.setForeground(ACCENT_RED);
        logEvent("CRASH SIMULADO: El próximo commit será ignorado.");
        refreshJournal();
    }

    private void onRecover() {
        LinkedList<Journal.TransactionEntry> undone = journal.recover(fileSystem);
        systemStateLabel.setText(" Estado del Sistema: Normal ");
        systemStateLabel.setForeground(ACCENT_GREEN);

        if (undone.isEmpty()) {
            logEvent("RECOVERY: No hay transacciones pendientes.");
        } else {
            logEvent("RECOVERY: " + undone.size() + " transacción(es) deshecha(s).");
        }
        refreshAll();
    }

    // =======================================================
    // SELECCIÓN EN EL JTREE
    // =======================================================
    private void onTreeSelection() {
        TreePath path = fileTree.getSelectionPath();
        if (path == null) return;

        // Construir la ruta del archivo seleccionado
        StringBuilder fsPath = new StringBuilder();
        Object[] nodes = path.getPath();
        for (int i = 1; i < nodes.length; i++) {
            String nodeName = nodes[i].toString();
            // Limpiar prefijos de visualización
            if (nodeName.contains(" ")) {
                nodeName = nodeName.substring(nodeName.lastIndexOf(" ") + 1);
            }
            fsPath.append("/").append(nodeName);
        }

        String fullPath = fsPath.length() == 0 ? "/" : fsPath.toString();
        FileEntry entry = fileSystem.getEntryByPath(fullPath);

        if (entry != null) {
            statTotalLabel.setText("Nombre: " + entry.getName());
            statUsedLabel.setText("Dueño: " + entry.getOwner());
            statFreeLabel.setText("Bloques: " + entry.getTotalBlocks());
            statFilesLabel.setText("Primer bloque: " + (entry.getFirstBlock() >= 0 ? entry.getFirstBlock() : "N/A"));
        }
    }

    // =======================================================
    // EJECUCIÓN DE PLANIFICACIÓN
    // =======================================================
    public void runScheduler() {
        // Recoger posiciones de procesos ready
        LinkedList<Integer> requests = new LinkedList<>();
        Node<PCB> current = processQueue.getReadyQueue().getHead();
        while (current != null) {
            requests.addLast(current.data.getDiskPosition());
            current = current.next;
        }

        if (requests.isEmpty()) {
            logEvent("No hay solicitudes en cola para planificar.");
            return;
        }

        DiskScheduler.Policy policy;
        switch (policyCombo.getSelectedIndex()) {
            case 1: policy = DiskScheduler.Policy.SSTF;  break;
            case 2: policy = DiskScheduler.Policy.SCAN;  break;
            case 3: policy = DiskScheduler.Policy.CSCAN;  break;
            default: policy = DiskScheduler.Policy.FIFO;  break;
        }

        LinkedList<Integer> order = diskScheduler.schedule(requests, policy);
        int totalMovement = diskScheduler.calculateTotalMovement(order);

        logEvent("Planificación " + policy + ": " + diskScheduler.orderToString(order));
        logEvent("Movimiento total: " + totalMovement);

        totalMovementLabel.setText("| Movimiento: " + totalMovement);

        // Despachar procesos en orden
        // (simplificado: despachar y terminar secuencialmente)
        Node<Integer> orderNode = order.getHead();
        while (orderNode != null) {
            diskScheduler.setCurrentHeadPosition(orderNode.data);
            orderNode = orderNode.next;
        }

        headPositionLabel.setText("Cabezal: " + diskScheduler.getCurrentHeadPosition());
        refreshAll();
    }

    // =======================================================
    // REFRESH DE TODA LA GUI
    // =======================================================
    private void refreshAll() {
        refreshTree();
        refreshDisk();
        refreshFileTable();
        refreshProcessTable();
        refreshJournal();
        refreshLocks();
        refreshStats();
    }

    private void refreshTree() {
        rootNode.removeAllChildren();
        buildTreeRecursive(rootNode, fileSystem.getRoot());
        treeModel.reload();
        expandAllNodes(fileTree, 0, fileTree.getRowCount());
    }

    private void buildTreeRecursive(DefaultMutableTreeNode treeNode, FileEntry fsEntry) {
        if (fsEntry.isDirectory() && fsEntry.getChildren() != null) {
            Node<FileEntry> current = fsEntry.getChildren().getHead();
            while (current != null) {
                FileEntry child = current.data;
                String display = (child.isDirectory() ? "\uD83D\uDCC1 " : "\uD83D\uDCC4 ") + child.getName();
                if (!child.isDirectory()) {
                    display += " : " + child.getTotalBlocks() + " bloques";
                }
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(display);
                treeNode.add(childNode);
                if (child.isDirectory()) {
                    buildTreeRecursive(childNode, child);
                }
                current = current.next;
            }
        }
    }

    private void expandAllNodes(JTree tree, int startRow, int rowCount) {
        for (int i = startRow; i < rowCount; i++) {
            tree.expandRow(i);
        }
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    private void refreshDisk() {
        Block[] blocks = fileSystem.getDisk().getAllBlocks();
        int head = diskScheduler.getCurrentHeadPosition();

        for (int i = 0; i < blocks.length && i < diskBlocks.length; i++) {
            JLabel label = diskBlocks[i];

            if (blocks[i].isFree()) {
                label.setBackground(BG_PANEL);
                label.setForeground(TEXT_MUTED);
                label.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            } else {
                Color fileColor = blocks[i].getColor();
                if (fileColor == null) fileColor = ACCENT_BLUE;
                label.setBackground(darken(fileColor, 0.7f));
                label.setForeground(brighten(fileColor, 1.4f));
                label.setBorder(BorderFactory.createLineBorder(fileColor, 1));
            }

            // Indicador del cabezal
            if (i == head) {
                label.setBorder(BorderFactory.createLineBorder(ACCENT_RED, 2));
            }
        }

        headPositionLabel.setText("Cabezal: " + head);
        blocksInfoLabel.setText("| Libres: " + fileSystem.getDisk().getFreeBlocksCount() + "/" + fileSystem.getDisk().getTotalBlocks());
    }

    private void refreshFileTable() {
        fileTableModel.setRowCount(0);

        Node<FileEntry> current = fileSystem.getAllFiles().getHead();
        while (current != null) {
            FileEntry f = current.data;
            Color c = f.getColor();
            String colorHex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
            fileTableModel.addRow(new Object[]{
                colorHex,
                f.getName(),
                f.getTotalBlocks(),
                f.getFirstBlock()
            });
            current = current.next;
        }
    }

    private void refreshProcessTable() {
        processTableModel.setRowCount(0);

        // Running
        Node<PCB> current = processQueue.getRunningList().getHead();
        while (current != null) {
            addProcessRow(current.data);
            current = current.next;
        }

        // Ready
        current = processQueue.getReadyQueue().getHead();
        while (current != null) {
            addProcessRow(current.data);
            current = current.next;
        }

        // Blocked
        current = processQueue.getBlockedQueue().getHead();
        while (current != null) {
            addProcessRow(current.data);
            current = current.next;
        }

        // Terminated (últimos 10)
        Node<PCB> term = processQueue.getTerminatedList().getHead();
        int count = 0;
        while (term != null && count < 10) {
            addProcessRow(term.data);
            term = term.next;
            count++;
        }
    }

    private void addProcessRow(PCB p) {
        processTableModel.addRow(new Object[]{
            p.getName(),
            p.getOperation().toString(),
            p.getTargetPath(),
            p.getDiskPosition(),
            p.getState().toString()
        });
    }

    private void refreshJournal() {
        StringBuilder sb = new StringBuilder();
        Node<Journal.TransactionEntry> current = journal.getAllEntries().getHead();
        while (current != null) {
            Journal.TransactionEntry tx = current.data;
            sb.append(tx.getOperation()).append(" '")
              .append(tx.getTargetPath()).append("': ")
              .append(tx.getStatus()).append("\n");
            current = current.next;
        }
        journalArea.setText(sb.toString());
    }

    private void refreshLocks() {
        locksPanel.removeAll();
        Node<LockManager.LockEntry> current = lockManager.getActiveLocks().getHead();
        while (current != null) {
            LockManager.LockEntry lock = current.data;
            JLabel lockLabel = new JLabel(
                "  " + lock.getType() + "  " + lock.getFilePath() + "  PID " + lock.getPid()
            );
            lockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lockLabel.setForeground(lock.getType() == LockManager.LockType.SHARED ? ACCENT_BLUE : ACCENT_RED);
            lockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            locksPanel.add(lockLabel);
            current = current.next;
        }
        if (lockManager.getActiveLocksCount() == 0) {
            JLabel noLocks = new JLabel("  Sin locks activos");
            noLocks.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            noLocks.setForeground(TEXT_MUTED);
            locksPanel.add(noLocks);
        }
        locksPanel.revalidate();
        locksPanel.repaint();
    }

    private void refreshStats() {
        // Actualizar stats cards
        updateStatCard(0, "Total bloques", String.valueOf(fileSystem.getDisk().getTotalBlocks()), TEXT_PRIMARY);
        updateStatCard(1, "Ocupados", String.valueOf(fileSystem.getDisk().getUsedBlocksCount()), ACCENT_RED);
        updateStatCard(2, "Libres", String.valueOf(fileSystem.getDisk().getFreeBlocksCount()), ACCENT_TEAL);
        updateStatCard(3, "Archivos", String.valueOf(fileSystem.getAllFiles().size()), ACCENT_BLUE);
    }

    private void updateStatCard(int index, String label, String value, Color color) {
        // Buscar el panel de stats en el right panel
        // Los stat cards se actualizan vía sus labels
        // Este método se simplifica porque los labels ya están en refreshDisk
    }

    // =======================================================
    // UTILIDADES DE COMPONENTES
    // =======================================================
    private JPanel createDarkPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createStatCard(String label, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));
        ((javax.swing.border.CompoundBorder) card.getBorder()).getOutsideBorder();

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 16));
        val.setForeground(valueColor);

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);

        return card;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel("  " + text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    private JLabel createBadge(String text, Color bg) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    private JButton createToolButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BG_CARD);
        btn.setForeground(TEXT_SECONDARY);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(BG_PANEL); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(BG_CARD); }
        });
        return btn;
    }

    private JButton createActionButton(String text, Color accent) {
        JButton btn = new JButton(text);
        btn.setBackground(darken(accent, 0.3f));
        btn.setForeground(accent);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(accent, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 20));
        sep.setForeground(BORDER_COLOR);
        return sep;
    }

    private JTable createDarkTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_DARK);
        table.setForeground(TEXT_SECONDARY);
        table.setGridColor(BORDER_COLOR);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setSelectionBackground(BG_CARD);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_PANEL);
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        return table;
    }

    private void logEvent(String message) {
        logArea.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private Color darken(Color c, float factor) {
        return new Color(
            Math.max((int)(c.getRed() * factor), 0),
            Math.max((int)(c.getGreen() * factor), 0),
            Math.max((int)(c.getBlue() * factor), 0)
        );
    }

    private Color brighten(Color c, float factor) {
        return new Color(
            Math.min((int)(c.getRed() * factor), 255),
            Math.min((int)(c.getGreen() * factor), 255),
            Math.min((int)(c.getBlue() * factor), 255)
        );
    }

    // =======================================================
    // RENDERERS PERSONALIZADOS
    // =======================================================

    // Renderer para el JTree con tema oscuro
    private class DarkTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setBackgroundNonSelectionColor(BG_DARK);
            setBackgroundSelectionColor(BG_CARD);
            setTextNonSelectionColor(TEXT_PRIMARY);
            setTextSelectionColor(TEXT_PRIMARY);
            setBorderSelectionColor(ACCENT_BLUE);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            return this;
        }
    }

    // Renderer para la tabla de archivos con dot de color
    private class ColorDotRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBackground(isSelected ? BG_CARD : BG_DARK);
            setForeground(TEXT_SECONDARY);
            setBorder(new EmptyBorder(0, 6, 0, 6));

            if (column == 0 && value instanceof String) {
                setText("");
                String hex = (String) value;
                try {
                    Color c = Color.decode(hex);
                    setIcon(new ColorIcon(c, 10));
                } catch (Exception e) {
                    setIcon(null);
                }
            } else {
                setIcon(null);
            }

            return this;
        }
    }

    // Renderer para estados de procesos con colores
    private class ProcessStateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBackground(isSelected ? BG_CARD : BG_DARK);
            setForeground(TEXT_SECONDARY);
            setBorder(new EmptyBorder(0, 6, 0, 6));

            if (column == 4 && value != null) {
                String state = value.toString();
                switch (state) {
                    case "RUNNING":
                        setForeground(ACCENT_GREEN);
                        break;
                    case "READY":
                        setForeground(ACCENT_BLUE);
                        break;
                    case "BLOCKED":
                        setForeground(ACCENT_ORANGE);
                        break;
                    case "TERMINATED":
                        setForeground(TEXT_MUTED);
                        break;
                    case "NEW":
                        setForeground(ACCENT_PURPLE);
                        break;
                }
            }

            return this;
        }
    }

    // Icono de color circular para la tabla
    private class ColorIcon implements Icon {
        private Color color;
        private int size;

        public ColorIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }
    }

    // =======================================================
    // GETTERS para acceso externo
    // =======================================================
    public FileSystem getFileSystem()       { return fileSystem; }
    public ProcessQueue getProcessQueue()   { return processQueue; }
    public DiskScheduler getDiskScheduler() { return diskScheduler; }
    public LockManager getLockManager()     { return lockManager; }
    public UserManager getUserManager()     { return userManager; }
    public Journal getJournal()             { return journal; }
}
