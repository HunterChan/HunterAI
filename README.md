# HunterAI

HunterAI是一个基于人工智能的综合应用平台，提供智能分析、数据处理和自动化解决方案。

## 项目结构

- `server/`: 后端服务，基于Spring Boot框架
- `webui/`: 前端界面，基于Vue3和Element Plus

## 技术栈

### 后端
- Java 8+
- Spring Boot
- MyBatis
- MySQL

### 前端
- Vue 3
- Element Plus
- Pinia
- Vue Router

## 开发环境设置

### 后端服务

1. 确保已安装JDK 8或更高版本
2. 导入Maven项目
3. 运行`server`目录下的主应用类

```bash
cd server
mvn spring-boot:run
```

### 前端开发

1. 确保已安装Node.js (推荐v14+)
2. 安装依赖并启动开发服务器

```bash
cd webui
npm install
npm run dev
```

## 构建与部署

### 前端构建

```bash
cd webui
npm run build
```

构建后的文件将生成在`webui/dist`目录中，可以部署到任何静态Web服务器。

### 后端构建

```bash
cd server
mvn clean package
```

构建后的JAR文件将生成在`server/target`目录中。

## 贡献指南

欢迎提交问题报告和合并请求！

## 许可证

本项目采用MIT许可证 - 详情参见[LICENSE](LICENSE)文件。 