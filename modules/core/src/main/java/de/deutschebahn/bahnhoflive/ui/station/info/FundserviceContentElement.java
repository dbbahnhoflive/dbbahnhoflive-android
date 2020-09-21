/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.method.MovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;
import de.deutschebahn.bahnhoflive.ui.MBTextView;

public class FundserviceContentElement implements Comparable<FundserviceContentElement> {
    private int position;
    private String type;
    private String value;
    private String title;
    private String action;

    public static FundserviceContentElement fromJSON(JSONObject element) {
        FundserviceContentElement result = new FundserviceContentElement();
        try {
            result.setPosition(element.getInt("position"));
            result.setType(element.getString("type"));
            result.setValue(element.getString("value"));
            if (!element.isNull("title")) {
                result.setTitle(element.getString("title"));
            }
            if (!element.isNull("action")) {
                result.setAction(element.getString("action"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static List<FundserviceContentElement> fromJSON(JSONArray definition) {
        List<FundserviceContentElement> result = new ArrayList<>();
        if (definition != null) {
            for (int i = 0; i < definition.length(); i++) {
                try {
                    FundserviceContentElement element =
                            FundserviceContentElement.fromJSON(definition.getJSONObject(i));
                    if (element != null) {
                        result.add(element);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    public static void render(LinearLayout serviceContainer, ServiceContent serviceContent, final Context context, MovementMethod movementMethod) {
        try {
            if (serviceContainer.getChildCount() > 0) {
                return;
            }

            List<FundserviceContentElement> elements =
                    fromJSON(new JSONArray(serviceContent.getDescriptionText()));
            final Resources resources = context.getResources();

            for (FundserviceContentElement element : elements) {
                if ("subline".equals(element.getType())) {
                    TextView subline = new TextView(context);
                    subline.setText(Html.fromHtml(element.getValue()));
                    subline.setTextColor(resources.getColor(R.color.textcolor_default));
                    subline.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.textsize_24));
                    subline.setPadding(
                            resources.getDimensionPixelSize(R.dimen.default_space),
                            resources.getDimensionPixelSize(R.dimen.standardPaddingV),
                            resources.getDimensionPixelSize(R.dimen.default_space),
                            resources.getDimensionPixelSize(R.dimen.standardPaddingV));
                    serviceContainer.addView(subline);
                }
                if ("text".equals(element.getType())) {
                    MBTextView text = new MBTextView(context);
                    text.setText(Html.fromHtml(element.getValue()));
                    text.setTextColor(resources.getColor(R.color.textcolor_default));
                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.textsize_24));
                    text.setAutoLinkMask(Linkify.ALL);
                    if (movementMethod != null) {
                        text.setMovementMethod(movementMethod);
                    }
                    text.setLinkTextColor(resources.getColor(R.color.textcolor_light));
                    text.setPadding(
                            resources.getDimensionPixelSize(R.dimen.default_space),
                            resources.getDimensionPixelSize(R.dimen.standardPaddingV),
                            resources.getDimensionPixelSize(R.dimen.standardPaddingH),
                            resources.getDimensionPixelSize(R.dimen.standardPaddingV));
                    serviceContainer.addView(text);
                }
                if ("button".equals(element.getType())) {
                    MBTextView button = new MBTextView(context);
                    button.setText(element.getTitle());
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.textsize_28));
                    button.setBackgroundResource(R.drawable.legacy_redbutton_rectangle);

                    final String action = element.getAction();
                    final String value = element.getValue();
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ("url".equals(action)) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(value));
                                context.startActivity(intent);
                            }
                        }
                    });
                    button.setTextColor(Color.WHITE);
                    button.setText(element.getTitle());
                    button.setGravity(Gravity.CENTER);

                    serviceContainer.addView(button);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
                    params.height = resources.getDimensionPixelSize(R.dimen.home_actionbuttonheight);
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.default_space);
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.standardPaddingH);
                    params.topMargin = resources.getDimensionPixelSize(R.dimen.standardPaddingV);
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.standardPaddingV);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(FundserviceContentElement another) {
        return this.getPosition() - another.getPosition();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
