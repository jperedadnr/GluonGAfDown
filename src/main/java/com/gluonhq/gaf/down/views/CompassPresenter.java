package com.gluonhq.gaf.down.views;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.CompassService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.control.roundslider.Knob;
import com.gluonhq.gaf.down.GluonGAfDown;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

public class CompassPresenter extends GluonPresenter<GluonGAfDown> {

    @FXML
    private View compassView;

    @FXML
    private Label label;
    
    @FXML
    private Knob knob;
    
    public void initialize() {
        compassView.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        getApp().getDrawer().open()));
                appBar.setTitleText(AppViewManager.COMPASS_VIEW.getTitle());
                
                run();
            }
        });
        
        compassView.setOnHidden(e -> {
            knob.rotateProperty().unbind();
            label.textProperty().unbind();
        });
        
        knob.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double val) {
                if (val == 0 || val == 360) {
                    return "N";
                } else if (val == 90) { 
                    return "E";
                } else if (val == 180) { 
                    return "S";
                } else if (val == 270) {
                    return "W";
                }
                return String.format("%.1f", val);
            }

            @Override
            public Double fromString(String string) {
                return 0d;
            }
        });
        
        label.setText("0\u00b0");
    }
    
    private void run() {
        Services.get(CompassService.class).ifPresent(c -> {
            knob.rotateProperty().bind(c.headingProperty().multiply(-1).add(360));
            label.textProperty().bind(Bindings.format("%.1f\u00b0", c.headingProperty()));
        });
    }
    
}
