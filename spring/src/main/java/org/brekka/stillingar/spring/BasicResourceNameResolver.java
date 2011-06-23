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

import static java.lang.String.format;

public class BasicResourceNameResolver implements ResourceNameResolver {

	private final String prefix;
	
	private String suffix = "xml";
	
	private String lastGoodMarker = "lastgood";
	
	
	
	public BasicResourceNameResolver(String prefix) {
		this.prefix = prefix;
	}

	public String prepareOriginalName() {
		return format("%s.%s", prefix, suffix);
	}

	public String prepareLastGoodName() {
		return format("%s-%s.%s", prefix, lastGoodMarker, suffix);
	}
	
	protected String getLastGoodMarker() {
		return lastGoodMarker;
	}
	
	protected String getSuffix() {
		return suffix;
	}
	
	protected String getPrefix() {
		return prefix;
	}

	public void setLastGoodMarker(String lastGoodMarker) {
		this.lastGoodMarker = lastGoodMarker;
	}
	
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}
