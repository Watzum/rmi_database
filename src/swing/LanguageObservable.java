package swing;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LanguageObservable {

    private final String resource = "swing.languages.transcription";
    private ResourceBundle bundle;

    private SwingPropertyChangeSupport support;

    public LanguageObservable() {
        support = new SwingPropertyChangeSupport(this);
        Locale locale = Locale.getDefault();
        bundle = ResourceBundle.getBundle(resource, locale);
    }


    public LanguageObservable(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle(resource, locale);
        } catch (MissingResourceException e) {
            locale = new Locale("en");
            bundle = ResourceBundle.getBundle(resource, locale);
        }
    }


    public String getText(String key) {
        return bundle.getString(key);
    }


    public void setNewLanguage(Locale locale) {
        bundle = ResourceBundle.getBundle(resource, locale);
        support.firePropertyChange("newLang", "", locale.getLanguage());
    }


    public void addPropertyChangeListener(PropertyChangeListener p) {
        support.addPropertyChangeListener(p);
    }


    public void removePropertyChangeListener(PropertyChangeListener p) {
        support.removePropertyChangeListener(p);
    }
}