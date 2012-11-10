/*
 * Copyright 2012 the original author or authors.
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

package org.brekka.stillingar.core.conversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds a list of converters.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class TypeConverterListBuilder {

    /**
     * Collect converters into this list
     */
    private final List<TypeConverter<?>> list;

    /**
     * A new empty list builder
     */
    public TypeConverterListBuilder() {
        this.list = new ArrayList<TypeConverter<?>>();
    }

    /**
     * A new list builder that will start off as a copy of <code>list</code>. The type converter instances themselves
     * will used as-is (ie will not be cloned).
     * 
     * @param list
     *            the list of existing type converters to use.
     */
    public TypeConverterListBuilder(List<TypeConverter<?>> list) {
        this.list = new ArrayList<TypeConverter<?>>(list);
    }

    /**
     * Add a type converter instance.
     * 
     * @param typeConverter
     *            the converter to add.
     * @return this for chaining
     */
    public TypeConverterListBuilder add(TypeConverter<?> typeConverter) {
        list.add(typeConverter);
        return this;
    }

    /**
     * Add multiple type converter instances.
     * 
     * @param typeConverters
     *            the converters to add.
     * @return this for chaining
     */
    public <T extends TypeConverter<?>> TypeConverterListBuilder addAll(T... typeConverters) {
        for (TypeConverter<?> typeConverter : typeConverters) {
            list.add(typeConverter);
        }
        return this;
    }

    /**
     * Establish a new package context in which classes can be added without having to specify their package name. Once
     * all classes have been added for the package, call {@link Package#done()}.
     * 
     * @param packageName
     *            the name of the package to add classes from.
     * @return the package the package context in which to add classes by name only.
     */
    public Package inPackage(String packageName) {
        return new Package(packageName);
    }

    /**
     * Complete the construction and return the list of type converter instances.
     * 
     * @return the list
     */
    public List<TypeConverter<?>> toList() {
        return list;
    }

    /**
     * Add a type converter instance named <code>className</code>. Any problems loading the class will result in and
     * error.
     * 
     * @param className
     *            the name of the class (including package name).
     * @param args
     *            any constructor arguments to include.
     * @return this for chaining.
     * @throws IllegalStateException
     *             if the specific converter cannot be instantiated as a result of the class not being found or
     *             incorrect arguments being provided for the constructor, or some other security related issue.
     */
    public TypeConverterListBuilder addClass(String className, Object... args) {
        return addClass(true, className, args);
    }

    /**
     * Add an optional type converter instance named <code>className</code>. If the class cannot be found
     * or the type it returns cannot be found, then the class is ignored. An error will still occur if the construction
     * arguments are incorrect or a security manager issue arises.
     * 
     * @param className
     *            the name of the class (including package name).
     * @param args
     *            any constructor arguments to include.
     * @return this for chaining.
     * @throws IllegalStateException
     *             in the event of incorrect arguments being provided for the constructor, or some other security related issue.
     */
    public TypeConverterListBuilder addOptionalClass(String className, Object... args) {
        return addClass(false, className, args);
    }

    /**
     * Add the class
     * 
     * @param failOnNotFound should an exception be thrown on class not found.
     * @param className
     * @param args
     * @return
     */
    private TypeConverterListBuilder addClass(boolean failOnNotFound, String className, Object... args) {
        TypeConverter<?> typeConverter = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Throwable error = null;
        try {
            Class<?> loadClass = classLoader.loadClass(className);
            Constructor<?>[] constructors = loadClass.getConstructors();
            Constructor<?> ctor = null;
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (constructor.getParameterTypes().length == args.length) {
                    boolean pass = true;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (args[i] != null 
                                && !parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                            pass = false;
                            break;
                        }
                    }
                    if (pass) {
                        ctor = constructor;
                        break;
                    }
                }
            }
            
            if (ctor != null) {
                Object result = ctor.newInstance(args);
                if (result instanceof TypeConverter) {
                    typeConverter = (TypeConverter<?>) result;
                    // Invoke now to trigger CNF
                    typeConverter.targetType();
                } else {
                    throw new IllegalStateException(String.format(
                            "The class '%s' does not implement '%s'", 
                            className, TypeConverter.class.getName()));
                }
            } else {
                Class<?>[] argClasses = new Class<?>[args.length];
                for (int i = 0; i < constructors.length; i++) {
                    argClasses[i] = (args[i] != null) ? args[i].getClass() : null;
                }
                throw new IllegalStateException(String.format(
                        "Unable to locate a suitable constructor for class '%s' with args %s", 
                        className, Arrays.asList(argClasses)));
            }
        } catch (ClassNotFoundException e) {
            if (failOnNotFound) {
                error = e;
            }
        } catch (NoClassDefFoundError e) {
            if (failOnNotFound) {
                error = e;
            }
        } catch (InstantiationException e) {
            error = e;
        } catch (IllegalAccessException e) {
            error = e;
        } catch (IllegalArgumentException e) {
            error = e;
        } catch (InvocationTargetException e) {
            error = e;
        }
        
        if (error != null) {
            throw new IllegalStateException(String.format(
                    "Failed to add the class '%s' with %d arguments", 
                    className, args.length), error);
        }

        if (typeConverter != null) {
            list.add(typeConverter);
        }
        return this;
    }

    /**
     * Provides a context for adding multiple classes by simple name only within a package.
     */
    public class Package {
        /**
         * Name of the package
         */
        private final String packageName;

        /**
         * @param packageName Name of the package
         */
        private Package(String packageName) {
            this.packageName = packageName;
        }
        
        /**
         * Add a type converter instance with simple name <code>classShortName</code>. Any problems loading the class will result in and
         * error.
         * 
         * @param classShortName
         *            the simple name of the class.
         * @param args
         *            any constructor arguments to include.
         * @return this for chaining.
         * @throws IllegalStateException
         *             if the specific converter cannot be instantiated as a result of the class not being found or
         *             incorrect arguments being provided for the constructor, or some other security related issue.
         */
        public Package addClass(String classShortName, Object... args) {
            TypeConverterListBuilder.this.addClass(className(classShortName), args);
            return this;
        }


        /**
         * Add an optional type converter instance with simple name <code>classShortName</code>. If the class cannot be found
         * or the type it returns cannot be found, then the class is ignored. An error will still occur if the construction
         * arguments are incorrect or a security manager issue arises.
         * 
         * @param classShortName
         *            the simple name of the class
         * @param args
         *            any constructor arguments to include.
         * @return this for chaining.
         * @throws IllegalStateException
         *             in the event of incorrect arguments being provided for the constructor, or some other security related issue.
         */
        public Package addOptionalClass(String classShortName, Object... args) {
            TypeConverterListBuilder.this.addOptionalClass(className(classShortName), args);
            return this;
        }

        /**
         * Invoke when all classes have been added to close the package context.
         * @return the TypeConverterListBuilder instance.
         */
        public TypeConverterListBuilder done() {
            return TypeConverterListBuilder.this;
        }

        private String className(String classShortName) {
            return packageName + "." + classShortName;
        }
    }
}
