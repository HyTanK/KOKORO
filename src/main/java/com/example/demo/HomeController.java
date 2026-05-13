package com.example.demo;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

	// --- 2. NFC分岐処理（セッション管理の導入） ---
	@GetMapping("/nfc")
	public String handleNfc(
			@RequestParam(value = "role", required = false) String role,
			HttpSession session) { // HttpSessionをインジェクション

		if ("user".equals(role)) {
			// NFCタグから正しくアクセスされた証拠として、セッションにフラグを保存
			session.setAttribute("nfc_authenticated", true);
			return "redirect:/user"; // URLパラメータから key=nfc_success を削除（隠蔽）
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
			return "admin";
		}

		// --- 4. 利用者ダッシュボード表示（直リンク防止・DBアクセス最適化） ---
		@GetMapping("/user")
		public String user(
				@RequestParam(value = "city", required = false) String city,
				@RequestParam(value = "sign", required = false) String sign,
				HttpSession session, // HttpSessionをインジェクション
				Model model) {

			// セッションチェック：NFC経由フラグがなければトップへ強制リダイレクト
			Boolean isAuthenticated = (Boolean) session.getAttribute("nfc_authenticated");
			if (isAuthenticated == null || !isAuthenticated) {
				return "redirect:/";
			}

			// 【重要】一度画面を表示したらセッションフラグを即座に削除（リロードや直リンクでの再進入を防止）
			session.removeAttribute("nfc_authenticated");

			// DBアクセスの最適化：すべてのスポット情報を一度だけ取得して使い回す
			List<Spot> allSpots = spotService.getAllSpots();

			// 設定の取得
			Spot config = allSpots.stream()
					.filter(s -> "user_config".equals(s.getCategory()))
					.findFirst()
					.orElse(null);

			String fullAddress = (city != null && !city.isEmpty()) ? city
					: (config != null ? config.getAddress() : "大阪府大阪市");
			String displaySign = (sign != null && !sign.isEmpty()) ? sign
					: (config != null ? config.getDescription() : "牡羊座");

			// 住所の分割ロジック（※既存ロジックを維持）
			String prefPart = "", cityPart = fullAddress;
			int idx = fullAddress.indexOf("県");
			if (idx == -1) idx = fullAddress.indexOf("府");
			if (idx == -1) idx = fullAddress.indexOf("都");
			if (idx == -1) idx = fullAddress.indexOf("道");
			if (idx != -1) {
				prefPart = fullAddress.substring(0, idx + 1);
				cityPart = fullAddress.substring(idx + 1);
			}
			if (!cityPart.endsWith("市") && !cityPart.endsWith("区") && !cityPart.endsWith("町") && !cityPart.endsWith("村")) {
				cityPart += "市";
			}

			// データの詰め込み
			model.addAttribute("musicList", allSpots.stream().filter(s -> "music_config".equals(s.getCategory())).toList());
			model.addAttribute("savedPref", prefPart);
			model.addAttribute("savedCity", cityPart);
			model.addAttribute("selectedSign", displaySign);

			String cityEn = weatherGet.convertCityName(cityPart);
			model.addAttribute("weather", weatherGet.getLiveWeather(cityEn));
			model.addAttribute("weeklyWeather", weatherGet.getWeeklyWeather(cityEn));
			model.addAttribute("hourlyWeather", weatherGet.getHourlyWeather(cityEn));
			model.addAttribute("fortune", fortuneApiClient.fetchFortune(displaySign));
			model.addAttribute("spotList", allSpots);

			try {
				model.addAttribute("newsList", newsApiClient.fetchNewsItems());
			} catch (Exception e) {
				// ニュース取得失敗時は無視
			}

			return "user";
		}
		
		// --- 5. AIチャットAPI ---
		@PostMapping("/api/chat")
		@ResponseBody
		public Map<String, String> chat(@RequestBody Map<String, String> request) {
		    String reply = chatService.getAiResponse(request);
		    return Map.of("reply", reply);
		}
	}