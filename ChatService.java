package com.example.demo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	/**
	 * 💡 利用者（USER）からのメッセージを受け取って、その人の部屋（userId）に保存する
	 */
	public String getAiResponse(Map<String, String> data) {
		try {
			String userMessage = data.get("message");

			// コントローラーから送られてくるデータから、NFCのID（userId）を安全に取得します
			String userIdStr = data.get("userId");
			Long userId = (userIdStr != null) ? Long.parseLong(userIdStr) : 1L; // デフォルトは1

			if (userMessage == null || userMessage.trim().isEmpty()) {
				return "メッセージが空っぽだよ。";
			}

			// 💡 送信者、本文に加えて「誰のチャット部屋か(userId)」をセットして保存します
			ChatMessage chatMessage = new ChatMessage("USER", userMessage);
			chatMessage.setUserId(userId);
			chatMessageRepository.save(chatMessage);

			return "SUCCESS";

		} catch (Exception e) {
			e.printStackTrace();
			return "ごめんね、メッセージを送れなかったみたい。";
		}
	}

	/**
	 * 💡 職員（ADMIN）からの返信を受け取って、特定の利用者の部屋（userId）に保存する
	 */
	public String saveAdminResponse(Long userId, String adminMessage) {
		try {
			if (adminMessage == null || adminMessage.trim().isEmpty()) {
				return "返信メッセージが空です。";
			}

			// どの利用者への返信かを明確にするため、userIdをセットして保存
			ChatMessage chatMessage = new ChatMessage("ADMIN", adminMessage);
			chatMessage.setUserId(userId);
			chatMessageRepository.save(chatMessage);

			return "SUCCESS";
		} catch (Exception e) {
			e.printStackTrace();
			return "エラーが発生しました。";
		}
	}

	/**
		 * 💡 特定の利用者（userId）の過去のチャット履歴だけを古い順に取得する
		 */
	public List<ChatMessage> getChatHistory(Long userId) {
		return chatMessageRepository.findAllByUserIdOrderByTimestampAsc(userId);
	}
}
