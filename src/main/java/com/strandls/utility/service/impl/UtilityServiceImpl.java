/** */
package com.strandls.utility.service.impl;

import java.util.function.Function;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.activity.pojo.MailData;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.User;
import com.strandls.user.pojo.UserIbp;
import com.strandls.utility.dao.FlagDao;
import com.strandls.utility.dao.GalleryConfigDao;
import com.strandls.utility.dao.GallerySliderDao;
import com.strandls.utility.dao.HabitatDao;
import com.strandls.utility.dao.HomePageDao;
import com.strandls.utility.dao.HomePageStatsDao;
import com.strandls.utility.dao.LanguageDao;
import com.strandls.utility.dao.MiniGallerySliderDao;
import com.strandls.utility.dao.TagLinksDao;
import com.strandls.utility.dao.TagsDao;
import com.strandls.utility.pojo.Flag;
import com.strandls.utility.pojo.FlagCreateData;
import com.strandls.utility.pojo.FlagIbp;
import com.strandls.utility.pojo.FlagShow;
import com.strandls.utility.pojo.GalleryConfig;
import com.strandls.utility.pojo.GallerySlider;
import com.strandls.utility.pojo.Habitat;
import com.strandls.utility.pojo.HomePageData;
import com.strandls.utility.pojo.HomePageStats;
import com.strandls.utility.pojo.Language;
import com.strandls.utility.pojo.MiniGallerySlider;
import com.strandls.utility.pojo.ParsedName;
import com.strandls.utility.pojo.ReorderHomePage;
import com.strandls.utility.pojo.TagLinks;
import com.strandls.utility.pojo.Tags;
import com.strandls.utility.pojo.TagsMapping;
import com.strandls.utility.pojo.TagsMappingData;
import com.strandls.utility.pojo.Translation;
import com.strandls.utility.service.UtilityService;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import net.minidev.json.JSONArray;

/**
 * @author Abhishek Rudra
 */
public class UtilityServiceImpl implements UtilityService {

	private static final Logger logger = LoggerFactory.getLogger(UtilityServiceImpl.class);

	private final CloseableHttpClient httpClient = HttpClients.createDefault();

	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	private static final String ROLES = "roles";

	@Inject
	private LogActivities logActivity;

	@Inject
	private UserServiceApi userService;

	@Inject
	private FlagDao flagDao;

	@Inject
	private TagLinksDao tagLinkDao;

	@Inject
	private TagsDao tagsDao;

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private LanguageDao languageDao;

	@Inject
	private HomePageStatsDao portalStatusDao;

	@Inject
	private GallerySliderDao gallerySliderDao;

	@Inject
	private MiniGallerySliderDao miniGallerySliderDao;

	@Inject
	private GalleryConfigDao galleryConfigDao;

	@Inject
	private HabitatDao habitatDao;

	@Inject
	private HomePageDao homePageDao;

	@Override
	public Flag fetchByFlagId(Long id) {
		Flag flag = flagDao.findById(id);
		return flag;
	}

	@Override
	public FlagIbp fetchByFlagIdIbp(Long id) {
		Flag flag = flagDao.findById(id);
		if (flag == null)
			return null;
		FlagIbp ibp = new FlagIbp(flag.getFlag(), flag.getNotes());
		return ibp;
	}

	@Override
	public List<FlagShow> fetchByFlagObject(String objectType, Long objectId) {
		try {
			if (objectType.equalsIgnoreCase("observation"))
				objectType = "species.participation.Observation";
			List<Flag> flagList = flagDao.findByObjectId(objectType, objectId);
			List<FlagShow> flagShow = new ArrayList<FlagShow>();
			for (Flag flag : flagList) {
				flagShow.add(new FlagShow(flag, userService.getUserIbp(flag.getAuthorId().toString())));
			}
			return flagShow;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public List<Flag> fetchFlagByUserId(Long id) {
		List<Flag> flags = flagDao.findByUserId(id);
		return flags;
	}

	@Override
	public List<FlagShow> createFlag(HttpServletRequest request, String type, Long userId, Long objectId,
			FlagCreateData flagCreateData) {
		if (type.equalsIgnoreCase("observation"))
			type = "species.participation.Observation";
		else if (type.equalsIgnoreCase("document"))
			type = "content.eml.Document";

		FlagIbp flagIbp = flagCreateData.getFlagIbp();
		Flag flag = flagDao.findByObjectIdUserId(objectId, userId, type);
		if (flag == null) {
			flag = new Flag(null, userId, new Date(), flagIbp.getFlag(), flagIbp.getNotes(), objectId, type);
			flag = flagDao.save(flag);
			String description = flag.getFlag() + ":" + flag.getNotes();

			if (type.equalsIgnoreCase("species.participation.Observation")) {
				logActivity.logActivity(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId, objectId,
						"observaiton", flag.getId(), "Flagged", flagCreateData.getMailData());

			} else if (type.equalsIgnoreCase("content.eml.Document")) {
				logActivity.logDocumentActivities(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId,
						objectId, "document", flag.getId(), "Flagged", flagCreateData.getMailData());
			}

			List<FlagShow> flagList = fetchByFlagObject(type, objectId);
			return flagList;
		}
		return null;
	}

	@Override
	public List<FlagShow> removeFlag(HttpServletRequest request, CommonProfile profile, String type, Long objectId,
			Long flagId, MailData mailData) {

		if (type.equalsIgnoreCase("observation"))
			type = "species.participation.Observation";
		else if (type.equalsIgnoreCase("document"))
			type = "content.eml.Document";
		Flag flagged = flagDao.findById(flagId);

		JSONArray userRole = (JSONArray) profile.getAttribute(ROLES);
		Long userId = Long.parseLong(profile.getId());

		if (flagged != null) {
			if (userRole.contains(ROLE_ADMIN) || userId.equals(flagged.getAuthorId())) {

				flagDao.delete(flagged);
				String description = flagged.getFlag() + ":" + flagged.getNotes();

				if (type.equalsIgnoreCase("species.participation.Observation")) {
					logActivity.logActivity(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId,
							objectId, "observaiton", flagged.getId(), "Flag removed", mailData);

				} else if (type.equalsIgnoreCase("content.eml.Document")) {
					logActivity.logDocumentActivities(request.getHeader(HttpHeaders.AUTHORIZATION), description,
							objectId, objectId, "document", flagged.getId(), "Flag removed", mailData);
				}

				List<FlagShow> flagList = fetchByFlagObject(type, objectId);
				return flagList;
			}
		}
		return null;
	}

	@Override
	public List<Tags> fetchTags(String objectType, Long id) {
		List<TagLinks> tagList = tagLinkDao.findObjectTags(objectType, id);
		List<Tags> tags = new ArrayList<Tags>();
		for (TagLinks tag : tagList) {
			tags.add(tagsDao.findById(tag.getTagId()));
		}
		return tags;
	}

	@Override
	public List<String> createTagsMapping(HttpServletRequest request, String objectType,
			TagsMappingData tagsMappingData) {

		try {
			TagsMapping tagsMapping = tagsMappingData.getTagsMapping();
			Long objectId = tagsMapping.getObjectId();
			List<String> resultList = new ArrayList<String>();
			List<String> errorList = new ArrayList<String>();
			String description = "";
			for (Tags tag : tagsMapping.getTags()) {
				TagLinks result = null;

				Tags tagsCheck = tagsDao.fetchByName(tag.getName());
				if (tagsCheck != null)
					tag = tagsCheck;
				if (tag.getId() == null) {

					Tags insertedTag = tagsDao.save(tag);
					description = description + insertedTag.getName() + ",";
					if (insertedTag.getId() != null) {
						TagLinks tagLink = new TagLinks(null, insertedTag.getId(), objectId, objectType);
						result = tagLinkDao.save(tagLink);
					}
				} else {
					Tags storedTag = tagsDao.findById(tag.getId());
					if (!(storedTag.getName().equals(tag.getName()))) {
						errorList.add("Mapping not proper for TagName and id Supplied for ID" + tag.getName() + " and "
								+ tag.getId());
					} else {
						description = description + storedTag.getName() + ",";
						TagLinks tagLink = new TagLinks(null, tag.getId(), objectId, objectType);
						result = tagLinkDao.save(tagLink);
					}
				}

				if (result != null && result.getId() != null)
					resultList.add(result.getId().toString());
			}
			if (!(errorList.isEmpty()))
				return errorList;
			description = description.substring(0, description.length() - 1);

			if (objectType.equals("observation"))
				logActivity.logActivity(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId, objectId,
						"observation", objectId, "Observation tag updated", tagsMappingData.getMailData());
			else if (objectType.equals("document"))
				logActivity.logDocumentActivities(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId,
						objectId, "document", objectId, "Document tag updated", tagsMappingData.getMailData());
			return resultList;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public ParsedName findParsedName(String scientificName) {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost").setPort(9091).setPath("/api/v1/" + scientificName)
				.setParameter("with_details", "true");

		List<ParsedName> parsedName = null;
		URI uri = null;

		try {
			uri = builder.build();
			HttpGet request = new HttpGet(uri);

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// return it as a String
					String result = EntityUtils.toString(entity);
					parsedName = Arrays.asList(objectMapper.readValue(result, ParsedName[].class));
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} catch (URISyntaxException e1) {
			logger.error(e1.getMessage());
		}

		if (parsedName != null) {
			return parsedName.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<Language> findAllLanguages(Boolean isDirty) {
		List<Language> result = null;
		if (isDirty != null)
			result = languageDao.findAll(isDirty);
		else
			result = languageDao.findAll();
		return result;
	}

	@Override
	public List<Tags> updateTags(HttpServletRequest request, String objectType, TagsMappingData tagsMappingData) {
		List<Tags> tags = new ArrayList<Tags>();

		try {
			String description = "";
			TagsMapping tagsMapping = tagsMappingData.getTagsMapping();
			Long objectId = tagsMapping.getObjectId();
			List<TagLinks> previousTags = tagLinkDao.findObjectTags(objectType, objectId);
			List<Tags> newTags = tagsMapping.getTags();
			// DELETE THE TAGS THAT ARE REMOVED
			for (TagLinks tagLinks : previousTags) {
				Tags tag = tagsDao.findById(tagLinks.getTagId());
				if (!(newTags.contains(tag))) {
					tagLinkDao.delete(tagLinks);
				}
			}
			// ADD OR CREATE THE NEW TAGS ADDED
			for (Tags tag : newTags) {

				if (tag.getId() != null) {
					TagLinks result = tagLinkDao.checkIfTagsLinked(objectType, objectId, tag.getId());
					if (result == null) {
						TagLinks tagLink = new TagLinks(null, tag.getId(), objectId, objectType);
						tagLinkDao.save(tagLink);
					}
				} else {
					tag = tagsDao.save(tag);
					TagLinks tagLink = new TagLinks(null, tag.getId(), objectId, objectType);
					tagLinkDao.save(tagLink);
				}
				description = description + tag.getName() + ",";
			}

			List<TagLinks> presentTags = tagLinkDao.findObjectTags(objectType, objectId);
			for (TagLinks tagLinks : presentTags) {
				tags.add(tagsDao.findById(tagLinks.getTagId()));
			}

			description = description.substring(0, description.length() - 1);

			if (objectType.equals("observation"))
				logActivity.logActivity(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId, objectId,
						"observation", objectId, "Observation tag updated", tagsMappingData.getMailData());
			else if (objectType.equals("document"))
				logActivity.logDocumentActivities(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId,
						objectId, "document", objectId, "Document tag updated", tagsMappingData.getMailData());

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return tags;
	}

	@Override
	public List<Tags> tagsAutoSugguest(String phrase) {
		List<Tags> result = tagsDao.fetchNameByLike(phrase);
		return result;
	}

	@Override
	public Language getLanguage(String codeType, String code) {
		Language lang = languageDao.getLanguageByProperty(codeType, code, "=");
		if (lang == null) {
			return getCurrentLanguage();
		}
		return lang;
	}

	@Override
	public Language getLanguageByTwoLetterCode(String language) {
		Language langTwoLetterCode = languageDao.getLanguageByProperty("twoLetterCode", language, "=");
		if (langTwoLetterCode == null) {
			return getCurrentLanguage();
		}
		return langTwoLetterCode;
	}

	private Language getCurrentLanguage() {
		return languageDao.getLanguageByProperty("name", Language.DEFAULT_LANGUAGE, "=");
	}

	@Override
	public HomePageData getHomePageData(HttpServletRequest request, Boolean adminList, Long languageId) {
		try {

			HomePageData result = null;
			Boolean isadmin = false;
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);

			if (profile != null) {
				JSONArray roles = (JSONArray) profile.getAttribute(ROLES);
				isadmin = roles.contains(ROLE_ADMIN);
			}

			List<GallerySlider> galleryData = isadmin && adminList
					? gallerySliderDao.getAllGallerySliderInfo(Boolean.TRUE)
					: gallerySliderDao.getAllGallerySliderInfo(Boolean.FALSE);

			List<GallerySlider> groupedBySliderId = groupGallerySliders(galleryData, languageId, isadmin && adminList);

			List<GalleryConfig> miniGalleryData = isadmin && adminList ? galleryConfigDao.getAllMiniSlider(true)
					: galleryConfigDao.getAllMiniSlider(false);
			List<Long> miniGalleryIds = new ArrayList<>();
			Map<Long, Integer> miniGalleryIndexMapping = new HashMap<>();
			List<GalleryConfig> miniGalleryConfig = new ArrayList<>();
			for (GalleryConfig miniGallery : miniGalleryData) {
				Long galleryId = miniGallery.getGalleryId();
				if (!miniGalleryIds.contains(galleryId)) {
					miniGalleryIds.add(galleryId);
					miniGalleryIndexMapping.put(galleryId, miniGalleryIndexMapping.size());
					List<MiniGallerySlider> miniSliders = isadmin && adminList
							? miniGallerySliderDao.getAllGallerySliderInfo(Boolean.TRUE, galleryId)
							: miniGallerySliderDao.getAllGallerySliderInfo(Boolean.FALSE, galleryId);
					miniGallery
							.setGallerySlider(groupMiniGallerySliders(miniSliders, languageId, isadmin && adminList));
					if (isadmin && adminList) {
						Translation translation = new Translation(miniGallery.getId(), miniGallery.getTitle(),
								miniGallery.getLanguageId(), null, null);
						miniGallery.setTranslations(Collections.singletonList(translation));
					}
					miniGalleryConfig.add(miniGallery);
				} else {
					int targetIndex = miniGalleryIndexMapping.get(galleryId);
					GalleryConfig targetGallery = miniGalleryConfig.get(targetIndex);

					if (isadmin && adminList) {
						// Ensure we have a mutable list
						List<Translation> translations = targetGallery.getTranslations() != null
								? new ArrayList<>(targetGallery.getTranslations())
								: new ArrayList<>();

						// Add new translation
						translations.add(new Translation(miniGallery.getId(), miniGallery.getTitle(),
								miniGallery.getLanguageId(), null, null));

						targetGallery.setTranslations(translations);
					} else if (miniGallery.getLanguageId().equals(languageId)) {
						targetGallery.setTitle(miniGallery.getTitle());
						targetGallery.setLanguageId(languageId);
					}
				}
			}

			HomePageStats homePageStats;
			// IBP home page DATA
			homePageStats = portalStatusDao.fetchPortalStats();

			result = homePageDao.findById(1L);
			result.setGallerySlider(groupedBySliderId);
			result.setMiniGallery(miniGalleryConfig);
			result.setStats(homePageStats);

			return result;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	private List<GallerySlider> groupGallerySliders(List<GallerySlider> galleryData, Long languageId, Boolean admin) {
		List<GallerySlider> gallerySlider = new ArrayList<>();
		List<Long> uniqueAuthorIds = galleryData.stream().map(GallerySlider::getAuthorId).filter(Objects::nonNull)
				.distinct().collect(Collectors.toList());
		try {
			List<User> users = userService.getUserBulk(uniqueAuthorIds);
			Map<String, User> userMap = users.stream()
					.collect(Collectors.toMap(user -> user.getId().toString(), Function.identity()));
			Map<Long, Integer> GalleryIndexMapping = new HashMap<>();
			for (GallerySlider gallery : galleryData) {
				Long galleryId = gallery.getSliderId();
				if (!GalleryIndexMapping.keySet().contains(galleryId)) {
					GalleryIndexMapping.put(galleryId, GalleryIndexMapping.size());
					if (gallery.getAuthorId() != null) {
						gallery.setAuthorImage(userMap.get(gallery.getAuthorId().toString()).getProfilePic());
						gallery.setAuthorName(userMap.get(gallery.getAuthorId().toString()).getName());
					}
					if (admin) {
						Translation translation = new Translation(gallery.getId(), gallery.getTitle(),
								gallery.getLanguageId(), gallery.getCustomDescripition(), gallery.getReadMoreText());
						gallery.setTranslations(Collections.singletonList(translation));
					}
					gallerySlider.add(gallery);
				} else {
					int targetIndex = GalleryIndexMapping.get(galleryId);
					GallerySlider targetGallery = gallerySlider.get(targetIndex);

					if (admin) { // Ensure we have a mutable list
						List<Translation> translations = targetGallery.getTranslations() != null
								? new ArrayList<>(targetGallery.getTranslations())
								: new ArrayList<>();

						translations.add(new Translation(gallery.getId(), gallery.getTitle(), gallery.getLanguageId(),
								gallery.getCustomDescripition(), gallery.getReadMoreText()));

						targetGallery.setTranslations(translations);
					} else if (gallery.getLanguageId().equals(languageId)) {
						gallerySlider.set(targetIndex, gallery);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while grouping gallery sliders", e);
		}
		return gallerySlider;
	}

	private List<MiniGallerySlider> groupMiniGallerySliders(List<MiniGallerySlider> sliders, Long languageId,
			Boolean admin) {
		List<MiniGallerySlider> gallerySlider = new ArrayList<>();
		List<Long> uniqueAuthorIds = sliders.stream().map(MiniGallerySlider::getAuthorId).filter(Objects::nonNull)
				.distinct().collect(Collectors.toList());
		try {
			List<User> users = userService.getUserBulk(uniqueAuthorIds);
			Map<String, User> userMap = users.stream()
					.collect(Collectors.toMap(user -> user.getId().toString(), Function.identity()));
			Map<Long, Integer> GalleryIndexMapping = new HashMap<>();
			for (MiniGallerySlider gallery : sliders) {
				Long galleryId = gallery.getSliderId();
				if (!GalleryIndexMapping.keySet().contains(galleryId)) {
					GalleryIndexMapping.put(galleryId, GalleryIndexMapping.size());
					if (gallery.getAuthorId() != null) {
						gallery.setAuthorImage(userMap.get(gallery.getAuthorId().toString()).getProfilePic());
						gallery.setAuthorName(userMap.get(gallery.getAuthorId().toString()).getName());
					}
					if (admin) {
						Translation translation = new Translation(gallery.getId(), gallery.getTitle(),
								gallery.getLanguageId(), gallery.getCustomDescripition(), gallery.getReadMoreText());
						gallery.setTranslations(Collections.singletonList(translation));
					}
					gallerySlider.add(gallery);
				} else {
					int targetIndex = GalleryIndexMapping.get(galleryId);
					MiniGallerySlider targetGallery = gallerySlider.get(targetIndex);

					if (admin) { // Ensure we have a mutable list
						List<Translation> translations = targetGallery.getTranslations() != null
								? new ArrayList<>(targetGallery.getTranslations())
								: new ArrayList<>();

						translations.add(new Translation(gallery.getId(), gallery.getTitle(), gallery.getLanguageId(),
								gallery.getCustomDescripition(), gallery.getReadMoreText()));

						targetGallery.setTranslations(translations);
					} else if (gallery.getLanguageId().equals(languageId)) {
						gallerySlider.set(targetIndex, gallery);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while grouping mini gallery sliders", e);
		}
		return gallerySlider;
	}

	@Override
	public HomePageData removeHomePage(HttpServletRequest request, Long galleryId) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute(ROLES);

			if (roles.contains(ROLE_ADMIN)) {
				List<GallerySlider> translations = gallerySliderDao.findBySliderId(galleryId);
				for (GallerySlider translation : translations) {
					gallerySliderDao.delete(translation);
				}
				return getHomePageData(request, true, (long) -1);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;
	}

	@Override
	public GalleryConfig createMiniGallery(HttpServletRequest request, GalleryConfig miniGalleryData) {
		try {
			List<Translation> translations = new ArrayList<>();
			Long galleryId = null;
			for (Translation translation : miniGalleryData.getTranslations()) {
				GalleryConfig miniGallery = new GalleryConfig();
				miniGallery.setSlidesPerView(miniGalleryData.getSlidesPerView());
				miniGallery.setIsVertical(miniGalleryData.getIsVertical());
				miniGallery.setTitle(translation.getTitle());
				miniGallery.setLanguageId(translation.getLanguageId());
				miniGallery.setIsActive(true);
				miniGallery.setId(null);

				// First save without galleryId to get the generated one
				if (galleryId == null) {
					miniGallery.setGalleryId(null);
					miniGallery = galleryConfigDao.save(miniGallery);
					galleryId = miniGallery.getId();
					miniGallery.setGalleryId(galleryId);
					miniGallery = galleryConfigDao.update(miniGallery);
					translations.add(new Translation(miniGallery.getId(), miniGallery.getTitle(),
							miniGallery.getLanguageId(), null, null));
				} else {
					miniGallery.setGalleryId(galleryId);
					miniGallery = galleryConfigDao.save(miniGallery); // just one save now
					translations.add(new Translation(miniGallery.getId(), miniGallery.getTitle(),
							miniGallery.getLanguageId(), null, null));
				}
				if (translation.getLanguageId().equals(miniGalleryData.getLanguageId())) {
					miniGalleryData.setTitle(translation.getTitle());
				}

			}
			miniGalleryData.setTranslations(translations);
			miniGalleryData.setIsActive(true);
			miniGalleryData.setGalleryId(galleryId);
			return miniGalleryData;
		} catch (Exception e) {
			logger.error("Failed to create mini gallery: {}", e.getMessage(), e);
			return null;
		}
	}

	@Override
	public GalleryConfig editMiniGallery(HttpServletRequest request, Long galleryId, GalleryConfig miniGalleryData) {
		try {
			List<Translation> translations = new ArrayList<>();
			for (Translation translation : miniGalleryData.getTranslations()) {
				if (translation.getId() != null) {
					GalleryConfig miniGallery = galleryConfigDao.findById(translation.getId());
					miniGallery.setTitle(translation.getTitle());
					miniGallery.setSlidesPerView(miniGalleryData.getSlidesPerView());
					miniGallery.setIsVertical(miniGalleryData.getIsVertical());
					miniGallery.setIsActive(miniGalleryData.getIsActive());
					miniGallery = galleryConfigDao.update(miniGallery);
					translations.add(new Translation(miniGallery.getId(), miniGallery.getTitle(),
							miniGallery.getLanguageId(), null, null));
				} else {
					GalleryConfig miniGallery = new GalleryConfig();
					miniGallery.setId(null);
					miniGallery.setIsActive(miniGalleryData.getIsActive());
					miniGallery.setSlidesPerView(miniGalleryData.getSlidesPerView());
					miniGallery.setTitle(translation.getTitle());
					miniGallery.setIsVertical(miniGalleryData.getIsVertical());
					miniGallery.setLanguageId(translation.getLanguageId());
					miniGallery.setGalleryId(galleryId);
					miniGallery = galleryConfigDao.save(miniGallery);
					translations.add(new Translation(miniGallery.getId(), miniGallery.getTitle(),
							miniGallery.getLanguageId(), null, null));
				}
				if (translation.getLanguageId().equals(miniGalleryData.getLanguageId())) {
					miniGalleryData.setTitle(translation.getTitle());
				}
			}
			miniGalleryData.setTranslations(translations);
			return miniGalleryData;

		} catch (Exception e) {
			logger.error("Failed to edit mini gallery with ID {}: {}", galleryId, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public Boolean removeMiniGallery(HttpServletRequest request, Long galleryId) {
		try {
			List<GalleryConfig> miniGallery = galleryConfigDao.getByGalleryId(galleryId);
			if (miniGallery == null) {
				logger.warn("Mini gallery with ID {} not found for deletion.", galleryId);
				return false;
			}
			for (GalleryConfig translation : miniGallery) {
				galleryConfigDao.delete(translation);
			}
			List<MiniGallerySlider> miniGallerySlides = miniGallerySliderDao.getAllGallerySliderInfo(true, galleryId);
			for (MiniGallerySlider slide : miniGallerySlides) {
				miniGallerySliderDao.delete(slide);
			}
			return true;
		} catch (Exception e) {
			logger.error("Error while deleting mini gallery with ID {}: {}", galleryId, e.getMessage(), e);
			return false;
		}
	}

	@Override
	public HomePageData editHomePage(HttpServletRequest request, Long galleryId, GallerySlider editData) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute(ROLES);
			if (roles.contains(ROLE_ADMIN)) {
				for (Translation translation : editData.getTranslations()) {
					if (translation.getId() != null) {
						GallerySlider gallerySliderEntity = gallerySliderDao.findById(translation.getId());
						gallerySliderEntity.setFileName(editData.getFileName());
						gallerySliderEntity.setTitle(translation.getTitle());
						gallerySliderEntity.setCustomDescripition(translation.getDescription());
						gallerySliderEntity.setMoreLinks(editData.getMoreLinks());
						gallerySliderEntity.setDisplayOrder(editData.getDisplayOrder());
						gallerySliderEntity.setTruncated(editData.getTruncated());
						gallerySliderEntity.setReadMoreText(translation.getReadMoreText());
						gallerySliderEntity.setReadMoreUIType(editData.getReadMoreUIType());
						gallerySliderEntity.setGallerySidebar(editData.getGallerySidebar());
						gallerySliderEntity.setLanguageId(translation.getLanguageId());
						gallerySliderEntity.setSliderId(galleryId);

						gallerySliderDao.update(gallerySliderEntity);
					} else {
						GallerySlider gallerySliderEntity = new GallerySlider();
						gallerySliderEntity.setId(null);
						gallerySliderEntity.setAuthorId(editData.getAuthorId());
						gallerySliderEntity.setCustomDescripition(translation.getDescription());
						gallerySliderEntity.setFileName(editData.getFileName());
						gallerySliderEntity.setMoreLinks(editData.getMoreLinks());
						gallerySliderEntity.setObservationId(editData.getObservationId());
						gallerySliderEntity.setTitle(translation.getTitle());
						gallerySliderEntity.setDisplayOrder(editData.getDisplayOrder());
						gallerySliderEntity.setTruncated(editData.getTruncated());
						gallerySliderEntity.setReadMoreText(translation.getReadMoreText());
						gallerySliderEntity.setGallerySidebar(editData.getGallerySidebar());
						gallerySliderEntity.setReadMoreUIType(editData.getReadMoreUIType());
						gallerySliderEntity.setLanguageId(translation.getLanguageId());
						gallerySliderEntity.setSliderId(galleryId);
						gallerySliderDao.save(gallerySliderEntity);
					}
				}
				return getHomePageData(request, true, (long) -1);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;
	}

	@Override
	public HomePageData editMiniHomePage(HttpServletRequest request, Long galleryId, MiniGallerySlider editData) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute(ROLES);
			if (roles.contains(ROLE_ADMIN)) {
				for (Translation translation : editData.getTranslations()) {
					if (translation.getId() != null) {
						MiniGallerySlider gallerySliderEntity = miniGallerySliderDao.findById(translation.getId());
						gallerySliderEntity.setFileName(editData.getFileName());
						gallerySliderEntity.setTitle(translation.getTitle());
						gallerySliderEntity.setCustomDescripition(translation.getDescription());
						gallerySliderEntity.setMoreLinks(editData.getMoreLinks());
						gallerySliderEntity.setDisplayOrder(editData.getDisplayOrder());
						gallerySliderEntity.setTruncated(editData.getTruncated());
						gallerySliderEntity.setReadMoreText(translation.getReadMoreText());
						gallerySliderEntity.setReadMoreUIType(editData.getReadMoreUIType());
						gallerySliderEntity.setLanguageId(translation.getLanguageId());
						gallerySliderEntity.setSliderId(galleryId);
						gallerySliderEntity.setColor(editData.getColor());
						gallerySliderEntity.setBgColor(editData.getBgColor());

						miniGallerySliderDao.update(gallerySliderEntity);
					} else {
						MiniGallerySlider gallerySliderEntity = new MiniGallerySlider();
						gallerySliderEntity.setId(null);
						gallerySliderEntity.setAuthorId(editData.getAuthorId());
						gallerySliderEntity.setCustomDescripition(translation.getDescription());
						gallerySliderEntity.setFileName(editData.getFileName());
						gallerySliderEntity.setMoreLinks(editData.getMoreLinks());
						gallerySliderEntity.setObservationId(editData.getObservationId());
						gallerySliderEntity.setTitle(translation.getTitle());
						gallerySliderEntity.setDisplayOrder(editData.getDisplayOrder());
						gallerySliderEntity.setTruncated(editData.getTruncated());
						gallerySliderEntity.setReadMoreText(translation.getReadMoreText());
						gallerySliderEntity.setReadMoreUIType(editData.getReadMoreUIType());
						gallerySliderEntity.setLanguageId(translation.getLanguageId());
						gallerySliderEntity.setSliderId(galleryId);
						gallerySliderEntity.setGalleryId(editData.getGalleryId());
						gallerySliderEntity.setColor(editData.getColor());
						gallerySliderEntity.setBgColor(editData.getBgColor());
						miniGallerySliderDao.save(gallerySliderEntity);
					}
				}
				return getHomePageData(request, true, (long) -1);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;
	}

	@Override
	public HomePageData removeMiniHomePage(HttpServletRequest request, Long galleryId) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute(ROLES);

			if (roles.contains(ROLE_ADMIN)) {
				List<MiniGallerySlider> translations = miniGallerySliderDao.findBySliderId(galleryId);
				for (MiniGallerySlider translation : translations) {
					miniGallerySliderDao.delete(translation);
				}
				return getHomePageData(request, true, (long) -1);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;
	}

	@Override
	public HomePageData editHomePageData(HttpServletRequest request, HomePageData editData) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute(ROLES);
			if (roles.contains(ROLE_ADMIN)) {

				HomePageData homePageDataEntity = homePageDao.findById(1L);
				homePageDataEntity.setShowStats(editData.getShowStats());
				homePageDataEntity.setShowRecentObservation(editData.getShowRecentObservation());
				homePageDataEntity.setShowPartners(editData.getShowPartners());
				homePageDataEntity.setShowSponsors(editData.getShowSponsors());
				homePageDataEntity.setShowDonors(editData.getShowDonors());
				homePageDataEntity.setShowGridMap(editData.getShowGridMap());
				homePageDataEntity.setShowGallery(editData.getShowGallery());
				homePageDataEntity.setShowDesc(editData.getShowDesc());
				homePageDataEntity.setDescription(editData.getDescription());
				homePageDao.update(homePageDataEntity);
				return getHomePageData(request, true, (long) -1);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;
	}

	@Override
	public String getYoutubeTitle(String videoId) {

		URIBuilder builder = new URIBuilder();
		builder.setScheme("https").setHost("www.youtube.com").setPath("/oembed").setParameter("url",
				"https://www.youtube.com/watch?v=" + videoId);

		URI uri = null;
		try {
			uri = builder.build();
			HttpGet request = new HttpGet(uri);

			try (CloseableHttpResponse response = httpClient.execute(request)) {

				HttpEntity entity = response.getEntity();

				if (entity != null) {
					String result = EntityUtils.toString(entity);
					Map<String, Object> resultMap = objectMapper.readValue(result,
							new TypeReference<Map<String, Object>>() {
							});

					String title = resultMap.get("title").toString();
					return title;
				}

			} catch (Exception e) {
				logger.error(e.getMessage());
			}

		} catch (URISyntaxException e1) {
			logger.error(e1.getMessage());
		}

		return null;
	}

	@Override
	public List<Habitat> fetchAllHabitat() {
		List<Habitat> result = habitatDao.findAllHabitat();
		return result;
	}

	@Override
	public HomePageData insertHomePage(HttpServletRequest request, HomePageData editData) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute(ROLES);

			if (roles != null && roles.contains(ROLE_ADMIN)) {
				editHomePageData(request, editData);

				// Save GallerySlider
				List<GallerySlider> galleryData = editData.getGallerySlider();
				if (galleryData != null && !galleryData.isEmpty() && galleryData.size() > 0) {
					galleryData.forEach(languageMap -> saveGallerySliderTranslations(languageMap));
				}

				// Save MiniGallerySlider
				for (GalleryConfig miniGallery : editData.getMiniGallery()) {
					if (miniGallery.getGallerySlider() != null && !miniGallery.getGallerySlider().isEmpty()
							&& miniGallery.getGallerySlider().size() > 0) {
						miniGallery.getGallerySlider()
								.forEach(languageMap -> saveMiniGallerySliderTranslations(languageMap));
					}
				}

				return getHomePageData(request, true, (long) -1);
			}
		} catch (Exception e) {
			logger.error("Error inserting homepage: ", e);
		}

		return null;
	}

	private void saveGallerySliderTranslations(GallerySlider translations) {
		Long sliderId = null;
		for (Translation translation : translations.getTranslations()) {
			GallerySlider gallerySliderEntity = new GallerySlider();
			gallerySliderEntity.setId(null);
			gallerySliderEntity.setAuthorId(translations.getAuthorId());
			gallerySliderEntity.setCustomDescripition(translation.getDescription());
			gallerySliderEntity.setFileName(translations.getFileName());
			gallerySliderEntity.setMoreLinks(translations.getMoreLinks());
			gallerySliderEntity.setObservationId(translations.getObservationId());
			gallerySliderEntity.setTitle(translation.getTitle());
			gallerySliderEntity.setDisplayOrder(translations.getDisplayOrder());
			gallerySliderEntity.setTruncated(translations.getTruncated());
			gallerySliderEntity.setReadMoreText(translation.getReadMoreText());
			gallerySliderEntity.setGallerySidebar(translations.getGallerySidebar());
			gallerySliderEntity.setReadMoreUIType(translations.getReadMoreUIType());
			gallerySliderEntity.setLanguageId(translation.getLanguageId());

			if (sliderId != null) {
				gallerySliderEntity.setSliderId(sliderId);
			}

			gallerySliderEntity = gallerySliderDao.save(gallerySliderEntity);

			if (sliderId == null) {
				sliderId = gallerySliderEntity.getId();
				gallerySliderEntity.setSliderId(sliderId);
				gallerySliderDao.update(gallerySliderEntity);
			}
		}
	}

	private void saveMiniGallerySliderTranslations(MiniGallerySlider languageMap) {
		Long sliderId = null;
		for (Translation translation : languageMap.getTranslations()) {
			MiniGallerySlider gallerySliderEntity = new MiniGallerySlider();
			gallerySliderEntity.setId(null);
			gallerySliderEntity.setAuthorId(languageMap.getAuthorId());
			gallerySliderEntity.setCustomDescripition(translation.getDescription());
			gallerySliderEntity.setFileName(languageMap.getFileName());
			gallerySliderEntity.setMoreLinks(languageMap.getMoreLinks());
			gallerySliderEntity.setObservationId(languageMap.getObservationId());
			gallerySliderEntity.setTitle(translation.getTitle());
			gallerySliderEntity.setDisplayOrder(languageMap.getDisplayOrder());
			gallerySliderEntity.setTruncated(languageMap.getTruncated());
			gallerySliderEntity.setReadMoreText(translation.getReadMoreText());
			gallerySliderEntity.setReadMoreUIType(languageMap.getReadMoreUIType());
			gallerySliderEntity.setLanguageId(translation.getLanguageId());
			gallerySliderEntity.setGalleryId(languageMap.getGalleryId());
			gallerySliderEntity.setColor(languageMap.getColor());
			gallerySliderEntity.setBgColor(languageMap.getBgColor());

			if (sliderId != null) {
				gallerySliderEntity.setSliderId(sliderId);
			}

			gallerySliderEntity = miniGallerySliderDao.save(gallerySliderEntity);

			if (sliderId == null) {
				sliderId = gallerySliderEntity.getId();
				gallerySliderEntity.setSliderId(sliderId);
				miniGallerySliderDao.update(gallerySliderEntity);
			}
		}
	}

	@Override
	public HomePageData reorderHomePageSlider(HttpServletRequest request, List<ReorderHomePage> reorderHomePage) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute(ROLES);

			if (roles.contains(ROLE_ADMIN)) {
				for (ReorderHomePage reorder : reorderHomePage) {
					List<GallerySlider> gallery = gallerySliderDao.findBySliderId(reorder.getGalleryId());
					for (GallerySlider translation : gallery) {
						translation.setDisplayOrder(reorder.getDisplayOrder());
						gallerySliderDao.update(translation);
					}
				}

				return getHomePageData(request, true, (long) -1);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public HomePageData reorderMiniHomePageSlider(HttpServletRequest request, List<ReorderHomePage> reorderHomePage) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray roles = (JSONArray) profile.getAttribute(ROLES);

			if (roles.contains(ROLE_ADMIN)) {
				for (ReorderHomePage reorder : reorderHomePage) {
					List<MiniGallerySlider> gallery = miniGallerySliderDao.findBySliderId(reorder.getGalleryId());
					for (MiniGallerySlider translation : gallery) {
						translation.setDisplayOrder(reorder.getDisplayOrder());
						miniGallerySliderDao.update(translation);
					}
				}

				return getHomePageData(request, true, (long) -1);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public List<Long> getResourceIds(String phrase, String type, String tagRefId) {
		List<Long> resourceIds = new ArrayList<>();

		List<String> types = Arrays.asList(type.split(","));
		List<String> tagRefIds = Arrays.asList(tagRefId.split(","));
		List<String> phraseList = Arrays.asList(phrase.split(","));

		List<Tags> taglist = tagsDao.fetchTag(phraseList);

		List<Long> tagIdList = taglist.stream().map(Tags::getId).collect(Collectors.toList());

		if (tagIdList != null && !tagIdList.isEmpty()) {
			List<TagLinks> taglinks;
			if (types != null && !types.isEmpty() && !types.contains("all")) {
				taglinks = tagLinkDao.findResourceTags(types, tagIdList, null);
				resourceIds.addAll(getTagReferList(taglinks));

			} else if (tagRefIds != null && !tagRefIds.isEmpty() && !tagRefIds.contains("all")) {
				taglinks = tagLinkDao.findResourceTags(null, tagIdList, tagRefIds);
				resourceIds.addAll(getTagReferList(taglinks));
			} else {
				taglinks = tagLinkDao.findResourceTags(Collections.singletonList("all"), tagIdList, null);
				resourceIds.addAll(getTagReferList(taglinks));
			}
		}

		return resourceIds;
	}

	private List<Long> getTagReferList(List<TagLinks> taglinks) {
		List<Long> tagRefers = new ArrayList<>();
		for (TagLinks taglink : taglinks) {
			tagRefers.add(taglink.getTagRefer());
		}
		return tagRefers;
	}
}
