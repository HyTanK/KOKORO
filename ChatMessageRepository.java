package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	// 💡 追加：特定の利用者(userId)のメッセージ履歴だけを古い順に取得します
	List<ChatMessage> findAllByUserIdOrderByTimestampAsc(Long userId);
}
