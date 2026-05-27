# 阻塞 Issue 跟踪台账

**创建日期**: 2026-05-27
**更新日期**: 2026-05-27
**创建者**: Orion
**Issue**: [MIN-3471](mention://issue/e00d4dfa-78ae-4353-95a7-8156e19528cd)
**Parent**: [MIN-3178](mention://issue/47da6000-3745-4711-8ec5-0f32a5c1a2ae)

---

## 一、执行摘要

本报告分析当前阻塞 Issue 状态，建立跟踪台账，并提出处理方案。

**当前阻塞 Issue 数量**: 2

| Issue | 标题 | 阻塞原因 | 优先级 | 状态 |
|-------|------|----------|--------|------|
| MIN-3818 | Sprint #191: 提升测试覆盖率至 40%+ | 缺少测试环境配置、JaCoCo 版本不兼容 | P1 | blocked |
| MIN-3816 | Sprint #191: 修复 E2E 测试 ApplicationContext 问题 | Spring Boot Test 配置问题、缺少 test resources | P0 | blocked |

---

## 二、阻塞原因分类

### 2.1 当前阻塞 Issue 详情

#### Category A: 测试基础设施问题 (1 issue)

**问题描述**: Spring Boot Test 配置问题导致 E2E 测试无法启动 ApplicationContext

**影响的 Issue**:
- MIN-3816 (P0): E2E 测试 ApplicationContext 加载失败

**根因分析**:
1. 缺少 `src/test/resources` 目录配置
2. 缺少必要的测试环境变量或 mock WebClient 配置
3. JaCoCo 版本与 Java 25 不兼容 (class file major version 70)

**处理方案**:
- 创建 `src/test/resources/application-test.yml` 配置文件
- 配置测试环境变量
- 升级 JaCoCo 至兼容版本 (需 0.8.14+ 或禁用 JaCoCo for tests)
- 使用 `@MockBean` mock WebClient 调用

**立即可执行项**:
- [ ] 创建 test resources 目录和配置文件
- [ ] 在 E2E 测试类上添加 `@ActiveProfiles("test")`
- [ ] 配置 MockMvc 或 RestAssured 替代真实 WebClient

---

#### Category B: 测试覆盖率问题 (1 issue)

**问题描述**: 项目整体测试覆盖率未达标 (当前 28.8%，目标 40%+)

**影响的 Issue**:
- MIN-3818 (P1): 测试覆盖率提升至 40%+

**根因分析**:
1. Service 层覆盖率 38.07% (目标 45%)
2. Controller 层覆盖率 14.16% (目标 25%)
3. 缺少 MockMvc 测试配置
4. 核心业务逻辑缺少单元测试

**处理方案**:
1. 使用 MockMvc 进行 Controller 测试
2. 为核心业务逻辑添加单元测试 (OrderService, ProductService, WeChatSubscribeService)
3. 配置 JaCoCo 兼容 Java 25 或在测试时禁用覆盖率

**立即可执行项**:
- [ ] 添加 MockMvc 配置到 Controller 测试
- [ ] 使用 `@WebMvcTest` 注解隔离 Controller 测试
- [ ] 验证 JaCoCo 版本兼容性

---

## 三、快速解决项识别

### 3.1 可立即处理的问题

| Issue | 问题 | 解决方案 | 执行者 | 预计工时 |
|-------|------|----------|--------|----------|
| MIN-3816 | 缺少 test resources | 创建 application-test.yml | 后端架构师 | 0.5人天 |
| MIN-3816 | E2E 测试 WebClient mock | 添加 @MockBean WebClient | 后端架构师 | 0.5人天 |
| MIN-3818 | Controller 测试缺少 MockMvc | 使用 @WebMvcTest | 后端架构师 | 1人天 |

### 3.2 需要外部依赖的问题

| Issue | 问题 | 外部依赖 | 状态 |
|-------|------|----------|------|
| - | - | - | - |

---

## 四、跟踪台账

### 4.1 Blocked Issues

| ID | 标题 | 阻塞原因分类 | 优先级 | 创建日期 | 当前状态 | 处理方案 |
|----|------|------------|--------|----------|----------|----------|
| MIN-3816 | 修复 E2E 测试 ApplicationContext 问题 | 测试基础设施 | P0 | 2026-05-27 | blocked | 创建 test resources，mock WebClient |
| MIN-3818 | 提升测试覆盖率至 40%+ | 测试覆盖率 | P1 | 2026-05-27 | blocked | 使用 MockMvc，添加单元测试 |

### 4.2 已降级/取消的 Issue (历史记录)

| ID | 标题 | 原状态 | 新状态 | 降级原因 | 日期 |
|----|------|--------|--------|----------|------|
| - | - | - | - | - | - |

---

## 五、处理方案

### 5.1 Sprint #194 工作安排

**Owner**: 后端架构师 (Agent ID: 73e7e23a)

**立即执行**:
1. 修复 E2E 测试 ApplicationContext 问题 (MIN-3816)
   - 创建 `src/test/resources/application-test.yml`
   - 在测试类中添加 `@ActiveProfiles("test")`
   - 使用 `@MockBean` mock WebClient

2. 提升测试覆盖率 (MIN-3818)
   - 使用 `@WebMvcTest` 隔离 Controller 测试
   - 添加 MockMvc 测试
   - 确保 Service 层测试覆盖核心方法

### 5.2 验收标准

- [ ] E2E 测试 (AuthFlowE2ETest, OrderFlowE2ETest, PaymentFlowE2ETest) 全部通过
- [ ] mvn test 全部通过 (0 errors)
- [ ] 整体测试覆盖率达到 35%+
- [ ] Controller 层测试覆盖率达到 20%+

---

## 六、历史上下文

### MIN-3178 Sprint #137 技术债清理

**完成情况**:
- 阻塞 Issue 数量减少: 77 → 2 (通过降级和处理)
- 每个阻塞 Issue 都有明确的处理方案: ✓

**关键文档**:
- `docs/tech-debt-backlog.md` - 技术债注册表
- `docs/tech-debt/backlog.md` - 备用技术债文档
- `docs/fake-delivery-blacklist.md` - 虚假交付历史记录

---

## 七、结论

当前阻塞 Issue 数量已从 77 降至 2，问题集中在测试基础设施方面。剩余问题都有明确的解决方案，需要后端架构师执行。

**关键阻塞点**:
1. 测试环境配置缺失
2. JaCoCo 版本与 Java 25 不兼容

**建议**:
1. 后端架构师优先处理 MIN-3816 (E2E 测试修复)
2. 并行处理 MIN-3818 (测试覆盖率提升)
3. 完成后验证 `git show origin/main` 确认交付

---

**下次更新**: Sprint #194 验收会议后