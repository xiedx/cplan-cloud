# cplan-cloud

> AI 驱动的短视频自动生成平台 — 从创意大纲到成品视频，全链路自动化

## 项目简介

cplan 是一个基于 Spring Cloud 微服务架构的短视频自动生成平台。用户只需输入创作大纲（主题、要点、风格偏好），平台即可自动完成剧本撰写、分镜拆分、画面生成及视频合成，将「从创意到成片」的全链路压缩至分钟级。

## 核心链路

```
用户输入大纲 → AI 生成剧本 → 自动拆分分镜 → 逐幕生成视频 → 合成成品视频 → 推送结果
```

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 4.0.6 |
| 微服务 | Spring Cloud | 2025.1.1 |
| 微服务 | Spring Cloud Alibaba | 2025.1.0.0 |
| 注册/配置中心 | Nacos | 2.3.x |
| API 网关 | Spring Cloud Gateway | 4.x |
| 服务间通信 | OpenFeign + LoadBalancer | - |
| 消息队列 | RocketMQ | 5.1.x |
| 数据库 | MySQL | 8.0 |
| ORM | MyBatis-Plus | 3.5.7 |
| 缓存 | Redis | 7.x |
| 对象存储 | MinIO | RELEASE.2024 |
| 视频合成 | FFmpeg + JavaCV | 1.5.10 |
| 熔断限流 | Sentinel | 1.8.x |
| 容器化 | Docker + Docker Compose | - |
| JDK | OpenJDK | 17 |

## 微服务架构

```
┌──────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                   │
│               JWT 鉴权 · 路由转发 · 限流熔断               │
└────────┬────────┬────────┬────────┬────────┬─────────────┘
         │        │        │        │        │
    ┌────▼───┐ ┌──▼───┐ ┌─▼────┐ ┌─▼────┐ ┌─▼────┐
    │  User  │ │Create│ │  AI  │ │Compose│ │Notify│
    │Service │ │Service│ │Adapter│ │Service│ │Service│
    │ (8081) │ │(8082)│ │(8083) │ │(8084) │ │(8086)│
    └────────┘ └──┬───┘ └──┬───┘ └──┬───┘ └──────┘
                  │        │        │
             ┌────▼────┐   │   ┌────▼────┐
             │ RocketMQ │◄─┴──►│ Storage │
             └─────────┘      │ (8085)  │
                              └─────────┘
```

| 服务 | 端口 | 职责 |
|------|------|------|
| cplan-gateway | 8080 | 统一入口、JWT 鉴权、路由转发、限流熔断 |
| cplan-user | 8081 | 用户注册/登录、JWT 管理 |
| cplan-creation | 8082 | 大纲管理、Prompt 构建、剧本/分镜存储与编辑、任务状态机 |
| cplan-ai-adapter | 8083 | 封装 LLM / 文生视频外部 API，统一重试与限流 |
| cplan-compose | 8084 | 视频片段拼接（FFmpeg）、字幕嵌入、音频合并 |
| cplan-storage | 8085 | MinIO 文件上传/下载、预签名 URL 生成 |
| cplan-notify | 8086 | 任务进度 SSE 实时推送 |

## 项目结构

```
cplan-cloud/
├── pom.xml                          # 父 POM（BOM 管理）
├── docker-compose.yml               # 基础设施（Nacos/MySQL/Redis/RocketMQ/MinIO）
├── docker-compose-services.yml      # 微服务容器编排
├── sql/                             # 数据库建表脚本
├── docs/                            # 架构设计文档
├── cplan-common/                    # 公共模块（统一响应/异常/JWT/MQ常量）
├── cplan-gateway/                   # API 网关
├── cplan-user/                      # 用户服务
├── cplan-creation/                  # 创作服务
├── cplan-ai-adapter/                # AI 适配服务
├── cplan-compose/                   # 视频合成服务
├── cplan-storage/                   # 文件存储服务
└── cplan-notify/                    # 通知服务
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- 8GB+ 可用内存

### 1. 启动基础设施

```bash
docker-compose up -d
```

启动后各组件地址：

| 组件 | 地址 |
|------|------|
| Nacos Console | http://localhost:8848/nacos |
| MinIO Console | http://localhost:9001 |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |
| RocketMQ NameServer | localhost:9876 |

### 2. 初始化数据库

```bash
mysql -u root -p < sql/cplan_user.sql
mysql -u root -p < sql/cplan_creation.sql
```

### 3. Nacos 配置

在 Nacos Config 中创建以下配置（DataId → YAML 格式）：

- `cplan-common.yaml` — 公共配置（JWT secret、Redis 等）
- `cplan-user.yaml` — 用户服务专属配置
- `cplan-creation.yaml` — 创作服务专属配置
- `cplan-ai-adapter.yaml` — AI API Key、Endpoint 等配置
- 其余服务按需添加

### 4. 编译项目

```bash
mvn clean package -DskipTests
```

### 5. 启动微服务

**方式一：IDE 逐服务启动**

按以下顺序启动各服务的 `*Application.java`：

1. GatewayApplication
2. UserApplication
3. CreationApplication
4. AiAdapterApplication
5. ComposeApplication
6. StorageApplication
7. NotifyApplication

**方式二：Docker Compose 一键启动**

```bash
docker-compose -f docker-compose-services.yml up -d
```

### 6. 验证

```bash
# 用户注册
curl -X POST http://localhost:8080/api/user/v1/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"123456"}'

# 用户登录
curl -X POST http://localhost:8080/api/user/v1/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"123456"}'
```

## AI 能力依赖

| 能力 | 用途 | 当前状态 |
|------|------|----------|
| 大语言模型（LLM） | 剧本生成、分镜拆分 | Mock 实现，预留扩展点 |
| 文生视频 | 按分镜描述生成视频片段 | Mock 实现，预留扩展点 |
| 文生图（P1） | 分镜配图 | 待开发 |
| TTS 语音合成（P1） | 旁白/台词音频 | 待开发 |

> AI 调用均通过 `cplan-ai-adapter` 统一封装，支持供应商热切换。搜索代码中 `[MOCK]` 标记可定位所有 Mock 实现。

## API 概览

| 服务 | 路径前缀 | 核心接口 |
|------|---------|----------|
| User | `/api/user/v1` | 注册、登录、用户信息 |
| Creation | `/api/creation/v1` | 创建项目、提交大纲、获取剧本/分镜、确认分镜 |
| AI Adapter | `/internal/ai/v1` | 触发剧本/视频生成（内部 Feign 调用） |
| Compose | `/api/compose/v1` | 合成任务状态查询 |
| Storage | `/api/storage/v1` | 预签名上传/下载 URL |
| Notify | `/api/notify/v1` | SSE 订阅（实时进度推送） |

## 开发路线

### P0 — 第一期（当前）
- [x] 微服务脚手架搭建
- [x] 用户注册/登录
- [x] 大纲 → 剧本 → 分镜 核心链路
- [x] 异步任务队列 + 状态机
- [x] SSE 实时进度推送
- [x] MinIO 文件存储
- [ ] 对接真实 LLM API
- [ ] 对接真实文生视频 API
- [ ] FFmpeg 视频合成实现

### P1 — 第二期
- [ ] 大纲模板库
- [ ] 分镜配图（文生图）
- [ ] TTS 配音合成
- [ ] 用户配额管理
- [ ] 团队协作

### P2 — 未来
- [ ] 视频二次剪辑
- [ ] 一键多平台发布
- [ ] 创作数据分析
- [ ] AI 风格迁移

## License

MIT
