package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class WeatherGet {

	private final ObjectMapper mapper = new ObjectMapper();
	private final HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	// 都市名変換マップ
	public String convertCityName(String cityJp) {
		if (cityJp == null)
			return "Tokyo";
		for (Map.Entry<String, String> entry : CityConfig.CITY_MAP.entrySet()) {
			if (cityJp.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return "Tokyo";
	}

	// 都市名から緯度・経度のパラメータを返す補助メソッド
	private String getCoordinateParams(String cityEn) {
		// デフォルト（東京）
		double lat = 35.6785;
		double lon = 139.6823;

		if ("Sapporo".equals(cityEn)) {
			lat = 43.0667;
			lon = 141.3500;
		} else if ("Osaka".equals(cityEn)) {
			lat = 34.6937;
			lon = 135.5023;
		} else if ("Fukuoka".equals(cityEn)) {
			lat = 33.6064;
			lon = 130.4181;
		}

		return "latitude=" + lat + "&longitude=" + lon;
	}

	// 現在の天気取得
	public Map<String, String> getLiveWeather(String cityEn) {
		Map<String, String> result = new HashMap<>();
		try {
			String url = "https://api.open-meteo.com/v1/forecast?"
					+ getCoordinateParams(cityEn)
					+ "&current=temperature_2m,weather_code&timezone=Asia%2FTokyo";

			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
			HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

			if (resp.statusCode() == 200) {
				JsonNode root = mapper.readTree(resp.body());
				JsonNode current = root.get("current");
				if (current != null) {
					JsonNode tempNode = current.get("temperature_2m");
					JsonNode codeNode = current.get("weather_code");

					result.put("temp", tempNode != null ? tempNode.asDouble() + "℃" : "--℃");
					result.put("code", codeNode != null ? getWeatherText(codeNode.asInt()) : "情報なし");
					return result;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		result.put("temp", "--℃");
		result.put("code", "情報なし");
		return result;
	}

	// 週間天気取得
	public List<Map<String, String>> getWeeklyWeather(String cityEn) {
		List<Map<String, String>> list = new ArrayList<>();
		try {
			String url = "https://api.open-meteo.com/v1/forecast?"
					+ getCoordinateParams(cityEn)
					+ "&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=Asia%2FTokyo";

			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
			HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

			if (resp.statusCode() == 200) {
				JsonNode root = mapper.readTree(resp.body());
				JsonNode daily = root.get("daily");
				if (daily != null) {
					JsonNode timeNode = daily.get("time");
					JsonNode codeNode = daily.get("weather_code");
					JsonNode maxNode = daily.get("temperature_2m_max");
					JsonNode minNode = daily.get("temperature_2m_min");

					if (timeNode != null) {
						for (int i = 0; i < timeNode.size(); i++) {
							Map<String, String> map = new HashMap<>();
							JsonNode t = timeNode.get(i);
							JsonNode c = codeNode != null ? codeNode.get(i) : null;
							JsonNode mx = maxNode != null ? maxNode.get(i) : null;
							JsonNode mn = minNode != null ? minNode.get(i) : null;

							map.put("date", t != null ? t.asString().substring(5) : "--");
							map.put("code", c != null ? getWeatherText(c.asInt()) : "くもり");
							map.put("max", mx != null ? mx.asDouble() + "℃" : "--℃");
							map.put("min", mn != null ? mn.asDouble() + "℃" : "--℃");
							list.add(map);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// 24時間天気取得
	public List<Map<String, String>> getHourlyWeather(String cityEn) {
		List<Map<String, String>> list = new ArrayList<>();
		try {
			String url = "https://api.open-meteo.com/v1/forecast?"
					+ getCoordinateParams(cityEn)
					+ "&hourly=temperature_2m,weather_code&timezone=Asia%2FTokyo&forecast_days=2";

			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
			HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

			if (resp.statusCode() == 200) {
				JsonNode root = mapper.readTree(resp.body());
				JsonNode hourly = root.get("hourly");
				if (hourly != null) {
					JsonNode timeNode = hourly.get("time");
					JsonNode tempNode = hourly.get("temperature_2m");
					JsonNode codeNode = hourly.get("weather_code");

					if (timeNode != null) {
						int currentHour = 6; // 👈 2行目を「6」に変えます

						for (int i = currentHour; i < currentHour + 24 && i < timeNode.size(); i++) { // 👈 4行目の i = 0 を i = currentHour に変えます
							Map<String, String> map = new HashMap<>();
							JsonNode t = timeNode.get(i);
							JsonNode tp = tempNode != null ? tempNode.get(i) : null;
							JsonNode c = codeNode != null ? codeNode.get(i) : null;

							String rawTime = t != null ? t.asString() : "0000-00-00T00:00";
							String timeStr = rawTime.contains("T") ? rawTime.split("T")[1] : "00:00";

							map.put("time", timeStr);
							map.put("temp", tp != null ? tp.asDouble() + "℃" : "--℃");
							map.put("code", c != null ? getWeatherText(c.asInt()) : "くもり");
							list.add(map);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// 天気コード変換
	private String getWeatherText(int code) {
		if (code == 0)
			return "☀️ 晴れ";
		if (code >= 1 && code <= 3)
			return "☁️ くもり";
		if (code >= 45 && code <= 48)
			return "🌫️ 霧";
		if (code >= 51 && code <= 55)
			return "🌧️ 霧雨";
		if (code >= 61 && code <= 65)
			return "☔ 雨";
		if (code >= 71 && code <= 75)
			return "❄️ 雪";
		if (code >= 80 && code <= 82)
			return "🌧️ にわか雨";
		if (code >= 95)
			return "⛈️ 雷雨";
		return "☁️ くもり";
	}
}
