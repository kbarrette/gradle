/*
 * Copyright 2007-2008 the original author or authors.
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

package org.gradle.api.internal.plugins

import org.gradle.api.internal.ThreadGlobalInstantiator
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.TestPluginConvention1
import org.gradle.api.plugins.TestPluginConvention2
import org.gradle.internal.reflect.Instantiator
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.*

class DefaultConventionTest {
    Convention convention

    TestPluginConvention1 convention1
    TestPluginConvention2 convention2

    Instantiator instantiator = ThreadGlobalInstantiator.getOrCreate()

    @Before void setUp() {
        convention = new DefaultConvention()
        convention1 = new TestPluginConvention1()
        convention2 = new TestPluginConvention2()
        convention.plugins.plugin1 = convention1
        convention.plugins.plugin2 = convention2
    }

    @Test void mixesInEachPropertyOfConventionObject() {
        assertEquals(convention1.b, convention.conventionsAsDynamicObject.b)
    }

    @Test void conventionObjectsPropertiesHavePrecendenceAccordingToOrderAdded() {
        assertEquals(convention1.a, convention.conventionsAsDynamicObject.a)
    }

    @Test void canSetConventionObjectProperties() {
        convention.conventionsAsDynamicObject.b = 'newvalue'
        assertEquals('newvalue', convention1.b)
    }

    @Test void canSetPropertiesWithAmbiguity() {
        convention.conventionsAsDynamicObject.a = 'newvalue'
        assertEquals('newvalue', convention1.a)
    }

    @Test(expected = MissingPropertyException) void throwsMissingPropertyExceptionForUnknownProperty() {
        convention.conventionsAsDynamicObject.prop
    }

    @Test void mixesInEachMethodOfConventionObject() {
        assertEquals(convention1.meth('somearg'), convention.conventionsAsDynamicObject.meth('somearg'))
    }

    @Test void conventionObjectsMethodsHavePrecendenceAccordingToOrderAdded() {
        assertEquals(convention1.meth(), convention.conventionsAsDynamicObject.meth())
    }

    @Test(expected = MissingMethodException) void testMissingMethod() {
        convention.conventionsAsDynamicObject.methUnknown()
    }

    @Test void testCanLocateConventionObjectByType() {
        assertSame(convention1, convention.getPlugin(TestPluginConvention1))
        assertSame(convention2, convention.getPlugin(TestPluginConvention2))
        assertSame(convention1, convention.findPlugin(TestPluginConvention1))
        assertSame(convention2, convention.findPlugin(TestPluginConvention2))
    }

    @Test void testGetPluginFailsWhenMultipleConventionObjectsWithCompatibleType() {
        try {
            convention.getPlugin(Object)
            fail()
        } catch (java.lang.IllegalStateException e) {
            assertThat(e.message, equalTo('Found multiple convention objects of type Object.'))
        }
    }

    @Test void testFindPluginFailsWhenMultipleConventionObjectsWithCompatibleType() {
        try {
            convention.getPlugin(Object)
            fail()
        } catch (java.lang.IllegalStateException e) {
            assertThat(e.message, equalTo('Found multiple convention objects of type Object.'))
        }
    }

    @Test void testGetPluginFailsWhenNoConventionObjectsWithCompatibleType() {
        try {
            convention.getPlugin(String)
            fail()
        } catch (java.lang.IllegalStateException e) {
            assertThat(e.message, equalTo('Could not find any convention object of type String.'))
        }
    }

    @Test void testFindPluginReturnsNullWhenNoConventionObjectsWithCompatibleType() {
        assertNull(convention.findPlugin(String))
    }
}
