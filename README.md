# Learn Redis

一个用于学习 Redis 相关知识的 Spring Boot 项目，包含 Docker 启动 Redis 以及 Spring 整合 Redis 的基础实践。

## 技术栈

- **Java** 21
- **Spring Boot** 3.5.13
- **Maven**
- **Docker** / Docker Compose

## 项目结构

```
learn-redis/
├── docker-redis/               # Docker 相关配置
│   ├── docker-compose.yml     # Redis 容器编排
│   ├── redis.conf            # Redis 配置文件
│   └── data/                  # Redis 数据持久化目录
├── src/main/java/            # Java 源码
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
