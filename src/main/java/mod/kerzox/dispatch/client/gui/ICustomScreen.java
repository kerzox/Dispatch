package mod.kerzox.dispatch.client.gui;

import mod.kerzox.dispatch.client.menu.CableMenu;

public interface ICustomScreen {
    int getGuiLeft();
    int getGuiTop() ;
    CableMenu getMenu();
}
