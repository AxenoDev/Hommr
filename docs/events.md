# Custom Events

Hommr fires several custom events that you can listen to in your plugin.

## HomeSetEvent

Fired when a player sets a new home or updates an existing one.

*   **Cancellable:** Yes
*   **Package:** `me.axeno.hommr.events`

**Methods:**

*   `getPlayer()`: The player setting the home.
*   `getHomeName()`: The name of the home.
*   `getHome()`: The `Home` object.
*   `isUpdate()`: `true` if updating an existing home, `false` if creating a new one.

```java
@EventHandler
public void onHomeSet(HomeSetEvent event) {
    if (event.getHomeName().equalsIgnoreCase("restricted")) {
        event.getPlayer().sendMessage("You cannot use this home name!");
        event.setCancelled(true);
    }
}
```

## HomeDeleteEvent

Fired when a player deletes a home.

*   **Cancellable:** Yes
*   **Package:** `me.axeno.hommr.events`

**Methods:**

*   `getPlayer()`: The player deleting the home.
*   `getHomeName()`: The name of the home being deleted.
*   `getHome()`: The `Home` object.

```java
@EventHandler
public void onHomeDelete(HomeDeleteEvent event) {
    if (event.getHome().getWorld().getName().equals("lobby")) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("You cannot delete homes in the lobby!");
    }
}
```

## HomeTeleportEvent

Fired when a player teleports to a home.

*   **Cancellable:** Yes
*   **Package:** `me.axeno.hommr.events`

**Methods:**

*   `getPlayer()`: The player teleporting.
*   `getHomeName()`: The name of the destination home.
*   `getHome()`: The `Home` object.

```java
@EventHandler
public void onHomeTeleport(HomeTeleportEvent event) {
    if (event.getPlayer().getHealth() < 5) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("You are too weak to teleport!");
    }
}
```
