/**
 * 
 */
package com.strandls.utility.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Abhishek Rudra
 *
 */
public class UtilityControllerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(UtilityController.class).in(Scopes.SINGLETON);
		bind(LanguageController.class).in(Scopes.SINGLETON);
	}

}
