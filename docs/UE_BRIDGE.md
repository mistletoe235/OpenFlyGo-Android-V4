# UE / AirSim 出站桥协议

> 本文描述保留的 1 Hz 航测观察接口。实时 Simulator 位姿、虚拟相机、心跳和
> 碰撞/STOP 闭环请使用 [UE / Android / DJI Simulator HIL v1](UE_HIL_PROTOCOL.md)。

## 安全定位

此桥用于让 UE5、AirSim、Gaussian Splatting 渲染环境或实验记录服务获取 Android 侧的任务和 DJI 遥测。它是只出站的观察接口，不是飞控接口：Android 不监听命令端口，载荷也没有 `command` 字段。

在 App 的“更多 → 系统”中设置服务根地址，例如 `http://192.168.1.2:30010`。
HIL 已发现 UE peer 时，Android 会复用该 peer IP，并保留这里配置的 scheme 和端口。

## HTTP 接口

### `POST /v1/survey/mission`

用户点击“发送当前任务”时发送一次完整任务。选择 UE 图像源并启动航线时也会自动发送。
主体包含：

- `schema`: `openfly.survey.ue.v1`
- `type`: `mission`
- `coordinate_contract`: 坐标约定
- `mission`: 与 App 导入导出的 SurveyMission v1 相同

### `POST /v1/survey/telemetry`

打开“遥测镜像”后以约 1 Hz 发送。若上一次请求尚未结束，新帧会直接丢弃，避免网络故障形成无限队列。

主要字段：

```json
{
  "schema": "openfly.survey.ue.v1",
  "type": "telemetry",
  "pose": {
    "latitude_wgs84_deg": 31.2304,
    "longitude_wgs84_deg": 121.4737,
    "altitude_agl_m": 60.0,
    "heading_cw_from_north_deg": 90.0,
    "gimbal_pitch_deg": -90.0
  },
  "dji_simulator": {"active": true, "flying": true},
  "execution": {"state": "RUNNING", "waypoint_index": 3}
}
```

### `POST /v1/survey/target`

航线开始、恢复或切换到下一执行 leg 时发送。UE 可用它绘制当前目标点、航线阶段和
云台方向。主要字段包括：

- `mission_id`
- `execution.state`
- `execution.phase`
- `execution.execution_leg_index`
- `execution.waypoint_index`
- `target.latitude_wgs84_deg / longitude_wgs84_deg / altitude_agl_m`
- `target.heading_cw_from_north_deg / gimbal_pitch_deg`
- `target.capture_action / capture_view / pass_index`

### `POST /v1/survey/capture`

Android 成功保存一张 UE 航测帧后发送。`frame_id` 和 `pose_sequence` 与 TCP 虚拟帧
头一致，因此 UE 可以把图片、渲染位姿和航线事件精确关联。消息还包含尺寸、格式、
Android 可见保存路径、拍摄原因、飞机 WGS-84 位姿和当前执行索引。

当 DJI Simulator 运行且 App 选择“图像源：UE”时，航测拍照不再调用 DJI 相机：

- 保存 UE 原始 JPEG/PNG，不二次压缩；
- 同目录保存同名 JSON 元数据；
- 路径为 `Download/DJI-VLN/survey/<mission_id>/`；
- 帧超过 1 秒或 TCP 断开时，本次拍照按超时处理并自动暂停航线。

## 坐标转换责任

| 数据 | Android 发送 | UE/AirSim 接收侧责任 |
|---|---|---|
| 经纬度 | WGS‑84，角度 | 以明确的场景原点转换到局部坐标 |
| 局部世界系 | ENU：X East、Y North、Z Up | 若使用 UE 左手系或 AirSim NED，在适配层显式转换 |
| 无人机机体系 | FRU：forward/right/up | AirSim NED/FRD 控制不得直接复用 |
| 航向 | 真北为 0°、顺时针为正 | 转 UE yaw 时记录符号和零轴变换 |
| 云台俯仰 | 向下为负 | `-90°` 表示正下视 |
| 长度/速度 | SI：m、m/s | 不使用 UE 默认厘米作为线协议单位 |

建议 UE 插件在场景加载时固定 `origin_wgs84`，并把它和 GS/碰撞代理的世界变换一起写入实验元数据。不要每帧改变地理原点。

## 与 Gaussian Splatting 的关系

桥只同步任务和位姿。GS/Niagara 负责视觉渲染，碰撞应来自独立的 UE Static Mesh、简化网格、SDF 或高度场；不要把高斯中心直接当作可靠碰撞体。这样视觉资产可以替换或分层加载，而 AirSim 的动力学和碰撞仍有确定性。
