package net.omegagames.core.bukkit.api.util.model;

public interface Callback<T> {
    void onSuccess(T var1);

    default void onFailure(Throwable cause) {
    }
}
