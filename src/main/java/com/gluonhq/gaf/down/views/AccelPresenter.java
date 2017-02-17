package com.gluonhq.gaf.down.views;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.AccelerometerService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.gaf.down.GluonGAfDown;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

public class AccelPresenter extends GluonPresenter<GluonGAfDown> {

    @FXML
    private View accel;

    @FXML
    private Label X;
    
    @FXML
    private Label Y;
    
    @FXML
    private Label Z;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;
    
    @FXML
    private LineChart<Number, Number> chart;
    
    private final int maxSize = 300;
    private XYChart.Series<Number, Number> xSeries;
    private XYChart.Series<Number, Number> ySeries;
    private XYChart.Series<Number, Number> zSeries;
    
    public void initialize() {
        accel.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        getApp().showLayer(GluonGAfDown.MENU_LAYER)));
                appBar.setTitleText(AppViewManager.ACCEL_VIEW.getTitle());
                
                run();
            }
        });
        
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
        xAxis.setTickLabelFormatter(new StringConverter<Number>(){

            @Override
            public String toString(Number t) {
                return formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(t.longValue()), ZoneId.systemDefault()));
            }

            @Override
            public Number fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        });

        xSeries = new XYChart.Series<>();
        ySeries = new XYChart.Series<>();
        zSeries = new XYChart.Series<>();
        xSeries.setName("X-Axis");
        ySeries.setName("Y-Axis");
        zSeries.setName("Z-Axis");
        chart.setTitle("Accelerometer");
        chart.getData().addAll(xSeries, ySeries, zSeries);
    }
    
    private void run() {
        Services.get(AccelerometerService.class)
            .ifPresent(a -> {
                a.accelerationProperty().addListener((obs, ov, nv) -> {
                    if (nv != null) {
                        X.setText(String.format("X: %.4f m/s\u00b2", nv.getX()));
                        Y.setText(String.format("Y: %.4f m/s\u00b2", nv.getY()));
                        Z.setText(String.format("Z: %.4f m/s\u00b2", nv.getZ()));
                    } 
                });
                a.accelerationProperty().addListener((obs, n, n1) -> {
                    long time = n1.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    xSeries.getData().add(new XYChart.Data<>(time, n1.getX()));
                    if (xSeries.getData().size() > maxSize) {
                        xSeries.getData().remove(0);
                    }
                    ySeries.getData().add(new XYChart.Data<>(time, n1.getY()));
                    if (ySeries.getData().size() > maxSize) {
                        ySeries.getData().remove(0);
                    }
                    zSeries.getData().add(new XYChart.Data<>(time, n1.getZ()));
                    if (zSeries.getData().size() > maxSize) {
                        zSeries.getData().remove(0);
                    }
                });
            });
    }
    
    
}
