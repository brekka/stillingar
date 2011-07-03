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

import java.util.Arrays;
import java.util.HashSet;

/**
 * Specialisation of {@link BaseDirResolver} that will attempt to locate the configuration base directory within the
 * current user's home under the directory identified by the constructors only argument.
 * 
 * @author Andrew Taylor
 */
public class BaseInHomeDirResolver extends BaseDirResolver {

    /**
     * The path of the configuration directory that will be formatted with the name of the sub-directory specific to
     * this application.
     */
    private static final String USER_HOME_PATH = "file:${user.home}/%s/";

    /**
     * @param baseWithinHome
     *            the name of the directory within the user home that will form the base configuration directory.
     */
    public BaseInHomeDirResolver(String baseWithinHome) {
        super(new HashSet<String>(Arrays.asList(format(USER_HOME_PATH, baseWithinHome))));
    }
}
