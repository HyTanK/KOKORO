// 地図の初期設定（枚方市周辺）
var map = L.map('map').setView([34.8147, 135.6512], 14);

// 地図のデザイン（OpenStreetMap）を読み込む
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

// アイコンの設定
var sosIcon = L.icon({ iconUrl: '/images/sos.png', iconSize: [50, 50], iconAnchor: [25, 50], popupAnchor: [0, -45] });
var heroIcon = L.icon({ iconUrl: '/images/hero.png', iconSize: [50, 50], iconAnchor: [25, 50], popupAnchor: [0, -45] });

// データベースの地点を表示
if (typeof dbSpots !== 'undefined') {
    dbSpots.forEach(s => {
        // 非公開タグや座標がないものはスキップ
        if (s.category === 'private' || !s.lat) return;

        // マーカーを作成
        var marker = L.marker([s.lat, s.lng], { 
            icon: s.category === 'sos' ? sosIcon : heroIcon 
        }).addTo(map);

        // 【修正完了】ポップアップの中身（案内ボタンのURLを正しく修正）
        marker.bindPopup(`
            <b>${s.name}</b><br>
            <small>${s.address}</small><br>
            <a href="https://www.google.com/maps/dir/?api=1&destination=${s.lat},${s.lng}" 
               target="_blank" 
               style="display:block; background:#4285F4; color:white; text-align:center; padding:10px; border-radius:5px; margin-top:8px; text-decoration:none; font-weight:bold;">
               案内開始
            </a>
        `);
    });
}
