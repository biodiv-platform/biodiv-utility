package com.strandls.utility.service.impl;

import javax.inject.Inject;

import com.strandls.utility.dao.LanguageDao;
import com.strandls.utility.pojo.Language;
import com.strandls.utility.service.LanguageService;

public class LanguageServiceImpl implements LanguageService {

	@Inject
	private LanguageDao languageDao;

	@Override
	public Language getLanguage(String codeType, String code) {
		Language lang = languageDao.findByPropertyWithCondition(codeType, code, "=");
		if (lang == null) {
			return getCurrentLanguage();
		}
		return lang;
	}

	@Override
	public Language getLanguageByTwoLetterCode(String language) {
		Language lang = languageDao.findByPropertyWithCondition("twoLetterCode", language, "=");
		if (lang == null) {
			return getCurrentLanguage();
		}
		return lang;
	}

	private Language getCurrentLanguage() {
		return languageDao.findByPropertyWithCondition("name", Language.DEFAULT_LANGUAGE, "=");
	}

	@Override
	public Language save(Language language) {
		return languageDao.save(language);
	}

	@Override
	public Language updateName(Long id, String name) {
		Language language = languageDao.findById(id);
		language.setName(name);
		return languageDao.update(language);
	}

	@Override
	public Language getLanguageById(Long languageId) {
		Language result = languageDao.findById(languageId);
		return result;
	}

}
