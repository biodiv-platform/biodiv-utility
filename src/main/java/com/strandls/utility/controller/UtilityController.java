package com.strandls.utility.controller;

import java.util.List;
import java.util.Map;

import org.pac4j.core.profile.CommonProfile;

import com.strandls.activity.pojo.MailData;
import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.utility.ApiConstants;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Tag(name = "Utility Service", description = "APIs for utility microservice")
@Path(ApiConstants.V1 + ApiConstants.SERVICES)
@Produces(MediaType.APPLICATION_JSON)
public class UtilityController {

	@Inject
	private UtilityService utilityService;

	@GET
	@Path(ApiConstants.FLAG + "/{flagId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Operation(summary = "Find Flag by Flag ID", responses = {
			@ApiResponse(responseCode = "200", description = "Flag details", content = @Content(schema = @Schema(implementation = Flag.class))),
			@ApiResponse(responseCode = "404", description = "Flag not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Find Flag by Flag ID for IBP", responses = {
			@ApiResponse(responseCode = "200", description = "Flag details for IBP", content = @Content(schema = @Schema(implementation = FlagIbp.class))),
			@ApiResponse(responseCode = "404", description = "Flag not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Find flag list by object type and ID", responses = {
			@ApiResponse(responseCode = "200", description = "Flags found", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FlagShow.class)))),
			@ApiResponse(responseCode = "400", description = "Flag not found", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@ValidateUser
	@Operation(summary = "Find flag by userId", responses = {
			@ApiResponse(responseCode = "200", description = "List of flags for a User", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Flag.class)))),
			@ApiResponse(responseCode = "400", description = "Flag not Found", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@ValidateUser
	@Operation(summary = "Flag an Object", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FlagCreateData.class))), responses = {
			@ApiResponse(responseCode = "200", description = "List of flag for the Object", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FlagShow.class)))),
			@ApiResponse(responseCode = "406", description = "User has already flagged", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Unable to flag object", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response createFlag(@Context HttpServletRequest request, @PathParam("type") String type,
			@PathParam("objectId") String objectId, FlagCreateData flagCreateData) {
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
	@ValidateUser
	@Operation(summary = "Unflag an Object", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = MailData.class))), responses = {
			@ApiResponse(responseCode = "200", description = "List of flag for the Object", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FlagShow.class)))),
			@ApiResponse(responseCode = "406", description = "User is not allowed to unflag", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Unable to unflag object", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response unFlag(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@PathParam("objectId") String objectId, @PathParam("flagId") String fId, MailData mailData) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long flagId = Long.parseLong(fId);
			Long objId = Long.parseLong(objectId);
			List<FlagShow> result = utilityService.removeFlag(request, profile, objectType, objId, flagId, mailData);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.TAGS + ApiConstants.AUTOCOMPLETE)
	@Consumes(MediaType.TEXT_PLAIN)
	@Operation(summary = "Find the Suggestions for tags", responses = {
			@ApiResponse(responseCode = "200", description = "Top 10 tags matching the phrase", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Tags.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to fetch the tags", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Find tags", responses = {
			@ApiResponse(responseCode = "200", description = "List of tags", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Tags.class)))),
			@ApiResponse(responseCode = "400", description = "Tags not Found", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@ValidateUser
	@Operation(summary = "Create Tags", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TagsMappingData.class))), responses = {
			@ApiResponse(responseCode = "201", description = "Tags Links created", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "206", description = "Partial success", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "409", description = "Error occurred in transaction", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "DB not Found", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response createTags(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			TagsMappingData tagsMappingData) {
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
	@ValidateUser
	@Operation(summary = "Update the tags", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TagsMappingData.class))), responses = {
			@ApiResponse(responseCode = "200", description = "Returns all the current tags", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Tags.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to edit", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response updateTags(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			TagsMappingData tagsMappingData) {
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
	@Operation(summary = "Find the Canonical Form of a Scientific Name", responses = {
			@ApiResponse(responseCode = "200", description = "Canonical Name of a Scientific Name", content = @Content(schema = @Schema(implementation = ParsedName.class))),
			@ApiResponse(responseCode = "400", description = "Canonical Name not Found", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Find all the Languages based on IsDirty field", responses = {
			@ApiResponse(responseCode = "200", description = "All Languages Details", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Language.class)))),
			@ApiResponse(responseCode = "400", description = "Languages Not Found", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Fetch Language by code type and code (eg. codeType=twoLetterCode, code=en)", responses = {
			@ApiResponse(responseCode = "200", description = "Language found", content = @Content(schema = @Schema(implementation = Language.class))),
			@ApiResponse(responseCode = "404", description = "Unable to return the Language", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Fetch Language by two letter code", responses = {
			@ApiResponse(responseCode = "200", description = "Language found", content = @Content(schema = @Schema(implementation = Language.class))),
			@ApiResponse(responseCode = "404", description = "Unable to return the Language", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Get home page data", responses = {
			@ApiResponse(responseCode = "200", description = "Home page data", content = @Content(schema = @Schema(implementation = HomePageData.class))),
			@ApiResponse(responseCode = "400", description = "Unable to fetch the data", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response getHomePageData(@Context HttpServletRequest request,
			@DefaultValue("false") @QueryParam("adminList") Boolean adminList, @DefaultValue("-1") @QueryParam("languageId") Long languageId) {
		try {
			HomePageData result = utilityService.getHomePageData(request, adminList, languageId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

// CREATE
@POST
@Path(ApiConstants.HOMEPAGE + ApiConstants.MINI_GALLERY + ApiConstants.CREATE)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ValidateUser
@Operation(
    summary = "Creates a new mini gallery",
    description = "Return created mini gallery",
	responses = {
    @ApiResponse(responseCode = "200", description = "Created",
        content = @Content(schema = @Schema(implementation = GalleryConfig.class))),
    @ApiResponse(responseCode = "400", description = "Unable to create mini gallery",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "404", description = "Not found")
})
public Response createMiniGallery(
    @Context HttpServletRequest request,
    @RequestBody(description = "Mini gallery payload", required = true,
        content = @Content(schema = @Schema(implementation = GalleryConfig.class)))
    GalleryConfig miniGalleryData
) {
    try {
        GalleryConfig result = utilityService.createMiniGallery(request, miniGalleryData);
        if (result != null) return Response.status(Response.Status.OK).entity(result).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}

// EDIT
@PUT
@Path(ApiConstants.HOMEPAGE + ApiConstants.MINI_GALLERY + ApiConstants.EDIT + "/{galleryId}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ValidateUser
@Operation(
    summary = "Edit mini gallery data",
    description = "Return updated mini gallery",
	responses = {
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = GalleryConfig.class))),
    @ApiResponse(responseCode = "400", description = "Unable to edit",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "404", description = "Not found")
})
public Response editMiniGallery(
    @Context HttpServletRequest request,
    @Parameter(description = "Gallery ID") @PathParam("galleryId") String galleryId,
    @RequestBody(description = "Edit payload", required = true,
        content = @Content(schema = @Schema(implementation = GalleryConfig.class)))
    GalleryConfig editData
) {
    try {
        if (galleryId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Gallery Id cannot be null").build();
        }
        Long gId = Long.parseLong(galleryId);
        GalleryConfig result = utilityService.editMiniGallery(request, gId, editData);
        if (result != null) return Response.status(Response.Status.OK).entity(result).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}

// DELETE
@DELETE
@Path(ApiConstants.HOMEPAGE + ApiConstants.MINI_GALLERY + ApiConstants.REMOVE + "/{galleryId}")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
@ValidateUser
@Operation(
    summary = "Delete mini gallery data",
    description = "Deletes the mini gallery and returns no content",
	responses = {
    @ApiResponse(responseCode = "200", description = "Deleted"),
    @ApiResponse(responseCode = "400", description = "Unable to delete",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "404", description = "Not found")
})
public Response removeMiniGalleryData(
    @Context HttpServletRequest request,
    @Parameter(description = "Gallery ID") @PathParam("galleryId") String galleryId
) {
    try {
        if (galleryId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Gallery Id cannot be null").build();
        }
        Long gId = Long.parseLong(galleryId);
        Boolean result = utilityService.removeMiniGallery(request, gId);
        if (Boolean.TRUE.equals(result)) return Response.status(Response.Status.OK).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}
	@DELETE
	@Path(ApiConstants.HOMEPAGE + ApiConstants.REMOVE + "/{galleryId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@ValidateUser
	@Operation(summary = "Delete homepage gallery data", responses = {
			@ApiResponse(responseCode = "200", description = "Home page data", content = @Content(schema = @Schema(implementation = HomePageData.class))),
			@ApiResponse(responseCode = "404", description = "Home page gallery not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Unable to retrieve the data", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@ValidateUser
	@Operation(summary = "Edit homepage gallery data", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = GallerySlider.class))), responses = {
			@ApiResponse(responseCode = "200", description = "Home page data", content = @Content(schema = @Schema(implementation = HomePageData.class))),
			@ApiResponse(responseCode = "404", description = "Home page gallery not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Unable to retrieve the data", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response editHomePage(@Context HttpServletRequest request, @PathParam("galleryId") String galleryId,
			GallerySlider editData) {
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

// PUT: edit homepage mini gallery
@PUT
@Path(ApiConstants.HOMEPAGE + ApiConstants.EDIT + ApiConstants.MINI_SLIDER + "/{galleryId}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ValidateUser
@Operation(
    summary = "Edit homepage mini gallery data",
    description = "Return home page data"
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = HomePageData.class))),
    @ApiResponse(responseCode = "400", description = "Unable to retrieve the data",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "404", description = "Not found")
})
public Response editMiniHomePage(
    @Context HttpServletRequest request,
    @Parameter(description = "Gallery ID") @PathParam("galleryId") String galleryId,
    @RequestBody(description = "Mini gallery slider edit payload", required = true,
        content = @Content(schema = @Schema(implementation = MiniGallerySlider.class)))
    MiniGallerySlider editData
) {
    try {
        Long gId = Long.parseLong(galleryId);
        HomePageData result = utilityService.editMiniHomePage(request, gId, editData);
        if (result != null) return Response.status(Response.Status.OK).entity(result).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}

// DELETE: delete homepage mini gallery
@DELETE
@Path(ApiConstants.HOMEPAGE + ApiConstants.REMOVE + ApiConstants.MINI_SLIDER + "/{galleryId}")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
@ValidateUser
@Operation(
    summary = "Delete homepage mini gallery data",
    description = "Return home page data"
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = HomePageData.class))),
    @ApiResponse(responseCode = "400", description = "Unable to retrieve the data",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "404", description = "Not found")
})
public Response removeMiniGallery(
    @Context HttpServletRequest request,
    @Parameter(description = "Gallery ID") @PathParam("galleryId") String galleryId
) {
    try {
        Long gId = Long.parseLong(galleryId);
        HomePageData result = utilityService.removeMiniHomePage(request, gId);
        if (result != null) return Response.status(Response.Status.OK).entity(result).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}

// PUT: reorder homepage gallery slider (List body)
@PUT
@Path(ApiConstants.HOMEPAGE + ApiConstants.REORDERING)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ValidateUser
@Operation(
    summary = "Reorder homepage gallery slider",
    requestBody = @RequestBody(
        required = true,
        description = "Array of reorder instructions",
        content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = ReorderHomePage.class))
        )
    )
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Home page data",
        content = @Content(schema = @Schema(implementation = HomePageData.class))),
    @ApiResponse(responseCode = "404", description = "Not found",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "400", description = "Unable to retrieve the data",
        content = @Content(schema = @Schema(implementation = String.class)))
})
public Response reorderingHomePageGallerySlider(
    @Context HttpServletRequest request,
    List<ReorderHomePage> reorderingHomePage
) {
    try {
        HomePageData result = utilityService.reorderHomePageSlider(request, reorderingHomePage);
        if (result != null) return Response.status(Response.Status.OK).entity(result).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}

// PUT: reorder mini homepage gallery slider (List body)
@PUT
@Path(ApiConstants.HOMEPAGE + ApiConstants.MINI_SLIDER + ApiConstants.REORDERING)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ValidateUser
@Operation(
    summary = "Reorder mini homepage gallery slider",
    requestBody = @RequestBody(
        required = true,
        description = "Array of reorder instructions",
        content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = ReorderHomePage.class))
        )
    )
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Home page data",
        content = @Content(schema = @Schema(implementation = HomePageData.class))),
    @ApiResponse(responseCode = "404", description = "Not found",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "400", description = "Unable to retrieve the data",
        content = @Content(schema = @Schema(implementation = String.class)))
})
public Response reorderingMiniHomePageGallerySlider(
    @Context HttpServletRequest request,
    List<ReorderHomePage> reorderingHomePage
) {
    try {
        HomePageData result = utilityService.reorderMiniHomePageSlider(request, reorderingHomePage);
        if (result != null) return Response.status(Response.Status.OK).entity(result).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}
	// Insert list of New Home gallery Data
	@PUT
	@Path(ApiConstants.HOMEPAGE + ApiConstants.INSERT)
	@Consumes(MediaType.APPLICATION_JSON)
	@ValidateUser
	@Operation(summary = "Update homepage gallery data (insert new)", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = HomePageData.class))), responses = {
			@ApiResponse(responseCode = "200", description = "Home page data", content = @Content(schema = @Schema(implementation = HomePageData.class))),
			@ApiResponse(responseCode = "404", description = "Home page gallery not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Unable to retrieve the data", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response updateGalleryData(@Context HttpServletRequest request, HomePageData editData) {
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
	@Operation(summary = "Get the YouTube video title", responses = {
			@ApiResponse(responseCode = "200", description = "The video title", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Unable to get the title", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Get all the Habitat", responses = {
			@ApiResponse(responseCode = "200", description = "All habitat in habitat order", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Habitat.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to get the habitat", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Get resource ids for tags", responses = {
			@ApiResponse(responseCode = "200", description = "Resource ids based on tags", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to get resource ids", content = @Content(schema = @Schema(implementation = String.class))) })
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
}
