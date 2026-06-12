
package view;

import service.ConvertService;
import service.ConvertService.ConvertResult;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainView extends JFrame {

    // ── Cores do tema ──────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(24, 24, 32);
    private static final Color BG_PANEL     = new Color(28, 28, 28);
    private static final Color BG_TABLE     = new Color(54, 54, 54);
    private static final Color BG_HEADER    = new Color(28, 28, 28);
    private static final Color LARANJA = new Color(255, 140, 0);
    private static final Color VERMELHO = new Color(255,0,0);
    private static final Color BRANCO = new Color(255,255,255);
    private static final Color TEXT_PRIMARY  = new Color(211, 211, 211);
    private static final Color TEXT_SECONDARY= new Color(192, 192, 192);
    private static final Color BORDER_COLOR = new Color(55, 58, 75);

    // ── Fontes ─────────────────────────────────────────────────────
    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_TABLE    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_HEADER   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_STATUS   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_SECTION  = new Font("Segoe UI", Font.BOLD, 15);

    // ── Componentes ────────────────────────────────────────────────
    private DefaultTableModel originalTableModel;
    private DefaultTableModel convertedTableModel;
    private JTable originalTable;
    private JTable convertedTable;
    private JTextArea txtLog;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private final ConvertService convertService;
    private final java.util.List<File> originalFiles = new ArrayList<>();
    private File outputDirectory = null;
    private JLabel outputDirLabel;

    private JCheckBox chkMerge;
    private JTextField txtOutputName;
    private JCheckBox chkLimitSize;
    private JSpinner spinLimitSize;

    public MainView() {
        this.convertService = new ConvertService();
        initUI();
    }

    private void initUI() {
        setTitle("FileForge");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 780);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setResizable(false);
        // ── Painel superior (Header) ───────────────────────────────
        add(createHeaderPanel(), BorderLayout.NORTH);

        // ── Conteúdo principal (superior) ──────────────────────────
        JSplitPane horizontalSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createOriginalPanel(),
                createConvertedPanel()
        );
        horizontalSplit.setDividerLocation(520);
        horizontalSplit.setDividerSize(6);
        horizontalSplit.setBorder(null);
        horizontalSplit.setBackground(BG_DARK);
        horizontalSplit.setContinuousLayout(true);

        // ── Painel inferior (logs de conversão) ────────────────────
        JSplitPane verticalSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                horizontalSplit,
                createLogPanel()
        );
        verticalSplit.setDividerLocation(380);
        verticalSplit.setDividerSize(6);
        verticalSplit.setBorder(null);
        verticalSplit.setBackground(BG_DARK);
        verticalSplit.setContinuousLayout(true);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(BG_DARK);
        contentWrapper.setBorder(new EmptyBorder(8, 12, 8, 12));
        contentWrapper.add(verticalSplit, BorderLayout.CENTER);

        add(contentWrapper, BorderLayout.CENTER);

        // ── Barra de status inferior ───────────────────────────────
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    // ════════════════════════════════════════════════════════════════
    //  HEADER
    // ════════════════════════════════════════════════════════════════
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 20, 14, 20)
        ));

        // Título
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("FileForgePDF");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(LARANJA);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Converta arquivos para PDF");
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitleLabel);

        header.add(titlePanel, BorderLayout.WEST);

        // Pasta de destino + Botões de ação no header
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        outputDirLabel = new JLabel("Destino: nenhum");
        outputDirLabel.setFont(FONT_STATUS);
        outputDirLabel.setForeground(TEXT_SECONDARY);
        actionPanel.add(outputDirLabel);
    //BOTÕES SUPERIORES
        JButton btnChooseDir = createStyledButton("Pasta Destino", LARANJA);
        btnChooseDir.addActionListener(e -> chooseOutputDirectory());
        actionPanel.add(btnChooseDir);

        header.add(actionPanel, BorderLayout.EAST);

        return header;
    }

    // ════════════════════════════════════════════════════════════════
    //  PAINEL ESQUERDO — ARQUIVOS ORIGINAIS
    // ════════════════════════════════════════════════════════════════
    private JPanel createOriginalPanel() {
        JPanel panel = createSectionPanel("Arquivos Originais - (txt,docx,csv,html,png,jpg) ");

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));
//ADICIONAR
        JButton btnAdd = createStyledButton("Adicionar", LARANJA);
        btnAdd.addActionListener(e -> addFiles());
//REMOVER
        JButton btnRemove = createStyledButton("Remover", VERMELHO);
        btnRemove.addActionListener(e -> removeSelectedOriginal());
//LIMPAR
        JButton btnClear = createStyledButton("Limpar",VERMELHO);
        btnClear.addActionListener(e -> clearOriginals());


        toolbar.add(btnAdd);
        toolbar.add(btnRemove);
        toolbar.add(btnClear);

        panel.add(toolbar, BorderLayout.NORTH);

        // Tabela
        String[] columns = {"Nome", "Caminho", "Tamanho (KB)"};
        originalTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        originalTable = createStyledTable(originalTableModel);
        originalTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        originalTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        originalTable.getColumnModel().getColumn(2).setPreferredWidth(80);

        JScrollPane scrollPane = createStyledScrollPane(originalTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Painel com Configurações e Botão de Conversão
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setOpaque(false);

        // Painel de Configurações
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setOpaque(false);
        settingsPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Mesclar arquivos
        chkMerge = new JCheckBox("Mesclar em um único PDF");
        chkMerge.setFont(FONT_TABLE);
        chkMerge.setForeground(TEXT_PRIMARY);
        chkMerge.setOpaque(false);
        chkMerge.setFocusPainted(false);

        JLabel lblName = new JLabel("Nome do PDF:");
        lblName.setFont(FONT_TABLE);
        lblName.setForeground(TEXT_SECONDARY);

        txtOutputName = new JTextField("resultado", 10);
        txtOutputName.setFont(FONT_TABLE);
        txtOutputName.setBackground(BG_TABLE);
        txtOutputName.setForeground(TEXT_PRIMARY);
        txtOutputName.setCaretColor(TEXT_PRIMARY);
        txtOutputName.setBorder(new LineBorder(BORDER_COLOR, 1));
        txtOutputName.setEnabled(false); // Inicia desabilitado

        chkMerge.addActionListener(e -> txtOutputName.setEnabled(chkMerge.isSelected()));

        // Limitar tamanho
        chkLimitSize = new JCheckBox("Limitar tamanho");
        chkLimitSize.setFont(FONT_TABLE);
        chkLimitSize.setForeground(TEXT_PRIMARY);
        chkLimitSize.setOpaque(false);
        chkLimitSize.setFocusPainted(false);

        JLabel lblSize = new JLabel("Tam. Máx (MB):");
        lblSize.setFont(FONT_TABLE);
        lblSize.setForeground(TEXT_SECONDARY);

        // Spinner para tamanho em MB (de 0.1 a 1000.0, passo 0.5)
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(5.0, 0.1, 1000.0, 0.5);
        spinLimitSize = new JSpinner(spinnerModel);
        spinLimitSize.setFont(FONT_TABLE);
        spinLimitSize.setEnabled(false); // Inicia desabilitado

        // Estilizar o spinner
        JComponent editor = spinLimitSize.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(BG_TABLE);
            tf.setForeground(TEXT_PRIMARY);
            tf.setCaretColor(TEXT_PRIMARY);
            tf.setBorder(null);
        }
        spinLimitSize.setBorder(new LineBorder(BORDER_COLOR, 1));

        chkLimitSize.addActionListener(e -> spinLimitSize.setEnabled(chkLimitSize.isSelected()));

        // Adicionar ao GridBagLayout
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        settingsPanel.add(chkMerge, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        namePanel.setOpaque(false);
        namePanel.add(lblName);
        namePanel.add(txtOutputName);
        settingsPanel.add(namePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.5;
        settingsPanel.add(chkLimitSize, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        sizePanel.setOpaque(false);
        sizePanel.add(lblSize);
        sizePanel.add(spinLimitSize);
        settingsPanel.add(sizePanel, gbc);

        southPanel.add(settingsPanel);

        // Botão converter selecionado
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(6, 0, 0, 0));

        JButton btnConvert = createStyledButton("Converter Selecionado", LARANJA);
        btnConvert.addActionListener(e -> convertSelected());
        bottomBar.add(btnConvert);

        JButton btnConvertAll = createStyledButton("Converter Todos", LARANJA);
        btnConvertAll.addActionListener(e -> convertAll());
        bottomBar.add(btnConvertAll);

        southPanel.add(bottomBar);

        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ════════════════════════════════════════════════════════════════
    //  PAINEL DIREITO — ARQUIVOS CONVERTIDOS
    // ════════════════════════════════════════════════════════════════
    private JPanel createConvertedPanel() {
        JPanel panel = createSectionPanel("Arquivos Convertidos (.pdf)");

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JButton btnOpen = createStyledButton("Abrir PDF", LARANJA);
        btnOpen.addActionListener(e -> openSelectedPdf());

        JButton btnClear = createStyledButton("Limpar", VERMELHO);
        btnClear.addActionListener(e -> clearConverted());

        toolbar.add(btnOpen);
        toolbar.add(btnClear);

        panel.add(toolbar, BorderLayout.NORTH);

        // Tabela
        String[] columns = {"Nome", "Caminho", "Tamanho (KB)", "Variação (%)"};
        convertedTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        convertedTable = createStyledTable(convertedTableModel);
        convertedTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        convertedTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        convertedTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        convertedTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        // Renderer colorido para a coluna de variação
        convertedTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    c.setBackground(BG_TABLE);
                }
                if (value != null) {
                    String text = value.toString();
                    if (text.startsWith("-")) {
                        c.setForeground(LARANJA);
                    } else if (text.startsWith("+")) {
                        c.setForeground(VERMELHO);
                    } else {
                        c.setForeground(TEXT_PRIMARY);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = createStyledScrollPane(convertedTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ════════════════════════════════════════════════════════════════
    //  PAINEL INFERIOR — HISTÓRICO DO BANCO DE DADOS
    // ════════════════════════════════════════════════════════════════
    private JPanel createLogPanel() {
        JPanel panel = createSectionPanel("Logs de Conversão");

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(BG_TABLE);
        txtLog.setForeground(TEXT_PRIMARY);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLog.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(txtLog);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(BG_TABLE);
        scrollPane.setBackground(BG_TABLE);

        // Estilizar scrollbars
        scrollPane.getVerticalScrollBar().setBackground(BG_PANEL);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = BG_PANEL;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ════════════════════════════════════════════════════════════════
    //  BARRA DE STATUS
    // ════════════════════════════════════════════════════════════════
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setBackground(BG_PANEL);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(8, 16, 8, 16)
        ));

        statusLabel = new JLabel("Pronto. Adicione arquivos para converter.");
        statusLabel.setFont(FONT_STATUS);
        statusLabel.setForeground(TEXT_SECONDARY);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 16));
        progressBar.setStringPainted(true);
        progressBar.setFont(FONT_STATUS);
        progressBar.setForeground(LARANJA);
        progressBar.setBackground(BG_TABLE);
        progressBar.setBorderPainted(false);
        progressBar.setVisible(false);

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(progressBar, BorderLayout.EAST);

        return statusBar;
    }

    // ════════════════════════════════════════════════════════════════
    //  AÇÕES
    // ════════════════════════════════════════════════════════════════

    private void addFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivos para Converter");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Arquivos Suportados (*.txt, *.jpg, *.png, *.docx, *.csv, *.html)",
                ConvertService.SUPPORTED_EXTENSIONS
        ));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            int added = 0;
            for (File file : selectedFiles) {
                // Evitar duplicatas
                boolean alreadyExists = originalFiles.stream()
                        .anyMatch(f -> f.getAbsolutePath().equals(file.getAbsolutePath()));
                if (!alreadyExists) {
                    originalFiles.add(file);
                    double sizeKB = file.length() / 1024.0;
                    originalTableModel.addRow(new Object[]{
                            file.getName(),
                            file.getAbsolutePath(),
                            String.format("%.2f", sizeKB)
                    });
                    added++;
                    appendLog("arquivo adicionado: " + file.getName());
                }
            }
            setStatus(added + " arquivo(s) adicionado(s). Total: " + originalFiles.size(), TEXT_PRIMARY);
        }
    }
    // reseta a seleção dos arquivos
    private void removeSelectedOriginal() {
        int[] selectedRows = originalTable.getSelectedRows();
        if (selectedRows.length == 0) {
            setStatus("Selecione um arquivo para remover.", LARANJA);
            return;
        }
        // Remover de trás para frente para manter os índices corretos
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            File file = originalFiles.get(selectedRows[i]);
            appendLog("arquivo removido: " + file.getName());
            originalFiles.remove(selectedRows[i]);
            originalTableModel.removeRow(selectedRows[i]);
        }
        setStatus(selectedRows.length + " arquivo(s) removido(s).", TEXT_PRIMARY);
    }

    private void clearOriginals() {
        originalFiles.clear();
        originalTableModel.setRowCount(0);
        setStatus("Lista de arquivos originais limpa.", TEXT_PRIMARY);
        appendLog("lista de arquivos originais limpa");
    }

    private void clearConverted() {
        convertedTableModel.setRowCount(0);
        setStatus("Lista de arquivos convertidos limpa.", TEXT_PRIMARY);
        appendLog("lista de arquivos convertidos limpa");
    }

    private void convertSelected() {
        int[] selectedRows = originalTable.getSelectedRows();
        if (selectedRows.length == 0) {
            setStatus("Selecione um arquivo para converter.", LARANJA);
            return;
        }
        if (!ensureOutputDirectory()) return;

        java.util.List<File> toConvert = new ArrayList<>();
        for (int row : selectedRows) {
            toConvert.add(originalFiles.get(row));
        }
        doConvert(toConvert);
    }

    private void convertAll() {
        if (originalFiles.isEmpty()) {
            setStatus("Nenhum arquivo na lista para converter.", LARANJA);
            return;
        }
        if (!ensureOutputDirectory()) return;
        doConvert(new ArrayList<>(originalFiles));
    }

    private void chooseOutputDirectory() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setDialogTitle("Selecionar Pasta de Destino dos PDFs");
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setAcceptAllFileFilterUsed(false);
        if (outputDirectory != null) {
            dirChooser.setCurrentDirectory(outputDirectory);
        }

        int result = dirChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputDirectory = dirChooser.getSelectedFile();
            outputDirLabel.setText("Destino: " + outputDirectory.getName());
            outputDirLabel.setToolTipText(outputDirectory.getAbsolutePath());
            outputDirLabel.setForeground(LARANJA);
            setStatus("Pasta de destino: " + outputDirectory.getAbsolutePath(), LARANJA);
        }
    }

    private boolean ensureOutputDirectory() {
        if (outputDirectory == null) {
            setStatus("Selecione a pasta de destino antes de converter.", LARANJA);
            chooseOutputDirectory();
            return outputDirectory != null;
        }
        return true;
    }

    private void doConvert(java.util.List<File> files) {
        boolean merge = chkMerge.isSelected();
        String mergedName = txtOutputName.getText().trim();
        if (merge && mergedName.isEmpty()) {
            setStatus("Digite um nome para o arquivo PDF mesclado.", LARANJA);
            return;
        }

        Double maxTargetSizeMB = null;
        if (chkLimitSize.isSelected()) {
            maxTargetSizeMB = (Double) spinLimitSize.getValue();
        }

        final Double targetSizeMB = maxTargetSizeMB;

        progressBar.setVisible(true);
        progressBar.setMaximum(merge ? 1 : files.size());
        progressBar.setValue(0);

        // Executar conversão em background para não travar a UI
        SwingWorker<Void, ConvertResult> worker = new SwingWorker<>() {
            int success = 0;
            int fail = 0;

            @Override
            protected Void doInBackground() {
                if (merge) {
                    try {
                        ConvertResult result = convertService.mergeAndConvertToPdf(
                                files,
                                mergedName,
                                outputDirectory.getAbsolutePath(),
                                targetSizeMB
                        );
                        publish(result);
                        success = 1;
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(1);
                            appendLog("sucesso ao converter: " + result.getPdfName());
                        });
                    } catch (Exception e) {
                        fail = 1;
                        System.err.println("Erro ao mesclar arquivos: " + e.getMessage());
                        SwingUtilities.invokeLater(() -> {
                            setStatus("Erro ao mesclar: " + e.getMessage(), VERMELHO);
                            appendLog("falha ao converter: " + mergedName + ".pdf");
                        });
                    }
                } else {
                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        try {
                            String outputName = file.getName().replaceFirst("\\.[^.]+$", "");
                            ConvertResult result = convertService.convertToPdf(
                                    file.getAbsolutePath(),
                                    outputName,
                                    outputDirectory.getAbsolutePath(),
                                    targetSizeMB
                            );
                            publish(result);
                            success++;
                            SwingUtilities.invokeLater(() -> appendLog("sucesso ao converter: " + file.getName()));
                        } catch (Exception e) {
                            fail++;
                            System.err.println("Erro ao converter " + file.getName() + ": " + e.getMessage());
                            SwingUtilities.invokeLater(() -> appendLog("falha ao converter: " + file.getName()));
                        }
                        final int progress = i + 1;
                        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<ConvertResult> chunks) {
                for (ConvertResult r : chunks) {
                    double percent = r.getCompressionPercent();
                    String percentStr = (percent >= 0 ? "+" : "") + String.format("%.1f%%", percent);
                    convertedTableModel.addRow(new Object[]{
                            r.getPdfName(),
                            r.getPdfPath(),
                            String.format("%.2f", r.getPdfSizeKB()),
                            percentStr
                    });
                }
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                String msg = merge 
                        ? (success > 0 ? "Mesclado com sucesso!" : "Falha ao mesclar arquivos.")
                        : "Conversão finalizada — " + success + " sucesso(s)";
                if (!merge && fail > 0) {
                    msg += ", " + fail + " erro(s)";
                    setStatus(msg, LARANJA);
                } else if (success > 0) {
                    setStatus(msg, LARANJA);
                } else {
                    setStatus(msg, VERMELHO);
                }
            }
        };
        worker.execute();
        setStatus(merge ? "Mesclando arquivos..." : "Convertendo " + files.size() + " arquivo(s)...", LARANJA);
    }

    private void openSelectedPdf() {
        int row = convertedTable.getSelectedRow();
        if (row < 0) {
            setStatus("Selecione um PDF para abrir.", LARANJA);
            return;
        }
        String path = (String) convertedTableModel.getValueAt(row, 1);
        try {
            Desktop.getDesktop().open(new File(path));
            setStatus("Abrindo: " + path, TEXT_PRIMARY);
        } catch (Exception e) {
            setStatus("Erro ao abrir arquivo: " + e.getMessage(), VERMELHO);
        }
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    // ════════════════════════════════════════════════════════════════
    //  COMPONENTES ESTILIZADOS
    // ════════════════════════════════════════════════════════════════

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Título da seção é colocado como titled border
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(),
                title
        );
        titledBorder.setTitleFont(FONT_SECTION);
        titledBorder.setTitleColor(TEXT_PRIMARY);

        // Combinar os borders
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createCompoundBorder(
                        titledBorder,
                        new EmptyBorder(4, 8, 8, 8)
                )
        ));

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = hovering
                        ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 40)
                        : new Color(color.getRed(), color.getGreen(), color.getBlue(), 25);

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(color);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 24, 34));
        return button;
    }

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_STATUS);
        button.setForeground(TEXT_SECONDARY);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_SECONDARY);
            }
        });
        return button;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_TABLE);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setFont(FONT_TABLE);
        table.setRowHeight(28);
        table.setSelectionBackground(new Color(LARANJA.getRed(), LARANJA.getGreen(), LARANJA.getBlue(), 50));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Header estilizado
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_HEADER);
        header.setForeground(TEXT_SECONDARY);
        header.setFont(FONT_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);

        // Renderer do header
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setBackground(BG_HEADER);
                label.setForeground(TEXT_SECONDARY);
                label.setFont(FONT_HEADER);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                        new EmptyBorder(6, 8, 6, 8)
                ));
                return label;
            }
        });

        // Renderer padrão para as células
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_TABLE : BG_PANEL);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        cellRenderer.setForeground(TEXT_PRIMARY);
        for (int i = 0; i < model.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        return table;
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(BG_TABLE);
        scrollPane.setBackground(BG_TABLE);

        // Estilizar scrollbars
        scrollPane.getVerticalScrollBar().setBackground(BG_PANEL);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = BG_PANEL;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
        });

        return scrollPane;
    }
}
