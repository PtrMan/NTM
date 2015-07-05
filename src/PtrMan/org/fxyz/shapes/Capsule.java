/**
* Capsule.java
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import org.fxyz.shapes.containers.ShapeContainer;
import org.fxyz.shapes.primitives.CapsuleMesh;

/**
 *
 * @author Dub
 */
public class Capsule extends ShapeContainer<CapsuleMesh>{
    private CapsuleMesh mesh;
    
    public Capsule() {
        super(new CapsuleMesh());
        this.mesh = getShape();
    }
    
    public Capsule(double radius, double height){
        this();
        mesh.setRadius(radius);
        mesh.setHeight(height);        
    }
    
    public Capsule(Color c){
        this();
        this.setDiffuseColor(c);
    }
    
    public Capsule(double radius, double height, Color c){
        this(radius, height);
        this.setDiffuseColor(c);
    }

    public final void setRadius(double value) {
        mesh.setRadius(value);
    }

    public final void setHeight(double value) {
        mesh.setHeight(value);
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

    public final double getRadius() {
        return mesh.getRadius();
    }


    public DoubleProperty radiusProperty() {
        return mesh.radiusProperty();
    }

    public final double getHeight() {
        return mesh.getHeight();
    }


    public DoubleProperty heightProperty() {
        return mesh.heightProperty();
    }
    
}
