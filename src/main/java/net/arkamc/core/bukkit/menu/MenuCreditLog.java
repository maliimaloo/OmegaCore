package net.arkamc.core.bukkit.menu;

import net.arkamc.core.bukkit.settings.SettingsCreditLogMenu;
import net.arkamc.core.bukkit.util.model.MenuItemCreator;
import net.arkamc.core.persistanceapi.beans.credit.CreditBean;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.button.Button;

import java.util.ArrayList;
import java.util.List;

public class MenuCreditLog extends MenuPagged<ItemStack> {
    private final String title = SettingsCreditLogMenu.TITLE;
    private final Integer row = SettingsCreditLogMenu.ROW;

    private static final Integer pageSize = SettingsCreditLogMenu.PAGE_SIZE;
    private static final String keyItemLog = "Log_Item";
    private static final String keyPreviousButton = "Previous_Page";
    private static final String keyNextButton = "Next_Page";

    public MenuCreditLog(List<CreditBean> playerLogs) {
        super(pageSize, compileItem(playerLogs));

        super.setTitle(this.title);
        super.setSize(this.row * 9);
        //super.setSlotNumbersVisible();
    }

    @Override
    public Button formPreviousButton() {
        return new Button() {
            final boolean canGo = getCurrentPage() > 1;

            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                if (canGo) {
                    setCurrentPage(MathUtil.range(getCurrentPage() - 1, 1, getPages().size()));
                }
            }

            @Override
            public ItemStack getItem() {
                return MenuItemCreator.fromKey(keyPreviousButton).toItemStack(null);
            }
        };
    }
    @Override
    protected boolean canShowPreviousButton() {
        return true;
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
                return MenuItemCreator.fromKey(keyNextButton).toItemStack(null);
            }
        };
    }
    @Override
    protected boolean canShowNextButton() {
        return true;
    }

    @Override
    protected int getNextButtonPosition() {
        return MenuItemCreator.fromKey(keyNextButton).getSlots().get(0);
    }
    @Override
    protected int getPreviousButtonPosition() {
        return MenuItemCreator.fromKey(keyPreviousButton).getSlots().get(0);
    }

    @Override
    public ItemStack getItemAt(int slot) {
        for (MenuItemCreator itemContent : SettingsCreditLogMenu.Content.getCache().values()) {
            if (itemContent.getSlots().contains(slot)) {
                return itemContent.toItemStack(null);
            }
        }

        return super.getItemAt(slot);
    }

    @Override
    protected ItemStack convertToItemStack(ItemStack itemStack) {
        return itemStack;
    }

    @Override
    protected void onPageClick(Player player, ItemStack itemStack, ClickType clickType) {
    }

    private static List<ItemStack> compileItem(List<CreditBean> playerLogs) {
        final List<ItemStack> list = new ArrayList<>();
        final MenuItemCreator logItemCreator = MenuItemCreator.fromKey(keyItemLog);
        if (logItemCreator == null) {
            return list;
        }

        for (CreditBean playerLog : playerLogs) {
            SerializedMap replaceHolder = replaceVariables(playerLog);
            list.add(logItemCreator.toItemStack(replaceHolder));
        }

        return list;
    }

    private static SerializedMap replaceVariables(CreditBean playerLog) {
        return SerializedMap.ofArray(
                "{credit_log_date}", playerLog.getTimestampToDate(),
                "{credit_log_type}", playerLog.getType(),
                "{credit_log_sender}", playerLog.getSender(),
                "{credit_log_receiver}", playerLog.getReceiver(),
                "{credit_log_amount}", playerLog.getAmount(),
                "{credit_log_reason}", playerLog.getReason()
        );
    }
}
