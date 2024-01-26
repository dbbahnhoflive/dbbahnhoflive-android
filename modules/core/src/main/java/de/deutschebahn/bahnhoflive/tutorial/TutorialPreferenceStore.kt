/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.tutorial

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder

class TutorialPreferenceStore(context: Context) {
    private val tutorialsStore: SharedPreferences
    private val gson = GsonBuilder().create()

    init {
        tutorialsStore = context.getSharedPreferences("tutorials.pref", Context.MODE_PRIVATE)
    }

    /**
     * Updates a single tutorial and stores it in the PreferenceStore.
     *
     * @param tutorial
     */
    fun update(tutorial: Tutorial) {
        val gson = gson
        if (tutorial.descriptionText == "") tutorialsStore.edit()
            .remove(tutorial.id)
            .commit() else tutorialsStore.edit()
            .putString(tutorial.id, gson.toJson(tutorial))
            .commit()
    }

    /**
     * Triggers an update on a list of tutorials
     *
     * @param tutorials
     */
    fun update(tutorials: List<Tutorial>) {
        for (tutorial in tutorials) {
            update(tutorial)
        }
    }

    /**
     * Check preferences for value
     *
     * @return boolean, true if user has tutorials enabled
     */
    fun doesUserWantToSeeTutorials(): Boolean {
        return tutorialsStore.getBoolean(KEY_TUTORIALS_ENABLED, !hasSeenAllTutorials())
    }

    /**
     * Checks if user as actively toggled on/off tutorials
     *
     * @param userWantsTutorials true, if user wants to see tutorials
     */
    fun setUserWantsTutorials(userWantsTutorials: Boolean) {
        tutorialsStore.edit().putBoolean(KEY_TUTORIALS_ENABLED, userWantsTutorials).commit()
    }

    /**
     * Gets a tutorial object from the store and serializes.
     * The identifier must match exactly.
     *
     * @param identifier The identifier of the tutorial
     * @return Tutorial instance or null
     */
    fun getTutorial(identifier: String): Tutorial? {
        val allTutorials = all
//        val relevantTutorials = ArrayList<Tutorial>()
        for (tutorial in allTutorials) {
            if (tutorial.id == identifier) {
                // Add Tutorials that not have been closed by the User yet
                return tutorial
            }
        }
        return null
    }

    /**
     * Gets a list of tutorials for a given identifier.
     *
     * @param identifier The identifier of the tutorial(s)
     * @return A List of Tutorials; might be empty
     */
    fun getTutorials(identifier: String?): List<Tutorial> {
        val allTutorials = all
        val relevantTutorials = ArrayList<Tutorial>()
        for (tutorial in allTutorials) {
            if (tutorial.id.contains(identifier!!)
                && !tutorial.closedByUser
            ) {
                // Add Tutorials that not have been closed by the User yet
                relevantTutorials.add(tutorial)
            }
        }
        return relevantTutorials
    }

    /**
     * Cleans the PreferenceStore for Turtorials
     *
     *
     * Warning: this also deletes user preference [.KEY_TUTORIALS_ENABLED]
     */
    @Suppress("UNUSED")
    fun cleanStore() {
        tutorialsStore
            .edit()
            .clear()
            .apply()
    }

    val all: List<Tutorial>
        /**
         * Gets all tutorial objects from the Store and serializes them into Objects
         *
         * @return A List of Tutorials
         */
        get() {
            val all = tutorialsStore.all
            val tutorials = ArrayList<Tutorial>(all.size)
            for (key in all.keys) {
                if (KEY_TUTORIALS_ENABLED != key) {
                    val station =
                        gson.fromJson(tutorialsStore.getString(key, null), Tutorial::class.java)
                    tutorials.add(station)
                }
            }
            return tutorials
        }

    fun hasSeenAllTutorials(): Boolean {
        for (tutorial in all) {
            if (!tutorial.closedByUser) {
                return false
            }
        }
        return true
    }

    companion object {
        const val KEY_TUTORIALS_ENABLED = "tutorials_enabled"
        var instance: TutorialPreferenceStore? = null
        fun getInstance(context: Context): TutorialPreferenceStore? {
            if (instance == null) {
                instance = TutorialPreferenceStore(context)
            }
            return instance
        }
    }
}
