package net.omegagames.core.bukkit.api.util;

import net.omegagames.core.bukkit.api.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.JavaScriptExecutor;
import org.mineacademy.fo.model.Replacer;
import org.mineacademy.fo.settings.SimpleLocalization;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class LangUtils {
    public static String of(String message, Object... variables) {
        return of(message, null, variables);
    }

    public static String of(String message, @Nullable SerializedMap serializedMap, Object... variables) {
        if (serializedMap != null && !serializedMap.isEmpty()) {
            message = Replacer.replaceArray(message, serializedMap);
        }
        message = Messenger.replacePrefixes(message);
        message = translate(message, variables);
        return message;
    }

    public static List<String> of(List<String> list, Object... variables) {
        return of(list, null, variables);
    }

    public static List<String> of(List<String> list, @Nullable SerializedMap serializedMap, Object... variables) {
        List<String> finalListMessage = new ArrayList<>();
        for (String paramMessage : list) {
            if (serializedMap == null ||serializedMap.isEmpty()) {
                paramMessage = LangUtils.of(paramMessage, variables);
            } else {
                paramMessage = LangUtils.of(paramMessage, serializedMap, variables);
            }

            finalListMessage.add(paramMessage);
        }

        return finalListMessage;
    }

    public static String ofScript(String message, SerializedMap scriptVariables, Object... stringVariables) {
        String script = LangUtils.of(message, stringVariables);
        if (!script.contains("?") && !script.contains(":") && !script.contains("+") && !script.startsWith("'") && !script.endsWith("'")) {
            script = "'" + script + "'";
        }

        Object result;
        try {
            result = JavaScriptExecutor.run(script, scriptVariables.asMap());
        } catch (Throwable var6) {
            if (Settings.LOCALE_PREFIX.equals("en")) {
                Common.error(var6, "Failed to compile localization key!", "It must be a valid JavaScript code, if you modified it, check the syntax!", "Variables: " + scriptVariables, "String variables: " + Common.join(stringVariables), "Script: " + script, "Error: %error%");
            } else {
                Common.error(var6, "Échec de la compilation de la clé de localization !", "Il doit s'agir d'un code JavaScript valide. Si vous l'avez modifié, vérifier la syntaxe !", "Variables: " + scriptVariables, "String variables: " + Common.join(stringVariables), "Script: " + script, "Erreur: %error%");
            }
            return script;
        }

        return result.toString();
    }

    public static List<String> ofScriptList(List<String> paramList, SerializedMap paramSerializedMap, Object... stringVariables) {
        final List<String> finalList = new ArrayList<>();

        for (String paramString : paramList) {
            try {
                paramString = ofScript(paramString, paramSerializedMap, stringVariables);
            } catch (NullPointerException ignored) {
            }

            finalList.add(paramString);
        }

        return finalList;
    }

    private static String translate(String key, Object... variables) {
        Valid.checkNotNull(key, "Impossible de translate une clef null avec les variables: " + Common.join(variables));
        if (variables != null) {
            for(int i = 0; i < variables.length; ++i) {
                Object variable = variables[i];
                variable = Common.getOrDefaultStrict(SerializeUtil.serialize(SerializeUtil.Mode.YAML, variable), SimpleLocalization.NONE);
                Valid.checkNotNull(variable, "Échec de remplacement {" + i + "} à " + variable + " (raw = " + variables[i] + ")");
                key = key.replace("{" + i + "}", variable.toString());
            }
        }

        return key;
    }
}
