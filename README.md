# CatShare

## uwuAOSP 对 CatShare 的变更
- 迁移首页和设置页到 `uwuComposeSettingsLib`/SettingsLib 风格，补充设置入口图标、偏好项图标、Google Sans 字体与更接近系统设置的输入弹窗。
- 支持在 AOSP/平台应用环境中构建，加入 Soong 配置、平台 manifest、privapp 权限白名单，并将第三方依赖以 `external/` 预置库形式纳入工程。
- 移除 Shizuku 实现，改用平台侧 MAC 地址获取路径。
- 首页改为内联传输体验：接收和发送都会在首页显示大卡片，支持进度、取消、完成/失败保留和手动关闭，提供更加友好的用户交互体验。
- 首页发送流程不再跳转到独立选择页面，选择文件后直接弹出“选择设备”面板，并在面板中列出可用设备。
- 修复发送端 Ktor server 在 Android/R8 下的模块反射崩溃，并放宽/细化 BLE 阶段日志，恢复 P2P 文件发送。
- 发送侧改用系统文件选择器，并为发送过程增加首页卡片状态广播。
- 保持与原版及绝大多数 Fork 版本 CatShare 的兼容

## 对于 uwuAOSP 的提示：
- 如果您能确定一个问题一定来自于 [上游实现](https://github.com/kmod-midori/CatShare) , 请按照下方原 README “汇报问题”一节向**原**仓库反馈 issue;
- 否则，请向 [uwuAOSP issue_tracker](https://github.com/uwuAOSP/issue_tracker/issues) 报告问题，并注明 `[CatShare]` 标签，例如：
```issue title
[Bug][CatShare] File send fails
```

## 原 README
类原生 & 海外设备，现已加入互传联盟。

Android 目前已不再支持非系统应用获取手机的 MAC 地址等无法重置的序列号，但由于各品牌的互传功能通常为系统应用，互传联盟协议将设备的 MAC 地址作为其认证信息的一部分，目前暂时无法绕过。

本 App 的 GitHub Release 和 F-Droid 版本签名一致， F-Droid 版本可能相对滞后，可以任意选择。

[<img src="https://f-droid.org/badge/get-it-on-zh-cn.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/packages/moe.reimu.catshare)
[<img src="https://www.openapk.net/images/openapk-badge.png"
    alt="Get it on OpenAPK"
    height="80">](https://www.openapk.net/catshare/moe.reimu.catshare/)
[<img src="https://www.androidfreeware.net/images/androidfreeware-badge.png"
    alt="Get it on Android Freeware"
    height="80">](https://www.androidfreeware.net/download-catshare-apk.html)

## 功能
- [x] 蓝牙发现
- [x] 文件接收
- [x] 文件发送（需要 Shizuku 支持）
- [x] 文本传输（两侧均为 CatShare 时复制至剪贴板，接收方为其他设备时以文本文件形式发送） 

## 支持设备（已测试）
| 品牌        | 向该设备发送 | 从该设备接收            |
| ----------- | ------------ | ----------------------- |
| 小米        | Y            | Y                       |
| OPPO/一加等 | Y            | Y，但发送端提示接收失败 |
| vivo        | Y            | Y                       |

## 汇报问题

你可以在该项目的 issue 区汇报你在使用 CatShare 期间遇到的问题，尽量的，请附上 CatShare 的 adb logcat 日志。

通过该命令获取 CatShare 的日志。
<details>
<summary>release(正式版)</summary>

shell(linux)
```shell
adb logcat --pid $(adb shell pidof -s moe.reimu.catshare)
```
cmd(windows)
```shell
for /f "tokens=1" %i in ('adb shell pidof -s moe.reimu.catshare') do adb logcat --pid %i
```
</details>
<details>
<summary>debug(测试版)</summary>

shell(linux)
```shell
adb logcat --pid $(adb shell pidof -s moe.reimu.catshare.debug)
```
cmd(windows)
```shell
for /f "tokens=1" %i in ('adb shell pidof -s moe.reimu.catshare.debug') do adb logcat --pid %i
```
</details>
建议尽可能完整的截取日志，并注释从什么时候发送或接收内容，尽量使用折叠块语法来包裹日志内容。

````markdown
<details>
<summary>Details</summary>

```
在此处填入日志内容，注意其应被包裹在反括号代码块内
```

</details>
````
