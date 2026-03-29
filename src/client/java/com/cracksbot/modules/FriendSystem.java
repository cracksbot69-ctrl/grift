package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class FriendSystem extends HackModule {
    public static final Set<String> friends = new HashSet<>();

    public FriendSystem() {
        super("Friends", "Friend system (no attack)", ModuleCategory.MISC, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    public static boolean isFriend(String name) {
        return friends.contains(name.toLowerCase());
    }

    public static void addFriend(String name) {
        friends.add(name.toLowerCase());
    }

    public static void removeFriend(String name) {
        friends.remove(name.toLowerCase());
    }

    @Override
    public void onTick(Minecraft mc) {}
}
