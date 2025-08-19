package com.strandls.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ModifyOpenApi {
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: ModifyOpenApi <path-to-openapi.json>");
			return;
		}

		// Load config.properties
		Properties properties = new Properties();
		ClassLoader classLoader = ModifyOpenApi.class.getClassLoader();
		URL resourceUrl = classLoader.getResource("config.properties");

		if (resourceUrl != null) {
			System.out.println("config.properties found at: " + resourceUrl);
			try (InputStream input = resourceUrl.openStream()) {
				properties.load(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("config.properties not found. Proceeding with defaults.");
		}

		// Get properties with defaults
		String title = properties.getProperty("title", "");
		String description = properties.getProperty("description", "");
		String version = properties.getProperty("version", "");

		String schemes = properties.getProperty("schemes", "http");
		String host = properties.getProperty("host", "localhost");
		String basePath = properties.getProperty("basePath", "");

		if (!basePath.startsWith("/") && !basePath.isEmpty()) {
			basePath = "/" + basePath;
		}

		String serverUrl = schemes + "://" + host + basePath;

		// Read OpenAPI file
		File openApiFile = new File(args[0]);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = (ObjectNode) mapper.readTree(openApiFile);

		// Set or update info
		ObjectNode info = mapper.createObjectNode();
		info.put("title", title);
		info.put("description", description);
		info.put("version", version);
		root.set("info", info);

		// Set servers array
		ArrayNode servers = mapper.createArrayNode();
		ObjectNode server = mapper.createObjectNode();
		server.put("url", serverUrl);
		servers.add(server);
		root.set("servers", servers);

		// Write modified file
		mapper.writerWithDefaultPrettyPrinter().writeValue(openApiFile, root);
		System.out.println("OpenAPI file updated successfully.");
	}
}
