# V86 Android V4 客户端接口与安全合同

更新：2026-08-30

工程：`dji-vln-mini2-camera` / `com.openfly.go.v4`

2026-08-31 提交前复核：270 项 JVM、10 项模拟器仪器测试通过。新增运行中服务地址锁定、
finalize 期间禁止入队、统一 JSON 审批字段检查、PLY 头大小限制及上传异常连接释放。
同一服务器仍允许更新访问码。详情见 `V4_PRECOMMIT_AUDIT_2026-08-31.md`；下方旧测试数量
保留为前次验证记录，不代表已经完成公网或真实飞机验收。

## 产品入口

航线规划页底部“云端重建”。配置页保存服务地址和 Bearer 访问码；访问码由 Android
Keystore AES-GCM 密钥加密，不写日志、不进入 URL、不进入 Git。

任务流程：

1. 保存连接并创建 V86 session，填写任务名和真实起飞点 ASL。
2. DJI 拍照成功时，触发图传 JPEG 已写 EXIF/sidecar；飞机 GPS 新鲜度不超过 2 秒且
   可得到绝对 ASL 才进入私有上传队列。
3. 状态卡显示已拍、已传、待传、未入队。队首失败不会跳过；sequence 和文件在重启后保留。
4. 待传为 0 后 finalize，随后可刷新、重试处理、读取 result。
5. schema 13 补拍任务下载后只进入本地任务和安全预检；`safe_to_execute=false` 时客户端
   直接拒绝导入和激活。
6. PLY 使用 `.part` 流式下载、长度核对和原子替换，再由独立横屏查看器打开。

## HTTP 接口

| 操作 | 请求 |
|---|---|
| 新建任务 | `POST /api/sessions` |
| 查询状态 | `GET /api/sessions/{id}` |
| 上传图片 | `PUT /api/sessions/{id}/images/{sequence}` |
| 结束采集 | `POST /api/sessions/{id}/finalize` |
| 重试处理 | `POST /api/sessions/{id}/retry` |
| 查询结果 | `GET /api/sessions/{id}/result` |
| 下载任务/候选/PLY | result 中的同源相对 URL |

所有请求携带 `Authorization: Bearer ...`。客户端拒绝向不同 scheme/host/port 的 URL
发送访问码，也不跟随重定向。实时帧上传携带 `X-Latitude`、`X-Longitude`、
`X-Altitude`、`X-Altitude-Source`、`X-Timestamp` 和 `X-Capture-View`。

服务端必须把同一 `{session_id, sequence}` 的 PUT 实现为幂等覆盖或返回 duplicate 成功；
客户端在“服务端已接收但响应丢失”和进程重启后会重放同一 sequence，绝不能因此重复计数。

## 持久存储

- 私有队列：`files/v86-streaming/pending/{sequence}.jpg/.json`
- 点云缓存：`files/v86-point-cloud/`
- 公开触发帧备份：`Download/DJI-VLN/trigger-frames/`
- 队列文件和 sequence 在写入时使用临时文件 + 原子替换。
- 上传成功后才删除队首；408、429、5xx 和网络错误按 2–60 秒退避自动重试；
  401/403 等确定性错误等待用户修正连接。
- Activity 重建时队列事务和上传泵使用进程级唯一锁；同一时刻只允许一个 controller 上传
  队首，新的 Activity 会等待而不是并发删除同一 pair。

## 历史照片离线回放

通过系统文件夹选择器取得持久只读授权，不申请全盘文件权限。JPG/JPEG 按文件名排序，
先在应用私有缓存中完成整批 EXIF 检查、解码、压缩和落盘；任意照片失败时整批不开始上传。
预处理期间 session 改变会阻断入队，避免跨任务串图；开始入队后的磁盘/服务异常会明确报告
已接受数量，不再误报“整批未开始”。

- ≤600 KiB：原字节上传。
- >600 KiB：缩放和 JPEG 质量循环，硬上限 640 KiB。
- 重编码后重建标准 GPS、绝对高度、拍摄时间、Make/Model。
- 原始 JPEG 中自包含的 DJI XMP APP1 段被重新注入；不复制可能依赖 MPO/MPF 偏移的
  原始 EXIF APP1。
- 离线上传不发送顶层 GPS/高度 header，服务端以图片 EXIF 为准；时间 header 来自同一
  张照片的 EXIF。

## 点云合同

只接受 `binary_little_endian 1.0`，vertex 字段固定为 float XYZ + uchar RGB。查看器从
文件随机访问并均匀采样最多 40,000 点；风险标记来自 viewer JSON，V50 红、V78 黄、
建议补拍相机位置青色。支持拖动旋转、双指缩放、双击适配、俯视和前视。

## 安全边界

- Debug 构建允许默认明文 HTTP endpoint，仅用于受控测试网络；Release manifest 禁止所有
  cleartext 流量，正式部署必须提供 HTTPS。
- 没有新鲜飞机 GPS、绝对 ASL 或成功 DJI 拍照回调时，公开图片可保留，但不进入 V86 队列。
- finalize 前必须 pending=0。
- 云端生成任务仍走 Android V4 本地相机兼容、地形、Home、GPS、VS、云台和航线预检；
  下载成功不等于允许飞行。

点云按 session/revision 命中缓存，不重复下载相同 PLY；最多保留当前版本和两个近期版本，
总 PLY 缓存上限 768 MiB，单次下载硬上限 512 MiB。

## 当前验证

- 267 项 JVM 单测通过。
- 10 项 Android 仪器测试通过：触发 JPEG EXIF/陈旧 GPS 拒绝、离线 EXIF 预检/保留、
  无 GPS 拒绝、64MB 有界输入、大图压缩、本地假服务 create-session → 私有队列 → PUT →
  pending 归零、进程重建恢复，以及损坏 pair、sealed/空任务 gate。
- 假服务断言离线 PUT 不含 `X-Latitude`/`X-Altitude`，Bearer 和 sequence 路径正确。
- 持久队列损坏会阻断上传/finalize，并由磁盘最大 sequence 修复计数；JSON、文本、错误体和
  PLY 下载均有明确大小上限。
- 航线页“云端重建”入口和配置窗口已在 Android 模拟器实际打开。
- 当前未连接真实 Android/MSDK4 设备，也未对默认公网 V86 服务执行真实照片上传；这两项
  仍是完成外场验收的外部门槛。
