# ArtForge 部署与运维

本文档描述 `v1.5.0` 的容器化部署基线。Docker Compose 会启动 MySQL、Redis、MinIO、Spring Boot 后端和 Nginx 前端。

## 环境要求

- Docker Desktop 或 Docker Engine 24+
- Docker Compose v2
- 至少 4 GB 可用内存
- 如果需要生图，宿主机需运行启用 API 的 Forge

## 首次启动

在项目根目录执行：

```powershell
Copy-Item .env.docker.example .env.docker
```

编辑 `.env.docker`，必须替换以下配置：

- `AIART_DB_PASSWORD`
- `AIART_DB_ROOT_PASSWORD`
- `AIART_JWT_SECRET`
- `AIART_MINIO_SECRET_KEY`

JWT 密钥应至少包含 32 个随机字节，并且不能与数据库或 MinIO 密码相同。

```powershell
docker compose --env-file .env.docker up -d --build
docker compose --env-file .env.docker ps
```

默认入口：

- 平台：`http://127.0.0.1:5174`
- 后端健康状态：运行 `docker compose --env-file .env.docker ps` 查看 `backend` 的 `healthy` 状态
- MinIO API：`http://127.0.0.1:9000`
- MinIO 控制台：`http://127.0.0.1:9001`

前端容器只公开一个 Web 入口，`/api` 和旧版 `/uploads` 请求会反向代理到后端。

## Forge 连接

容器默认通过以下地址访问宿主机 Forge：

```text
http://host.docker.internal:7860
```

如果 Forge 部署在其他机器，修改 `.env.docker`：

```dotenv
AIART_SD_BASE_URL=http://192.168.1.20:7860
```

Forge 必须允许来自 ArtForge 后端所在机器的网络访问。

## 文件存储

Docker 部署默认使用 MinIO：

```dotenv
AIART_STORAGE_PROVIDER=minio
```

公开生成图片和标签预览通过 ArtForge 公共文件端点读取。资源包原文件存放在私有对象中，只有资源包拥有者、协作者、免费资源包用户或已兑换用户可以请求下载。

私有资源由后端完成鉴权并流式返回，浏览器不需要直接访问 MinIO，因此不会受到 MinIO 跨域配置影响。预签名地址的默认有效期仍保留为 5 分钟，供后续非浏览器客户端扩展使用：

```dotenv
AIART_DOWNLOAD_EXPIRY_SECONDS=300
```

如果后续启用预签名直连下载，再把 `AIART_MINIO_PUBLIC_ENDPOINT` 设置为客户端可访问的 HTTPS 地址；当前 Web 下载链路无需公开 MinIO API。

仍可切换为本地持久卷：

```dotenv
AIART_STORAGE_PROVIDER=local
```

本地模式下，受保护文件由后端完成权限检查后流式传输。

## 日志与状态

```powershell
docker compose --env-file .env.docker ps
docker compose --env-file .env.docker logs -f backend
docker compose --env-file .env.docker logs -f frontend
```

后端容器使用 `/actuator/health` 执行健康检查。MySQL 和 Redis 也配置了独立健康检查，后端会在二者可用后启动。

## 数据备份

数据库备份：

```powershell
docker compose --env-file .env.docker exec mysql mysqldump -uroot -p aiart_codex_platform > aiart-backup.sql
```

实际执行时 MySQL 会提示输入 `.env.docker` 中的 root 密码。不要把备份文件提交到 Git。

MinIO 数据位于 `minio_data` 卷。本地存储模式的文件位于 `uploads_data` 卷。生产部署应使用宿主机快照、云盘快照或 MinIO 复制策略定期备份这些卷。

## 升级

升级前先备份数据库和对象存储，然后执行：

```powershell
git pull
docker compose --env-file .env.docker build --pull
docker compose --env-file .env.docker up -d
```

已有数据库需要按顺序执行 `database/migrations/` 中尚未应用的迁移。升级到 `v1.5.0` 时至少确认已应用：

- `20260721_style_package_resource_assets.sql`
- `20260721_admin_operation_log.sql`

全新的 MySQL 数据卷会自动执行 `database/schema.sql`。

## 生产检查清单

- 使用 HTTPS 反向代理公开平台、MinIO API 和 Forge API。
- 将 `AIART_CORS_ALLOWED_ORIGINS` 限制为真实站点域名。
- 不要公开 MySQL、Redis 或 MinIO 控制台端口。
- 替换所有示例密码和 JWT 密钥。
- 为数据库和对象存储配置定期备份。
- 限制上传大小，并根据业务需要调整允许的资源类型。
- 上线前运行后端测试与前端生产构建。
