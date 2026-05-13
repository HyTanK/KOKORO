package com.example.demo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CityConfig {
    public static final Map<String, String> CITY_MAP;

    static {
        Map<String, String> map = new HashMap<>(64);
        map.put("北海道", "Sapporo"); map.put("青森", "Aomori");
        map.put("岩手", "Morioka");   map.put("宮城", "Sendai");
        map.put("秋田", "Akita");     map.put("山形", "Yamagata");
        map.put("福島", "Fukushima"); map.put("茨城", "Mito");
        map.put("栃木", "Utsunomiya"); map.put("群馬", "Maebashi");
        map.put("埼玉", "Saitama");   map.put("千葉", "Chiba");
        map.put("東京", "Tokyo");     map.put("神奈川", "Yokohama");
        map.put("新潟", "Niigata");   map.put("富山", "Toyama");
        map.put("石川", "Kanazawa");  map.put("福井", "Fukui");
        map.put("山梨", "Kofu");      map.put("長野", "Nagano");
        map.put("岐阜", "Gifu");      map.put("静岡", "Shizuoka");
        map.put("愛知", "Nagoya");    map.put("三重", "Tsu");
        map.put("滋賀", "Otsu");      map.put("京都", "Kyoto");
        map.put("大阪", "Osaka");     map.put("兵庫", "Kobe");
        map.put("奈良", "Nara");      map.put("和歌山", "Wakayama");
        map.put("鳥取", "Tottori");   map.put("島根", "Matsue");
        map.put("岡山", "Okayama");   map.put("広島", "Hiroshima");
        map.put("山口", "Yamaguchi"); map.put("徳島", "Tokushima");
        map.put("香川", "Takamatsu"); map.put("愛媛", "Matsuyama");
        map.put("高知", "Kochi");     map.put("福岡", "Fukuoka");
        map.put("佐賀", "Saga");      map.put("長崎", "Nagasaki");
        map.put("熊本", "Kumamoto");  map.put("大分", "Oita");
        map.put("宮崎", "Miyazaki");  map.put("鹿児島", "Kagoshima");
        map.put("沖縄", "Naha");
        CITY_MAP = Collections.unmodifiableMap(map);
    }
}
