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
package org.gradle.api.plugins;

import org.gradle.api.Action;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.HasInternalProtocol;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * <p>A {@code Convention} manages a set of <i>convention objects</i>. When you add a convention object to a {@code
 * Convention}, and the properties and methods of the convention object become available as properties and methods of
 * the object which the convention is associated to. A convention object is simply a POJO or POGO. Usually, a {@code
 * Convention} is used by plugins to extend a {@link org.gradle.api.Project} or a {@link org.gradle.api.Task}.</p>
 *
 * This class extends {@link ExtensionContainer} for historical reasons, but should not be used as such.
 */
@HasInternalProtocol
public interface Convention extends ExtensionContainer {

    /**
     * Returns the plugin convention objects contained in this convention.
     *
     * @return The plugins. Returns an empty map when this convention does not contain any convention objects.
     */
    Map<String, Object> getPlugins();

    /**
     * Locates the plugin convention object with the given type.
     *
     * @param type The convention object type.
     * @return The object. Never returns null.
     * @throws IllegalStateException When there is no such object contained in this convention, or when there are multiple such objects.
     */
    <T> T getPlugin(Class<T> type) throws IllegalStateException;

    /**
     * Locates the plugin convention object with the given type.
     *
     * @param type The convention object type.
     * @return The object. Returns null if there is no such object.
     * @throws IllegalStateException When there are multiple matching objects.
     */
    @Nullable
    <T> T findPlugin(Class<T> type) throws IllegalStateException;

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> void add(Class<T> publicType, String name, T extension);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> void add(TypeOf<T> publicType, String name, T extension);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    void add(String name, Object extension);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> T create(Class<T> publicType, String name, Class<? extends T> instanceType, Object... constructionArguments);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> T create(TypeOf<T> publicType, String name, Class<? extends T> instanceType, Object... constructionArguments);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> T create(String name, Class<T> type, Object... constructionArguments);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    Map<String, TypeOf<?>> getSchema();

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> T getByType(Class<T> type) throws UnknownDomainObjectException;

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> T getByType(TypeOf<T> type) throws UnknownDomainObjectException;

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    @Nullable
    <T> T findByType(Class<T> type);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    @Nullable
    <T> T findByType(TypeOf<T> type);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    Object getByName(String name) throws UnknownDomainObjectException;

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    @Nullable
    Object findByName(String name);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> void configure(Class<T> type, Action<? super T> action);

    /**
     * v
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> void configure(TypeOf<T> type, Action<? super T> action);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    <T> void configure(String name, Action<? super T> action);

    /**
     * This method is only here for historical reasons.
     *
     * @deprecated use {@link ExtensionAware#getExtensions()} instead.
     */
    @Deprecated
    ExtraPropertiesExtension getExtraProperties();
}
