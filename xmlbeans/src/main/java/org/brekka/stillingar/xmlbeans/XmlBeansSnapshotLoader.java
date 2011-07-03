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

package org.brekka.stillingar.xmlbeans;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.snapshot.Snapshot;
import org.brekka.stillingar.core.snapshot.SnapshotLoader;

/**
 * Loader of Apache XmlBean based snapshots.
 * 
 * @author Andrew Taylor
 */
public class XmlBeansSnapshotLoader implements SnapshotLoader {

	
	private Map<String, String> xpathNamespaces;
	
	private boolean validate = true;

	
	public Snapshot load(URL fromUrl, long timestamp) {
		Snapshot snapshot;
		try {
			XmlObject xmlBean = XmlObject.Factory.parse(fromUrl);
			
			if (this.validate) {
				validate(xmlBean);
			}
			
			snapshot = new XmlBeansSnapshot(fromUrl, timestamp, xmlBean, this.xpathNamespaces);
		} catch (IOException e) {
			throw new ConfigurationException(format(
					"Failed to read"), e);
		} catch (XmlException e) {
			throw new ConfigurationException(format(
					"Illegal XML"), e);
		}
		return snapshot;
	}
	
	protected void validate(XmlObject bean) {
		List<XmlError> errors = new ArrayList<XmlError>();
		XmlOptions validateOptions = new XmlOptions().setErrorListener(errors);
		
		if (!bean.validate(validateOptions)) {
			throw new ConfigurationException(format(
					"Configuration XML does not validate. " +
					"Errors: %s", errors));
		}
	}

	
	public void setXpathNamespaces(Map<String, String> xpathNamespaces) {
		this.xpathNamespaces = xpathNamespaces;
	}
	
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
}
