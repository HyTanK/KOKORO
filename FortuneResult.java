package com.example.demo;

import lombok.Data;

@Data
public class FortuneResult {
    public String sign;      // 星座名
    public String content;   // 占い内容
    public String item;      // ラッキーアイテム
    public String money;     // 金運
    public String love;      // 恋愛運
    public String work;      // 仕事運
}
