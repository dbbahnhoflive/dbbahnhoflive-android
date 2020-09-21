/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;

/**
 * Fixes an issue introduced with app version 2.3.0. Be careful when refactoring classes
 * that are persisted by a {@link FavoriteStationsStore}.
 */
class EvaIdsJsonDeserializer implements JsonDeserializer<EvaIds> {
    private Type stringListType = new TypeToken<List<String>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().create();

    @Override
    public EvaIds deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            return gson.fromJson(json, typeOfT);
        }

        final List<String> evaIdList = gson.fromJson(json, stringListType);

        return new EvaIds(evaIdList);
    }
}
