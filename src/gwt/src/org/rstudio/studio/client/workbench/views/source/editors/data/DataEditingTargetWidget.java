/*
 * DataEditingTargetWidget.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.source.editors.data;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.*;

import org.rstudio.core.client.Debug;
import org.rstudio.core.client.StringUtil;
import org.rstudio.core.client.dom.IFrameElementEx;
import org.rstudio.core.client.dom.WindowEx;
import org.rstudio.core.client.widget.FindTextBox;
import org.rstudio.core.client.widget.RStudioFrame;
import org.rstudio.core.client.widget.Toolbar;
import org.rstudio.core.client.widget.ToolbarButton;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.views.source.PanelWithToolbars;
import org.rstudio.studio.client.workbench.views.source.editors.EditingTargetToolbar;
import org.rstudio.studio.client.workbench.views.source.editors.text.findreplace.FindReplaceBar;
import org.rstudio.studio.client.workbench.views.source.editors.urlcontent.UrlContentEditingTarget;
import org.rstudio.studio.client.workbench.views.source.model.DataItem;

public class DataEditingTargetWidget extends Composite
   implements UrlContentEditingTarget.Display
{
   interface Resources extends ClientBundle
   {
      @Source("DataEditingTargetWidget.css")
      Styles styles();
   }
   private static Resources resources = GWT.create(Resources.class);

   public interface Styles extends CssResource
   {
      String description();

      String statusBar();
      String statusBarDisplayed();
      String statusBarOmitted();
   }

   static
   {
      resources.styles().ensureInjected();
   }

   public DataEditingTargetWidget(Commands commands, DataItem dataItem)
   {
      Styles styles = resources.styles();

      commands_ = commands;

      frame_ = new RStudioFrame(dataItem.getContentUrl());
      frame_.setSize("100%", "100%");

      Widget mainWidget = frame_;

      if (dataItem.getDisplayedObservations() != dataItem.getTotalObservations())
      {
         FlowPanel statusBar = new FlowPanel();
         statusBar.setStylePrimaryName(styles.statusBar());
         statusBar.setSize("100%", "100%");
         Label label1 = new Label(
               "Displayed "
               + StringUtil.formatGeneralNumber(dataItem.getDisplayedObservations())
               + " rows of "
               + StringUtil.formatGeneralNumber(dataItem.getTotalObservations()));
         int omitted = dataItem.getTotalObservations()
                       - dataItem.getDisplayedObservations();
         Label label2 = new Label("(" +
                                  StringUtil.formatGeneralNumber(omitted) +
                                  " omitted)");

         label1.addStyleName(styles.statusBarDisplayed());
         label2.addStyleName(styles.statusBarOmitted());

         statusBar.add(label1);
         statusBar.add(label2);

         DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
         dockPanel.addSouth(statusBar, 20);
         dockPanel.add(frame_);
         dockPanel.setSize("100%", "100%");
         mainWidget = dockPanel;
      }

      PanelWithToolbars panel = new PanelWithToolbars(createToolbar(dataItem,
                                                                  styles),
                                                    mainWidget);

      initWidget(panel);
   }

   private Toolbar createToolbar(DataItem dataItem, Styles styles)
   {

      Toolbar toolbar = new EditingTargetToolbar(commands_);
      toolbar.addLeftWidget(commands_.popoutDoc().createToolbarButton());
      toolbar.addLeftSeparator();
      findButton_ = new ToolbarButton(
              FindReplaceBar.getFindIcon(),
              new ClickHandler() {
                 public void onClick(ClickEvent event)
                 {
                    filtered_ = !filtered_;
                    setFilterUIVisible(filtered_);
                    findButton_.setLeftImage(filtered_ ? 
                          FindReplaceBar.getFindLatchedIcon() :
                          FindReplaceBar.getFindIcon());
                 }
              });
      toolbar.addLeftWidget(findButton_);
      find_ = new FindTextBox("Find in data");
      find_.getElement().getStyle().setMarginBottom(2, Unit.PX);
      find_.setIconVisible(true);
      toolbar.addRightWidget(find_);
      
      find_.addValueChangeHandler(new ValueChangeHandler<String>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<String> arg0)
         {
            applySearch(getWindow(), arg0.getValue());
         }
      });
      
      return toolbar;
   }
   
   private WindowEx getWindow()
   {
      IFrameElementEx frameEl = (IFrameElementEx) frame_.getElement().cast();
      return frameEl.getContentWindow();
   }

   public void print()
   {
      getWindow().print();
   }
   
   public void setFilterUIVisible(boolean visible)
   {
      setFilterUIVisible(getWindow(), visible);
   }
   
   public void refreshData(boolean structureChanged)
   {
      refreshData(getWindow(), structureChanged);
   }

   private static final native void setFilterUIVisible (WindowEx frame, boolean visible) /*-{
      frame.setFilterUIVisible(visible);
   }-*/;
   
   private static final native void refreshData(WindowEx frame, boolean structureChanged) /*-{
      frame.refreshData(structureChanged);
   }-*/;

   private static final native void applySearch(WindowEx frame, String text) /*-{
      frame.applySearch(text);
   }-*/;

   public Widget asWidget()
   {
      return this;
   }

   private final Commands commands_;
   private RStudioFrame frame_;
   private ToolbarButton findButton_;
   private boolean filtered_ = false;
   private FindTextBox find_;
}
