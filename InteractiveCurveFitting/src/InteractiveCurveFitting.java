import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ui.RectangleEdge;

public class InteractiveCurveFitting {
    // 存储用户点击的坐标点
    private static final List<Double> xData = new ArrayList<>();
    private static final List<Double> yData = new ArrayList<>();
    private static final XYSeries rawDataSeries = new XYSeries("原始数据点");
    private static final XYSeries fittedCurveSeries = new XYSeries("拟合曲线");
    private static JFreeChart chart;
    private static final SplineInterpolator interpolator = new SplineInterpolator();

    public static void main(String[] args) {
        // 1. 创建数据集
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(rawDataSeries);
        dataset.addSeries(fittedCurveSeries);

        // 2. 创建散点图
        chart = ChartFactory.createScatterPlot(
                "交互式曲线拟合",  // 图表标题
                "X",            // X轴标签
                "Y",            // Y轴标签
                dataset         // 数据集
        );

        // 3. 创建图表面板并添加鼠标监听
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 将鼠标点击坐标转换为图表数据坐标
                double x = chart.getXYPlot().getDomainAxis().java2DToValue(
                        e.getX(), chartPanel.getScreenDataArea(), org.jfree.chart.ui.RectangleEdge.BOTTOM  // 正确参数：坐标轴边缘类型
                );
                double y = chart.getXYPlot().getRangeAxis().java2DToValue(
                        e.getY(), chartPanel.getScreenDataArea(),  org.jfree.chart.ui.RectangleEdge.LEFT    // 正确参数：坐标轴边缘类型
                );

                // 添加新点并更新曲线
                addDataPoint(x, y);
                updateFittedCurve();
            }
        });

        // 4. 创建重置按钮
        JButton resetButton = new JButton("重置数据");
        resetButton.addActionListener(e -> resetData());

        // 5. 设置主窗口
        JFrame frame = new JFrame("交互式曲线拟合程序");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(chartPanel);
        frame.add(resetButton);
        frame.pack(); // 自动调整窗口大小
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // 添加数据点
    private static void addDataPoint(double x, double y) {
        xData.add(x);
        yData.add(y);
        rawDataSeries.add(x, y);
    }

    // 更新拟合曲线
    private static void updateFittedCurve() {
        if (xData.size() < 4) {
            return; // 至少需要4个点才能进行样条插值
        }

        try {
            // 参数化处理：将x和y视为参数t的函数
            int n = xData.size();
            double[] t = new double[n];
            for (int i = 0; i < n; i++) {
                t[i] = i; // t从0开始递增
            }

            // 转换为数组
            double[] xArr = new double[n];
            double[] yArr = new double[n];
            for (int i = 0; i < n; i++) {
                xArr[i] = xData.get(i);
                yArr[i] = yData.get(i);
            }

            // 创建样条函数
            PolynomialSplineFunction xSpline = interpolator.interpolate(t, xArr);
            PolynomialSplineFunction ySpline = interpolator.interpolate(t, yArr);

            // 生成拟合曲线点
            fittedCurveSeries.clear();
            int numPoints = 1000;
            for (int i = 0; i < numPoints; i++) {
                double ti = t[0] + (t[n - 1] - t[0]) * i / (numPoints - 1.0);
                double xFit = xSpline.value(ti);
                double yFit = ySpline.value(ti);
                fittedCurveSeries.add(xFit, yFit);
            }
        } catch (Exception e) {
            System.out.println("插值失败：" + e.getMessage());
        }
    }

    // 重置数据
    private static void resetData() {
        xData.clear();
        yData.clear();
        rawDataSeries.clear();
        fittedCurveSeries.clear();
    }
}