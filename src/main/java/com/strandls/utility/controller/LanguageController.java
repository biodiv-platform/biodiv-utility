package com.strandls.utility.controller;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.strandls.utility.ApiConstants;
import com.strandls.utility.pojo.Language;
import com.strandls.utility.service.LanguageService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Language Service")
@Path(ApiConstants.V1 + ApiConstants.LANGUAGES)
public class LanguageController {

	@Inject
	private LanguageService languageService;

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
			Language result = languageService.getLanguage(codeType, code);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.LANGUAGES + "/{languageId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Get language by Id", notes = "Returns language", response = Language.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to get the language", response = String.class) })

	public Response fetchLanguageById(@PathParam("languageId") String languageId) {
		try {
			Long langId = Long.parseLong(languageId);
			Language result = languageService.getLanguageById(langId);
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
			Language result = languageService.getLanguageByTwoLetterCode(code);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Save the Language", notes = "Returns the saved Language ", response = Language.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Unable to return the Langauge", response = String.class) })

	public Response save(@ApiParam("language") Language language) {
		try {
			Language result = languageService.save(language);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Save the Language", notes = "Returns the saved Language ", response = Language.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Unable to return the Langauge", response = String.class) })

	public Response update(@QueryParam("languageId") Long languageId, @QueryParam("name") String name) {
		try {
			Language result = languageService.updateName(languageId, name);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

}
