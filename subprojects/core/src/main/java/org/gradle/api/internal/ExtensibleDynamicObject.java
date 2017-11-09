/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.internal;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.gradle.api.internal.plugins.ConventionInternal;
import org.gradle.api.internal.plugins.DefaultConvention;
import org.gradle.api.internal.plugins.DefaultExtensionContainer;
import org.gradle.api.internal.plugins.ExtraPropertiesDynamicObjectAdapter;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.internal.metaobject.AbstractDynamicObject;
import org.gradle.internal.metaobject.BeanDynamicObject;
import org.gradle.internal.metaobject.CompositeDynamicObject;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.DynamicObject;
import org.gradle.internal.metaobject.MixInClosurePropertiesAsMethodsDynamicObject;
import org.gradle.internal.reflect.Instantiator;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A {@link DynamicObject} implementation that provides extensibility.
 *
 * This is the dynamic object implementation that “enhanced” objects expose.
 *
 * @see org.gradle.api.internal.AsmBackedClassGenerator.MixInExtensibleDynamicObject
 */
public class ExtensibleDynamicObject extends MixInClosurePropertiesAsMethodsDynamicObject implements HasConvention {


    public enum Location {
        BeforeConvention, AfterConvention
    }

    private final AbstractDynamicObject dynamicDelegate;
    private final DefaultExtensionContainer extensions;
    private final DynamicObject extraPropertiesDynamicObject;

    private DynamicObject parent;
    private DynamicObject beforeConvention;
    private ConventionInternal convention;
    private DynamicObject afterConvention;

    private DynamicObject[] objects;
    private DynamicObject[] objectsForUpdate;
    private DynamicObject[] inheritableObjects;

    public ExtensibleDynamicObject(Object delegate, Class<?> publicType, Instantiator instantiator) {
        this(delegate, createDynamicObject(delegate, publicType), instantiator);
    }

    public ExtensibleDynamicObject(Object delegate, AbstractDynamicObject dynamicDelegate, Instantiator instantiator) {
        this.dynamicDelegate = dynamicDelegate;
        extensions = new DefaultExtensionContainer(instantiator);
        extraPropertiesDynamicObject = new ExtraPropertiesDynamicObjectAdapter(delegate.getClass(), extensions.getExtraProperties());
    }

    private static BeanDynamicObject createDynamicObject(Object delegate, Class<?> publicType) {
        return new BeanDynamicObject(delegate, publicType);
    }

    private void updateDelegates() {
        DynamicObject extensionsAsDynamicObject = extensions.getExtensionsAsDynamicObject();
        DynamicObject conventionsAsDynamicObject = convention == null ? null : convention.getConventionsAsDynamicObject();

        if (objects != null) {
            objects[0] = dynamicDelegate;
            objects[1] = extraPropertiesDynamicObject;
            objects[2] = beforeConvention;
            objects[3] = extensionsAsDynamicObject;
            objects[4] = conventionsAsDynamicObject;
            objects[5] = afterConvention;
            objects[6] = parent;
        }

        if (objectsForUpdate != null) {
            objectsForUpdate[0] = dynamicDelegate;
            objectsForUpdate[1] = extraPropertiesDynamicObject;
            objectsForUpdate[2] = beforeConvention;
            objectsForUpdate[3] = extensionsAsDynamicObject;
            objectsForUpdate[4] = conventionsAsDynamicObject;
            objectsForUpdate[5] = afterConvention;
        }

        if (inheritableObjects != null) {
            inheritableObjects[0] = extraPropertiesDynamicObject;
            inheritableObjects[1] = beforeConvention;
            inheritableObjects[2] = extensionsAsDynamicObject;
            inheritableObjects[3] = conventionsAsDynamicObject;
            inheritableObjects[4] = parent;
        }
    }

    @Override
    public DynamicObject[] getObjects() {
        if (objects == null) {
            objects = new DynamicObject[7];
            updateDelegates();
        }
        return objects;
    }

    public DynamicObject[] getUpdateObjects() {
        if (objectsForUpdate == null) {
            objectsForUpdate = new DynamicObject[7];
            updateDelegates();
        }
        return objectsForUpdate;
    }

    @Override
    public String getDisplayName() {
        return dynamicDelegate.getDisplayName();
    }

    @Nullable
    @Override
    public Class<?> getPublicType() {
        return dynamicDelegate.getPublicType();
    }

    @Override
    public boolean hasUsefulDisplayName() {
        return dynamicDelegate.hasUsefulDisplayName();
    }

    public ExtraPropertiesExtension getDynamicProperties() {
        return extensions.getExtraProperties();
    }

    public void addProperties(Map<String, ?> properties) {
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            getDynamicProperties().set(entry.getKey(), entry.getValue());
        }
    }

    public DynamicObject getParent() {
        return parent;
    }

    public void setParent(DynamicObject parent) {
        this.parent = parent;
        updateDelegates();
    }

    public ExtensionContainer getExtensions() {
        return extensions;
    }

    public Convention getConvention() {
        if (convention == null) {
            convention = new DefaultConvention(extensions);
            updateDelegates();
        }
        return convention;
    }

    public void addObject(DynamicObject object, Location location) {
        switch (location) {
            case BeforeConvention:
                beforeConvention = object;
                break;
            case AfterConvention:
                afterConvention = object;
        }
        updateDelegates();
    }

    /**
     * Returns the inheritable properties and methods of this object.
     *
     * @return an object containing the inheritable properties and methods of this object.
     */
    public DynamicObject getInheritable() {
        if (inheritableObjects == null) {
            inheritableObjects = new DynamicObject[5];
            updateDelegates();
        }
        return new InheritedDynamicObject(dynamicDelegate, inheritableObjects);
    }

    private static class InheritedDynamicObject implements DynamicObject {
        private final AbstractDynamicObject dynamicDelegate;
        private DynamicObject inheritableDelegate;

        private InheritedDynamicObject(final AbstractDynamicObject dynamicDelegate, final DynamicObject[] inheritableObjects) {
            this.dynamicDelegate = dynamicDelegate;
            inheritableDelegate = new CompositeDynamicObject() {
                @Override
                protected DynamicObject[] getObjects() {
                    return inheritableObjects;
                }

                @Override
                public String getDisplayName() {
                    return dynamicDelegate.getDisplayName();
                }
            };
        }

        @Override
        public void setProperty(String name, Object value) {
            throw new MissingPropertyException(String.format("Could not find property '%s' inherited from %s.", name,
                dynamicDelegate.getDisplayName()));
        }

        @Override
        public MissingPropertyException getMissingProperty(String name) {
            return dynamicDelegate.getMissingProperty(name);
        }

        @Override
        public MissingPropertyException setMissingProperty(String name) {
            return dynamicDelegate.setMissingProperty(name);
        }

        @Override
        public MissingMethodException methodMissingException(String name, Object... params) {
            return dynamicDelegate.methodMissingException(name, params);
        }

        @Override
        public DynamicInvokeResult trySetProperty(String name, Object value) {
            setProperty(name, value);
            return DynamicInvokeResult.found();
        }

        @Override
        public boolean hasProperty(String name) {
            return inheritableDelegate.hasProperty(name);
        }

        @Override
        public Object getProperty(String name) {
            return inheritableDelegate.getProperty(name);
        }

        @Override
        public DynamicInvokeResult tryGetProperty(String name) {
            return inheritableDelegate.tryGetProperty(name);
        }

        @Override
        public Map<String, ?> getProperties() {
            return inheritableDelegate.getProperties();
        }

        @Override
        public boolean hasMethod(String name, Object... arguments) {
            return inheritableDelegate.hasMethod(name, arguments);
        }

        @Override
        public DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
            return inheritableDelegate.tryInvokeMethod(name, arguments);
        }

        @Override
        public Object invokeMethod(String name, Object... arguments) {
            return inheritableDelegate.invokeMethod(name, arguments);
        }

    }
}

