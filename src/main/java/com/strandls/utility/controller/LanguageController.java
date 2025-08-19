package com.strandls.utility.controller;

import java.util.List;

import com.strandls.utility.ApiConstants;
import com.strandls.utility.pojo.Language;
import com.strandls.utility.service.LanguageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Tag(name = "Language Service", description = "APIs for language lookup and management")
@Path(ApiConstants.V1 + ApiConstants.LANGUAGES)
@Produces(MediaType.APPLICATION_JSON)
public class LanguageController {

	@Inject
	private LanguageService languageService;

	@GET
	@Path(ApiConstants.LANGUAGES + "/{code}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Fetch Language by code type and code", description = "Returns Language by codeType and code. CodeType can be 'twoLetterCode' or 'threeLetterCode'.", responses = {
			@ApiResponse(responseCode = "200", description = "Language found", content = @Content(schema = @Schema(implementation = Language.class))),
			@ApiResponse(responseCode = "404", description = "Unable to return the Language", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response getLanguageByCodeType(@QueryParam("codeType") String codeType, @PathParam("code") String code) {
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
	@Operation(summary = "Get language by Id", responses = {
			@ApiResponse(responseCode = "200", description = "Language found", content = @Content(schema = @Schema(implementation = Language.class))),
			@ApiResponse(responseCode = "400", description = "Unable to get the language", content = @Content(schema = @Schema(implementation = String.class))) })
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
	@Operation(summary = "Fetch Language by two letter code", responses = {
			@ApiResponse(responseCode = "200", description = "Language found", content = @Content(schema = @Schema(implementation = Language.class))),
			@ApiResponse(responseCode = "404", description = "Unable to return the Language", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response getLanguageByTwoLetterCodeType(@PathParam("code") String code) {
		try {
			Language result = languageService.getLanguageByTwoLetterCode(code);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Save the Language", requestBody = @RequestBody(required = true, description = "The language to create", content = @Content(schema = @Schema(implementation = Language.class))), responses = {
			@ApiResponse(responseCode = "200", description = "Returns the saved Language", content = @Content(schema = @Schema(implementation = Language.class))),
			@ApiResponse(responseCode = "400", description = "Unable to return the Language", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response save(@Parameter(description = "language", required = true) Language language) {
		try {
			Language result = languageService.save(language);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update a Language name", responses = {
			@ApiResponse(responseCode = "200", description = "Returns the updated Language", content = @Content(schema = @Schema(implementation = Language.class))),
			@ApiResponse(responseCode = "400", description = "Unable to update the Language", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response update(@QueryParam("languageId") Long languageId, @QueryParam("name") String name) {
		try {
			Language result = languageService.updateName(languageId, name);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.FIELD_HEADER)
	@Operation(summary = "Get languages with field headers", responses = {
			@ApiResponse(responseCode = "200", description = "Languages that have associated field headers", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Language.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to fetch languages with field headers", content = @Content(schema = @Schema(implementation = String.class))) })
	public Response getLanguagesWithFieldHeaders() {
		try {
			List<Language> result = languageService.getLanguagesWithFieldHeaders();
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
}
