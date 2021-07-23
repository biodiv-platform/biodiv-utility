/**
 * 
 */
package com.strandls.utility.service.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.strandls.utility.service.LanguageService;
import com.strandls.utility.service.UtilityService;

/**
 * @author Abhishek Rudra
 *
 */
public class UtilityServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(UtilityService.class).to(UtilityServiceImpl.class).in(Scopes.SINGLETON);
		bind(LogActivities.class).in(Scopes.SINGLETON);
		bind(LanguageService.class).to(LanguageServiceImpl.class).in(Scopes.SINGLETON);
	}
}
