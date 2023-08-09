package net.omegagames.core.bukkit.api.model;

import net.omegagames.core.bukkit.ApiImplementation;
import net.omegagames.core.persistanceapi.beans.credit.CreditBean;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.List;

public class CreditLogMenu extends MenuPagged<CreditBean> {
    private final ApiImplementation api;

    public CreditLogMenu(ApiImplementation api, List<CreditBean> playerLogs) {
        super(9, playerLogs);
        this.api = api;

        super.setSlotNumbersVisible();
        super.setTitle("Liste des transactions !");
        super.setSize(9 * 2);
    }

    @Override
    protected ItemStack convertToItemStack(CreditBean creditBean) {
        return ItemCreator.of(CompMaterial.PAPER,
                creditBean.getTimestamp().toLocalDateTime().toString(),
                " - Date: " + creditBean.getTimestamp().toLocalDateTime().toString(),
                " - Type: " + creditBean.getType(),
                " - Sender: " + creditBean.getSender(),
                " - Receiver: " + creditBean.getReceiver(),
                " - Quantité: " + creditBean.getAmount(),
                " - Raison: " + creditBean.getReason()).make();
    }

    @Override
    protected void onPageClick(Player player, CreditBean creditBean, ClickType clickType) {

    }

    @Override
    public ItemStack getItemAt(int slot) {
        if (super.getItemAt(slot) == null || super.getItemAt(slot).getType() == CompMaterial.AIR.toMaterial()) {
            return ItemCreator.of(CompMaterial.WHITE_STAINED_GLASS_PANE, " ", " ").make();
        }

        return super.getItemAt(slot);
    }

    @Override
    public Button formPreviousButton() {
        return new Button() {
            final boolean canGo = getCurrentPage() > 1;

            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (canGo) {
                    setCurrentPage(MathUtil.range(getCurrentPage() - 1, 1, getPages().size()));
                }
            }

            @Override
            public ItemStack getItem() {
                int previousPage = getCurrentPage() - 1;
                return ItemCreator.of(canGo ? CompMaterial.ARROW : CompMaterial.BARRIER)
                        .name(previousPage == 0 ? "Première page" : "Page précédente")
                        .make();
            }
        };
    }

    @Override
    public Button formNextButton() {
        return new Button() {
            final boolean canGo = getCurrentPage() < getPages().size();

            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (canGo) {
                    setCurrentPage(MathUtil.range(getCurrentPage() + 1, 1, getPages().size()));
                }
            }

            @Override
            public ItemStack getItem() {
                boolean lastPage = getCurrentPage() == getPages().size();
                return ItemCreator.of(canGo ? CompMaterial.ARROW : CompMaterial.BARRIER)
                        .name(lastPage ? "Dernière page" : "Page suivante")
                        .make();
            }
        };
    }
}
