package org.greenstand.android.TreeTracker.models

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.greenstand.android.TreeTracker.activities.TreeTrackerActivity
import org.greenstand.android.TreeTracker.preferences.PrefKey
import org.greenstand.android.TreeTracker.preferences.PrefKeys
import org.greenstand.android.TreeTracker.preferences.Preferences
import java.util.*

enum class Language(val locale: Locale) {
    ENGLISH(Locale("en")),
    SWAHILI(Locale("sw"));

    companion object {

        fun fromString(lang: String): Language? {
            return when (lang.lowercase()) {
                "en" -> ENGLISH
                "sw" -> SWAHILI
                else -> null
            }
        }
    }
}

class LanguageSwitcher(private val prefs: Preferences) {

    fun applyCurrentLanguage(activity: Activity) {
        val language = Language.fromString(prefs.getString(LANGUAGE_PREF_KEY) ?: "")
        language?.also{ language ->
            setLanguage(language, activity.resources)
        }
    }

    fun switch(activity: Activity) {
        val res = activity.resources
        currentLanguage()?.also{ language ->
            val newLanguage = when (language) {
                Language.ENGLISH -> Language.SWAHILI
                Language.SWAHILI -> Language.ENGLISH
            }
            setLanguage(newLanguage, res)
        }

        activity.finish()
        activity.startActivity(Intent(activity, TreeTrackerActivity::class.java))
    }

    fun setLanguage(language: Language, res: Resources) {
        prefs.edit().putString(LANGUAGE_PREF_KEY, language.locale.toLanguageTag()).commit()

        val config = Configuration(res.configuration).apply {
            Locale.setDefault(language.locale)
            setLocale(language.locale)
        }

        res.updateConfiguration(
            config,
            res.displayMetrics
        )
    }

    fun currentLanguage(): Language? {
        val language = Language.fromString(prefs.getString(LANGUAGE_PREF_KEY) ?: "")
        return language
    }

    fun observeCurrentLanguage(): Flow<Language> {
        return prefs.observeString(LANGUAGE_PREF_KEY)
            .mapNotNull { langString -> langString?.let { Language.fromString(it) } }
    }

    companion object {
        val LANGUAGE_PREF_KEY = PrefKeys.USER_SETTINGS + PrefKey("language")
    }
}
