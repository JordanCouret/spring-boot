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

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.scheduling.QuartzTriggerEndpoint.TriggerDesciptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Tests for {@link QuartzTriggerEndpoint}.
 *
 * @author Jordan Couret
 * @since 2.2.0
 */

class QuartzTriggerEndpointTests extends AbstractQuartzJobEndpointTests {

	@Test
	void shouldFindAllJob() {
		this.contextRunner.run((context) -> {
			this.createTestJob(context);

			Assertions.assertThat(context).hasSingleBean(QuartzTriggerEndpoint.class);
			QuartzTriggerEndpoint quartzJobEndpoint = context.getBean(QuartzTriggerEndpoint.class);
			Assertions.assertThat(quartzJobEndpoint.findAll()).hasSize(1);
		});
	}

	@Test
	void shouldFindByGroup() {
		this.contextRunner.run((context) -> {
			this.createTestJob(context);

			Assertions.assertThat(context).hasSingleBean(QuartzTriggerEndpoint.class);
			QuartzTriggerEndpoint quartzJobEndpoint = context.getBean(QuartzTriggerEndpoint.class);
			Set<TriggerKey> searchByGroup = quartzJobEndpoint.findByGroup(TRIGGER_GROUP_NAME);
			Assertions.assertThat(searchByGroup).hasSize(1);
			searchByGroup.forEach(
					(triggerKey) -> Assertions.assertThat(triggerKey.getGroup()).isEqualTo(TRIGGER_GROUP_NAME));
		});
	}

	@Test
	void shouldFindByGroupAndName() {
		this.contextRunner.run((context) -> {
			this.createTestJob(context);

			Assertions.assertThat(context).hasSingleBean(QuartzTriggerEndpoint.class);
			QuartzTriggerEndpoint quartzJobEndpoint = context.getBean(QuartzTriggerEndpoint.class);
			TriggerDesciptor searchByGroupAndName = quartzJobEndpoint.findByGroupAndName(TRIGGER_GROUP_NAME,
					TRIGGER_NAME);
			Assertions.assertThat(searchByGroupAndName.getName()).isEqualTo(TRIGGER_NAME);
			Assertions.assertThat(searchByGroupAndName.getGroup()).isEqualTo(TRIGGER_GROUP_NAME);
		});
	}

	@Test
	void shouldPutInPauseThenResume() {
		this.contextRunner.run((context) -> {
			Assertions.assertThat(context).hasSingleBean(Scheduler.class);
			Scheduler scheduler = context.getBean(Scheduler.class);
			SimpleTrigger simpleTrigger = this.createTestJob(context);

			// In pause
			Assertions.assertThat(context).hasSingleBean(QuartzTriggerEndpoint.class);
			QuartzTriggerEndpoint quartzJobEndpoint = context.getBean(QuartzTriggerEndpoint.class);
			Assertions.assertThat(quartzJobEndpoint.pauseOrResumeTrigger(TRIGGER_GROUP_NAME, TRIGGER_NAME)).isNotNull();
			Assertions.assertThat(scheduler.getTriggerState(simpleTrigger.getKey())).isEqualTo(TriggerState.PAUSED);

			// Resume
			Assertions.assertThat(quartzJobEndpoint.pauseOrResumeTrigger(TRIGGER_GROUP_NAME, TRIGGER_NAME)).isNotNull();
			Assertions.assertThat(scheduler.getTriggerState(simpleTrigger.getKey())).isNotEqualTo(TriggerState.PAUSED);
		});
	}

	@Test
	void shouldDelete() {
		this.contextRunner.run((context) -> {
			Assertions.assertThat(context).hasSingleBean(Scheduler.class);
			Scheduler scheduler = context.getBean(Scheduler.class);
			this.createTestJob(context);

			Assertions.assertThat(context).hasSingleBean(QuartzTriggerEndpoint.class);
			QuartzTriggerEndpoint quartzJobEndpoint = context.getBean(QuartzTriggerEndpoint.class);
			quartzJobEndpoint.deleteTrigger(TRIGGER_GROUP_NAME, TRIGGER_NAME);

			JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(JOB_NAME, JOB_GROUP_NAME));
			Assertions.assertThat(jobDetail).isNull();
		});
	}

	@Override
	public Class getAutoConfigurationForEndpoint() {
		return QuartzTriggerEndpointAutoConfiguration.class;
	}

	@AutoConfigureAfter(QuartzAutoConfiguration.class)
	public static class QuartzTriggerEndpointAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public QuartzTriggerEndpoint quartzEndpoint(ObjectProvider<Scheduler> scheduler) {
			return new QuartzTriggerEndpoint(scheduler.getIfAvailable());

		}

	}

}
