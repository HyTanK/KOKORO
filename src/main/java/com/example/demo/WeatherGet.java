package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class WeatherGet {
	private final String apiKey = "3b26363f572445b02efe7efbaf0bd6a2";

	// ⚠️【追加】フリーズ防止用の共通HttpClient（接続タイムアウト5秒）
	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	public static class WeatherResult {
		public String temp, weather, high, low, humidity, pressure, alert, alertColor, feelsLike, windSpeed, clouds;
		public double feelsLikeNum;
		public double windSpeedNum;
	}

	public static class ForecastResult {
		public String date, time, temp, weather, feelsLike, windSpeed, clouds;
	}

	public WeatherResult getLiveWeather(String city) {
		WeatherResult result = new WeatherResult();
		try {
			String json = fetchJson("weather", city);
			String wsStr = getValue(json, "\"speed\":", ","); 
			double wsNum = Double.parseDouble(wsStr); 
			double flNum = Double.parseDouble(getValue(json, "\"feels_like\":", ","));
			double temp = Double.parseDouble(getValue(json, "\"temp\":", ","));
			double hum = Double.parseDouble(getValue(json, "\"humidity\":", ","));
			double pre = Double.parseDouble(getValue(json, "\"pressure\":", ","));
			result.feelsLikeNum = flNum;
			result.temp = Math.round(temp) + "°C";
			result.feelsLike = Math.round(Double.parseDouble(getValue(json, "\"feels_like\":", ","))) + "°C";
			result.windSpeed = getValue(json, "\"speed\":", ",") + "m/s";
			result.clouds = getValue(json, "\"all\":", "}") + "%";
			result.high = Math.round(Double.parseDouble(getValue(json, "\"temp_max\":", ","))) + "°C";
			result.low = Math.round(Double.parseDouble(getValue(json, "\"temp_min\":", ","))) + "°C";
			result.humidity = (int) hum + "%";
			result.pressure = (int) pre + "hPa";
			result.feelsLike = Math.round(flNum) + "°C";		
			result.windSpeedNum = wsNum; 
			result.feelsLikeNum = flNum; 
			
			if (pre < 1003) {
				result.alert = "⚠️ 気象病警報";
				result.alertColor = "#805ad5";
			} else if (temp > 31 || (temp > 28 && hum > 70)) {
				result.alert = "🔥 熱中症警報";
				result.alertColor = "#e53e3e";
			} else {
				result.alert = "✅ 異常なし";
				result.alertColor = "#38b2ac";
			}
			result.weather = translate(getValue(json, "\"main\":\"", "\""));
		} catch (Exception e) {
			result.alert = "取得エラー";
			result.weather = "データなし ☁️";
			result.temp = "--°C";
			result.high = "--°C";
			result.low = "--°C";
			result.humidity = "--%";
			result.pressure = "--hPa";
			result.alertColor = "#cbd5e0";
		}
		return result;
	}

	public List<ForecastResult> getWeeklyWeather(String city) {
		return fetchForecast(city, true);
	}

	public List<ForecastResult> getHourlyWeather(String city) {
		return fetchForecast(city, false);
	}

	private List<ForecastResult> fetchForecast(String city, boolean onlyNoon) {
		List<ForecastResult> list = new ArrayList<>();
		try {
			String json = fetchJson("forecast", city);
			String[] blocks = json.split("\"dt\":");

			int count = 0;
			for (int i = 1; i < blocks.length; i++) {
				if (onlyNoon && !blocks[i].contains("12:00:00"))
					continue;
				if (!onlyNoon && count >= 8)
					break;

				ForecastResult f = new ForecastResult();
				String key = "\"dt_txt\":\"";
				int startIdx = blocks[i].indexOf(key);
				if (startIdx == -1)
					continue;

				int start = startIdx + key.length();
				String dtTxt = blocks[i].substring(start, start + 19);

				f.date = dtTxt.substring(5, 10).replace("-", "/");
				f.time = dtTxt.substring(11, 16);
				f.temp = Math.round(Double.parseDouble(getValue(blocks[i], "\"temp\":", ","))) + "°";
				f.weather = translate(getValue(blocks[i], "\"main\":\"", "\""));

				list.add(f);
				count++;
			}
		} catch (Exception e) {
			// フリーズ防止のため、失敗時は空のリストを安全に返す
		}
		return list;
	}

	private String fetchJson(String type, String city) throws Exception {
		String url = "https://api.openweathermap.org/data/2.5/" + type + "?q=" + city + "&appid=" + apiKey
				+ "&units=metric&lang=ja";
		
		// ⚠️【修正】通信タイムアウト（5秒）を設定して無限フリーズを完全に防止
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(5))
				.build();
				
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
	}

	private String getValue(String json, String key, String end) {
		int idx = json.indexOf(key);
		if (idx == -1)
			return "0";
		int start = idx + key.length();
		int last = json.indexOf(end, start);
		if (last == -1)
			last = json.indexOf("}", start);
		String val = json.substring(start, last);
		if (key.contains("main"))
			return val.replace("\"", "");
		return val.replaceAll("[^0-9.-]", "");
	}

	private String translate(String en) {
		if (en.contains("Clear")) return "晴れ ☀️";
		if (en.contains("Clouds")) return "くもり ☁️";
		if (en.contains("Thunderstorm")) return "雷雨 ⚡";
		if (en.contains("Drizzle")) return "小雨 🌦️";
		if (en.contains("Rain")) return "雨 ☔";
		if (en.contains("Snow")) return "雪 ❄️";
		if (en.contains("Mist") || en.contains("Fog") || en.contains("Haze")) return "霧・霞 🌫️";
		if (en.contains("Squall")) return "突風 🌪️";
		if (en.contains("Smoke") || en.contains("Dust") || en.contains("Sand")) return "視界不良 🌫️";
		return en; 
	}

	public String convertCityName(String input) {
		String clean = (input == null) ? "大阪" : input.replaceAll("[都道府県]", "");
		return CityConfig.CITY_MAP.getOrDefault(clean, "Osaka");
	}

	public List<String> getCityList() {
		List<String> cities = new ArrayList<>(CityConfig.CITY_MAP.keySet());
		Collections.sort(cities);
		return cities;
	}
}
