/*
 * Copyright 2017 the original author or authors.
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

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.metaobject.AbstractDynamicObject;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.DynamicObject;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.util.ConfigureUtil;

import java.util.Map;

import static java.lang.String.format;
import static org.gradle.api.reflect.TypeOf.typeOf;
import static org.gradle.internal.Cast.uncheckedCast;

public class DefaultExtensionContainer implements ExtensionContainerInternal {

    private final DefaultExtensionContainer.ExtensionsDynamicObject extensionsDynamicObject = new ExtensionsDynamicObject();
    private final ExtensionsStorage extensionsStorage = new ExtensionsStorage();
    private final ExtraPropertiesExtension extraProperties = new DefaultExtraPropertiesExtension();
    private final Instantiator instantiator;

    public DefaultExtensionContainer(Instantiator instantiator) {
        this.instantiator = instantiator;
        add(ExtraPropertiesExtension.class, ExtraPropertiesExtension.EXTENSION_NAME, extraProperties);
    }

    @Override
    public DynamicObject getExtensionsAsDynamicObject() {
        return extensionsDynamicObject;
    }

    private Instantiator getInstantiator() {
        if (instantiator == null) {
            throw new GradleException("request for DefaultConvention.instantiator when the object was constructed without a convention");
        }
        return instantiator;
    }
    @Override
    public void add(String name, Object extension) {
        if (extension instanceof Class) {
            create(name, (Class<?>) extension);
        } else {
            addWithDefaultPublicType(extension.getClass(), name, extension);
        }
    }

    @Override
    public <T> void add(Class<T> publicType, String name, T extension) {
        add(typeOf(publicType), name, extension);
    }

    @Override
    public <T> void add(TypeOf<T> publicType, String name, T extension) {
        extensionsStorage.add(publicType, name, extension);
    }

    @Override
    public <T> T create(String name, Class<T> instanceType, Object... constructionArguments) {
        T instance = instantiate(instanceType, constructionArguments);
        addWithDefaultPublicType(instanceType, name, instance);
        return instance;
    }

    @Override
    public <T> T create(Class<T> publicType, String name, Class<? extends T> instanceType, Object... constructionArguments) {
        return create(typeOf(publicType), name, instanceType, constructionArguments);
    }

    @Override
    public <T> T create(TypeOf<T> publicType, String name, Class<? extends T> instanceType, Object... constructionArguments) {
        T instance = instantiate(instanceType, constructionArguments);
        add(publicType, name, instance);
        return instance;
    }

    @Override
    public ExtraPropertiesExtension getExtraProperties() {
        return extraProperties;
    }

    @Override
    public Map<String, TypeOf<?>> getSchema() {
        return extensionsStorage.getSchema();
    }

    @Override
    public <T> T getByType(Class<T> type) {
        return getByType(typeOf(type));
    }

    @Override
    public <T> T getByType(TypeOf<T> type) {
        return extensionsStorage.getByType(type);
    }

    @Override
    public <T> T findByType(Class<T> type) {
        return findByType(typeOf(type));
    }

    @Override
    public <T> T findByType(TypeOf<T> type) {
        return extensionsStorage.findByType(type);
    }

    @Override
    public Object getByName(String name) {
        return extensionsStorage.getByName(name);
    }

    @Override
    public Object findByName(String name) {
        return extensionsStorage.findByName(name);
    }

    @Override
    public <T> void configure(Class<T> type, Action<? super T> action) {
        configure(typeOf(type), action);
    }

    @Override
    public <T> void configure(TypeOf<T> type, Action<? super T> action) {
        extensionsStorage.configureExtension(type, action);
    }

    @Override
    public <T> void configure(String name, Action<? super T> action) {
        extensionsStorage.configureExtension(name, action);
    }

    @Override
    public Map<String, Object> getAsMap() {
        return extensionsStorage.getAsMap();
    }

    public Object propertyMissing(String name) {
        return getByName(name);
    }

    public void propertyMissing(String name, Object value) {
        checkExtensionIsNotReassigned(name);
        add(name, value);
    }

    private void addWithDefaultPublicType(Class<?> defaultType, String name, Object extension) {
        add(preferredPublicTypeOf(extension, defaultType), name, extension);
    }

    private TypeOf<Object> preferredPublicTypeOf(Object extension, Class<?> defaultType) {
        if (extension instanceof HasPublicType) {
            return uncheckedCast(((HasPublicType) extension).getPublicType());
        }
        return TypeOf.<Object>typeOf(defaultType);
    }

    private <T> T instantiate(Class<? extends T> instanceType, Object[] constructionArguments) {
        return getInstantiator().newInstance(instanceType, constructionArguments);
    }

    private class ExtensionsDynamicObject extends AbstractDynamicObject {
        @Override
        public String getDisplayName() {
            return "extensions";
        }

        @Override
        public boolean hasProperty(String name) {
            if (extensionsStorage.hasExtension(name)) {
                return true;
            }
            return false;
        }

        @Override
        public Map<String, Object> getProperties() {
            return extensionsStorage.getAsMap();
        }

        @Override
        public DynamicInvokeResult tryGetProperty(String name) {
            Object extension = extensionsStorage.findByName(name);
            if (extension != null) {
                return DynamicInvokeResult.found(extension);
            }
            return DynamicInvokeResult.notFound();
        }

        public Object propertyMissing(String name) {
            return getProperty(name);
        }

        @Override
        public DynamicInvokeResult trySetProperty(String name, Object value) {
            checkExtensionIsNotReassigned(name);
            return DynamicInvokeResult.notFound();
        }

        public void propertyMissing(String name, Object value) {
            setProperty(name, value);
        }

        @Override
        public DynamicInvokeResult tryInvokeMethod(String name, Object... args) {
            if (isConfigureExtensionMethod(name, args)) {
                return DynamicInvokeResult.found(configureExtension(name, args));
            }
            return DynamicInvokeResult.notFound();
        }

        public Object methodMissing(String name, Object args) {
            return invokeMethod(name, (Object[]) args);
        }

        @Override
        public boolean hasMethod(String name, Object... args) {
            if (isConfigureExtensionMethod(name, args)) {
                return true;
            }
            return false;
        }
    }

    private void checkExtensionIsNotReassigned(String name) {
        if (extensionsStorage.hasExtension(name)) {
            throw new IllegalArgumentException(
                format("There's an extension registered with name '%s'. You should not reassign it via a property setter.", name));
        }
    }

    private boolean isConfigureExtensionMethod(String name, Object[] args) {
        return args.length == 1 && args[0] instanceof Closure && extensionsStorage.hasExtension(name);
    }

    private Object configureExtension(String name, Object[] args) {
        Closure closure = (Closure) args[0];
        Action<Object> action = ConfigureUtil.configureUsing(closure);
        return extensionsStorage.configureExtension(name, action);
    }
}
