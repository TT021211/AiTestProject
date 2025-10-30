# Java 版本升级指南

## ⚠️ 问题说明

当前检测到您的系统使用的是 **Java 8 (1.8.0_152)**，但 MCP 架构项目需要 **Java 17** 才能运行。

```
当前版本: Java 8 (1.8.0_152)
所需版本: Java 17+
```

## 🔧 解决方案

### 方案1: 升级到 Java 17（推荐）

#### Windows

**方式 A: 使用 Oracle JDK**

1. 下载 Oracle JDK 17
   - 访问: https://www.oracle.com/java/technologies/downloads/#java17
   - 选择 Windows x64 Installer
   - 下载并运行安装程序

2. 配置环境变量
   ```
   JAVA_HOME=C:\Program Files\Java\jdk-17
   Path=%JAVA_HOME%\bin;其他路径...
   ```

3. 验证安装
   ```bash
   java -version
   # 应显示: java version "17.0.x"
   ```

**方式 B: 使用 OpenJDK**

1. 下载 Temurin JDK 17 (推荐)
   - 访问: https://adoptium.net/
   - 选择 JDK 17 (LTS)
   - 下载 Windows x64 MSI installer
   - 安装时勾选 "Set JAVA_HOME" 选项

2. 验证安装
   ```bash
   java -version
   # 应显示: openjdk version "17.0.x"
   ```

**方式 C: 使用 Chocolatey (如果已安装)**

```bash
choco install openjdk17
```

#### Linux

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**CentOS/RHEL:**
```bash
sudo yum install java-17-openjdk-devel
```

**验证:**
```bash
java -version
update-alternatives --config java  # 如果有多个版本
```

#### macOS

**使用 Homebrew:**
```bash
brew install openjdk@17
```

**配置环境:**
```bash
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### 方案2: 使用 SDKMAN 管理多版本 Java (推荐用于开发者)

**安装 SDKMAN:**
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

**安装和切换 Java 17:**
```bash
# 列出可用版本
sdk list java

# 安装 Java 17
sdk install java 17.0.9-tem

# 设为默认版本
sdk default java 17.0.9-tem

# 临时使用
sdk use java 17.0.9-tem
```

### 方案3: 临时降级项目到 Java 8 (不推荐)

如果暂时无法升级 Java，可以临时降级项目，但会失去一些特性：

**需要修改的内容:**

1. **pom.xml**
   ```xml
   <properties>
       <java.version>8</java.version>
       <maven.compiler.source>8</maven.compiler.source>
       <maven.compiler.target>8</maven.compiler.target>
   </properties>
   ```

2. **代码修改**
   - 删除所有 `var` 关键字，改用显式类型
   - 删除 Text Blocks (三引号字符串)
   - 删除 Switch Expressions
   - 删除 Records

3. **Spring Boot 降级**
   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>2.7.18</version>  <!-- 降级到 2.x -->
   </parent>
   ```

**⚠️ 警告**: 降级会导致以下问题：
- 失去 MCP 的许多现代特性
- 代码需要大量修改
- 性能下降
- 安全性降低
- 不推荐用于生产环境

## ✅ 推荐步骤

1. **备份当前环境**
   ```bash
   java -version > java_version_backup.txt
   echo %JAVA_HOME% >> java_version_backup.txt  # Windows
   echo $JAVA_HOME >> java_version_backup.txt   # Linux/Mac
   ```

2. **升级到 Java 17**
   - 使用上面的方案 1

3. **验证安装**
   ```bash
   java -version
   javac -version
   echo %JAVA_HOME%  # Windows
   echo $JAVA_HOME   # Linux/Mac
   ```

4. **重新编译项目**
   ```bash
   cd d:\cuixing-project\AiTestProject
   mvn clean compile
   ```

5. **运行项目**
   ```bash
   mvn spring-boot:run
   ```

## 🔍 常见问题

### Q1: 安装后还是显示 Java 8？

**A:** 可能是环境变量问题

**Windows:**
```cmd
# 检查当前 JAVA_HOME
echo %JAVA_HOME%

# 修改系统环境变量
系统属性 -> 高级 -> 环境变量
修改 JAVA_HOME 指向 Java 17 目录
修改 Path，确保 %JAVA_HOME%\bin 在前面
```

**重启命令行或IDE**

### Q2: Maven 还是使用 Java 8？

**A:** Maven 可能使用了自己的 Java 版本

```bash
# 检查 Maven 使用的 Java 版本
mvn -version

# 设置 JAVA_HOME 后重启 IDE
```

### Q3: 多个 Java 版本共存？

**A:** 使用 SDKMAN 或手动管理

```bash
# 方式 1: SDKMAN
sdk use java 17.0.9-tem

# 方式 2: 手动设置
set JAVA_HOME=C:\Program Files\Java\jdk-17  # Windows
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk  # Linux
```

### Q4: IDE (IDEA/Eclipse) 中如何配置？

**IntelliJ IDEA:**
```
File -> Project Structure -> Project
Project SDK: 选择 Java 17
Project language level: 17

File -> Settings -> Build, Execution, Deployment -> Build Tools -> Maven
Maven home directory: 确认
JRE for importer: 选择 Java 17
```

**Eclipse:**
```
Window -> Preferences -> Java -> Installed JREs
添加 Java 17
设为默认

右键项目 -> Properties -> Java Compiler
Compiler compliance level: 17
```

## 📝 验证清单

升级完成后，请检查：

- [ ] `java -version` 显示 17.x
- [ ] `javac -version` 显示 17.x
- [ ] `mvn -version` 显示使用 Java 17
- [ ] `echo %JAVA_HOME%` 指向 Java 17 目录
- [ ] IDE 配置使用 Java 17
- [ ] `mvn clean compile` 成功编译
- [ ] `mvn test` 测试通过
- [ ] `mvn spring-boot:run` 成功启动

## 🎯 推荐配置

**环境变量 (Windows):**
```
JAVA_HOME=C:\Program Files\Java\jdk-17
M2_HOME=C:\Program Files\Maven\apache-maven-3.9.x
Path=%JAVA_HOME%\bin;%M2_HOME%\bin;...
```

**环境变量 (Linux/Mac):**
```bash
# 添加到 ~/.bashrc 或 ~/.zshrc
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export M2_HOME=/usr/share/maven
export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
```

## 📞 需要帮助？

如果升级过程中遇到问题：

1. 查看 Maven 输出的完整错误信息
2. 确认 JAVA_HOME 和 Path 设置正确
3. 重启终端/命令行
4. 重启 IDE
5. 检查防火墙和代理设置

## 🚀 升级后的好处

使用 Java 17 的优势：

- ✅ 支持最新的语言特性（Text Blocks, Switch Expressions, Records）
- ✅ 更好的性能和内存管理
- ✅ 增强的安全性
- ✅ 长期支持 (LTS) 版本
- ✅ 更好的工具链支持
- ✅ 符合现代 Java 开发标准

---

**准备好升级后，运行:**

```bash
mvn clean install
mvn spring-boot:run
```

**祝升级顺利！** 🎉








