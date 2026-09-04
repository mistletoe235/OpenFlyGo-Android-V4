# 全球地形与建筑高度数据链路

Android 端按任务 ROI 下载全球裸地 DEM，并在可用时叠加建筑相对高度：

1. Mapzen Terrain Tiles（AWS Open Data）提供无需账号的全球裸地高程瓦片。
2. Overture Buildings 提供全球建筑轮廓，但闵行样例只有约 0.08% 的建筑带高度，不能作为完整测高层。
3. GlobalBuildingAtlas Height 提供全球约 3 m 建筑相对高度估计。其官方上海 5° 分块压缩包约 33.9 GB，不允许手机客户端整包下载。
4. 原始 TIFF 离线转换为按 0.2°规则命名的 COG，放到支持 HTTP Range 的静态对象存储。Android 根据 ROI 计算相交的 `{x}/{y}`，直接下载并在本地拼接，不依赖常驻服务器。
5. 合成表面高程为 `裸地绝对高程 + 建筑相对高度`。公开全球层一律只允许预览和仿真；近期、现场核对的用户 DSM 才能进入真机验证流程。

一次性数据准备示例（可在工作站或 DSW3 执行，完成后不需要继续运行）：

```bash
rio cogeo create source.tif s3://surface-data/gba-height/source.tif \
  --cog-profile deflate --overview-resampling nearest
```

不能擅自把 0 设置为 NoData：建筑相对高度的 0 通常表示非建筑地面，必须保留源文件原有的 NoData 语义。对象存储键统一为全球网格索引，例如 `gba-height/1507/605.tif`。

Android 的 `local.properties`：

```properties
GLOBAL_BUILDING_HEIGHT_COG_TEMPLATE=https://static.example.org/gba-height/{x}/{y}.tif
```

模板必须包含 `{x}` 和 `{y}`，也可使用 `{west}`、`{south}`、`{east}`、`{north}`。App 限制规划区不超过 0.25°、最多 9 块、单块最多 128 MB，并持久缓存下载结果。静态存储需要 HTTPS、访问控制和正确的缓存头；不得把官方 WFS 当批量接口。GlobalBuildingAtlas Height 为 CC BY-NC 4.0，不能直接作为商业产品默认数据源。

没有配置静态模板时，先点击“全球地形”，再点击“建筑增强”，App 会要求从文件管理器选择覆盖当前规划区的建筑相对高度 GeoTIFF，并完全在手机本地完成合成。该文件必须是相对地面的非负建筑高度，不能选择绝对高程 DSM。

TiTiler、MosaicJSON、pgSTAC/eoAPI 仅保留为未来可选扩展：只有需要服务端 ROI 裁剪、统一鉴权或大规模并发时才引入，当前 App 不依赖它们。

对于实飞，推荐的开源本地高精度链路是 OpenDroneMap/NodeODM：先用较高、安全的测绘航线采集影像，以 `--dsm` 生成近期现场 DSM，再由用户导入 App 并核验。全球机器学习高度层始终只用于预览和仿真。
