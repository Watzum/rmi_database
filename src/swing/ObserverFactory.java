package swing;

import javax.swing.*;

public class ObserverFactory {

    public static JMenu MakeMenu(String key, LanguageObservable observable)  {
        JMenu menu = new JMenu();
        changeAbstractButtonToObserver(menu, key, observable);
        return menu;
    }

    public static JMenuItem MakeMenuItem(String key, LanguageObservable observable) {
        JMenuItem item = new JMenuItem();
        changeAbstractButtonToObserver(item, key, observable);
        return item;
    }

    private static void changeAbstractButtonToObserver(AbstractButton b, String key,
                                                LanguageObservable lo) {
        b.setName(key);
        b.setText(lo.getText(key));
        b.addPropertyChangeListener("newLang", evt -> {
            b.setText(lo.getText(b.getName()));
        });
        lo.addPropertyChangeListener(
                b.getPropertyChangeListeners("newLang")[0]);
        b.setHideActionText(true);
    }
}
