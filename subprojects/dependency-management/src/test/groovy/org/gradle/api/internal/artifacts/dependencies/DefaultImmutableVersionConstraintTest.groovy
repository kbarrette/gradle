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

package org.gradle.api.internal.artifacts.dependencies

import org.gradle.api.internal.artifacts.ImmutableVersionConstraint
import spock.lang.Specification

class DefaultImmutableVersionConstraintTest extends Specification {
    def "can create an immutable version constraint without rejects"() {
        given:
        def v = new DefaultImmutableVersionConstraint('1.0')

        expect:
        v.preferredVersion == '1.0'
        v.rejectedVersions == []
    }

    def "can create an immutable version constraint with rejects"() {
        given:
        def v = new DefaultImmutableVersionConstraint('1.0', ['1.1','2.0'])

        expect:
        v.preferredVersion == '1.0'
        v.rejectedVersions == ['1.1','2.0']
    }

    def "cannot mutate rejection list"() {
        given:
        def v = new DefaultImmutableVersionConstraint('1.0', ['1.1','2.0'])

        when:
        v.rejectedVersions.add('3.0')

        then:
        def e = thrown(UnsupportedOperationException)
    }

    def "cannot use null as preferred version"() {
        when:
        new DefaultImmutableVersionConstraint(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Preferred version must not be null'

        when:
        new DefaultImmutableVersionConstraint(null, [])

        then:
        e = thrown(IllegalArgumentException)
        e.message == 'Preferred version must not be null'
    }

    def "cannot use null as rejected versions"() {
        when:
        def v = new DefaultImmutableVersionConstraint('1.0', null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Rejected versions must not be null'
    }

    def "doesn't create a copy of an already immutable version constraint"() {
        given:
        def v = new DefaultImmutableVersionConstraint('1.0')

        when:
        def c = DefaultImmutableVersionConstraint.of(v)

        then:
        v.is(c)
    }

    def "can convert mutable version constraint to immutable version constraint"() {
        given:
        def v = new DefaultMutableVersionConstraint('1.0', ['1.1', '2.0'])

        when:
        def c = DefaultImmutableVersionConstraint.of(v)

        then:
        !v.is(c)
        c instanceof ImmutableVersionConstraint
        c.preferredVersion == v.preferredVersion
        c.rejectedVersions == v.rejectedVersions
    }
}
