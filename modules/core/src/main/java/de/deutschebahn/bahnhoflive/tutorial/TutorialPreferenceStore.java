/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.tutorial;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TutorialPreferenceStore {

    public static final String KEY_TUTORIALS_ENABLED = "tutorials_enabled";

    private final SharedPreferences tutorialsStore;
    private final Gson gson = new GsonBuilder().create();
    public static TutorialPreferenceStore instance;

    public TutorialPreferenceStore(Context context) {
        tutorialsStore = context.getSharedPreferences("tutorials.pref", Context.MODE_PRIVATE);
    }

    public static TutorialPreferenceStore getInstance(Context context) {
        if (instance == null) {
            instance = new TutorialPreferenceStore(context);
        }
        return instance;
    }

    /**
     * Updates a single tutorial and stores it in the PreferenceStore.
     *
     * @param tutorial
     */
    public void update(final Tutorial tutorial) {
        final Gson gson = this.gson;
        if(Objects.equals(tutorial.descriptionText, ""))
            tutorialsStore.edit()
                    .remove(tutorial.id)
                    .commit();
        else
        tutorialsStore.edit()
                .putString(tutorial.getId(), gson.toJson(tutorial))
                .commit();
    }

    /**
     * Triggers an update on a list of tutorials
     *
     * @param tutorials
     */
    public void update(final List<Tutorial> tutorials) {
        for (Tutorial tutorial : tutorials) {
            update(tutorial);
        }
    }

    /**
     * Check preferences for value
     *
     * @return boolean, true if user has tutorials enabled
     */
    public boolean doesUserWantToSeeTutorials() {
        return tutorialsStore.getBoolean(KEY_TUTORIALS_ENABLED, !hasSeenAllTutorials());
    }

    /**
     * Checks if user as actively toggled on/off tutorials
     *
     * @param userWantsTutorials true, if user wants to see tutorials
     */
    public void setUserWantsTutorials(boolean userWantsTutorials) {
        tutorialsStore.edit().putBoolean(KEY_TUTORIALS_ENABLED, userWantsTutorials).commit();
    }

    /**
     * Gets a tutorial object from the store and serializes.
     * The identifier must match exactly.
     *
     * @param identifier The identifier of the tutorial
     * @return Tutorial instance or null
     */
    public Tutorial getTutorial(final String identifier) {
        final List<Tutorial> allTutorials = getAll();

        ArrayList<Tutorial> relevantTutorials = new ArrayList<>();
        for (Tutorial tutorial : allTutorials) {
            if (tutorial.getId().equals(identifier)) {
                // Add Tutorials that not have been closed by the User yet
                return tutorial;
            }
        }
        return null;
    }

    /**
     * Gets a list of tutorials for a given identifier.
     *
     * @param identifier The identifier of the tutorial(s)
     * @return A List of Tutorials; might be empty
     */
    public List<Tutorial> getTutorials(final String identifier) {
        final List<Tutorial> allTutorials = getAll();

        ArrayList<Tutorial> relevantTutorials = new ArrayList<>();
        for (Tutorial tutorial : allTutorials) {
            if (tutorial.getId().contains(identifier)
                    && !tutorial.closedByUser) {
                // Add Tutorials that not have been closed by the User yet
                relevantTutorials.add(tutorial);
            }
        }
        return relevantTutorials;
    }

    /**
     * Cleans the PreferenceStore for Turtorials
     * <p>
     * Warning: this also deletes user preference {@link #KEY_TUTORIALS_ENABLED}
     */
    public void cleanStore() {
        tutorialsStore
                .edit()
                .clear()
                .apply();
    }

    /**
     * Gets all tutorial objects from the Store and serializes them into Objects
     *
     * @return A List of Tutorials
     */
    public List<Tutorial> getAll() {
        final Map<String, ?> all = tutorialsStore.getAll();
        final ArrayList<Tutorial> tutorials = new ArrayList<>(all.size());

        for (String key : all.keySet()) {
            if (!KEY_TUTORIALS_ENABLED.equals(key)) {
                final Tutorial station = gson.fromJson(tutorialsStore.getString(key, null), Tutorial.class);
                tutorials.add(station);
            }
        }

        return tutorials;
    }

    public boolean hasSeenAllTutorials() {
        for (Tutorial tutorial : getAll()) {
            if (!tutorial.closedByUser) {
                return false;
            }
        }
        return true;
    }
}
