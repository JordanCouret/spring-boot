/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.scheduling;

import org.quartz.Scheduler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.scheduling.QuartzGroupEndpoint;
import org.springframework.boot.actuate.scheduling.QuartzJobEndpoint;
import org.springframework.boot.actuate.scheduling.QuartzTriggerEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link QuartzJobEndpoint},
 * {@link QuartzGroupEndpoint}, {@link QuartzTriggerEndpoint}.
 *
 * @author Jordan Couret
 * @since 2.2.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Scheduler.class)
@ConditionalOnBean(Scheduler.class)
@AutoConfigureAfter(QuartzAutoConfiguration.class)
public class QuartzEndpointAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint(endpoint = QuartzGroupEndpoint.class)
	public QuartzGroupEndpoint quartzGroupEndpoint(ObjectProvider<Scheduler> scheduler) {
		return new QuartzGroupEndpoint(scheduler.getIfAvailable());
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint(endpoint = QuartzJobEndpoint.class)
	public QuartzJobEndpoint quartzJobEndpoint(ObjectProvider<Scheduler> scheduler) {
		return new QuartzJobEndpoint(scheduler.getIfAvailable());
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint(endpoint = QuartzTriggerEndpoint.class)
	public QuartzTriggerEndpoint quartzTriggerEndpoint(ObjectProvider<Scheduler> scheduler) {
		return new QuartzTriggerEndpoint(scheduler.getIfAvailable());
	}

}
