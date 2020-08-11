package com.gluonhq.control.roundslider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

public class Knob extends Control {
    
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/
    
    /**
     * Creates a new Knob instance using default values of 0.0, 0.5
     * and 1.0 for min/value/max, respectively. 
     */
    public Knob() {
        this(0, 100, 0);
    }
    
    /**
     * Instantiates a default, horizontal Knob with the specified 
     * min/max/low/high values.
     * 
     * @param min The minimum allowable value that the Knob will allow.
     * @param max The maximum allowable value that the Knob will allow.
     * @param value The initial value for the the Knob.
     */
    public Knob(double min, double max, double value) {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        
        setMax(max);
        setMin(min);
        setValue(value);
        adjustValues();
        
    }
    
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("knob.css").toExternalForm();
    }
    
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new KnobSkin(this);
    }
    
    /***************************************************************************
     *                                                                         *
     * New properties (over and above what is in Slider)                       *
     *                                                                         *
     **************************************************************************/
    
    // --- value
    /**
     * The value property represents the current position of the value
     * thumb, and is within the allowable range as specified by the
     * {@link #minProperty() min} and {@link #maxProperty() max} properties. By
     * default this value is 0.
     * @return 
     */
    public final DoubleProperty valueProperty() {
        return value;
    }
    private final DoubleProperty value = new SimpleDoubleProperty(this, "value", 0.0D) {
        @Override protected void invalidated() {
            adjustValues();
        }
    };
    
    /**
     * Sets the value for the range slider, which may or may not be clamped
     * to be within the allowable range as specified by the
     * {@link #minProperty() min} and {@link #maxProperty() max} properties.
     * @param d
     */
    public final void setValue(double d) {
        valueProperty().set(d);
    }

    /**
     * Returns the current low value for the range slider.
     * @return 
     */
    public final double getValue() {
        return value != null ? value.get() : 0.0D;
    }
    
    // --- value changing
    /**
     * When true, indicates the current low value of this Knob is changing.
     * It provides notification that the low value is changing. Once the  
     * value is computed, it is set back to false.
     * @return 
     */
    public final BooleanProperty valueChangingProperty() {
        if (valueChanging == null) {
            valueChanging = new SimpleBooleanProperty(this, "valueChanging", false);
        }
        return valueChanging;
    }
    
    private BooleanProperty valueChanging;

    /**
     * Call this when the value is changing.
     * @param value True if the value is changing, false otherwise.
     */
    public final void setValueChanging(boolean value) {
        valueChangingProperty().set(value);
    }

    /**
     * Returns whether or not the value of this RangeSlider is currently
     * changing.
     * @return 
     */
    public final boolean isValueChanging() {
        return valueChanging == null ? false : valueChanging.get();
    }

    /***************************************************************************
     *                                                                         *
     * New public API                                                          *
     *                                                                         *
     **************************************************************************/
    
    /**
     * Increments the {@link #highProperty() value} by the 
     * {@link #blockIncrementProperty() block increment} amount.
     */
    public void incrementValue() {
        adjustValue(getValue() + getBlockIncrement());
    }

    /**
     * Decrements the {@link #valueProperty() value} by the 
     * {@link #blockIncrementProperty() block increment} amount.
     */
    public void decrementValue() {
        adjustValue(getValue() - getBlockIncrement());
    }
    
    /**
     * Adjusts {@link #valueProperty() value} to match <code>newValue</code>,
     * or as closely as possible within the constraints imposed by the 
     * {@link #minProperty() min} and {@link #maxProperty() max} properties. 
     * This function also takes into account 
     * {@link #snapToTicksProperty() snapToTicks}, which is the main difference 
     * between <code>adjustLowValue</code> and 
     * {@link #setLowValue(double) setLowValue}.
     * @param newValue
     */
    public void adjustValue(double newValue) {
        double d1 = getMin();
        double d2 = getMax();
        if (d2 <= d1) {
            // no-op
        } else {
            newValue = newValue >= d1 ? newValue : d1;
            newValue = newValue <= d2 ? newValue : d2;
            setValue(snapValueToTicks(newValue));
        }
    }

    
    /***************************************************************************
     *                                                                         *
     * Properties copied from Slider (and slightly edited)                     *
     *                                                                         *
     **************************************************************************/
    
    
    /**
     * The maximum value represented by this Slider. This must be a
     * value greater than {@link #minProperty() min}.
     */
    private DoubleProperty max;
    public final void setMax(double value) {
        maxProperty().set(value);
    }

    public final double getMax() {
        return max == null ? 100 : max.get();
    }

    public final DoubleProperty maxProperty() {
        if (max == null) {
            max = new DoublePropertyBase(100) {
                @Override protected void invalidated() {
//                    if (get() < getMin()) {
//                        setMin(get());
//                    }
                    adjustValues();
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "max";
                }
            };
        }
        return max;
    }
    /**
     * The minimum value represented by this Slider. This must be a
     * value less than {@link #maxProperty() max}.
     */
    private DoubleProperty min;
    public final void setMin(double value) {
        minProperty().set(value);
    }

    public final double getMin() {
        return min == null ? 0 : min.get();
    }

    public final DoubleProperty minProperty() {
        if (min == null) {
            min = new DoublePropertyBase(0) {
                @Override protected void invalidated() {
//                    if (get() > getMax()) {
//                        setMax(get());
//                    }
                    adjustValues();
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "min";
                }
            };
        }
        return min;
    }
    
    /**
     * The maximum angle represented by this Slider. This must be a
     * value greater than {@link #minAngleProperty() min}.
     */
    private DoubleProperty maxAngle;
    public final void setMaxAngle(double value) {
        maxAngleProperty().set(value);
    }

    public final double getMaxAngle() {
        return maxAngle == null ? 140 : maxAngle.get();
    }

    public final DoubleProperty maxAngleProperty() {
        if (maxAngle == null) {
            maxAngle = new DoublePropertyBase(140) {
                @Override protected void invalidated() {
                    if (get() < getMinAngle()) {
                        setMinAngle(get());
                    }
                    adjustValues();
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "maxAngle";
                }
            };
        }
        return maxAngle;
    }
    /**
     * The minimum angle represented by this Slider. This must be a
     * value less than {@link #maxProperty() max}.
     */
    private DoubleProperty minAngle;
    public final void setMinAngle(double value) {
        minAngleProperty().set(value);
    }

    public final double getMinAngle() {
        return minAngle == null ? -140 : minAngle.get();
    }

    public final DoubleProperty minAngleProperty() {
        if (minAngle == null) {
            minAngle = new DoublePropertyBase(-140) {
                @Override protected void invalidated() {
                    if (get() > getMaxAngle()) {
                        setMaxAngle(get());
                    }
                    adjustValues();
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "minAngle";
                }
            };
        }
        return minAngle;
    }
    
    /**
     * Indicates whether the {@link #valueProperty()} value}/{@link #highValueProperty()} value} of the {@code Slider} should always
     * be aligned with the tick marks. This is honored even if the tick marks
     * are not shown.
     */
    private BooleanProperty snapToTicks;
    public final void setSnapToTicks(boolean value) {
        snapToTicksProperty().set(value);
    }

    public final boolean isSnapToTicks() {
        return snapToTicks == null ? false : snapToTicks.get();
    }

    public final BooleanProperty snapToTicksProperty() {
        if (snapToTicks == null) {
            snapToTicks = new StyleableBooleanProperty(false) {
                @Override public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return Knob.StyleableProperties.SNAP_TO_TICKS;
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "snapToTicks";
                }
            };
        }
        return snapToTicks;
    }
    /**
     * The unit distance between major tick marks. For example, if
     * the {@link #minProperty() min} is 0 and the {@link #maxProperty() max} is 100 and the
     * {@link #majorTickUnitProperty() majorTickUnit} is 25, then there would be 5 tick marks: one at
     * position 0, one at position 25, one at position 50, one at position
     * 75, and a final one at position 100.
     * <p>
     * This value should be positive and should be a value less than the
     * span. Out of range values are essentially the same as disabling
     * tick marks.
     */
    private DoubleProperty majorTickUnit;
    public final void setMajorTickUnit(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("MajorTickUnit cannot be less than or equal to 0.");
        }
        majorTickUnitProperty().set(value);
    }

    public final double getMajorTickUnit() {
        return majorTickUnit == null ? 25 : majorTickUnit.get();
    }

    public final DoubleProperty majorTickUnitProperty() {
        if (majorTickUnit == null) {
            majorTickUnit = new StyleableDoubleProperty(25) {
                @Override public void invalidated() {
                    if (get() <= 0) {
                        throw new IllegalArgumentException("MajorTickUnit cannot be less than or equal to 0.");
                    }
                }
                
                @Override public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.MAJOR_TICK_UNIT;
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "majorTickUnit";
                }
            };
        }
        return majorTickUnit;
    }
    /**
     * The number of minor ticks to place between any two major ticks. This
     * number should be positive or zero. Out of range values will disable
     * disable minor ticks, as will a value of zero.
     */
    private IntegerProperty minorTickCount;
    public final void setMinorTickCount(int value) {
        minorTickCountProperty().set(value);
    }

    public final int getMinorTickCount() {
        return minorTickCount == null ? 3 : minorTickCount.get();
    }

    public final IntegerProperty minorTickCountProperty() {
        if (minorTickCount == null) {
            minorTickCount = new StyleableIntegerProperty(3) {
                @Override public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return Knob.StyleableProperties.MINOR_TICK_COUNT;
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "minorTickCount";
                }
            };
        }
        return minorTickCount;
    }
    /**
     * The amount by which to adjust the slider if the track of the slider is
     * clicked. This is used when manipulating the slider position using keys. If
     * {@link #snapToTicksProperty() snapToTicks} is true then the nearest tick mark to the adjusted
     * value will be used.
     */
    private DoubleProperty blockIncrement;
    public final void setBlockIncrement(double value) {
        blockIncrementProperty().set(value);
    }

    public final double getBlockIncrement() {
        return blockIncrement == null ? 10 : blockIncrement.get();
    }

    public final DoubleProperty blockIncrementProperty() {
        if (blockIncrement == null) {
            blockIncrement = new StyleableDoubleProperty(10) {
                @Override public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return Knob.StyleableProperties.BLOCK_INCREMENT;
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "blockIncrement";
                }
            };
        }
        return blockIncrement;
    }
    
    /**
     * Indicates that the labels for tick marks should be shown. Typically a
     * {@link Skin} implementation will only show labels if
     * {@link #showTickMarksProperty() showTickMarks} is also true.
     */
    private BooleanProperty showTickLabels;
    public final void setShowTickLabels(boolean value) {
        showTickLabelsProperty().set(value);
    }

    public final boolean isShowTickLabels() {
        return showTickLabels == null ? false : showTickLabels.get();
    }

    public final BooleanProperty showTickLabelsProperty() {
        if (showTickLabels == null) {
            showTickLabels = new StyleableBooleanProperty(false) {
                @Override public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return Knob.StyleableProperties.SHOW_TICK_LABELS;
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "showTickLabels";
                }
            };
        }
        return showTickLabels;
    }
    /**
     * Specifies whether the {@link Skin} implementation should show tick marks.
     */
    private BooleanProperty showTickMarks;
    public final void setShowTickMarks(boolean value) {
        showTickMarksProperty().set(value);
    }

    public final boolean isShowTickMarks() {
        return showTickMarks == null ? false : showTickMarks.get();
    }

    public final BooleanProperty showTickMarksProperty() {
        if (showTickMarks == null) {
            showTickMarks = new StyleableBooleanProperty(false) {
                @Override public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return Knob.StyleableProperties.SHOW_TICK_MARKS;
                }

                @Override public Object getBean() {
                    return Knob.this;
                }

                @Override public String getName() {
                    return "showTickMarks";
                }
            };
        }
        return showTickMarks;
    }
    
    /**
     * A function for formatting the label for a major tick. The number
     * representing the major tick will be passed to the function. If this
     * function is not specified, then a default function will be used by
     * the {@link Skin} implementation.
     */
    private ObjectProperty<StringConverter<Double>> labelFormatter;
    
    public final void setLabelFormatter(StringConverter<Double> value) {
        labelFormatterProperty().set(value);
    }

    public final StringConverter<Double> getLabelFormatter() {
        return labelFormatter == null ? null : labelFormatter.get();
    }

    public final ObjectProperty<StringConverter<Double>> labelFormatterProperty() {
        if (labelFormatter == null) {
            labelFormatter = new SimpleObjectProperty<>(this, "labelFormatter");
        }
        return labelFormatter;
    }
    
    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/    
     private void adjustValues() {
         if (getValue() < getMin() || getValue() > getMax()) {
            setValue(clamp(getMin(), getValue(), getMax()));
        }
     }
     
     private double snapValueToTicks(double d) {
        double d1 = d;
        if (isSnapToTicks()) {
            double d2;
            if (getMinorTickCount() != 0) {
                d2 = getMajorTickUnit() / (double) (Math.max(getMinorTickCount(), 0) + 1);
            } else {
                d2 = getMajorTickUnit();
            }
            int i = (int) ((d1 - getMin()) / d2);
            double d3 = (double) i * d2 + getMin();
            double d4 = (double) (i + 1) * d2 + getMin();
            d1 = nearest(d3, d1, d4);
        }
        return clamp(getMin(), d1, getMax());
    }
     
    private static float clamp(float input, float min, float max) {
        return (input < min) ? min : (input > max) ? max : input;
    }
    
    private static double clamp(double input, double min, double max) {
        return (input < min) ? min : (input > max) ? max : input;
    }
    
    private static double nearest(double less, double value, double more) {
        double lessDiff = value - less;
        double moreDiff = more - value;
        if (lessDiff < moreDiff) return less;
        return more;
    }
    
     /**************************************************************************
    *                                                                         *
    * Stylesheet Handling                                                     *
    *                                                                         *
    **************************************************************************/
    private static final String DEFAULT_STYLE_CLASS = "knob";
    
    private ObjectProperty<Paint> tickMarkFill;
    private ObjectProperty<Paint> tickLabelFill;
    private ObjectProperty<Font> tickLabelFont;
    
    public final Paint getTickMarkFill() {
        return null == tickMarkFill ? Color.BLACK : tickMarkFill.get();
    }
    public final void setTickMarkFill(Paint value) {
        tickMarkFillProperty().set(value);
    }
    public final ObjectProperty<Paint> tickMarkFillProperty() {
        if (null == tickMarkFill) {
            tickMarkFill = new StyleableObjectProperty<Paint>(Color.BLACK) {

                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_MARK_FILL; }

                @Override public Object getBean() { return Knob.this; }

                @Override public String getName() { return "tickMarkFill"; }
            };
        }
        return tickMarkFill;
    }

    public final Paint getTickLabelFill() {
        return null == tickLabelFill ? Color.BLACK : tickLabelFill.get();
    }
    public final void setTickLabelFill(Paint value) {
        tickLabelFillProperty().set(value);
    }
    public final ObjectProperty<Paint> tickLabelFillProperty() {
        if (null == tickLabelFill) {
            tickLabelFill = new StyleableObjectProperty<Paint>(Color.BLACK) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_LABEL_FILL; }
                @Override public Object getBean() { return Knob.this; }
                @Override public String getName() { return "tickLabelFill"; }
            };
        }
        return tickLabelFill;
    }
    
    public final Font getTickLabelFont() {
        return null == tickLabelFont ? Font.getDefault() : tickLabelFont.get();
    }
    public final void setTickLabelFont(Font value) {
        tickLabelFontProperty().set(value);
    }
    public final ObjectProperty<Font> tickLabelFontProperty() {
        if (null == tickLabelFont) {
            tickLabelFont = new StyleableObjectProperty<Font>(Font.getDefault()) {
                private boolean fontSetByCss = false;
                @Override public void applyStyle(StyleOrigin newOrigin, Font value) {
                    try {
                        // super.applyStyle calls set which might throw if value is bound.
                        // Have to make sure fontSetByCss is reset.
                        fontSetByCss = true;
                        super.applyStyle(newOrigin, value);
                    } catch(Exception e) {
                        throw e;
                    } finally {
                        fontSetByCss = false;
                    }
                }
                @Override public void set(Font value) {
                    final Font oldValue = get();
                    if (value != null ? !value.equals(oldValue) : oldValue != null) {
                        super.set(value);
                    }
                }
                @Override protected void invalidated() {
                    if(fontSetByCss == false) {
                        Knob.this.applyCss();
                    }
                }
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_LABEL_FONT; }
                @Override public Object getBean() { return Knob.this; }
                @Override public String getName() { return "tickLabelFont"; }
            };
        }
        return tickLabelFont;
    }
    
    private static class StyleableProperties {
        private static final CssMetaData<Knob,Number> BLOCK_INCREMENT =
            new CssMetaData<Knob,Number>("-fx-block-increment",
                SizeConverter.getInstance(), 10.0) {

            @Override public boolean isSettable(Knob n) {
                return n.blockIncrement == null || !n.blockIncrement.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override public StyleableProperty<Number> getStyleableProperty(Knob n) {
                return (StyleableProperty<Number>)n.blockIncrementProperty();
            }
        };
        
        private static final CssMetaData<Knob,Boolean> SHOW_TICK_LABELS =
            new CssMetaData<Knob,Boolean>("-fx-show-tick-labels",
                BooleanConverter.getInstance(), Boolean.FALSE) {

            @Override public boolean isSettable(Knob n) {
                return n.showTickLabels == null || !n.showTickLabels.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override public StyleableProperty<Boolean> getStyleableProperty(Knob n) {
                return (StyleableProperty<Boolean>)n.showTickLabelsProperty();
            }
        };
                    
        private static final CssMetaData<Knob,Boolean> SHOW_TICK_MARKS =
            new CssMetaData<Knob,Boolean>("-fx-show-tick-marks",
                BooleanConverter.getInstance(), Boolean.FALSE) {

            @Override public boolean isSettable(Knob n) {
                return n.showTickMarks == null || !n.showTickMarks.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override public StyleableProperty<Boolean> getStyleableProperty(Knob n) {
                return (StyleableProperty<Boolean>)n.showTickMarksProperty();
            }
        };
            
        private static final CssMetaData<Knob,Boolean> SNAP_TO_TICKS =
            new CssMetaData<Knob,Boolean>("-fx-snap-to-ticks",
                BooleanConverter.getInstance(), Boolean.FALSE) {

            @Override public boolean isSettable(Knob n) {
                return n.snapToTicks == null || !n.snapToTicks.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override public StyleableProperty<Boolean> getStyleableProperty(Knob n) {
                return (StyleableProperty<Boolean>)n.snapToTicksProperty();
            }
        };
        
        private static final CssMetaData<Knob,Number> MAJOR_TICK_UNIT =
            new CssMetaData<Knob,Number>("-fx-major-tick-unit",
                SizeConverter.getInstance(), 25.0) {

            @Override public boolean isSettable(Knob n) {
                return n.majorTickUnit == null || !n.majorTickUnit.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override public StyleableProperty<Number> getStyleableProperty(Knob n) {
                return (StyleableProperty<Number>)n.majorTickUnitProperty();
            }
        };
        
        private static final CssMetaData<Knob,Number> MINOR_TICK_COUNT =
            new CssMetaData<Knob,Number>("-fx-minor-tick-count",
                SizeConverter.getInstance(), 3.0) {

            @SuppressWarnings("deprecation")
            @Override public void set(Knob node, Number value, StyleOrigin origin) {
                super.set(node, value.intValue(), origin);
            } 
            
            @Override public boolean isSettable(Knob n) {
                return n.minorTickCount == null || !n.minorTickCount.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override public StyleableProperty<Number> getStyleableProperty(Knob n) {
                return (StyleableProperty<Number>)n.minorTickCountProperty();
            }
        };
        private static final CssMetaData<Knob, Paint> TICK_MARK_FILL =
            new CssMetaData<Knob, Paint>("-tick-mark-fill", PaintConverter.getInstance(), Color.BLACK) {

                @Override public boolean isSettable(Knob knob) {
                    return null == knob.tickMarkFill || !knob.tickMarkFill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Knob knob) {
                    return (StyleableProperty) knob.tickMarkFillProperty();
                }
            };

        private static final CssMetaData<Knob, Paint> TICK_LABEL_FILL =
            new CssMetaData<Knob, Paint>("-tick-label-fill", PaintConverter.getInstance(), Color.BLACK) {

                @Override public boolean isSettable(Knob knob) {
                    return null == knob.tickLabelFill || !knob.tickLabelFill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Knob knob) {
                    return (StyleableProperty) knob.tickLabelFillProperty();
                }
            };
        
        private static final FontCssMetaData<Knob> TICK_LABEL_FONT =
            new FontCssMetaData<Knob>("-tick-label-font", Font.getDefault()) {

                @Override public boolean isSettable(Knob knob) {
                    return null == knob.tickLabelFont || !knob.tickLabelFont.isBound();
                }

                @Override public StyleableProperty<Font> getStyleableProperty(Knob knob) {
                    return (StyleableProperty) knob.tickLabelFontProperty();
                }
            };
        
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = 
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(BLOCK_INCREMENT);
            styleables.add(SHOW_TICK_LABELS);
            styleables.add(SHOW_TICK_MARKS);
            styleables.add(SNAP_TO_TICKS);
            styleables.add(MAJOR_TICK_UNIT);
            styleables.add(MINOR_TICK_COUNT);
            styleables.add(TICK_MARK_FILL);
            styleables.add(TICK_LABEL_FILL);
            styleables.add(TICK_LABEL_FONT);

            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
    
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
}
