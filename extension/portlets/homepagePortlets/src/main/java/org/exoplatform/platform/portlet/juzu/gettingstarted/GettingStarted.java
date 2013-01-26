/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.platform.portlet.juzu.gettingstarted;

import juzu.Path;
import juzu.Resource;
import juzu.View;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.exoplatform.platform.portlet.juzu.gettingstarted.models.GettingStartedService;
import org.exoplatform.platform.portlet.juzu.gettingstarted.models.GettingStartedUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.web.application.RequestContext;
import org.gatein.common.text.EntityEncoder;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author <a href="fbradai@exoplatform.com">Fbradai</a>
 * @date 07/12/12
 */

public class GettingStarted {

    private static Log logger = ExoLogger.getLogger(GettingStarted.class);
    HashMap parameters = new HashMap();
    HashMap<String, String> status = new HashMap();

    @Inject
    NodeHierarchyCreator nodeHierarchyCreator_;

    @Inject
    @Path("gettingStarted.gtmpl")
    Template gettingStarted;

    @Inject
    @Path("gettingStartedList.gtmpl")
    Template gettingStartedList;

    @View
    public void index() throws Exception {
        String remoteUser = RequestContext.getCurrentInstance().getRemoteUser();
        SessionProvider sProvider = SessionProvider.createSystemProvider();
        Node userPrivateNode = nodeHierarchyCreator_.getUserNode(sProvider, remoteUser).getNode(GettingStartedUtils.JCR_APPLICATION_NODE);
        if (!userPrivateNode.hasNode(GettingStartedUtils.JCR_GS_NODE)) {

            Node gettingStartedNode = userPrivateNode.addNode(GettingStartedUtils.JCR_GS_NODE);
            userPrivateNode.save();
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_DELETE_GADGET_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_PROFILE_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_CONNECT_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_SPACE_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_ACTIVITY_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_DOCUMENT_PROPERTY_NAME, false);
            gettingStartedNode.save();
        }
        gettingStarted.render();
    }

    @Ajax
    @Resource
    public void delete() throws Exception
    {
        //set Delete
        String userId = RequestContext.getCurrentInstance().getRemoteUser();
        SessionProvider sProvider = SessionProvider.createSystemProvider();
        Node userPrivateNode = nodeHierarchyCreator_.getUserNode(sProvider, userId).getNode(GettingStartedUtils.JCR_APPLICATION_NODE);
        if (userPrivateNode.hasNode(GettingStartedUtils.JCR_GS_NODE))
        {
            Node gettingStartedNode = userPrivateNode.getNode(GettingStartedUtils.JCR_GS_NODE);
            if (gettingStartedNode.hasProperty(GettingStartedUtils.JCR_DELETE_GADGET_PROPERTY_NAME))
            {
                gettingStartedNode.setProperty(GettingStartedUtils.JCR_DELETE_GADGET_PROPERTY_NAME, true);
                gettingStartedNode.save();
            }
        }
        gettingStarted.render();
    }

    @Ajax
    @Resource
    public void getGsList() throws Exception {
        HashMap bundle = new HashMap();
        Locale locale = null;
        Boolean Isshow = true;
        PropertyIterator propertiesIt = null;
        int progress = 0;
        String remoteUser = RequestContext.getCurrentInstance().getRemoteUser();
        SessionProvider sProvider = SessionProvider.createSystemProvider();
        Node userPrivateNode = nodeHierarchyCreator_.getUserNode(sProvider, remoteUser).getNode(GettingStartedUtils.JCR_APPLICATION_NODE);
        if (userPrivateNode.hasNode(GettingStartedUtils.JCR_GS_NODE))
        {
            Node gettingStartedNode = userPrivateNode.getNode(GettingStartedUtils.JCR_GS_NODE);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_PROFILE_PROPERTY_NAME, GettingStartedService.hasAvatar(remoteUser));
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_CONNECT_PROPERTY_NAME, GettingStartedService.hasContacts(remoteUser));
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_SPACE_PROPERTY_NAME, GettingStartedService.hasSpaces(remoteUser));
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_ACTIVITY_PROPERTY_NAME, GettingStartedService.hasActivities(remoteUser));
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_DOCUMENT_PROPERTY_NAME, GettingStartedService.hasDocuments(null, remoteUser));
            propertiesIt = userPrivateNode.getNode(GettingStartedUtils.JCR_GS_NODE).getProperties("exo:gs_*");
            while (propertiesIt.hasNext())
            {
                String clazz = "";
                Property prop = (Property) propertiesIt.next();
                if (prop.getString().equals(GettingStartedUtils.TRUE))
                {
                    progress += 20;
                    clazz = GettingStartedUtils.DONE;
                }
                status.put(prop.getName().substring(4), clazz);
            }
            if (progress == 100) Isshow = false;
        } else
        {
            Node gettingStartedNode = userPrivateNode.addNode(GettingStartedUtils.JCR_GS_NODE);
            userPrivateNode.save();
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_DELETE_GADGET_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_PROFILE_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_CONNECT_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_SPACE_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_ACTIVITY_PROPERTY_NAME, false);
            gettingStartedNode.setProperty(GettingStartedUtils.JCR_DOCUMENT_PROPERTY_NAME, false);
            gettingStartedNode.save();
        }

        try {
            locale = RequestContext.getCurrentInstance().getLocale();
            ResourceBundle rs = ResourceBundle.getBundle("gettingStarted/gettingStarted", locale);
            bundle.put("profile", LinkProvider.getUserProfileUri(remoteUser));
            bundle.put("profileLabel", EntityEncoder.FULL.encode(rs.getString("Upload.label")));
            bundle.put("connect", LinkProvider.getUserConnectionsUri(remoteUser));
            bundle.put("connectLabel", EntityEncoder.FULL.encode(rs.getString("Connect.Label")));
            bundle.put("space", GettingStartedUtils.SPACE_URL);
            bundle.put("spaceLabel", EntityEncoder.FULL.encode(rs.getString("Space.Label")));
            bundle.put("activity", "#");
            bundle.put("activityLabel", EntityEncoder.FULL.encode(rs.getString("Activity.Label")));
            bundle.put("upload", GettingStartedUtils.UPLOAD_URL);
            bundle.put("uploadLabel", EntityEncoder.FULL.encode(rs.getString("Document.Label")));
            bundle.put("titleLabel", EntityEncoder.FULL.encode(rs.getString("title.Label")));
        } catch (MissingResourceException ex) {
            logger.warn("##Missing Labels of GettingStarted Portlet");
        }
        parameters.putAll(bundle);
        parameters.put(GettingStartedUtils.PROGRESS, new Integer(progress));
        parameters.put(GettingStartedUtils.WIDTH, new Integer((Math.round((160 * progress) / 100))).toString());
        parameters.put(GettingStartedUtils.STATUS, status);
        parameters.put(GettingStartedUtils.SHOW, Isshow.toString());
        gettingStartedList.render(parameters);
    }
}


