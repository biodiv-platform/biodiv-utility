/**
 * 
 */
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
import com.strandls.utility.pojo.FieldValue;
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

import net.minidev.json.JSONArray;

/**
 * @author Abhishek Rudra
 *
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

	// Current Y position tracker
	private static float currentY;

	private static PDFont fallbackFont;
	
	// ============================================================================
	// CONSTANTS & CONFIGURATION
	// ============================================================================

	private static final float HEADER_BOTTOM_MARGIN = 25f;
	private static final float IMAGE_GALLERY_MIN_SPACE = 200f;
	private static final float VIEW_MORE_OFFSET_X = 30f;
	private static final float VIEW_MORE_OFFSET_Y = 30f;
	private static final float VIEW_MORE_WIDTH = 50f;
	private static final float VIEW_MORE_HEIGHT = 10f;
	private static final int VIEW_MORE_FONT_SIZE = 10;
	private static final String FALLBACK_FONT_PATH = "/usr/share/fonts/truetype/freefont/FreeSerif.ttf";
	private static final String LOGO_PATH = "/app/data/biodiv/logo/IBP.png";

	// Color constants
	private static final Color LIGHT_BLUE = new Color(246, 250, 252);
	private static final Color BANNER_BACKGROUND = new Color(199, 212, 224);
	private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
	private static final Color BORDER_GRAY = new Color(222, 226, 230);
	private static final Color ROW_ALTERNATE = new Color(240, 245, 250);
	private static final Color SECTION_HEADER_BG = new Color(250, 248, 245);
	private static final Color GALLERY_DARK = new Color(45, 55, 70);

	// Badge colors
	private static final Color BADGE_ACCEPTED_BG = new Color(220, 252, 231);
	private static final Color BADGE_ACCEPTED_TEXT = new Color(17, 105, 50);
	private static final Color BADGE_SYNONYM_BG = new Color(243, 232, 255);
	private static final Color BADGE_SYNONYM_TEXT = new Color(100, 27, 163);
	private static final Color BADGE_HELP_BG = new Color(254, 226, 226);
	private static final Color BADGE_HELP_TEXT = new Color(153, 25, 25);

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
			for (Entry<Long, String> editTranslation : editTranslationsData.entrySet()) {
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
	        
	        // Initialize fallback font for multilingual support
	        loadFallbackFont(document);
	        
	        // Create and configure initial page
	        PDPage page = createInitialPage(document);
	        PDPageContentStream contentStream = new PDPageContentStream(document, page);
	        
	        // Build document content
	        PageContext ctx = buildDocumentContent(document, page, contentStream, speciesData);
	        
	        // Save and return
	        ctx.contentStream.close();
	        document.save(baos);
	        document.close();
	        
	        return baos.toByteArray();
	        
	    } catch (Exception e) {
	        logger.error("Error while generating PDF: {}", e.getMessage(), e);
	        closeDocumentSafely(document);
	        return null;
	    }
	}
	
	// ============================================================================
	// DOCUMENT BUILDING
	// ============================================================================

	private PageContext buildDocumentContent(PDDocument document, PDPage page, 
	                                        PDPageContentStream contentStream, 
	                                        SpeciesDownload speciesData) throws Exception {
	    PDPage sourcePage = page;
	    
	    // Set page background
	    setPageBackground(contentStream);
	    currentY = PAGE_HEIGHT;
	    
	    // Add header banner
	    addHeaderBanner(document, contentStream, page, speciesData);
	    currentY -= HEADER_BOTTOM_MARGIN;
	    contentStream.close();
	    
	    // Check space for image gallery
	    PageContext ctx = checkAndCreateNewPage(document, page, null, IMAGE_GALLERY_MIN_SPACE);
	    page = ctx.page;
	    contentStream = ctx.contentStream;
	    
	    // Add image gallery
	    addImageGallery(document, page, contentStream, speciesData);
	    float currentLeftY = currentY;
	    
	    // Add "View More" link if multiple images exist
	    PDAnnotationLink link = null;
	    if (hasMultipleImages(speciesData)) {
	        addViewMoreText(contentStream);
	        link = createViewMoreLink();
	        sourcePage = page;
	    }
	    
	    // Add taxonomy section
	    ctx = addTaxonomySection(document, contentStream, page, speciesData, currentLeftY);
	    contentStream = ctx.contentStream;
	    page = ctx.page;
	    currentLeftY = ctx.yPosition;
	    
	    // Add synonyms if available
	    if (hasData(speciesData.getSynonyms())) {
	        ctx = addSynonymSection(document, contentStream, page, speciesData, currentLeftY);
	        contentStream = ctx.contentStream;
	        page = ctx.page;
	        currentLeftY = ctx.yPosition;
	    }
	    
	    // Add common names if available
	    if (hasData(speciesData.getCommonNames())) {
	        ctx = addCommonNamesSection(document, contentStream, page, speciesData, currentLeftY);
	        contentStream = ctx.contentStream;
	        page = ctx.page;
	        currentLeftY = ctx.yPosition;
	    }
	    
	    // Add species field data
	    for (SpeciesField speciesFieldData : speciesData.getFieldData()) {
	        ctx = addSpeciesFieldSection(document, contentStream, page, speciesFieldData,
	                currentLeftY, speciesData.getObservationMap(), 
	                speciesData.getDocumentMetaList(),
	                speciesData.getUrl(), speciesData.getLanguageId());
	        contentStream = ctx.contentStream;
	        page = ctx.page;
	        currentLeftY = ctx.yPosition;
	    }
	    
	    // Add references
	    ctx = addReferencesSection(document, contentStream, page, speciesData, currentLeftY);
	    contentStream = ctx.contentStream;
	    page = ctx.page;
	    currentLeftY = ctx.yPosition;
	    
	    // Add charts if available
	    if (hasChartImage(speciesData)) {
	        ctx = addTemporalObservedOn(document, contentStream, page, speciesData, currentLeftY);
	        contentStream = ctx.contentStream;
	        page = ctx.page;
	        currentLeftY = ctx.yPosition;
	    }
	    
	    if (hasTraitsChart(speciesData)) {
	        ctx = addTraitsPerMonth(document, contentStream, page, speciesData, currentLeftY);
	        contentStream = ctx.contentStream;
	        page = ctx.page;
	        currentLeftY = ctx.yPosition;
	    }
	    
	    // Add additional images if available
	    if (hasMultipleImages(speciesData)) {
	        ctx = addAdditionalImages(document, contentStream, page, speciesData, 
	                                  currentLeftY, link, sourcePage);
	        contentStream = ctx.contentStream;
	        page = ctx.page;
	        currentLeftY = ctx.yPosition;
	    }
	    
	    return ctx;
	}
	
	// ============================================================================
	// HELPER CLASSES
	// ============================================================================

	/**
	 * Context object for tracking page state across document generation
	 */
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
	    
	    void close() throws IOException {
	        if (contentStream != null) {
	            contentStream.close();
	        }
	    }
	}

	/**
	 * Represents a formatted text segment with specific font styling
	 */
	private static class TextSegment {
	    private final String text;
	    private final PDFont font;
	    private final float width;
	    
	    public TextSegment(String text, PDFont font, float width) {
	        this.text = text;
	        this.font = font;
	        this.width = width;
	    }
	    
	    public String getText() { return text; }
	    public PDFont getFont() { return font; }
	    public float getWidth() { return width; }
	}

	// ============================================================================
	// INITIALIZATION & SETUP
	// ============================================================================

	private void loadFallbackFont(PDDocument document) throws IOException {
	    File fontFile = new File(FALLBACK_FONT_PATH);
	    
	    if (!fontFile.exists()) {
	        logger.warn("Fallback font not found at {}, using default", FALLBACK_FONT_PATH);
	        fallbackFont = primaryFont;
	        return;
	    }
	    
	    try {
	        fallbackFont = PDType0Font.load(document, fontFile);
	    } catch (IOException e) {
	        logger.error("Failed to load fallback font: {}", e.getMessage());
	        fallbackFont = primaryFont;
	    }
	}

	private PDPage createInitialPage(PDDocument document) {
	    PDPage page = new PDPage(PDRectangle.A4);
	    document.addPage(page);
	    return page;
	}

	private static void setPageBackground(PDPageContentStream cs) throws IOException {
	    cs.setNonStrokingColor(LIGHT_BLUE);
	    cs.addRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
	    cs.fill();
	}

	private void closeDocumentSafely(PDDocument document) {
	    if (document != null) {
	        try {
	            document.close();
	        } catch (Exception ex) {
	            logger.error("Couldn't close document: {}", ex.getMessage(), ex);
	        }
	    }
	}

	// ============================================================================
	// VALIDATION HELPERS
	// ============================================================================

	private static boolean hasData(List<?> list) {
	    return list != null && !list.isEmpty();
	}

	private boolean hasData(Map<?, ?> map) {
	    return map != null && !map.isEmpty();
	}

	private boolean hasMultipleImages(SpeciesDownload speciesData) {
	    return speciesData.getResourceData() != null && 
	           speciesData.getResourceData().size() > 1;
	}

	private boolean hasChartImage(SpeciesDownload speciesData) {
	    return speciesData.getChartImage() != null && 
	           !speciesData.getChartImage().isEmpty();
	}

	private boolean hasTraitsChart(SpeciesDownload speciesData) {
	    return speciesData.getTraitsChart() != null && 
	           !speciesData.getTraitsChart().isEmpty();
	}

	// ============================================================================
	// PAGE MANAGEMENT
	// ============================================================================

	/**
	 * Checks if a new page is needed and creates one if necessary
	 */
	private static PageContext checkAndCreateNewPage(PDDocument document, 
	                                                 PDPage currentPage,
	                                                 PDPageContentStream currentStream, 
	                                                 float neededSpace) throws Exception {
	    //If don't have needed space, then add new Page
		if (currentY - neededSpace < 0) {
	        if (currentStream != null) {
	            currentStream.close();
	        }
	        
	        PDPage newPage = new PDPage(PDRectangle.A4);
	        document.addPage(newPage);
	        currentY = PAGE_HEIGHT;
	        
	        PDPageContentStream newStream = new PDPageContentStream(document, newPage);
	        setPageBackground(newStream);
	        
	        return new PageContext(newPage, newStream);
	    }
	    
	    if (currentStream == null) {
	        currentStream = new PDPageContentStream(document, currentPage, 
	                                               PDPageContentStream.AppendMode.APPEND, true);
	    }
	    
	    return new PageContext(currentPage, currentStream);
	}

	// ============================================================================
	// HEADER SECTION
	// ============================================================================

	private static float addHeaderBanner(PDDocument document, 
	                                    PDPageContentStream cs, 
	                                    PDPage page,
	                                    SpeciesDownload speciesData) throws Exception {
	    // Calculate banner height based on title length
	    float bannerHeight = calculateBannerHeight(speciesData.getTitle());
	    
	    // Draw banner background
	    drawBannerBackground(cs, bannerHeight);
	    
	    // Add logo
	    addLogo(document, page);
	    
	    // Add portal name and date
	    addPortalInfo(cs);
	    addDownloadDate(cs);
	    
	    // Add species name
	    currentY = drawSpeciesTitle(cs, speciesData.getTitle());
	    
	    // Add status badge
	    addStatusBadge(cs, speciesData.getBadge());
	    
	    // Add species group icon
	    addSpeciesGroupIcon(document, page, speciesData.getSpeciesGroup());
	    
	    currentY = PAGE_HEIGHT - bannerHeight;
	    return currentY;
	}

	private static float calculateBannerHeight(String title) throws IOException {
		float fontSize = 32;
		float maxWidth = PAGE_WIDTH -80;
		float otherDetailsHeight = 170;
	    List<String> lines = splitTextIntoLines(title, PDType1Font.HELVETICA_BOLD, 
	                                           fontSize, maxWidth);
	    return (lines.size() * (fontSize+3)) + otherDetailsHeight;
	}

	private static void drawBannerBackground(PDPageContentStream cs, float height) 
	        throws IOException {
	    cs.setNonStrokingColor(BANNER_BACKGROUND);
	    //0 is x coordinate and y is PAGE_HEIGHT - height
	    cs.addRect(0, PAGE_HEIGHT - height, PAGE_WIDTH, height);
	    cs.fill();
	}

	private static void addLogo(PDDocument document, PDPage page) throws IOException {
		float x = MARGIN;
		float y = currentY-70;
		float logoHeight = 60;
	    addImage(document, page, LOGO_PATH, x, y, logoHeight, 
	            true, false, CONTENT_WIDTH, false);
	}

	private static void addPortalInfo(PDPageContentStream cs) throws IOException {
		float x = MARGIN + 158;
		float y = currentY - 45;
	    cs.setNonStrokingColor(TEXT_PRIMARY);
	    cs.beginText();
	    cs.setFont(primaryFont, 14);
	    cs.newLineAtOffset(x, y);
	    cs.showText("India Biodiversity Portal");
	    cs.endText();
	}

	private static void addDownloadDate(PDPageContentStream cs) throws IOException {
	    String formattedDate = LocalDate.now()
	                                   .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	    float x = MARGIN + CONTENT_WIDTH - 80;
	    float y = currentY - 45;
	    cs.beginText();
	    cs.setFont(primaryFont, 14);
	    cs.newLineAtOffset(x, y);
	    cs.showText(formattedDate);
	    cs.endText();
	}

	private static float drawSpeciesTitle(PDPageContentStream cs, String title) 
	        throws IOException {
	    return drawTextWithWordWrap(cs, title, PDType1Font.HELVETICA_BOLD, 32, 
	                               40, PAGE_HEIGHT - 110, PAGE_WIDTH - 80, 35, null);
	}

	private static void addStatusBadge(PDPageContentStream cs, String badge) 
	        throws IOException {
	    float badgeX = 40;
	    float badgeY = currentY;
	    float badgeWidth = 80;
	    float badgeHeight = 16;
	    
	    // Determine badge colors
	    Color bgColor = getBadgeBackgroundColor(badge);
	    Color textColor = getBadgeTextColor(badge);
	    String badgeText = getBadgeText(badge);
	    
	    // Draw badge background
	    cs.setNonStrokingColor(bgColor);
	    cs.addRect(badgeX, badgeY, badgeWidth, badgeHeight);
	    cs.fill();
	    
	    // Draw badge text
	    cs.setNonStrokingColor(textColor);
	    cs.beginText();
	    cs.setFont(primaryFont, 11);
	    cs.newLineAtOffset(badgeX + 8, badgeY + 5);
	    cs.showText(badgeText);
	    cs.endText();
	}

	private static Color getBadgeBackgroundColor(String badge) {
	    switch (badge) {
	        case "ACCEPTED": return BADGE_ACCEPTED_BG;
	        case "SYNONYM": return BADGE_SYNONYM_BG;
	        default: return BADGE_HELP_BG;
	    }
	}

	private static Color getBadgeTextColor(String badge) {
	    switch (badge) {
	        case "ACCEPTED": return BADGE_ACCEPTED_TEXT;
	        case "SYNONYM": return BADGE_SYNONYM_TEXT;
	        default: return BADGE_HELP_TEXT;
	    }
	}

	private static String getBadgeText(String badge) {
	    switch (badge) {
	        case "ACCEPTED": return "Accepted";
	        case "SYNONYM": return "Synonym";
	        default: return "Help Identify";
	    }
	}

	private static void addSpeciesGroupIcon(PDDocument document, PDPage page, 
	                                       String speciesGroup) throws IOException {
		float imageX = 40;
		float imageY = currentY - 50;
		float height = 40;
	    String iconPath = "/app/data/biodiv/sgroup/speciesGroups/" + 
	                     speciesGroup.toLowerCase() + ".png";
	    addImage(document, page, iconPath, imageX, imageY, height, 
	            false, false, CONTENT_WIDTH, false);
	}

	// ============================================================================
	// IMAGE GALLERY SECTION
	// ============================================================================

	private static void addImageGallery(PDDocument document, PDPage page, 
	                                   PDPageContentStream cs,
	                                   SpeciesDownload species) throws Exception {
	    float galleryHeight = 360;
	    float galleryY = currentY - galleryHeight;
	    
	    // Draw gallery background
	    cs.setNonStrokingColor(GALLERY_DARK);
	    //Here MARGIN is x
	    cs.addRect(MARGIN, galleryY, CONTENT_WIDTH, galleryHeight);
	    cs.fill();
	    
	    if (!hasData(species.getResourceData())) {
	        drawPlaceholderImage(cs, galleryY);
	    } else {
	        addMainGalleryImage(document, page, species, galleryY, galleryHeight);
	    }
	    
	    //Adding a padding of 10
	    currentY = galleryY - 10;
	}

	private static void drawPlaceholderImage(PDPageContentStream cs, float galleryY) 
	        throws IOException {
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
	    cs.setFont(PDType1Font.HELVETICA_BOLD, 80);
	    cs.newLineAtOffset(circleCenterX - 20, circleCenterY - 25);
	    cs.showText("?");
	    cs.endText();
	    
	    // Caption
	    addPlaceholderCaption(cs, circleCenterY);
	}

	private static void addPlaceholderCaption(PDPageContentStream cs, float centerY) 
	        throws IOException {
	    String caption = "No image available - Placeholder shown";
	    float captionWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(caption) / 1000 * 10;
	    
	    cs.setNonStrokingColor(new Color(160, 170, 180));
	    cs.beginText();
	    cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
	    cs.newLineAtOffset((PAGE_WIDTH - captionWidth) / 2, centerY - 130);
	    cs.showText(caption);
	    cs.endText();
	}

	private static void addMainGalleryImage(PDDocument document, PDPage page, 
	                                       SpeciesDownload species, 
	                                       float galleryY, float galleryHeight) 
	        throws IOException {
	    String imagePath = "/app/data/biodiv/img" + species.getResourceData().get(0);
	    addImage(document, page, imagePath, MARGIN, galleryY + 20, 
	            galleryHeight - 20, true, true, CONTENT_WIDTH, false);
	}

	private static void drawCircle(PDPageContentStream cs, float centerX, 
	                              float centerY, float radius) throws IOException {
	    float magic = radius * 0.551915024494f;
	    
	    cs.moveTo(centerX, centerY + radius);
	    cs.curveTo(centerX + magic, centerY + radius, 
	              centerX + radius, centerY + magic, 
	              centerX + radius, centerY);
	    cs.curveTo(centerX + radius, centerY - magic, 
	              centerX + magic, centerY - radius, 
	              centerX, centerY - radius);
	    cs.curveTo(centerX - magic, centerY - radius, 
	              centerX - radius, centerY - magic, 
	              centerX - radius, centerY);
	    cs.curveTo(centerX - radius, centerY + magic, 
	              centerX - magic, centerY + radius, 
	              centerX, centerY + radius);
	    cs.fill();
	}

	// ============================================================================
	// VIEW MORE LINK
	// ============================================================================

	private static void addViewMoreText(PDPageContentStream contentStream) 
	        throws IOException {
	    float xPosition = CONTENT_WIDTH - VIEW_MORE_OFFSET_X;
	    float yPosition = currentY + VIEW_MORE_OFFSET_Y;
	    
	    contentStream.setNonStrokingColor(WHITE);
	    contentStream.beginText();
	    contentStream.setFont(primaryFont, VIEW_MORE_FONT_SIZE);
	    contentStream.newLineAtOffset(xPosition, yPosition);
	    contentStream.showText("View More");
	    contentStream.endText();
	}

	private static PDAnnotationLink createViewMoreLink() {
	    PDAnnotationLink link = new PDAnnotationLink();
	    PDRectangle position = new PDRectangle(
	        CONTENT_WIDTH - VIEW_MORE_OFFSET_X, 
	        currentY + VIEW_MORE_OFFSET_Y, 
	        VIEW_MORE_WIDTH, 
	        VIEW_MORE_HEIGHT
	    );
	    link.setRectangle(position);
	    return link;
	}

	// ============================================================================
	// IMAGE HANDLING
	// ============================================================================

	/**
	 * Adds an image to the PDF with fallback support
	 */
	public static void addImage(PDDocument document, PDPage page, String imagePath, 
	                           float x, float y, float height, Boolean fallback, 
	                           Boolean align, float maxWidth, Boolean fixedWidth) 
	        throws IOException {
	    File imageFile = new File(imagePath);
	    
	    try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
	            PDPageContentStream.AppendMode.APPEND, true)) {
	        
	        if (!isValidImageFile(imageFile)) {
	            handleMissingImage(contentStream, x, y, height, maxWidth, align, fallback);
	            return;
	        }
	        
	        try {
	            drawImageFromFile(contentStream, document, imagePath, x, y, height, 
	                            maxWidth, align, fixedWidth);
	        } catch (Exception e) {
	            logger.error("Loading fails for image path: {}", imagePath);
	            if (fallback) {
	                drawFallbackRectangle(contentStream, x, y, height, height, "!");
	            }
	        }
	    }
	}

	private static boolean isValidImageFile(File imageFile) {
	    return imageFile.exists() && imageFile.canRead() && imageFile.length() > 0;
	}

	private static void drawImageFromFile(PDPageContentStream cs, PDDocument document, 
	                                     String imagePath, float x, float y, float height,
	                                     float maxWidth, Boolean align, Boolean fixedWidth) 
	        throws IOException {
	    PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
	    float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
	    float width = height / aspectRatio;
	    //Here x and y refers to the x and y position of image
	    
	    if (fixedWidth) {
	        width = maxWidth;
	        y = y + (height - width * aspectRatio) / 2;
	        height = width * aspectRatio;
	    }
	    
	    if (align) {
	        x = x + (maxWidth - width) / 2;
	    }
	    
	    cs.drawImage(pdImage, x, y, width, height);
	}

	private static void handleMissingImage(PDPageContentStream cs, float x, float y, 
	                                      float height, float maxWidth, Boolean align, 
	                                      Boolean fallback) throws IOException {
	    logger.error("Image file doesn't exist");
	    
	    if (fallback) {
	        if (align) {
	            x = x + (maxWidth - height) / 2;
	        }
	        drawFallbackRectangle(cs, x, y, height, height, "!");
	    }
	}

	/**
	 * Draws a fallback rectangle with text when image is unavailable
	 */
	private static void drawFallbackRectangle(PDPageContentStream cs, float x, float y, 
	                                         float width, float height, String text) 
	        throws IOException {
	    // Draw background
	    cs.setNonStrokingColor(new Color(220, 220, 220));
	    cs.addRect(x, y, width, height);
	    cs.fill();
	    
	    // Draw text
	    cs.beginText();
	    cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
	    cs.setNonStrokingColor(Color.DARK_GRAY);
	    
	    // Center text
	    float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000 * 10;
	    float textX = x + (width - textWidth) / 2;
	    float textY = y + (height / 2) - 4;
	    
	    cs.newLineAtOffset(textX, textY);
	    cs.showText(text);
	    cs.endText();
	}

	public static void addBase64Image(PDDocument document, PDPage page, 
	                                 PDImageXObject pdImage, float x, float y,
	                                 float width, float height) throws IOException {
	    try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
	            PDPageContentStream.AppendMode.APPEND, true)) {
	        contentStream.drawImage(pdImage, x, y, width, height);
	    }
	}

	// ============================================================================
	// CIRCULAR IMAGE HANDLING
	// ============================================================================

	public static void addCircularImage(PDDocument document, PDPage page, 
	                                   String imagePath, float centerX, float centerY, 
	                                   float diameter, String name) throws IOException {
	    File imageFile = new File(imagePath);
	    
	    try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
	            PDPageContentStream.AppendMode.APPEND, true, true)) {
	        
	        if (isValidImageFile(imageFile)) {
	            try {
	                drawCircularImage(contentStream, document, imagePath, 
	                                centerX, centerY, diameter);
	            } catch (Exception e) {
	                drawFallbackCircle(contentStream, centerX, centerY, diameter / 2, name);
	            }
	        } else {
	            drawFallbackCircle(contentStream, centerX, centerY, diameter / 2, name);
	        }
	    }
	}

	private static void drawCircularImage(PDPageContentStream cs, PDDocument document,
	                                     String imagePath, float centerX, float centerY, 
	                                     float diameter) throws IOException {
	    PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
	    
	    float x = centerX - diameter / 2;
	    float y = centerY - diameter / 2;
	    
	    cs.saveGraphicsState();
	    createCircularClip(cs, centerX, centerY, diameter / 2);
	    cs.drawImage(pdImage, x, y, diameter, diameter);
	    cs.restoreGraphicsState();
	}

	private static void createCircularClip(PDPageContentStream cs, float centerX, 
	                                      float centerY, float radius) throws IOException {
	    final float k = 0.552284749831f;
	    
	    cs.moveTo(centerX - radius, centerY);
	    cs.curveTo(centerX - radius, centerY + k * radius, 
	              centerX - k * radius, centerY + radius, 
	              centerX, centerY + radius);
	    cs.curveTo(centerX + k * radius, centerY + radius, 
	              centerX + radius, centerY + k * radius,
	              centerX + radius, centerY);
	    cs.curveTo(centerX + radius, centerY - k * radius, 
	              centerX + k * radius, centerY - radius, 
	              centerX, centerY - radius);
	    cs.curveTo(centerX - k * radius, centerY - radius, 
	              centerX - radius, centerY - k * radius,
	              centerX - radius, centerY);
	    cs.closePath();
	    cs.clip();
	}

	private static void drawFallbackCircle(PDPageContentStream cs, float centerX, 
	                                      float centerY, float radius, String text) 
	        throws IOException {
	    // Draw gray background circle
	    cs.setNonStrokingColor(Color.LIGHT_GRAY);
	    createCircularPath(cs, centerX, centerY, radius);
	    cs.fill();
	    
	    // Draw border
	    cs.setStrokingColor(Color.DARK_GRAY);
	    cs.setLineWidth(1);
	    createCircularPath(cs, centerX, centerY, radius);
	    cs.stroke();
	    
	    // Draw initials
	    text = text.length() < 3 ? text : text.substring(0, 2);
	    cs.setNonStrokingColor(Color.DARK_GRAY);
	    cs.beginText();
	    cs.setFont(PDType1Font.HELVETICA_BOLD, radius);
	    cs.newLineAtOffset((centerX - radius) + (text.length() > 1 ? 2.5f : 3.5f), 
	                      centerY - radius / 3);
	    cs.showText(text);
	    cs.endText();
	}

	private static void createCircularPath(PDPageContentStream cs, float centerX, 
	                                      float centerY, float radius) throws IOException {
	    final float k = 0.552284749831f;
	    
	    cs.moveTo(centerX - radius, centerY);
	    cs.curveTo(centerX - radius, centerY + k * radius, 
	              centerX - k * radius, centerY + radius, 
	              centerX, centerY + radius);
	    cs.curveTo(centerX + k * radius, centerY + radius, 
	              centerX + radius, centerY + k * radius,
	              centerX + radius, centerY);
	    cs.curveTo(centerX + radius, centerY - k * radius, 
	              centerX + k * radius, centerY - radius, 
	              centerX, centerY - radius);
	    cs.curveTo(centerX - k * radius, centerY - radius, 
	              centerX - radius, centerY - k * radius,
	              centerX - radius, centerY);
	    cs.closePath();
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

	// ============================================================================
	// TEXT FORMATTING & HTML CONVERSION
	// ============================================================================

	/**
	 * Converts HTML tags to markdown-style markers for word-level formatting
	 */
	public static String convertHtmlToWordLevelMarkers(String text) {
	    if (text == null) {
	        return null;
	    }
	    
	    String step1 = processFormatting(text, "b", "strong", "**");
	    String step2 = processFormatting(step1, "i", null, "*");
	    
	    return step2;
	}

	private static String processFormatting(String text, String tag, String altTag, 
	                                       String marker) {
	    String startTag = "<" + tag + ">";
	    String endTag = "</" + tag + ">";
	    
	    String temp = text.replace(startTag, "〖START〗").replace(endTag, "〖END〗");
	    
	    if (altTag != null) {
	        String altStartTag = "<" + altTag + ">";
	        String altEndTag = "</" + altTag + ">";
	        temp = temp.replace(altStartTag, "〖START〗").replace(altEndTag, "〖END〗");
	    }
	    
	    StringBuilder result = new StringBuilder();
	    String[] parts = temp.split("(〖START〗|〖END〗)");
	    boolean inFormat = false;
	    
	    for (String part : parts) {
	        if (inFormat) {
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

	public static String convertHtmlToText(String html) {
	    if (html == null || html.trim().isEmpty()) {
	        return "";
	    }
	    
	    // Convert headings
	    html = html.replaceAll("<h[1-6][^>]*>", "\n<h>")
	               .replaceAll("</h[1-6]>", "\n");
	    
	    // Convert block elements
	    html = html.replaceAll("<p[^>]*>", "\n")
	               .replaceAll("</p>", "\n")
	               .replaceAll("<br[^>]*>", "\n")
	               .replaceAll("<div[^>]*>", "\n")
	               .replaceAll("</div>", "\n")
	               .replaceAll("<span[^>]*>", "")
	               .replaceAll("</span>", "");
	    
	    return decodeHtmlEntities(html);
	}

	private static String decodeHtmlEntities(String text) {
	    return text.replace("\t", "")
	               .replace("&amp;", "&")
	               .replace("&lt;", "<")
	               .replace("&gt;", ">")
	               .replace("&quot;", "\"")
	               .replace("&#39;", "'")
	               .replace("&nbsp;", " ")
	               .replace("&copy;", "(c)")
	               .replace("&reg;", "(r)")
	               .replace("&#8217;", "'")
	               .replace("&#8220;", "\"")
	               .replace("&#8221;", "\"");
	}

	// ============================================================================
	// TEXT SPLITTING & LINE BREAKING
	// ============================================================================

	/**
	 * Splits text into lines that fit within the specified width
	 */
	public static List<String> splitTextIntoLines(String text, PDFont font, 
	                                             float fontSize, float maxWidth) 
	        throws IOException {
	    List<String> lines = new ArrayList<>();
	    
	    String markdownText = convertHtmlToWordLevelMarkers(text);
	    if (markdownText == null) {
	        return lines;
	    }
	    
	    String[] words = markdownText.split(" ");
	    StringBuilder currentLine = new StringBuilder();
	    
	    for (String word : words) {
	        String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
	        String testLineWithoutMarkers = testLine.replaceAll("\\*", "");
	        
	        try {
	            float testWidth = font.getStringWidth(testLineWithoutMarkers) / 1000 * fontSize;
	            
	            if (testWidth < maxWidth) {
	                if (currentLine.length() > 0) {
	                    currentLine.append(" ");
	                }
	                currentLine.append(word);
	            } else {
	                addLineToList(lines, currentLine, word);
	            }
	            
	        } catch (IllegalArgumentException e) {
	            handleFallbackFont(lines, currentLine, word, testLineWithoutMarkers, 
	                             fontSize, maxWidth);
	        }
	    }
	    
	    if (currentLine.length() > 0) {
	        lines.add(currentLine.toString());
	    }
	    
	    return lines;
	}

	private static void addLineToList(List<String> lines, StringBuilder currentLine, 
	                                 String word) {
	    if (currentLine.length() > 0) {
	        lines.add(currentLine.toString());
	        currentLine.setLength(0);
	        currentLine.append(word);
	    } else {
	        lines.add(word);
	    }
	}

	private static void handleFallbackFont(List<String> lines, StringBuilder currentLine,
	                                      String word, String testLine, float fontSize, 
	                                      float maxWidth) {
	    logger.warn("Primary font failed, using fallback");
	    
	    try {
	        float testWidth = fallbackFont.getStringWidth(testLine) / 1000 * fontSize;
	        
	        if (testWidth < maxWidth) {
	            if (currentLine.length() > 0) {
	                currentLine.append(" ");
	            }
	            currentLine.append(word);
	        } else {
	            addLineToList(lines, currentLine, word);
	        }
	    } catch (IllegalArgumentException | IOException e2) {
	        logger.error("Both fonts failed for: {} - {}", word, e2.getMessage());
	    }
	}

	// ============================================================================
	// TEXT RENDERING
	// ============================================================================

	/**
	 * Draws text with word wrapping
	 */
	public static float drawTextWithWordWrap(PDPageContentStream cs, String text, 
	                                        PDFont font, float fontSize, float x, 
	                                        float y, float maxWidth, float lineHeight, 
	                                        Color color) throws IOException {
	    List<String> lines = splitTextIntoLines(text, font, fontSize, maxWidth);
	    float currentYPos = y;
	    
	    for (String line : lines) {
	        if (color != null) {
	            drawLineBackground(cs, currentYPos, lineHeight, color);
	        }
	        
	        //Adding 1.5f for aligning text in the middle
	        drawFormattedLine(cs, line, font, fontSize, x, currentYPos + 1.5f, maxWidth);
	        currentYPos -= lineHeight;
	    }
	    
	    return currentYPos;
	}

	private static void drawLineBackground(PDPageContentStream cs, float y, 
	                                      float lineHeight, Color color) 
	        throws IOException {
	    cs.setNonStrokingColor(color);
	    cs.setLineWidth(1);
	    cs.addRect(MARGIN, y - 1.5f, CONTENT_WIDTH, lineHeight);
	    cs.fill();
	}

	/**
	 * Draws formatted text line with bold and italic support
	 */
	private static void drawFormattedLine(PDPageContentStream cs, String line, 
	                                     PDFont baseFont, float fontSize, 
	                                     float startX, float y, float maxWidth) 
	        throws IOException {
	    List<TextSegment> segments = parseFormattedSegments(line, baseFont, fontSize);
	    float currentX = startX;
	    
	    try {
	        for (TextSegment segment : segments) {
	            cs.beginText();
	            cs.setFont(segment.getFont(), fontSize);
	            cs.newLineAtOffset(currentX, y);
	            cs.showText(segment.getText());
	            cs.endText();
	            
	            currentX += segment.getWidth();
	        }
	    } catch (Exception e) {
	        logger.error("Error drawing formatted line: {}", e.toString());
	    }
	}

	/**
	 * Parses text into segments with formatting markers
	 */
	private static List<TextSegment> parseFormattedSegments(String line, PDFont baseFont, 
	                                                       float fontSize) 
	        throws IOException {
	    List<TextSegment> segments = new ArrayList<>();
	    
	    // Pattern matches: **bold**, *italic*, or plain text
	    Pattern pattern = Pattern.compile("(\\*\\*(.*?)\\*\\*)|(\\*([^*]+)\\*)|([^*]+)");
	    Matcher matcher = pattern.matcher(line);
	    
	    PDFont boldFont = PDType1Font.HELVETICA_BOLD;
	    PDFont italicFont = PDType1Font.HELVETICA_OBLIQUE;
	    PDFont boldItalicFont = PDType1Font.HELVETICA_BOLD_OBLIQUE;
	    
	    while (matcher.find()) {
	        String boldText = matcher.group(2);
	        String italicText = matcher.group(4);
	        String normalText = matcher.group(5);
	        
	        String segmentText;
	        PDFont segmentFont = baseFont;
	        
	        if (boldText != null) {
	            segmentText = boldText;
	            segmentFont = boldFont;
	        } else if (italicText != null) {
	            segmentText = italicText;
	            segmentFont = baseFont.equals(boldFont) ? boldItalicFont : italicFont;
	        } else {
	            segmentText = normalText;
	        }
	        
	        if (segmentText != null && !segmentText.isEmpty()) {
	            segmentFont = selectFontWithFallback(segmentFont, segmentText);
	            float segmentWidth = segmentFont.getStringWidth(segmentText) * fontSize / 1000f;
	            segments.add(new TextSegment(segmentText, segmentFont, segmentWidth));
	        }
	    }
	    
	    return segments;
	}

	private static PDFont selectFontWithFallback(PDFont font, String text) {
	    try {
	        font.getStringWidth(text);
	        return font;
	    } catch (Exception e) {
	        logger.warn("Font cannot render text, using fallback: {}", text);
	        return fallbackFont;
	    }
	}

	// ============================================================================
	// COMPLEX TEXT RENDERING WITH OVERFLOW
	// ============================================================================

	/**
	 * Draws text with word wrapping and automatic page overflow handling
	 */
	public static PageContext drawTextWithWordWrapAndOverflow(
	        PDPageContentStream cs, PDDocument document, PDPage currentPage, 
	        String text, PDFont font, float fontSize, float x, float y, 
	        float maxWidth, float lineHeight, Color color, String leftText, 
	        float paddingBottom, boolean speciesField, boolean contributor, 
	        Color traitColor, float level, String url) throws IOException {
	    
	    List<String> lines = getTextLines(text, font, fontSize, maxWidth);
	    float currentYPos = y - 5;
	    int lineIndex = 0;
	    
	    for (String line : lines) {
	        boolean isLastLine = (lineIndex == lines.size() - 1);
	        
	        // Check if new page is needed
	        if (needsNewPage(currentYPos, lineHeight, paddingBottom, isLastLine)) {
	            PageContext ctx = createNewPageForOverflow(cs, document, currentPage, 
	                                                      currentYPos, lineIndex, color,
	                                                      speciesField, level);
	            cs = ctx.contentStream;
	            currentPage = ctx.page;
	            currentYPos = PAGE_HEIGHT - lineHeight;
	        }
	        
	        // Draw line content
	        drawLineContent(cs, line, font, fontSize, x, currentYPos, maxWidth, 
	                       lineHeight, color, leftText, paddingBottom, speciesField, 
	                       traitColor, level, url, lineIndex, isLastLine, currentPage);
	        
	        currentYPos -= lineHeight;
	        lineIndex++;
	    }
	    
	    return new PageContext(currentPage, cs, currentYPos - paddingBottom);
	}

	private static List<String> getTextLines(String text, PDFont font, float fontSize, 
	                                        float maxWidth) throws IOException {
	    if (text == null || text.isEmpty()) {
	        return List.of("");
	    }
	    
	    List<String> lines = splitTextIntoLines(text, font, fontSize, maxWidth);
	    return (lines == null) ? List.of("") : lines;
	}

	private static boolean needsNewPage(float currentY, float lineHeight, 
	                                   float paddingBottom, boolean isLastLine) {
	    float neededSpace = lineHeight + (isLastLine ? paddingBottom : 0);
	    return currentY - neededSpace < 0;
	}
	
	// ============================================================================
	// PAGE OVERFLOW HANDLING
	// ============================================================================

	private static PageContext createNewPageForOverflow(PDPageContentStream cs, 
	                                                   PDDocument document, 
	                                                   PDPage currentPage,
	                                                   float currentYPos, 
	                                                   int lineIndex, 
	                                                   Color color,
	                                                   boolean speciesField, 
	                                                   float level) throws IOException {
	    // Draw continuation backgrounds and borders
	    drawContinuationBackground(cs, currentYPos, lineIndex, color, speciesField, level);
	    cs.close();
	    
	    // Create new page
	    PDPage newPage = new PDPage(PDRectangle.A4);
	    document.addPage(newPage);
	    
	    PDPageContentStream newCs = new PDPageContentStream(document, newPage);
	    setPageBackground(newCs);
	    
	    return new PageContext(newPage, newCs);
	}

	private static void drawContinuationBackground(PDPageContentStream cs, float currentYPos,
	                                              int lineIndex, Color color,
	                                              boolean speciesField, float level) 
	        throws IOException {
	    float height = currentYPos + 14 + (lineIndex == 0 ? 5 : 0);
	    
	    // Main background
	    cs.setNonStrokingColor(color != null ? color : WHITE);
	    cs.addRect(MARGIN, 0, CONTENT_WIDTH, height);
	    cs.fill();
	    
	    if (speciesField) {
	        drawSpeciesFieldContinuation(cs, height);
	    }
	    
	    if (level != 0) {
	        drawLevelBorders(cs, height, level);
	    }
	    
	    // Side borders
	    drawBorder(cs, MARGIN, height, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, height, 0);
	}

	private static void drawSpeciesFieldContinuation(PDPageContentStream cs, float height) 
	        throws IOException {
	    cs.setNonStrokingColor(WHITE);
	    cs.addRect(MARGIN + 15, 0, CONTENT_WIDTH - 30, height);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN + 15, height, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH - 15, height, 0);
	}

	private static void drawLevelBorders(PDPageContentStream cs, float height, float level) 
	        throws IOException {
	    drawBorder(cs, MARGIN + 10, height, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH - 10, height, 0);
	    
	    if (level != 1) {
	        drawBorder(cs, MARGIN + 13, height, 0);
	        drawBorder(cs, MARGIN + CONTENT_WIDTH - 13, height, 0);
	    }
	}

	private static void drawBorder(PDPageContentStream cs, float x, float yStart, float yEnd) 
	        throws IOException {
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setLineWidth(1);
	    cs.moveTo(x, yStart);
	    cs.lineTo(x, yEnd);
	    cs.stroke();
	}

	private static void drawLineContent(PDPageContentStream cs, String line, PDFont font,
	                                   float fontSize, float x, float y, float maxWidth,
	                                   float lineHeight, Color color, String leftText,
	                                   float paddingBottom, boolean speciesField,
	                                   Color traitColor, float level, String url,
	                                   int lineIndex, boolean isLastLine, PDPage page) 
	        throws IOException {
	    boolean isFirstLine = (lineIndex == 0);
	    float padding = isLastLine ? paddingBottom : 0;
	    
	    // Draw background colors
	    if (color != null) {
	        drawLineBackground(cs, y, lineHeight, padding, isFirstLine, isLastLine, 
	                          color, speciesField);
	    }
	    
	    if (traitColor != null) {
	        drawTraitBackground(cs, x, y, maxWidth, lineHeight, isFirstLine, 
	                           isLastLine, traitColor);
	    }
	    
	    // Add hyperlink for species field if URL provided
	    if (speciesField && url != null) {
	        addHyperlinkToLine(cs, page, y, lineHeight, url);
	    }
	    
	    // Draw borders for species field
	    if (speciesField) {
	        drawSpeciesFieldBorders(cs, y, lineHeight, leftText, padding, 
	                               isFirstLine, isLastLine);
	    }
	    
	    // Draw level borders
	    if (level != 0) {
	        drawLevelLineBorders(cs, y, lineHeight, padding, isFirstLine, 
	                            isLastLine, level);
	    }
	    
	    // Draw left label text
	    if (leftText != null && isFirstLine) {
	        drawLeftLabel(cs, leftText, font, fontSize, y, speciesField);
	    }
	    
	    // Draw main text content
	    cs.setNonStrokingColor(BLACK);
	    drawFormattedLine(cs, line, font, fontSize, x, y + 1.5f, maxWidth);
	    
	    // Draw side borders
	    drawSideBorders(cs, y, lineHeight, padding, isFirstLine, isLastLine);
	}

	private static void drawLineBackground(PDPageContentStream cs, float y, float lineHeight,
	                                      float padding, boolean isFirst, boolean isLast,
	                                      Color color, boolean speciesField) 
	        throws IOException {
	    float height = lineHeight + (isFirst ? 5 : 0) + (isLast ? padding : 0);
	    
	    cs.setNonStrokingColor(color);
	    cs.setLineWidth(1);
	    cs.addRect(MARGIN, y - 1.5f - (isLast ? padding : 0), CONTENT_WIDTH, height);
	    cs.fill();
	    
	    if (speciesField) {
	        cs.setNonStrokingColor(ROW_ALTERNATE);
	        cs.addRect(MARGIN + 15, y - 1.5f - (isLast ? padding : 0), 
	                  CONTENT_WIDTH - 30, height);
	        cs.fill();
	    }
	}

	private static void drawTraitBackground(PDPageContentStream cs, float x, float y,
	                                       float maxWidth, float lineHeight,
	                                       boolean isFirst, boolean isLast, Color color) 
	        throws IOException {
	    float height = lineHeight + (isFirst ? 5 : 0) + (isLast ? 5 : 0);
	    cs.setNonStrokingColor(color);
	    cs.setLineWidth(1);
	    cs.addRect(x, y - 1.5f - (isLast ? 5 : 0), maxWidth, height);
	    cs.fill();
	}

	private static void addHyperlinkToLine(PDPageContentStream cs, PDPage page, 
	                                      float y, float lineHeight, String url) 
	        throws IOException {
	    PDAnnotationLink link = new PDAnnotationLink();
	    PDRectangle position = new PDRectangle(MARGIN + 15, y - 1.5f, 
	                                          CONTENT_WIDTH - 30, lineHeight);
	    link.setRectangle(position);
	    link.setBorderStyle(new PDBorderStyleDictionary());
	    link.getBorderStyle().setWidth(0);
	    
	    PDActionURI action = new PDActionURI();
	    action.setURI(url);
	    link.setAction(action);
	    
	    page.getAnnotations().add(link);
	}

	private static void drawLeftLabel(PDPageContentStream cs, String leftText, 
	                                 PDFont font, float fontSize, float y, 
	                                 boolean speciesField) throws IOException {
	    boolean isBold = leftText.startsWith("*");
	    String text = isBold ? leftText.substring(1) : leftText;
	    PDFont labelFont = isBold ? PDType1Font.HELVETICA_BOLD : font;
	    
	    cs.beginText();
	    cs.setFont(labelFont, fontSize);
	    cs.newLineAtOffset(speciesField ? MARGIN + 25 : MARGIN + 15, y + 1.5f);
	    cs.showText(text);
	    cs.endText();
	}

	private static void drawSpeciesFieldBorders(PDPageContentStream cs, float y, 
	                                           float lineHeight, String leftText,
	                                           float padding, boolean isFirst, 
	                                           boolean isLast) throws IOException {
	    float extraPadding = (isLast && leftText != null) ? 5 : padding;
	    float topY = y - 1.5f + lineHeight + (isFirst ? 5 : 0);
	    float bottomY = y - 1.5f - (isLast ? extraPadding : 0);
	    
	    cs.setNonStrokingColor(WHITE);
	    cs.addRect(MARGIN + 15, bottomY, CONTENT_WIDTH - 30, 
	              topY - bottomY);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN + 15, topY, bottomY);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH - 15, topY, bottomY);
	}

	private static void drawLevelLineBorders(PDPageContentStream cs, float y, 
	                                        float lineHeight, float padding,
	                                        boolean isFirst, boolean isLast, 
	                                        float level) throws IOException {
	    float topY = y - 1.5f + lineHeight + (isFirst ? 5 : 0);
	    float bottomY = y - 1.5f - (isLast ? padding : 0);
	    
	    drawBorder(cs, MARGIN + 10, topY, bottomY);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH - 10, topY, bottomY);
	    
	    if (level != 1) {
	        drawBorder(cs, MARGIN + 13, topY, bottomY);
	        drawBorder(cs, MARGIN + CONTENT_WIDTH - 13, topY, bottomY);
	    }
	}

	private static void drawSideBorders(PDPageContentStream cs, float y, float lineHeight,
	                                   float padding, boolean isFirst, boolean isLast) 
	        throws IOException {
	    float topY = y + lineHeight - 1.5f + (isFirst ? 5 : 0) + (isLast ? padding : 0);
	    float bottomY = y - 1.5f - (isLast ? padding : 0);
	    
	    drawBorder(cs, MARGIN, topY, bottomY);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, topY, bottomY);
	}

	// ============================================================================
	// SECTION CARD RENDERING
	// ============================================================================

	private static void drawSectionCard(PDPageContentStream cs, String title, 
	                                   float height, float currentLeftY) throws Exception {
	    float cardY = currentLeftY - height;
	    float width = CONTENT_WIDTH;
	    float headerHeight = 35;
	    
	    // Draw header background
	    cs.setNonStrokingColor(SECTION_HEADER_BG);
	    cs.setLineWidth(1);
	    cs.addRect(MARGIN, currentLeftY - headerHeight, width, headerHeight);
	    cs.fill();
	    
	    // Draw header border
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setLineWidth(1);
	    cs.addRect(MARGIN, currentLeftY - headerHeight, width, headerHeight);
	    cs.stroke();
	    
	    // Draw title text
	    cs.setNonStrokingColor(TEXT_PRIMARY);
	    cs.beginText();
	    cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
	    cs.newLineAtOffset(MARGIN + 15, currentLeftY - 22);
	    cs.showText(title);
	    cs.endText();
	    
	    currentY = cardY;
	}

	// ============================================================================
	// TAXONOMY SECTION
	// ============================================================================

	private static PageContext addTaxonomySection(PDDocument document, 
	                                             PDPageContentStream cs, 
	                                             PDPage page,
	                                             SpeciesDownload speciesData, 
	                                             float currentLeftY) throws Exception {
	    float width = CONTENT_WIDTH;
	    float y = currentLeftY - 40;
	    float sectionStartY = currentLeftY;
	    
	    // Handle page overflow
	    if (y < 0) {
	        PageContext ctx = handleSectionOverflow(cs, document, "Taxonomy");
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = currentY - 50;
	    } else {
	        drawSectionCard(cs, "Taxonomy", 0, sectionStartY);
	        y = y - 10;
	    }
	    
	    // Add taxonomy entries
	    for (BreadCrumb taxonomy : speciesData.getTaxonomy()) {
	        String rankName = taxonomy.getRankName();
	        String label = "*" + rankName.substring(0, 1).toUpperCase() + 
	                      rankName.substring(1).toLowerCase();
	        
	        PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page,
	                taxonomy.getName(), PDType1Font.HELVETICA, 11, MARGIN + 165, y, 
	                width - 185, 16, WHITE, label, 5, false, false, null, 0, null);
	        
	        page = context.page;
	        cs = context.contentStream;
	        y = context.yPosition;
	    }
	    
	    // Draw bottom border
	    drawSectionBottomBorder(cs, y, width);
	    
	    return new PageContext(page, cs, y - 10);
	}

	// ============================================================================
	// SYNONYM SECTION
	// ============================================================================

	private static PageContext addSynonymSection(PDDocument document, 
	                                            PDPageContentStream cs, 
	                                            PDPage page,
	                                            SpeciesDownload speciesData, 
	                                            float currentLeftY) throws Exception {
	    float width = CONTENT_WIDTH;
	    float y = currentLeftY - 40;
	    float sectionStartY = currentLeftY;
	    
	    // Handle page overflow
	    if (y < 0) {
	        PageContext ctx = handleSectionOverflow(cs, document, "Synonyms");
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = currentY - 50;
	    } else {
	        drawSectionCard(cs, "Synonyms", 0, sectionStartY);
	        y = y - 10;
	    }
	    
	    // Add synonym entries with alternating row colors
	    for (int i = 0; i < speciesData.getSynonyms().size(); i++) {
	        Color rowColor = (i % 2 == 0) ? ROW_ALTERNATE : WHITE;
	        
	        PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	                speciesData.getSynonyms().get(i), PDType1Font.HELVETICA, 11, 
	                MARGIN + 165, y, width - 185, 16, rowColor, "synonym", 5, 
	                false, false, null, 0, null);
	        
	        page = context.page;
	        cs = context.contentStream;
	        y = context.yPosition;
	    }
	    
	    drawSectionBottomBorder(cs, y, width);
	    
	    return new PageContext(page, cs, y - 10);
	}

	// ============================================================================
	// COMMON NAMES SECTION
	// ============================================================================

	private static PageContext addCommonNamesSection(PDDocument document, 
	                                                PDPageContentStream cs, 
	                                                PDPage page,
	                                                SpeciesDownload speciesData, 
	                                                float currentLeftY) throws Exception {
	    float width = CONTENT_WIDTH;
	    float y = currentLeftY - 40;
	    float sectionStartY = currentLeftY;
	    
	    // Handle page overflow
	    if (y < 0) {
	        PageContext ctx = handleSectionOverflow(cs, document, "Common Names");
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = currentY - 50;
	    } else {
	        drawSectionCard(cs, "Common Names", 0, sectionStartY);
	        y = y - 10;
	    }
	    
	    // Add common names grouped by language
	    int rowIndex = 0;
	    for (Map.Entry<String, List<String>> entry : speciesData.getCommonNames().entrySet()) {
	        String language = entry.getKey();
	        List<String> names = entry.getValue();
	        
	        for (int j = 0; j < names.size(); j++) {
	            String commonName = names.get(j);
	            Color rowColor = (rowIndex % 2 == 0) ? ROW_ALTERNATE : WHITE;
	            String label = (j == 0) ? language : null;
	            
	            PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	                    commonName, PDType1Font.HELVETICA, 11, MARGIN + 165, y, 
	                    width - 185, 16, rowColor, label, 5, false, false, null, 0, null);
	            
	            page = context.page;
	            cs = context.contentStream;
	            y = context.yPosition;
	        }
	        rowIndex++;
	    }
	    
	    drawSectionBottomBorder(cs, y, width);
	    
	    return new PageContext(page, cs, y - 10);
	}

	// ============================================================================
	// SECTION HELPER METHODS
	// ============================================================================

	private static PageContext handleSectionOverflow(PDPageContentStream cs, 
	                                                PDDocument document, 
	                                                String sectionTitle) throws Exception {
	    float y = currentY - 40;
	    
	    // Draw continuation background and borders
	    cs.setNonStrokingColor(WHITE);
	    cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN, y + 14, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y + 14, 0);
	    cs.close();
	    
	    // Create new page
	    PDPage newPage = new PDPage(PDRectangle.A4);
	    document.addPage(newPage);
	    currentY = PAGE_HEIGHT;
	    
	    PDPageContentStream newCs = new PDPageContentStream(document, newPage);
	    setPageBackground(newCs);
	    
	    drawSectionCard(newCs, sectionTitle, 0, currentY);
	    
	    return new PageContext(newPage, newCs);
	}

	private static void drawSectionBottomBorder(PDPageContentStream cs, float y, 
	                                           float width) throws IOException {
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setLineWidth(0.5f);
	    cs.moveTo(MARGIN, y + 15);
	    cs.lineTo(MARGIN + width, y + 15);
	    cs.stroke();
	}

	// ============================================================================
	// SPECIES FIELD SECTION
	// ============================================================================

	private static PageContext addSpeciesFieldSection(PDDocument document, 
	                                                 PDPageContentStream cs, 
	                                                 PDPage page,
	                                                 SpeciesField speciesField, 
	                                                 float currentLeftY, 
	                                                 String observationMap, 
	                                                 List<DocumentMeta> documentList, 
	                                                 String url,
	                                                 Long languageId) throws Exception {
	    float y = currentLeftY - 40;
	    float sectionStartY = currentLeftY;
	    
	    // Handle page overflow
	    if (y < 0) {
	        PageContext ctx = handleSectionOverflow(cs, document, speciesField.getName());
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = currentY - 50;
	    } else {
	        drawSectionCard(cs, speciesField.getName(), 0, sectionStartY);
	        y = y - 10;
	    }
	    
	    // Add field content
	    PageContext ctx = addSpeciesFieldGroup(document, cs, page, speciesField, 0, y, 
	            observationMap, documentList, url, languageId);
	    cs = ctx.contentStream;
	    page = ctx.page;
	    y = ctx.yPosition;
	    
	    // Draw bottom border
	    drawSectionBottomBorder(cs, y);
	    
	    return new PageContext(page, cs, y - 10);
	}

	private static void drawSectionBottomBorder(PDPageContentStream cs, float y) 
	        throws IOException {
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setLineWidth(0.5f);
	    cs.moveTo(MARGIN, y + 15);
	    cs.lineTo(MARGIN + CONTENT_WIDTH, y + 15);
	    cs.stroke();
	}
	
	// ============================================================================
	// SPECIES FIELD GROUP (COMPLEX NESTED CONTENT)
	// ============================================================================

	private static PageContext addSpeciesFieldGroup(PDDocument document, 
	                                               PDPageContentStream cs, 
	                                               PDPage page,
	                                               SpeciesField speciesField, 
	                                               int level, 
	                                               float currentLeftY, 
	                                               String observationMap, 
	                                               List<DocumentMeta> documentList,
	                                               String url, 
	                                               Long languageId) throws Exception {
	    float width = CONTENT_WIDTH;
	    float y = currentLeftY;
	    
	    // Handle observation map (special case for field ID 65)
	    if (speciesField.getId() == 65) {
	        return handleObservationMap(document, cs, page, observationMap, y);
	    }
	    
	    // Add field title for nested levels
	    if (level != 0 && speciesField.getId() != 82) {
	        PageContext ctx = addFieldTitle(cs, document, page, speciesField, level, y, width);
	        page = ctx.page;
	        cs = ctx.contentStream;
	        y = ctx.yPosition;
	    }
	    
	    // Handle document meta list (special case for field ID 82)
	    if (speciesField.getId() == 82) {
	        PageContext ctx = addDocumentMetaList(document, cs, page, documentList, 
	                                             y, width, url);
	        page = ctx.page;
	        cs = ctx.contentStream;
	        y = ctx.yPosition;
	    }
	    
	    // Add traits
	    for (Trait trait : speciesField.getTraits()) {
	        PageContext ctx = addTraitSection(document, cs, page, trait, y, 
	                                         width, level);
	        page = ctx.page;
	        cs = ctx.contentStream;
	        y = ctx.yPosition;
	    }
	    
	    // Add field values (descriptions)
	    for (int i = 0; i < speciesField.getValues().size(); i++) {
	        if (speciesField.getValues().get(i).getLanguageId().equals(languageId)) {
	            PageContext ctx = addFieldValue(document, cs, page, 
	                    speciesField.getValues().get(i), y, width, level);
	            page = ctx.page;
	            cs = ctx.contentStream;
	            y = ctx.yPosition;
	        }
	    }
	    
	    // Add child fields (recursive)
	    for (SpeciesField childField : speciesField.getChildField()) {
	        PageContext ctx = addSpeciesFieldGroup(document, cs, page, childField,
	                level + 1, y, observationMap, documentList, url, languageId);
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = ctx.yPosition;
	    }
	    
	    return new PageContext(page, cs, y);
	}

	// ============================================================================
	// OBSERVATION MAP HANDLING
	// ============================================================================

	private static PageContext handleObservationMap(PDDocument document, 
	                                               PDPageContentStream cs,
	                                               PDPage page, 
	                                               String observationMap, 
	                                               float y) throws Exception {
	    if (observationMap == null || observationMap.trim().isEmpty()) {
	        return new PageContext(page, cs, y);
	    }
	    
	    try {
	        // Decode base64 image
	        String imageData = extractBase64Data(observationMap);
	        byte[] imageBytes = Base64.getDecoder().decode(imageData);
	        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, 
	                imageBytes, "map");
	        
	        // Calculate dimensions
	        float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
	        float imageWidth = CONTENT_WIDTH - 30;
	        float height = aspectRatio * imageWidth;
	        float x = (CONTENT_WIDTH - imageWidth) / 2;
	        
	        // Check if new page needed
	        if (y - height - 25 < 0) {
	            PageContext ctx = createPageForMap(document, cs, page, y);
	            cs = ctx.contentStream;
	            page = ctx.page;
	            y = PAGE_HEIGHT - 10;
	        }
	        
	        // Add map image
	        addBase64Image(document, page, pdImage, MARGIN + x, y - height, 
	                      imageWidth, height);
	        
	        // Draw background and borders
	        drawMapBackground(cs, y, height);
	        
	        y = y - height - 25;
	        
	    } catch (Exception e) {
	        logger.error("Invalid map image");
	        drawFallbackRectangle(cs, MARGIN + 15, y, CONTENT_WIDTH - 30, 100, "!");
	    }
	    
	    return new PageContext(page, cs, y);
	}

	private static String extractBase64Data(String data) {
	    if (data.contains(",")) {
	        return data.split(",")[1];
	    }
	    return data;
	}

	private static PageContext createPageForMap(PDDocument document, 
	                                           PDPageContentStream cs,
	                                           PDPage page, float y) throws Exception {
	    // Draw continuation
	    cs.setNonStrokingColor(ROW_ALTERNATE);
	    cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN, y + 14, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y + 14, 0);
	    cs.close();
	    
	    // New page
	    PDPage newPage = new PDPage(PDRectangle.A4);
	    document.addPage(newPage);
	    
	    PDPageContentStream newCs = new PDPageContentStream(document, newPage);
	    setPageBackground(newCs);
	    
	    return new PageContext(newPage, newCs);
	}

	private static void drawMapBackground(PDPageContentStream cs, float y, float height) 
	        throws IOException {
	    cs.setNonStrokingColor(ROW_ALTERNATE);
	    cs.addRect(MARGIN, y - height - 25 + 15, CONTENT_WIDTH, height + 25);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN, y + 15, y + 15 - height - 25);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y + 15, y + 15 - height - 25);
	}

	// ============================================================================
	// FIELD TITLE
	// ============================================================================

	private static PageContext addFieldTitle(PDPageContentStream cs, PDDocument document,
	                                        PDPage page, SpeciesField speciesField,
	                                        int level, float y, float width) 
	        throws Exception {
	    float[] titleSize = {15, 12, 10};
	    
	    // Draw separator line
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setLineWidth(0.5f);
	    cs.moveTo(MARGIN + 10 + (level != 1 ? 3 : 0), y + 15);
	    cs.lineTo(MARGIN + width - 10 - (level != 1 ? 3 : 0), y + 15);
	    cs.stroke();
	    
	    // Add title text
	    PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	            speciesField.getName(), PDType1Font.HELVETICA_BOLD, titleSize[level], 
	            MARGIN + 15, y, width - 30, 16, ROW_ALTERNATE, null, 10, 
	            false, false, null, level, null);
	    
	    return context;
	}

	// ============================================================================
	// DOCUMENT META LIST
	// ============================================================================

	private static PageContext addDocumentMetaList(PDDocument document, 
	                                              PDPageContentStream cs,
	                                              PDPage page, 
	                                              List<DocumentMeta> documentList,
	                                              float y, float width, String url) 
	        throws Exception {
	    for (DocumentMeta doc : documentList) {
	        // Add document title with hyperlink
	        String docUrl = url + "/document/show/" + doc.getId();
	        PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	                doc.getTitle(), PDType1Font.HELVETICA_BOLD, 13, MARGIN + 25, y, 
	                width - 50, 16, ROW_ALTERNATE, null, 30, true, false, null, 1, docUrl);
	        
	        page = context.page;
	        cs = context.contentStream;
	        y = context.yPosition;
	        
	        // Add author name
	        drawFormattedLine(cs, doc.getUser(), PDType1Font.HELVETICA, 11, 
	                         MARGIN + 50, y + 30, width - 30);
	        
	        // Add author image
	        addCircularImage(document, page, "/app/data/biodiv/users" + doc.getPic(), 
	                        MARGIN + 35, y + 34, 16, getInitials(doc.getUser()));
	    }
	    
	    return new PageContext(page, cs, y);
	}

	// ============================================================================
	// TRAIT SECTION
	// ============================================================================

	private static PageContext addTraitSection(PDDocument document, 
	                                          PDPageContentStream cs,
	                                          PDPage page, Trait trait, 
	                                          float y, float width, float level) 
	        throws Exception {
	    // Add trait name/title
	    String traitTitle = trait.getName() + 
	            (trait.getDataType().equals("DATE") ? " (" + trait.getUnits() + ")" : "");
	    
	    PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page,
	            traitTitle, PDType1Font.HELVETICA_BOLD, 11, MARGIN + 25, y, 
	            width - 50, 16, ROW_ALTERNATE, null, 5, false, false, null, level, null);
	    
	    page = context.page;
	    cs = context.contentStream;
	    y = context.yPosition;
	    
	    // Add trait values in grid layout
	    context = addTraitValuesGrid(document, cs, page, trait, y, level);
	    
	    return context;
	}

	// ============================================================================
	// TRAIT VALUES GRID
	// ============================================================================

	private static PageContext addTraitValuesGrid(PDDocument document, 
	                                             PDPageContentStream cs,
	                                             PDPage page, Trait trait, 
	                                             float y, float level) throws Exception {
	    float boxWidth = (CONTENT_WIDTH - 50 - 20) / 3;
	    float boxHeight = 48;
	    float boxSpacing = 10;
	    float gridStartX = MARGIN + 25;
	    
	    int totalValues = trait.getValues().size();
	    int rows = (int) Math.ceil(totalValues / 3.0);
	    
	    for (int row = 0; row < rows; row++) {
	        // Check if new page needed
	        if (y - boxHeight - 10 < 0) {
	            PageContext ctx = createPageForTraits(document, cs, page, y, level);
	            cs = ctx.contentStream;
	            page = ctx.page;
	            y = PAGE_HEIGHT - 10;
	        }
	        
	        // Draw row background
	        drawTraitRowBackground(cs, y, boxHeight);
	        
	        // Draw trait boxes
	        for (int col = 0; col < 3; col++) {
	            int valueIndex = row * 3 + col;
	            if (valueIndex >= totalValues) break;
	            
	            float boxX = gridStartX + (col * (boxWidth + boxSpacing));
	            float boxY = y - boxHeight + 15;
	            
	            drawTraitValue(cs, document, page, trait, valueIndex, boxX, boxY, 
	                          boxWidth, boxHeight, y);
	        }
	        
	        y = y - boxHeight - 10;
	        
	        // Draw row borders
	        drawTraitRowBorders(cs, y, boxHeight);
	    }
	    
	    return new PageContext(page, cs, y);
	}

	private static PageContext createPageForTraits(PDDocument document, 
	                                              PDPageContentStream cs,
	                                              PDPage page, float y, float level) 
	        throws Exception {
	    // Draw continuation
	    cs.setNonStrokingColor(ROW_ALTERNATE);
	    cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN, y + 14, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y + 14, 0);
	    drawBorder(cs, MARGIN + 10, y + 14, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH - 10, y + 14, 0);
	    cs.close();
	    
	    // New page
	    PDPage newPage = new PDPage(PDRectangle.A4);
	    document.addPage(newPage);
	    
	    PDPageContentStream newCs = new PDPageContentStream(document, newPage);
	    setPageBackground(newCs);
	    
	    return new PageContext(newPage, newCs);
	}

	private static void drawTraitRowBackground(PDPageContentStream cs, float y, 
	                                          float boxHeight) throws IOException {
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setNonStrokingColor(ROW_ALTERNATE);
	    cs.addRect(MARGIN, y - boxHeight - 10 + 15, CONTENT_WIDTH, 58);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN + 10, y + 15, y - boxHeight - 10 + 15);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH - 10, y + 15, y - boxHeight - 10 + 15);
	}

	private static void drawTraitRowBorders(PDPageContentStream cs, float y, 
	                                       float boxHeight) throws IOException {
	    drawBorder(cs, MARGIN, y + boxHeight + 10 + 15, y + 15);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y + boxHeight + 10 + 15, y + 15);
	}

	private static void drawTraitValue(PDPageContentStream cs, PDDocument document,
	                                   PDPage page, Trait trait, int valueIndex,
	                                   float boxX, float boxY, float boxWidth, 
	                                   float boxHeight, float y) throws Exception {
	    if (trait.getDataType().equals("COLOR")) {
	        drawColorTrait(cs, trait, valueIndex, boxX, boxY, boxWidth, boxHeight);
	    } else {
	        drawTextTrait(cs, document, page, trait, valueIndex, boxX, boxY, 
	                     boxWidth, boxHeight, y);
	    }
	}

	private static void drawColorTrait(PDPageContentStream cs, Trait trait, 
	                                   int valueIndex, float boxX, float boxY,
	                                   float boxWidth, float boxHeight) throws IOException {
	    String rgbValue = trait.getValues().get(valueIndex).getValue();
	    Color color = parseRGBColor(rgbValue);
	    
	    cs.setNonStrokingColor(color);
	    cs.addRect(boxX, boxY, boxWidth, boxHeight);
	    cs.fill();
	}

	private static Color parseRGBColor(String rgbValue) {
	    String values = rgbValue.split("rgb\\(")[1].split("\\)")[0];
	    String[] parts = values.split(",");
	    
	    int r = Integer.parseInt(parts[0].trim());
	    int g = Integer.parseInt(parts[1].trim());
	    int b = Integer.parseInt(parts[2].trim());
	    
	    return new Color(r, g, b);
	}

	private static void drawTextTrait(PDPageContentStream cs, PDDocument document,
	                                  PDPage page, Trait trait, int valueIndex,
	                                  float boxX, float boxY, float boxWidth, 
	                                  float boxHeight, float y) throws Exception {
	    // Draw box
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.addRect(boxX, boxY, boxWidth, boxHeight);
	    cs.stroke();
	    
	    cs.setNonStrokingColor(WHITE);
	    cs.addRect(boxX, boxY, boxWidth, boxHeight);
	    cs.fill();
	    
	    // Get trait text
	    String text = getTraitValueText(trait, valueIndex);
	    
	    // Handle trait with image
	    boolean hasImage = text.contains("|");
	    String displayText = hasImage ? text.split("\\|")[0] : text;
	    
	    // Split text into lines
	    List<String> lines = splitTextIntoLines(displayText, PDType1Font.HELVETICA, 11,
	            boxWidth - 10 - (hasImage ? 45 : 0));
	    
	    // Draw text
	    float textY = y - (boxHeight - (Math.min(lines.size(), 3) * 16)) / 2;
	    cs.setNonStrokingColor(TEXT_PRIMARY);
	    
	    for (int l = 0; l < Math.min(lines.size(), 3); l++) {
	        drawFormattedLine(cs, lines.get(l), PDType1Font.HELVETICA, 11,
	                boxX + 5 + (hasImage ? 45 : 0), textY + 3.5f - l * 16,
	                boxWidth - 10 - (hasImage ? 45 : 0));
	    }
	    
	    // Add image if present
	    if (hasImage) {
	        String imagePath = "/app/data/biodiv/traits" + text.split("\\|")[1];
	        addImage(document, page, imagePath, boxX, y - 43 + 15, 
	                boxHeight - 10, true, true, 45, false);
	    }
	}

	private static String getTraitValueText(Trait trait, int valueIndex) {
	    String dataType = trait.getDataType();
	    
	    if (dataType.equals("STRING")) {
	        return trait.getOptions().get(trait.getValues().get(valueIndex).getValueId());
	    } else if (dataType.equals("NUMERIC")) {
	        return trait.getValues().get(valueIndex).getValue();
	    } else if (dataType.equals("DATE")) {
	        return formatDateRange(trait, valueIndex);
	    }
	    
	    return "";
	}

	private static String formatDateRange(Trait trait, int valueIndex) {
	    Date fromDate = trait.getValues().get(valueIndex).getFromDate();
	    Date toDate = trait.getValues().get(valueIndex).getoDate();
	    
	    if (trait.getUnits().equals("MONTH")) {
	        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
	        return monthFormat.format(fromDate) + " - " + monthFormat.format(toDate);
	    } else {
	        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
	        return yearFormat.format(fromDate) + " - " + yearFormat.format(toDate);
	    }
	}
	
	// ============================================================================
	// FIELD VALUE (DESCRIPTIONS & ATTRIBUTIONS)
	// ============================================================================

	private static PageContext addFieldValue(PDDocument document, PDPageContentStream cs,
	                                        PDPage page,FieldValue fieldValue,
	                                        float y, float width, float level) 
	        throws Exception {
	    // Draw separator
	    drawDashedSeparator(cs, y, width);
	    
	    // Add description paragraphs
	    String plainText = convertHtmlToText(fieldValue.getDescription());
	    String[] paragraphs = plainText.split("\n");
	    
	    for (String paragraph : paragraphs) {
	        if (!paragraph.isEmpty()) {
	            PageContext context = addDescriptionParagraph(cs, document, page, 
	                    paragraph, y, width, level);
	            page = context.page;
	            cs = context.contentStream;
	            y = context.yPosition;
	        }
	    }
	    
	    y = y + 5;
	    
	    // Draw dotted separator
	    drawDottedSeparator(cs, y, width);
	    
	    // Check space for attributions
	    if (!hasSpaceForAttributions(fieldValue, y, width)) {
	        PageContext ctx = createPageForAttributions(cs, document, page, y, level);
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = PAGE_HEIGHT - 16;
	    }
	    
	    // Add attributions
	    PageContext context = addAttributions(cs, document, page, fieldValue, y, width, level);
	    page = context.page;
	    cs = context.contentStream;
	    y = context.yPosition;
	    
	    // Draw final separator
	    drawSolidSeparator(cs, y, width);
	    
	    return new PageContext(page, cs, y);
	}

	private static void drawDashedSeparator(PDPageContentStream cs, float y, float width) 
	        throws IOException {
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setLineWidth(0.5f);
	    cs.moveTo(MARGIN + 15, y + 15);
	    cs.lineTo(MARGIN + width - 15, y + 15);
	    cs.stroke();
	}

	private static void drawDottedSeparator(PDPageContentStream cs, float y, float width) 
	        throws IOException {
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setLineWidth(1);
	    cs.setLineCapStyle(1); // Round cap
	    cs.setLineDashPattern(new float[]{2, 3}, 0);
	    cs.moveTo(MARGIN + 15, y + 15);
	    cs.lineTo(MARGIN + width - 15, y + 15);
	    cs.stroke();
	    cs.setLineDashPattern(new float[]{}, 0); // Reset
	}

	private static void drawSolidSeparator(PDPageContentStream cs, float y, float width) 
	        throws IOException {
	    cs.setStrokingColor(BORDER_GRAY);
	    cs.setLineWidth(1);
	    cs.moveTo(MARGIN + 15, y + 25);
	    cs.lineTo(MARGIN + width - 15, y + 25);
	    cs.stroke();
	}

	private static PageContext addDescriptionParagraph(PDPageContentStream cs, 
	                                                  PDDocument document,
	                                                  PDPage page, String paragraph,
	                                                  float y, float width, float level) 
	        throws Exception {
	    boolean isHeading = paragraph.startsWith("<h>");
	    String text = isHeading ? paragraph.substring(3) : paragraph;
	    PDFont font = isHeading ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
	    
	    return drawTextWithWordWrapAndOverflow(cs, document, page, text, font, 11, 
	            MARGIN + 25, y, width - 50, 16, ROW_ALTERNATE, null, 10, true, 
	            false, null, level, null);
	}

	private static boolean hasSpaceForAttributions(FieldValue fieldValue, 
	                                              float y, float width) 
	        throws IOException {
	    int attributionLines = splitTextIntoLines(fieldValue.getAttributions(), 
	            PDType1Font.HELVETICA, 9, width - 175).size();
	    
	    int contributorLines = 0;
	    for (String contributor : fieldValue.getContributor()) {
	        contributorLines += splitTextIntoLines(contributor, PDType1Font.HELVETICA, 
	                9, width - 175).size();
	    }
	    
	    int licenseLines = splitTextIntoLines(fieldValue.getLicense(), 
	            PDType1Font.HELVETICA, 9, width - 175).size();
	    
	    int totalNeededLines = attributionLines + contributorLines + licenseLines;
	    float neededSpace = (totalNeededLines * 16) + 20 + ((16 + 10) * contributorLines);
	    
	    return y - neededSpace >= 0;
	}

	private static PageContext createPageForAttributions(PDPageContentStream cs,
	                                                    PDDocument document,
	                                                    PDPage page, float y, 
	                                                    float level) throws Exception {
	    // Draw continuation
	    cs.setNonStrokingColor(ROW_ALTERNATE);
	    cs.addRect(MARGIN, 0, CONTENT_WIDTH, y - 5 + 14);
	    cs.fill();
	    
	    cs.setNonStrokingColor(WHITE);
	    cs.addRect(MARGIN + 15, 0, CONTENT_WIDTH - 30, y - 5 + 14);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN + 15, y - 5 + 14, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH - 15, y - 5 + 14, 0);
	    
	    if (level != 0) {
	        drawLevelBorders(cs, y - 5 + 14, level);
	    }
	    
	    drawBorder(cs, MARGIN, y - 5 + 14, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y - 5 + 14, 0);
	    cs.close();
	    
	    // New page
	    PDPage newPage = new PDPage(PDRectangle.A4);
	    document.addPage(newPage);
	    
	    PDPageContentStream newCs = new PDPageContentStream(document, newPage);
	    setPageBackground(newCs);
	    
	    return new PageContext(newPage, newCs);
	}

	private static PageContext addAttributions(PDPageContentStream cs, PDDocument document,
	                                          PDPage page, FieldValue fieldValue,
	                                          float y, float width, float level) 
	        throws Exception {
	    // Add attributions
	    PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page,
	            fieldValue.getAttributions(), PDType1Font.HELVETICA, 9, MARGIN + 155, y, 
	            width - 175, 16, ROW_ALTERNATE, "*Attributions", 5, true, false, 
	            null, level, null);
	    page = context.page;
	    cs = context.contentStream;
	    y = context.yPosition;
	    
	    // Add contributors
	    for (int i = 0; i < fieldValue.getContributor().size(); i++) {
	        String label = (i == 0) ? "*Contributors" : "";
	        context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	                fieldValue.getContributor().get(i), PDType1Font.HELVETICA, 9, 
	                MARGIN + 155, y, width - 175, 16, ROW_ALTERNATE, label, 5, 
	                true, false, null, level, null);
	        page = context.page;
	        cs = context.contentStream;
	        y = context.yPosition;
	    }
	    
	    // Add license
	    context = drawTextWithWordWrapAndOverflow(cs, document, page,
	            fieldValue.getLicense(), PDType1Font.HELVETICA, 9, MARGIN + 155, y, 
	            width - 175, 16, ROW_ALTERNATE, "*License", 15, true, true, 
	            null, level, null);
	    
	    return context;
	}

	// ============================================================================
	// REFERENCES SECTION
	// ============================================================================

	private static PageContext addReferencesSection(PDDocument document, 
	                                               PDPageContentStream cs,
	                                               PDPage page, 
	                                               SpeciesDownload speciesData,
	                                               float currentLeftY) throws Exception {
	    float width = CONTENT_WIDTH;
	    float y = currentLeftY - 40;
	    float sectionStartY = currentLeftY;
	    
	    // Handle page overflow
	    if (y < 0) {
	        PageContext ctx = handleSectionOverflow(cs, document, "References");
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = currentY - 50;
	    } else {
	        drawSectionCard(cs, "References", 0, sectionStartY);
	        y = y - 10;
	    }
	    
	    // Add references by language
	    for (Map.Entry<String, List<String>> entry : speciesData.getReferences().entrySet()) {
	        String language = entry.getKey();
	        List<String> references = entry.getValue();
	        
	        // Add language header
	        PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	                language, PDType1Font.HELVETICA_BOLD, 11, MARGIN + 15, y, 
	                width - 30, 16, WHITE, null, 5, false, false, null, 0, null);
	        page = context.page;
	        cs = context.contentStream;
	        y = context.yPosition;
	        
	        // Add numbered references
	        for (int j = 0; j < references.size(); j++) {
	            String numberedRef = (j + 1) + ". " + references.get(j);
	            context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	                    numberedRef, PDType1Font.HELVETICA, 11, MARGIN + 15, y, 
	                    width - 30, 16, WHITE, null, 5, false, false, null, 0, null);
	            page = context.page;
	            cs = context.contentStream;
	            y = context.yPosition;
	        }
	    }
	    
	    // Add common references
	    PageContext context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	            "Common references", PDType1Font.HELVETICA_BOLD, 11, MARGIN + 15, y, 
	            width - 30, 16, WHITE, null, 5, false, false, null, 0, null);
	    page = context.page;
	    cs = context.contentStream;
	    y = context.yPosition;
	    
	    for (int j = 0; j < speciesData.getCommonReferences().size(); j++) {
	        String numberedRef = (j + 1) + ". " + speciesData.getCommonReferences().get(j);
	        context = drawTextWithWordWrapAndOverflow(cs, document, page, 
	                numberedRef, PDType1Font.HELVETICA, 11, MARGIN + 15, y, 
	                width - 30, 16, WHITE, null, 5, false, false, null, 0, null);
	        page = context.page;
	        cs = context.contentStream;
	        y = context.yPosition;
	    }
	    
	    drawSectionBottomBorder(cs, y, width);
	    
	    return new PageContext(page, cs, y - 10);
	}

	// ============================================================================
	// TEMPORAL OBSERVED ON SECTION
	// ============================================================================

	private static PageContext addTemporalObservedOn(PDDocument document, 
	                                                PDPageContentStream cs,
	                                                PDPage page, 
	                                                SpeciesDownload speciesData,
	                                                float currentLeftY) throws Exception {
	    return addChartSection(document, cs, page, speciesData.getChartImage(),
	            "Temporal Observed On", currentLeftY);
	}

	// ============================================================================
	// TRAITS DISTRIBUTION SECTION
	// ============================================================================

	private static PageContext addTraitsPerMonth(PDDocument document, 
	                                            PDPageContentStream cs,
	                                            PDPage page, 
	                                            SpeciesDownload speciesData,
	                                            float currentLeftY) throws Exception {
	    return addChartSection(document, cs, page, speciesData.getTraitsChart(),
	            "Traits Distribution", currentLeftY);
	}

	// ============================================================================
	// GENERIC CHART SECTION
	// ============================================================================

	private static PageContext addChartSection(PDDocument document, 
	                                          PDPageContentStream cs,
	                                          PDPage page, String chartImage,
	                                          String sectionTitle, float currentLeftY) 
	        throws Exception {
	    float width = CONTENT_WIDTH;
	    float y = currentLeftY - 40;
	    float sectionStartY = currentLeftY;
	    
	    // Handle page overflow
	    if (y < 0) {
	        PageContext ctx = handleSectionOverflow(cs, document, sectionTitle);
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = currentY - 50;
	    } else {
	        drawSectionCard(cs, sectionTitle, 0, sectionStartY);
	        y = y - 10;
	    }
	    
	    // Add chart image
	    if (chartImage != null && !chartImage.trim().isEmpty()) {
	        try {
	            PageContext ctx = addChartImage(document, cs, page, chartImage, y);
	            cs = ctx.contentStream;
	            page = ctx.page;
	            y = ctx.yPosition;
	        } catch (Exception e) {
	            logger.error("Invalid chart image");
	            drawFallbackRectangle(cs, MARGIN + 15, y, CONTENT_WIDTH - 30, 100, 
	                    "Invalid image");
	        }
	    }
	    
	    drawSectionBottomBorder(cs, y, width);
	    
	    return new PageContext(page, cs, y - 10);
	}

	private static PageContext addChartImage(PDDocument document, PDPageContentStream cs,
	                                        PDPage page, String chartImage, float y) 
	        throws Exception {
	    // Decode image
	    String imageData = extractBase64Data(chartImage);
	    byte[] imageBytes = Base64.getDecoder().decode(imageData);
	    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, 
	            imageBytes, "chart");
	    
	    // Calculate dimensions
	    float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
	    float imageWidth = CONTENT_WIDTH - 30;
	    float height = aspectRatio * imageWidth;
	    
	    // Limit height to page size
	    if (height > PAGE_HEIGHT - 75) {
	        height = PAGE_HEIGHT - 75;
	        imageWidth = height / aspectRatio;
	    }
	    
	    float x = (CONTENT_WIDTH - imageWidth) / 2;
	    
	    // Check if new page needed
	    if (y - height - 25 < 0) {
	        PageContext ctx = createPageForChart(document, cs, page, y);
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = PAGE_HEIGHT - 10;
	    }
	    
	    // Add image
	    addBase64Image(document, page, pdImage, MARGIN + x, y - height, imageWidth, height);
	    
	    // Draw background and borders
	    drawChartBackground(cs, y, height);
	    
	    return new PageContext(page, cs, y - height - 25);
	}

	private static PageContext createPageForChart(PDDocument document, 
	                                             PDPageContentStream cs,
	                                             PDPage page, float y) throws Exception {
	    cs.setNonStrokingColor(WHITE);
	    cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN, y + 14, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y + 14, 0);
	    cs.close();
	    
	    PDPage newPage = new PDPage(PDRectangle.A4);
	    document.addPage(newPage);
	    
	    PDPageContentStream newCs = new PDPageContentStream(document, newPage);
	    setPageBackground(newCs);
	    
	    return new PageContext(newPage, newCs);
	}

	private static void drawChartBackground(PDPageContentStream cs, float y, float height) 
	        throws IOException {
	    cs.setNonStrokingColor(WHITE);
	    cs.addRect(MARGIN, y - height - 25 + 15, CONTENT_WIDTH, height + 25);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN, y + 15, y + 15 - height - 25);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y + 15, y + 15 - height - 25);
	}

	// ============================================================================
	// ADDITIONAL IMAGES SECTION
	// ============================================================================

	private static PageContext addAdditionalImages(PDDocument document, 
	                                              PDPageContentStream cs,
	                                              PDPage page, 
	                                              SpeciesDownload speciesData,
	                                              float currentLeftY, 
	                                              PDAnnotationLink link,
	                                              PDPage sourcePage) throws Exception {
	    float width = CONTENT_WIDTH;
	    float y = currentLeftY - 40;
	    float sectionStartY = currentLeftY;
	    
	    // Handle page overflow
	    if (y < 0) {
	        PageContext ctx = handleSectionOverflow(cs, document, "Additional Images");
	        cs = ctx.contentStream;
	        page = ctx.page;
	        y = currentY - 50;
	    } else {
	        drawSectionCard(cs, "Additional Images", 0, sectionStartY);
	        y = y - 10;
	    }
	    
	    // Setup hyperlink to this section
	    setupViewMoreLink(link, page, sourcePage);
	    
	    // Add images in grid
	    PageContext ctx = addImageGrid(document, cs, page, speciesData, y);
	    cs = ctx.contentStream;
	    page = ctx.page;
	    y = ctx.yPosition;
	    
	    drawSectionBottomBorder(cs, y, width);
	    
	    return new PageContext(page, cs, y - 10);
	}

	private static void setupViewMoreLink(PDAnnotationLink link, PDPage targetPage, 
	                                     PDPage sourcePage) throws IOException {
	    PDActionGoTo action = new PDActionGoTo();
	    PDPageFitDestination destination = new PDPageFitDestination();
	    destination.setPage(targetPage);
	    action.setDestination(destination);
	    link.setAction(action);
	    
	    sourcePage.getAnnotations().add(link);
	}

	private static PageContext addImageGrid(PDDocument document, PDPageContentStream cs,
	                                       PDPage page, SpeciesDownload speciesData,
	                                       float y) throws Exception {
	    float boxWidth = (CONTENT_WIDTH - 50 - 10) / 2;
	    float boxSpacing = 10;
	    float gridStartX = MARGIN + 25;
	    
	    List<String> images = speciesData.getResourceData();
	    int totalImages = images.size();
	    int rows = (int) Math.ceil(totalImages / 2.0);
	    
	    for (int row = 0; row < rows; row++) {
	        int index = row * 2;
	        if (index >= totalImages) break;
	        
	        // Calculate max height for this row
	        float maxHeight = calculateRowMaxHeight(document, images, index, boxWidth);
	        
	        // Check if new page needed
	        if (y - maxHeight - 10 < 0) {
	            PageContext ctx = createPageForImages(document, cs, page, y);
	            cs = ctx.contentStream;
	            page = ctx.page;
	            y = PAGE_HEIGHT - 10;
	        }
	        
	        // Draw row background
	        cs.setNonStrokingColor(WHITE);
	        cs.addRect(MARGIN, y - maxHeight - 10 + 15, CONTENT_WIDTH, maxHeight + 10);
	        cs.fill();
	        
	        // Add images in this row
	        for (int col = 0; col < 2; col++) {
	            int imgIndex = row * 2 + col;
	            if (imgIndex >= totalImages) break;
	            
	            float boxX = gridStartX + (col * (boxWidth + boxSpacing));
	            float boxY = y - maxHeight + 15;
	            
	            addImage(document, page, "/app/data/biodiv/img" + images.get(imgIndex), 
	                    boxX, boxY - 5, maxHeight, true, true, boxWidth, true);
	        }
	        
	        y = y - maxHeight - 10;
	        
	        // Draw row borders
	        drawBorder(cs, MARGIN, y + maxHeight + 10 + 15, y + 15);
	        drawBorder(cs, MARGIN + CONTENT_WIDTH, y + maxHeight + 10 + 15, y + 15);
	    }
	    
	    return new PageContext(page, cs, y);
	}

	private static float calculateRowMaxHeight(PDDocument document, List<String> images,
	                                          int startIndex, float boxWidth) {
	    float maxHeight = boxWidth;
	    
	    for (int i = 0; i < 2 && (startIndex + i) < images.size(); i++) {
	        try {
	            String imagePath = "/app/data/biodiv/img" + images.get(startIndex + i);
	            File imageFile = new File(imagePath);
	            
	            if (imageFile.exists() && imageFile.canRead() && imageFile.length() > 0) {
	                PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
	                float aspectRatio = (float) pdImage.getHeight() / pdImage.getWidth();
	                float imageHeight = boxWidth * aspectRatio;
	                maxHeight = Math.max(maxHeight, imageHeight);
	            }
	        } catch (IOException e) {
	            logger.error("Failed to load image: {}", images.get(startIndex + i));
	        }
	    }
	    
	    return maxHeight;
	}

	private static PageContext createPageForImages(PDDocument document, 
	                                              PDPageContentStream cs,
	                                              PDPage page, float y) throws Exception {
	    cs.setNonStrokingColor(WHITE);
	    cs.addRect(MARGIN, 0, CONTENT_WIDTH, y + 14);
	    cs.fill();
	    
	    drawBorder(cs, MARGIN, y + 14, 0);
	    drawBorder(cs, MARGIN + CONTENT_WIDTH, y + 14, 0);
	    cs.close();
	    
	    PDPage newPage = new PDPage(PDRectangle.A4);
	    document.addPage(newPage);
	    
	    PDPageContentStream newCs = new PDPageContentStream(document, newPage);
	    setPageBackground(newCs);
	    
	    return new PageContext(newPage, newCs);
	}

}
