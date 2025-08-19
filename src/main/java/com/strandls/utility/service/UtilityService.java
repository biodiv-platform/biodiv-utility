/** */
package com.strandls.utility.service;

import java.util.List;

import org.pac4j.core.profile.CommonProfile;

import com.strandls.activity.pojo.MailData;
import com.strandls.utility.pojo.Flag;
import com.strandls.utility.pojo.FlagCreateData;
import com.strandls.utility.pojo.FlagIbp;
import com.strandls.utility.pojo.FlagShow;
import com.strandls.utility.pojo.GallerySlider;
import com.strandls.utility.pojo.Habitat;
import com.strandls.utility.pojo.HomePageData;
import com.strandls.utility.pojo.Language;
import com.strandls.utility.pojo.ParsedName;
import com.strandls.utility.pojo.ReorderHomePage;
import com.strandls.utility.pojo.Tags;
import com.strandls.utility.pojo.TagsMappingData;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Abhishek Rudra
 */
public interface UtilityService {

	public Flag fetchByFlagId(Long id);

	public FlagIbp fetchByFlagIdIbp(Long id);

	public List<FlagShow> fetchByFlagObject(String objectType, Long objectId);

	public List<Flag> fetchFlagByUserId(Long id);

	public List<FlagShow> createFlag(HttpServletRequest request, String type, Long userId, Long objectId,
			FlagCreateData flagCreateData);

	public List<FlagShow> removeFlag(HttpServletRequest request, CommonProfile profile, String type, Long objectId,
			Long flagId, MailData mailData);

	public List<Tags> fetchTags(String objectType, Long id);

	public List<String> createTagsMapping(HttpServletRequest request, String objectType,
			TagsMappingData tagsMappingData);

	public ParsedName findParsedName(String scientificName);

	public List<Language> findAllLanguages(Boolean isDirty);

	public List<Tags> updateTags(HttpServletRequest request, String objectType, TagsMappingData tagsMappingData);

	public List<Tags> tagsAutoSugguest(String phrase);

	public Language getLanguage(String codeType, String code);

	public Language getLanguageByTwoLetterCode(String language);

	public HomePageData getHomePageData(HttpServletRequest request, Boolean adminList);

	public String getYoutubeTitle(String videoId);

	public List<Habitat> fetchAllHabitat();

	public HomePageData removeHomePage(HttpServletRequest request, Long gId);

	public HomePageData editHomePage(HttpServletRequest request, Long galleryId, GallerySlider editData);

	public HomePageData editHomePageData(HttpServletRequest request, HomePageData editData);

	public HomePageData insertHomePage(HttpServletRequest request, HomePageData editData);

	public HomePageData reorderHomePageSlider(HttpServletRequest request, List<ReorderHomePage> reorderHomePage);

	public List<Long> getResourceIds(String phrase, String type, String tagRefId);
}
