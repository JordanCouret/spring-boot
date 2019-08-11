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
import java.util.Objects;
import java.util.Set;
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
 * Web {@link Endpoint} that provides access to the {@link Scheduler} {@link Trigger}.
 *
 * @author Jordan Couret
 * @since 2.2.0
 */
@Endpoint(id = "quartztrigger")
public class QuartzTriggerEndpoint {

	private final Scheduler scheduler;

	public QuartzTriggerEndpoint(Scheduler scheduler) {
		Assert.notNull(scheduler, "Scheduler must not be null");
		this.scheduler = scheduler;
	}

	@ReadOperation
	public Set<TriggerKey> findAll() throws SchedulerException {
		return this.scheduler.getTriggerGroupNames().stream().map((groupName) -> {
			try {
				return this.scheduler.getTriggerKeys(GroupMatcher.groupEquals(groupName));
			}
			catch (SchedulerException ignored) {
				// Do nothing
				return null;
			}
		}).filter(Objects::nonNull).flatMap(Set::stream).collect(Collectors.toSet());
	}

	@ReadOperation
	public Set<TriggerKey> findByGroup(@Selector String group) throws SchedulerException {
		return this.scheduler.getTriggerKeys(GroupMatcher.groupEquals(group));
	}

	@ReadOperation
	public TriggerDesciptor findByGroupAndName(@Selector String group, @Selector String name)
			throws SchedulerException {
		TriggerKey triggerKey = TriggerKey.triggerKey(name, group);
		Trigger trigger = this.scheduler.getTrigger(triggerKey);
		if (trigger != null) {
			JobDetail jobDetail = this.scheduler.getJobDetail(trigger.getJobKey());
			TriggerState triggerState = this.scheduler.getTriggerState(trigger.getKey());
			if (jobDetail != null) {
				return new TriggerDesciptor(trigger, jobDetail, triggerState);
			}
		}
		return null;
	}

	@WriteOperation
	public TriggerDesciptor pauseOrResumeTrigger(@Selector String group, @Selector String name)
			throws SchedulerException {
		TriggerKey triggerKey = TriggerKey.triggerKey(name, group);
		Trigger trigger = this.scheduler.getTrigger(triggerKey);

		if (trigger != null) {
			boolean isInPauseMode = TriggerState.PAUSED.equals(this.scheduler.getTriggerState(trigger.getKey()));

			if (isInPauseMode) {
				this.scheduler.resumeTrigger(triggerKey);
			}
			else {
				this.scheduler.pauseTrigger(triggerKey);
			}

			JobDetail jobDetail = this.scheduler.getJobDetail(trigger.getJobKey());
			TriggerState triggerState = this.scheduler.getTriggerState(triggerKey);
			return new TriggerDesciptor(trigger, jobDetail, triggerState);
		}
		return null;
	}

	@DeleteOperation
	public void deleteTrigger(@Selector String group, @Selector String name) throws SchedulerException {
		TriggerKey triggerKey = TriggerKey.triggerKey(name, group);
		Trigger trigger = this.scheduler.getTrigger(triggerKey);
		if (trigger != null) {
			this.scheduler.deleteJob(trigger.getJobKey());
		}
	}

	public static final class JobDescriptor {

		private JobKey key;

		private String description;

		private Class<? extends Job> jobClass;

		private JobDataMap data;

		private JobDescriptor(JobDetail jobDetail) {
			this.key = jobDetail.getKey();
			this.description = jobDetail.getDescription();
			this.jobClass = jobDetail.getJobClass();
			this.data = jobDetail.getJobDataMap();
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

	}

	public static final class TriggerDesciptor {

		private String name;

		private String group;

		private final LocalDateTime date;

		private final TriggerState state;

		private final JobDescriptor jobDescriptor;

		private TriggerDesciptor(Trigger trigger, JobDetail jobDetail, TriggerState state) {
			this.name = trigger.getKey().getName();
			this.group = trigger.getKey().getGroup();
			this.date = trigger.getNextFireTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			this.jobDescriptor = new JobDescriptor(jobDetail);
			this.state = state;
		}

		public LocalDateTime getDate() {
			return this.date;
		}

		public TriggerState getState() {
			return this.state;
		}

		public JobDescriptor getJobDescriptor() {
			return this.jobDescriptor;
		}

		public String getName() {
			return this.name;
		}

		public String getGroup() {
			return this.group;
		}

	}

}
