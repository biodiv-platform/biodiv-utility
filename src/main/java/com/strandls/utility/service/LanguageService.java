package com.strandls.utility.service;

import java.util.List;

import com.strandls.utility.pojo.Language;

/**
 * @author vilay
 */
public interface LanguageService {

	public Language getLanguage(String codeType, String code);

	public Language getLanguageByTwoLetterCode(String language);

	public Language save(Language language);

	public Language updateName(Long id, String name);

	public Language getLanguageById(Long languageId);

	public List<Language> getLanguagesWithFieldHeaders();
}
