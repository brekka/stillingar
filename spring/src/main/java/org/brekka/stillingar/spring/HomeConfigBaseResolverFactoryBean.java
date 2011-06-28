package org.brekka.stillingar.spring;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashSet;

public class HomeConfigBaseResolverFactoryBean extends
		ConfigBaseResolverFactoryBean {
	
	private static final String USER_HOME_PATH = "file:${user.home}/%s/";

	public HomeConfigBaseResolverFactoryBean(String baseWithinHome) {
		super(new HashSet<String>(Arrays.asList(format(USER_HOME_PATH, baseWithinHome))));
	}
}
