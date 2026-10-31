package com.mlproject.gui;

import com.mlproject.algorithms.DecisionTreeClassifier;
import com.mlproject.algorithms.KNNClassifier;
import com.mlproject.core.BaseAlgorithm;
import com.mlproject.data.DataLoader;
import com.mlproject.data.UserRecord;
import com.mlproject.preprocessing.PreProcessor;
import com.mlproject.evaluation.Evaluator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryLabelPositions;

import java.io.Serial;
import java.text.DecimalFormat;
import java.io.File;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.lang.management.ManagementFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

// Main window. The UI texts are in Turkish because the dataset is Turkish.
public class MainFrame extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient List<UserRecord> processedRecords;
    private transient List<UserRecord> separateTrainRecords;
    private transient List<UserRecord> separateTestRecords;

    private static class ModelRunResult {
        String displayText;
        double accuracy;
        double time;
        JCheckBox checkBox;

        public ModelRunResult(String text, double acc, double time) {
            this.displayText = text;
            this.accuracy = acc;
            this.time = time;
        }
    }

    private final List<ModelRunResult> runHistory = new ArrayList<>();
    private int runCounter = 1;
    private JPanel historyListPanel;
    private JButton btnCompare;

    private JTabbedPane tabbedPaneLoading;
    private JButton btnSelectFile;
    private JLabel lblFilePath;

    private JButton btnSelectTrainFile;
    private JLabel lblTrainPath;
    private JButton btnSelectTestFile;
    private JLabel lblTestPath;

    private JButton btnReset;

    private JComboBox<String> cmbAlgorithm;
    private JLabel lblParamName;
    private JTextField txtParamValue;
    private JTextField txtTrainRatio;
    private JButton btnRunModel;

    private JTextPane txtResults;
    private final StringBuilder htmlContent = new StringBuilder();

    private DefaultCategoryDataset chartDataset;
    private DefaultCategoryDataset timeDataset;
    private ChartPanel chartPanel;
    private ChartPanel timeChartPanel;

    private static final Color BG_WINDOW = new Color(28, 28, 30);
    private static final Color BG_CONTROL = new Color(44, 44, 46);
    private static final Color ACCENT_BLUE = new Color(10, 132, 255);
    private static final Color SUCCESS_GREEN2 = new Color(29, 129, 54);
    private static final Color ERROR_RED = new Color(255, 69, 58);
    private static final Color ACCENT_INDIGO = new Color(94, 92, 230);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(152, 152, 157);
    private static final Color BORDER_COLOR = new Color(56, 56, 58);
    private static final Color BORDER_LIGHT = new Color(51, 51, 51);


    private static final Font FONT_SYSTEM_TITLE = new Font("Helvetica Neue", Font.BOLD, 13);
    private static final Font FONT_SYSTEM_BOLD = new Font("Helvetica Neue", Font.BOLD, 11);
    private static final Font FONT_SYSTEM_REG = new Font("Helvetica Neue", Font.PLAIN, 11);

    public MainFrame() {
        setTitle("Makine Öğrenmesi Sınıflandırma Aracı");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_WINDOW);

        setLayout(new BorderLayout(0, 0));

        add(createLeftPanel(), BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout(0, 0));
        mainContent.setBackground(BG_WINDOW);
        mainContent.setOpaque(true);
        mainContent.add(createTopPanel(), BorderLayout.NORTH);
        mainContent.add(createCenterPanel(), BorderLayout.CENTER);
        mainContent.add(createRightChartPanel(), BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);

        registerEventListeners();

        clearHtmlConsole();
    }

    private JPanel createTopPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_WINDOW);
        wrapper.setBorder(new EmptyBorder(8, 8, 8, 8));

        tabbedPaneLoading = new JTabbedPane();
        tabbedPaneLoading.setFont(FONT_SYSTEM_REG);
        tabbedPaneLoading.setBackground(BG_WINDOW);
        tabbedPaneLoading.setForeground(TEXT_PRIMARY);
        tabbedPaneLoading.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                lightHighlight = BG_WINDOW;
                highlight = BG_WINDOW;
                shadow = BORDER_COLOR;
                darkShadow = BG_WINDOW;
                focus = BG_WINDOW;
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
                g.setColor(BORDER_COLOR);
                g.drawRect(x, y, w, h);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
                g.setColor(isSelected ? BG_CONTROL : BG_WINDOW);
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                int width = tabPane.getWidth();
                int height = tabPane.getHeight();
                Insets insets = tabPane.getInsets();

                int x = insets.left;
                int y = insets.top;
                int w = width - insets.right - insets.left;
                int h = height - insets.top - insets.bottom;

                y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);

                g.setColor(BORDER_COLOR);
                g.drawRect(x, y, w - 1, h - 1);
            }
        });


        JPanel pnlSingle = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlSingle.setBackground(BG_WINDOW);

        btnSelectFile = new RoundedButton("Excel Seç (.xlsx)", SUCCESS_GREEN2, 6);
        lblFilePath = new JLabel("Henüz bir dosya seçilmedi.");
        lblFilePath.setFont(FONT_SYSTEM_REG);
        lblFilePath.setForeground(TEXT_SECONDARY);

        JLabel lblRatio = new JLabel("Eğitim Oranı (%):");
        lblRatio.setFont(FONT_SYSTEM_TITLE);
        lblRatio.setForeground(TEXT_PRIMARY);

        txtTrainRatio = new JTextField("80", 3);
        txtTrainRatio.setFont(FONT_SYSTEM_REG);
        txtTrainRatio.setBackground(BG_CONTROL);
        txtTrainRatio.setForeground(TEXT_PRIMARY);
        txtTrainRatio.setCaretColor(TEXT_PRIMARY);
        txtTrainRatio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 5, 5, 5)));

        pnlSingle.add(btnSelectFile);
        pnlSingle.add(lblFilePath);
        pnlSingle.add(Box.createHorizontalStrut(20));
        pnlSingle.add(lblRatio);
        pnlSingle.add(txtTrainRatio);


        JPanel pnlSeparate = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlSeparate.setBackground(BG_WINDOW);

        btnSelectTrainFile = new RoundedButton("Eğitim Yükle", SUCCESS_GREEN2, 6);
        lblTrainPath = new JLabel("Eğitim seçilmedi.");
        lblTrainPath.setFont(FONT_SYSTEM_REG);
        lblTrainPath.setForeground(TEXT_SECONDARY);

        btnSelectTestFile = new RoundedButton("Test Yükle", ACCENT_BLUE, 6);
        lblTestPath = new JLabel("Test seçilmedi.");
        lblTestPath.setFont(FONT_SYSTEM_REG);
        lblTestPath.setForeground(TEXT_SECONDARY);

        pnlSeparate.add(btnSelectTrainFile);
        pnlSeparate.add(lblTrainPath);
        pnlSeparate.add(Box.createHorizontalStrut(20));
        pnlSeparate.add(btnSelectTestFile);
        pnlSeparate.add(lblTestPath);

        tabbedPaneLoading.addTab("Tek Dosya (Ayrım)", pnlSingle);
        tabbedPaneLoading.addTab("İki Dosya (Eğitim + Test)", pnlSeparate);

        wrapper.add(tabbedPaneLoading, BorderLayout.CENTER);

        JPanel eastWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        eastWrapper.setBorder(new EmptyBorder(24, 0, 0, 0));
        eastWrapper.setBackground(BG_WINDOW);
        btnReset = new RoundedButton("Sıfırla", BG_CONTROL, 6);
        btnReset.setForeground(ERROR_RED);
        eastWrapper.add(btnReset);
        wrapper.add(eastWrapper, BorderLayout.EAST);

        return wrapper;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_WINDOW);
        panel.setOpaque(true);
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                new EmptyBorder(25, 20, 25, 20)));


        JLabel lblAlgoTitle = new JLabel("Algoritma Seçimi");
        lblAlgoTitle.setFont(FONT_SYSTEM_TITLE);
        lblAlgoTitle.setForeground(TEXT_PRIMARY);
        lblAlgoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblAlgoTitle);
        panel.add(Box.createVerticalStrut(10));

        cmbAlgorithm = new JComboBox<>(new String[] { "K-Nearest Neighbors", "Decision Tree" });
        cmbAlgorithm.setFont(FONT_SYSTEM_REG);
        cmbAlgorithm.setBackground(BG_CONTROL);
        cmbAlgorithm.setForeground(TEXT_PRIMARY);
        cmbAlgorithm.setOpaque(true);
        cmbAlgorithm.setFocusable(false);
        cmbAlgorithm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cmbAlgorithm.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbAlgorithm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(3, 8, 3, 8)));
        cmbAlgorithm.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Serial
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BG_CONTROL);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        int size = 6;
                        int x = (getWidth() - size) / 2;
                        int y = (getHeight() - (size / 2)) / 2;
                        g2.setColor(TEXT_SECONDARY);
                        int[] xPoints = { x, x + size, x + size / 2 };
                        int[] yPoints = { y, y, y + size / 2 + 1 };
                        g2.fillPolygon(xPoints, yPoints, 3);
                        g2.dispose();
                    }
                };
                button.setBackground(BG_CONTROL);
                button.setBorder(BorderFactory.createEmptyBorder());
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_CONTROL);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        panel.add(cmbAlgorithm);
        panel.add(Box.createVerticalStrut(20));


        lblParamName = new JLabel("K Değeri:");
        lblParamName.setFont(FONT_SYSTEM_TITLE);
        lblParamName.setForeground(TEXT_PRIMARY);
        lblParamName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblParamName);
        panel.add(Box.createVerticalStrut(10));

        txtParamValue = new JTextField("3");
        txtParamValue.setFont(FONT_SYSTEM_REG);
        txtParamValue.setBackground(BG_CONTROL);
        txtParamValue.setForeground(TEXT_PRIMARY);
        txtParamValue.setCaretColor(TEXT_PRIMARY);
        txtParamValue.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)));
        txtParamValue.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtParamValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtParamValue);
        panel.add(Box.createVerticalStrut(20));


        btnRunModel = new RoundedButton("Modeli Çalıştır", TEXT_SECONDARY, 6);
        btnRunModel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRunModel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(btnRunModel);
        panel.add(Box.createVerticalStrut(40));


        JLabel lblHistoryTitle = new JLabel("Karşılaştırma Geçmişi");
        lblHistoryTitle.setFont(FONT_SYSTEM_TITLE);
        lblHistoryTitle.setForeground(TEXT_PRIMARY);
        lblHistoryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblHistoryTitle);
        panel.add(Box.createVerticalStrut(10));

        historyListPanel = new JPanel();
        historyListPanel.setLayout(new BoxLayout(historyListPanel, BoxLayout.Y_AXIS));
        historyListPanel.setBackground(BG_WINDOW);
        historyListPanel.setOpaque(true);

        JScrollPane historyScroll = new JScrollPane(historyListPanel);
        historyScroll.setPreferredSize(new Dimension(200, 180));
        historyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        historyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyScroll.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, BORDER_COLOR));
        historyScroll.setBackground(BG_WINDOW);
        historyScroll.getViewport().setBackground(BG_WINDOW);
        historyScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        applyMacScrollbarStyle(historyScroll);
        panel.add(historyScroll);

        panel.add(Box.createVerticalStrut(20));

        btnCompare = new RoundedButton("Seçilenleri Kıyasla", TEXT_SECONDARY, 6);
        btnCompare.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCompare.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(btnCompare);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_WINDOW);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(12, 18, 18, 12));

        txtResults = new JTextPane();
        txtResults.setEditable(false);
        txtResults.setBackground(BG_WINDOW);
        txtResults.setOpaque(true);
        txtResults.setBorder(BorderFactory.createEmptyBorder());

        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                "body { font-family: '-apple-system', 'SF Pro Text', 'Helvetica Neue', sans-serif; font-size: 11px; line-height: 1.2; margin: 5px; color: #FFFFFF; background-color: #1C1C1E; }");
        styleSheet.addRule(
                ".card { background-color: #2C2C2E; padding: 12px; border-radius: 8px; margin-bottom: 15px; border: 0.5px solid #333333; }");
        styleSheet.addRule(
                ".card-title { color: #FFFFFF; font-size: 13px; font-weight: bold; margin-bottom: 8px; border-bottom: 0.5px solid #333333; padding-bottom: 6px; }");
        styleSheet.addRule("table { width: 100%; border-collapse: collapse; }");
        styleSheet.addRule(
                "th, td { padding: 4px 6px; text-align: left; border: 0.5px solid #333333; font-size: 11px; }");
        styleSheet.addRule("th { color: rgba(235, 235, 245, 0.6); font-weight: normal; }");
        styleSheet.addRule(".correct { color: #30D158; font-weight: bold; }");
        styleSheet.addRule(".error { color: #FF453A; font-weight: bold; }");
        styleSheet.addRule(
                ".log { color: rgba(235, 235, 245, 0.6); font-family: 'SF Mono', Consolas, monospace; font-size: 10px; margin: 3px 0; border-left: 2px solid #0A84FF; padding-left: 6px; }");

        txtResults.setEditorKit(kit);

        JScrollPane scrollPane = new JScrollPane(txtResults);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.setBackground(BG_WINDOW);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setBackground(BG_WINDOW);
        scrollPane.getViewport().setOpaque(true);
        applyMacScrollbarStyle(scrollPane);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightChartPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 15));
        panel.setBackground(BG_WINDOW);
        panel.setOpaque(true);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(new EmptyBorder(12, 12, 18, 18));


        JPanel pnlAccuracy = new JPanel(new BorderLayout());
        pnlAccuracy.setBackground(BG_WINDOW);

        JLabel lblChartTitle = new JLabel("Doğruluk Analizi");
        lblChartTitle.setFont(FONT_SYSTEM_TITLE);
        lblChartTitle.setForeground(TEXT_PRIMARY);
        lblChartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        chartDataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart(null, "Model", "Doğruluk (%)", chartDataset,
                PlotOrientation.VERTICAL, false, true, false);

        chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        chart.getRenderingHints().put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        chart.getRenderingHints().put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        chart.getRenderingHints().put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        chart.setTextAntiAlias(true);
        chart.setAntiAlias(true);
        chart.setBackgroundPaint(Color.BLACK);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.BLACK);
        plot.setDomainGridlinePaint(BORDER_LIGHT);
        plot.setRangeGridlinePaint(BORDER_LIGHT);
        plot.setOutlinePaint(null);

        Font chartFont = new Font("Helvetica Neue", Font.PLAIN, 10);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(TEXT_PRIMARY);
        domainAxis.setLabelPaint(TEXT_SECONDARY);
        domainAxis.setTickLabelFont(chartFont);
        domainAxis.setLabelFont(chartFont);
        domainAxis.setCategoryMargin(0.35);
        domainAxis.setAxisLinePaint(BORDER_LIGHT);
        domainAxis.setMaximumCategoryLabelWidthRatio(2.0f);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(TEXT_SECONDARY);
        rangeAxis.setLabelPaint(TEXT_SECONDARY);
        rangeAxis.setTickLabelFont(chartFont);
        rangeAxis.setLabelFont(chartFont);
        rangeAxis.setRange(0.0, 100.0);
        rangeAxis.setAxisLinePaint(BORDER_LIGHT);

        Color excelGreen = new Color(16, 124, 65);

        BarRenderer renderer = new BarRenderer() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Paint getItemPaint(int row, int column) {
                String cat = (String) chartDataset.getColumnKey(column);
                return cat.contains("KNN") ? ACCENT_INDIGO : excelGreen;
            }
        };

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}%", new DecimalFormat("0.0")));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelPaint(Color.WHITE);
        renderer.setDefaultItemLabelFont(new Font("San Francisco Text", Font.BOLD, 12));
        ItemLabelPosition position = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
        renderer.setDefaultPositiveItemLabelPosition(position);

        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.5);
        plot.setRenderer(renderer);

        chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.BLACK);
        chartPanel.setOpaque(true);
        chartPanel.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        chartPanel.setPopupMenu(null);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        pnlAccuracy.add(lblChartTitle, BorderLayout.NORTH);
        pnlAccuracy.add(chartPanel, BorderLayout.CENTER);


        JPanel pnlTime = new JPanel(new BorderLayout());
        pnlTime.setBackground(BG_WINDOW);

        JLabel lblTimeTitle = new JLabel("Çalışma Süresi (ms)");
        lblTimeTitle.setFont(FONT_SYSTEM_TITLE);
        lblTimeTitle.setForeground(TEXT_PRIMARY);
        lblTimeTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        timeDataset = new DefaultCategoryDataset();
        JFreeChart timeChart = ChartFactory.createBarChart(null, "Model", "Süre (ms)", timeDataset,
                PlotOrientation.VERTICAL, false, true, false);

        timeChart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        timeChart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        timeChart.getRenderingHints().put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        timeChart.getRenderingHints().put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        timeChart.getRenderingHints().put(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        timeChart.setTextAntiAlias(true);
        timeChart.setAntiAlias(true);
        timeChart.setBackgroundPaint(Color.BLACK);

        CategoryPlot timePlot = timeChart.getCategoryPlot();
        timePlot.setBackgroundPaint(Color.BLACK);
        timePlot.setDomainGridlinePaint(BORDER_LIGHT);
        timePlot.setRangeGridlinePaint(BORDER_LIGHT);
        timePlot.setOutlinePaint(null);

        CategoryAxis timeDomainAxis = timePlot.getDomainAxis();
        timeDomainAxis.setTickLabelPaint(TEXT_PRIMARY);
        timeDomainAxis.setLabelPaint(TEXT_SECONDARY);
        timeDomainAxis.setTickLabelFont(chartFont);
        timeDomainAxis.setLabelFont(chartFont);
        timeDomainAxis.setCategoryMargin(0.35);
        timeDomainAxis.setAxisLinePaint(BORDER_LIGHT);
        timeDomainAxis.setMaximumCategoryLabelWidthRatio(2.0f);
        timeDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ValueAxis timeRangeAxis = timePlot.getRangeAxis();
        timeRangeAxis.setTickLabelPaint(TEXT_SECONDARY);
        timeRangeAxis.setLabelPaint(TEXT_SECONDARY);
        timeRangeAxis.setTickLabelFont(chartFont);
        timeRangeAxis.setLabelFont(chartFont);
        timeRangeAxis.setAxisLinePaint(BORDER_LIGHT);

        BarRenderer timeRenderer = new BarRenderer() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Paint getItemPaint(int row, int column) {
                String cat = (String) timeDataset.getColumnKey(column);
                return cat.contains("KNN") ? ACCENT_INDIGO : excelGreen;
            }
        };

        timeRenderer
                .setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0")));
        timeRenderer.setDefaultItemLabelsVisible(true);
        timeRenderer.setDefaultItemLabelPaint(Color.WHITE);
        timeRenderer.setDefaultItemLabelFont(new Font("San Francisco Text", Font.BOLD, 12));
        timeRenderer.setDefaultPositiveItemLabelPosition(position);

        timeRenderer.setBarPainter(new StandardBarPainter());
        timeRenderer.setDrawBarOutline(false);
        timeRenderer.setShadowVisible(false);
        timeRenderer.setMaximumBarWidth(0.5);
        timePlot.setRenderer(timeRenderer);

        timeChartPanel = new ChartPanel(timeChart);
        timeChartPanel.setBackground(Color.BLACK);
        timeChartPanel.setOpaque(true);
        timeChartPanel.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        timeChartPanel.setPopupMenu(null);
        timeChartPanel.setDomainZoomable(false);
        timeChartPanel.setRangeZoomable(false);

        pnlTime.add(lblTimeTitle, BorderLayout.NORTH);
        pnlTime.add(timeChartPanel, BorderLayout.CENTER);

        panel.add(pnlAccuracy);
        panel.add(pnlTime);

        return panel;
    }

    private void applyMacScrollbarStyle(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(69, 69, 69);
                this.trackColor = MainFrame.BG_WINDOW;
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
                JButton jb = new JButton();
                jb.setPreferredSize(new Dimension(0, 0));
                return jb;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
                    return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 3, thumbBounds.y + 3, thumbBounds.width - 6, thumbBounds.height - 6, 8,
                        8);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        });
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));

        scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(69, 69, 69);
                this.trackColor = MainFrame.BG_WINDOW;
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
                JButton jb = new JButton();
                jb.setPreferredSize(new Dimension(0, 0));
                return jb;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
                    return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 3, thumbBounds.y + 3, thumbBounds.width - 6, thumbBounds.height - 6, 8,
                        8);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        });
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
    }

    private void registerEventListeners() {
        btnSelectFile.addActionListener(e -> onSelectFile(0));
        btnSelectTrainFile.addActionListener(e -> onSelectFile(1));
        btnSelectTestFile.addActionListener(e -> onSelectFile(2));
        cmbAlgorithm.addActionListener(e -> onAlgorithmChanged());
        btnRunModel.addActionListener(e -> onRunModel());
        btnCompare.addActionListener(e -> onCompareModels());
        btnReset.addActionListener(e -> onReset());
    }

    private void onReset() {
        processedRecords = null;
        separateTrainRecords = null;
        separateTestRecords = null;

        lblFilePath.setText("Henüz bir dosya seçilmedi.");
        lblFilePath.setForeground(TEXT_SECONDARY);
        lblTrainPath.setText("Eğitim seçilmedi.");
        lblTrainPath.setForeground(TEXT_SECONDARY);
        lblTestPath.setText("Test seçilmedi.");
        lblTestPath.setForeground(TEXT_SECONDARY);

        btnSelectFile.setEnabled(true);
        btnSelectTrainFile.setEnabled(true);
        btnSelectTestFile.setEnabled(true);

        txtTrainRatio.setText("80");
        cmbAlgorithm.setSelectedIndex(0);

        runHistory.clear();
        runCounter = 1;
        historyListPanel.removeAll();
        historyListPanel.revalidate();
        historyListPanel.repaint();

        clearHtmlConsole();
        btnCompare.setEnabled(false);
        btnRunModel.setEnabled(true);
        btnRunModel.setText("Modeli Çalıştır");

        if (chartDataset != null)
            chartDataset.clear();
        if (timeDataset != null)
            timeDataset.clear();
    }

    private void onSelectFile(int mode) {
        FileDialog fileDialog = new FileDialog(this, "Excel Dosyası Seçin", FileDialog.LOAD);
        fileDialog.setFile("*.xlsx");

        String userHome = System.getProperty("user.home");
        File desktop = new File(userHome, "Desktop");

        if (!desktop.exists()) {
            desktop = new File(userHome, "OneDrive" + File.separator + "Desktop");
        }

        String initialDir = desktop.exists() ? desktop.getAbsolutePath() : userHome;
        fileDialog.setDirectory(initialDir + File.separator);

        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String file = fileDialog.getFile();

        if (directory != null && file != null) {
            String filePath = new File(directory, file).getAbsolutePath();

            if (mode == 0) {
                lblFilePath.setText(file);
                lblFilePath.setForeground(ACCENT_BLUE);
                btnSelectFile.setEnabled(false);
            } else if (mode == 1) {
                lblTrainPath.setText(file);
                lblTrainPath.setForeground(ACCENT_BLUE);
                btnSelectTrainFile.setEnabled(false);
            } else if (mode == 2) {
                lblTestPath.setText(file);
                lblTestPath.setForeground(ACCENT_BLUE);
                btnSelectTestFile.setEnabled(false);
            }

            appendHtmlLog("Veri seti yüklemesi başlatılıyor: " + file);

            btnRunModel.setEnabled(false);
            btnCompare.setEnabled(false);

            SwingWorker<Void, String> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    publish("Veriler okunuyor...");
                    DataLoader loader = new DataLoader();
                    List<UserRecord> tempRecords = loader.loadData(filePath);

                    int count = (tempRecords != null) ? tempRecords.size() : 0;
                    publish(count + " kayıt başarıyla yüklendi.");

                    if (count > 0) {
                        if (mode == 0) {
                            processedRecords = tempRecords;
                        } else if (mode == 1) {
                            separateTrainRecords = tempRecords;
                        } else if (mode == 2) {
                            separateTestRecords = tempRecords;
                        }
                        publish("Dosya belleğe yüklendi. Ön işleme model çalıştırılırken yapılacak.");
                    } else {
                        publish("Uyarı: Dosya boş veya okunamadı.");
                        if (mode == 0)
                            processedRecords = null;
                        else if (mode == 1)
                            separateTrainRecords = null;
                        else if (mode == 2)
                            separateTestRecords = null;
                    }

                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String msg : chunks) {
                        appendHtmlLog(msg);
                    }
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Dosya yüklenirken hata oluştu:\n" + ex.getMessage(),
                                "Hata", JOptionPane.ERROR_MESSAGE);
                        if (mode == 0) {
                            lblFilePath.setText("Dosya yüklenemedi!");
                            lblFilePath.setForeground(ERROR_RED);
                            processedRecords = null;
                        } else if (mode == 1) {
                            lblTrainPath.setText("Yüklenemedi!");
                            lblTrainPath.setForeground(ERROR_RED);
                            separateTrainRecords = null;
                        } else if (mode == 2) {
                            lblTestPath.setText("Yüklenemedi!");
                            lblTestPath.setForeground(ERROR_RED);
                            separateTestRecords = null;
                        }
                    } finally {
                        if (mode == 0)
                            btnSelectFile.setEnabled(true);
                        else if (mode == 1)
                            btnSelectTrainFile.setEnabled(true);
                        else if (mode == 2)
                            btnSelectTestFile.setEnabled(true);

                        btnRunModel.setEnabled(true);
                        btnCompare.setEnabled(runHistory.size() > 1);
                    }
                }
            };
            worker.execute();
        }
    }

    private void onAlgorithmChanged() {
        if ("K-Nearest Neighbors".equals(cmbAlgorithm.getSelectedItem())) {
            lblParamName.setText("K Değeri:");
            txtParamValue.setText("3");
        } else {
            lblParamName.setText("Maks. Derinlik:");
            txtParamValue.setText("5");
        }
    }

    private void onRunModel() {
        int tabIndex = tabbedPaneLoading.getSelectedIndex();
        boolean isSingleFileMode = (tabIndex == 0);

        if (isSingleFileMode) {
            if (processedRecords == null || processedRecords.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen önce veri seti yükleyin!", "Uyarı",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            if (separateTrainRecords == null || separateTrainRecords.isEmpty() || separateTestRecords == null
                    || separateTestRecords.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen hem eğitim hem de test verilerini yükleyiniz!",
                        "Eksik Veri", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String paramText = txtParamValue.getText().trim();
        final int paramValue;
        try {
            paramValue = Integer.parseInt(paramText);
            if (paramValue <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Parametre geçerli bir pozitif tam sayı olmalıdır!",
                    "Geçersiz Parametre", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final double trainRatio;
        if (isSingleFileMode) {
            String ratioText = txtTrainRatio.getText().trim();
            try {
                int ratioInt = Integer.parseInt(ratioText);
                if (ratioInt <= 0 || ratioInt >= 100)
                    throw new NumberFormatException();
                trainRatio = ratioInt / 100.0;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Eğitim oranı 1 ile 99 arasında bir tam sayı olmalıdır!\n(Örn: 80 → %80 eğitim, %20 test)",
                        "Geçersiz Oran", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            trainRatio = 0.0;
        }

        final String selectedAlgo = (String) cmbAlgorithm.getSelectedItem();

        btnRunModel.setEnabled(false);
        btnRunModel.setText("Çalışıyor...");
        btnSelectFile.setEnabled(false);
        btnSelectTrainFile.setEnabled(false);
        btnSelectTestFile.setEnabled(false);
        btnCompare.setEnabled(false);
        appendHtmlLog(
                "<span style='color:#0A84FF;font-weight:bold;'>&#9654; Model eğitiliyor, lütfen bekleyin...</span>");

        SwingWorker<double[], Void> worker = new SwingWorker<>() {
            long startTime;
            int trainSize, testSize;
            BaseAlgorithm classifier;

            @Override
            protected double[] doInBackground() {
                if ("K-Nearest Neighbors".equals(selectedAlgo)) {
                    classifier = new KNNClassifier(paramValue);
                } else {
                    classifier = new DecisionTreeClassifier(paramValue);
                }

                List<UserRecord> actualTrain;
                List<UserRecord> actualTest;

                PreProcessor preProcessor = new PreProcessor();

                if (isSingleFileMode) {
                    // Split first, then learn the min-max values only from the training part.
                    // Otherwise information from the test data leaks into training.
                    Evaluator evaluator = new Evaluator();
                    evaluator.splitData(new ArrayList<>(processedRecords), trainRatio);
                    actualTrain = evaluator.getTrainData();
                    actualTest = evaluator.getTestData();

                    preProcessor.fitAndTransform(actualTrain);
                    preProcessor.transform(actualTest);
                } else {
                    actualTrain = new ArrayList<>(separateTrainRecords);
                    actualTest = new ArrayList<>(separateTestRecords);

                    preProcessor.fitAndTransform(actualTrain);
                    preProcessor.transform(actualTest);
                }

                trainSize = actualTrain.size();
                testSize = actualTest.size();

                com.sun.management.ThreadMXBean threadMXBean = (com.sun.management.ThreadMXBean) ManagementFactory
                        .getThreadMXBean();
                long threadId = Thread.currentThread().threadId();
                long memBefore = threadMXBean.getThreadAllocatedBytes(threadId);

                startTime = System.currentTimeMillis();
                classifier.train(actualTrain);
                double accuracy = classifier.calculateAccuracy(actualTest);
                long elapsed = System.currentTimeMillis() - startTime;

                long memAfter = threadMXBean.getThreadAllocatedBytes(threadId);
                long memoryDiff = memAfter - memBefore;
                double ramUsedMB = Math.max(0L, memoryDiff) / (1024.0 * 1024.0);

                return new double[] { accuracy, elapsed, ramUsedMB };
            }

            @Override
            protected void done() {
                try {
                    double[] result = get();
                    double accuracy = result[0];
                    long elapsed = (long) result[1];
                    double ramUsedMB = result[2];

                    String algoShort = "K-Nearest Neighbors".equals(selectedAlgo) ? "KNN" : "Decision Tree";
                    String algDetail = "K-Nearest Neighbors".equals(selectedAlgo) ? "(K=" + paramValue + ")"
                            : "(MaxD=" + paramValue + ")";
                    String displayText = algoShort + " " + algDetail + " #" + runCounter;

                    StringBuilder resBody = new StringBuilder();
                    resBody.append("<div class='card'>");
                    resBody.append("<div class='card-title'>Çalıştırma Özeti: ").append(displayText).append("</div>");
                    resBody.append("<div class='card-content'><table>");
                    resBody.append(
                            "<tr><th>Parametre</th><th>Eğitim Verisi</th><th>Test Verisi</th><th>Süre</th><th>Doğruluk</th></tr>");
                    String paramName = "K-Nearest Neighbors".equals(selectedAlgo) ? "K = " : "Max Derinlik = ";
                    resBody.append("<tr>");
                    resBody.append("<td><b>").append(paramName).append(paramValue).append("</b></td>");
                    resBody.append("<td>").append(trainSize).append("</td>");
                    resBody.append("<td>").append(testSize).append("</td>");
                    resBody.append("<td>").append(elapsed).append(" ms (RAM Tüketimi: ")
                            .append(String.format("%.2f", ramUsedMB)).append(" MB)</td>");
                    resBody.append("<td style='color:#0A84FF;'><b>").append(String.format("%.2f%%", accuracy))
                            .append("</b></td>");
                    resBody.append("</tr></table></div></div>");

                    resBody.append("<div class='card'>");
                    resBody.append("<div class='card-title'>Hata Matrisi Analizi (Matris Görünümü): ")
                            .append(displayText).append("</div>");
                    resBody.append("<div class='card-content'>");

                    java.util.Map<String, java.util.Map<String, Integer>> rawMatrix = classifier
                            .getRawConfusionMatrix();
                    if (rawMatrix == null || rawMatrix.isEmpty()) {
                        resBody.append("<p>Veri yok.</p></div></div>");
                    } else {
                        java.util.Set<String> uniqueCategories = new java.util.TreeSet<>();
                        for (String actual : rawMatrix.keySet()) {
                            uniqueCategories.add(actual);
                            uniqueCategories.addAll(rawMatrix.get(actual).keySet());
                        }
                        java.util.List<String> categories = new java.util.ArrayList<>(uniqueCategories);

                        resBody.append(
                                "<table style='width:auto; max-width:100%; font-size:9px; margin:0 auto; text-align:center;'><tr><th style='text-align:left; padding:2px 4px;'>Gerçek \\ Tahmin</th>");
                        for (String cat : categories) {
                            resBody.append("<th style='padding:2px 4px;'>").append(cat).append("</th>");
                        }
                        resBody.append("</tr>");

                        for (String actual : categories) {
                            resBody.append("<tr>");
                            resBody.append("<td style='text-align:left; padding:2px 4px;'><b>").append(actual)
                                    .append("</b></td>");
                            for (String predicted : categories) {
                                int count = 0;
                                if (rawMatrix.containsKey(actual) && rawMatrix.get(actual).containsKey(predicted)) {
                                    count = rawMatrix.get(actual).get(predicted);
                                }

                                if (actual.equals(predicted)) {
                                    resBody.append(
                                            "<td style='background-color:#30D158; color:#000000; font-weight:bold; text-align:center; padding:2px 4px;'>")
                                            .append(count).append("</td>");
                                } else {
                                    if (count > 0) {
                                        resBody.append(
                                                "<td style='background-color:#D32F2F; color:#FFFFFF; font-weight:bold; text-align:center; padding:2px 4px;'>")
                                                .append(count).append("</td>");
                                    } else {
                                        resBody.append(
                                                "<td style='background-color:#000000; color:#FFFFFF; text-align:center; padding:2px 4px;'>")
                                                .append(count).append("</td>");
                                    }
                                }
                            }
                            resBody.append("</tr>");
                        }
                        resBody.append("</table></div></div>");
                    }

                    resBody.append("<div class='card'>");
                    resBody.append("<div class='card-title'>Hata Matrisi Analizi (Liste Görünümü): ")
                            .append(displayText).append("</div>");
                    resBody.append("<div class='card-content'>");
                    resBody.append(
                            "<table><tr><th>Gerçek Kategori</th><th>Tahmin Edilen</th><th>Sayı</th><th>Durum</th></tr>");

                    // Rows are built directly from the raw confusion matrix
                    if (rawMatrix != null) {
                        for (java.util.Map.Entry<String, java.util.Map<String, Integer>> row : rawMatrix.entrySet()) {
                            String txtActual = displayCategory(row.getKey());
                            for (java.util.Map.Entry<String, Integer> cell : row.getValue().entrySet()) {
                                String txtPredicted = displayCategory(cell.getKey());
                                boolean correct = txtActual.equals(txtPredicted);
                                resBody.append("<tr>");
                                resBody.append("<td><b>").append(txtActual).append("</b></td>");
                                resBody.append("<td>").append(txtPredicted).append("</td>");
                                resBody.append("<td>").append(cell.getValue()).append("</td>");
                                resBody.append("<td class='").append(correct ? "correct" : "error").append("'>")
                                        .append(correct ? "[Doğru Tahmin]" : "[Hatalı Tahmin]").append("</td>");
                                resBody.append("</tr>");
                            }
                        }
                    }
                    resBody.append("</table></div></div>");

                    appendHtml(resBody.toString());

                    String htmlLabel = "<html><span style='font-family:\"Helvetica Neue\", sans-serif; font-weight:bold; font-size:11px; color:#FFFFFF;'>"
                            + displayText
                            + "</span> <span style='font-family:\"Helvetica Neue\", sans-serif; font-size:11px; color:#98989D;'>"
                            + String.format("%.1f%%", accuracy) + "</span></html>";

                    runCounter++;

                    ModelRunResult resultObj = new ModelRunResult(displayText, accuracy, elapsed);
                    JCheckBox chk = new JCheckBox(htmlLabel);
                    chk.setIcon(new AppleCheckBoxIcon());
                    chk.setForeground(TEXT_PRIMARY);
                    chk.setBackground(BG_WINDOW);
                    chk.setFocusPainted(false);
                    chk.setBorderPainted(false);
                    chk.setOpaque(true);
                    chk.setSelected(true);
                    chk.setBorder(new EmptyBorder(6, 4, 6, 4));
                    chk.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    resultObj.checkBox = chk;
                    runHistory.add(resultObj);

                    historyListPanel.add(chk);
                    historyListPanel.revalidate();
                    historyListPanel.repaint();

                    chartDataset.clear();
                    timeDataset.clear();
                    updateCharts(displayText, accuracy, elapsed);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Model çalışırken beklenmeyen bir hata oluştu:\n" + ex.getMessage(),
                            "Model Hatası", JOptionPane.ERROR_MESSAGE);
                    appendHtmlLog("<span style='color:#FF453A;font-weight:bold;'>&#9888; Hata: " + ex.getMessage()
                            + "</span>");
                } finally {
                    btnRunModel.setEnabled(true);
                    btnRunModel.setText("Modeli Çalıştır");
                    btnSelectFile.setEnabled(true);
                    btnSelectTrainFile.setEnabled(true);
                    btnSelectTestFile.setEnabled(true);
                    btnCompare.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // Empty category names are shown as [BİLİNMEYEN] in the confusion matrix list
    private static String displayCategory(String category) {
        return (category == null || category.trim().isEmpty()) ? "[BİLİNMEYEN]" : category;
    }

    private void onCompareModels() {
        chartDataset.clear();
        timeDataset.clear();
        boolean anySelected = false;
        for (ModelRunResult res : runHistory) {
            if (res.checkBox != null && res.checkBox.isSelected()) {
                updateCharts(res.displayText, res.accuracy, res.time);
                anySelected = true;
            }
        }
        if (!anySelected)
            JOptionPane.showMessageDialog(this, "Kıyaslama için listeden en az bir model seçmelisiniz!", "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
    }

    private void updateCharts(String displayName, double accuracy, double time) {
        chartDataset.setValue(accuracy, "Doğruluk (%)", displayName);
        timeDataset.setValue(time, "Süre (ms)", displayName);
        chartPanel.repaint();
        timeChartPanel.repaint();
    }

    private void appendHtml(String html) {
        htmlContent.append(html);
        txtResults.setText(htmlContent.toString());
        SwingUtilities.invokeLater(() -> txtResults.setCaretPosition(txtResults.getDocument().getLength()));
    }

    private void appendHtmlLog(String msg) {
        appendHtml("<div class='log'>" + msg + "</div>");
    }

    private void clearHtmlConsole() {
        htmlContent.setLength(0);
        appendHtml(
                "<div class='card'><div class='card-title'>Sistem Hazır</div><div class='card-content'><p style='color:rgba(235,235,245,0.6); margin:0;'>Tüm sistem başlatıldı. Lütfen başlamak için bir Excel veri seti yükleyin.</p></div></div>");
    }

    static class AppleCheckBoxIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            JCheckBox cb = (JCheckBox) c;
            boolean isSelected = cb.isSelected();
            int size = 14;
            int drawY = y + (getIconHeight() - size) / 2 + 1;

            if (isSelected) {
                g2.setColor(ACCENT_BLUE);
                g2.fillRoundRect(x, drawY, size, size, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 3, drawY + 7, x + 6, drawY + 10);
                g2.drawLine(x + 6, drawY + 10, x + 10, drawY + 4);
            } else {
                g2.setColor(BG_CONTROL);
                g2.fillRoundRect(x, drawY, size, size, 6, 6);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(x, drawY, size, size, 6, 6);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    static class RoundedButton extends JButton {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Color bgColor;
        private final Color hoverColor;
        private final Color pressColor;
        private boolean isHovered = false, isPressed = false;
        private final int radius;

        public RoundedButton(String text, Color bgColor, int radius) {
            super(text);
            this.bgColor = bgColor;
            this.hoverColor = bgColor.brighter();
            this.pressColor = bgColor.darker();
            this.radius = radius;
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            setFont(FONT_SYSTEM_BOLD);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isPressed) {
                g2.setColor(pressColor);
            } else if (isHovered) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(bgColor);
            }
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}