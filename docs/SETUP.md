# 安装与配置

## 环境要求

- JDK 17
- Node.js 20.19+ 或 22+
- pnpm
- MySQL 8
- Redis 7
- Forge 或其他兼容 Stable Diffusion WebUI API 的服务

## 数据库

全新部署执行：

```powershell
mysql -u root -p < database/schema.sql
```

默认数据库名为 `aiart_codex_platform`。升级已有实例时，应按文件名中的日期顺序执行 `database/migrations/` 中尚未应用的 SQL。执行前请备份数据库。

## 后端配置

项目通过环境变量读取运行配置。根目录的 `.env.example` 是配置清单，但 Spring Boot 不会自动加载根目录 `.env`；可以在 PowerShell 中设置，或交给部署平台注入。

```powershell
$env:AIART_DB_USER='root'
$env:AIART_DB_PASSWORD='your_password'
$env:AIART_JWT_SECRET='replace_with_a_long_random_secret_at_least_32_bytes'
$env:AIART_SD_BASE_URL='http://127.0.0.1:7860'

cd backend
.\mvnw.cmd spring-boot:run
```

常用变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `AIART_DB_URL` | 本机 `aiart_codex_platform` | JDBC 地址 |
| `AIART_DB_USER` | `root` | 数据库用户 |
| `AIART_DB_PASSWORD` | 空 | 数据库密码 |
| `AIART_REDIS_HOST` | `localhost` | Redis 地址 |
| `AIART_REDIS_PORT` | `6379` | Redis 端口 |
| `AIART_JWT_SECRET` | 仅开发用默认值 | JWT 签名密钥，生产环境必须替换 |
| `AIART_UPLOAD_ROOT` | `uploads` | 上传与生成文件目录 |
| `AIART_SD_BASE_URL` | `http://127.0.0.1:7860` | Forge API 地址 |
| `AIART_SD_TIMEOUT_SECONDS` | `300` | 生图请求超时秒数 |

后端 API 位于 `http://127.0.0.1:8080/api`，上传文件由 `http://127.0.0.1:8080/uploads` 提供。

## Forge

启动 Forge 时启用 API，并确认以下兼容接口可访问：

- `GET /sdapi/v1/options`
- `GET /sdapi/v1/samplers`
- `GET /sdapi/v1/sd-models`
- `POST /sdapi/v1/txt2img`

如果 Forge 部署在另一台机器，将 `AIART_SD_BASE_URL` 设置为后端能够访问的地址。不要把仅绑定 `127.0.0.1` 的服务误认为可以被远程后端访问。

## 前端

```powershell
cd frontend
pnpm install
pnpm dev
```

打开 `http://127.0.0.1:5173`。若端口被占用，Vite 会选择下一个可用端口。

## 首次管理员

1. 打开登录页。
2. 点击“管理员初始化”。
3. 创建第一个管理员账号。
4. 登录后从右上角头像菜单进入“后台审核”。

初始化接口仅在系统内不存在管理员时有效。请使用独立强密码，不要复用数据库密码或 GitHub 密码。

## 演示市场数据

需要本地展示风格包和任务市场时，在服务已启动且具备相应账号的环境中运行：

```powershell
node scripts/seed-demo-market.mjs
```

脚本按既定规则设计为可重复执行。运行前仍建议先备份非开发数据库。

## 生产部署检查

- 使用随机且足够长的 JWT 密钥。
- 通过 HTTPS 反向代理暴露前后端，并限制 CORS 来源。
- 将 `uploads` 放到持久化存储，或替换为对象存储实现。
- 不直接把 MySQL、Redis 和 Forge 端口暴露到公网。
- 配置数据库备份、日志轮转、服务健康检查和资源限制。
- 对初始化管理员接口、上传类型和内容审核策略做部署级复核。
