package school.management.system.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import school.management.system.App;
import school.management.system.data.DB;

/**
 * A panel that displays a visual dashboard with charts summarizing key school
 * statistics, such as student distribution and fee collection.
 */
public class DashboardPanel extends JPanel {

    /** Database instance for fetching statistics */
    private final DB db;
    /** Container panel for charts */
    private final JPanel chartsContainer;

    /**
     * Constructs the DashboardPanel.
     *
     * @param db An instance of the DB class to fetch statistics.
     */
    public DashboardPanel(DB db) {
        this.db = db;
        setLayout(new BorderLayout());

        chartsContainer = new JPanel(new GridLayout(2, 2, 10, 10));
        chartsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(chartsContainer, BorderLayout.CENTER);

        refreshCharts();
    }

    /**
     * Refreshes all charts on the dashboard by fetching the latest data from the
     * database in a background thread.
     */
    public void refreshCharts() {
        new SwingWorker<Void, Void>() {
            private JFreeChart barChart;
            private JFreeChart pieChart;
            private JFreeChart lineChart;

            @Override
            protected Void doInBackground() throws Exception {
                // This is done in the background to avoid freezing the UI
                barChart = createStudentByClassChart();
                pieChart = createGenderDistributionChart();
                lineChart = createFeesByMonthChart();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    chartsContainer.removeAll();
                    chartsContainer.add(new ChartPanel(barChart));
                    chartsContainer.add(new ChartPanel(pieChart));
                    chartsContainer.add(new ChartPanel(lineChart));
                    chartsContainer.revalidate();
                    chartsContainer.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    chartsContainer.removeAll();
                    chartsContainer.add(new JLabel("Error loading charts: " + e.getMessage()));
                    chartsContainer.revalidate();
                    chartsContainer.repaint();
                }
            }
        }.execute();
    }

    /** 
     * Creates a bar chart showing the number of students per class.
     * @return a JFreeChart object representing the bar chart
     * @throws Exception if data retrieval fails
     */
    private JFreeChart createStudentByClassChart() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> data = db.getStudentCountPerClassName();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Students", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart("Students per Class", "Class Level", "Number of Students",
                dataset, PlotOrientation.VERTICAL, false, // No legend
                true, false);

        applyChartStyling(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Customize bar color
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        Color barColor = new Color(App.prefs.getInt("chartBarColor", new Color(0, 150, 136).getRGB()));
        renderer.setSeriesPaint(0, barColor);

        // Customize axis fonts
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
        domainAxis.setLabelFont(new Font("Arial", Font.BOLD, 14));

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
        rangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 14));

        return chart;
    }

    /** 
     * Creates a pie chart showing the distribution of students by gender.
     * @return a JFreeChart object representing the pie chart
     * @throws Exception if data retrieval fails
     */
    private JFreeChart createGenderDistributionChart() throws Exception {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<String, Integer> data = db.getStudentGenderDistribution();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String genderKey = entry.getKey();
            if (genderKey == null || genderKey.trim().isEmpty()) {
                genderKey = "Not Specified";
            }
            // Capitalize gender for display, ensuring key is not empty
            String gender = genderKey.substring(0, 1).toUpperCase() + genderKey.substring(1);
            dataset.setValue(gender, entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart("Student Gender Distribution", dataset, true, // Include legend
                true, false);

        applyChartStyling(chart);

        // Customize pie section colors
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        Color maleColor = new Color(App.prefs.getInt("chartMaleColor", new Color(33, 150, 243).getRGB()));
        Color femaleColor = new Color(App.prefs.getInt("chartFemaleColor", new Color(233, 30, 99).getRGB()));
        plot.setSectionPaint("Male", maleColor);
        plot.setSectionPaint("Female", femaleColor);
        plot.setSectionPaint("Not Specified", Color.GRAY);
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");

        return chart;
    }

    /** 
     * Creates a line chart showing the fees collected per month.
     * @return a JFreeChart object representing the line chart
     * @throws Exception if data retrieval fails
     */
    private JFreeChart createFeesByMonthChart() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> data = db.getFeesCollectedPerMonth();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String monthKey = entry.getKey();
            String category = "Unknown Date";
            if (monthKey != null && !monthKey.trim().isEmpty()) {
                category = monthKey;
            }
            dataset.addValue(entry.getValue(), "Fees Collected", category);
        }

        JFreeChart chart = ChartFactory.createLineChart("Fees Collected per Month", "Month", "Amount", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        applyChartStyling(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Customize line color and thickness
        Color lineColor = new Color(App.prefs.getInt("chartLineColor", new Color(255, 152, 0).getRGB()));
        plot.getRenderer().setSeriesPaint(0, lineColor);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));

        return chart;
    }

    /** 
     * Applies consistent styling to the given chart.
     * @param chart the JFreeChart object to style
     */
    private void applyChartStyling(JFreeChart chart) {
        chart.setBackgroundPaint(this.getBackground()); // Match panel background
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));
        chart.getTitle().setPaint(new Color(50, 50, 50));

        chart.getPlot().setBackgroundPaint(Color.WHITE);
        chart.getPlot().setOutlinePaint(null); // No border around the plot area
    }
}