# MiniMall 贡献指南

感谢您对 MiniMall 项目的关注！我们欢迎所有形式的贡献，包括代码、文档、问题反馈等。

## 贡献方式

### 报告问题
- 使用 GitHub Issues 报告 bug 或功能请求
- 描述问题的背景、重现步骤和期望行为
- 搜索现有问题，避免重复

### 代码贡献
1. Fork 仓库
2. 创建特性分支 (`git checkout -b feature/my-feature`)
3. 提交更改 (`git commit -m 'feat: 添加新功能'`)
4. 推送到分支 (`git push origin feature/my-feature`)
5. 创建 Pull Request

### 文档贡献
- 修复错别字或改进文档表述
- 补充缺失的文档内容
- 翻译文档为其他语言

## 开发流程

### 环境准备
```bash
# 克隆仓库
git clone https://github.com/iivum/minimall.git
cd minimall

# 安装依赖
./mvnw dependency:go-offline
```

### 代码规范
- 遵循 Google Java Style Guide
- 运行 Checkstyle 检查代码风格
```bash
./mvnw checkstyle:check
```

### 测试
```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=ProductServiceTest
```

### 提交规范
遵循 Conventional Commits 规范：
- `feat:` 新功能
- `fix:` 修复 bug
- `docs:` 文档变更
- `refactor:` 代码重构
- `test:` 测试相关
- `chore:` 构建/工具变更

## 分支策略

- `main` - 生产分支，受保护
- `agent/*` - Agent 工作分支
- `feature/*` - 功能开发分支
- `fix/*` - bug 修复分支

## Pull Request 流程

1. 确保代码通过所有检查
2. 更新相关文档
3. 描述您的更改内容
4. 等待代码审查

## 许可证

通过贡献代码，您同意您的贡献将在 MIT 许可证下发布。详见 [LICENSE](../LICENSE)。

## 问题咨询

如有疑问，请通过 GitHub Issues 联系项目维护者。