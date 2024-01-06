package dev.khbd.result4j.javac;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JavacMessages;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

/**
 * @author Sergei_Khadanovich
 */
@UtilityClass
public class BundleInitializer {

    private static final List<String> BUNDLES = List.of(
            DefaultResult4jBundle.class.getName()
    );

    /**
     * Init plugin bundles.
     */
    static void initPluginBundles(Context context) {
        JavacMessages messages = JavacMessages.instance(context);

        for (String name : BUNDLES) {
            ResourceBundle bundle = ResourceBundle.getBundle(name);
            messages.add(l -> bundle);
        }
    }

    public static class DefaultResult4jBundle extends ListResourceBundle {

        @Override
        protected Object[][] getContents() {
            return new Object[][]{
                    {"compiler.err.unwrap.call.at.unsupported.position", "Unsupported position for unwrap method call"}
            };
        }
    }
}
