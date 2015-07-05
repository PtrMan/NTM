/**
* Torus.java
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

package org.fxyz.shapes;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import org.fxyz.shapes.containers.ShapeContainer;
import org.fxyz.shapes.primitives.TorusMesh;

/**
 *
 * @author JDub https://github.com/jdub1581
 * refactored by Sean refactored again by jdub
 */
public class Torus extends ShapeContainer<TorusMesh> {

    public TorusMesh mesh;

    public Torus() {
        super(new TorusMesh());
        this.mesh = getShape();        
    }
    
    public Torus(double radius, double tRadius){
        this();
        mesh.setRadius(radius);
        mesh.setTubeRadius(tRadius);
    }
    
    public Torus(int rDivs, int tDivs, double radius, double tRadius) {
        this();
        mesh.setRadiusDivisions(rDivs);
        mesh.setTubeDivisions(tDivs);
        mesh.setRadius(radius);
        mesh.setTubeRadius(tRadius);
    }

    public Torus(Color c) {
        this();
        this.setDiffuseColor(c);
    }
    
    public Torus(double radius, double tRadius, Color c){
        this(radius, tRadius);
        this.setDiffuseColor(c);
    }
    
    public Torus(int rDivs, int tDivs, double radius, double tRadius, Color c) {
        this(rDivs, tDivs,radius, tRadius);
        this.setDiffuseColor(c);
    }

    public final int getRadiusDivisions() {
        return mesh.getRadiusDivisions();
    }

    public final void setRadiusDivisions(int value) {
        mesh.setRadiusDivisions(value);
    }

    public IntegerProperty radiusDivisionsProperty() {
        return mesh.radiusDivisionsProperty();
    }

    public final int getTubeDivisions() {
        return mesh.getTubeDivisions();
    }

    public final void setTubeDivisions(int value) {
        mesh.setTubeDivisions(value);
    }

    public IntegerProperty tubeDivisionsProperty() {
        return mesh.tubeDivisionsProperty();
    }

    public final double getRadius() {
        return mesh.getRadius();
    }

    public final void setRadius(double value) {
        mesh.setRadius(value);
    }

    public DoubleProperty radiusProperty() {
        return mesh.radiusProperty();
    }

    public final double getTubeRadius() {
        return mesh.getTubeRadius();
    }

    public final void setTubeRadius(double value) {
        mesh.setTubeRadius(value);
    }

    public DoubleProperty tubeRadiusProperty() {
        return mesh.tubeRadiusProperty();
    }

    public final double getTubeStartAngleOffset() {
        return mesh.getTubeStartAngleOffset();
    }

    public void setTubeStartAngleOffset(double value) {
        mesh.setTubeStartAngleOffset(value);
    }

    public DoubleProperty tubeStartAngleOffsetProperty() {
        return mesh.tubeStartAngleOffsetProperty();
    }

    public final double getxOffset() {
        return mesh.getxOffset();
    }

    public void setxOffset(double value) {
        mesh.setxOffset(value);
    }

    public DoubleProperty xOffsetProperty() {
        return mesh.xOffsetProperty();
    }

    public final double getyOffset() {
        return mesh.getyOffset();
    }

    public void setyOffset(double value) {
        mesh.setyOffset(value);
    }

    public DoubleProperty yOffsetProperty() {
        return mesh.yOffsetProperty();
    }

    public final double getzOffset() {
        return mesh.getzOffset();
    }

    public void setzOffset(double value) {
        mesh.setzOffset(value);
    }

    public DoubleProperty zOffsetProperty() {
        return mesh.zOffsetProperty();
    }

    public final void setMaterial(Material value) {
        mesh.setMaterial(value);
    }

    public final void setDrawMode(DrawMode value) {
        mesh.setDrawMode(value);
    }

    public final void setCullFace(CullFace value) {
        mesh.setCullFace(value);
    }
    
    
}
