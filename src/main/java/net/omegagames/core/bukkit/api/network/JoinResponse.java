package net.omegagames.core.bukkit.api.network;

public class JoinResponse {
    private String reason;
    private ResponseType responseType;

    /**
     * Constructeur
     */
    public JoinResponse() {
        this.reason = null;
        this.responseType = ResponseType.ALLOW;
    }

    /**
     * Autorise la demande de connexion
     */
    public void allow() {
        this.responseType = ResponseType.ALLOW;
    }

    /**
     * Refuse la demande de connexion avec une raison donnée
     *
     * @param reason Raison
     */
    public void disallow(String reason) {
        this.responseType = ResponseType.DENY_OTHER;
        this.reason = reason;
    }

    /**
     * Refuse la demande de connexion avec un type de réponse donné {@link ResponseType}
     *
     * @param responseType Type de réponse
     */
    public void disallow(ResponseType responseType) {
        this.responseType = responseType;
        this.reason = responseType.getMessage();
    }

    /**
     * Obtient le type de réponse utilisé
     *
     * @return Type de réponse
     */
    public ResponseType getResponseType() {
        return this.responseType;
    }

    /**
     * Indique si la connexion est autorisée
     *
     * @return {@code true} si autorisée
     */
    public boolean isAllowed() {
        return this.responseType == ResponseType.ALLOW;
    }

    /**
     * Obtient la raison du refus de la demande de connexion
     *
     * @return Raison
     */
    public String getReason() {
        return this.reason;
    }
}
