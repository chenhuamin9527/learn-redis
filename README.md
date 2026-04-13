# Learn Redis

一个用于学习 Redis 相关知识的 Spring Boot 项目，包含 Docker 启动 Redis 以及 Spring 整合 Redis 的基础实践。

## 技术栈

- **Java** 21
- **Spring Boot** 3.5.13
- **Maven**
- **Docker**
- **MyBatis-Plus**
- **Guava**

## 项目结构

```
learn-redis/
├── docker-redis/               # Docker 相关配置
│   ├── docker-compose.yml     # Redis 容器编排
│   ├── redis.conf            # Redis 配置文件
│   └── data/                  # Redis 数据持久化目录
├── src/main/java/            # Java 源码
│   └── com/mark/learnRedis/
│       ├── config/           # 配置类
│       ├── controller/        # 控制器
│       ├── service/           # 服务层
│       ├── mapper/            # 数据访问层
│       └── entity/            # 实体类
└── src/main/resources/       # 配置文件
```

## 快速开始

### 1. 启动 Redis

```bash
cd docker-redis
docker-compose up -d
```

验证 Redis 是否启动成功：

```bash
docker exec -it redis redis-cli ping
```

如果需要在宿主机或其他机器连接容器的 redis，需要修改 redis.conf 配置：

```config
# 注释绑定本机地址
# bind 127.0.0.1 -::1

# 关闭保护模式，否则只能通过进入容器使用 redis-cli 连接
protected-mode no
```

### 2. 运行 Spring Boot 应用

```bash
./mvnw spring-boot:run
```

或使用 IDE 直接运行 `LearnRedisApplication`。

## Docker Redis 说明

- **镜像**: redis:latest
- **端口**: 6379
- **配置文件挂载**: `./docker-redis/redis.conf` -> `/usr/local/bin/redis.conf`
- **数据目录挂载**: `./docker-redis/data` -> `/data`

## 停止 Redis

```bash
cd docker-redis
docker-compose down
```

如需清除数据：

```bash
docker-compose down -v
```

## Spring Cache 缓存

项目使用 Spring Cache 结合 Redis 实现缓存功能。

### 常用注解

| 注解 | 作用 |
|------|------|
| `@Cacheable` | 先查缓存，缓存没有才执行方法 |
| `@CachePut` | 每次都执行方法，并更新缓存 |
| `@CacheEvict` | 删除缓存 |

### 使用示例

```java
@Service
public class UserService {

    @Cacheable(cacheNames = "user", key = "#id")
    public User getByIdCache(Long id) {
        return this.getById(id);
    }

    @CachePut(cacheNames = "user", key = "#user.id")
    public User updateUser(User user) {
        this.updateById(user);
        return user;
    }

    @CacheEvict(cacheNames = "user", key = "#id")
    public void deleteUser(Long id) {
        this.removeById(id);
    }
}
```

## 布隆过滤器 (Bloom Filter)

> 本项目的布隆过滤器实现仅用于学习理解原理。生产环境建议使用 RedisBloom 官方模块。

### 布隆过滤器原理

布隆过滤器是一种空间效率极高的概率型数据结构，用于判断一个元素是否"可能存在"于集合中。

**核心思想**：
- 用一个大型位数组存储数据
- 使用多个哈希函数将元素映射到位数组的多个位置
- 添加时将对应位置设为 1
- 查询时检查所有位置是否都为 1

```
添加 "zhangsan"：
    ↓
  hash1 = 7    → 第 7 位设为 1
  hash2 = 13   → 第 13 位设为 1
  hash3 = 4    → 第 4 位设为 1

位数组: [0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0]
             4    7              13
```

### 核心参数公式

```
m = -(n × ln(fpp)) / (ln(2)²)    // 位数组大小
k = (m / n) × ln(2)              // 哈希函数数量
```

其中：
- `n` = 预期插入数量
- `fpp` = 期望误判率（0.0 ~ 1.0）

### 使用示例

```java
@Service
public class UserService {
    private final BloomFilter bloomFilter;

    @PostConstruct
    public void init() {
        bloomFilter.init("user", 10000, 0.01);
    }

    public User getByIdCache(Long id) {
        // 布隆过滤器返回 false，说明确定不存在
        if (!bloomFilter.contains("user", String.valueOf(id))) {
            return null;
        }
        // 布隆过滤器返回 true，可能存在，查询数据库
        User user = this.getById(id);
        if (user != null) {
            bloomFilter.add("user", String.valueOf(id));
        }
        return user;
    }

    public boolean saveUser(User user) {
        boolean saved = this.save(user);
        if (saved) {
            bloomFilter.add("user", String.valueOf(user.getId()));
        }
        return saved;
    }
}
```

### 二次哈希法 (Double Hashing)

使用两个哈希函数组合出 k 个位置，避免实现 k 个独立哈希函数：

```
h(i) = h1 + i × h2
```

本项目使用 Guava 的 MurmurHash3 作为哈希函数。

## RedisBloom 官方模块

> 本项目的布隆过滤器是手写实现，仅用于学习原理。生产环境推荐使用 RedisBloom 模块。
[RedisBloom](https://redis.io/docs/latest/develop/data-types/probabilistic/bloom-filter/) 是 Redis 官方提供的布隆过滤器模块，支持：
