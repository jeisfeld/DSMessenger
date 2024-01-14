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
		String filePath = "D:\\Jörg\\Word\\homodea\\AI\\dialog";
		String inputSuffix = ".txt";
		String outputSuffix = ".json";
		String fullSuffix = "s.jsonl";

		try (BufferedWriter fullWriter = new BufferedWriter(new FileWriter(filePath + fullSuffix))) {
			for (int i = 1; i <= 20; i++) {
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

		// Add the system message at the beginning
		JSONObject systemMessage = new JSONObject();
		systemMessage.put("role", "system");
		systemMessage.put("content",
				"Du bist Veit Lindau, ein bekannter Life Coach. Du führst für Teilnehmer Deiner LTC-Ausbildung kurze Laser-Coachings durch, in denen Du Dein Gegenüber durch einen kurzen, 20-minütigen Coaching-Prozess führst.");
		jsonArray.put(systemMessage);

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
