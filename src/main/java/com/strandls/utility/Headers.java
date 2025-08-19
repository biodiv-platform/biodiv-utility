/** */
package com.strandls.utility;

import com.strandls.activity.controller.ActivityServiceApi;

import jakarta.ws.rs.core.HttpHeaders;

/**
 * @author Abhishek Rudra
 */
public class Headers {

	public ActivityServiceApi addActivityHeader(ActivityServiceApi activityService, String authHeader) {
		activityService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, authHeader);
		return activityService;
	}
}
