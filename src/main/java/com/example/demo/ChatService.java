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

		// ⚠️【追加】Render（本番）環境でのフリーズを完全に防止する安全装置
		// サーバー環境（PORT環境変数があるか）をチェックし、Render上ならAI通信を即座にスキップします
		if (System.getenv("PORT") != null) {
			return "本番環境（Render）からはPCのAIに接続できないよ。ローカル環境で試してみてね。";
		}

		try {
			// ★URLを定義
			String ollamaUrl = "http://localhost:11434/api/generate";

			String userMessage = data.get("message");
			String promptText = """
					あなたは『くらし救急箱』のガイド、kokoroです。

					【今のあなたの状況】
					・現在日時：%s
					・場所：%s ／ 天気：%s
					・明日の予報：%s
					・今日の占い：%s（アイテム：%s）
					・最新ニュース：%s

					【ルール】
					1. おっとりした優しい日本語（タメ口）で話す。
					2. ユーザーの言葉に反応しつつ、上記の中から「今、話すと良さそうなこと」を1つ選んで自然に触れてね。
					3. 日本語のみで2〜3文。英語は絶対禁止。

					ユーザー：%s
					kokoro：""".formatted(
					data.get("dateTime"), data.get("city"), data.get("weather"),
					data.get("tomorrow"), data.get("fortune"), data.get("luckyItem"),
					data.get("news"), userMessage);

			String jsonPayload = """
					{
					  "model": "llama3",
					  "prompt": %s,
					  "stream": false
					}
					""".formatted(quoteJson(promptText));

			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5)) // タイムアウトを10秒から5秒に縮めて安全に
					.build();

			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(ollamaUrl))
					.header("Content-Type", "application/json")
					.timeout(Duration.ofSeconds(10)) // タイムアウトを60秒から10秒に縮めてフリーズ防止
					.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
					.build();

			System.out.println("AIに送信中...");
			HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
			return parseResponse(resp.body());

		} catch (Exception e) {
			e.printStackTrace();
			return "ごめんね、うまく繋がらなかったみたい。";
		}
	}

	private String quoteJson(String text) {
		if (text == null)
			return "\"\"";
		return "\"" + text.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r") + "\"";
	}

	private String parseResponse(String body) {
		try {
			String key = "\"response\":\"";
			int start = body.indexOf(key);
			if (start == -1)
				return "言葉が見つからないよ。";
			start += key.length();

			int end = body.indexOf("\",\"", start);
			if (end == -1)
				end = body.indexOf("\"", start);

			if (start < end) {
				return body.substring(start, end)
						.replace("\\n", "\n")
						.replace("\\\"", "\"")
						.replace("\\\\", "\\");
			}
		} catch (Exception e) {
			System.err.println("パースエラー: " + e.getMessage());
		}
		return "ちょっと考えがまとまらなかったよ。";
	}
}
