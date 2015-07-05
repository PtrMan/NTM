/**
* ControlPanel.java
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

package org.fxyz.controls;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ControlPanel extends VBox{

    public ControlPanel() {
        this.accordion = new ControlBasePane();
        VBox.setVgrow(accordion, Priority.ALWAYS);
        this.rootCategory = new ControlCategory("");
        this.accordion.getPanes().add(rootCategory);
        this.accordion.setExpandedPane(rootCategory);
        this.getChildren().add(accordion);
        this.setAlignment(Pos.CENTER);
    }
    
    private final ControlCategory rootCategory;
    private final ControlBasePane accordion;

    public ControlPanel(ControlCategory cat) {
        this();
        this.accordion.getPanes().clear();
        this.accordion.getPanes().add(cat);
        
    }

    public final TitledPane getExpandedPane() {
        return accordion.getExpandedPane();
    }

    public final void setExpandedPane(TitledPane value) {
        accordion.setExpandedPane(value);
    }

    public final ObjectProperty<TitledPane> expandedPaneProperty() {
        return accordion.expandedPaneProperty();
    }

    public final ObservableList<TitledPane> getPanes() {
        return accordion.getPanes();
        
    }
    
    public final void addToRoot(StackPane control){
        this.rootCategory.addControl(control);
    }
    
    public final void addToRoot(StackPane ... control){
        this.rootCategory.addControls(control);
    }
}
