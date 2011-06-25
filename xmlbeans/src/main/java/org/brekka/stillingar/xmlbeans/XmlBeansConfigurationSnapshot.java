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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlAnyURI;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlFloat;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlObject;
import org.brekka.stillingar.core.ConfigurationSnapshot;
import org.brekka.stillingar.core.ValueConfigurationException;

public class XmlBeansConfigurationSnapshot implements ConfigurationSnapshot {

	private final long timestamp;
	
	private final URL location;
	
	private final XmlObject bean;
	
	private final Map<String, String> xpathNamespaces;
	
	

	public XmlBeansConfigurationSnapshot(URL location, long timestamp, XmlObject bean,
			Map<String, String> xpathNamespaces) {
		this.location = location;
		this.timestamp = timestamp;
		this.bean = bean;
		this.xpathNamespaces = xpathNamespaces;
	}
	
	public URL getLocation() {
		return location;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	/* (non-Javadoc)
	 * @see org.brekka.configuration.xmlbeans.Instance#retrieve(java.lang.Class)
	 */
	public <T> T retrieve(Class<T> valueType) {
		T result = null;
		XmlObject[] found = find(valueType, true);
		if (found.length == 1) {
			result = (T) convert(valueType, found[0], null);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.brekka.configuration.xmlbeans.Instance#retrieve(java.lang.Class, java.lang.String)
	 */
	public <T> T retrieve(Class<T> valueType, String expression) {
		T value;
		XmlObject[] found = evaluate(expression);
		if (found.length == 1) {
			XmlObject xml = found[0];
			value = convert(valueType, xml, expression);
		} else if (found.length == 0) {
			// No value found, return null
			value = null;
		} else {
			throw new ValueConfigurationException(
					"multiple values found, only one expected",
					valueType.getClass(), expression);
		}
		return value;
	}
	
	/* (non-Javadoc)
	 * @see org.brekka.configuration.xmlbeans.Instance#retrieveList(java.lang.Class)
	 */
	public <T> List<T> retrieveList(Class<T> valueType) {
		List<T> results = new ArrayList<T>();
		XmlObject[] found = find(valueType, true);
		for (XmlObject xmlObject : found) {
			T value = (T) convert(valueType, xmlObject, null);
			results.add(value);
		}
		return results;
	}
	
	/* (non-Javadoc)
	 * @see org.brekka.configuration.xmlbeans.Instance#retrieveList(java.lang.Class, java.lang.String)
	 */
	public <T> List<T> retrieveList(Class<T> valueType, String expression) {
		List<T> results = new ArrayList<T>();
		XmlObject[] found = evaluate(expression);
		for (XmlObject xmlObject : found) {
			T value = (T) convert(valueType, xmlObject, expression);
			results.add(value);
		}
		return results;
	}
	
	private XmlObject[] find(Class<?> type, boolean singleExpected) {
		List<XmlObject> results = new ArrayList<XmlObject>();
		XmlCursor cursor = bean.newCursor();
		TokenType token = cursor.toNextToken();
		while (token != TokenType.ENDDOC) {
			if (token == TokenType.START) {
				XmlObject object = cursor.getObject();
				if (type.isAssignableFrom(object.getClass())) {
					results.add(object);
					if (results.size() > 1 && singleExpected) {
						throw new ValueConfigurationException(
								"multiple values found, only one expected",
								type.getClass(), null);
					}
				}
			}
			token = cursor.toNextToken();
		}
		cursor.dispose();
		return results.toArray(new XmlObject[results.size()]);
	}
	
	private XmlObject[] evaluate(String expression) {
		StringBuilder sb = new StringBuilder();
		Set<Entry<String,String>> entrySet = xpathNamespaces.entrySet();
		for (Entry<String, String> entry : entrySet) {
			sb.append("declare namespace ");
			sb.append(entry.getKey());
			sb.append("='");
			sb.append(entry.getValue());
			sb.append("';");
		}
		sb.append('.');
		sb.append(expression);
		return bean.selectPath(sb.toString());
	}
	
	protected <T> T convert(Class<T> expectedType, XmlObject object, String expression) {
		T value = null;
		boolean nullValue = false;
		if (object == null) {
			// Leave as null
			nullValue = true;
		} else if (expectedType.isAssignableFrom(object.getClass())) {
			value = (T) object;
		} else if (expectedType == String.class) {
			if (object instanceof XmlAnySimpleType) {
				value = (T) ((XmlAnySimpleType) object).getStringValue();
			} else {
				value = (T) object.xmlText();
			}
		} else if (expectedType == Long.class || expectedType == Long.TYPE) {
			if (object instanceof XmlLong) {
				value = (T) Long.valueOf(((XmlLong) object).getLongValue());
			} else if (object instanceof XmlInt) {
				value = (T) Integer.valueOf(((XmlInt) object).getIntValue());
			}
		} else if (expectedType == Integer.class || expectedType == Integer.TYPE) {
			if (object instanceof XmlInt) {
				value = (T) Integer.valueOf(((XmlInt) object).getIntValue());
			}
		} else if (expectedType == Float.class || expectedType == Float.TYPE) {
			if (object instanceof XmlFloat) {
				value = (T) Float.valueOf(((XmlFloat) object).getFloatValue());
			}
		} else if (expectedType == Double.class || expectedType == Double.TYPE) {
			if (object instanceof XmlDouble) {
				value = (T) Double.valueOf(((XmlDouble) object).getDoubleValue());
			}
		} else if (expectedType == URI.class) {
			if (object instanceof XmlAnyURI) {
				XmlAnyURI xmlUri = (XmlAnyURI) object;
				String uri = xmlUri.getStringValue();
				try {
					value = (T) new URI(uri);
				} catch (URISyntaxException e) {
					throw new ValueConfigurationException(format("value '%s' cannot be converted to a '%s'",
							uri, URI.class.getName()),
							expectedType, expression);
				}
			}
		} 
		if (!nullValue 
				&& value == null) {
			throw new ValueConfigurationException(format(
					"no conversion from type '%s' to '%s'",
					object.getClass().getName(), expectedType.getName()),
					expectedType, expression);
		}
		return value;
	}
}
