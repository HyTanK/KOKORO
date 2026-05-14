-- 1件目：ユーザー設定（user_config）の初期データ
INSERT INTO spot (category, address, building_name, description, lat, lng, name, nfc_address, owner_name) 
VALUES ('user_config', '大阪府大阪市', 'サンプルビル1F', '牡羊座', 34.6937, 135.5023, '初期ユーザー設定', 'nfc_001', '管理者A');

-- 2件目：音楽設定（music_config）の初期データ
INSERT INTO spot (category, address, building_name, description, lat, lng, name, nfc_address, owner_name) 
VALUES ('music_config', '東京都新宿区', '音楽スタジオB', 'サンプル曲：歓喜の歌', 35.6895, 139.6917, '初期音楽設定', 'nfc_002', '管理者B');
