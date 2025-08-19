/** */
package com.strandls.utility.dao;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Abhishek Rudra
 */
public class UtilityDaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(FlagDao.class).in(Scopes.SINGLETON);
		bind(TagsDao.class).in(Scopes.SINGLETON);
		bind(TagLinksDao.class).in(Scopes.SINGLETON);
		bind(LanguageDao.class).in(Scopes.SINGLETON);
		bind(HabitatDao.class).in(Scopes.SINGLETON);
		bind(HomePageStatsDao.class).in(Scopes.SINGLETON);
		bind(GallerySliderDao.class).in(Scopes.SINGLETON);
		bind(HomePageDao.class).in(Scopes.SINGLETON);
	}
}
