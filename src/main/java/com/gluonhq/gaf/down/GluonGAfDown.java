package com.gluonhq.gaf.down;

import com.gluonhq.gaf.down.views.AppViewManager;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.visual.Swatch;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class GluonGAfDown extends MobileApplication {

    public static final String MENU_LAYER = "Side Menu";
    
    @Override
    public void init() {
        AppViewManager.registerViews(this);
        
        addLayerFactory(MENU_LAYER, () -> new SidePopupView(new DrawerManager().getDrawer()));
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.BLUE.assignTo(scene);

        scene.getStylesheets().add(GluonGAfDown.class.getResource("style.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(GluonGAfDown.class.getResourceAsStream("/icon.png")));
    }
}
