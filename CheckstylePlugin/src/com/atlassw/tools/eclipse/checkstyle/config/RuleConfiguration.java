//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.config;

//=================================================
// Imports from java namespace
//=================================================
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Configuration;

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  Represents the configuration of a specific rule.
 */
public class RuleConfiguration implements Cloneable, XMLTags, Configuration
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================
    
    private static final String SEVERITY_PROP = "severity";

	//=================================================
	// Instance member variables.
	//=================================================
    
    private String           mImplClassname;
    
    private String           mRuleName;
    
    private SeverityLevel    mSeverityLevel;
    
    private HashMap          mConfigProperties = new HashMap();
    
	//=================================================
	// Constructors & finalizer.
	//=================================================
     
    /**
     *  Constructor.
     * 
     *  @param  implClassname   Fully qualified class name of the
     *                           rule's implementation class.
     * 
     *  @param  ruleName        The common name for the rule.
     */
    public RuleConfiguration(String implClassname, String ruleName)
    {
        mImplClassname = implClassname;
        mRuleName      = ruleName;
    }
    
    /**
     *  Construct from a config file DOM node.
     */
    public RuleConfiguration(Node node) throws CheckstylePluginException
    {
    	
    	this(node,false);
    	
    }
    
    /**
     *  Construct from a config file DOM node.
     * @param node	the node
     * @param defaultCSFormat	if ture parse in default checkstyle format, if false parse using
     * Schnedier enhanced format
     */
    public RuleConfiguration(Node node, boolean defaultCSFormat) throws CheckstylePluginException
    {
    	if (defaultCSFormat)
    	{
    		String temp = XMLUtil.getNodeAttributeValue(node, NAME_TAG);
        if (temp != null)
        {
            mImplClassname = temp.trim();
        }
        else
        {
            String message = "Rule missing implementation classname";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        
        temp = XMLUtil.getNodeAttributeValue(node, NAME_TAG);
        if (temp != null)
        {
            mRuleName = temp.trim();

        }
        else
        {
            String message = "Rule missing name";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        
        /*
        temp = XMLUtil.getNodeAttributeValue(node, SEVERITY_TAG);
        if (temp != null)
        {
            mSeverityLevel = SeverityLevel.getInstance(temp.trim());
        }
        else
        {
            String message = "Rule missing severity level in metadata";
            CheckstyleLog.warning(message);
            throw new CheckstyleException(message);
        }
        */
        
        
        //Node configItems = XMLUtil.getChildNode(node, PROPERTY_TAG);
        NodeList children = node.getChildNodes();
        boolean serverityLevelFound = false;
        int count = children.getLength();
        for (int i = 0; i < count; i++)
        {
            Node child = children.item(i);
            if (child.getNodeName().equals(PROPERTY_TAG))
            {
                ConfigProperty prop = new ConfigProperty(child);
                mConfigProperties.put(prop.getName(), prop);
                
                if (prop.getName().equals(SEVERITY_TAG))
                {
                	
                	serverityLevelFound = true;
                	String secLev = prop.getValue();
                	mSeverityLevel = SeverityLevel.getInstance(secLev.trim());
                }
            }
        }
        if (!serverityLevelFound)
        {
        	String message = "Rule missing severity level in metadata";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        /////////////////////////////////////////
    	}
    	else
    	{
    	
    	
    	
        String temp = XMLUtil.getNodeAttributeValue(node, CLASSNAME_TAG);
        if (temp != null)
        {
            mImplClassname = temp.trim();
        }
        else
        {
            String message = "Rule missing implementation classname";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        
        temp = XMLUtil.getNodeAttributeValue(node, NAME_TAG);
        if (temp != null)
        {
            mRuleName = temp.trim();
        }
        else
        {
            String message = "Rule missing name";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        
        temp = XMLUtil.getNodeAttributeValue(node, SEVERITY_TAG);
        if (temp != null)
        {
            mSeverityLevel = SeverityLevel.getInstance(temp.trim());
        }
        else
        {
            String message = "Rule missing severity level in metadata";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        
        Node configItems = XMLUtil.getChildNode(node, CONFIG_PROPERTIES_TAG);
        NodeList children = configItems.getChildNodes();
        int count = children.getLength();
        for (int i = 0; i < count; i++)
        {
            Node child = children.item(i);
            if (child.getNodeName().equals(CONFIG_PROPERTY_TAG))
            {
                ConfigProperty prop = new ConfigProperty(child);
                mConfigProperties.put(prop.getName(), prop);
            }
        }
    	}//end else
    }

	//=================================================
	// Methods.
	//=================================================
    
    public ConfigProperty getConfigProperty(String name)
    {
        return (ConfigProperty)mConfigProperties.get(name);
    }
    
    /**
     * Set the property values. <B>Note on security level:</B>  If the properties
     * being set doe not contain a security level config then there could be 
     * problems.  Be sure to call setSecurityLevel after you make this call.
     * This is a necessary work around until security is refactored into a property
     * fully. 
     */
    public void setConfigProperties(HashMap items)
    {
        mConfigProperties = items;
    }
        
    public SeverityLevel getSeverityLevel()
    {
        return mSeverityLevel;
    }
    
    public void setSeverityLevel(SeverityLevel level)
    {
        mSeverityLevel = level;
        
        ConfigProperty prop = (ConfigProperty)mConfigProperties.get(SEVERITY_TAG);
        String value = (level == null) ? null : level.getName();
        if (prop == null)
        {
        	
        	prop = new ConfigProperty(SEVERITY_TAG, value);
        	mConfigProperties.put(SEVERITY_TAG, prop);
        }
        else
        {
        	prop.setValue(value);
        }
        
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
	/**
	 * Returns the implClassname.
	 * @return String
	 */
	public String getImplClassname()
	{
		return mImplClassname;
	}

	/**
	 * Sets the implClassname.
	 * @param implClassname The implClassname to set
	 */
	public void setImplClassname(String implClassname)
	{
		mImplClassname = implClassname;
	}
    
    /**
     *  Create an XML DOM node representation of the rule configuration.
     * 
     *  @param  doc  The document to create the node within.
     */
    public Node toDOMNode(Document doc)
    {
        Element ruleNode = null;
        
        try
        {
            ruleNode = doc.createElement(RULE_CONFIG_TAG);
            ruleNode.setAttribute(CLASSNAME_TAG, mImplClassname);
            ruleNode.setAttribute(NAME_TAG, mRuleName);
            String severity = mSeverityLevel.getName();
            ruleNode.setAttribute(SEVERITY_TAG, severity);
            
            Node cfgPropsNode = doc.createElement(CONFIG_PROPERTIES_TAG);
            ruleNode.appendChild(cfgPropsNode);
            
            Iterator iter = mConfigProperties.values().iterator();
            while (iter.hasNext())
            {
                ConfigProperty prop = (ConfigProperty)iter.next();
                Node propNode = prop.toDOMNode(doc);
                cfgPropsNode.appendChild(propNode);
            }
        }
        catch (DOMException e)
        {
            ruleNode = null;
            CheckstyleLog.warning("Failed to create XML DOM node for rule, ignoring rule");
        }
        
        return ruleNode;
    }
    
    
    public String getAttribute(String name)
        throws com.puppycrawl.tools.checkstyle.api.CheckstyleException
    {
        if (name.equals(SEVERITY_PROP))
        {
            return mSeverityLevel.getName();
        }
        
        ConfigProperty prop = (ConfigProperty)mConfigProperties.get(name);
        if (prop == null)
        {
            String msg = "Invalid attribute name";
            throw new com.puppycrawl.tools.checkstyle.api.CheckstyleException(msg);
        }
        String result = prop.getValue();
        if ((result != null) && (result.length() == 0))
        {
            result = null;
        }
        return result;
    }

    public String[] getAttributeNames()
    {
        LinkedList nonNullProps = new LinkedList();
        Iterator iter = mConfigProperties.values().iterator();
        while (iter.hasNext())
        {
            ConfigProperty prop = (ConfigProperty)iter.next();
            String value = prop.getValue();
            
            if ((value != null) && (value.length() > 0))
            {
                nonNullProps.add(prop.getName());
            }
        }
        
        //need to prevent multiple security property types here
        //I believe this to be necessary are security it not initially treated as a
        //"property"
        if (!nonNullProps.contains(SEVERITY_PROP))
        {
        	nonNullProps.add(SEVERITY_PROP);
        }
        
        String[] result = new String[nonNullProps.size()];
        result = (String[])nonNullProps.toArray(result);
        return result;
    }

    public Configuration[] getChildren()
    {
        Configuration[] result = new Configuration[0];
        return result;
    }

    public String getName()
    {
        return getImplClassname();
    }

    public String getRuleID()
    {
    	//DKN 031903--do we need the siffix part anymore?
        return mImplClassname + " - " + mRuleName;
        //return mImplClassname;
    }
}
