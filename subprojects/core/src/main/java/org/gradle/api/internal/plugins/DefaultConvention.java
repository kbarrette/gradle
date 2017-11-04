/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.api.internal.plugins;

import com.google.common.collect.Maps;
import org.gradle.api.Action;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.metaobject.AbstractDynamicObject;
import org.gradle.internal.metaobject.BeanDynamicObject;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.DynamicObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class DefaultConvention implements ConventionInternal {

    private final Map<String, Object> plugins = Maps.newLinkedHashMap();
    private final ConventionsDynamicObject conventionsDynamicObject = new ConventionsDynamicObject();
    private Map<Object, BeanDynamicObject> dynamicObjects;
    private DefaultExtensionContainer extensions;

    /**
     * This method should not be used in runtime code proper as means that the convention cannot create
     * dynamic extensions.
     *
     * It's here for backwards compatibility with our tests and for convenience.
     *
     * @see #DefaultConvention(DefaultExtensionContainer)
     */
    public DefaultConvention() {
        this(null);
    }

    public DefaultConvention(DefaultExtensionContainer extensions) {
        this.extensions = extensions;
    }

    @Override
    public Map<String, Object> getPlugins() {
        return plugins;
    }

    @Override
    public DynamicObject getConventionsAsDynamicObject() {
        return conventionsDynamicObject;
    }


    public Object propertyMissing(String name) {
        return extensions.propertyMissing(name);
    }

    public void propertyMissing(String name, Object value) {
        extensions.propertyMissing(name, value);
    }

    @Override
    public <T> T getPlugin(Class<T> type) {
        T value = findPlugin(type);
        if (value == null) {
            throw new IllegalStateException(
                format("Could not find any convention object of type %s.", type.getSimpleName()));
        }
        return value;
    }

    @Override
    public <T> T findPlugin(Class<T> type) throws IllegalStateException {
        List<T> values = new ArrayList<T>();
        for (Object object : getPlugins().values()) {
            if (type.isInstance(object)) {
                values.add(type.cast(object));
            }
        }
        if (values.isEmpty()) {
            return null;
        }
        if (values.size() > 1) {
            throw new IllegalStateException(
                format("Found multiple convention objects of type %s.", type.getSimpleName()));
        }
        return values.get(0);
    }

    @Override
    public <T> void add(Class<T> publicType, String name, T extension) {
        extensions.add(publicType, name, extension);
    }

    @Override
    public <T> void add(TypeOf<T> publicType, String name, T extension) {
        extensions.add(publicType, name, extension);
    }

    @Override
    public void add(String name, Object extension) {
        extensions.add(name, extension);
    }

    @Override
    public <T> T create(Class<T> publicType, String name, Class<? extends T> instanceType, Object... constructionArguments) {
        return extensions.create(publicType, name, instanceType, constructionArguments);
    }

    @Override
    public <T> T create(TypeOf<T> publicType, String name, Class<? extends T> instanceType, Object... constructionArguments) {
        return extensions.create(publicType, name, instanceType, constructionArguments);
    }

    @Override
    public <T> T create(String name, Class<T> type, Object... constructionArguments) {
        return extensions.create(name, type, constructionArguments);
    }

    @Override
    public Map<String, TypeOf<?>> getSchema() {
        return extensions.getSchema();
    }

    @Override
    public <T> T getByType(Class<T> type) throws UnknownDomainObjectException {
        return extensions.getByType(type);
    }

    @Override
    public <T> T getByType(TypeOf<T> type) throws UnknownDomainObjectException {
        return extensions.getByType(type);
    }

    @Nullable
    @Override
    public <T> T findByType(Class<T> type) {
        return extensions.findByType(type);
    }

    @Nullable
    @Override
    public <T> T findByType(TypeOf<T> type) {
        return extensions.findByType(type);
    }

    @Override
    public Object getByName(String name) throws UnknownDomainObjectException {
        return extensions.getByName(name);
    }

    @Nullable
    @Override
    public Object findByName(String name) {
        return extensions.findByName(name);
    }

    @Override
    public <T> void configure(Class<T> type, Action<? super T> action) {
        extensions.configure(type, action);
    }

    @Override
    public <T> void configure(TypeOf<T> type, Action<? super T> action) {
        extensions.configure(type, action);
    }

    @Override
    public <T> void configure(String name, Action<? super T> action) {
        extensions.configure(name, action);
    }

    @Override
    public ExtraPropertiesExtension getExtraProperties() {
        return extensions.getExtraProperties();
    }

    @Override
    public Map<String, Object> getAsMap() {
        return extensions.getAsMap();
    }

    @Override
    public DynamicObject getExtensionsAsDynamicObject() {
        return extensions.getExtensionsAsDynamicObject();
    }

    private class ConventionsDynamicObject extends AbstractDynamicObject {
        @Override
        public String getDisplayName() {
            return "conventions";
        }

        @Override
        public boolean hasProperty(String name) {
            for (Object object : getPlugins().values()) {
                if (asDynamicObject(object).hasProperty(name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Map<String, Object> getProperties() {
            Map<String, Object> properties = new HashMap<String, Object>();
            List<Object> reverseOrder = new ArrayList<Object>(getPlugins().values());
            Collections.reverse(reverseOrder);
            for (Object object : reverseOrder) {
                properties.putAll(asDynamicObject(object).getProperties());
            }
            return properties;
        }

        @Override
        public DynamicInvokeResult tryGetProperty(String name) {
            for (Object object : getPlugins().values()) {
                DynamicObject dynamicObject = asDynamicObject(object).withNotImplementsMissing();
                DynamicInvokeResult result = dynamicObject.tryGetProperty(name);
                if (result.isFound()) {
                    return result;
                }
            }
            return DynamicInvokeResult.notFound();
        }

        public Object propertyMissing(String name) {
            return getProperty(name);
        }

        @Override
        public DynamicInvokeResult trySetProperty(String name, Object value) {
            for (Object object : getPlugins().values()) {
                BeanDynamicObject dynamicObject = asDynamicObject(object).withNotImplementsMissing();
                DynamicInvokeResult result = dynamicObject.trySetProperty(name, value);
                if (result.isFound()) {
                    return result;
                }
            }
            return DynamicInvokeResult.notFound();
        }

        public void propertyMissing(String name, Object value) {
            setProperty(name, value);
        }

        @Override
        public DynamicInvokeResult tryInvokeMethod(String name, Object... args) {
            for (Object object : getPlugins().values()) {
                BeanDynamicObject dynamicObject = asDynamicObject(object).withNotImplementsMissing();
                DynamicInvokeResult result = dynamicObject.tryInvokeMethod(name, args);
                if (result.isFound()) {
                    return result;
                }
            }
            return DynamicInvokeResult.notFound();
        }

        public Object methodMissing(String name, Object args) {
            return invokeMethod(name, (Object[]) args);
        }

        @Override
        public boolean hasMethod(String name, Object... args) {
            for (Object object : getPlugins().values()) {
                BeanDynamicObject dynamicObject = asDynamicObject(object);
                if (dynamicObject.hasMethod(name, args)) {
                    return true;
                }
            }
            return false;
        }

        private BeanDynamicObject asDynamicObject(Object object) {
            if (dynamicObjects == null) {
                dynamicObjects = Maps.newIdentityHashMap();
            }
            BeanDynamicObject dynamicObject = dynamicObjects.get(object);
            if (dynamicObject == null) {
                dynamicObject = new BeanDynamicObject(object);
                dynamicObjects.put(object, dynamicObject);
            }
            return dynamicObject;
        }
    }
}
