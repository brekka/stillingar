/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.stillingar.spring;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Formatter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.UpdatableConfigurationSource;
import org.brekka.stillingar.core.UpdateReport;

/**
 * TODO
 * 
 * @author Andrew Taylor
 */
public class LoggingBackgroundUpdater implements Runnable {

	private static final Log log = LogFactory.getLog(LoggingBackgroundUpdater.class);
	
	private final UpdatableConfigurationSource configurationSource;

	public LoggingBackgroundUpdater(
			UpdatableConfigurationSource configurationSource) {
		this.configurationSource = configurationSource;
	}
	
	@Override
	public void run() {
		UpdateReport update = this.configurationSource.update();
		if (update != null) {
			List<GroupConfigurationException> errors = update.getErrors();
			URL location = update.getLocation();
			if (!errors.isEmpty()) {
				StringWriter writer = new StringWriter();
				PrintWriter out = new PrintWriter(writer);
				Formatter fmt = new Formatter(out);
				
				fmt.format("Errors encountered during configuration update from '%s'. " +
						"%d group errors follow.%n", location, errors.size());
				for (GroupConfigurationException groupConfigurationException : errors) {
					fmt.format(" Group '%s' errors (total %d):%n", groupConfigurationException.getGroupName());
					List<ConfigurationException> errorList = groupConfigurationException.getErrorList();
					int cnt = 1;
					for (ConfigurationException configurationException : errorList) {
						fmt.format("  Error %d of %d:%n", cnt, errorList.size());
						configurationException.printStackTrace(out);
						cnt++;
					}
				}
				// log the error
				log.error(writer.toString());
			} else {
				if (log.isInfoEnabled()) {
					log.info(String.format("Configuration has been updated successfully from '%s'.", location));
				}
			}
		} else {
			if (log.isTraceEnabled()) {
				log.trace("No change to configuration");
			}
		}
	}
}
