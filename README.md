<div align="center">
  <img src=".github/hommr.png" alt="Hommr Logo" width="100">
  <br>

  [![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg)](https://modrinth.com/plugin/hommr)
  ![Unsupported spigot](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/unsupported/spigot_vector.svg)
  <br>
  [![CodeFactor](https://www.codefactor.io/repository/github/axenodev/hommr/badge?style=for-the-badge)](https://www.codefactor.io/repository/github/axenodev/hommr)
  ![Version](https://img.shields.io/github/v/release/AxenoDev/Hommr?style=for-the-badge)
</div>

**Hommr** is a simple, lightweight, and modern home management plugin for Minecraft servers running Paper. It allows players to set, delete, and teleport to multiple homes with ease.

## ✨ Features

- 📍 **Set Multiple Homes**: Players can set multiple homes to save important locations.
- 🚀 **Easy Teleportation**: Quick teleportation to saved homes.
- 📋 **List Homes**: View a list of all your saved homes with world information.
- 🔐 **Permissions**: Control who can set homes and how many homes they can have.
- 🛠️ **Developer API**: Events and API methods for other plugins to hook into.
- 💻 **Modern Tech**: Built with the latest technologies (Java 21, Paper API).

## 📥 Installation

1. Download the latest release from the [Releases](https://github.com/AxenoDev/Hommr/releases) page.
2. Place the JAR file into your server's `plugins` folder.
3. Restart your server.

## 🎮 Commands & Permissions

| Command | Description | Permission |
|---------|-------------|------------|
| `/home [name]` | Teleport to a specific home (or list if none specified). | `hommr.home.list` (for listing) |
| `/sethome <name>` | Set a new home at your current location. | `hommr.home.set` |
| `/delhome <name>` | Delete an existing home. | `hommr.home.delete` |
| `/home list` | List all your saved homes. | `hommr.home.list` |
| `/home help` | Show the help menu. | - |

## ⚙️ Configuration

The `config.yml` file is automatically generated when you first run the plugin. Currently, it is minimal, but more options will be added in future updates.

## 👨‍💻 Developer API

Hommr provides an API for developers to interact with the plugin. You can listen to events such as `HomeSetEvent`, `HomeDeleteEvent`, and `HomeTeleportEvent`.

### Maven Dependency

To use the API, add the following to your `build.gradle`:

```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/AxenoDev/Hommr")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    compileOnly "me.axeno:hommr:VERSION"
}
```

Replace `VERSION` with the latest version from the releases.

## 📄 License

This project is licensed under the [GNU GENERAL PUBLIC LICENSE v3.0](LICENSE).
