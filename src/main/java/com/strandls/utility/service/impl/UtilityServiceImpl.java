/** */
package com.strandls.utility.service.impl;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.activity.pojo.MailData;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.utility.util.PropertyFileUtil;
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
import com.strandls.utility.pojo.BreadCrumb;
import com.strandls.utility.pojo.DocumentMeta;
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
import com.strandls.utility.pojo.Trait;
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

	private static final Color WHITE = Color.WHITE;
	private static final Color BLACK = Color.BLACK;

	// Page dimensions
	private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
	private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
	private static final float MARGIN = 30;
	private static final float CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);
	private static final PDFont primaryFont = PDType1Font.HELVETICA;
	private static final PDFont boldFont = PDType1Font.HELVETICA_BOLD;
	private static final PDFont italicFont = PDType1Font.HELVETICA_OBLIQUE;

	// Current Y position tracker
	private static float currentY;

	private static PDFont fallbackFont;

	// ============================================================================
	// CONSTANTS & CONFIGURATION
	// ============================================================================

	private String FALLBACK_FONT_PATH = PropertyFileUtil.fetchProperty("config.properties", "fallback_font_path");
	private String LOGO_PATH = PropertyFileUtil.fetchProperty("config.properties", "logo_path");
	private String SPECIES_GROUP_IMAGE_PATH = PropertyFileUtil.fetchProperty("config.properties",
			"species_group_image_path");
	private String SPECIES_IMAGE_PATH = PropertyFileUtil.fetchProperty("config.properties", "species_image_path");
	private String USER_IMAGE = PropertyFileUtil.fetchProperty("config.properties", "user_image");
	private String TRAITS_IMAGE = PropertyFileUtil.fetchProperty("config.properties", "traits_image");
	private String SITENAME = PropertyFileUtil.fetchProperty("config.properties", "siteName");

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

	@Override
	public Announcement createAnnouncement(HttpServletRequest request, Announcement announcementData) {
		try {
			Map<Long, String> translations = new HashMap<>();
			Long aId = null;
			for (Map.Entry<Long, String> translation : announcementData.getTranslations().entrySet()) {
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
			for (Map.Entry<Long, String> editTranslation : editTranslationsData.entrySet()) {
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

			// Free Serif fallback font for other languages
			File fontFile = new File(FALLBACK_FONT_PATH);

			fallbackFont = PDType0Font.load(document, fontFile);

			// Add initial page of A4 size
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);
			PDPageContentStream contentStream = new PDPageContentStream(document, page);

			PDPage sourcePage = page;

			// Adding background colour to page
			addPageBackground(contentStream);

			currentY = PAGE_HEIGHT;

			// Add Header
			addHeaderBanner(document, contentStream, page, speciesData);
			// Here 25 is padding
			currentY -= 25;

			contentStream.close();

			// Check if new page is needed
			// 200 is the needed space
			PageContext ctx = checkAndCreateNewPage(document, page, null, 200);
			page = ctx.page;
			contentStream = ctx.contentStream;

			// Add image gallery
			addImageGallery(document, page, contentStream, speciesData);

			float currentLeftY = currentY;

			// Adding view more for hyperlinking it to extra images
			if (speciesData.getResourceData() != null && speciesData.getResourceData().size() > 1) {
				// Code for write white text
				contentStream.setNonStrokingColor(WHITE);
				contentStream.beginText();
				// font size is 10
				contentStream.setFont(primaryFont, 10);
				// offset position is startX and startY
				contentStream.newLineAtOffset(CONTENT_WIDTH - 30, currentY + 30);
				contentStream.showText("View More");
				contentStream.endText();

				sourcePage = page;
			}

			// Adding position for the link
			PDAnnotationLink link = new PDAnnotationLink();
			// (x, y, width , height)
			PDRectangle position = new PDRectangle(CONTENT_WIDTH - 30, currentY + 30, 50, 10);
			link.setRectangle(position);

			// Add taxonomy section
			ctx = addTaxonomySection(document, contentStream, page, speciesData, currentLeftY);
			contentStream = ctx.contentStream;
			page = ctx.page;
			currentLeftY = ctx.yPosition;

			// Add synonym section
			if (speciesData.getSynonyms().size() > 0 && speciesData.getSynonyms() != null) {
				ctx = addSynonymSection(document, contentStream, page, speciesData, currentLeftY);
				contentStream = ctx.contentStream;
				page = ctx.page;
				currentLeftY = ctx.yPosition;
			}

			// Add common name section
			if (speciesData.getCommonNames().size() > 0 && speciesData.getCommonNames() != null) {
				ctx = addCommonNamesSection(document, contentStream, page, speciesData, currentLeftY);
				contentStream = ctx.contentStream;
				page = ctx.page;
				currentLeftY = ctx.yPosition;
			}

			// Adding species Field sections
			for (SpeciesField speciesField : speciesData.getFieldData()) {
				ctx = addSpeciesFieldSection(document, contentStream, page, speciesField, currentLeftY,
						speciesData.getObservationMap(), speciesData.getDocumentMetaList(), speciesData.getUrl(),
						speciesData.getLanguageId());
				contentStream = ctx.contentStream;
				page = ctx.page;
				currentLeftY = ctx.yPosition;
			}

			// Adding referneces sections
			ctx = addReferencesSection(document, contentStream, page, speciesData, currentLeftY);
			contentStream = ctx.contentStream;
			page = ctx.page;
			currentLeftY = ctx.yPosition;

			// Adding temporal observed on chart
			if (speciesData.getChartImage() != null && !speciesData.getChartImage().isEmpty()) {
				ctx = addTemporalObservedOn(document, contentStream, page, speciesData, currentLeftY);
				contentStream = ctx.contentStream;
				page = ctx.page;
				currentLeftY = ctx.yPosition;
			}

			// Adding traits chart
			if (speciesData.getTraitsChart() != null && !speciesData.getTraitsChart().isEmpty()) {
				ctx = addTraitsPerMonth(document, contentStream, page, speciesData, currentLeftY);
				contentStream = ctx.contentStream;
				page = ctx.page;
				currentLeftY = ctx.yPosition;
			}

			// Adding additional images
			if (speciesData.getResourceData() != null && speciesData.getResourceData().size() > 1) {
				ctx = addAdditionalImages(document, contentStream, page, speciesData, currentLeftY, link, sourcePage);
				contentStream = ctx.contentStream;
				page = ctx.page;
				currentLeftY = ctx.yPosition;
			}

			contentStream.close();

			document.save(baos);
			document.close();
			return baos.toByteArray();
		} catch (Exception e) {
			logger.error("Error while generating pdf : {}", e.getMessage(), e);
			if (document != null) {
				try {
					document.close();
				} catch (Exception ex) {
					logger.error("Couldn't close document : {}", ex.getMessage(), ex);
				}
			}
			return null;
		}
	}

	private static void addPageBackground(PDPageContentStream contentStream) throws IOException {
		contentStream.setNonStrokingColor(new Color(246, 250, 252));
		contentStream.addRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
		contentStream.fill();
	}

	// For tracking current page, contentStream and position of y
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

	// To check if new page is needed
	private static PageContext checkAndCreateNewPage(PDDocument document, PDPage currentPage,
			PDPageContentStream currentStream, float neededSpace) throws Exception {
		// If we don't have required space add new page
		if (currentY - neededSpace < 0) {
			if (currentStream != null) {
				currentStream.close();
			}
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			currentY = PAGE_HEIGHT;
			PDPageContentStream newStream = new PDPageContentStream(document, newPage);
			addPageBackground(newStream);
			return new PageContext(newPage, newStream);
		}
		if (currentStream == null) {
			currentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true);
		}
		return new PageContext(currentPage, currentStream);
	}

	// Code for adding images
	public static void addImage(PDDocument document, PDPage page, String imagePath, float x, float y, float height,
			Boolean fallback, Boolean align, float maxWidth, Boolean fixedWidth) throws IOException {

		File imageFile = new File(imagePath);

		try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
				PDPageContentStream.AppendMode.APPEND, true)) {

			if (imageFile.exists() && imageFile.canRead() && imageFile.length() > 0) {
				try {
					// Try to load and draw the image
					PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
					float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
					float width = (float) height / aspectRatio;

					// Aligning the image
					if (fixedWidth) {
						width = maxWidth;
						y = y + (height - width * aspectRatio) / 2;
						height = width * aspectRatio;
					}
					if (align) {
						x = x + (maxWidth - width) / 2;
					}
					contentStream.drawImage(pdImage, x, y, width, height);
				} catch (Exception e) {
					// If image loading fails
					logger.error("Loading fails for image path: " + imagePath);
					if (fallback) {
						if (align) {
							x = x + (maxWidth - height) / 2;
						}
						drawFallbackRectangle(contentStream, x, y, height, height, "!");
					}
				}
			} else {
				// fallback if file doesn't exist
				logger.error("Image file doesn't exist: " + imagePath);
				if (fallback) {
					if (align) {
						x = x + (maxWidth - height) / 2;
					}
					drawFallbackRectangle(contentStream, x, y, height, height, "!");
				}
			}
		}
	}

	// simple rectangle with text as fallback
	private static void drawFallbackRectangle(PDPageContentStream contentStream, float x, float y, float width,
			float height, String text) throws IOException {

		// Draw background
		// r, g, b values are 220 , 220, 220
		contentStream.setNonStrokingColor(new Color(220, 220, 220));
		contentStream.addRect(x, y, width, height);
		contentStream.fill();

		// Draw text
		contentStream.beginText();
		// Font size is 10
		contentStream.setFont(boldFont, 10);
		contentStream.setNonStrokingColor(Color.DARK_GRAY);

		// Center text approximately
		float textWidth = boldFont.getStringWidth(text) / 1000 * 10;
		float textX = x + (width - textWidth) / 2;
		float textY = y + (height / 2) - 4;

		contentStream.newLineAtOffset(textX, textY);
		contentStream.showText(text);
		contentStream.endText();
	}

	private float addHeaderBanner(PDDocument document, PDPageContentStream cs, PDPage page, SpeciesDownload speciesData)
			throws Exception {
		// Divides text into lines based on width available
		// 32 is fontSize, 35 is line height and 170 is space required for other banner
		// details
		float fontSize = 32;
		float lineHeight = fontSize + 3;
		float otherHeaderSpace = 170;
		float width = PAGE_WIDTH - 80;
		float bannerHeight = (splitTextIntoLines(speciesData.getTitle(), boldFont, fontSize, width).size() * lineHeight)
				+ otherHeaderSpace;

		// Adding Banner background color
		cs.setNonStrokingColor(new Color(199, 212, 224));
		cs.addRect(0, PAGE_HEIGHT - bannerHeight, PAGE_WIDTH, bannerHeight);
		cs.fill();

		// Adding Logo
		float logoHeight = 60;
		addImage(document, page, LOGO_PATH, MARGIN, currentY - logoHeight - 10, logoHeight, true, false, CONTENT_WIDTH,
				false);

		fontSize = 14;

		// Adding portal name
		cs.setNonStrokingColor(new Color(33, 37, 41));
		drawTextWithWordWrap(cs, SITENAME, primaryFont, fontSize, MARGIN + 158, currentY - 43, CONTENT_WIDTH - 90 - 158,
				16, null);

		// Adding date of download
		cs.beginText();
		cs.setFont(primaryFont, fontSize);
		// (x,y)
		cs.newLineAtOffset(MARGIN + CONTENT_WIDTH - 80, currentY - 45);
		String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		cs.showText(formattedDate);
		cs.endText();

		// Adding Species Name
		fontSize = 32;
		float x = 40;
		float y = PAGE_HEIGHT - 110;
		currentY = drawTextWithWordWrap(cs, speciesData.getTitle(), boldFont, fontSize, x, y, PAGE_WIDTH - 80,
				lineHeight, null);

		float badgeX = 40;
		float badgeY = currentY;
		float badgeWidth = 80;
		float badgeHeight = 16;

		// Adding Badge background
		cs.setNonStrokingColor(speciesData.getBadge().equals("ACCEPTED") ? new Color(220, 252, 231)
				: speciesData.getBadge().equals("SYNONYM") ? new Color(243, 232, 255) : new Color(254, 226, 226));
		cs.addRect(badgeX, badgeY, badgeWidth, badgeHeight);
		cs.fill();

		fontSize = 11;

		// Adding badge text
		cs.setNonStrokingColor(speciesData.getBadge().equals("ACCEPTED") ? new Color(17, 105, 50)
				: speciesData.getBadge().equals("SYNONYM") ? new Color(100, 27, 163) : new Color(153, 25, 25));
		cs.beginText();
		cs.setFont(primaryFont, fontSize);
		cs.newLineAtOffset(badgeX + 8, badgeY + 5);
		cs.showText(speciesData.getBadge().equals("ACCEPTED") ? "Accepted"
				: speciesData.getBadge().equals("SYNONYM") ? "Synonym" : "Help Identify");
		cs.endText();

		// Adding species group image
		float sgImageHeight = 40;
		addImage(document, page, SPECIES_GROUP_IMAGE_PATH + speciesData.getSpeciesGroup().toLowerCase() + ".png",
				badgeX, currentY - sgImageHeight - 10, sgImageHeight, false, false, CONTENT_WIDTH, false);

		currentY = PAGE_HEIGHT - bannerHeight;

		return currentY;
	}

	// For maintaining different kinds of text: bold, italic and normal
	public static String convertHtmlToWordLevelMarkers(String text) {
		if (text == null)
			return null;

		// Step 1 formats text for bold tag and strong tag
		String step1 = processFormatting(text, "b", "strong", "**");
		// Step 2 formats the processed text for italic tag
		String step2 = processFormatting(step1, "i", "em", "*");

		return step2;
	}

	private static String processFormatting(String text, String tag, String altTag, String marker) {
		String startTag = "<" + tag + ">";
		String endTag = "</" + tag + ">";

		// Adding start and end markers for tags
		String temp = text.replace(startTag, "〖START〗").replace(endTag, "〖END〗");

		if (altTag != null) {
			// Adding start and end markers for alternative tag
			String altStartTag = "<" + altTag + ">";
			String altEndTag = "</" + altTag + ">";
			temp = temp.replace(altStartTag, "〖START〗").replace(altEndTag, "〖END〗");
		}

		StringBuilder result = new StringBuilder();
		// Splits parts based on start and end markers
		String[] parts = temp.split("(〖START〗|〖END〗)");
		boolean inFormat = false;

		for (String part : parts) {
			if (inFormat) {
				// Splits on the basis of spaces
				String[] words = part.split("(?<= )|(?= )");
				for (String word : words) {
					if (!word.trim().isEmpty() && !word.equals(" ")) {
						result.append(marker).append(word).append(marker);
					} else {
						result.append(word);
					}
				}
			} else {
				result.append(part);
			}
			inFormat = !inFormat;
		}

		return result.toString();
	}

	public static List<String> splitTextIntoLines(String text, PDFont font, float fontSize, float maxWidth)
			throws IOException {
		List<String> lines = new ArrayList<>();

		// Convert HTML tags to word-level markers first
		String markdownText = convertHtmlToWordLevelMarkers(text);

		// Now split using the markdown text
		if (markdownText == null) {
			return lines;
		}

		// extracting words
		String[] words = markdownText.split(" ");
		StringBuilder currentLine = new StringBuilder();

		for (String word : words) {
			String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;

			// Calculate width without asterisks for accurate measurement
			String testLineWithoutMarkers = testLine.replaceAll("\\*", "");
			try {
				// Trying with helvetica font
				float testWidth = font.getStringWidth(testLineWithoutMarkers) / 1000 * fontSize;
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
			} catch (IllegalArgumentException e) {
				logger.warn("Primary font failed, using fallback: " + e.getMessage());

				try {
					// Trying with fallback font
					float testWidth = fallbackFont.getStringWidth(testLineWithoutMarkers) / 1000 * fontSize;
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
				} catch (IllegalArgumentException e2) {
					// Fallback font also failed
					logger.error("Both fonts failed for: " + word + " - " + e2.getMessage());
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
		// Splits text into lines for managing overflow
		List<String> lines = splitTextIntoLines(text, font, fontSize, maxWidth);

		float currentYPos = y;
		for (String line : lines) {
			if (color != null) {
				// Adds background colour
				cs.setNonStrokingColor(color);
				cs.setLineWidth(1);
				cs.addRect(MARGIN, currentYPos - 1.5f, CONTENT_WIDTH, lineHeight);
				cs.fill();
			}

			// Adds formatting and draws each line
			drawFormattedLine(cs, line, font, fontSize, x, currentYPos + 1.5f, maxWidth);

			currentYPos -= lineHeight;
		}

		return currentYPos;
	}

	private static void drawFormattedLine(PDPageContentStream cs, String line, PDFont baseFont, float fontSize,
			float startX, float y, float maxWidth) throws IOException {

		// Parse the line for formatting markers: **bold** and *italic*
		List<TextSegment> segments = parseFormattedSegments(line, baseFont, fontSize);

		float currentX = startX;

		try {
			for (TextSegment segment : segments) {
				cs.beginText();
				cs.setFont(segment.getFont(), fontSize);
				cs.newLineAtOffset(currentX, y);
				cs.showText(segment.getText());
				cs.endText();

				// Move X position for next segment
				currentX += segment.getWidth();
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	/**
	 * NEW: Parse a line into formatted segments
	 */
	private static List<TextSegment> parseFormattedSegments(String line, PDFont baseFont, float fontSize)
			throws IOException {
		List<TextSegment> segments = new ArrayList<>();

		// This pattern matches: **bold**, *italic*, or any text without asterisks
		Pattern pattern = Pattern.compile("(\\*\\*\\*(.+?)\\*\\*\\*)|" + // ***bold italic*** - groups 1 & 2
				"(\\*\\*(.+?)\\*\\*)|" + // **bold** - groups 3 & 4
				"(\\*([^*]+)\\*)|" + // *italic* - groups 5 & 6
				"([^*]+)" // plain text - group 7
		);
		Matcher matcher = pattern.matcher(line);

		PDFont boldItalicFont = PDType1Font.HELVETICA_BOLD_OBLIQUE;

		while (matcher.find()) {
			String boldItalicText = matcher.group(2);
			String boldText = matcher.group(4);
			String italicText = matcher.group(6);
			String normalText = matcher.group(7);

			String segmentText;
			PDFont segmentFont = baseFont;

			if (boldItalicText != null) {
				segmentText = boldItalicText;
				segmentFont = boldItalicFont;
			} else if (boldText != null) {
				segmentText = boldText;
				segmentFont = boldFont;
			} else if (italicText != null) {
				segmentText = italicText;
				segmentFont = baseFont.equals(boldFont) ? boldItalicFont : italicFont;
			} else {
				segmentText = normalText;
			}

			// Skip empty segments
			if (segmentText != null && !segmentText.isEmpty()) {
				// Calculate width for this segment

				try {
					segmentFont.getStringWidth(segmentText);
				} catch (IllegalArgumentException e) {
					logger.warn("Font cannot render text, using fallback: " + segmentText);
					segmentFont = fallbackFont;
				}
				float segmentWidth = segmentFont.getStringWidth(segmentText) * fontSize / 1000f;
				segments.add(new TextSegment(segmentText, segmentFont, segmentWidth));
			}
		}

		return segments;
	}

	private static class TextSegment {
		private String text;
		private PDFont font;
		private float width;

		public TextSegment(String text, PDFont font, float width) {
			this.text = text;
			this.font = font;
			this.width = width;
		}

		public String getText() {
			return text;
		}

		public PDFont getFont() {
			return font;
		}

		public float getWidth() {
			return width;
		}
	}

	public static PageContext drawTextWithWordWrapAndOverflow(PDPageContentStream cs, PDDocument document,
			PDPage currentPage, String text, PDFont font, float fontSize, float x, float y, float maxWidth,
			float lineHeight, Color color, String leftText, float paddingBottom, boolean speciesField,
			boolean contributor, Color traitColor, float level, String url) throws IOException {
		List<String> lines = List.of("");
		if (text != null && !text.isEmpty()) {
			// Split text into lines
			lines = splitTextIntoLines(text, font, fontSize, maxWidth);
			if (lines == null) {
				lines = List.of("");
			}
		}

		// Adding a top padding of 5
		float currentYPos = y - 5;
		int i = 0;
		for (String line : lines) {
			// Checking for required space of each line
			if (currentYPos - lineHeight - (i == lines.size() - 1 ? paddingBottom : 0) < 0) {
				cs.setNonStrokingColor(color != null ? color : WHITE);
				cs.addRect(MARGIN, 0, CONTENT_WIDTH, currentYPos + 14 + (i == 0 ? 5 : 0));
				cs.fill();

				if (speciesField) {
					// If speciesField add white background
					cs.setNonStrokingColor(WHITE);
					// (x, y, width , height)
					cs.addRect(MARGIN + 15, 0, CONTENT_WIDTH - 30, currentYPos + 14 + (i == 0 ? 5 : 0));
					cs.fill();

					// Adding left and right borders
					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + 15, currentYPos + 14 + (i == 0 ? 5 : 0));
					cs.lineTo(MARGIN + 15, 0);
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH - 15, currentYPos + 14 + (i == 0 ? 5 : 0));
					cs.lineTo(MARGIN + CONTENT_WIDTH - 15, 0);
					cs.stroke();
				}

				if (level != 0) {
					// Adding secondary borders for childFIelds
					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + 10, currentYPos + 14 + (i == 0 ? 5 : 0));
					cs.lineTo(MARGIN + 10, 0);
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH - 10, currentYPos + 14 + (i == 0 ? 5 : 0));
					cs.lineTo(MARGIN + CONTENT_WIDTH - 10, 0);
					cs.stroke();

					if (level != 1) {
						// Adding another borders for other level
						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + 13, currentYPos + 14 + (i == 0 ? 5 : 0));
						cs.lineTo(MARGIN + 13, 0);
						cs.stroke();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + CONTENT_WIDTH - 13, currentYPos + 14 + (i == 0 ? 5 : 0));
						cs.lineTo(MARGIN + CONTENT_WIDTH - 13, 0);
						cs.stroke();
					}
				}

				// Adding box borders
				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN, currentYPos + 14 + (i == 0 ? 5 : 0));
				cs.lineTo(MARGIN, 0);
				cs.stroke();

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN + CONTENT_WIDTH, currentYPos + 14 + (i == 0 ? 5 : 0));
				cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
				cs.stroke();
				cs.close();

				// Creating new page
				PDPage newPage = new PDPage(PDRectangle.A4);
				document.addPage(newPage);
				currentPage = newPage;
				currentYPos = PAGE_HEIGHT - lineHeight;
				cs = new PDPageContentStream(document, newPage);
				addPageBackground(cs);
			}

			if (color != null) {
				// Adding colored background
				cs.setNonStrokingColor(color);
				cs.setLineWidth(1);
				cs.addRect(MARGIN, currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0), CONTENT_WIDTH,
						lineHeight + (i == 0 ? 5 : 0) + (i == lines.size() - 1 ? paddingBottom : 0));
				cs.fill();

				if (speciesField) {
					// For speciesField use species colour if background color is mentioned
					cs.setNonStrokingColor(new Color(240, 245, 250));
					cs.setLineWidth(1);
					cs.addRect(MARGIN + 15, currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0),
							CONTENT_WIDTH - 30,
							lineHeight + (i == 0 ? 5 : 0) + (i == lines.size() - 1 ? paddingBottom : 0));
					cs.fill();
				}
			}

			if (traitColor != null) {
				// For adding colour traits
				cs.setNonStrokingColor(traitColor);
				cs.setLineWidth(1);
				cs.addRect(x, currentYPos - 1.5f - (i == lines.size() - 1 ? 5 : 0), maxWidth,
						lineHeight + (i == 0 ? 5 : 0) + (i == lines.size() - 1 ? 5 : 0));
				cs.fill();
			}

			if (speciesField) {

				if (url != null) {
					// Adding url to speciesField text

					PDAnnotationLink link = new PDAnnotationLink();

					PDRectangle position = new PDRectangle(MARGIN + 15, currentYPos - 1.5f, CONTENT_WIDTH - 30,
							lineHeight);
					link.setRectangle(position);

					link.setBorderStyle(new PDBorderStyleDictionary());
					link.getBorderStyle().setWidth(0);

					PDActionURI action = new PDActionURI();
					action.setURI(url);
					link.setAction(action);

					currentPage.getAnnotations().add(link);
				}

				// Adding white background color for speciesField text
				cs.setNonStrokingColor(WHITE);
				cs.setLineWidth(1);
				cs.addRect(MARGIN + 15,
						currentYPos - 1.5f - (i == lines.size() - 1 ? leftText != null ? 5 : paddingBottom : 0),
						CONTENT_WIDTH - 30, lineHeight + (i == 0 ? 5 : 0)
								+ (i == lines.size() - 1 ? leftText != null ? 5 : paddingBottom : 0));
				cs.fill();

				// Adding left and right borders
				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN + 15, currentYPos - 1.5f + lineHeight + (i == 0 ? 5 : 0));
				cs.lineTo(MARGIN + 15,
						currentYPos - 1.5f - (i == lines.size() - 1 ? leftText != null ? 5 : paddingBottom : 0));
				cs.stroke();

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN + CONTENT_WIDTH - 15, currentYPos - 1.5f + lineHeight + (i == 0 ? 5 : 0));
				cs.lineTo(MARGIN + CONTENT_WIDTH - 15,
						currentYPos - 1.5f - (i == lines.size() - 1 ? leftText != null ? 5 : paddingBottom : 0));
				cs.stroke();
			}

			// Adding borders based on level of speciesField
			if (level != 0) {
				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN + 10, currentYPos - 1.5f + lineHeight + (i == 0 ? 5 : 0));
				cs.lineTo(MARGIN + 10, currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0));
				cs.stroke();

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN + CONTENT_WIDTH - 10, currentYPos - 1.5f + lineHeight + (i == 0 ? 5 : 0));
				cs.lineTo(MARGIN + CONTENT_WIDTH - 10,
						currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0));
				cs.stroke();

				if (level != 1) {
					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + 13, currentYPos - 1.5f + lineHeight + (i == 0 ? 5 : 0));
					cs.lineTo(MARGIN + 13, currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0));
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH - 13, currentYPos - 1.5f + lineHeight + (i == 0 ? 5 : 0));
					cs.lineTo(MARGIN + CONTENT_WIDTH - 13,
							currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0));
					cs.stroke();
				}
			}

			cs.setNonStrokingColor(BLACK);

			// Adding left text
			if (leftText != null && i == 0) {
				cs.beginText();
				cs.setFont(leftText.startsWith("*") ? boldFont : font, fontSize);
				cs.newLineAtOffset(speciesField ? MARGIN + 25 : MARGIN + 15, currentYPos + 1.5f);
				cs.showText(leftText.startsWith("*") ? leftText.substring(1) : leftText);
				cs.endText();
			}

			// Adding original text
			drawFormattedLine(cs, line, font, fontSize, x, currentYPos + 1.5f, maxWidth);

			// Adding box borders
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN,
					currentYPos + lineHeight - 1.5f + (i == 0 ? 5 : 0) + (i == lines.size() - 1 ? paddingBottom : 0));
			cs.lineTo(MARGIN, currentYPos - 1.5f - (i == lines.size() - 1 ? paddingBottom : 0));
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
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

	private static void drawSectionCard(PDPageContentStream cs, String title, float height, float curretLeftY)
			throws Exception {
		float cardY = curretLeftY - height;
		float width = (CONTENT_WIDTH);

		float headerHeight = 35;

		// Adding background color
		cs.setNonStrokingColor(new Color(250, 248, 245));
		cs.setLineWidth(1);
		cs.addRect(MARGIN, curretLeftY - headerHeight, width, headerHeight);
		cs.fill();

		// Adding border
		cs.setStrokingColor(new Color(222, 226, 230));
		cs.setLineWidth(1);
		cs.addRect(MARGIN, curretLeftY - headerHeight, width, headerHeight);
		cs.stroke();

		// Adding text
		cs.setNonStrokingColor(new Color(33, 37, 41));
		cs.beginText();
		cs.setFont(boldFont, 13);
		cs.newLineAtOffset(MARGIN + 15, curretLeftY - 22);
		cs.showText(title);
		cs.endText();

		currentY = cardY;
	}

	private void addImageGallery(PDDocument document, PDPage page, PDPageContentStream cs, SpeciesDownload species)
			throws Exception {

		float galleryY = currentY - 360;

		// Gallery background color
		cs.setNonStrokingColor(new Color(45, 55, 70));
		cs.addRect(MARGIN, currentY - 360, CONTENT_WIDTH, 360);
		cs.fill();

		if (species.getResourceData() == null || species.getResourceData().size() < 1) {

			float circleRadius = 100;
			float circleCenterX = PAGE_WIDTH / 2;
			float circleCenterY = currentY - 150;

			// Outer circle
			cs.setNonStrokingColor(new Color(97, 142, 74));
			drawCircle(cs, circleCenterX, circleCenterY, circleRadius);

			// Inner circle
			cs.setNonStrokingColor(new Color(161, 201, 57));
			drawCircle(cs, circleCenterX, circleCenterY, circleRadius * 0.7f);

			// Question mark
			cs.setNonStrokingColor(new Color(97, 142, 74));
			cs.beginText();
			cs.setFont(boldFont, 80);
			cs.newLineAtOffset(circleCenterX - 20, circleCenterY - 25);
			cs.showText("?");
			cs.endText();

			// placeholder text
			cs.setNonStrokingColor(new Color(160, 170, 180));
			cs.beginText();
			cs.setFont(italicFont, 10);
			String caption = "No image available - Placeholder shown";
			float captionWidth = italicFont.getStringWidth(caption) / 1000 * 10;
			cs.newLineAtOffset((PAGE_WIDTH - captionWidth) / 2, circleCenterY - 130);
			cs.showText(caption);
			cs.endText();

			currentY = galleryY - 10;
		} else {
			float galleryHeight = 360;
			galleryY = currentY - galleryHeight - 10;
			// Adding main gallery image
			addImage(document, page, SPECIES_IMAGE_PATH + species.getResourceData().get(0), MARGIN, galleryY + 20,
					galleryHeight - 20, true, true, CONTENT_WIDTH, false);
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
		float y = currentLeftY - 40;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			// Adding continuation background and left and right borders
			cs.setNonStrokingColor(WHITE);
			cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
			cs.fill();
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN, y + 14);
			cs.lineTo(MARGIN, 0);
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
			cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
			cs.stroke();
			cs.close();

			// Adding new page
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;

			// Adding background
			cs = new PDPageContentStream(document, newPage);
			addPageBackground(cs);
			// Adding SectionHeader
			drawSectionCard(cs, "Taxonomy", 0, currentY);
			y = currentY - 50;
		} else {
			// Adding SectionHeader
			drawSectionCard(cs, "Taxonomy", 0, sectionStartY);
			y = y - 10;
		}

		float fontSize = 11;
		float rightX = MARGIN + 165;
		float paddingBottom = 5;
		float lineHeight = 16;

		for (BreadCrumb taxonomy : speciesData.getTaxonomy()) {

			// Left text
			String name = taxonomy.getRankName();
			cs.setNonStrokingColor(new Color(33, 37, 41));

			// Changed row colors
			Color rowColor = new Color(255, 255, 255);

			// Adding row Content
			PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, taxonomy.getName(), primaryFont,
					fontSize, rightX, y, width - 185, lineHeight, rowColor,
					"*" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase(), paddingBottom, false,
					false, null, 0, null);
			page = context.page;
			cs = context.contentStream;
			y = context.yPosition;
		}

		// Adding line at bottom
		cs.setStrokingColor(new Color(222, 226, 230));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + width, y + 15);
		cs.stroke();

		// Returning with padding of 10
		return new PageContext(page, cs, y - 10);
	}

	private static PageContext addSynonymSection(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData, float currentLeftY) throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY - 40;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			// Adding continuation backgroung and borders
			cs.setNonStrokingColor(WHITE);
			cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
			cs.fill();
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN, y + 14);
			cs.lineTo(MARGIN, 0);
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
			cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
			cs.stroke();
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			// Adding page backgrounds
			addPageBackground(cs);
			drawSectionCard(cs, "Synonyms", 0, currentY);
			y = currentY - 50;
		} else {
			drawSectionCard(cs, "Synonyms", 0, sectionStartY);
			y = y - 10;
		}

		float fontSize = 11;
		float rightX = MARGIN + 165;
		float paddingBottom = 5;
		float lineHeight = 16;

		for (int i = 0; i < speciesData.getSynonyms().size(); i++) {
			cs.setNonStrokingColor(new Color(33, 37, 41));

			// Changed row colors
			Color rowColor = i % 2 == 0 ? new Color(240, 245, 250) : new Color(255, 255, 255);

			// Row content
			// Since text is right side adding an offset of 165 and font Size is 11
			PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, speciesData.getSynonyms().get(i),
					primaryFont, fontSize, rightX, y, width - 185, lineHeight, rowColor, "synonym", paddingBottom,
					false, false, null, 0, null);
			page = context.page;
			cs = context.contentStream;
			y = context.yPosition;
		}

		// Bottom line
		cs.setStrokingColor(new Color(222, 226, 230));
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
		float y = currentLeftY - 40;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			// Adding continuation background and borders
			cs.setNonStrokingColor(WHITE);
			cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
			cs.fill();
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN, y + 14);
			cs.lineTo(MARGIN, 0);
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
			cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
			cs.stroke();
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			// Adding page background
			cs = new PDPageContentStream(document, newPage);
			addPageBackground(cs);
			drawSectionCard(cs, "Common Names", 0, currentY);
			y = currentY - 50;
		} else {
			drawSectionCard(cs, "Common Names", 0, sectionStartY);
			y = y - 10;
		}

		float fontSize = 11;
		float rightX = MARGIN + 165;
		float paddingBottom = 5;
		float lineHeight = 16;

		for (Map.Entry<String, List<String>> entry : speciesData.getCommonNames().entrySet()) {
			String language = entry.getKey();
			List<String> names = entry.getValue();

			for (int j = 0; j < names.size(); j++) {
				String commonName = names.get(j);

				cs.setNonStrokingColor(new Color(33, 37, 41));

				// Changed row colors
				Color rowColor = i % 2 == 0 ? new Color(240, 245, 250) : new Color(255, 255, 255);

				// Adding row content
				PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, commonName, primaryFont,
						fontSize, rightX, y, width - 185, lineHeight, rowColor, j == 0 ? language : null, paddingBottom,
						false, false, null, 0, null);
				page = context.page;
				cs = context.contentStream;
				y = context.yPosition;
			}
			i = i + 1;
		}

		// Adding bottom line
		cs.setStrokingColor(new Color(222, 226, 230));
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

		html = fixTagPlacement(html);

		// Replaces all heading tags with new line
		html = html.replaceAll("<h1[^>]*>", "\n<h>").replaceAll("</h1>", "\n").replaceAll("<h2[^>]*>", "\n<h>")
				.replaceAll("</h2>", "\n").replaceAll("<h3[^>]*>", "\n<h>").replaceAll("</h3>", "\n")
				.replaceAll("<h4[^>]*>", "\n<h>").replaceAll("</h4>", "\n").replaceAll("<h5[^>]*>", "\n<h>")
				.replaceAll("</h5>", "\n").replaceAll("<h6[^>]*>", "\n<h>").replaceAll("</h6>", "\n");

		// Replaces paragraphs and divs with new line
		html = html.replaceAll("<p[^>]*>", "\n").replaceAll("</p>", "\n").replaceAll("<br[^>]*>", "\n")
				.replaceAll("<div[^>]*>", "\n").replaceAll("</div>", "\n").replaceAll("<span[^>]*>", "")
				.replaceAll("</span>", "");

		return decodeHtmlEntities(html);
	}

	private static String fixTagPlacement(String html) {
		// Pattern to match opening tag followed immediately by <br>
		// Example: <strong><br> or <b><br> or <em><br> etc.
		Pattern pattern = Pattern.compile("<(strong|b|em|i|u|span)([^>]*)>\\s*<br\\s*/?>", Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(html);
		StringBuffer result = new StringBuffer();

		while (matcher.find()) {
			// Move the tag after the <br>
			String replacement = "<br><" + matcher.group(1) + matcher.group(2) + ">";
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);

		return result.toString();
	}

	private static String decodeHtmlEntities(String text) {
		String cleanedText = text.replace("\t", "");
		return cleanedText.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
				.replace("&#39;", "'").replace("&nbsp;", "").replace("&copy;", "(c)").replace("&reg;", "(r)")
				.replace("&#8217;", "'").replace("&#8220;", "\"").replace("&#8221;", "\"");
	}

	private PageContext addSpeciesFieldSection(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesField speciesField, float currentLeftY, String Map, List<DocumentMeta> documentList, String url,
			Long languageId) throws Exception {
		float y = currentLeftY - 40;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			// Adding continuation background and borders
			cs.setNonStrokingColor(WHITE);
			cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
			cs.fill();
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN, y + 14);
			cs.lineTo(MARGIN, 0);
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
			cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
			cs.stroke();
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			// Adding page background
			addPageBackground(cs);
			// Adding section card
			drawSectionCard(cs, speciesField.getName(), 0, currentY);
			y = currentY - 50;
		} else {
			drawSectionCard(cs, speciesField.getName(), 0, sectionStartY);
			y = y - 10;
		}

		PageContext ctx = addSpeciesFieldGroup(document, cs, page, speciesField, 0, y, Map, documentList, url,
				languageId);
		cs = ctx.contentStream;
		page = ctx.page;
		y = ctx.yPosition;

		// Adding bottom border
		cs.setStrokingColor(new Color(222, 226, 230));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + CONTENT_WIDTH, y + 15);
		cs.stroke();

		return new PageContext(page, cs, y - 10);
	}

	private PageContext addSpeciesFieldGroup(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesField speciesField, int level, float currentLeftY, String Map, List<DocumentMeta> documentList,
			String url, Long languageId) throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY;
		float[] titleSize = { 15, 12, 10 };

		cs.setNonStrokingColor(new Color(33, 37, 41));
		float lineHeight = 16;
		// Observation Map
		if (speciesField.getId() == 65) {
			if (Map != null && !Map.trim().isEmpty()) {
				try {
					if (Map.contains(",")) {
						Map = Map.split(",")[1];
					}

					byte[] imageBytes = Base64.getDecoder().decode(Map);
					PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "map");

					// Calculate height maintaining aspect ratio

					float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
					float imageWidth = CONTENT_WIDTH - 30;
					float height = aspectRatio * imageWidth;
					float x = (CONTENT_WIDTH - imageWidth) / 2;

					if (y - height - 25 < 0) {
						// Adding continuation borders and background
						cs.setNonStrokingColor(new Color(240, 245, 250));
						cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
						cs.fill();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN, y + 14);
						cs.lineTo(MARGIN, 0);
						cs.stroke();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
						cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
						cs.stroke();
						cs.close();
						PDPage newPage = new PDPage(PDRectangle.A4);
						document.addPage(newPage);
						page = newPage;
						y = PAGE_HEIGHT - 10;
						cs = new PDPageContentStream(document, newPage);
						// Adding page background
						addPageBackground(cs);
					}

					// Adding map image
					addBase64Image(document, page, pdImage, MARGIN + x, y - height, imageWidth, height);

					// Adding background and borders
					cs.setNonStrokingColor(new Color(240, 245, 250));
					cs.addRect(MARGIN, y - height - 25 + 15, CONTENT_WIDTH, height + 25);
					cs.fill();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN, y + 15);
					cs.lineTo(MARGIN, y + 15 - height - 25);
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH, y + 15);
					cs.lineTo(MARGIN + CONTENT_WIDTH, y + 15 - height - 25);
					cs.stroke();
					y = y - height - 25;

				} catch (Exception e) {
					logger.error("Invalid Image");
					drawFallbackRectangle(cs, MARGIN + 15, y, CONTENT_WIDTH - 30, 100, "!");
				}
			}

		} else {
			if (level != 0 && speciesField.getId() != 82) {
				// Adding title
				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(0.5f);
				cs.moveTo(MARGIN + 10 + (level != 1 ? 3 : 0), y + 15);
				cs.lineTo(MARGIN + width - 10 - (level != 1 ? 3 : 0), y + 15);
				cs.stroke();
				float paddingBottom = 10;
				PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, speciesField.getName(),
						boldFont, titleSize[level], MARGIN + 15, y, width - 30, lineHeight, new Color(240, 245, 250),
						null, paddingBottom, false, false, null, level, null);
				page = context.page;
				cs = context.contentStream;
				y = context.yPosition;
			}

			// Adding document Meta list
			if (speciesField.getId() == 82) {
				float paddingBottom = 30;
				for (DocumentMeta doc : documentList) {
					// Adding title
					PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, doc.getTitle(), boldFont,
							13, MARGIN + 25, y, width - 50, lineHeight, new Color(240, 245, 250), null, paddingBottom,
							true, false, null, 1, url + "/document/show/" + doc.getId());

					page = context.page;
					cs = context.contentStream;
					y = context.yPosition;

					// Adding author name
					// Here fontSIze is 11 x is MARGIN+50 AND y is y+30
					drawFormattedLine(cs, doc.getUser(), primaryFont, 11, MARGIN + 50, y + 30, width - 30);
					// Adding author image
					addCircularImage(document, page, USER_IMAGE + doc.getPic(), MARGIN + 35, y + 34, lineHeight,
							getInitials(doc.getUser()));
				}
			}

			float paddingBottom = 5;

			for (Trait trait : speciesField.getTraits()) {
				// Trait name/title
				PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page,
						trait.getName() + (trait.getDataType().equals("DATE") ? " (" + trait.getUnits() + ")" : ""),
						boldFont, 11, MARGIN + 25, y, width - 50, lineHeight, new Color(240, 245, 250), null,
						paddingBottom, false, false, null, level, null);
				page = context.page;
				cs = context.contentStream;
				y = context.yPosition;

				// Grid layout for trait values
				float boxWidth = (CONTENT_WIDTH - 50 - 20) / 3;
				float boxHeight = 48;
				float boxSpacing = 10;
				float gridStartX = MARGIN + 25;

				int totalValues = trait.getValues().size();
				int rows = (int) Math.ceil(totalValues / 3.0);

				for (int row = 0; row < rows; row++) {
					if (y - boxHeight - 10 < 0) {
						cs.setNonStrokingColor(new Color(240, 245, 250));
						cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
						cs.fill();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN, y + 14);
						cs.lineTo(MARGIN, 0);
						cs.stroke();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
						cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
						cs.stroke();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + 10, y + 14);
						cs.lineTo(MARGIN + 10, 0);
						cs.stroke();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + CONTENT_WIDTH - 10, y + 14);
						cs.lineTo(MARGIN + CONTENT_WIDTH - 10, 0);
						cs.stroke();

						cs.close();
						PDPage newPage = new PDPage(PDRectangle.A4);
						document.addPage(newPage);
						page = newPage;
						y = PAGE_HEIGHT - 10;
						cs = new PDPageContentStream(document, newPage);
						addPageBackground(cs);
					}
					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setNonStrokingColor(new Color(240, 245, 250));
					cs.addRect(MARGIN, y - boxHeight - 10 + 15, CONTENT_WIDTH, 58);
					cs.fill();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + 10, y + 15);
					cs.lineTo(MARGIN + 10, y - boxHeight - 10 + 15);
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH - 10, y + 15);
					cs.lineTo(MARGIN + CONTENT_WIDTH - 10, y - boxHeight - 10 + 15);
					cs.stroke();

					for (int col = 0; col < 3; col++) {
						int valueIndex = row * 3 + col;
						if (valueIndex >= totalValues)
							break;

						if (trait.getDataType().equals("COLOR")) {
							String rgbValue = trait.getValues().get(valueIndex).getValue();
							String values = rgbValue.split("rgb\\(")[1];
							values = values.split("\\)")[0];
							String[] parts = values.split(",");
							int r = Integer.parseInt(parts[0].trim());
							int g = Integer.parseInt(parts[1].trim());
							int b = Integer.parseInt(parts[2].trim());
							float boxX = gridStartX + (col * (boxWidth + boxSpacing));
							float boxY = y - boxHeight + 15;
							cs.setNonStrokingColor(new Color(r, g, b));
							cs.addRect(boxX, boxY, boxWidth, boxHeight);
							cs.fill();
						} else {
							cs.setStrokingColor(new Color(222, 226, 230));
							float boxX = gridStartX + (col * (boxWidth + boxSpacing));
							float boxY = y - boxHeight + 15;
							cs.addRect(boxX, boxY, boxWidth, boxHeight);
							cs.stroke();

							cs.setNonStrokingColor(WHITE);
							cs.addRect(boxX, boxY, boxWidth, boxHeight);
							cs.fill();

							String text = "";
							if (trait.getDataType().equals("STRING")) {
								text = trait.getOptions().get(trait.getValues().get(valueIndex).getValueId());
							}
							if (trait.getDataType().equals("NUMERIC")) {
								text = trait.getValues().get(valueIndex).getValue();
							}
							if (trait.getDataType().equals("DATE")) {
								Date fromdate = trait.getValues().get(valueIndex).getFromDate();
								Date todate = trait.getValues().get(valueIndex).getoDate();
								SimpleDateFormat sdfName = new SimpleDateFormat("MMMM");
								SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
								if (trait.getUnits().equals("MONTH")) {
									text = sdfName.format(fromdate) + " - " + sdfName.format(todate);
								} else {
									text = yearFormat.format(fromdate) + " - " + yearFormat.format(todate);
								}
							}

							List<String> lines = List.of("");
							if (!text.isEmpty()) {
								lines = splitTextIntoLines(text.split("\\|").length > 1 ? text.split("\\|")[0] : text,
										primaryFont, 11, boxWidth - 10 - (text.split("\\|").length > 1 ? 45 : 0));
							}

							float textY = y - (boxHeight - (Math.min(lines.size(), 3) * 16)) / 2;

							cs.setNonStrokingColor(new Color(33, 37, 41));

							for (int l = 0; l < (Math.min(lines.size(), 3)); l++) {
								drawFormattedLine(cs, lines.get(l), primaryFont, 11,
										boxX + 5 + (text.split("\\|").length > 1 ? 45 : 0), textY + 3.5f - l * 16,
										boxWidth - 10 - (text.split("\\|").length > 1 ? 45 : 0));

								if (text.split("\\|").length > 1) {

									addImage(document, page, TRAITS_IMAGE + text.split("\\|")[1], boxX, y - 43 + 15,
											boxHeight - 10, true, true, 45, false);
								}
							}
						}
					}

					y = y - boxHeight - 10;

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN, y + boxHeight + 10 + 15);
					cs.lineTo(MARGIN, y + 15);
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH, y + boxHeight + 10 + 15);
					cs.lineTo(MARGIN + CONTENT_WIDTH, y + 15);
					cs.stroke();
				}

			}

			for (int i = 0; i < speciesField.getValues().size(); i++) {
				if (speciesField.getValues().get(i).getLanguageId().equals(languageId)) {
					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(0.5f);
					cs.moveTo(MARGIN + 15, y + 15);
					cs.lineTo(MARGIN + width - 15, y + 15);
					cs.stroke();
					String plainText = convertHtmlToText(speciesField.getValues().get(i).getDescription());
					String[] paragraphs = plainText.split("\n");
					for (String paragraph : paragraphs) {
						if (!paragraph.isEmpty()) {
							PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page,
									paragraph.startsWith("<h>") ? paragraph.substring(3) : paragraph,
									paragraph.startsWith("<h>") ? boldFont : primaryFont, 11, MARGIN + 25, y,
									width - 50, 16, new Color(240, 245, 250), null, 10, true, false, null, level, null);
							page = context.page;
							cs = context.contentStream;
							y = context.yPosition;
						}
					}

					y = y + 5;

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.setLineCapStyle(1); // Round cap
					cs.setLineDashPattern(new float[] { 2, 3 }, 0);
					cs.moveTo(MARGIN + 15, y + 15);
					cs.lineTo(MARGIN + width - 15, y + 15);
					cs.stroke();

					cs.setLineDashPattern(new float[] {}, 0);
					// Adding attributions
					List<String> attributionLines = splitTextIntoLines(
							speciesField.getValues().get(i).getAttributions(), primaryFont, 9, width - 175);
					int contributorLines = 0;
					for (String contributor : speciesField.getValues().get(i).getContributor()) {
						List<String> contriLines = splitTextIntoLines(contributor, primaryFont, 9, width - 175);
						contributorLines += contriLines.size();
					}

					List<String> licenseLines = splitTextIntoLines(speciesField.getValues().get(i).getLicense(),
							primaryFont, 9, width - 175);

					if (y - (attributionLines.size() * 16) - 10 - ((16 + 10) * contributorLines)
							- (licenseLines.size() * 16) - 20 < 0) {
						cs.setNonStrokingColor(new Color(240, 245, 250));
						cs.addRect(MARGIN, 0, CONTENT_WIDTH, y - 5 + 14 + (i == 0 ? 5 : 0));
						cs.fill();
						cs.setNonStrokingColor(WHITE);
						cs.addRect(MARGIN + 15, 0, CONTENT_WIDTH - 30, y - 5 + 14 + (i == 0 ? 5 : 0));
						cs.fill();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + 15, y - 5 + 14 + (i == 0 ? 5 : 0));
						cs.lineTo(MARGIN + 15, 0);
						cs.stroke();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + CONTENT_WIDTH - 15, y - 5 + 14 + (i == 0 ? 5 : 0));
						cs.lineTo(MARGIN + CONTENT_WIDTH - 15, 0);
						cs.stroke();

						if (level != 0) {
							cs.setStrokingColor(new Color(222, 226, 230));
							cs.setLineWidth(1);
							cs.moveTo(MARGIN + 10, y - 5 + 14 + (i == 0 ? 5 : 0));
							cs.lineTo(MARGIN + 10, 0);
							cs.stroke();

							cs.setStrokingColor(new Color(222, 226, 230));
							cs.setLineWidth(1);
							cs.moveTo(MARGIN + CONTENT_WIDTH - 10, y - 5 + 14 + (i == 0 ? 5 : 0));
							cs.lineTo(MARGIN + CONTENT_WIDTH - 10, 0);
							cs.stroke();

							if (level != 1) {
								cs.setStrokingColor(new Color(222, 226, 230));
								cs.setLineWidth(1);
								cs.moveTo(MARGIN + 13, y - 5 + 14 + (i == 0 ? 5 : 0));
								cs.lineTo(MARGIN + 13, 0);
								cs.stroke();

								cs.setStrokingColor(new Color(222, 226, 230));
								cs.setLineWidth(1);
								cs.moveTo(MARGIN + CONTENT_WIDTH - 13, y - 5 + 14 + (i == 0 ? 5 : 0));
								cs.lineTo(MARGIN + CONTENT_WIDTH - 13, 0);
								cs.stroke();
							}
						}

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN, y - 5 + 14 + (i == 0 ? 5 : 0));
						cs.lineTo(MARGIN, 0);
						cs.stroke();

						cs.setStrokingColor(new Color(222, 226, 230));
						cs.setLineWidth(1);
						cs.moveTo(MARGIN + CONTENT_WIDTH, y - 5 + 14 + (i == 0 ? 5 : 0));
						cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
						cs.stroke();
						cs.close();
						PDPage newPage = new PDPage(PDRectangle.A4);
						document.addPage(newPage);
						page = newPage;
						y = PAGE_HEIGHT - 16;
						cs = new PDPageContentStream(document, newPage);
						addPageBackground(cs);
					}

					PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page,
							speciesField.getValues().get(i).getAttributions(), primaryFont, 9, MARGIN + 155, y,
							width - 175, 16, new Color(240, 245, 250), "*Attributions", 5, true, false, null, level,
							null);
					page = context.page;
					cs = context.contentStream;
					y = context.yPosition;

					float j = 0;
					// Adding Contributors
					for (String contributor : speciesField.getValues().get(i).getContributor()) {
						context = drawTextWithWordWrapAndOverflow(cs, document, page, contributor, primaryFont, 9,
								MARGIN + 155, y, width - 175, 16, new Color(240, 245, 250),
								j == 0 ? "*Contributors" : "", 5, true, false, null, level, null);
						page = context.page;
						cs = context.contentStream;
						y = context.yPosition;
						j = j + 1;
					}

					// Adding License
					context = drawTextWithWordWrapAndOverflow(cs, document, page,
							speciesField.getValues().get(i).getLicense(), primaryFont, 9, MARGIN + 155, y, width - 175,
							16, new Color(240, 245, 250), "*License", 15, true, true, null, level, null);
					page = context.page;
					cs = context.contentStream;
					y = context.yPosition;

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + 15, y + 25);
					cs.lineTo(MARGIN + width - 15, y + 25);
					cs.stroke();
				}
			}

			for (int i = 0; i < speciesField.getChildField().size(); i++) {
				PageContext ctx = addSpeciesFieldGroup(document, cs, page, speciesField.getChildField().get(i),
						level + 1, y, Map, documentList, url, languageId);
				cs = ctx.contentStream;
				page = ctx.page;
				y = ctx.yPosition;
			}
		}

		return new PageContext(page, cs, y);
	}

	public static String getInitials(String name) {
		if (name == null || name.trim().isEmpty()) {
			return "";
		}

		String[] parts = name.split(" ");
		StringBuilder initials = new StringBuilder();

		for (String part : parts) {
			if (!part.isEmpty()) {
				initials.append(part.charAt(0));
			}
		}

		return initials.toString().toUpperCase();
	}

	private static PageContext addReferencesSection(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData, float currentLeftY) throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY - 40;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			// Adding continuation background and borders
			cs.setNonStrokingColor(WHITE);
			cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
			cs.fill();
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN, y + 14);
			cs.lineTo(MARGIN, 0);
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
			cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
			cs.stroke();
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			// Adding Page background
			addPageBackground(cs);
			drawSectionCard(cs, "References", 0, currentY);
			y = currentY - 50;
		} else {
			drawSectionCard(cs, "References", 0, sectionStartY);
			y = y - 10;
		}

		for (Map.Entry<String, List<String>> entry : speciesData.getReferences().entrySet()) {
			String language = entry.getKey();
			List<String> names = entry.getValue();

			PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, language, boldFont, 11,
					MARGIN + 15, y, width - 30, 16, new Color(255, 255, 255), null, 5, false, false, null, 0, null);
			page = context.page;
			cs = context.contentStream;
			y = context.yPosition;

			for (int j = 0; j < names.size(); j++) {
				String commonName = names.get(j);

				cs.setNonStrokingColor(new Color(33, 37, 41));

				// Changed row colors
				Color rowColor = new Color(255, 255, 255);

				context = drawTextWithWordWrapAndOverflow(cs, document, page,
						Integer.toString(j + 1) + ". " + commonName, primaryFont, 11, MARGIN + 15, y, width - 30, 16,
						rowColor, null, 5, false, false, null, 0, null);
				page = context.page;
				cs = context.contentStream;
				y = context.yPosition;
			}
		}

		PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, "Common references", boldFont, 11,
				MARGIN + 15, y, width - 30, 16, new Color(255, 255, 255), null, 5, false, false, null, 0, null);
		page = context.page;
		cs = context.contentStream;
		y = context.yPosition;

		for (int j = 0; j < speciesData.getCommonReferences().size(); j++) {
			String commonName = speciesData.getCommonReferences().get(j);

			cs.setNonStrokingColor(new Color(33, 37, 41));

			// Changed row colors
			Color rowColor = new Color(255, 255, 255);

			context = drawTextWithWordWrapAndOverflow(cs, document, page, Integer.toString(j + 1) + ". " + commonName,
					primaryFont, 11, MARGIN + 15, y, width - 30, 16, rowColor, null, 5, false, false, null, 0, null);
			page = context.page;
			cs = context.contentStream;
			y = context.yPosition;
		}

		// Adding bottom line
		cs.setStrokingColor(new Color(222, 226, 230));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + width, y + 15);
		cs.stroke();

		return new PageContext(page, cs, y - 10);
	}

	private static PageContext addTemporalObservedOn(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData, float currentLeftY) throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY - 40;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			cs.setNonStrokingColor(WHITE);
			cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
			cs.fill();
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN, y + 14);
			cs.lineTo(MARGIN, 0);
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
			cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
			cs.stroke();
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			addPageBackground(cs);
			drawSectionCard(cs, "Temporal Observed On", 0, currentY);
			y = currentY - 50;
		} else {
			drawSectionCard(cs, "Temporal Observed On", 0, sectionStartY);
			y = y - 10;
		}

		if (speciesData.getChartImage() != null && !speciesData.getChartImage().trim().isEmpty()) {
			try {
				String imageData = speciesData.getChartImage();
				if (imageData.contains(",")) {
					imageData = imageData.split(",")[1];
				}

				byte[] imageBytes = Base64.getDecoder().decode(imageData);
				PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "chart");

				float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
				float imageWidth = CONTENT_WIDTH - 30;
				float height = aspectRatio * imageWidth;
				if (height > PAGE_HEIGHT - 75) {
					height = PAGE_HEIGHT - 75;
					imageWidth = (float) height / aspectRatio;
				}
				float x = (float) (CONTENT_WIDTH - imageWidth) / 2;

				if (y - height - 25 < 0) {
					cs.setNonStrokingColor(WHITE);
					cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
					cs.fill();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN, y + 14);
					cs.lineTo(MARGIN, 0);
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
					cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
					cs.stroke();
					cs.close();
					PDPage newPage = new PDPage(PDRectangle.A4);
					document.addPage(newPage);
					page = newPage;
					y = PAGE_HEIGHT - 10;
					cs = new PDPageContentStream(document, newPage);
					addPageBackground(cs);
				}

				addBase64Image(document, page, pdImage, MARGIN + x, y - height, imageWidth, height);

				cs.setNonStrokingColor(WHITE);
				cs.addRect(MARGIN, y - height - 25 + 15, CONTENT_WIDTH, height + 25);
				cs.fill();

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN, y + 15);
				cs.lineTo(MARGIN, y + 15 - height - 25);
				cs.stroke();

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN + CONTENT_WIDTH, y + 15);
				cs.lineTo(MARGIN + CONTENT_WIDTH, y + 15 - height - 25);
				cs.stroke();
				y = y - height - 25;

			} catch (Exception e) {
				drawFallbackRectangle(cs, MARGIN + 15, y, CONTENT_WIDTH - 30, 100, "Invalid image");
			}
		}

		cs.setStrokingColor(new Color(222, 226, 230));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + width, y + 15);
		cs.stroke();

		return new PageContext(page, cs, y - 10);
	}

	private static PageContext addTraitsPerMonth(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData, float currentLeftY) throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY - 40;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			cs.setNonStrokingColor(WHITE);
			cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
			cs.fill();
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN, y + 14);
			cs.lineTo(MARGIN, 0);
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
			cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
			cs.stroke();
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			addPageBackground(cs);
			drawSectionCard(cs, "Traits Distribution", 0, currentY);
			y = currentY - 50;
		} else {
			drawSectionCard(cs, "Traits Distribution", 0, sectionStartY);
			y = y - 10;
		}

		if (speciesData.getTraitsChart() != null && !speciesData.getTraitsChart().trim().isEmpty()) {
			try {
				String imageData = speciesData.getTraitsChart();
				if (imageData.contains(",")) {
					imageData = imageData.split(",")[1];
				}

				byte[] imageBytes = Base64.getDecoder().decode(imageData);
				PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "chart");

				float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
				float imageWidth = CONTENT_WIDTH - 30;
				float height = aspectRatio * imageWidth;
				if (height > PAGE_HEIGHT - 75) {
					height = PAGE_HEIGHT - 75;
					imageWidth = (float) height / aspectRatio;
				}
				float x = (float) (CONTENT_WIDTH - imageWidth) / 2;

				if (y - height - 25 < 0) {
					cs.setNonStrokingColor(WHITE);
					cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
					cs.fill();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN, y + 14);
					cs.lineTo(MARGIN, 0);
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
					cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
					cs.stroke();
					cs.close();
					PDPage newPage = new PDPage(PDRectangle.A4);
					document.addPage(newPage);
					page = newPage;
					y = PAGE_HEIGHT - 10;
					cs = new PDPageContentStream(document, newPage);
					addPageBackground(cs);
				}

				addBase64Image(document, page, pdImage, MARGIN + x, y - height, imageWidth, height);

				cs.setNonStrokingColor(WHITE);
				cs.addRect(MARGIN, y - height - 25 + 15, CONTENT_WIDTH, height + 25);
				cs.fill();

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN, y + 15);
				cs.lineTo(MARGIN, y + 15 - height - 25);
				cs.stroke();

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN + CONTENT_WIDTH, y + 15);
				cs.lineTo(MARGIN + CONTENT_WIDTH, y + 15 - height - 25);
				cs.stroke();
				y = y - height - 25;

			} catch (Exception e) {
				drawFallbackRectangle(cs, MARGIN + 15, y, CONTENT_WIDTH - 30, 100, "Invalid image");
			}
		}

		cs.setStrokingColor(new Color(222, 226, 230));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + width, y + 15);
		cs.stroke();
		return new PageContext(page, cs, y - 10);
	}

	private PageContext addAdditionalImages(PDDocument document, PDPageContentStream cs, PDPage page,
			SpeciesDownload speciesData, float currentLeftY, PDAnnotationLink link, PDPage sourcePage)
			throws Exception {
		float width = CONTENT_WIDTH;
		float y = currentLeftY - 40;
		float sectionStartY = currentLeftY;

		if (y < 0) {
			cs.setNonStrokingColor(WHITE);
			cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
			cs.fill();
			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN, y + 14);
			cs.lineTo(MARGIN, 0);
			cs.stroke();

			cs.setStrokingColor(new Color(222, 226, 230));
			cs.setLineWidth(1);
			cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
			cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
			cs.stroke();
			cs.close();
			PDPage newPage = new PDPage(PDRectangle.A4);
			document.addPage(newPage);
			page = newPage;
			currentY = PAGE_HEIGHT;
			cs = new PDPageContentStream(document, newPage);
			addPageBackground(cs);
			drawSectionCard(cs, "Additional Images", 0, currentY);
			y = currentY - 50;
		} else {
			drawSectionCard(cs, "Additional Images", 0, sectionStartY);
			y = y - 10;
		}

		PDActionGoTo action = new PDActionGoTo();
		PDPageFitDestination destination = new PDPageFitDestination();
		destination.setPage(page);
		action.setDestination(destination);
		link.setAction(action);

		sourcePage.getAnnotations().add(link);

		float boxWidth = (CONTENT_WIDTH - 50 - 10) / 2;
		float boxSpacing = 10;
		float gridStartX = MARGIN + 25;

		int totalValues = speciesData.getResourceData().size();
		int rows = (int) Math.ceil(totalValues / 2.0);

		for (int row = 0; row < rows; row++) {
			int index = row * 2;

			if (index < totalValues) {
				File imageFile = new File(SPECIES_IMAGE_PATH + speciesData.getResourceData().get(index));
				float maxHeight = boxWidth;
				if (imageFile.exists() && imageFile.canRead() && imageFile.length() > 0) {
					try {
						PDImageXObject pdImage = PDImageXObject.createFromFile(
								SPECIES_IMAGE_PATH + speciesData.getResourceData().get(index), document);
						float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
						maxHeight = boxWidth * aspectRatio;
					} catch (IOException e) {
						logger.error("Failed to load image (actual format may differ from extension): "
								+ imageFile.getPath());
					}
				}

				if ((index + 1) < totalValues) {
					imageFile = new File(SPECIES_IMAGE_PATH + speciesData.getResourceData().get(index + 1));
					if (imageFile.exists() && imageFile.canRead() && imageFile.length() > 0) {
						try {
							PDImageXObject pdImage = PDImageXObject.createFromFile(
									SPECIES_IMAGE_PATH + speciesData.getResourceData().get(index + 1), document);
							float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
							float image2Height = boxWidth * aspectRatio;
							if (image2Height > maxHeight) {
								maxHeight = image2Height;
							}
						} catch (IOException e) {
							logger.error("Failed to load image (actual format may differ from extension): "
									+ imageFile.getPath());
						}
					}
				}
				if (y - maxHeight - 10 < 0) {
					cs.setNonStrokingColor(WHITE);
					cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
					cs.fill();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN, y + 14);
					cs.lineTo(MARGIN, 0);
					cs.stroke();

					cs.setStrokingColor(new Color(222, 226, 230));
					cs.setLineWidth(1);
					cs.moveTo(MARGIN + CONTENT_WIDTH, y + 14);
					cs.lineTo(MARGIN + CONTENT_WIDTH, 0);
					cs.stroke();

					cs.close();
					PDPage newPage = new PDPage(PDRectangle.A4);
					document.addPage(newPage);
					page = newPage;
					y = PAGE_HEIGHT - 10;
					cs = new PDPageContentStream(document, newPage);
					addPageBackground(cs);
				}
				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setNonStrokingColor(WHITE);
				cs.addRect(MARGIN, y - maxHeight - 10 + 15, CONTENT_WIDTH, maxHeight + 10);
				cs.fill();

				for (int col = 0; col < 2; col++) {
					int valueIndex = row * 2 + col;
					if (valueIndex >= totalValues)
						break;
					float boxX = gridStartX + (col * (boxWidth + boxSpacing));
					float boxY = y - maxHeight + 15;
					cs.setNonStrokingColor(BLACK);

					addImage(document, page, SPECIES_IMAGE_PATH + speciesData.getResourceData().get(index), boxX,
							boxY - 5, maxHeight, true, true, boxWidth, true);

					index = index + 1;
				}

				y = y - maxHeight - 10;

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN, y + maxHeight + 10 + 15);
				cs.lineTo(MARGIN, y + 15);
				cs.stroke();

				cs.setStrokingColor(new Color(222, 226, 230));
				cs.setLineWidth(1);
				cs.moveTo(MARGIN + CONTENT_WIDTH, y + maxHeight + 10 + 15);
				cs.lineTo(MARGIN + CONTENT_WIDTH, y + 15);
				cs.stroke();
			}
		}
		cs.setStrokingColor(new Color(222, 226, 230));
		cs.setLineWidth(0.5f);
		cs.moveTo(MARGIN, y + 15);
		cs.lineTo(MARGIN + width, y + 15);
		cs.stroke();
		return new PageContext(page, cs, y - 10);
	}

	public static void addBase64Image(PDDocument document, PDPage page, PDImageXObject pdImage, float x, float y,
			float width, float height) throws IOException {

		try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
				PDPageContentStream.AppendMode.APPEND, true)) {

			contentStream.drawImage(pdImage, x, y, width, height);

		}
	}

	public static void addCircularImage(PDDocument document, PDPage page, String imagePath, float centerX,
			float centerY, float diameter, String name) throws IOException {

		File imageFile = new File(imagePath);

		try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
				PDPageContentStream.AppendMode.APPEND, true, true)) {

			if (imageFile.exists() && imageFile.canRead() && imageFile.length() > 0) {
				try {
					PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);

					float x = centerX - diameter / 2;
					float y = centerY - diameter / 2;

					contentStream.saveGraphicsState();

					createCircularClip(contentStream, centerX, centerY, diameter / 2);

					contentStream.drawImage(pdImage, x, y, diameter, diameter);

					contentStream.restoreGraphicsState();

				} catch (Exception e) {

					drawFallbackCircle(contentStream, centerX, centerY, diameter / 2, name);
				}
			} else {
				drawFallbackCircle(contentStream, centerX, centerY, diameter / 2, name);
			}
		}
	}

//Helper method to create circular clipping path
	private static void createCircularClip(PDPageContentStream contentStream, float centerX, float centerY,
			float radius) throws IOException {
		final float k = 0.552284749831f;

		contentStream.moveTo(centerX - radius, centerY);

		contentStream.curveTo(centerX - radius, centerY + k * radius, centerX - k * radius, centerY + radius, centerX,
				centerY + radius);

		contentStream.curveTo(centerX + k * radius, centerY + radius, centerX + radius, centerY + k * radius,
				centerX + radius, centerY);

		contentStream.curveTo(centerX + radius, centerY - k * radius, centerX + k * radius, centerY - radius, centerX,
				centerY - radius);

		contentStream.curveTo(centerX - k * radius, centerY - radius, centerX - radius, centerY - k * radius,
				centerX - radius, centerY);

		contentStream.closePath();
		contentStream.clip();
	}

	private static void drawFallbackCircle(PDPageContentStream contentStream, float centerX, float centerY,
			float radius, String text) throws IOException {

		contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
		createCircularPath(contentStream, centerX, centerY, radius);
		contentStream.fill();

		contentStream.setStrokingColor(Color.DARK_GRAY);
		contentStream.setLineWidth(1);
		createCircularPath(contentStream, centerX, centerY, radius);
		contentStream.stroke();

		text = text.length() < 3 ? text : text.substring(0, 2);
		contentStream.setNonStrokingColor(Color.DARK_GRAY);
		contentStream.beginText();
		contentStream.setFont(boldFont, radius);
		contentStream.newLineAtOffset((centerX - radius) + (text.length() > 1 ? 2.5f : 3.5f), centerY - radius / 3);
		contentStream.showText(text);
		contentStream.endText();
	}

	private static void createCircularPath(PDPageContentStream contentStream, float centerX, float centerY,
			float radius) throws IOException {
		final float k = 0.552284749831f;

		contentStream.moveTo(centerX - radius, centerY);

		contentStream.curveTo(centerX - radius, centerY + k * radius, centerX - k * radius, centerY + radius, centerX,
				centerY + radius);

		contentStream.curveTo(centerX + k * radius, centerY + radius, centerX + radius, centerY + k * radius,
				centerX + radius, centerY);

		contentStream.curveTo(centerX + radius, centerY - k * radius, centerX + k * radius, centerY - radius, centerX,
				centerY - radius);

		contentStream.curveTo(centerX - k * radius, centerY - radius, centerX - radius, centerY - k * radius,
				centerX - radius, centerY);

		contentStream.closePath();
	}

}
