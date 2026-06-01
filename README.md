# RBAC 权限管理系统

基于 **RBAC (Role-Based Access Control)** 模型的后台权限管理系统，采用前后端分离架构，实现用户认证、角色管理、菜单/权限管理、动态路由与按钮级权限控制。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **前端** | Vue.js + Element UI | Vue 2.6 / Element 2.15 |
| | Vue Router (动态路由) | 3.5 |
| | Vuex (状态管理) | 3.6 |
| | Axios (HTTP 请求) | 1.12 |
| **后端** | Spring Boot | 2.4.0 |
| | Spring Security (认证与授权) | — |
| | MyBatis Plus (ORM) | 3.4.1 |
| | JWT (无状态认证) | jjwt 0.9.1 |
| | Kaptcha (验证码) | 0.0.9 |
| **数据库** | MySQL | 5.7+ |
| **缓存** | Redis | — |
| **语言** | Java 1.8 | — |

---

## 功能特性

### 认证与授权
- **验证码登录** — 图形验证码 + Redis 校验，防止暴力破解
- **JWT 无状态认证** — Token 签发、校验、过期自动失效
- **Spring Security** — 集成 Security 过滤器链，方法级权限控制

### 用户管理
- 用户 CRUD（增删改查），支持按用户名搜索、批量删除
- 为用户分配一个或多个角色
- 密码重置（恢复默认密码）与个人中心密码修改
- 启用/禁用状态控制

### 角色管理
- 角色 CRUD，支持按名称搜索
- 通过**菜单权限树**为角色分配菜单与按钮权限
- 角色与菜单多对多关联

### 菜单管理（权限资源）
- 菜单 CRUD，三级树结构：
  - **目录**（type=0）：仅导航分组，无路径/组件
  - **菜单**（type=1）：对应前端路由，含路径与 Vue 组件
  - **按钮**（type=2）：权限标识，控制页面内按钮显隐
- 删除时自动清理子菜单与关联数据

### 动态路由与权限
- 登录后根据用户角色动态生成侧边栏菜单
- **按钮级权限控制** — 通过全局混入方法 `hasAuth(perms)` 控制按钮显隐
- 权限缓存至 Redis（1 小时 TTL），角色/菜单变更时自动清除

### 标签页导航
- 多标签页打开页面，首页标签固定不可关闭

---

## 项目结构

```
rbac-permission-system/
├── vuemanager.sql                    # 数据库建表 + 种子数据
├── vue_manger/                       # 前端项目 (Vue CLI)
│   ├── package.json
│   ├── vue.config.js
│   └── src/
│       ├── main.js                   # Vue 入口
│       ├── App.vue                   # 根组件
│       ├── axios.js                  # Axios 配置 (baseURL, 拦截器)
│       ├── globalFun.js              # 全局混入 (hasAuth 权限检查)
│       ├── router/index.js           # 路由配置 + 动态路由 + 导航守卫
│       ├── store/
│       │   ├── index.js              # Vuex 根 Store
│       │   └── modules/menu.js       # 菜单/权限状态模块
│       └── views/
│           ├── Login.vue             # 登录页
│           ├── Home.vue              # 主布局 (侧边栏 + 头部 + 标签页)
│           ├── Index.vue             # 仪表盘
│           ├── UserCenter.vue        # 个人中心 / 修改密码
│           ├── inc/
│           │   ├── SideMenu.vue      # 侧边导航组件
│           │   └── Tabs.vue          # 标签页组件
│           └── sys/
│               ├── User.vue          # 用户管理
│               ├── Role.vue          # 角色管理
│               └── Menu.vue          # 菜单管理
├── vue-manager-java/                 # 后端项目 (Spring Boot + Maven)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/
│       │   ├── VueManagerJavaApplication.java   # 启动类
│       │   ├── CodeGenerator.java               # MyBatis Plus 代码生成器
│       │   ├── config/               # Security, CORS, MyBatis Plus, Redis, Kaptcha 配置
│       │   ├── common/
│       │   │   ├── lang/             # 统一响应 Result, 常量, DTO
│       │   │   └── exception/        # 验证码异常, 全局异常处理
│       │   ├── entity/               # 实体类 (SysUser, SysRole, SysMenu, 关联表)
│       │   ├── mapper/               # MyBatis Mapper 接口 + XML
│       │   ├── service/              # 业务接口与实现
│       │   ├── security/             # JWT 过滤器, 登录/登出处理器, 权限拒绝处理
│       │   ├── controller/           # REST 控制器
│       │   └── util/                 # JWT 工具, Redis 工具
│       └── resources/
│           ├── application.yml        # 应用配置
│           └── mapper/               # Mapper XML
└── README.md
```

---

## 数据库设计

数据库名：`vuemanager`，共 5 张表：

### sys_user（用户表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK | 主键，自增 |
| username | varchar(64) UNIQUE | 登录用户名 |
| password | varchar(64) | BCrypt 加密密码 |
| avatar | varchar(255) | 头像 URL |
| email | varchar(64) | 邮箱 |
| city | varchar(64) | 城市 |
| created | datetime | 创建时间 |
| updated | datetime | 更新时间 |
| last_login | datetime | 上次登录时间 |
| statu | int(5) | 状态（1=启用，0=禁用） |

### sys_role（角色表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK | 主键，自增 |
| name | varchar(64) UNIQUE | 角色名称 |
| code | varchar(64) UNIQUE | 角色编码（如 admin, normal） |
| remark | varchar(64) | 备注 |
| created | datetime | 创建时间 |
| updated | datetime | 更新时间 |
| statu | int(5) | 状态 |

### sys_menu（菜单/权限表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK | 主键，自增 |
| parent_id | bigint(20) | 父菜单 ID（顶级为 0） |
| name | varchar(64) | 菜单名称 |
| path | varchar(255) | 路由路径 |
| perms | varchar(255) | 权限标识（如 sys:user:list） |
| component | varchar(255) | Vue 组件路径 |
| type | int(5) | 类型：0=目录, 1=菜单, 2=按钮 |
| icon | varchar(32) | Element UI 图标类名 |
| orderNum | int(11) | 排序号 |
| created | datetime | 创建时间 |
| updated | datetime | 更新时间 |
| statu | int(5) | 状态 |

### sys_user_role（用户-角色关联表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK | 主键 |
| user_id | bigint(20) | 用户 ID |
| role_id | bigint(20) | 角色 ID |

### sys_role_menu（角色-菜单关联表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK | 主键 |
| role_id | bigint(20) | 角色 ID |
| menu_id | bigint(20) | 菜单 ID |

### ER 关系
```
sys_user ──┐              ┌── sys_role ──┐
           ├── sys_user_role ──┤              ├── sys_role_menu ── sys_menu
           │                  │              │
```

---

## API 接口

### 认证接口
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/captcha` | 获取图形验证码 | 白名单 |
| POST | `/login` | 表单登录（username, password, code, token） | 白名单 |
| POST | `/logout` | 登出 | 白名单 |
| GET | `/sys/userInfo` | 获取当前用户信息与权限 | 已认证 |

### 用户管理 `/sys/user`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 分页查询用户 | `sys:user:list` |
| GET | `/info/{id}` | 用户详情（含角色） | `sys:user:list` |
| POST | `/save` | 新增用户 | `sys:user:save` |
| POST | `/update` | 修改用户 | `sys:user:update` |
| POST | `/delete` | 删除用户（支持批量） | `sys:user:delete` |
| POST | `/role/{userId}` | 分配角色 | `sys:user:role` |
| POST | `/repass` | 重置密码 | `sys:user:repass` |
| POST | `/updatePass` | 修改当前用户密码 | 已认证 |

### 角色管理 `/sys/role`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 分页查询角色 | `sys:role:list` |
| GET | `/info/{id}` | 角色详情 + 已分配菜单 ID | `sys:role:list` |
| POST | `/save` | 新增角色 | `sys:role:save` |
| POST | `/update` | 修改角色 | `sys:role:update` |
| POST | `/delete` | 删除角色 | `sys:role:delete` |
| POST | `/perm/{roleId}` | 为角色分配菜单权限 | `sys:role:perm` |

### 菜单管理 `/sys/menu`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/nav` | 当前用户导航菜单 + 权限列表 | 已认证 |
| GET | `/list` | 完整菜单树 | `sys:menu:list` |
| GET | `/info/{id}` | 菜单详情 | `sys:menu:list` |
| POST | `/save` | 新增菜单 | `sys:menu:save` |
| POST | `/update` | 修改菜单 | `sys:menu:update` |
| POST | `/delete/{id}` | 删除菜单（有子菜单时拒绝） | `sys:menu:delete` |

---

## 快速开始

### 环境要求
- **JDK** 1.8+
- **Node.js** 14+
- **MySQL** 5.7+
- **Redis**
- **Maven** 3.x

### 1. 初始化数据库
在 MySQL 中执行项目根目录的 SQL 脚本：
```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS vuemanager DEFAULT CHARACTER SET utf8mb4;
USE vuemanager;

-- 导入表结构与种子数据
SOURCE vuemanager.sql;
```

### 2. 启动后端
```bash
cd vue-manager-java

# 修改 application.yml 中的数据库连接与 Redis 配置（如需要）
# src/main/resources/application.yml

# 启动 Spring Boot 应用
mvn spring-boot:run
```
后端默认运行在 **http://localhost:8081**

### 3. 启动前端
```bash
cd vue_manger

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```
前端默认运行在 **http://localhost:8080**

### 4. 访问系统
打开浏览器访问前端地址，使用以下默认账户登录：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| **admin** | 888888 | 超级管理员（全部权限） |
| test | 888888 | 普通用户 |

---

## 认证流程

```
客户端                    服务端                      Redis
  │                        │                          │
  ├─ GET /captcha ────────>│                          │
  │<── 验证码图片 + token ──┤                          │
  │                        │── 存储 code:token ──────>│
  │                        │                          │
  ├─ POST /login ─────────>│                          │
  │  (username, password,  │<── 校验验证码 ────────────┤
  │   code, token)         │                          │
  │                        ├─ 校验用户名/密码           │
  │                        ├─ 生成 JWT                 │
  │<── Authorization: Bearer <jwt> ──┤                │
  │                        │                          │
  ├─ 后续请求 (Header:     │                          │
  │   Authorization) ─────>│                          │
  │                        ├─ JWT 过滤器校验 Token      │
  │                        ├─ @PreAuthorize 权限检查    │
  │<── 响应数据 ───────────┤                          │
```

---

## 权限控制说明

本系统实现三级权限控制：

1. **路由级** — 前端根据用户权限动态添加路由，无权限的页面无法访问
2. **菜单级** — 侧边栏仅展示用户有权限的菜单项
3. **按钮级** — 页面内通过 `hasAuth('sys:user:save')` 控制按钮显隐

示例：
```vue
<!-- 仅有 sys:user:save 权限的用户才能看到此按钮 -->
<el-button v-if="hasAuth('sys:user:save')" type="primary">新增用户</el-button>
```

权限字符串格式：`模块:资源:操作`，如 `sys:user:list`、`sys:role:delete`。

---

## 配置说明

### 后端配置 (`application.yml`)
```yaml
server:
  port: 8081                          # 服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vuemanager?...  # 数据库连接
    username: root                    # 数据库用户名
    password: admin                   # 数据库密码
  redis:                              # Redis 连接配置
    host: localhost
    port: 6379

vuemanager:
  jwt:
    header: Authorization             # JWT 请求头名称
    expire: 7200                      # Token 过期时间 (秒)
    secret: your-secret-key           # JWT 签名密钥
```

### 前端配置
- API 基础地址：修改 `vue_manger/src/axios.js` 中的 `baseURL`
- 开发代理：可在 `vue_manger/vue.config.js` 中配置 `devServer.proxy`

---

## 安全机制

- **密码加密**：用户密码使用 BCrypt 加密存储
- **验证码防护**：登录必须通过图形验证码校验，验证码存储在 Redis 中，一次性消费
- **JWT 认证**：无状态 Token，过期自动失效
- **方法级权限**：后端使用 `@PreAuthorize` 注解进行细粒度权限校验
- **CORS 配置**：后端配置跨域支持，可限制允许的来源
- **SQL 注入防护**：MyBatis Plus 参数化查询 + 防注入插件
