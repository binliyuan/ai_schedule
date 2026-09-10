# WakeupSchedule — AI 智能课程表

一款基于 Jetpack Compose + Material3 的 Android 大学课程表应用。支持 AI 图片识别导入课表、内置 MCP Server 对外暴露课表数据、远程服务器同步、游戏中心等功能。

## 技术架构

```
┌──────────────────────────────────────────────┐
│              UI Layer                        │
│   Jetpack Compose + Material3 (主要页面)      │
│   传统 View + DataBinding (成绩/设置页面)      │
├──────────────────────────────────────────────┤
│           Navigation                         │
│   Jetpack Navigation (Fragment 宿主 + Compose)│
├──────────────────────────────────────────────┤
│           Business Layer                     │
│   ViewModel + LiveData + Coroutines          │
├──────────────────────────────────────────────┤
│      AI Agent           MCP Server           │
│   OpenAI 兼容 API       NanoHTTPD :8090      │
│   Tool-calling 循环     JSON-RPC 协议         │
├──────────────────────────────────────────────┤
│           Network                            │
│   Retrofit2 + OkHttp4 (远程后端)              │
├──────────────────────────────────────────────┤
│           Data                               │
│   Room Database (wakeup_schedule.db)         │
└──────────────────────────────────────────────┘
```

## 项目结构

```
WakeupSchedule/
├── app/
│   ├── build.gradle.kts                       # 模块构建配置
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/skills/                     # AI Agent 提示词
│       │   ├── system_prompt.md               #   Agent 系统人设 + 数据规则
│       │   ├── generate_schedule.md           #   生成示例课表的 Prompt
│       │   └── recognize_image.md             #   图片识别课表的 Prompt
│       ├── res/                               # 布局/导航/资源文件
│       │   ├── layout/                        #   XML 布局 (成绩/设置/登录/主界面)
│       │   └── navigation/nav_main.xml        #   底部导航图
│       └── java/com/solunis/schedule/
│           ├── WakeupScheduleApp.kt           # Application 入口，初始化 DB/Token/AI 配置
│           ├── MainActivity.kt                # 主界面：Fragment Navigation + BottomNav
│           ├── data/
│           │   ├── ai/                        # ★ AI Agent 模块
│           │   ├── database/                  # Room 数据库层
│           │   ├── local/                     # 本地存储 (TokenManager)
│           │   ├── model/                     # 数据模型 (User 等)
│           │   ├── network/                   # 网络请求层
│           │   └── repository/                # 数据仓库层
│           ├── mcp/                           # ★ MCP Server 模块
│           └── ui/                            # 界面层
│               ├── theme/                     #   Material3 主题
│               ├── schedule/                  #   课表主页
│               ├── game/                      #   游戏中心
│               ├── grade/                     #   成绩页面
│               ├── settings/                  #   设置页面
│               ├── manage/                    #   课程管理
│               ├── camera/                    #   AI 拍照 (开发中)
│               └── gallery/                   #   图片导入识别
├── build.gradle.kts                           # 根构建文件 (插件声明)
├── settings.gradle.kts                        # 项目设置，单模块 :app
├── gradle/
│   ├── libs.versions.toml                     # 版本目录
│   └── wrapper/
├── gradle.properties
└── local.properties                           # SDK 路径 (不入库)
```

## 模块详解

### `data/ai/` — AI Agent 模块

基于 OpenAI 兼容 API 的 Tool-calling Agent，支持多轮对话自动调用工具操作课表数据库。

| 文件 | 作用 |
|---|---|
| `AiAgent.kt` | Agent 主循环，最多 10 轮 tool-call → execute → append，直到模型返回纯文本回复 |
| `AiConfig.kt` | AI 提供商注册表（OpenAI / DeepSeek / Gemini / 通义千问 / 文心一言 / 豆包 / Ollama 等 8 家），API Key 与模型名持久化到 SharedPreferences |
| `AiService.kt` | 基于 OkHttp 的 chat/completions 请求封装 |
| `AiModels.kt` | OpenAI 兼容的请求/响应数据类 |
| `AiToolDefs.kt` | 5 个 Function-calling 工具 Schema 定义 |
| `AiToolExecutor.kt` | 工具执行器，解析参数后直接操作 Room DAO |
| `AiLogger.kt` | 结构化日志，追踪每次请求/工具调用/结果 |
| `SkillLoader.kt` | 从 `assets/skills/` 加载 Markdown 提示词 |

**支持的 5 个 AI 工具：** `get_schedule` / `add_course` / `update_course` / `delete_course` / `batch_import_courses`

### `mcp/` — MCP Server 模块

设备端运行的 MCP (Model Context Protocol) 服务器，允许外部 AI 客户端通过 HTTP JSON-RPC 操作课表。

| 文件 | 作用 |
|---|---|
| `McpServer.kt` | NanoHTTPD HTTP 服务器，默认端口 8090，路由 `POST /mcp`、`GET /health`、`GET /` |
| `McpService.kt` | Android 前台 Service，管理 McpServer 生命周期 |
| `McpModels.kt` | JSON-RPC + MCP 协议数据类 |
| `McpTools.kt` | 5 个 MCP 工具 Schema |
| `McpToolExecutor.kt` | 工具执行器（与 AiToolExecutor 同构，操作相同 DAO） |

**协议版本：** `2024-11-05`，支持 `initialize` / `tools/list` / `tools/call` / `ping`，在设置页面通过开关控制启停。

### `data/database/` — Room 数据库

数据库名 `wakeup_schedule.db`，包含以下实体：

| 实体 | 说明 |
|---|---|
| `CourseBaseBean` | 课程基础信息（名称、颜色），复合主键 `id + tableId` |
| `CourseDetailBean` | 课程时间详情（星期、教室、教师、起始节次、持续节数、周次范围），6 列复合主键 |
| `TableBean` | 课表配置（起始日期、最大周数、节数、颜色方案、显示选项等） |
| `HomeworkBean` | 作业记录（关联课程，文本内容，完成状态） |
| `TimeTableBean` | 时间表（上午/下午/晚上的时段划分） |
| `TimeDetailBean` | 每节课的起止时间（默认填充 12 节，08:00 – 22:45） |

**DAO：** `CourseDao` / `TableDao` / `TimeDao` / `HomeworkDao`

`CourseBean` 为非实体数据类，作为 `coursebase NATURAL JOIN coursedetail` 的查询投影。

### `data/network/` — 网络请求

| 文件 | 作用 |
|---|---|
| `ApiService.kt` | Retrofit 接口定义：用户注册/登录/信息获取、课表同步/增删改 |
| `RetrofitClient.kt` | Retrofit + OkHttp 单例，后端地址 `http://124.223.93.2:8012/api/` |
| `AuthInterceptor.kt` | 自动附加 `x-token` 请求头，透明轮换 `new-token` 响应头 |
| `dto/` | `ScheduleResponse` 网络响应模型 + DTO → Entity 映射 |

### `data/repository/` — 数据仓库

| 文件 | 作用 |
|---|---|
| `CourseRepository.kt` | 课程 CRUD + 网络同步（拉取远程 → 清表 → 重写入），离线优先 |
| `TableRepository.kt` | 课表配置读写 |
| `HomeworkRepository.kt` | 作业 CRUD |
| `UserRepository.kt` | 用户注册/登录/信息获取 |

### `data/local/` — 本地存储

`TokenManager.kt`：基于 SharedPreferences 管理用户 Token 和缓存的 User 对象。

### `ui/` — 界面层

| 目录/文件 | 技术 | 说明 |
|---|---|---|
| `schedule/` | Compose | 课表主页：日期/周数头部、当前课程卡片（渐变+环形倒计时）、七天课程网格、课程详情浮层、作业弹窗 |
| `game/` | Compose | 游戏中心：精选横幅、本周战绩、游戏卡片列表 |
| `grade/` | DataBinding | 成绩页面（当前为空状态占位） |
| `settings/` | DataBinding | 设置页面：AI 配置（提供商/模型/API Key）、MCP 服务开关、课表参数、登录弹窗 |
| `manage/` | Compose | 课程管理：课程列表（按 ID 分组）、新增/删除课程 |
| `camera/` | Compose | AI 拍照识别（当前为 Stub，仅显示"开发中"提示） |
| `gallery/` | Compose | 图片导入识别：选图 → Bitmap 压缩 → Base64 → AI Agent 识别 → 批量写入课表 |
| `theme/` | Compose | Material3 主题配置（Color / Theme / Type） |

**底部导航四个 Tab：** 课表 / 成绩 / 游戏 / 我的（设置）

### `assets/skills/` — AI 提示词

| 文件 | 作用 |
|---|---|
| `system_prompt.md` | Agent 系统 Prompt：角色设定、数据字段规则（day 1-7、startNode、step、周次范围）、行为规范 |
| `generate_schedule.md` | 引导模型生成示例大学课表 |
| `recognize_image.md` | 引导模型 OCR 识别课表图片并调用 `batch_import_courses` 写入 |

## 数据同步流程

```
App 启动 → ScheduleScreen
  ├── 本地 Room LiveData 自动加载（先展示缓存）
  └── viewModel.syncScheduleData()
       ├── Retrofit 请求远程后端
       ├── DTO → Entity 映射
       ├── 清空旧数据 → 批量写入 Room
       └── LiveData 自动通知 UI 刷新

  网络失败时：
       ├── 401 → 提示"未登录"
       ├── 本地有数据 → 继续展示缓存
       └── 本地为空 → fallback 插入示例数据
```

## AI 图片识别流程

```
GalleryImportActivity
  ├── 调用系统图片选择器
  ├── 选中图片 → 解码 Bitmap → JPEG 压缩 (quality=80) → Base64
  ├── 构建 OpenAI 多模态消息 (image_url: data:image/jpeg;base64,...)
  ├── 加载 recognize_image.md 作为 system prompt
  └── AiAgent.run()
       ├── 发送到配置的 AI 提供商 (chat/completions)
       ├── 模型返回 tool_calls → AiToolExecutor 执行
       │   └── batch_import_courses: 清空课表 → 批量写入识别结果
       ├── 工具结果追加到消息列表 → 继续对话
       └── 最终返回纯文本确认（最多 10 轮）
```

## 构建运行

1. Android Studio 打开项目根目录
2. 等待 Gradle Sync 完成
3. 运行到模拟器或真机（minSdk 26 / Android 8.0+）

## 依赖版本

| 依赖 | 版本 |
|---|---|
| AGP | 8.2.2 |
| Kotlin | 1.9.22 |
| KSP | 1.9.22-1.0.17 |
| compileSdk / targetSdk | 34 |
| Compose BOM | 2024.02.00 |
| Compose Compiler | 1.5.10 |
| Material3 | BOM 管理 |
| Room | 2.6.1 |
| Navigation | 2.7.7 |
| Lifecycle | 2.7.0 |
| Retrofit | 2.9.0 |
| OkHttp | 4.12.0 |
| NanoHTTPD | 2.3.1 |
| Coroutines | 1.8.0 |

## 应用信息

- **包名：** `com.solunis.schedule`
- **版本：** 4.0.0
- **最低系统：** Android 8.0 (API 26)
- **权限：** INTERNET / ACCESS_NETWORK_STATE / CAMERA / FOREGROUND_SERVICE / POST_NOTIFICATIONS
