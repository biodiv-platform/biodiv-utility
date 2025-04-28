package com.strandls.utility.service.impl;

import java.util.List;

import javax.inject.Inject;

import com.strandls.utility.dao.LanguageDao;
import com.strandls.utility.pojo.Language;
import com.strandls.utility.service.LanguageService;

public class LanguageServiceImpl implements LanguageService {

	@Inject
	private LanguageDao languageDao;

	@Override
	public Language getLanguage(String codeType, String code) {
		Language language = languageDao.getLanguageByProperty(codeType, code, "=");
		if (language == null) {
			return getCurrentLanguage();
		}
		return language;
	}

	@Override
	public Language getLanguageByTwoLetterCode(String language) {
		Language twoletterCodelang = languageDao.getLanguageByProperty("twoLetterCode", language, "=");
		if (twoletterCodelang == null) {
			return getCurrentLanguage();
		}
		return twoletterCodelang;
	}

	private Language getCurrentLanguage() {
		return languageDao.getLanguageByProperty("name", Language.DEFAULT_LANGUAGE, "=");
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
		return languageDao.findById(languageId);
	}
	
	@Override
	public List<Language> getLanguagesWithFieldHeaders() {
	    return languageDao.getLanguagesWithFieldHeaders();
	}

}
