//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars K�dderitzsch
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package com.atlassw.tools.eclipse.checkstyle.stats.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;

/**
 * Manages and caches images for the plugin.
 * 
 * @author Lars K�dderitzsch
 */
public class CheckstyleStatsPluginImages
{

    /** Image descriptor for the filter icon. */
    public static final ImageDescriptor FILTER_ICON;

    /** Image cache. */
    private static final Map CACHED_IMAGES = new HashMap();

    static
    {
        FILTER_ICON = CheckstylePlugin.imageDescriptorFromPlugin(
            StatsCheckstylePlugin.PLUGIN_ID, "icons/filter_16.gif"); //$NON-NLS-1$
    }

    /**
     * Gets an image from a given descriptor.
     * 
     * @param descriptor
     *            the descriptor
     * @return the image
     */
    public static Image getImage(ImageDescriptor descriptor)
    {

        Image image = (Image) CACHED_IMAGES.get(descriptor);
        if (image == null)
        {
            image = descriptor.createImage();
            CACHED_IMAGES.put(descriptor, image);
        }
        return image;
    }
}