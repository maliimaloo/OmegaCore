package net.arkamc.arkacore.bukkit.util.model;

public interface IFinancialCallback
{
    /**
     * Déclenché après une opération financière
     *
     * @param newAmount Nouveau montant d'argent
     * @param amount Montant d'argent avant l'opération
     * @param error Erreur {@link Throwable} si l'opération a échoué
     */
    void done(long newAmount, long amount, Throwable error);
}