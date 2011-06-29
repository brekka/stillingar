package org.brekka.stillingar.spring.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class StillingarNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("configuration", new ConfigurationBeanDefinitionParser());
	}

}
