# 校园集市 · 校园二手交易平台

> 一个开箱即跑的校园二手交易平台：SpringBoot + Vue3 全栈，零配置启动（内置 H2 数据库 + 演示数据），适合课程作业、毕业设计原型与前后端练手。

![tech](https://img.shields.io/badge/backend-SpringBoot%202.7-orange)
![tech](https://img.shields.io/badge/frontend-Vue%203%20%2B%20Vite-42b883)
![tech](https://img.shields.io/badge/database-H2%20(MySQL%20mode)-blue)
![license](https://img.shields.io/badge/license-MIT-lightgrey)

## ✨ 功能清单

| 模块 | 能力 |
| --- | --- |
| 账号 | 注册、登录（JWT）、个人资料修改 |
| 商品 | 发布、编辑、上架 / 下架、分页列表、关键词搜索、分类筛选、4 种排序、浏览量统计 |
| 交易 | 下单、取消订单、确认收货、我买到的 / 我卖出的 |
| 收藏 | 收藏 / 取消收藏、收藏列表 |
| 首页 | 数据概览（在售数 / 用户数 / 成交数）、分类导航 |
| 管理后台 | 数据看板（用户 / 商品 / 订单 / GMV / 最近 7 天趋势）、用户与角色管理、商品强审下架、全站订单、分类增删改 |
| 工程化 | 统一响应体、全局异常处理、参数校验、登录拦截器、角色权限校验、事务控制、演示数据自动初始化 |

## 🧱 技术栈

**后端**：Java 11、Spring Boot 2.7.18、MyBatis-Plus 3.5.3.2、H2（MySQL 兼容模式）、JJWT、BCrypt、Lombok、Maven

**前端**：Vue 3.5、Vite 5、Vue Router 4、Pinia 2、Element Plus 2.8、Axios

## 📁 目录结构

```
campus-market
├── backend
│   ├── pom.xml
│   └── src/main
│       ├── java/com/campus/market
│       │   ├── common        # 统一响应 Result / 分页 PageResult / 业务异常 / 全局异常处理
│       │   ├── config        # 跨域、登录拦截器、MyBatis-Plus、Jackson、演示数据初始化
│       │   ├── controller    # 8 个 REST 控制器（含管理后台）
│       │   ├── dto / vo      # 入参与出参对象
│       │   ├── entity        # 5 张表实体
│       │   ├── mapper        # BaseMapper + 联表查询
│       │   ├── service       # 业务逻辑
│       │   └── util          # JWT / BCrypt
│       └── resources
│           ├── application.yml
│           ├── schema.sql    # 建表脚本（启动自动执行）
│           └── mapper/*.xml  # 联表查询 SQL
└── frontend
    ├── package.json
    ├── vite.config.js       # /api 代理到 8080
    └── src
        ├── api              # axios 封装 + 接口定义
        ├── components       # 商品卡片
        ├── router           # 路由与登录守卫
        ├── store            # Pinia 用户状态
        ├── styles
        └── views            # 首页 / 详情 / 发布 / 登录 / 注册 / 个人中心 / 管理后台
```

## 🚀 快速启动

### 1. 启动后端（默认 8080）

```bash
cd backend
mvn spring-boot:run
# 或先打包再运行
mvn clean package -DskipTests
java -jar target/campus-market.jar
```

启动后浏览器访问 `http://localhost:8080/api/items?page=1&size=4` 可看到商品数据。

- H2 数据文件生成在 `backend/data/`，删除该目录即可重置全部数据。
- H2 控制台：`http://localhost:8080/h2-console`，JDBC URL `jdbc:h2:file:./data/campus_market;MODE=MySQL;DATABASE_TO_LOWER=TRUE`，用户名 `sa`，密码 `sa`。

### 2. 启动前端（默认 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 `http://localhost:5173` 即可。前端通过 Vite 代理访问后端，无需额外配置跨域。

### 3. 演示账号

| 用户名 | 密码 | 说明 |
| --- | --- | --- |
| `demo` | `123456` | 主演示账号，名下有 3 件在售闲置 |
| `linxm` / `zhouza` / `suqing` | `123456` | 其他卖家账号，可用来测试「下单 → 确认收货」全流程 |
| `admin` | `123456` | **管理员账号**，登录后顶栏出现「管理后台」入口 |

首次启动会自动写入 6 个分类、5 位用户（含 1 位管理员）、12 件商品。

## 🔌 接口一览

统一返回结构：

```json
{ "code": 0, "message": "ok", "data": { ... } }
```

`code`：0 成功，400 参数/业务错误，401 未登录，403 无权限，404 不存在，500 服务异常。

| 方法 | 路径 | 说明 | 需登录 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录 | 否 |
| GET | `/api/user/me` | 当前用户资料 | 是 |
| PUT | `/api/user/me` | 修改资料 | 是 |
| GET | `/api/categories` | 分类列表 | 否 |
| GET | `/api/items` | 商品分页（`keyword` / `categoryId` / `sort` / `page` / `size`） | 否 |
| GET | `/api/items/{id}` | 商品详情（自动 +1 浏览量） | 否 |
| GET | `/api/items/mine` | 我发布的（可按 `status` 过滤） | 是 |
| POST | `/api/items` | 发布商品 | 是 |
| PUT | `/api/items/{id}` | 编辑商品（仅本人） | 是 |
| POST | `/api/items/{id}/off-shelf` | 下架 | 是 |
| POST | `/api/items/{id}/on-shelf` | 重新上架 | 是 |
| POST | `/api/orders` | 下单（body：`{ "itemId": 1 }`） | 是 |
| GET | `/api/orders?role=bought\|sold` | 订单列表 | 是 |
| POST | `/api/orders/{id}/cancel` | 取消订单 | 是 |
| POST | `/api/orders/{id}/confirm` | 确认收货 | 是 |
| GET | `/api/favorites` | 收藏列表 | 是 |
| POST | `/api/favorites/{itemId}` | 收藏 | 是 |
| DELETE | `/api/favorites/{itemId}` | 取消收藏 | 是 |
| GET | `/api/stats/summary` | 首页数据概览 | 否 |

管理后台接口（统一前缀 `/api/admin`，后端 `@RequireAdmin` 校验角色）：

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/admin/summary` | 数据概览：用户 / 商品 / 订单 / GMV / 最近 7 天趋势 | 管理员 |
| GET | `/api/admin/users` | 用户分页（`keyword` / `role` / `page` / `size`） | 管理员 |
| PUT | `/api/admin/users/{id}/role` | 调整角色（body：`{ "role": "ADMIN" }`），不能改自己 | 管理员 |
| GET | `/api/admin/items` | 全站商品分页（`keyword` / `status` / `page` / `size`） | 管理员 |
| POST | `/api/admin/items/{id}/status` | 强制上架（`{"status":1}`）或下架（`{"status":4}`） | 管理员 |
| GET | `/api/admin/orders` | 全站订单分页（可按 `status` 过滤） | 管理员 |
| GET | `/api/admin/categories` | 分类列表 | 管理员 |
| POST | `/api/admin/categories` | 新增分类（body：`{ "name": "乐器", "icon": "🎸", "sort": 7 }`） | 管理员 |
| PUT | `/api/admin/categories/{id}` | 编辑分类 | 管理员 |
| DELETE | `/api/admin/categories/{id}` | 删除分类，分类下还有商品时拒绝 | 管理员 |

> 需要登录的接口请携带请求头 `Authorization: Bearer <token>`。

## 🗃️ 数据模型

```
t_user(id, username, password, nickname, avatar, phone, intro, role, created_at)
t_category(id, name, icon, sort)
t_item(id, title, description, price, original_price, category_id, seller_id,
       image_url, item_condition, location, status, view_count, created_at, updated_at)
t_order(id, order_no, item_id, buyer_id, seller_id, amount, status, created_at, finished_at)
t_favorite(id, user_id, item_id, created_at)
```

商品状态：`1 在售 / 2 交易中 / 3 已售出 / 4 已下架`；订单状态：`1 待确认 / 2 已完成 / 3 已取消`。

## 🔄 交易流程

1. 买家在商品详情页点击「立即购买」→ 生成订单，商品进入「交易中」，其他人无法再下单；
2. 买卖双方线下当面验货交易；
3. 买家在「我的 - 我买到的」点击「确认收货」→ 订单完成，商品变为「已售出」；
4. 任意一方可在完成前取消订单 → 订单关闭，商品回到「在售」。

## 🛡 管理员后台

登录管理员账号后，顶栏出现「管理后台」入口，页面地址 `/admin`，五个模块：

| 模块 | 能做什么 |
| --- | --- |
| 数据概览 | 用户数 / 商品数 / 在售 / 已下架 / 订单数 / 成交总额 GMV，以及最近 7 天新增商品与成交订单柱状图 |
| 用户与权限 | 关键词、角色筛选，一键把普通用户设为管理员或降级；管理员不能修改自己的角色 |
| 商品审核 | 按关键词、状态检索全站商品，对在售商品强制下架、对下架商品恢复上架（交易中 / 已售出不允许改状态） |
| 全站订单 | 按状态查看所有订单，含订单号、商品、金额、买卖双方 |
| 分类管理 | 新增、编辑、删除分类；分类下还有商品时拒绝删除 |

权限模型：

1. 用户表已有 `role` 字段（`USER` / `ADMIN`），登录签发的 JWT 中携带 `role`；
2. 拦截器解析 token 后写入 `CurrentUser`，标注 `@RequireAdmin` 的接口在角色不符时返回 `403`；
3. 前端路由 `/admin` 带 `admin` 守卫，非管理员访问会被重定向回首页；
4. 管理员入口只在 `role === 'ADMIN'` 时出现在顶栏。

## 🛠 二次开发提示

- 换成 MySQL：改 `application.yml` 的 `spring.datasource`，把 `schema.sql` 中 `CLOB` 换成 `TEXT`、`TINYINT` 保持，再补上 `utf8mb4` 字符集即可；
- 生产环境请替换 `app.jwt.secret`，并把密码盐、图片上传（OSS / 本地存储）接进来；
- 想加聊天、评价、举报处理，可直接在 `controller / service` 层扩展，前端按现有 `views` 结构加页面即可；
- 管理员后台已内置角色体系，新增后台能力时，只需在 `AdminController` 加接口并在 `AdminView.vue` 左侧菜单加一项。

## 📄 License

MIT
