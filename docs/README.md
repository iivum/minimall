# MiniMall 文档导航

本文档提供项目文档的完整索引，帮助您快速找到所需信息。

## 文档结构

```
docs/
├── adr/                    # 架构决策记录
│   ├── README.md          # ADR 索引
│   └── ADR-0001-tech-stack.md
├── api/                   # API 参考文档
│   └── README.md
├── deployment/            # 部署指南
│   └── docker.md
├── faq/                   # 常见问题
│   └── README.md
├── meetings/             # 会议记录
│   ├── Sprint_28_Meeting_Minutes.md
│   └── phase12-sprint-planning-meeting.md
├── monitoring/           # 监控告警
│   ├── ALERTING.md
│   └── grafana-dashboard.json
├── superpowers/          # Agent 工作计划
│   └── plans/
├── user-guide/           # 用户指南
│   ├── admin.md
│   └── miniprogram.md
└── sprint-34-report.md   # Sprint 报告
```

## 快速索引

### 新手入门
| 文档 | 说明 |
|------|------|
| [README.md](../README.md) | 项目概览和快速开始 |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | 贡献指南 |
| [docs/faq/README.md](faq/README.md) | 常见问题 |

### 开发参考
| 文档 | 说明 |
|------|------|
| [docs/api/README.md](api/README.md) | 完整 API 文档 |
| [docs/adr/README.md](adr/README.md) | 技术决策记录 |
| [docs/deployment/docker.md](deployment/docker.md) | Docker 部署指南 |

### 用户指南
| 文档 | 说明 |
|------|------|
| [docs/user-guide/admin.md](user-guide/admin.md) | 管理后台使用指南 |
| [docs/user-guide/miniprogram.md](user-guide/miniprogram.md) | 小程序用户指南 |

### 运营监控
| 文档 | 说明 |
|------|------|
| [docs/monitoring/ALERTING.md](monitoring/ALERTING.md) | 告警配置 |
| [docs/monitoring/grafana-dashboard.json](monitoring/grafana-dashboard.json) | Grafana 仪表盘 |

## 文档更新

文档随项目迭代持续更新。如发现文档过期或错误，请通过 Pull Request 提交修改。

## 相关链接
- GitHub 仓库: https://github.com/iivum/minimall
- API 文档: http://localhost:8080/swagger-ui.html (本地运行后)