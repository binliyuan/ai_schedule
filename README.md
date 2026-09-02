# WakeupSchedule — AI 智能课程表

一款基于 Jetpack Compose + Material3 的现代 Android 课程表应用，支持 AI 拍照识别、网络同步、游戏中心等功能。

## 截图预览

| 课表主页 | 课程详情（长按） | 作业弹窗 |
|:---:|:---:|:---:|
| ![课表主页](screenshots/schedule_main.png) | ![课程详情](screenshots/course_detail_overlay.png) | ![作业弹窗](screenshots/homework_popup.png) |

| 游戏中心 | 课程管理 | AI 拍照识别 |
|:---:|:---:|:---:|
| ![游戏中心](screenshots/game_center.png) | ![课程管理](screenshots/course_manage.png) | ![AI拍照](screenshots/ai_camera.png) |

| 图片导入 | 成绩页面 | 设置页面 |
|:---:|:---:|:---:|
| ![图片导入](screenshots/gallery_import.png) | ![成绩](screenshots/grade.png) | ![设置](screenshots/settings.png) |

> **添加截图方法：** 在模拟器/真机上运行 App，逐个打开上述页面截图，保存到 `screenshots/` 目录并命名为表格中对应的文件名。

## 技术架构

```
┌─────────────────────────────────────────┐
│           UI Layer                       │
│  Jetpack Compose + Material3             │
│  + 传统 View + DataBinding               │
├─────────────────────────────────────────┤
│        Navigation                        │
│  Jetpack Navigation (Fragment + Compose) │
├─────────────────────────────────────────┤
│        Business Layer                    │
│  ViewModel + LiveData + Coroutines       │
├─────────────────────────────────────────┤
│        Network                           │
│  Ktor Client + OkHttp3 + Retrofit2       │
│  + Cronet (加速)                         │
├─────────────────────────────────────────┤
│        Data                              │
│  Room Database                           │
└─────────────────────────────────────────┘
```

## 页面说明

### 1. 课表主页 (`ScheduleScreen`)
- 顶部显示日期、当前周数、星期
- 网络同步状态指示器（正在同步 / 已同步）
- 当前课程卡片（渐变背景 + 环形倒计时）
- 三个操作按钮：📋 管理 / 📷 AI拍照 / 🖼️ 图片导入
- 七天课程网格，支持 12 节课显示
- 彩色课程块（7 种配色），作业状态红/绿点标记

### 2. 课程详情浮层 (`CourseDetailOverlay`)
- 长按当前课程卡片触发
- 毛玻璃暗色背景 + 装饰性渐变光斑
- 大号环形倒计时（剩余分钟数）
- 课程进度条 + 开始/结束时间
- 下一节课提示

### 3. 作业弹窗 (`HomeworkPopup`)
- 点击课程网格中任意课程块触发
- ModalBottomSheet 样式
- 编辑课程名称/教室 → 保存同步到数据库
- 作业清单：勾选完成/添加新作业

### 4. 游戏中心 (`GameScreen`)
- 深色主题渐变背景
- 精选游戏横幅（单词消消乐 + 经验值进度条）
- 本周战绩统计（闯关/单词/正确率/连胜）
- 横向滚动游戏卡片列表

### 5. 课程管理 (`CourseManageActivity`)
- 独立 Activity，查看所有课程列表
- 按课程 ID 分组显示，包含所有上课时间段
- 左侧彩色条标识课程颜色
- 支持添加新课程（填写名称/教室/星期/节次）
- 支持删除课程，操作实时同步到 Room 数据库

### 6. AI 拍照识别 (`AiCameraActivity`)
- 独立 Activity，请求相机权限
- 渐变圆形图标 + 功能说明
- 拍摄纸质课表/屏幕截图，AI 自动识别导入（功能预留）

### 7. 图片导入 (`GalleryImportActivity`)
- 独立 Activity，调用系统图片选择器
- 选择课表截图后自动解析（功能预留）

### 8. 成绩页面 (`GradeFragment`)
- 传统 View + DataBinding
- 成绩列表（当前为空状态占位）

### 9. 设置页面 (`SettingsFragment`)
- 传统 View + DataBinding
- 课表设置：最大周数、每天节数、显示周末开关
- 关于信息：应用名称 + 版本号

## 项目结构

```
app/src/main/java/com/solunis/schedule/
├── WakeupScheduleApp.kt              # Application 入口
├── MainActivity.kt                    # 主 Activity (Fragment Navigation + BottomNav)
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt            # Room 数据库 (6 实体, 4 DAO)
│   │   ├── entity/                    # CourseBaseBean, CourseDetailBean, TableBean...
│   │   └── dao/                       # CourseDao, TableDao, TimeDao, HomeworkDao
│   ├── network/
│   │   ├── ApiService.kt             # Retrofit 接口 (syncSchedule, getGrades)
│   │   ├── RetrofitClient.kt         # OkHttp + Retrofit 单例
│   │   ├── KtorClient.kt             # Ktor HTTP 客户端
│   │   ├── CronetClient.kt           # Cronet HTTP/3 加速
│   │   ├── MockInterceptor.kt        # OkHttp 拦截器 (模拟网络数据)
│   │   └── dto/                       # ScheduleResponse, DtoMapper
│   └── repository/                    # CourseRepository (含 syncFromNetwork), TableRepository, HomeworkRepository
├── ui/
│   ├── theme/                         # Color.kt, Theme.kt, Type.kt (Material3)
│   ├── schedule/
│   │   ├── ScheduleFragment.kt        # Fragment (Compose)
│   │   ├── ScheduleScreen.kt          # 主课表 Compose 页面
│   │   ├── ScheduleViewModel.kt       # ViewModel (网络同步 + 本地数据)
│   │   └── components/                # ScheduleHeader, CurrentClassCard, DaySelector,
│   │                                  # ScheduleGrid, CourseBlock, HomeworkPopup, CourseDetailOverlay
│   ├── game/
│   │   ├── GameFragment.kt            # Fragment (Compose)
│   │   ├── GameScreen.kt              # 游戏中心页面
│   │   └── GameViewModel.kt
│   ├── grade/
│   │   ├── GradeFragment.kt           # Fragment (DataBinding)
│   │   └── GradeViewModel.kt
│   ├── settings/
│   │   ├── SettingsFragment.kt         # Fragment (DataBinding)
│   │   └── SettingsViewModel.kt
│   ├── manage/
│   │   ├── CourseManageActivity.kt     # 课程管理 (独立 Activity)
│   │   └── CourseManageViewModel.kt
│   ├── camera/
│   │   └── AiCameraActivity.kt         # AI 拍照 (独立 Activity)
│   └── gallery/
│       └── GalleryImportActivity.kt    # 图片导入 (独立 Activity)
└── utils/
    └── DateUtils.kt
```

## 数据同步流程

```
App 启动 → ScheduleScreen
  ├── 本地 Room LiveData 自动加载（先展示缓存）
  └── viewModel.syncScheduleData()
       ├── 网络请求 → MockInterceptor 返回 JSON
       ├── DTO → Entity 映射
       ├── 清空旧数据 → 批量写入 Room
       └── LiveData 自动通知 UI 刷新
       
  网络失败时：
       ├── 本地有数据 → 继续展示
       └── 本地为空 → fallback 插入示例数据
```

## 构建运行

1. 用 Android Studio 打开 `D:\android project`
2. 等待 Gradle Sync 完成
3. 运行到模拟器或真机（minSdk 26 / Android 8.0+）

## 依赖版本

| 依赖 | 版本 |
|---|---|
| AGP | 8.2.2 |
| Kotlin | 1.9.22 |
| Compose BOM | 2024.02.00 |
| Compose Compiler | 1.5.10 |
| Material3 | BOM 管理 |
| Room | 2.6.1 |
| Navigation | 2.7.7 |
| Lifecycle | 2.7.0 |
| Retrofit | 2.9.0 |
| OkHttp | 4.12.0 |
| Ktor | 2.3.8 |
| Coroutines | 1.8.0 |

## 包名

```
com.solunis.schedule
```

如需更改包名，全局搜索替换 `com.solunis.schedule` 为新包名，同时移动 `app/src/main/java/com/solunis/schedule/` 目录。
