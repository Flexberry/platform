package org.exoplatform.portal.webui.portal;

import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkspace;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  template = "app:/groovy/portal/webui/portal/UIPortalBrowser.gtmpl",
  events = { 
      @EventConfig(listeners = UIPortalBrowser.AddNewPortalActionListener.class),
      @EventConfig(listeners = UIPortalBrowser.DeletePortalActionListener.class, confirm = "UIPortalBrowser.deletePortal")
  }
)
public class UIPortalBrowser extends UIContainer {

  public static String[] BEAN_FIELD = {"creator", "name", "skin", "factoryId"} ;  
  public static String[] SELECT_ACTIONS = {"DeletePortal"} ; 
  
  public UIPortalBrowser() throws Exception {
    setName("UIPortalBrowser");
    UIGrid uiGrid = addChild(UIGrid.class, null, null) ;
    uiGrid.configure("name", BEAN_FIELD, SELECT_ACTIONS) ;
    loadPortalConfigs();
  }

  public void loadPortalConfigs() throws Exception {    
    DataStorage service = getApplicationComponent(DataStorage.class) ;
    List<PortalConfig> configs = service.getAllPortalConfig();
    PageList pagelist = new ObjectPageList(configs, 10);
    UIGrid uiGrid = findFirstComponentOfType(UIGrid.class) ;
    uiGrid.setUseAjax(false);
    uiGrid.getUIPageIterator().setPageList(pagelist);
  } 

  static public class DeletePortalActionListener extends EventListener<UIPortalBrowser> {
    public void execute(Event<UIPortalBrowser> event) throws Exception {
      String portalName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UserPortalConfigService service = event.getSource().getApplicationComponent(UserPortalConfigService.class);
      WebuiRequestContext webuiContext = event.getRequestContext();
      UserPortalConfig config = service.getUserPortalConfig(portalName, webuiContext.getRemoteUser());
      if(config != null && config.getPortalConfig().isModifiable()) {
        service.removeUserPortalConfig(portalName);
      }
      event.getSource().loadPortalConfigs();
      UIPortalApplication uiPortalApp = event.getSource().getAncestorOfType(UIPortalApplication.class);
      UIWorkspace uiWorkingWS = uiPortalApp.findComponentById(UIPortalApplication.UI_WORKING_WS_ID);    
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS) ;
    }
  }
  
  static public class AddNewPortalActionListener extends EventListener<UIPortalBrowser> {
    public void execute(Event<UIPortalBrowser> event) throws Exception {
      PortalRequestContext prContext = Util.getPortalRequestContext();
      UIPortalApplication uiApp = event.getSource().getAncestorOfType(UIPortalApplication.class);      
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID) ;
      UIPortalForm uiNewPortal = uiMaskWS.createUIComponent(UIPortalForm.class, "CreatePortal", "UIPortalForm");
      uiMaskWS.setUIComponent(uiNewPortal);
      uiMaskWS.setShow(true);
      prContext.addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }
}
