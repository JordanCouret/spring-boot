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

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.assertj.core.api.Assertions;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Default context for the quartz test with the creation of a job in the scheduler
 *
 * @author Jordan Couret
 * @since 2.2.0
 */
public abstract class AbstractQuartzJobEndpointTests {

	final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(QuartzAutoConfiguration.class, getAutoConfigurationForEndpoint()));

	final String JOB_NAME = "JOB_NAME";

	final String JOB_GROUP_NAME = "JOB_GROUP_NAME";

	final String TRIGGER_NAME = "TRIGGER_NAME";

	final String TRIGGER_GROUP_NAME = "TRIGGER_GROUP_NAME";

	/**
	 * Provide configuration to create endpoint in testing
	 */
	abstract Class getAutoConfigurationForEndpoint();

	/**
	 * Create default job with JOB_NAME and JOB_GROUP_NAME
	 */
	final SimpleTrigger createTestJob(AssertableApplicationContext context) throws SchedulerException {
		Assertions.assertThat(context).hasSingleBean(Scheduler.class);
		Scheduler scheduler = context.getBean(Scheduler.class);
		return this.createJob(scheduler);
	}

	private SimpleTrigger createJob(Scheduler scheduler) throws SchedulerException {
		JobDataMap map = new JobDataMap();
		map.put("name", this.JOB_NAME);
		JobDetail jobDetail = JobBuilder.newJob(SampleJob.class).withIdentity(this.JOB_NAME, this.JOB_GROUP_NAME)
				.usingJobData(map).build();

		java.util.Date start = Date
				.from(LocalDateTime.now().plusSeconds(15).atZone(ZoneId.systemDefault()).toInstant());

		SimpleTrigger sampleTrigger = TriggerBuilder.newTrigger().forJob(jobDetail)
				.withIdentity(this.TRIGGER_NAME, this.TRIGGER_GROUP_NAME)
				.withSchedule(SimpleScheduleBuilder.simpleSchedule()).startAt(start).build();

		scheduler.scheduleJob(jobDetail, sampleTrigger);
		return sampleTrigger;
	}

	/**
	 * Job without actions
	 */
	public static class SampleJob implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			// Do nothing
		}

	}

}
