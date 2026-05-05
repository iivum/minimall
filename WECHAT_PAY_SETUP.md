# 微信支付配置指南
## Phase 9: 微信支付集成与上线
### 一、商户号 (mchid) 配置
1. 登录 [微信商户平台](https://pay.weixin.qq.com)
2. 获取商户号（在商户平台首页可以看到）
### 二、APIv3 密钥配置
1. 在商户平台 → API安全 → 设置APIv3密钥
2. 设置32位的APIv3密钥（自动生成或手动输入）
3. 保存好密钥，不要泄露
### 三、证书下载与配置
1. 在商户平台 → API安全 → 申请证书
2. 下载证书工具生成证书请求
3. 或直接在商户平台下载平台证书
4. 将证书文件放到 `src/main/resources/cert/` 目录：
   - `apiclient_key.pem` - 商户私钥
   - `apiclient_cert.pem` - 商户证书
### 四、支付回调地址配置
1. 在商户平台 → 支付回调URL
2. 配置回调URL，例如：`https://your-domain.com/api/pay/callback`
3. 确保该URL可公网访问且支持POST请求
### 五、应用配置
在 `application.properties` 中配置：
```properties
wechatpay.appid=YOUR_APPID
wechatpay.mchid=YOUR_MCHID
wechatpay.serialNo=YOUR_SERIAL_NO
wechatpay.privateKeyPath=classpath:cert/apiclient_key.pem
wechatpay.apiV3Key=YOUR_API_V3_KEY
wechatpay.callbackUrl=https://your-domain.com/api/pay/callback
wechatpay.sandbox=true
```
### 六、沙箱测试
1. 微信支付提供沙箱环境用于测试
2. 设置 `wechatpay.sandbox=true` 启用沙箱
3. 沙箱环境说明：
   - 沙箱API调用方式与正式环境一致
   - 沙箱资金不会真实扣款
   - 使用沙箱测试密钥和证书
### 七、正式环境切换
1. 完成沙箱测试后，切换到正式环境
2. 设置 `wechatpay.sandbox=false`
3. 使用正式的商户号、密钥和证书
4. 确保回调URL为公网可访问的HTTPS地址
### 八、测试验证
1. 创建订单
2. 调用 `/api/pay/create/{orderId}?openid=xxx` 获取prepay_id
3. 在小程序端调用 `wx.requestPayment()` 发起支付
4. 观察回调是否正常接收
5. 检查订单状态是否正确更新
### 九、注意事项
- 敏感信息（私钥、APIv3密钥）不要提交到代码仓库
- 生产环境建议使用环境变量或配置中心管理敏感配置
- 支付回调必须支持幂等性处理
- 建议记录完整的支付日志便于排查问题
