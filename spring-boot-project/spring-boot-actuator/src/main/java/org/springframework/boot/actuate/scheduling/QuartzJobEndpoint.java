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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.util.Assert;

/**
 * Web {@link Endpoint} that provides access to the {@link Scheduler} {@link Job}.
 *
 * @author Jordan Couret
 * @since 2.2.0
 */
@Endpoint(id = "quartzjob")
public class QuartzJobEndpoint {

	private final Scheduler scheduler;

	public QuartzJobEndpoint(Scheduler scheduler) {
		Assert.notNull(scheduler, "Scheduler must not be null");
		this.scheduler = scheduler;
	}

	@ReadOperation
	public Set<JobKey> findAll() throws SchedulerException {
		return this.scheduler.getJobGroupNames().stream().map((groupName) -> {
			try {
				return this.scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
			}
			catch (SchedulerException ignored) {
				// Do nothing
				return null;
			}
		}).filter(Objects::nonNull).flatMap(Set::stream).collect(Collectors.toSet());
	}

	@ReadOperation
	public Set<JobKey> findByGroup(@Selector String group) throws SchedulerException {
		return this.scheduler.getJobKeys(GroupMatcher.groupEquals(group));
	}

	@ReadOperation
	public JobDescriptor findByGroupAndName(@Selector String group, @Selector String name) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(name, group);
		JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
		if (jobDetail != null) {
			List<? extends Trigger> triggersOfJob = this.scheduler.getTriggersOfJob(jobKey);
			return new JobDescriptor(jobDetail, triggersOfJob);
		}
		return null;
	}

	@WriteOperation
	public JobDescriptor pauseOrResumeJob(@Selector String group, @Selector String name) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(name, group);
		JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
		// If job exist
		if (jobDetail != null) {
			List<? extends Trigger> triggersOfJob = this.scheduler.getTriggersOfJob(jobKey);
			boolean allTriggersInPause = triggersOfJob.stream().map(Trigger::getKey).map(getGetTriggerState())
					.filter(Objects::nonNull).allMatch(TriggerState.PAUSED::equals);

			if (allTriggersInPause) {
				this.scheduler.resumeJob(jobKey);
			}
			else {
				this.scheduler.pauseJob(jobKey);
			}
			return new JobDescriptor(jobDetail, triggersOfJob);
		}
		return null;
	}

	@DeleteOperation
	public void deleteJob(@Selector String group, @Selector String name) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(name, group);
		JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
		// If job exist
		if (jobDetail != null) {
			this.scheduler.deleteJob(jobKey);
		}
	}

	private Function<TriggerKey, TriggerState> getGetTriggerState() {
		return (triggerKey) -> {
			try {
				return this.scheduler.getTriggerState(triggerKey);
			}
			catch (SchedulerException ignored) {
				return null;
			}
		};
	}

	public static final class JobDescriptor {

		private JobKey key;

		private String description;

		private Class<? extends Job> jobClass;

		private JobDataMap data;

		private List<TriggerDesciptor> triggers;

		JobDescriptor(JobDetail jobDetail, List<? extends Trigger> triggers) {
			this.key = jobDetail.getKey();
			this.description = jobDetail.getDescription();
			this.jobClass = jobDetail.getJobClass();
			this.data = jobDetail.getJobDataMap();
			this.triggers = new ArrayList<>();
			triggers.stream().map(TriggerDesciptor::new).forEach(this.triggers::add);
		}

		public JobKey getKey() {
			return this.key;
		}

		public String getDescription() {
			return this.description;
		}

		public Class<? extends Job> getJobClass() {
			return this.jobClass;
		}

		public JobDataMap getData() {
			return this.data;
		}

		public List<TriggerDesciptor> getTriggers() {
			return this.triggers;
		}

	}

	public static final class TriggerDesciptor {

		private final LocalDateTime date;

		TriggerDesciptor(Trigger trigger) {
			this.date = trigger.getNextFireTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		}

		public LocalDateTime getDate() {
			return this.date;
		}

	}

}
