package de.jeisfeld.coachat.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class DialogToJsonFileConverter2 {

	public static void main(final String[] args) {
		String filePath = "D:\\Git\\DSMessenger\\finetuning\\Lasercoaching\\dialog";
		String inputSuffix = ".txt";
		String outputSuffix = ".json";
		String fullSuffix = "s.jsonl";

		try (BufferedWriter fullWriter = new BufferedWriter(new FileWriter(filePath + fullSuffix))) {
			for (int i = 1; i <= 41; i++) {
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

		for (String line : dialog) {
			if (line.trim().isEmpty()) {
				continue; // Skip empty lines
			}

			JSONObject jsonEntry = new JSONObject();
			if (line.startsWith("User:")) {
				jsonEntry.put("role", "user");
				jsonEntry.put("content", line.substring(6).trim()); // Remove "User: "
			}
			else if (line.startsWith("AI:")) {
				jsonEntry.put("role", "assistant");
				jsonEntry.put("content", line.substring(4).trim()); // Remove "AI: "
			}
			else if (line.startsWith("System:")) {
				jsonEntry.put("role", "system");
				jsonEntry.put("content", line.substring(8).trim()); // Remove "System: "
			}
			else if (!line.isEmpty()) {
				System.out.println("Unexpected line: " + line);
			}

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
