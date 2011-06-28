package org.brekka.stillingar.spring;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class ConfigBaseResolverFactoryBeanTest {

	private ConfigBaseResolverFactoryBean resolverFactoryBean;
	
	private File baseDir;
	
	@Before
	public void setUp() throws Exception {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		this.baseDir = new File(tmpDir, "ConfigBaseResolverFactoryBeanTest-" + System.currentTimeMillis());
		File configDir = new File(baseDir, "conf");
		configDir.mkdirs();
		configDir.deleteOnExit();
		baseDir.deleteOnExit();
		resolverFactoryBean = new ConfigBaseResolverFactoryBean();
	}

	@Test
	public void testResolveSysProp() throws Exception {
		System.setProperty("catalina.base", baseDir.getAbsolutePath());
		Resource resolve = resolverFactoryBean.resolve("file:${catalina.base}/conf");
		assertEquals("file:" + baseDir.getAbsolutePath() + "/conf", resolve.getURL().toString());
	}

	@Test
	public void testResolveEnvProp() throws Exception {
		
		Map<String, String> env = new HashMap<String, String>();
		env.put("DOMAIN_HOME", baseDir.getAbsolutePath());
		resolverFactoryBean.setEnvMap(env);
		Resource resolve = resolverFactoryBean.resolve("file:${env.DOMAIN_HOME}/conf");
		assertEquals("file:" + baseDir.getAbsolutePath() + "/conf", resolve.getURL().toString());
	}
}
