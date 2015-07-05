/**
* Histogram.java
*
* Copyright (c) 2013-2015, F(X)yz
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* * Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimer.
* * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the
* documentation and/or other materials provided with the distribution.
* * Neither the name of the organization nor the
* names of its contributors may be used to endorse or promote products
* derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.fxyz.shapes.composites;

import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Sean
 */
public class Histogram extends Group {

    private ArrayList<Double> xAxisData = new ArrayList<>();
    private ArrayList<Double> yAxisData = new ArrayList<>();
    private ArrayList<Double> zAxisData = new ArrayList<>();
    public Group histogramDataGroup = new Group();
    public AmbientLight selfLight = new AmbientLight(Color.WHITE);
    public double nodeRadius = 1;
    private double axesSize = 1000;
    private boolean normalized = false;
    public boolean selfLightEnabled = false;
    private ArrayList<Node> bars;

    public enum NodeType {

        CYLINDER, CUBE
    };
    private NodeType defaultNodeType = NodeType.CUBE;

    public Histogram(boolean selfLit) {
        selfLightEnabled = selfLit;
        init();
    }

    public Histogram(double axesSize, double nodeRadius, boolean selfLit) {
        selfLightEnabled = selfLit;
        this.axesSize = axesSize;
        this.nodeRadius = nodeRadius;
        init();
    }

    private void init() {
        getChildren().add(histogramDataGroup);
        if (selfLightEnabled) {
            getChildren().add(selfLight);
        }
        setDepthTest(DepthTest.ENABLE);
    }

    public synchronized void setHeightData(float[][] arrayY, int barSize, int spacing,
            Color color, boolean ambient, boolean fill) {

        histogramDataGroup.getChildren().clear();

        PhongMaterial phong = new PhongMaterial();
        phong.setSpecularColor(color);
        phong.setDiffuseColor(color);

        float min = arrayY[0][0], max = arrayY[0][0];
        // Find min and max's for color gradient
        for (int x = 0; x < arrayY.length; x++) {

            final float[] aa = arrayY[x];

            for (int z = 0; z < aa.length; z++) {


                final float bb = aa[z];
                if (bb < min) {
                    min = bb;
                }
                if (bb > max) {
                    max = bb;
                }
            }
        }
        float range = max - min; //

        // Generate Bars
        final int maxBars = arrayY[0].length * arrayY.length;
        if (bars == null)
            bars = new ArrayList(maxBars);
        else
            bars.clear();

        for (int x = 0; x < arrayY.length; x++) {

            final float[] aa = arrayY[x];

            for (int z = 0; z < aa.length; z++) {

                double ySize = aa[z];
                if (ySize < 0) {
                    ySize *= -1;
                }

                Node bar = createDefaultNode(barSize, ySize);

                bar.setCacheHint(CacheHint.SPEED);
                bar.setCache(true);

                bar.setTranslateX(x * spacing);
                bar.setTranslateY(-aa[z]);
                bar.setTranslateZ(z * spacing);
                //Apply normalized color scale
                double normalizedColor = (aa[z] - min) / range;
                if (aa[z] < range / 2) //if it is in the lower half of the range
                {
                    phong = new PhongMaterial(new Color(0.25, 0, normalizedColor, 1));
                } else //if it is in the upper half of the range
                {
                    phong = new PhongMaterial(new Color(normalizedColor, 0, 0.25, 1));
                }
                switch (getDefaultNodeType()) {
                    case CYLINDER: {
                        ((Cylinder) bar).setMaterial(phong);
                        ((Cylinder) bar).setCullFace(CullFace.BACK);
                        if (fill) {
                            ((Cylinder) bar).setDrawMode(DrawMode.FILL);
                        } else {
                            ((Cylinder) bar).setDrawMode(DrawMode.LINE); //show lines only by default
                        }
                        break;
                    }
                    case CUBE:
                    default: {

                        ((Box) bar).setMaterial(phong);
                        ((Box) bar).setCullFace(CullFace.BACK);
                        if (fill) {
                            ((Box) bar).setDrawMode(DrawMode.FILL);
                        } else {
                            ((Box) bar).setDrawMode(DrawMode.LINE); //show lines only by default
                        }
                    }
                }

                if (ambient) {
                    AmbientLight light = new AmbientLight(color);
                    light.getScope().add(bar);
                    histogramDataGroup.getChildren().add(light);
                }

                bars.add(bar);
            }
        }

        histogramDataGroup.getChildren().addAll(bars);

    }

    private Node createDefaultNode(double barWidth, double barHeight) {
        switch (defaultNodeType) {
            case CYLINDER:
                return new Cylinder(barWidth / 2, barHeight);
            case CUBE:
                return new Box(barWidth, barHeight, barWidth);
            default:
                return new Box(barWidth, barHeight, barWidth);
        }
    }

    /**
     * @return the defaultNodeType
     */
    public NodeType getDefaultNodeType() {
        return defaultNodeType;
    }

    /**
     * @param defaultNodeType the defaultNodeType to set
     */
    public void setDefaultNodeType(NodeType defaultNodeType) {
        this.defaultNodeType = defaultNodeType;
    }
}
