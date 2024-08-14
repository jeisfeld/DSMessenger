package de.jeisfeld.coachat.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class DialogToJsonFileConverter3 {

	public static void main(final String[] args) {
		String filePath = "D:\\Git\\DSMessenger\\finetuning\\Dominia\\dialog";
		String inputSuffix = ".txt";
		String outputSuffix = ".json";
		String fullSuffix = "s.jsonl";

		try (BufferedWriter fullWriter = new BufferedWriter(new FileWriter(filePath + fullSuffix))) {
			for (int i = 1; i <= 52; i++) {
				String[] dialog = readDialogFromFile(filePath + i + inputSuffix);
				String jsonOutput = convertDialogToJson(dialog);
				writeJsonToFile(jsonOutput, filePath + i + outputSuffix, fullWriter);
				System.out.println("Conversion completed. Output saved to: " + filePath + i + outputSuffix);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String[] readDialogFromFile(final String filePath) throws IOException {
		return Files.readAllLines(Paths.get(filePath)).toArray(new String[0]);
	}

	private static String convertDialogToJson(final String[] dialog) {
		JSONObject root = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		StringBuffer newJsonContent = new StringBuffer();
		JSONObject jsonEntry = null;

		for (String line : dialog) {
			if (line.trim().isEmpty()) {
				continue; // Skip empty lines
			}

			if (line.startsWith("User:")) {
				if (jsonEntry != null) {
					jsonEntry.put("content", newJsonContent);
					jsonArray.put(jsonEntry);
				}
				jsonEntry = new JSONObject();
				jsonEntry.put("role", "user");
				newJsonContent = new StringBuffer(line.substring(6)); // Remove "User: "
			}
			else if (line.startsWith("AI:")) {
				if (jsonEntry != null) {
					jsonEntry.put("content", newJsonContent);
					jsonArray.put(jsonEntry);
				}
				jsonEntry = new JSONObject();
				jsonEntry.put("role", "assistant");
				newJsonContent = new StringBuffer(line.substring(4)); // Remove "AI: "
			}
			else if (line.startsWith("System:")) {
				if (jsonEntry != null) {
					jsonEntry.put("content", newJsonContent);
					jsonArray.put(jsonEntry);
				}
				jsonEntry = new JSONObject();
				jsonEntry.put("role", "system");
				newJsonContent = new StringBuffer(line.substring(8)); // Remove "System: "
			}
			else {
				newJsonContent.append("\n").append(line);
			}
		}
		if (jsonEntry != null) {
			jsonEntry.put("content", newJsonContent.toString().trim());
			jsonArray.put(jsonEntry);
		}

		root.put("messages", jsonArray);
		return root.toString(); // Single line output
	}

	private static void writeJsonToFile(final String json, final String filePath, final BufferedWriter fullWriter) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(json);
			fullWriter.write(json);
			fullWriter.write("\n");
		}
	}
}
