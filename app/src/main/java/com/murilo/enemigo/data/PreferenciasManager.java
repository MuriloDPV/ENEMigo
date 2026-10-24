package com.murilo.enemigo.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.murilo.enemigo.model.Avatar;
import com.murilo.enemigo.util.Constantes;

/**
 * Preferências do aluno que não são progresso: som, vibração e o avatar.
 * Ficam no mesmo SharedPreferences do ProgressoManager, então o "resetar progresso" (RN34)
 * também volta tudo ao padrão. Singleton: use getInstance(context).
 */
public final class PreferenciasManager {

    private static PreferenciasManager instancia;

    private final SharedPreferences prefs;

    private PreferenciasManager(Context context) {
        prefs = context.getSharedPreferences(Constantes.PREFS_NOME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferenciasManager getInstance(Context context) {
        if (instancia == null) {
            instancia = new PreferenciasManager(context.getApplicationContext());
        }
        return instancia;
    }

    /** Sons de acerto, erro e lição concluída. Padrão: ligado. */
    public boolean isSomAtivo() {
        return prefs.getBoolean(Constantes.CHAVE_SOM, true);
    }

    public void setSomAtivo(boolean ativo) {
        prefs.edit().putBoolean(Constantes.CHAVE_SOM, ativo).apply();
    }

    /** Vibração curta ao errar (C10). Padrão: ligada. */
    public boolean isVibracaoAtiva() {
        return prefs.getBoolean(Constantes.CHAVE_VIBRACAO, true);
    }

    public void setVibracaoAtiva(boolean ativa) {
        prefs.edit().putBoolean(Constantes.CHAVE_VIBRACAO, ativa).apply();
    }

    /** O avatar montado; sem nenhum salvo (pulou no primeiro acesso), o padrão. */
    public Avatar getAvatar() {
        return Avatar.deTexto(prefs.getString(Constantes.CHAVE_AVATAR, null));
    }

    public void setAvatar(Avatar avatar) {
        prefs.edit().putString(Constantes.CHAVE_AVATAR, avatar.paraTexto()).apply();
    }
}
