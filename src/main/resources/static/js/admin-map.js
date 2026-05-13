var map;
var marker;

async function searchLocation() {
	const addr = document.getElementById('inputAddress').value;
	const msg = document.getElementById('resultMsg');
	if (!addr) return;

	msg.innerText = "🔍 検索中...";

	// 住所の正規化
	const cleanAddr = addr.replace(/(号室|階|F|f).*/, "");
	// 検索精度の向上のため「大阪府」を付与
	const query = "大阪府 " + cleanAddr;
	const baseUrl = "https://nominatim.openstreetmap.org/search";
	const url = baseUrl + "?format=json&countrycodes=jp&addressdetails=1&limit=1&q=" + encodeURIComponent(query);
	try {
		const resp = await fetch(url, { headers: { 'User-Agent': 'KokoroApp/1.0' } });
		const data = await resp.json();

		if (data && data.length > 0) {
			// data[0] から lat と lon を取得
			const result = data[0];
			const lat = result.lat;
			const lon = result.lon;

			// 隠しフィールドに座標をセット
			document.getElementById('finalLat').value = lat;
			document.getElementById('finalLng').value = lon;

			// プレビュー地図の表示
			document.getElementById('previewMap').style.display = 'block';

			if (!map) {
				map = L.map('previewMap').setView([lat, lon], 17);
				L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
			} else {
				map.setView([lat, lon], 17);
			}

			// 既存のマーカーがあれば削除して再配置
			if (marker) map.removeLayer(marker);
			marker = L.marker([lat, lon], { draggable: true }).addTo(map);

			// ドラッグ終了時に座標を更新するイベント
			marker.on('dragend', (e) => {
				const pos = e.target.getLatLng();
				document.getElementById('finalLat').value = pos.lat;
				document.getElementById('finalLng').value = pos.lng;
			});

			msg.innerText = "✅ 場所が見つかりました！";
			msg.style.color = "green";
		} else {
			msg.innerText = "❌ 住所を特定できませんでした。";
			msg.style.color = "red";
		}
	} catch (e) {
		msg.innerText = "⚠️ 検索エラーが発生しました。";
		msg.style.color = "red";
		console.error(e);
	}
}