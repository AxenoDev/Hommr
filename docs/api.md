# Developer API

Hommr provides a comprehensive API that allows other plugins to interact with the home system.

## Setup

First, add Hommr to your project dependencies.

### Gradle

```groovy
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly "com.github.AxenoDev:Hommr:VERSION"
}
```

### Maven

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.AxenoDev</groupId>
    <artifactId>Hommr</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

## Getting the API Instance

You can retrieve the `HommrApi` instance using Bukkit's ServicesManager.

```java
import me.axeno.hommr.api.HommrApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public HommrApi getHommrApi() {
    RegisteredServiceProvider<HommrApi> provider = Bukkit.getServicesManager().getRegistration(HommrApi.class);
    if (provider != null) {
        return provider.getProvider();
    }
    return null;
}
```

## Methods

### Set a Home

Sets a home for a player at a specific location.

```java
boolean setHome(UUID playerUniqueId, String homeName, Location location);
```

### Get a Home

Retrieves a specific home object.

```java
Optional<Home> getHome(UUID playerUniqueId, String homeName);
```

### Delete a Home

Deletes a player's home.

```java
boolean deleteHome(UUID playerUniqueId, String homeName);
```

### List Homes

Gets the names of all homes belonging to a player.

```java
Set<String> getHomeNames(UUID playerUniqueId);
```

### Teleport to Home

Teleports a player to one of their homes. This triggers the `HomeTeleportEvent`.

```java
boolean teleportToHome(Player player, String homeName);
```

### Get Home Count

Gets the number of homes set by a player.

```java
int getHomeCount(UUID playerUniqueId);
```

### Check Home Existence

Checks if a player has a home with a specific name.

```java
boolean hasHome(UUID playerUniqueId, String homeName);
```
