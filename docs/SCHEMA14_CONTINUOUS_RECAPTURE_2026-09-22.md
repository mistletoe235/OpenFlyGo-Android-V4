# Android V4 schema 14 连续补拍适配

日期：2026-09-22。适用：私有 Android V4 与开源 Android V4；不包含 iOS。

同日后续更新：iOS 私有版与开源版也已补齐 schema 14，详见各自仓库的
`docs/SCHEMA14_CONTINUOUS_RECAPTURE_2026-09-22.md`。下文“不包含 iOS / iOS 仍为 1–13”
记录的是本轮 Android 验证当时的范围，不是当前三端兼容表。

## 协议与操作

- Android V4 接受 schema 1–14；schema 14 必须有已知的 `recapture_flight_mode`。
- `CONTINUOUS_EXPERIMENTAL` 必须同时有 `active_mapping`。未知模式、缺失模式、
  在 schema 13 中塞入模式字段，以及高于 14 的版本都会被拒绝，不静默降级。
- 普通任务和 `STOP_AND_CAPTURE` 仍导出 schema 13，不附加模式字段。
- 新建云端会话时，勾选“连续补拍（实验）”才请求连续模式；默认不勾选。
  会话声明 `supported_mission_schemas=[13,14]`，服务端仍须按能力协商导出。
- 导入连续任务后按原流程预检、启动，不会因为下载或勾选选项而自行起飞。
  保留 `execution_review` 审核检查；开源云端浏览器的只读预览不等于允许执行。
- 任务保存 / 再读取保留连续模式。iOS 仍只接受 schema 1–13。

## V4 的执行方式

V4 使用 App 侧 Virtual Stick 速度控制，不把 V5 的 KMZ 后端套到 Mini 2 上。
必须保持 App 前台、遥控连接和控制权，不是可关闭 App 的机载离线任务。

仅满足下列条件的中间点启用连续跟随；其他点沿用停车拍照逻辑：

- 前、中、后三点均为 `CAPTURE_POINT` / `CAPTURE_ON_REACH`，且视角相同。
- 当任务提供 pass 元数据时，三点必须能查到同一 `region_id`，均为 `SURVEY`，
  且不是重建桥接点。`pass_index` 是逐点编号，不能用编号相等判断同一连续段。
- 两侧水平段长各至少 3 m，路径转角不超过 30°。
- 相邻航向变化不超过 5°、云台俯仰变化不超过 3°、高度变化不超过 0.5 m。
- 首尾点、转场点、区域边界与不满足上述条件的转弯不会被强制连续通过。

采用有界前视跟随，沿相邻线段给出速度；按段长与相机最小拍照间隔进一步限速。
这不是 V5 KMZ 转弯曲线的逐项复刻，也不是新的全局轨迹优化器。

## 拍照确认和失败处理

- 飞控 / 云台遥测须不早于当前时间 1 s，且不可来自未来时间；连接须有效。
- 航向误差不超过 3°、高度误差不超过 0.4 m、云台回读到位且完成原有稳定等待。
- 拍照触发仍须进入既有到点窗口（水平 1.2 m）。连续模式不要求水平速度降到零。
- 每个执行点只保留一个未完成请求；收到成功回调才推进任务索引，避免提前标记完成。
- 等待回调期间可以继续运动，但前视目标最多到下一段中点；慢相机仍可能导致减速 / 停下。
- 未触发就超过拍照窗口、等待期间姿态失效、拍照失败或 8 s 超时会暂停，
  不把漏拍当成功。恢复仍走原有预检和恢复路径；恢复转场不是连续拍照点。
- DJI 拍照命令成功回调不等于精确曝光时间或已核验的 SD 卡照片；移动拍摄模糊、
  实际曝光位置和新路径的硬件行为需要后续验收，不能靠 JVM 测试宣称解决。

## 已验证

两个 V4 工程均完成：

```sh
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew \
  :app:testDebugUnitTest :app:testReleaseUnitTest \
  :app:assembleDebug :app:assembleRelease \
  -Pkotlin.incremental=false -Pkapt.incremental.apt=false
```

| 工程 | Debug 单测 | Release 单测 | APK |
| --- | --- | --- | --- |
| 私有 Android V4 | 308 通过 | 308 通过 | Debug / 签名 Release 构建成功 |
| 开源 Android V4 | 305 通过，1 跳过 | 305 通过，1 跳过 | Debug / 未签名 Release 构建成功 |

跳过项是需要显式配置环境的在线服务测试，没有据此创建真实云端会话。
新增 `RecaptureFlightModeTest` 共 9 项，包含协议往返 / 拒绝、审核、能力声明、
几何与区域限制、遥测新鲜度、拍照窗口，以及 64 组间距 × 速度 × 回调延迟的
确定性数值跟随测试。这些是 JVM / 简化运动学测试，不是 DJI 模拟器或实飞验收。

私有 Release：`0.3.0-v4` / versionCode 4；APK v2 签名和 zipalign 检查通过。
开源模板：`0.3.0-v4` / versionCode 3；自行提供合法应用密钥与签名后再安装 / 发布。
本次未控制飞机、未执行航线、未安装手机，也未提交或推送仓库。
