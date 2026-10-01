pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ★ 高德地图 SDK 仓库
        maven { url = uri("https://developer.amap.com/maven/") }
        // ★ 备用（阿里云镜像）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
    }
}

rootProject.name = "音乐库"
include(":app")