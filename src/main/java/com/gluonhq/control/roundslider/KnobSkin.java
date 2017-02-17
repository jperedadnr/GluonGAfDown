package com.gluonhq.control.roundslider;

import javafx.animation.RotateTransition;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class KnobSkin extends SkinBase<Knob> {
    private static final double      PREFERRED_WIDTH  = 300;
    private static final double      PREFERRED_HEIGHT = 300;
    private static final double      MINIMUM_WIDTH    = 50;
    private static final double      MINIMUM_HEIGHT   = 50;
    private static final double      MAXIMUM_WIDTH    = 1024;
    private static final double      MAXIMUM_HEIGHT   = 1024; 
    private double                   size;
    
    private double dragOffset;
    private boolean onDrag;
    private double dragStartX,dragStartY;
    
    private Pane pane;
    private Canvas knobTicks;
    private GraphicsContext ticks;
    private Region knobOut;
    private Region knobOutFrame1, knobOutFrame2;
    private Region knobIn;
    private Region knobInFrame1, knobInFrame2;
    private Region knobDot;
    private Rotate dotRotate;
    
    private RotateTransition rot;
        
    public KnobSkin(final Knob knob) {
        super(knob);
        initialize();
        knob.widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        knob.heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        knob.minProperty().addListener(observable -> handleControlPropertyChanged("MIN"));
        knob.valueProperty().addListener(observable -> handleControlPropertyChanged("VALUE"));
        knob.maxProperty().addListener(observable -> handleControlPropertyChanged("MAX"));
        knob.showTickMarksProperty().addListener(observable -> handleControlPropertyChanged("SHOW_TICK_MARKS"));
        knob.showTickLabelsProperty().addListener(observable -> handleControlPropertyChanged("SHOW_TICK_LABELS"));
        knob.majorTickUnitProperty().addListener(observable -> handleControlPropertyChanged("MAJOR_TICK_UNIT"));
        knob.minorTickCountProperty().addListener(observable -> handleControlPropertyChanged("MINOR_TICK_COUNT"));
        knob.labelFormatterProperty().addListener(observable -> handleControlPropertyChanged("LABEL_FORMATTER"));
        knob.tickLabelFillProperty().addListener(observable -> handleControlPropertyChanged("TICKS"));
        knob.tickLabelFontProperty().addListener(observable -> handleControlPropertyChanged("TICKS"));
        knob.tickMarkFillProperty().addListener(observable -> handleControlPropertyChanged("TICKS"));
        
    }
    
    private void initialize() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || 
                Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || 
                Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || 
                Double.compare(getSkinnable().getMinHeight(),0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || 
                Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
        
        pane=new Pane();
        knobOut = new Region();
        knobOut.getStyleClass().setAll("knobOut");
        knobOutFrame1 = new Region();
        knobOutFrame1.getStyleClass().setAll("knobOutFrame1");
        knobOutFrame2 = new Region();
        knobOutFrame2.getStyleClass().setAll("knobOutFrame2");
        knobTicks = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        knobTicks.setMouseTransparent(true);
        ticks = knobTicks.getGraphicsContext2D();
        knobIn = new Region();
        knobIn.getStyleClass().setAll("knobIn");
        knobInFrame1 = new Region();
        knobInFrame1.getStyleClass().setAll("knobInFrame1");
        knobInFrame2 = new Region();
        knobInFrame2.getStyleClass().setAll("knobInFrame2");
        knobDot = new Region();
        knobDot.getStyleClass().setAll("knobDot");
        dotRotate = new Rotate();
        dotRotate.angleProperty().bind(knobIn.rotateProperty());
        knobDot.getTransforms().setAll(dotRotate);
        
        pane.getChildren().setAll(knobOut, knobOutFrame1, knobOutFrame2, 
                knobInFrame1, knobInFrame2,
                knobIn, knobDot, knobTicks);
        getChildren().setAll(pane);
        
//        getSkinnable().setOnMousePressed((MouseEvent me) -> {
//            onDrag=true;
//            double dragStart = mouseToValue(me.getX(), me.getY());
//            double zeroOneValue = (getSkinnable().getValue() - getSkinnable().getMin()) / (getSkinnable().getMax() - getSkinnable().getMin());
//            dragOffset = zeroOneValue - dragStart;
//            knobPressed(me, dragStart);
//        });
//        getSkinnable().setOnMouseReleased((MouseEvent me) -> {
//            onDrag=false;
//            knobRelease(me, mouseToValue(me.getX(), me.getY()));
//        });
//        getSkinnable().setOnMouseDragged((MouseEvent me) -> {
//            knobDragged(me, mouseToValue(me.getX(), me.getY()) + dragOffset);
//        });
//        
//        rotateKnob();
    }
    
    private double mouseToValue(double mouseX, double mouseY) {
        double cx = getSkinnable().getWidth()/2;
        double cy = getSkinnable().getHeight()/2;
        double mouseAngle = Math.toDegrees(Math.atan((mouseY-cy) / (mouseX-cx)));
        double topZeroAngle;
        if (mouseX<cx) {
            topZeroAngle = -90 + mouseAngle;
        } else {
            topZeroAngle = (90 + mouseAngle);
        }
        double value = (topZeroAngle-getSkinnable().getMinAngle()) / 
                (getSkinnable().getMaxAngle() - getSkinnable().getMinAngle());
        return value;
    }
    
    protected void handleControlPropertyChanged(String p) {
        if (null != p) switch (p) {
            case "RESIZE":
                resize();
                break;
            case "MIN":
            case "MAX":
            case "SHOW_TICK_MARKS":
            case "SHOW_TICK_LABELS":
            case "MAJOR_TICK_UNIT":
            case "MINOR_TICK_COUNT":
                drawTickMarks(ticks);
                break;
             case "TICKS":
                drawTickMarks(ticks);
                break;
            case "VALUE":
                rotateKnob();
                break;
            case "LABEL_FORMATTER":
                drawTickMarks(ticks);
                break;
        }
    }
    
    private void drawTickMarks(final GraphicsContext CTX) {        
        CTX.clearRect(0, 0, size, size);
        CTX.setLineCap(StrokeLineCap.ROUND);
        Knob s = getSkinnable();
        if(s.isShowTickMarks() || s.isShowTickLabels()){
            int numMajorTicks=(int)((s.getMax()-s.getMin())/s.getMajorTickUnit())+1;
            int numMinorTicks=(numMajorTicks-1)*s.getMinorTickCount();
            int totalTicks=numMajorTicks+numMinorTicks;
            double minor=s.getMajorTickUnit();
            if (s.getMinorTickCount() != 0) {
                minor = minor / (double) (Math.max(s.getMinorTickCount(), 0) + 1);
            }
            Point2D center = new Point2D(size * 0.5, size * 0.5);
            Font old=getSkinnable().getTickLabelFont();
            for(int i=0;i<totalTicks;i++){
                boolean isMinor=true;
                if(i*minor%s.getMajorTickUnit()==0){
                    isMinor=false;
                }
                Font font = Font.font(old.getFamily(), 
                    old.getName().contains("Bold")?FontWeight.BOLD:FontWeight.NORMAL, 
                    old.getName().contains("Italic")?FontPosture.ITALIC:FontPosture.REGULAR,
                    isMinor? size * 0.04 : size * 0.06);
                double zeroOneValue = (i*minor-s.getMin()) / (s.getMax() - s.getMin());
                double angle = s.getMinAngle() + ((s.getMaxAngle() - s.getMinAngle()) * zeroOneValue);
                if(s.isShowTickMarks()){
                    CTX.setStroke(getSkinnable().getTickMarkFill());  
                    CTX.setLineWidth(size * (isMinor?0.002:0.004));  
                    Point2D innerPoint = new Point2D(center.getX() + size * 0.445 * Math.sin(Math.toRadians(angle)), 
                                                     center.getY() - size * 0.445 * Math.cos(Math.toRadians(angle)));            
                    Point2D outerPoint = new Point2D(center.getX() + size * (isMinor?0.47:0.48) * Math.sin(Math.toRadians(angle)), 
                                                     center.getY() - size * (isMinor?0.47:0.48) * Math.cos(Math.toRadians(angle)));            
                    CTX.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY()); 
                    
                }
                if(s.isShowTickLabels()){
                    Point2D textPoint = new Point2D(center.getX() + size * 0.41 * Math.sin(Math.toRadians(angle)), 
                                                    center.getY() - size * 0.41 * Math.cos(Math.toRadians(angle)));            
                    // Draw text
                    CTX.save();
                    CTX.translate(textPoint.getX(), textPoint.getY());
                    CTX.rotate(angle);
                    CTX.setFont(font);
                    CTX.setTextAlign(TextAlignment.CENTER);
                    CTX.setTextBaseline(VPos.CENTER);
                    CTX.setFill(getSkinnable().getTickLabelFill());
                    
                    if (getSkinnable().getLabelFormatter() != null) {
                        CTX.fillText(getSkinnable().getLabelFormatter().toString((i*minor-s.getMin())), 0, 0);
                    } else {
                        CTX.fillText(String.format("%d", (int) (i*minor-s.getMin())), 0, 0);
                    }
                    CTX.restore();

                }
            }
        }
                
    }
    
    private void rotateKnob() {
        Knob s = getSkinnable();
        double zeroOneValue = (s.getValue()-s.getMin()) / (s.getMax() - s.getMin());
        double angle = getSkinnable().getMinAngle() + ((getSkinnable().getMaxAngle() - getSkinnable().getMinAngle()) * zeroOneValue);
        if(onDrag){
            knobIn.setRotate(angle);
        } else {
            if(rot!=null){
                rot.stop();
            }
            rot=new RotateTransition(Duration.millis(500),knobIn);
            rot.setToAngle(angle);
            rot.play();
        }
    }
    
    private void resize() {
        size = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();
        
        if (size > 0) {
            pane.setMaxSize(size, size);
            
            knobOut.setPrefSize(0.998*size, 0.998*size);
            knobOut.relocate(0.001*size,0.001*size);
            knobOutFrame1.setPrefSize(0.996*size, 0.996*size);
            knobOutFrame1.relocate(0.002*size,0.002*size);
            knobOutFrame2.setPrefSize(0.96*size, 0.96*size);
            knobOutFrame2.relocate(0.02*size,0.02*size);
            
            knobInFrame1.setPrefSize(0.89*size, 0.89*size);
            knobInFrame1.relocate(0.055*size,0.055*size);
            knobInFrame2.setPrefSize(0.88*size, 0.88*size);
            knobInFrame2.relocate(0.06*size,0.06*size);
            
            knobIn.setPrefSize(0.86*size, 0.86*size);
            knobIn.relocate(0.07*size,0.07*size);            
//            knobDot.setPrefSize(0.05*size,0.05*size);
//            knobDot.relocate(0.475*size,0.24*size);
//            dotRotate.setPivotX(size * 0.025);
//            dotRotate.setPivotY(size * 0.26);
            knobDot.setPrefSize(0.02*size,0.16*size);
            knobDot.relocate(0.49*size,0.22*size);
            dotRotate.setPivotX(size * 0.01);
            dotRotate.setPivotY(size * 0.24);
            
            knobTicks.relocate(0,0);
            knobTicks.setWidth(size);
            knobTicks.setHeight(size);
            ticks.clearRect(0, 0, size, size);        
            drawTickMarks(ticks);
            knobTicks.setCache(true);
            knobTicks.setCacheHint(CacheHint.QUALITY);
        }
    }
    
    /**
     * @param e
     * @param position The position of mouse in 0=min to 1=max range
     */
    private void knobRelease(MouseEvent e, double position) {
        final Knob slider = getSkinnable();
        slider.setValueChanging(false);
        // detect click rather than drag
        if(Math.abs(e.getX()-dragStartX) < 3 && Math.abs(e.getY()-dragStartY) < 3) {
            slider.adjustValue((position+slider.getMin()) * (slider.getMax()-slider.getMin()));
        }
    }

     /**
     * @param e
     * @param position The position of mouse in 0=min to 1=max range
     */
    private void knobPressed(MouseEvent e, double position) {
        // If not already focused, request focus
        final Knob slider = getSkinnable();
        if (!slider.isFocused()){
            slider.requestFocus();
        }
        slider.setValueChanging(true);
        dragStartX = e.getX();
        dragStartY = e.getY();
    }

    /**
     * @param e
     * @param position The position of mouse in 0=min to 1=max range
     */
    private void knobDragged(MouseEvent e, double position) {
        final Knob slider = getSkinnable();
        slider.adjustValue((position+slider.getMin()) * (slider.getMax()-slider.getMin()));
    }
    
}
