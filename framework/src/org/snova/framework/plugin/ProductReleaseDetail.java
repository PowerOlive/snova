/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ProductReleaseDetail.java 
 *
 * @author qiying.wang [ May 17, 2010 | 2:13:47 PM ]
 *
 */
package org.snova.framework.plugin;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name = "information")
public class ProductReleaseDetail
{
	public static class PluginReleaseDetail
	{
		@Override
        public String toString()
        {
	        return "PluginReleaseDetail [name=" + name + ", version=" + version
	                + ", url=" + url + ", desc=" + desc + "]";
        }
		@XmlAttribute
		public String name;
		@XmlAttribute
		public String version;
		@XmlElement
		public String url;
		@XmlElement
		public String desc;
	}
	@XmlElementWrapper(name = "plugins")
	@XmlElements(@XmlElement(name = "plugin", type = PluginReleaseDetail.class))
	public List<PluginReleaseDetail> plugins;
}
