package com.example.demo;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

	@Autowired
	private WeatherGet weatherGet;
	@Autowired
	private NewsApiClient newsApiClient;
	@Autowired
	private FortuneApiClient fortuneApiClient;
	@Autowired
	private SpotService spotService;
	@Autowired
	private ChatService chatService;

	// --- 1. トップ画面 ---
	@GetMapping("/")
	public String index() {
		return "index";
	}

	// --- 2. NFC分岐処理 ---
	@GetMapping("/nfc")
	public String handleNfc(
			@RequestParam(required = false) String role,
			@RequestParam(required = false) Long userId,
			HttpSession session) {

		if ("user".equals(role)) {
			session.setAttribute("nfc_authenticated", true);
			session.setAttribute("chat_user_id", userId != null ? userId : 1L);
			return "redirect:/user";
		}
		if ("admin".equals(role)) {
			return "redirect:/admin";
		}
		return "redirect:/";
	}

	// --- 3. 管理者画面表示 ---
	@GetMapping("/admin")
	public String admin(Model model) {
		model.addAttribute("spotList", spotService.getAllSpots());
		model.addAttribute("chatHistory", chatService.getChatHistory(1L));
		return "admin";
	}

	// --- 4. 利用者ダッシュボード表示 ---
	@GetMapping("/user")
	public String user(
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String sign,
			HttpSession session,
			Model model) {

		// セッションチェック
		Boolean isAuthenticated = (Boolean) session.getAttribute("nfc_authenticated");
		if (isAuthenticated == null || !isAuthenticated) {
			return "redirect:/";
		}

		Long currentUserId = (Long) session.getAttribute("chat_user_id");
		if (currentUserId == null)
			currentUserId = 1L;

		List<ChatMessage> history = chatService.getChatHistory(currentUserId);
		model.addAttribute("chatHistory", history);
		model.addAttribute("currentUserId", currentUserId);

		// DBアクセスの最適化
		List<Spot> allSpots = spotService.getAllSpots();

		// 設定の取得
		Spot config = null;
		if (allSpots != null) {
			config = allSpots.stream()
					.filter(s -> "user_config".equals(s.getCategory()))
					.findFirst()
					.orElse(null);
		}

		String fullAddress = "大阪府大阪市";
		if (city != null && !city.isEmpty()) {
			fullAddress = city;
		} else if (config != null && config.getAddress() != null && !config.getAddress().isEmpty()) {
			fullAddress = config.getAddress();
		}

		String displaySign = "牡羊座";
		if (sign != null && !sign.isEmpty()) {
			displaySign = sign;
		} else if (config != null && config.getDescription() != null && !config.getDescription().isEmpty()) {
			displaySign = config.getDescription();
		}

		// 住所の分割ロジック
		String prefPart = "", cityPart = fullAddress;
		int idx = fullAddress.indexOf("県");
		if (idx == -1)
			idx = fullAddress.indexOf("府");
		if (idx == -1)
			idx = fullAddress.indexOf("都");
		if (idx == -1)
			idx = fullAddress.indexOf("道");
		if (idx != -1) {
			prefPart = fullAddress.substring(0, idx + 1);
			cityPart = fullAddress.substring(idx + 1);
		}
		if (!cityPart.endsWith("市") && !cityPart.endsWith("区") && !cityPart.endsWith("町") && !cityPart.endsWith("村")) {
			cityPart += "市";
		}

		// データの詰め込み
		if (allSpots != null) {
			model.addAttribute("musicList",
					allSpots.stream().filter(s -> "music_config".equals(s.getCategory())).toList());
			model.addAttribute("spotList", allSpots);
		} else {
			model.addAttribute("musicList", java.util.Collections.emptyList());
			model.addAttribute("spotList", java.util.Collections.emptyList());
		}

		model.addAttribute("savedPref", prefPart);
		model.addAttribute("savedCity", cityPart);
		model.addAttribute("selectedSign", displaySign);

		// 外部API呼び出し
		String cityEn = weatherGet.convertCityName(cityPart);
		model.addAttribute("weather", weatherGet.getLiveWeather(cityEn));
		model.addAttribute("weeklyWeather", weatherGet.getWeeklyWeather(cityEn));
		model.addAttribute("hourlyWeather", weatherGet.getHourlyWeather(cityEn));
		model.addAttribute("fortune", fortuneApiClient.fetchFortune(displaySign));

		try {
			model.addAttribute("newsList", newsApiClient.fetchNewsItems());
		} catch (Exception e) {
			model.addAttribute("newsList", java.util.Collections.emptyList());
		}

		return "user";
	}

	// --- 5. サポートチャット（Q&A）送信処理（型不一致バグを完全に修正版） ---
	@PostMapping("/user/question")
	public String handleQuestion(@RequestParam String question, HttpSession session) {
		// セッションからこのNFCタグの所有者ID（userId）を安全に引き出す
		Long currentUserId = (Long) session.getAttribute("chat_user_id");
		if (currentUserId == null)
			currentUserId = 1L;

		if (question != null && !question.trim().isEmpty()) {
			// 新生ChatServiceを利用し、送信者「USER」としてデータベースに保存
			java.util.Map<String, String> data = new java.util.HashMap<>();
			data.put("message", question);
			data.put("userId", String.valueOf(currentUserId));
			chatService.getAiResponse(data);
		}

		// 💡 正しい戻り値：画面をリダイレクトさせるためにString型の文字列を返します
		return "redirect:/user";
	}
}
