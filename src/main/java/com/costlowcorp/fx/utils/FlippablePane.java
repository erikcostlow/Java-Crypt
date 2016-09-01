/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.fx.utils;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 *
 * @author ecostlow
 */
public class FlippablePane extends Pane{
    
    private final Region mainContents;
    
    private final BooleanProperty flipped = new SimpleBooleanProperty(false);
    
    public FlippablePane(Region mainContents){
        this.mainContents=mainContents;
        getChildren().setAll(mainContents);
        UIUtils.setAnchors(mainContents, 0, 0, 0, 0);
    }

    public BooleanProperty getFlipped() {
        return flipped;
    }
    
    public void flipBack(){
        flipTo(null);
    }
    
    public void flipTo(Region node){
        final Node firstChild=getChildren().get(0);
        if(node==firstChild || (node==null && mainContents.equals(node))){//No need to flip
            return;
        }
        final Region newNode = node==null ? mainContents : makeAnchor(node);
        if(newNode!=mainContents){
            newNode.prefHeightProperty().bind(mainContents.heightProperty());
            newNode.prefWidthProperty().bind(mainContents.widthProperty());
        }
        RotateTransition start = new RotateTransition(Duration.millis(750), this);
        start.setAxis(Rotate.Y_AXIS);
        start.setFromAngle(0);
        start.setToAngle(90);
        start.setInterpolator(Interpolator.LINEAR);
        start.setOnFinished(event -> getChildren().setAll(newNode));

        RotateTransition end = new RotateTransition(Duration.millis(750), this);
        end.setAxis(Rotate.Y_AXIS);
        end.setFromAngle(270);
        end.setToAngle(360);
        end.setInterpolator(Interpolator.LINEAR);

        final SequentialTransition sq = new SequentialTransition(start, end);
        sq.play();
        flipped.set(!flipped.get());
    }
    
    private AnchorPane makeAnchor(Region node){
        final Hyperlink x = new Hyperlink("X");
        x.setOnAction(event -> flipBack());
        final AnchorPane retval = new AnchorPane(node, x);
        UIUtils.setAnchors(node, 0, 0, 0, 0);
        AnchorPane.setTopAnchor(x, 2.0);
        AnchorPane.setRightAnchor(x, 2.0);
        return retval;
    }
}
