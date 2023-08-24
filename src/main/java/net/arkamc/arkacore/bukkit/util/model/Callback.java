package net.arkamc.arkacore.bukkit.util.model;

public interface Callback<T> {
    void onSuccess(T var1);

    default void onFailure(Throwable cause) {
    }
}
