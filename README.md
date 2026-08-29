# 猫猫输入法 (Cat Keyboard)

一个通用 Android 输入法，通过「订阅规则」实现类似猫猫助手的文本替换功能。

## 特性

- **任意 App 可用**：通用输入法，不限定 QQ
- **两种模式**：实时替换 / 按钮触发替换，可随时切换
- **订阅链接**：从 URL 导入第三方维护的规则集
- **可选脚本**：规则集可附带 JS 脚本，做任意文本变换（纯函数沙箱）

## 订阅格式

订阅链接指向一个 JSON 文件，结构如下：

```json
{
  "name": "猫猫助手规则",
  "version": "1.0.0",
  "author": "作者名",
  "description": "描述",
  "mode_default": "realtime",
  "rules": [
    { "type": "replace", "match": "你", "replace": "主人" },
    { "type": "replace", "match": "我", "replace": "本喵" },
    { "type": "append", "text": "喵", "trigger": "punct" }
  ],
  "script": "function transform(text){ return text; }"
}
```

## 规则类型

| type | 说明 |
|------|------|
| replace | 精确替换 |
| replace_regex | 正则替换 |
| append | 追加文本 |

## 构建

使用 GitHub Actions 自动编译。

## 许可

Apache License 2.0