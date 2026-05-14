package com.example.demo;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class FortuneApiClient {
	private final String[] contents = {
			"最高の一日になりそう！積極的な行動が吉です。",
			"ラッキーアイテムが幸運を運びます。落ち着いて行動しよう。",
			"周りへの感謝を伝えると、思わぬ助けがあるかも。",
			"今日はのんびり過ごすのがおすすめ。自分を労わって。",
			"笑顔で過ごせば、新しい素敵な繋がりが生まれます。"
	};

	private final String[] items = { "青い小物", "お気に入りの靴", "ハンカチ", "ミントタブレット", "折りたたみ傘" };

	public String getSignEmoji(String sign) {
		if (sign == null)
			return "✨";
		return switch (sign) {
		case "牡羊座" -> "♈";
		case "牡牛座" -> "♉";
		case "双子座" -> "♊";
		case "蟹座" -> "♋";
		case "獅子座" -> "♌";
		case "乙女座" -> "♍";
		case "天秤座" -> "♎";
		case "蠍座" -> "♏";
		case "射手座" -> "♐";
		case "山羊座" -> "♑";
		case "水瓶座" -> "♒";
		case "魚座" -> "♓";
		default -> "✨";
		};
	}

	public FortuneResult fetchFortune(String sign) {
		FortuneResult result = new FortuneResult();
		Random rand = new Random();
		result.sign = getSignEmoji(sign) + " " + (sign != null ? sign : "不明な星座");
		result.content = contents[rand.nextInt(contents.length)];
		result.item = items[rand.nextInt(items.length)];
		result.money = "★".repeat(rand.nextInt(5) + 1);
		result.love = "★".repeat(rand.nextInt(5) + 1);
		result.work = "★".repeat(rand.nextInt(5) + 1);
		return result;
	}
}
