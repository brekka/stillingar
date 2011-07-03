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

package org.brekka.stillingar.spring.resource;

import static java.lang.String.format;

import java.io.IOException;
import java.util.List;

import org.brekka.stillingar.core.ConfigurationException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * TODO better name
 * @author Andrew Taylor
 */
public class ScanningResourceSelector implements ResourceSelector {

	private final Resource original;
	
	private final Resource lastGood;
	
	public ScanningResourceSelector(List<Resource> locations, ResourceNaming resourceNameResolver) {
		Resource original = null;
		Resource lastGood = null;
		
		String originalName = resourceNameResolver.prepareOriginalName();
		String lastGoodName = resourceNameResolver.prepareLastGoodName();
		
		for (Resource locationBase : locations) {
			try {
				if (locationBase != null 
						&& locationBase.exists()) {
					Resource locationOriginal = locationBase.createRelative(originalName);
					if (locationOriginal.exists()
							&& locationOriginal.isReadable()) {
						original = locationOriginal;
						if (locationBase instanceof FileSystemResource) {
							lastGood = locationBase.createRelative(lastGoodName);
						}
						// We have found what we are looking for
						break;
					}
				}
			} catch (IOException e) {
				// TODO fix exception handling
				throw new ConfigurationException("Bad url", e);
			}
		}
		if (original == null) {
			// TODO message
			throw new ConfigurationException(format("No configuration could be found with the name '%s'", originalName));
		}
		this.original = original;
		this.lastGood = lastGood;
	}
	
	public Resource getOriginal() {
		return original;
	}
	
	public Resource getLastGood() {
		return lastGood;
	}
}
