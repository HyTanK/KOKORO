package com.example.demo;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId; // 利用者を識別するためのNFC用ID
	private String sender; // 送信者（"USER" または "ADMIN"）
	private String message; // メッセージ本文
	private LocalDateTime timestamp; // 送信日時

	// コンストラクタ
	public ChatMessage(String sender, String message) {
		this.sender = sender;
		this.message = message;
		this.timestamp = LocalDateTime.now();
	}
}
