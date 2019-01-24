/*
 * Copyright 2019 Institut Laue–Langevin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.ill.puma.analysis.annotation;

import eu.ill.puma.persistence.domain.analysis.EntityType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by letreguilly on 24/07/17.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Analyser {
	String name();

	EntityType[] produces();

	int maxInstances() default -1;

	boolean limitMaxInstanceToPhysicalCpuCore() default false;

	boolean enabled() default true;
}
