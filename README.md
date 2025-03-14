# channels
A simple API for creating Pub/Sub channels using [lettuce](https://github.com/redis/lettuce).


## Use
How include the API with Maven:
```xml
<respositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/eupedroosouza/channels</url>
    </repository>
</respositories>

<dependencies>
    <dependency>
        <groupId>com.github.eupedroosouza</groupId>
        <artifactId>channels</artifactId>
        <version>x.y.z</version>
    </dependency>
</dependencies>
```

How include the API with Gradle (Groovy):
```groovy
repositories {
    maven { url 'https://maven.pkg.github.com/eupedroosouza/channels' }
}
dependencies {
    implementation "com.github.eupedroosouza:channels:x.y.z"
}
```

How include the API with Gradle (Kotlin DSL):
```groovy
repositories {
    maven("https://maven.pkg.github.com/eupedroosouza/channels")
}
dependencies {
    implementation("com.github.eupedroosouza:channels:x.y.z")
}
```
Replace x.y.z with your preferred version, see versions in [packages](https://github.com/eupedroosouza/channels/packages/2436137).

