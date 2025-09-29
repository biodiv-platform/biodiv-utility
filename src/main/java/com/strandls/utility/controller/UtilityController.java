/**
 * 
 */
package com.strandls.utility.controller;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.pac4j.core.profile.CommonProfile;

import com.strandls.activity.pojo.MailData;
import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.utility.ApiConstants;
import com.strandls.utility.pojo.Announcement;
import com.strandls.utility.pojo.Flag;
import com.strandls.utility.pojo.FlagCreateData;
import com.strandls.utility.pojo.FlagIbp;
import com.strandls.utility.pojo.FlagShow;
import com.strandls.utility.pojo.GalleryConfig;
import com.strandls.utility.pojo.GallerySlider;
import com.strandls.utility.pojo.Habitat;
import com.strandls.utility.pojo.HomePageData;
import com.strandls.utility.pojo.Language;
import com.strandls.utility.pojo.MiniGallerySlider;
import com.strandls.utility.pojo.ParsedName;
import com.strandls.utility.pojo.ReorderHomePage;
import com.strandls.utility.pojo.Tags;
import com.strandls.utility.pojo.TagsMappingData;
import com.strandls.utility.service.UtilityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Abhishek Rudra
 *
 */

@Api("Utility Service")
@Path(ApiConstants.V1 + ApiConstants.SERVICES)
public class UtilityController {

	@Inject
	private UtilityService utilityService;

	@GET
	@Path(ApiConstants.FLAG + "/{flagId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find Flag by Flag ID", notes = "Returns Flag details", response = Flag.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Flag not found", response = String.class) })
	public Response getFlags(@PathParam("flagId") String flagId) {

		try {
			Long id = Long.parseLong(flagId);
			Flag flag = utilityService.fetchByFlagId(id);

			return Response.status(Status.OK).entity(flag).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.FLAG + ApiConstants.IBP + "/{flagId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find Flag by Flag ID for IBP", notes = "Returns Flag details for IBP", response = FlagIbp.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Flag not found", response = String.class) })

	public Response getFlagsIbp(@PathParam("flagId") String flagId) {
		try {
			Long id = Long.parseLong(flagId);
			FlagIbp ibp = utilityService.fetchByFlagIdIbp(id);
			return Response.status(Status.OK).entity(ibp).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.OBJECTFLAG + "/{objectType}/{objectId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find flag by Observation Id", notes = "Return of Flags", response = FlagShow.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Flag not found", response = String.class) })

	public Response getFlagByObjectType(@PathParam("objectType") String objectType,
			@PathParam("objectId") String objectId) {

		try {
			Long id = Long.parseLong(objectId);
			List<FlagShow> flag = utilityService.fetchByFlagObject(objectType, id);
			return Response.status(Status.OK).entity(flag).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}

	}

	@GET
	@Path(ApiConstants.USERFLAG)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser
	@ApiOperation(value = "Find flag by userId", notes = "Returns List of Flag for a User", response = Flag.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Flag not Found", response = String.class) })

	public Response getFlagByUserId(@Context HttpServletRequest request) {
		try {

			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long id = Long.parseLong(profile.getId());
			List<Flag> flags = utilityService.fetchFlagByUserId(id);
			return Response.status(Status.OK).entity(flags).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path(ApiConstants.CREATE + ApiConstants.FLAG + "/{type}/{objectId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser

	@ApiOperation(value = "Flag a Object", notes = "Return a list of flag to the Object", response = FlagShow.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to flag a object", response = String.class),
			@ApiResponse(code = 406, message = "User has already flagged", response = String.class) })

	public Response createFlag(@Context HttpServletRequest request, @PathParam("type") String type,
			@PathParam("objectId") String objectId, @ApiParam(name = "flagIbp") FlagCreateData flagCreateData) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long userId = Long.parseLong(profile.getId());
			Long objId = Long.parseLong(objectId);
			List<FlagShow> result = utilityService.createFlag(request, type, userId, objId, flagCreateData);
			if (result.isEmpty())
				return Response.status(Status.NOT_ACCEPTABLE).entity("User Allowed Flagged").build();
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.UNFLAG + "/{objectType}/{objectId}/{flagId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser

	@ApiOperation(value = "Unflag a Object", notes = "Return a list of flag to the Object", response = FlagShow.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to unflag a object", response = String.class),
			@ApiResponse(code = 406, message = "User is not allowed to unflag", response = String.class) })

	public Response unFlag(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@PathParam("objectId") String objectId, @PathParam("flagId") String fId,
			@ApiParam(name = "mailData") MailData mailData) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);

			Long flagId = Long.parseLong(fId);
			List<FlagShow> result = null;
			Long objId = Long.parseLong(objectId);
			result = utilityService.removeFlag(request, profile, objectType, objId, flagId, mailData);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.TAGS + ApiConstants.AUTOCOMPLETE)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find the Sugguestion for tags", notes = "Return list of Top 10 tags matching the phrase", response = Tags.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to fetch the tags", response = String.class) })

	public Response getTagsAutoComplete(@QueryParam("phrase") String phrase) {
		try {
			List<Tags> result = utilityService.tagsAutoSugguest(phrase);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.TAGS + "/{objectType}/{objectId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find tags", notes = "Return list tags", response = Tags.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Tags not Found", response = String.class) })

	public Response getTags(@PathParam("objectType") String objectType, @PathParam("objectId") String objectId) {
		try {
			Long id = Long.parseLong(objectId);
			List<Tags> tags = utilityService.fetchTags(objectType, id);
			return Response.status(Status.OK).entity(tags).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path(ApiConstants.TAGS + "/{objectType}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Create Tags", notes = "Return the id of Tags Links created", response = String.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 409, message = "Error occured in transaction", response = String.class),
			@ApiResponse(code = 400, message = "DB not Found", response = String.class),
			@ApiResponse(code = 206, message = "partial succes ", response = String.class) })

	public Response createTags(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@ApiParam(name = "tagsMappingData") TagsMappingData tagsMappingData) {
		try {
			List<String> result = utilityService.createTagsMapping(request, objectType, tagsMappingData);
			if (result == null)
				return Response.status(Status.CONFLICT).entity("Error occured in transaction").build();
			else {
				if (result.get(0).startsWith("Mapping not proper for TagName and id Supplied for ID"))
					return Response.status(206).entity(result).build(); // PARTIAL CONTENT 206
				return Response.status(Status.CREATED).entity(result).build();

			}
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.TAGS + "/{objectType}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser
	@ApiOperation(value = "Update the tags", notes = "Returns all the current tags", response = Tags.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to edit", response = String.class) })

	public Response updateTags(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@ApiParam(name = "tagsMappingData") TagsMappingData tagsMappingData) {
		try {
			List<Tags> result = utilityService.updateTags(request, objectType, tagsMappingData);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.NAMEPARSER)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find the Canonical Form of a Scientific Name", notes = "Returns the Canonical Name of a Scientific Name", response = ParsedName.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Canonical Name not Found", response = String.class) })

	public Response getNameParsed(@QueryParam("scientificName") String name) {

		try {
			ParsedName result = utilityService.findParsedName(name);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}

	}

	@GET
	@Path(ApiConstants.LANGUAGES)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find all the Languages based on IsDirty field", notes = "Returns all the Languages Details", response = Language.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Languages Not Found", response = String.class) })

	public Response getAllLanguages(@QueryParam("isDirty") Boolean isDirty) {
		try {
			List<Language> result = utilityService.findAllLanguages(isDirty);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.LANGUAGES + "/{code}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Fetch Language by code type and code(eg. codeType=twoLetterCode, code=en)", notes = "Returns Language by codeType and code default value for the codeType is treeLetterCode", response = Language.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Unable to return the Langauge", response = String.class) })

	public Response getLanguage(@DefaultValue("threeLetterCode") @QueryParam("codeType") String codeType,
			@PathParam("code") String code) {
		try {
			Language result = utilityService.getLanguage(codeType, code);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.LANGUAGES + ApiConstants.TWOLETTERCODE + "/{code}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Fetch Language by two letter code", notes = "Returns Language by two letter code", response = Language.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Unable to return the Langauge", response = String.class) })

	public Response getLanguageByTwoLetterCode(@PathParam("code") String code) {
		try {
			Language result = utilityService.getLanguageByTwoLetterCode(code);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.HOMEPAGE)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Get home page data", notes = "Return home page data", response = HomePageData.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to fetch the data", response = String.class) })
	public Response getHomePageData(@Context HttpServletRequest request,
			@DefaultValue("false") @QueryParam("adminList") Boolean adminList, @DefaultValue("-1") @QueryParam("languageId") Long languageId) {
		try {
			HomePageData result = utilityService.getHomePageData(request, adminList, languageId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.HOMEPAGE + ApiConstants.MINI_GALLERY + ApiConstants.CREATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser

	@ApiOperation(value = "Creates a new mini gallery", notes = "Return created mini gallery", response = Map.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to create mini gallery", response = String.class)})

	public Response createMiniGallery(@Context HttpServletRequest request, @ApiParam(name = "miniGalleryData") GalleryConfig miniGalleryData) {
		try {
			GalleryConfig result = utilityService.createMiniGallery(request, miniGalleryData);
			if (result!=null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.HOMEPAGE + ApiConstants.MINI_GALLERY + ApiConstants.EDIT + "/{galleryId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Edit mini gallery data", notes = "return mini gallery data", response = Map.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to retrieve the data", response = String.class) })

	public Response editMiniGallery(@Context HttpServletRequest request, @PathParam("galleryId") String galleryId,
			@ApiParam(name = "editData") GalleryConfig editData) {
		try {
			if (galleryId == null) {
				return Response.status(Status.BAD_REQUEST).entity("Gallery Id cannot be null").build();
			}
			Long gId = Long.parseLong(galleryId);
			GalleryConfig result = utilityService.editMiniGallery(request, gId, editData);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Path(ApiConstants.HOMEPAGE + ApiConstants.MINI_GALLERY + ApiConstants.REMOVE + "/{galleryId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Delete mini gallery data", notes = "returns null", response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to delete the data", response = String.class) })

	public Response removeMiniGalleryData(@Context HttpServletRequest request,
			@PathParam("galleryId") String galleryId) {
		try {
			if (galleryId == null) {
				return Response.status(Status.BAD_REQUEST).entity("Gallery Id cannot be null").build();
			}
			Long gId = Long.parseLong(galleryId);
			Boolean result = utilityService.removeMiniGallery(request, gId);
			if (Boolean.TRUE.equals(result))
				return Response.status(Status.OK).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Path(ApiConstants.HOMEPAGE + ApiConstants.REMOVE + "/{galleryId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Delete homepage gallery data", notes = "return home page data", response = HomePageData.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to retrieve the data", response = String.class) })

	public Response removeGalleryData(@Context HttpServletRequest request, @PathParam("galleryId") String galleryId) {
		try {
			Long gId = Long.parseLong(galleryId);
			HomePageData result = utilityService.removeHomePage(request, gId);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.HOMEPAGE + ApiConstants.EDIT + "/{galleryId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Edit homepage gallery data", notes = "return home page data", response = HomePageData.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to retrieve the data", response = String.class) })

	public Response editHomePage(@Context HttpServletRequest request, @PathParam("galleryId") String galleryId,
			@ApiParam(name = "editData") GallerySlider editData) {
		try {
			Long gId = Long.parseLong(galleryId);
			HomePageData result = utilityService.editHomePage(request, gId, editData);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.HOMEPAGE + ApiConstants.EDIT + ApiConstants.MINI_SLIDER + "/{galleryId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Edit homepage mini gallery data", notes = "return home page data", response = HomePageData.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to retrieve the data", response = String.class) })

	public Response editMiniHomePage(@Context HttpServletRequest request, @PathParam("galleryId") String galleryId,
			@ApiParam(name = "editData") MiniGallerySlider editData) {
		try {
			Long gId = Long.parseLong(galleryId);
			HomePageData result = utilityService.editMiniHomePage(request, gId, editData);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Path(ApiConstants.HOMEPAGE + ApiConstants.REMOVE + ApiConstants.MINI_SLIDER + "/{galleryId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Delete homepage mini gallery data", notes = "return home page data", response = HomePageData.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to retrieve the data", response = String.class) })

	public Response removeMiniGallery(@Context HttpServletRequest request, @PathParam("galleryId") String galleryId) {
		try {
			Long gId = Long.parseLong(galleryId);
			HomePageData result = utilityService.removeMiniHomePage(request, gId);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.HOMEPAGE + ApiConstants.REORDERING)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	public Response reorderingHomePageGallerySlider(@Context HttpServletRequest request,
			@ApiParam(name = "reorderingHomePage") List<ReorderHomePage> reorderingHomePage) {
		try {
			HomePageData result = utilityService.reorderHomePageSlider(request, reorderingHomePage);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.HOMEPAGE + ApiConstants.MINI_SLIDER + ApiConstants.REORDERING)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	public Response reorderingMiniHomePageGallerySlider(@Context HttpServletRequest request,
			@ApiParam(name = "reorderingHomePage") List<ReorderHomePage> reorderingHomePage) {
		try {
			HomePageData result = utilityService.reorderMiniHomePageSlider(request, reorderingHomePage);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	// Insert list of New Home gallery Data
	@PUT
	@Path(ApiConstants.HOMEPAGE + ApiConstants.INSERT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "update  homepage gallery data", notes = "return  home page data", response = HomePageData.class)
	@ApiResponses(value = {

			@ApiResponse(code = 400, message = "unable to retrieve the data", response = String.class) })
	public Response updateGalleryData(@Context HttpServletRequest request,
			@ApiParam(name = "editData") HomePageData editData) {
		try {

			HomePageData result = utilityService.insertHomePage(request, editData);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.YOUTUBE + "/{id}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)

	@ApiOperation(value = "Get the youtube video title", notes = "Takes the youtube videoId and returns the title", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to get the title", response = String.class) })

	public Response getYoutubeTitle(@PathParam("id") String videoId) {
		try {
			String result = utilityService.getYoutubeTitle(videoId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.HABITAT + ApiConstants.ALL)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Get all the Habitat", notes = "Returns all the habitat in habitat order", response = Habitat.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to get the habitat", response = String.class) })

	public Response getAllHabitat() {
		try {
			List<Habitat> result = utilityService.fetchAllHabitat();
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.RESOURCE)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Get resource ids for tags", notes = "Returns resource ids based on tags", response = Long.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to get resource ids", response = String.class) })

	public Response getResourceIds(@DefaultValue("all") @QueryParam("phrase") String phrase,
			@DefaultValue("all") @QueryParam("type") String type,
			@DefaultValue("all") @QueryParam("tagRefId") String tagRefId) {
		try {
			List<Long> result = utilityService.getResourceIds(phrase, type, tagRefId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@POST
	@Path(ApiConstants.HOMEPAGE + ApiConstants.ANNOUNCEMENT + ApiConstants.CREATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser

	@ApiOperation(value = "Creates an announcement", notes = "Return created announcement", response = Map.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to create announcement", response = String.class)})

	public Response createAnnouncement(@Context HttpServletRequest request, @ApiParam(name = "announcementData") Announcement announcementData) {
		try {
			Announcement result = utilityService.createAnnouncement(request, announcementData);
			if (result!=null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Path(ApiConstants.ANNOUNCEMENT+ApiConstants.ALL)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Get announcements data", notes = "Return announcements data", response = List.class)
	@ApiResponses(value = { @ApiResponse(code =	 400, message = "unable to fetch the data", response = String.class) })
	public Response getAnnouncementData(@Context HttpServletRequest request) {
		try {
			List<Announcement> result = utilityService.getAnnouncementData(request);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@DELETE
	@Path(ApiConstants.ANNOUNCEMENT + ApiConstants.REMOVE + "/{announcementId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Delete announcement data", notes = "return if success", response = Boolean.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to delete the data", response = String.class) })

	public Response removeAnnouncementData(@Context HttpServletRequest request, @PathParam("announcementId") String announcementId) {
		try {
			Long aId = Long.parseLong(announcementId);
			Boolean result = utilityService.removeAnnouncement(request, aId);
			if (result != null)
				return Response.status(Status.OK).entity(result).build();
			return Response.status(Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

}
