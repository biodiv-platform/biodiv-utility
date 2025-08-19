/** */
package com.strandls.utility.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.activity.controller.ActivityServiceApi;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.DocumentActivityLogging;
import com.strandls.activity.pojo.MailData;
import com.strandls.utility.Headers;

import jakarta.inject.Inject;

/**
 * @author Abhishek Rudra
 */
public class LogActivities {

	private final Logger logger = LoggerFactory.getLogger(LogActivities.class);

	@Inject
	private ActivityServiceApi activityService;

	@Inject
	private Headers headers;

	public void logActivity(String authHeader, String activityDescription, Long rootObjectId, Long subRootObjectId,
			String rootObjectType, Long activityId, String activityType, MailData mailData) {

		try {
			ActivityLoggingData activityLogging = new ActivityLoggingData();
			activityLogging.setActivityDescription(activityDescription);
			activityLogging.setActivityId(activityId);
			activityLogging.setActivityType(activityType);
			activityLogging.setRootObjectId(rootObjectId);
			activityLogging.setRootObjectType(rootObjectType);
			activityLogging.setSubRootObjectId(subRootObjectId);
			activityLogging.setMailData(mailData);
			activityService = headers.addActivityHeader(activityService, authHeader);
			activityService.logActivity(activityLogging);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void logDocumentActivities(String authHeader, String activityDescription, Long rootObjectId,
			Long subRootObjectId, String rootObjectType, Long activityId, String activityType, MailData mailData) {
		try {

			DocumentActivityLogging loggingData = new DocumentActivityLogging();
			loggingData.setActivityDescription(activityDescription);
			loggingData.setActivityId(activityId);
			loggingData.setActivityType(activityType);
			loggingData.setRootObjectId(rootObjectId);
			loggingData.setRootObjectType(rootObjectType);
			loggingData.setSubRootObjectId(subRootObjectId);
			loggingData.setMailData(mailData);

			activityService = headers.addActivityHeader(activityService, authHeader);
			activityService.logDocumentActivity(loggingData);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
