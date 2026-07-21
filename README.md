# ArtForge

面向 AI 图像创作者的创作、协作与交易平台。ArtForge 将结构化提示词、生图工作台、作品管理、风格包共创、任务市场和平台运营整合在同一套系统中。

当前仓库是可运行的稳定基线，目标是持续演进为真实可用的平台，而不是论文演示项目。

## 核心能力

- **生图工作台**：对接 Forge / Stable Diffusion WebUI 兼容 API，支持模型、采样器、尺寸、种子、高清修复等参数。
- **双语提示词库**：按风格、主体、构图、光线、色彩等分类选择标签，展示中文释义和多场景预览图。
- **作品管理**：保存生成结果、正负向提示词、参数和标签，提供个人作品库与公开作品广场。
- **风格包共创**：将同一视觉风格下的作品组织为可持续扩展的内容集合，支持投稿、协作者、审核、版本、评价与市场访问。
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
代码实现文档/             早期设计与实现参考资料
论文.docx                项目原始研究背景
```

## 快速开始

### 1. 环境要求

- JDK 17
- Node.js 20.19+ 或 22+
- pnpm
- MySQL 8
- Redis 7
- 启用 API 的 Forge，默认地址为 `http://127.0.0.1:7860`

### 2. 初始化数据库

```powershell
mysql -u root -p < database/schema.sql
```

默认数据库名为 `aiart_codex_platform`。已有数据库按时间顺序执行 `database/migrations/` 中尚未应用的脚本。

### 3. 配置并启动后端

根据 [.env.example](.env.example) 设置环境变量，至少应配置数据库密码和新的 JWT 密钥。

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

后端默认运行于 `http://127.0.0.1:8080`。

### 4. 启动前端

```powershell
cd frontend
pnpm install
pnpm dev
```

前端默认运行于 `http://127.0.0.1:5173`。开发服务器会把 `/api` 与 `/uploads` 代理到后端。

### 5. 初始化管理员

首次部署时，在登录页打开“管理员初始化”，创建系统中的第一个管理员账号。系统已有管理员后，该接口会拒绝再次初始化。登录后从右上角头像菜单进入“后台审核”。

## 文档

- [完整安装与配置](docs/SETUP.md)
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
- 本仓库目前未声明开源许可证，未经仓库所有者明确授权，不授予复制、修改或再分发权利。

## 当前状态

`v1.3.0` 是首个面向持续使用与 GitHub 托管整理的稳定版本。核心业务链路已经可用，后续重点是自动化测试、部署工程化、权限边界加固和真实运营数据验证。
