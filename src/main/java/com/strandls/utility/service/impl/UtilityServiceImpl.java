/**
 * 
 */
package com.strandls.utility.service.impl;

import java.util.function.Function;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.activity.pojo.MailData;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.User;
import com.strandls.utility.dao.AnnouncementDao;
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
import com.strandls.utility.pojo.Announcement;
import com.strandls.utility.pojo.FieldData;
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
import com.strandls.utility.pojo.SpeciesDownload;
import com.strandls.utility.pojo.SpeciesField;
import com.strandls.utility.pojo.TagLinks;
import com.strandls.utility.pojo.Tags;
import com.strandls.utility.pojo.TagsMapping;
import com.strandls.utility.pojo.TagsMappingData;
import com.strandls.utility.pojo.Translation;
import com.strandls.utility.service.UtilityService;

import net.minidev.json.JSONArray;

/**
 * @author Abhishek Rudra
 *
 */
public class UtilityServiceImpl implements UtilityService {

	private static final Logger logger = LoggerFactory.getLogger(UtilityServiceImpl.class);
	String storageBasePath = null;

	private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
	private static final Color DARK_BLUE = new Color(30, 58, 138);
	private static final Color LIGHT_BLUE = new Color(239, 246, 255);
	private static final Color GRAY_50 = new Color(249, 250, 251);
	private static final Color GRAY_100 = new Color(243, 244, 246);
	private static final Color GRAY_200 = new Color(229, 231, 235);
	private static final Color GRAY_600 = new Color(75, 85, 99);
	private static final Color GRAY_700 = new Color(55, 65, 81);
	private static final Color GRAY_800 = new Color(31, 41, 55);
	private static final Color GREEN_50 = new Color(236, 253, 245);
	private static final Color GREEN_100 = new Color(209, 250, 229);
	private static final Color GREEN_700 = new Color(4, 120, 87);
	private static final Color GREEN_CIRCLE = new Color(16, 185, 129);
	private static final Color RED = new Color(255, 0, 0);
	private static final Color WHITE = Color.WHITE;
	private static final Color BLACK = Color.BLACK;

	// Page dimensions
	private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
	private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
	private static final float MARGIN = 30;
	private static final float CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);

	// Current Y position tracker
	private static float currentY;

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
	private AnnouncementDao announcementDao;

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
//			DELETE THE TAGS THAT ARE REMOVED
			for (TagLinks tagLinks : previousTags) {
				Tags tag = tagsDao.findById(tagLinks.getTagId());
				if (!(newTags.contains(tag))) {
					tagLinkDao.delete(tagLinks);
				}
			}
//			ADD OR CREATE THE NEW TAGS ADDED
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
//				IBP home page DATA
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

	@Override
	public Announcement createAnnouncement(HttpServletRequest request, Announcement announcementData) {
		try {
			Map<Long, String> translations = new HashMap<>();
			Long aId = null;
			for (Entry<Long, String> translation : announcementData.getTranslations().entrySet()) {
				Announcement announcement = new Announcement();
				announcement.setDescription(translation.getValue());
				announcement.setLanguageId(translation.getKey());
				announcement.setColor(announcementData.getColor());
				announcement.setBgColor(announcementData.getBgColor());
				announcement.setEnabled(announcementData.getEnabled());
				announcement.setId(null);

				// First save without galleryId to get the generated one
				if (aId == null) {
					announcement.setAnnouncementId(null);
					announcement = announcementDao.save(announcement);
					aId = announcement.getId();
					announcement.setAnnouncementId(aId);
					announcement = announcementDao.update(announcement);
					translations.put(translation.getKey(), translation.getValue());
				} else {
					announcement.setAnnouncementId(aId);
					announcement = announcementDao.save(announcement); // just one save now
					translations.put(translation.getKey(), translation.getValue());

				}
				if (translation.getKey().equals(announcement.getLanguageId())) {
					announcement.setDescription(translation.getValue());
				}

			}
			announcementData.setAnnouncementId(aId);
			return announcementData;
		} catch (Exception e) {
			logger.error("Failed to create announcement: {}", e.getMessage(), e);
			return null;
		}
	}

	@Override
	public List<Announcement> getAnnouncementData(HttpServletRequest request) {
		System.out.println("Hi");
		try {
			List<Announcement> announcementData = new ArrayList<>();
			Map<Long, Integer> announcementIndexMapping = new HashMap<>();
			List<Announcement> announcements = announcementDao.findAll();

			for (Announcement announcement : announcements) {
				Long aId = announcement.getAnnouncementId();
				if (!announcementIndexMapping.keySet().contains(aId)) {
					announcementIndexMapping.put(aId, announcementIndexMapping.size());
					Map<Long, String> translations = new HashMap<>();
					translations.put(announcement.getLanguageId(), announcement.getDescription());
					announcement.setTranslations(translations);
					announcementData.add(announcement);
				} else {
					int targetIndex = announcementIndexMapping.get(aId);
					Announcement targetAnnouncement = announcementData.get(targetIndex);

					Map<Long, String> translationsMap = targetAnnouncement.getTranslations();
					translationsMap.put(announcement.getLanguageId(), announcement.getDescription());

					targetAnnouncement.setTranslations(translationsMap);
				}
			}
			return announcementData;
		} catch (Exception e) {
			logger.error("Failed to get announcement Data: {}", e.getMessage(), e);
			return null;
		}
	}

	@Override
	public Boolean removeAnnouncement(HttpServletRequest request, Long aId) {
		try {
			List<Announcement> announcements = announcementDao.findByAnnouncemntId(aId);
			if (announcements == null) {
				logger.warn("Announcement with ID {} not found for deletion.", aId);
				return false;
			}
			for (Announcement translation : announcements) {
				announcementDao.delete(translation);
			}
			return true;
		} catch (Exception e) {
			logger.error("Error while deleting announcement with ID {}: {}", aId, e.getMessage(), e);
			return false;
		}
	}

	@Override
	public Announcement editAnnouncement(HttpServletRequest request, Long aId, Announcement announcementData) {
		try {
			Map<Long, String> translations = new HashMap<>();
			Map<Long, String> editTranslationsData = announcementData.getTranslations();
			List<Announcement> announcementTranslations = announcementDao.findByAnnouncemntId(aId);
			for (Announcement translation : announcementTranslations) {
					translation.setBgColor(announcementData.getBgColor());
					translation.setColor(announcementData.getColor());
					translation.setDescription(editTranslationsData.get(translation.getLanguageId()));
					translation.setEnabled(announcementData.getEnabled());
					translation = announcementDao.update(translation);
					translations.put(translation.getLanguageId(), editTranslationsData.get(translation.getLanguageId()));
					editTranslationsData.remove(translation.getLanguageId());
			}
			for (Entry<Long, String> editTranslation: editTranslationsData.entrySet()) {
				Announcement announcement = new Announcement();
				announcement.setDescription(editTranslation.getValue());
				announcement.setLanguageId(editTranslation.getKey());
				announcement.setColor(announcementData.getColor());
				announcement.setBgColor(announcementData.getBgColor());
				announcement.setEnabled(announcementData.getEnabled());
				announcement.setId(null);
				announcement.setAnnouncementId(aId);
				announcement = announcementDao.save(announcement);
				translations.put(editTranslation.getKey(), editTranslation.getValue());

			}
			announcementData.setTranslations(translations);
			return announcementData;

		} catch (Exception e) {
			logger.error("Failed to edit annnouncement with ID {}: {}", aId, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public List<Announcement> getActiveAnnouncement(HttpServletRequest request) {
		try {
			List<Announcement> announcementData = new ArrayList<>();
			Map<Long, Integer> announcementIndexMapping = new HashMap<>();
			List<Announcement> announcements = announcementDao.getActiveAnnouncemntInfo();
			for (Announcement announcement : announcements) {
				Long aId = announcement.getAnnouncementId();
				if (!announcementIndexMapping.keySet().contains(aId)) {
					announcementIndexMapping.put(aId, announcementIndexMapping.size());
					Map<Long, String> translations = new HashMap<>();
					translations.put(announcement.getLanguageId(), announcement.getDescription());
					announcement.setTranslations(translations);
					announcementData.add(announcement);
				} else {
					int targetIndex = announcementIndexMapping.get(aId);
					Announcement targetAnnouncement = announcementData.get(targetIndex);

					Map<Long, String> translationsMap = targetAnnouncement.getTranslations();
					translationsMap.put(announcement.getLanguageId(), announcement.getDescription());

					targetAnnouncement.setTranslations(translationsMap);
				}
			}
			return announcementData;
		} catch (Exception e) {
			logger.error("Failed to get active announcement : {}", e.getMessage(), e);
			return null;
		}
	}

	@Override
	public byte[] download(HttpServletRequest request, SpeciesDownload speciesData) {
		PDDocument document = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			document = new PDDocument();

			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);
			PDPageContentStream contentStream = new PDPageContentStream(document, page);

			currentY = PAGE_HEIGHT;

			addHeaderBanner(document, contentStream, page, speciesData);
			currentY -= 25;

			contentStream.close();

			PageContext ctx = checkAndCreateNewPage(document, page, null, 200);
			page = ctx.page;
			contentStream = ctx.contentStream;

			addImageGallery(contentStream);

			float currentLeftY = currentY;

			ctx = addTaxonomySection(document, contentStream, page, speciesData, currentLeftY);
			contentStream = ctx.contentStream;
			page = ctx.page;
			currentLeftY = ctx.yPosition;

			ctx = addSynonymSection(document, contentStream, page, speciesData, currentLeftY);
			contentStream = ctx.contentStream;
			page = ctx.page;
			currentLeftY = ctx.yPosition;

			ctx = addCommonNamesSection(document, contentStream, page, speciesData, currentLeftY);
			contentStream = ctx.contentStream;
			page = ctx.page;
			currentLeftY = ctx.yPosition;

			for (int i = 0; i < speciesData.getFieldData().size(); i++) {
				ctx = addSpeciesFieldSection(document, contentStream, page, speciesData.getFieldData().get(i),
						currentLeftY);
				contentStream = ctx.contentStream;
				page = ctx.page;
				currentLeftY = ctx.yPosition;
			}

			contentStream.close();

			document.save(baos);
			document.close();
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			if (document != null) {
				try {
					document.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return null;
		}
	}

	private static class PageContext {
		PDPage page;
		PDPageContentStream contentStream;
		float yPosition;

		PageContext(PDPage page, PDPageContentStream contentStream) {
			this.page = page;
			this.contentStream = contentStream;
			this.yPosition = currentY;
		}

		PageContext(PDPage page, PDPageContentStream contentStream, float yPosition) {
			this.page = page;
			this.contentStream = contentStream;
			this.yPosition = yPosition;
		}
	}

	private static PageContext checkAndCreateNewPage(PDDocument document, PDPage currentPage,
			PDPageContentStream currentStream, float neededSpace) throws Exception {
		if (currentY - neededSpace < 0) {
			if (currentStream != null) {
				currentStream.close();
			}
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			currentY = PAGE_HEIGHT;
			PDPageContentStream newStream = new PDPageContentStream(document, newPage);
			return new PageContext(newPage, newStream);
		}
		if (currentStream == null) {
			currentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true);
		}
		return new PageContext(currentPage, currentStream);
	}

	public static void addImage(PDDocument document, PDPage page, String imagePath, float x, float y, float width,
			float height) throws IOException {

// Create image object from file
		PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);

		try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
				PDPageContentStream.AppendMode.APPEND, true)) {
// Draw the image
			contentStream.drawImage(pdImage, x, y, width, height);
		}
	}

	private static float addHeaderBanner(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData) throws Exception {
		float bannerHeight = (splitTextIntoLines(speciesData.getTitle(), PDType1Font.HELVETICA_BOLD_OBLIQUE, 32,
				PAGE_WIDTH - 80).size() * 35) + 20 + 50;

		// Solid background color - changed from GRAY_200
		cs.setNonStrokingColor(new Color(240, 245, 250));
		cs.addRect(0, PAGE_HEIGHT - bannerHeight, PAGE_WIDTH, bannerHeight);
		cs.fill();

		addImage(document, page, "/app/data/biodiv/logo/IBP.png", 0, currentY -20, 128, 60);

		cs.setNonStrokingColor(BLACK);
		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA, 14);
		cs.newLineAtOffset(MARGIN+128, currentY - 20);
		cs.showText("India Biodiversity Portal");
		cs.endText();

		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA, 14);
		cs.newLineAtOffset(MARGIN + CONTENT_WIDTH - 80, currentY - 20);
		String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		cs.showText(formattedDate);
		cs.endText();

		currentY = drawTextWithWordWrap(cs, speciesData.getTitle(), PDType1Font.HELVETICA_BOLD_OBLIQUE, 32, 40,
				PAGE_HEIGHT - 60, PAGE_WIDTH - 80, 35, null);

		float badgeX = 40;
		float badgeY = currentY;
		float badgeWidth = 80;
		float badgeHeight = 20;

		// Badge background - changed color
		cs.setNonStrokingColor(new Color(203, 255, 232));
		cs.addRect(badgeX, badgeY, badgeWidth, badgeHeight);
		cs.fill();

		cs.setNonStrokingColor(new Color(0, 140, 68));
		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA, 11);
		cs.newLineAtOffset(badgeX + 8, badgeY + 5);
		cs.showText(speciesData.getBadge());
		cs.endText();

		currentY = PAGE_HEIGHT - bannerHeight;

		return currentY;
	}

	public static List<String> splitTextIntoLines(String text, PDFont font, float fontSize, float maxWidth)
			throws IOException {
		List<String> lines = new ArrayList<>();
		String[] words = text.split(" ");
		StringBuilder currentLine = new StringBuilder();

		for (String word : words) {
			String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
			float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;

			if (testWidth < maxWidth) {
				if (currentLine.length() > 0) {
					currentLine.append(" ");
				}
				currentLine.append(word);
			} else {
				if (currentLine.length() > 0) {
					lines.add(currentLine.toString());
					currentLine = new StringBuilder(word);
				} else {
					lines.add(word);
				}
			}
		}

		if (currentLine.length() > 0) {
			lines.add(currentLine.toString());
		}

		return lines;
	}

	public static float drawTextWithWordWrap(PDPageContentStream cs, String text, PDFont font, float fontSize, float x,
			float y, float maxWidth, float lineHeight, Color color) throws IOException {
		List<String> lines = splitTextIntoLines(text, font, fontSize, maxWidth);

		float currentYPos = y;
		for (String line : lines) {
			if (color != null) {
				cs.setNonStrokingColor(color);
				cs.setLineWidth(1);
				cs.addRect(MARGIN, currentYPos - 1.5f, CONTENT_WIDTH, lineHeight);
				cs.fill();
			}

			cs.setNonStrokingColor(BLACK);
			cs.beginText();
			cs.setFont(font, fontSize);
			cs.newLineAtOffset(x, currentYPos + 1.5f);
			cs.showText(line);
			cs.endText();

			currentYPos -= lineHeight;
		}

		return currentYPos;
	}

	public static PageContext drawTextWithWordWrapAndOverflow(PDPageContentStream cs, PDDocument document,
			PDPage currentPage, String text, PDFont font, float fontSize, float x, float y, float maxWidth,
			float lineHeight, Color color, String leftText, float paddingBottom) throws IOException {
		List<String> lines = splitTextIntoLines(text, font, fontSize, maxWidth);

		float currentYPos = y - 5;
		int i = 0;
		for (String line : lines) {
			if (currentYPos - lineHeight - (i == lines.size() - 1 ? paddingBottom : 0) < 0) {
				cs.close();
				PDPage newPage = new PDPage(PDRectangle.A4);
				document.addPage(newPage);
				currentPage = newPage;
				currentYPos = PAGE_HEIGHT - lineHeight;
				cs = new PDPageContentStream(document, newPage);
			}

			if (color != null) {
				cs.setNonStrokingColor(color);
				cs.setLineWidth(1);
				cs.addRect(MARGIN, currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0), CONTENT_WIDTH,
						lineHeight + (i == 0 ? 5 : 0) + (i == lines.size() - 1 ? paddingBottom : 0));
				cs.fill();
			}

			cs.setNonStrokingColor(BLACK);

			if (leftText != null && i == 0) {
				cs.beginText();
				cs.setFont(font, fontSize);
				cs.newLineAtOffset(MARGIN + 15, currentYPos + 1.5f);
				cs.showText(leftText);
				cs.endText();
			}

			cs.beginText();
			cs.setFont(font, fontSize);
			cs.newLineAtOffset(x, currentYPos + 1.5f);
			cs.showText(line);
			cs.endText();

			cs.setStrokingColor(new Color(220, 220, 220));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN,
					currentYPos + lineHeight - 1.5f + (i == 0 ? 5 : 0) + (i == lines.size() - 1 ? paddingBottom : 0));
			cs.lineTo(MARGIN, currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0));
			cs.stroke();

			cs.setStrokingColor(new Color(220, 220, 220));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH,
					currentYPos + lineHeight - 1.5f + (i == 0 ? 5 : 0) + (i == lines.size() - 1 ? paddingBottom : 0));
			cs.lineTo(MARGIN + CONTENT_WIDTH, currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0));
			cs.stroke();

			currentYPos -= lineHeight;
			i = i + 1;
		}

		return new PageContext(currentPage, cs, currentYPos - paddingBottom);
	}

	public static PageContext addPadding(PDPageContentStream cs, PDDocument document, PDPage currentPage, float x,
			float y, float paddingHeight, Color color) throws IOException {

		float currentYPos = y;
		if (currentYPos - paddingHeight < 0) {
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			currentPage = newPage;
			currentYPos = PAGE_HEIGHT - 11;
			cs = new PDPageContentStream(document, newPage);
		}
		cs.setNonStrokingColor(color);
		cs.setLineWidth(1);
		cs.addRect(MARGIN, currentYPos - 1.5f, CONTENT_WIDTH, paddingHeight);
		cs.fill();

		return new PageContext(currentPage, cs, currentYPos);
	}

	private static void drawSectionCard(PDPageContentStream cs, String title, float height, float curretLeftY)
			throws Exception {
		float cardY = curretLeftY - height;
		float width = (CONTENT_WIDTH);

		float headerHeight = 35;

		// Section header background - changed color
		/*
		 * cs.setNonStrokingColor(new Color(225, 225, 225)); cs.addRect(MARGIN,
		 * curretLeftY - headerHeight, width, headerHeight); cs.fill();
		 */

		// Bottom border - changed color
		cs.setStrokingColor(BLACK);
		cs.setLineWidth(2);
		cs.moveTo(MARGIN, curretLeftY - headerHeight);
		cs.lineTo(MARGIN + width, curretLeftY - headerHeight);
		cs.stroke();

		cs.setStrokingColor(new Color(220, 220, 220));
		cs.setLineWidth(1);
		cs.addRect(MARGIN, curretLeftY - headerHeight, width, headerHeight);
		cs.stroke();

		cs.setNonStrokingColor(BLACK);
		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
		cs.newLineAtOffset(MARGIN + 15, curretLeftY - 22);
		cs.showText(title);
		cs.endText();

		currentY = cardY;
	}

	private static void addImageGallery(PDPageContentStream cs) throws Exception {
		float galleryHeight = 450;
		float galleryY = currentY - galleryHeight;

		// Dark background - changed color
		cs.setNonStrokingColor(new Color(45, 55, 70));
		cs.addRect(MARGIN, galleryY, CONTENT_WIDTH, galleryHeight);
		cs.fill();

		// Main circle placeholder
		float circleRadius = 100;
		float circleCenterX = PAGE_WIDTH / 2;
		float circleCenterY = galleryY + galleryHeight - 150;

		// Draw green circle - changed color
		cs.setNonStrokingColor(new Color(80, 180, 130));
		drawCircle(cs, circleCenterX, circleCenterY, circleRadius);

		// Inner circle - changed color
		cs.setNonStrokingColor(new Color(100, 200, 150));
		drawCircle(cs, circleCenterX, circleCenterY, circleRadius * 0.8f);

		// Draw "?" in center - changed color
		cs.setNonStrokingColor(new Color(70, 170, 120));
		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA_BOLD, 80);
		cs.newLineAtOffset(circleCenterX - 20, circleCenterY - 25);
		cs.showText("?");
		cs.endText();

		// Caption - changed color
		cs.setNonStrokingColor(new Color(160, 170, 180));
		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
		String caption = "No image available - Placeholder shown";
		float captionWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(caption) / 1000 * 10;
		cs.newLineAtOffset((PAGE_WIDTH - captionWidth) / 2, circleCenterY - 130);
		cs.showText(caption);
		cs.endText();

		// Thumbnail grid (4x2)
		float thumbWidth = 100;
		float thumbHeight = 60;
		float thumbSpacing = 10;
		float gridWidth = (thumbWidth * 4) + (thumbSpacing * 3);
		float gridStartX = (PAGE_WIDTH - gridWidth) / 2;
		float gridY = galleryY + 80;

		// Changed thumbnail colors
		cs.setNonStrokingColor(new Color(60, 75, 90));
		cs.setStrokingColor(new Color(45, 55, 70));

		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 4; col++) {
				float thumbX = gridStartX + (col * (thumbWidth + thumbSpacing));
				float thumbY = gridY - (row * (thumbHeight + thumbSpacing));

				cs.addRect(thumbX, thumbY, thumbWidth, thumbHeight);
				cs.fill();

				// Changed text color
				cs.setNonStrokingColor(new Color(130, 140, 150));
				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA, 9);
				String label = "Image " + ((row * 4) + col + 1);
				float labelWidth = PDType1Font.HELVETICA.getStringWidth(label) / 1000 * 9;
				cs.newLineAtOffset(thumbX + (thumbWidth - labelWidth) / 2, thumbY + thumbHeight / 2 - 3);
				cs.showText(label);
				cs.endText();

				cs.setNonStrokingColor(new Color(60, 75, 90));
			}
		}

		currentY = galleryY - 10;
	}

	private static void drawCircle(PDPageContentStream cs, float centerX, float centerY, float radius)
			throws IOException {
		float magic = radius * 0.551915024494f;

		cs.moveTo(centerX, centerY + radius);
		cs.curveTo(centerX + magic, centerY + radius, centerX + radius, centerY + magic, centerX + radius, centerY);
		cs.curveTo(centerX + radius, centerY - magic, centerX + magic, centerY - radius, centerX, centerY - radius);
		cs.curveTo(centerX - magic, centerY - radius, centerX - radius, centerY - magic, centerX - radius, centerY);
		cs.curveTo(centerX - radius, centerY + magic, centerX - magic, centerY + radius, centerX, centerY + radius);
		cs.fill();
	}

	private static PageContext addTaxonomySection(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData, float currentLeftY) throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY - 37;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			drawSectionCard(cs, "Taxonomy", 0, currentY);
			y = currentY - 47;
		} else {
			drawSectionCard(cs, "Taxonomy", 0, sectionStartY);
			y = y - 10;
		}

		for (int i = 0; i < speciesData.getTaxonomy().size(); i++) {

			String name = speciesData.getTaxonomy().get(i).getRankName();
			cs.setNonStrokingColor(BLACK);

			// Changed row colors
			Color rowColor = new Color(255, 255, 255);

			PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page,
					speciesData.getTaxonomy().get(i).getName(), PDType1Font.HELVETICA, 11, MARGIN + 165, y, width - 185,
					16, rowColor, name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase(), 5);
			page = context.page;
			cs = context.contentStream;
			y = context.yPosition;
		}

		cs.setStrokingColor(new Color(220, 220, 220));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + width, y + 15);
		cs.stroke();

		return new PageContext(page, cs, y - 10);
	}

	private static PageContext addSynonymSection(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData, float currentLeftY) throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY - 37;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			drawSectionCard(cs, "Synonyms", 0, currentY);
			y = currentY - 47;
		} else {
			drawSectionCard(cs, "Synonyms", 0, sectionStartY);
			y = y - 10;
		}

		for (int i = 0; i < speciesData.getSynonyms().size(); i++) {
			cs.setNonStrokingColor(BLACK);

			// Changed row colors
			Color rowColor = i % 2 == 0 ? new Color(235, 242, 247) : new Color(255, 255, 255);

			PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, speciesData.getSynonyms().get(i),
					PDType1Font.HELVETICA, 11, MARGIN + 165, y, width - 185, 16, rowColor, "synonym", 5);
			page = context.page;
			cs = context.contentStream;
			y = context.yPosition;
		}

		cs.setStrokingColor(new Color(220, 220, 220));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + width, y + 15);
		cs.stroke();

		return new PageContext(page, cs, y - 10);
	}

	private static PageContext addCommonNamesSection(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData, float currentLeftY) throws Exception {
		float i = 0;
		float width = CONTENT_WIDTH;
		float y = currentLeftY - 37;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			drawSectionCard(cs, "Common Names", 0, currentY);
			y = currentY - 47;
		} else {
			drawSectionCard(cs, "Common Names", 0, sectionStartY);
			y = y - 10;
		}

		for (Map.Entry<String, List<String>> entry : speciesData.getCommonNames().entrySet()) {
			String language = entry.getKey();
			List<String> names = entry.getValue();

			for (int j = 0; j < names.size(); j++) {
				String commonName = names.get(j);

				cs.setNonStrokingColor(BLACK);

				// Changed row colors
				Color rowColor = i % 2 == 0 ? new Color(235, 242, 247) : new Color(255, 255, 255);

				PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, commonName,
						PDType1Font.HELVETICA, 11, MARGIN + 165, y, width - 185, 16, rowColor, j == 0 ? language : null,
						5);
				page = context.page;
				cs = context.contentStream;
				y = context.yPosition;
			}
			i = i + 1;
		}

		cs.setStrokingColor(new Color(220, 220, 220));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + width, y + 15);
		cs.stroke();

		return new PageContext(page, cs, y - 10);
	}

	public static String convertHtmlToText(String html) {
		if (html == null || html.trim().isEmpty()) {
			return "";
		}

		html = html.replaceAll("<h1[^>]*>", "\n\n<h>").replaceAll("</h1>", "\n\n").replaceAll("<h2[^>]*>", "\n\n<h>")
				.replaceAll("</h2>", "\n\n").replaceAll("<h3[^>]*>", "\n\n<h>").replaceAll("</h3>", "\n\n")
				.replaceAll("<h4[^>]*>", "\n\n<h>").replaceAll("</h4>", "\n\n").replaceAll("<h5[^>]*>", "\n\n<h>")
				.replaceAll("</h5>", "\n\n").replaceAll("<h6[^>]*>", "\n\n<h>").replaceAll("</h6>", "\n\n");

		html = html.replaceAll("<p[^>]*>", "\n").replaceAll("</p>", "\n").replaceAll("<br[^>]*>", "\n")
				.replaceAll("<div[^>]*>", "\n").replaceAll("</div>", "\n");

		return html;
	}

	private static String decodeHtmlEntities(String text) {
		return text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
				.replace("&#39;", "'").replace("&nbsp;", " ").replace("&copy;", "(c)").replace("&reg;", "(r)")
				.replace("&#8217;", "'").replace("&#8220;", "\"").replace("&#8221;", "\"");
	}

	private static PageContext addSpeciesFieldSection(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesField speciesField, float currentLeftY) throws Exception {
		float y = currentLeftY - 37;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			drawSectionCard(cs, speciesField.getName(), 0, currentY);
			y = currentY - 47;
		} else {
			drawSectionCard(cs, speciesField.getName(), 0, sectionStartY);
			y = y - 10;
		}

		PageContext ctx = addSpeciesFieldGroup(document, cs, page, speciesField, 0, y);
		cs = ctx.contentStream;
		page = ctx.page;
		y = ctx.yPosition;

		cs.setStrokingColor(new Color(220, 220, 220));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + CONTENT_WIDTH, y + 15);
		cs.stroke();

		return new PageContext(page, cs, y - 10);
	}

	private static PageContext addSpeciesFieldGroup(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesField speciesField, int level, float currentLeftY) throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY;
		float[] titleSize = { 15, 13, 10 };

		cs.setNonStrokingColor(BLACK);

		if (level != 0) {
			// Changed background color
			PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, speciesField.getName(),
					PDType1Font.HELVETICA_BOLD, titleSize[level], MARGIN + 15, y, width - 30, 16,
					new Color(250, 250, 250), null, 5);
			page = context.page;
			cs = context.contentStream;
			y = context.yPosition;
		}

		for (int i = 0; i < speciesField.getValues().size(); i++) {
			String plainText = convertHtmlToText(speciesField.getValues().get(i));
			String[] paragraphs = plainText.split("\n\n");
			/*
			 * for (String paragraph : paragraphs) { String[] lines = paragraph.split("\n");
			 * for (String line : lines) { // Changed background color PageContext context =
			 * drawTextWithWordWrapAndOverflow(cs, document, page, line,
			 * PDType1Font.HELVETICA, 11, MARGIN + 25, y, width - 50, 16, new Color(255,
			 * 255, 255), null); page = context.page; cs = context.contentStream; y =
			 * context.yPosition; } }
			 */

			for (String paragraph : paragraphs) {
				String[] lines = paragraph.split("\n");
				for (String line : lines) {
				if (!line.isEmpty()) {
					PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page,
							paragraph.startsWith("<h>") ? line.substring(3) : line,
							paragraph.startsWith("<h>") ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, 11,
							MARGIN + 25, y, width - 50, 16, new Color(255, 255, 255), null,
							paragraph.startsWith("<h>") ? 10 : 5);
					page = context.page;
					cs = context.contentStream;
					y = context.yPosition;
				}
				}
			}
		}

		for (int i = 0; i < speciesField.getChildField().size(); i++) {
			PageContext ctx = addSpeciesFieldGroup(document, cs, page, speciesField.getChildField().get(i), level + 1,
					y);
			cs = ctx.contentStream;
			page = ctx.page;
			y = ctx.yPosition;
		}

		return new PageContext(page, cs, y);
	}

}
