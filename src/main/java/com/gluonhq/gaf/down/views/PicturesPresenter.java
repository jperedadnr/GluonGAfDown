package com.gluonhq.gaf.down.views;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.PicturesService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.BottomNavigation;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.gaf.down.GluonGAfDown;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;

public class PicturesPresenter extends GluonPresenter<GluonGAfDown> {

    @FXML
    private View pictures;

    @FXML
    private ImageView imageView;
    
    @FXML
    private BottomNavigation bottomNavigation;
    
    public void initialize() {
        pictures.setShowTransitionFactory(BounceInRightTransition::new);
        
        pictures.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        getApp().showLayer(GluonGAfDown.MENU_LAYER)));
                appBar.setTitleText(AppViewManager.PICTURES_VIEW.getTitle());
            }
        });
        pictures.setOnShown( e-> {
            imageView.fitWidthProperty().bind(pictures.widthProperty().subtract(10));
                imageView.fitHeightProperty().bind(pictures.getScene().heightProperty()
                        .subtract(getApp().getAppBar().getHeight() + bottomNavigation.prefHeight(-1) + 10));
        });
        
        pictures.setOnHidden(e -> {
            imageView.fitWidthProperty().unbind();
            imageView.fitHeightProperty().unbind();
        });
        
        final ToggleButton takePicButton = bottomNavigation.createButton("Take Picture", MaterialDesignIcon.PHOTO_CAMERA.graphic(), null);
        takePicButton.setOnMousePressed(e -> Services.get(PicturesService.class).ifPresent(p -> p.takePhoto(true).ifPresent(imageView::setImage)));
        
        final ToggleButton retrievePicButton = bottomNavigation.createButton("Retrieve Picture", MaterialDesignIcon.PHOTO_ALBUM.graphic(), null);
        retrievePicButton.setOnMousePressed(e -> Services.get(PicturesService.class).ifPresent(p -> p.loadImageFromGallery().ifPresent(imageView::setImage)));

        bottomNavigation.getActionItems().addAll(takePicButton, retrievePicButton);

    }
}
