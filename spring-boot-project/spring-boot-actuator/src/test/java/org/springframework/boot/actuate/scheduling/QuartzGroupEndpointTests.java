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

package org.springframework.boot.actuate.scheduling;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Tests for {@link QuartzGroupEndpoint}.
 *
 * @author Jordan Couret
 * @since 2.2.0
 */
class QuartzGroupEndpointTests extends AbstractQuartzJobEndpointTests {

	@Test
	void shouldRetrieveAllGroups() {
		this.contextRunner.run((context) -> {
			this.createTestJob(context);
			Assertions.assertThat(context).hasSingleBean(QuartzGroupEndpoint.class);
			QuartzGroupEndpoint quartzGroupEndpoint = context.getBean(QuartzGroupEndpoint.class);
			Assertions.assertThat(quartzGroupEndpoint.findAllGroup()).isNotNull();
			Assertions.assertThat(quartzGroupEndpoint.findAllGroup().getJobGroups()).hasSize(1);
			Assertions.assertThat(quartzGroupEndpoint.findAllGroup().getTriggerGroups()).hasSize(1);
		});
	}

	@Override
	public Class getAutoConfigurationForEndpoint() {
		return QuartzGroupEndPointConfiguration.class;
	}

	@AutoConfigureAfter(QuartzAutoConfiguration.class)
	public static class QuartzGroupEndPointConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public QuartzGroupEndpoint quartzEndpoint(ObjectProvider<Scheduler> scheduler) {
			return new QuartzGroupEndpoint(scheduler.getIfAvailable());

		}

	}

}
