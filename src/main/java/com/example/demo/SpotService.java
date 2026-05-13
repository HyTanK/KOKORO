package com.example.demo;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpotService {
	@Autowired
	private SpotRepository spotRepository;

	public List<Spot> getAllSpots() {
		return spotRepository.findAll();
	}

	public void saveSpot(Spot spot) {
		spotRepository.save(spot);
	}

	public void deleteSpot(Long id) {
		spotRepository.deleteById(id);
	}

	public void updateUserConfig(String address, String sign) {
		Spot settings = spotRepository.findAll().stream()
				.filter(s -> "user_config".equals(s.getCategory())).findFirst().orElse(new Spot());
		settings.setCategory("user_config");
		settings.setName("ユーザー設定");
		if (address != null)
			settings.setAddress(address);
		if (sign != null)
			settings.setDescription(sign);
		spotRepository.save(settings);
	}

	@Transactional
	public void updateMusicConfig(String title, String author, String fileName) {
		Spot music = new Spot();
		music.setCategory("music_config");
		music.setName(title); // 曲名
		music.setOwnerName(author); // 作者名
		music.setNfcAddress(fileName); // ファイルパス
		spotRepository.save(music);
	}
}