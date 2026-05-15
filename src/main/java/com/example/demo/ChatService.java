package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ChatService {
	public String getAiResponse(Map<String, String> data) {

		// Renderの環境変数からAPIキーを読み取る
		String apiKey = System.getenv("GEMINI_API_KEY");

		// APIキーがない場合は警告（ローカル実行時など）
		if (apiKey == null || apiKey.isEmpty()) {
			return "APIキーが設定されていないよ。RenderのEnvironment設定を確認してね。";
		}

		try {
			// Google Gemini API のURL
			String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
					+ apiKey;

			String userMessage = data.get("message");
			String promptText = """
					あなたは『くらし救急箱』のガイド、kokoroです。
					【今の状況】現在日時：%s、場所：%s、天気：%s、占い：%s、ニュース：%s
					【ルール】
					1. おっとりした優しい日本語（タメ口）で話す。
					2. ユーザーの言葉に反応しつつ、状況から1つ話題を選んで触れる。
					3. 日本語のみで2〜3文。英語禁止。
					ユーザー：%s
					kokoro：""".formatted(
					data.get("dateTime"), data.get("city"), data.get("weather"),
					data.get("fortune"), data.get("news"), userMessage);

			// Gemini用のJSON形式に整形
			String jsonPayload = """
					{
					  "contents": [{
					    "parts":[{"text": %s}]
					  }]
					}
					""".formatted(quoteJson(promptText));

			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(10))
					.build();

			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(apiUrl))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
					.build();

			HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

			// 応答の解析（簡易版）
			return parseGeminiResponse(resp.body());

		} catch (Exception e) {
			e.printStackTrace();
			return "ごめんね、うまく繋がらなかったみたい。";
		}
	}

	private String quoteJson(String text) {
		if (text == null)
			return "\"\"";
		return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
	}

	private String parseGeminiResponse(String body) {
		try {
			// Geminiのレスポンス構造からテキスト部分を抽出
			String key = "\"text\": \"";
			int start = body.indexOf(key);
			if (start == -1)
				return "ちょっと考えがまとまらなかったよ。";
			start += key.length();
			int end = body.indexOf("\"", start);
			return body.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
		} catch (Exception e) {
			return "言葉が見つからないよ。";
		}
	}
}
