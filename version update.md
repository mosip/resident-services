# <a href="https://github.com/mosip/resident-services" style="background: url('image-url') repeat;">resident-services</a>

## <a href="https://github.com/mosip/resident-services/blob/master/resident/pom.xml" style="background: url('image-url') repeat;">resident\pom.xml (Parent pom.xml)</a>

## 1. Maven

### Older version:

```xml
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <maven.compiler.version>3.8.0</maven.compiler.version>
    <maven.jar.plugin.version>3.0.2</maven.jar.plugin.version>
    <maven.war.plugin.version>3.1.0</maven.war.plugin.version>
    <maven.surefire.plugin.version>2.22.0</maven.surefire.plugin.version>
    <maven.jacoco.version>0.8.5</maven.jacoco.version>
    <maven.sonar.plugin.version>3.7.0.1746</maven.sonar.plugin.version>
    <maven.javadoc.version>3.2.0</maven.javadoc.version>
    <maven-shade-plugin.version>2.3</maven-shade-plugin.version>
```

### Updated version:

```xml
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <maven.compiler.version>3.8.1</maven.compiler.version>
    <maven.jar.plugin.version>3.3.0</maven.jar.plugin.version>
    <maven.war.plugin.version>3.4.1-SNAPSHOT</maven.war.plugin.version>
    <maven.surefire.plugin.version>3.2.5</maven.surefire.plugin.version>
    <maven.jacoco.version>0.8.8</maven.jacoco.version>
    <maven.sonar.plugin.version>7.31.0.34839</maven.sonar.plugin.version>
    <maven.javadoc.version>3.6.3</maven.javadoc.version>
    <maven-shade-plugin.version>3.3.0</maven-shade-plugin.version>
```

## 2. Spring boot version and its related

### Older:

```xml
<spring.boot.version>2.0.2.RELEASE</spring.boot.version>
<spring.data.jpa.version>2.0.7.RELEASE</spring.data.jpa.version>
<spring.security.test.version>5.0.5.RELEASE</spring.security.test.version>
<spring-cloud-config.version>2.0.0.RELEASE</spring-cloud-config.version>
```

### Updated:

```xml
<spring.boot.version>2.7.18</spring.boot.version>
<spring.data.jpa.version>2.7.18</spring.data.jpa.version>
<spring.security.test.version>5.7.11</spring.security.test.version>
<spring-cloud-config.version>3.1.3</spring-cloud-config.version>
```

## 3. h2:

### Older:

```xml
<h2.version>1.4.197</h2.version>
```

### Updated:

```xml
<h2.version>2.2.224</h2.version>
```

## 4. Postgresql

### Older:

```xml
<postgresql.version>42.2.2</postgresql.version>
```

### Updated:

```xml
<postgresql.version>42.7.2</postgresql.version>
```

## 5. hibernate

### Older:

```xml
<hibernate.version>5.2.17.Final</hibernate.version>
```

### Updated:

```xml
<hibernate.version>6.4.4.Final</hibernate.version>
```

## 6. junit

### Older:

```xml
<junit.version>4.12</junit.version>
```

### Updated:

```xml
<junit.version>4.13.2</junit.version>
```

## 7. lombok

### Older:

```xml
<lombok.version>1.18.8</lombok.version>
```

### Updated:

```xml
<lombok.version>1.18.30</lombok.version>
```

## <a href="https://github.com/mosip/resident-services/blob/master/resident/resident-service/pom.xml" style="background: url('image-url') repeat;">resident\resident-service\pom.xml</a>

### Added plugin to support lombok (needed for java 17):

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${maven.compiler.version}</version>
    <configuration>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
        <fork>true</fork>
        <compilerArgs>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED</arg>
        </compilerArgs>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## 2. springdoc:

### Older:

```xml
<version>2.5.4</version>
```

### Updated:

```xml
<version>${spring.boot.version}</version>
```

