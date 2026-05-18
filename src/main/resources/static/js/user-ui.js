// --- 共通・ユーティリティ ---
function toggleForm(id) {
	const f = document.getElementById(id);
	if (f) f.style.display = (f.style.display === 'none' || f.style.display === '') ? 'block' : 'none';
}

// --- 音楽プレイヤー関連 ---
function changeSong() {
	const select = document.getElementById('songSelect');
	const audio = document.getElementById('myAudio');
	const songInfo = document.getElementById('songInfo');
	const displayTitle = document.getElementById('displayTitle');
	const displayAuthor = document.getElementById('displayAuthor');

	if (select && audio) {
		const selectedOption = select.options[select.selectedIndex];
		const fileUrl = selectedOption.value;

		if (fileUrl) {
			audio.src = fileUrl;
			audio.play().catch(e => console.log("Playback blocked:", e));
			const title = selectedOption.getAttribute('data-title');
			const author = selectedOption.getAttribute('data-author');

			if (title) {
				displayTitle.innerText = title;
				displayAuthor.innerText = (author && author !== 'null') ? author : "不明";
				songInfo.style.display = 'block';
			}
		}
	}
}

function playMusic() {
	const player = document.getElementById('MusicPlayer'); // ← youtubePlayer から修正
	const chat = document.getElementById('chatInline');
	const audio = document.getElementById('myAudio');

	if (player.style.display === 'none' || player.style.display === '') {
		player.style.display = 'block';
		if (chat) chat.style.display = 'none';
	} else {
		player.style.display = 'none';
		if (audio) { audio.pause(); audio.currentTime = 0; }
	}
}

// --- AIチャット関連（インライン版） ---
function toggleChatInline() {
	const chat = document.getElementById('chatInline');
	const music = document.getElementById('youtubePlayer');
	const audio = document.getElementById('myAudio');

	if (chat.style.display === 'none' || chat.style.display === '') {
		chat.style.display = 'flex';
		if (music) music.style.display = 'none'; // 音楽プレイヤーが開いていたら閉じる
		if (audio) { audio.pause(); audio.currentTime = 0; }
		const input = document.getElementById('chatInput');
		if (input) input.focus();
	} else {
		chat.style.display = 'none';
	}
}

async function sendChatMessage() {
	const input = document.getElementById('chatInput');
	if (!input) return;
	const message = input.value.trim();
	if (!message) return;

	// ⭕ 1. 修正：サーバーに依存せず、スマホやPCの日本時間を正しく取得する
	const now = new Date();
	const days = ['日', '月', '火', '水', '木', '金', '土'];
	const timeInfo = `${now.getMonth() + 1}/${now.getDate()}(${days[now.getDay()]}) ${now.getHours()}:${now.getMinutes().toString().padStart(2, '0')}`;

	// 2. ラッキーアイテム (安全に取得)
	const itemText = document.querySelector('.fortune-box p[style*="color:#ff9800"]')?.innerText || "なし";
	const luckyItem = itemText.replace('🍀 アイテム: ', '');

	// ⭕ 3. 修正：th\\:eachを廃止し、クラス名や要素の順番から安全に取得する
	// info-cardの中にある予報要素（div）から2番目（明日のデータ）を取得
	const tomorrowWeatherEl = document.querySelectorAll('.info-card div')[1];
	const tomorrowInfo = tomorrowWeatherEl ? tomorrowWeatherEl.innerText.replace(/\n/g, ' ') : "不明";

	// 4. ニュースの最新3件 (安全に取得)
	const newsEls = document.querySelectorAll('.info-card ul li a');
	const newsHeadlines = newsEls.length > 0 ? Array.from(newsEls).slice(0, 3).map(el => el.innerText).join('／') : "なし";

	// --------------------------------

	addMessage("user", message);
	input.value = "";
	const loadingId = addMessage("kokoro", "考え中...");

	try {
		const response = await fetch('/api/chat', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({
				message: message,
				city: document.querySelector('.weather-header h2 span')?.innerText || "設定地域",
				weather: document.querySelector('.weather-main-temp')?.innerText || "",
				fortune: document.querySelector('.fortune-box p')?.innerText || "",
				luckyItem: luckyItem,
				tomorrow: tomorrowInfo,
				news: newsHeadlines,
				dateTime: timeInfo
			})
		});

		if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
		const data = await response.json();

		const loadingElement = document.getElementById(loadingId);
		if (loadingElement) {
			loadingElement.closest('.chat-wrapper').remove();
		}
		// 「reply」キーでメッセージを表示
		addMessage("kokoro", data.reply || "言葉が見つからないよ。");

	} catch (e) {
		console.error("Chat Error:", e);
		const loadingElement = document.getElementById(loadingId);
		if (loadingElement) {
			loadingElement.innerText = "ごめんね、うまく繋がらなかったみたい。";
		}
	}
}

function addMessage(sender, text) {
	const log = document.getElementById('chatLog');
	const wrapper = document.createElement('div');
	wrapper.style.display = "flex";
	wrapper.style.alignItems = "flex-end";
	wrapper.style.marginBottom = "10px";

	// 【重要】考え中を消す際の間違いを防ぐため、親要素にクラス名を付けておく
	wrapper.className = "chat-wrapper";

	if (sender === "user") {
		wrapper.style.justifyContent = "flex-end";
	} else {
		wrapper.style.justifyContent = "flex-start";
		const icon = document.createElement('img');
		icon.src = "/images/kokoro.png";
		icon.style.width = "35px";
		icon.style.height = "35px";
		icon.style.borderRadius = "50%";
		icon.style.marginRight = "8px";
		wrapper.appendChild(icon);
	}

	const msgDiv = document.createElement('div');
	const id = "msg-" + Date.now() + "-" + Math.floor(Math.random() * 1000);
	msgDiv.id = id; // ★これが無いと「考え中」が消せません
	msgDiv.style.padding = "10px 14px";
	msgDiv.style.borderRadius = "18px";
	msgDiv.style.maxWidth = "70%";
	msgDiv.style.fontSize = "0.9em";
	msgDiv.style.wordBreak = "break-all"; // 長い文の改行対策

	if (sender === "user") {
		msgDiv.style.background = "#00acc1";
		msgDiv.style.color = "white";
		msgDiv.style.borderRadius = "18px 18px 0 18px";
	} else {
		msgDiv.style.background = "white";
		msgDiv.style.color = "#333";
		msgDiv.style.borderRadius = "18px 18px 18px 0";
	}

	msgDiv.innerText = text;
	wrapper.appendChild(msgDiv);
	log.appendChild(wrapper);
	log.scrollTop = log.scrollHeight;
	return id;
}

function updateMessage(id, text) {
	const msgDiv = document.getElementById(id);
	if (msgDiv) msgDiv.innerText = text;
}

// --- 初期化処理 ---
document.addEventListener('DOMContentLoaded', function() {
	const input = document.getElementById('chatInput');
	if (input) {
		input.addEventListener('keypress', function(e) {
			if (e.key === 'Enter') sendChatMessage();
		});
	}

	// 成功メッセージのフェードアウト（あれば）
	const msg = document.getElementById('successMessage');
	if (msg) {
		setTimeout(() => {
			msg.style.transition = 'opacity 1s ease'; msg.style.opacity = '0';
			setTimeout(() => msg.style.display = 'none', 1000);
		}, 3000);
	}
});
