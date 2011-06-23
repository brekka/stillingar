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

package org.brekka.stillingar.core;

import java.net.URL;
import java.util.List;


/**
 * Represents a snapshot in time of the configuration. A
 * {@link ConfigurationSource} will reference a single snapshot at any given
 * time. When an update is performed, it is based on values extracted from a new
 * immutable snapshot.
 * 
 * @author Andrew Taylor
 */
public interface ConfigurationSnapshot {

	/**
	 * The timestamp associated with the resource that was loaded into this
	 * snapshot. For example if the resource was a file, then the lastModified
	 * date would be returned.
	 * 
	 * This value will be used to determine whether the configuration has
	 * changed.
	 * 
	 * @return the timestamp of this snapshot
	 */
	long getTimestamp();

	/**
	 * The location where this snapshot was loaded from. Used for error tracing
	 * so that information messages can include the path of the resource that
	 * this was actually loaded from.
	 * 
	 * @return the URL location of the resource.
	 */
	URL getLocation();

	<T> T retrieve(Class<T> valueType);

	<T> T retrieve(Class<T> valueType, String expression);

	<T> List<T> retrieveList(Class<T> valueType);

	<T> List<T> retrieveList(Class<T> valueType, String expression);

}