package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConfigController {

    @Autowired
    private SpotService spotService;

    // ユーザー設定（住所・星座）の保存
    @PostMapping("/user/save-config")
    public String saveConfig(
            @RequestParam(required = false) String pref, 
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sign, 
            RedirectAttributes ra) {
        
        if (city != null && !city.isEmpty()) {
            spotService.updateUserConfig((pref != null ? pref : "") + city, null);
            ra.addAttribute("city", city);
        }
        if (sign != null && !sign.isEmpty()) {
            spotService.updateUserConfig(null, sign);
            ra.addAttribute("sign", sign);
        }
        return "redirect:/user?key=nfc_success";
    }

    // 事業所・拠点の追加
    @PostMapping("/admin/add-spot")
    public String addSpot(Spot spot) {
        spotService.saveSpot(spot);
        return "redirect:/admin";
    }

    // スポットの削除
    @PostMapping("/admin/delete-spot")
    public String deleteSpot(@RequestParam Long id) {
        spotService.deleteSpot(id);
        return "redirect:/admin";
    }

    // 音楽設定の保存
    @PostMapping("/admin/save-music")
    public String saveMusic(
            @RequestParam String name,
            @RequestParam String ownerName,
            @RequestParam String fileName) {
        spotService.updateMusicConfig(name, ownerName, fileName);
        return "redirect:/admin";
    }
}
