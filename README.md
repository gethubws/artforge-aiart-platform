# ArtForge

面向 AI 图像创作者的创作、资源共创与交易平台。ArtForge 将结构化提示词、Forge 生图工作台、作品管理、风格资源包、任务市场和平台运营整合在同一套系统中。

平台的核心不是单纯展示 AI 图片，而是把创作结果继续整理成可复用、可协作、可版本化、可授权交付的数字资源。当前仓库是可运行的稳定基线，目标是持续演进为真实可用的平台，而不是论文演示项目。

## 风格资源包

ArtForge 的风格资源包接近 Unity Asset Store 中的美术资源包：一个资源包包含多种视觉一致、能够组合使用的植被、建筑、角色、道具、地形、UI 和特效素材，而不是只有一张封面或一组提示词。

- 创作者可以共同维护资源、提交新修订并发布版本。
- 每个发布版本锁定资源清单，避免后续修改破坏已经购买的交付内容。
- 访客可浏览预览，兑换后可获取文件地址、提示词、生成参数和授权信息。
- 资源包可以继续接收作品投稿，用真实使用效果验证风格一致性。

仓库内置“翡翠童话森林”完整示例，包含 32 项资源和 8 个资源类目，可用于验证浏览、协作、版本、兑换与交付流程。

## 界面预览

### 创作工作台

![ArtForge 创作工作台](docs/screenshots/02-workbench.png)

### 作品广场

![ArtForge 作品广场](docs/screenshots/03-community.png)

### 风格资源市场

![ArtForge 风格资源市场](docs/screenshots/04-style-market.png)

### 任务市场

![ArtForge 任务市场](docs/screenshots/05-task-market.png)

<details>
<summary>查看登录页面</summary>

![ArtForge 登录页面](docs/screenshots/01-login.png)

</details>

## 核心能力

- **生图工作台**：对接 Forge / Stable Diffusion WebUI 兼容 API，支持模型、采样器、尺寸、种子、高清修复等参数。
- **双语提示词库**：按风格、主体、构图、光线、色彩等分类选择标签，展示中文释义和多场景预览图。
- **作品管理**：保存生成结果、正负向提示词、参数和标签，提供个人作品库与公开作品广场。
- **风格资源包**：像游戏引擎资源包一样组织统一风格的植被、建筑、角色、道具、地形、UI 与特效资源，支持不可变资源修订、版本锁定、授权、协作者、积分兑换和交付清单。
- **效果共创**：资源包仍可接收作品投稿，用于展示真实使用效果、社区评价与风格一致性验证。
- **任务市场**：发布创作需求、按等级与状态浏览任务、投稿作品、审核交付并记录积分流转。
- **社区互动**：作品收藏、内容订阅、站内通知以及公开内容浏览。
- **运营后台**：用户与角色管理、内容审核、平台指标、标签维护、预览图库管理和搜索/组合统计。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite 6、Element Plus、Axios、ECharts |
| 后端 | Java 17、Spring Boot 3.2.3、Spring Security、MyBatis-Plus |
| 数据 | MySQL 8、Redis 7 |
| 生图 | Forge / Stable Diffusion WebUI-compatible API |
| 鉴权 | JWT |

## 项目结构

```text
backend/                 Spring Boot API 服务
database/                完整建库脚本与增量迁移
docs/                    安装、路线图与标签图库规划
frontend/                Vue 3 Web 应用
scripts/                 演示数据辅助脚本
```

## 快速开始

### Docker Compose（推荐）

```powershell
Copy-Item .env.docker.example .env.docker
# 修改 .env.docker 中的数据库、JWT 和 MinIO 密钥
docker compose --env-file .env.docker up -d --build
```

平台默认运行于 `http://127.0.0.1:5174`。完整配置、备份和升级方法见 [部署与运维](docs/DEPLOYMENT.md)。

### 手动启动

#### 1. 环境要求

- JDK 17
- Node.js 20.19+ 或 22+
- pnpm
- MySQL 8
- Redis 7
- 启用 API 的 Forge，默认地址为 `http://127.0.0.1:7860`

#### 2. 初始化数据库

```powershell
mysql -u root -p < database/schema.sql
```

默认数据库名为 `aiart_codex_platform`。已有数据库按时间顺序执行 `database/migrations/` 中尚未应用的脚本。

资源包升级使用 `20260721_style_package_resource_assets.sql`，生产基础版本另新增 `20260721_admin_operation_log.sql`。已有数据库应按文件名顺序执行尚未应用的迁移；全新数据库直接导入 `database/schema.sql`。

#### 3. 配置并启动后端

根据 [.env.example](.env.example) 设置环境变量，至少应配置数据库密码和新的 JWT 密钥。

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

后端默认运行于 `http://127.0.0.1:8080`。

#### 4. 启动前端

```powershell
cd frontend
pnpm install
pnpm dev
```

前端默认运行于 `http://127.0.0.1:5173`。开发服务器会把 `/api` 与 `/uploads` 代理到后端。

#### 5. 初始化管理员

首次部署时，在登录页打开“管理员初始化”，创建系统中的第一个管理员账号。系统已有管理员后，该接口会拒绝再次初始化。登录后从右上角头像菜单进入“后台审核”。

#### 6. 导入示例资源包

仓库包含一个可选的“翡翠童话森林”示例资源包。先设置 `AIART_IMAGE_API_KEY` 生成或补齐图片，再启动后端运行：

```powershell
python scripts/generate-emerald-fable-pack.py
AIART_EMERALD_PASSWORD='set-a-local-demo-password' node scripts/seed-emerald-fable-pack.mjs
```

生成脚本支持 `AIART_IMAGE_API_BASE`、`AIART_IMAGE_MODEL` 和断点续跑；密钥只从环境变量读取。示例包包含 32 项资源，覆盖植被、建筑、道具、角色、生物、地形、UI 和特效。

## 文档

- [完整安装与配置](docs/SETUP.md)
- [Docker 部署与运维](docs/DEPLOYMENT.md)
- [产品路线图](docs/ROADMAP.md)
- [标签图库扩展计划](docs/TAG_LIBRARY_EXPANSION_PLAN.md)
- [版本记录](CHANGELOG.md)

## 验证命令

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
pnpm build
```

## 安全与发布说明

- 不要提交真实数据库密码、API Key、JWT 密钥、生成文件或上传目录。
- 生产环境必须替换 `AIART_JWT_SECRET`，并通过反向代理提供 HTTPS。
- 当前 CORS 与本地文件存储策略以本地部署为基线，公网部署前应限制来源并配置持久化对象存储。
- 项目代码按 [MIT License](LICENSE) 开源。部署者仍需自行确认所使用模型、LoRA、生成图片与第三方素材的授权范围。

## 当前状态

`v1.5.0` 在资源包业务基础上补齐 Docker Compose、MinIO/本地对象存储、私有资源鉴权下载、登录限流、管理员操作日志、自动化测试和 GitHub Actions。当前版本可作为单机生产部署基线；真实支付、提现、CDN 与大规模集群仍属于后续范围。
