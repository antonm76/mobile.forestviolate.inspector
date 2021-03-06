/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (c) 2015-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.forestinspector.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.IDocumentFeatureSource;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class VehicleViewFragment
        extends TabFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_vehicles_view, container, false);

        Activity activity = getActivity();
        if (activity instanceof IDocumentFeatureSource) {
            IDocumentFeatureSource documentFeatureSource = (IDocumentFeatureSource) activity;
            DocumentFeature feature = documentFeatureSource.getFeature();

            if (null != feature) {
                //author
                TextView author = (TextView) view.findViewById(R.id.author);
                author.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER));

                //create_datetime
                TextView createDateTime = (TextView) view.findViewById(R.id.creation_datetime);
                createDateTime.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE));

                //place
                TextView place = (TextView) view.findViewById(R.id.creation_place);
                place.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_PLACE));

                //intro
                Date date = new Date((Long) feature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE));
                String sDate = DateFormat.getDateInstance().format(date);
                String sIndictmentNum =
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_NUMBER);

                String parentDoc =
                        getString(R.string.found_logging) + " (" + getString(R.string.indictment) +
                                " " + getString(R.string.number) + " " + sIndictmentNum + " " +
                                getString(R.string.on) + " " + sDate + ")";

                TextView intro = (TextView) view.findViewById(R.id.intro);
                intro.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER_PICK) +
                                " " +
                                parentDoc);

                //vehicle
                List<Feature> vehicleItems = feature.getSubFeatures(Constants.KEY_LAYER_VEHICLES);

                if (null != vehicleItems) {
                    String vehiclesDesc = "";
                    int counter = 0;
                    for (Feature item : vehicleItems) {
                        vehiclesDesc += ++counter + ". ";
                        vehiclesDesc +=
                                item.getFieldValueAsString(Constants.FIELD_VEHICLE_NAME) + ", ";
                        vehiclesDesc +=
                                item.getFieldValueAsString(Constants.FIELD_VEHICLE_DESCRIPTION)
                                        + ", ";
                        vehiclesDesc +=
                                item.getFieldValueAsString(Constants.FIELD_VEHICLE_ENGINE_NUM)
                                        + " (";
                        vehiclesDesc += getString(R.string.owner_details) + ": ";
                        vehiclesDesc +=
                                item.getFieldValueAsString(Constants.FIELD_VEHICLE_USER) + ")\n";
                    }

                    if (TextUtils.isEmpty(vehiclesDesc)) {
                        vehiclesDesc = getString(R.string.n_a);
                    }

                    TextView vehicle = (TextView) view.findViewById(R.id.vehicle);
                    vehicle.setText(getString(R.string.vehicles_found) + ":\n" + vehiclesDesc);
                }

                //possible_crime
                TextView possibleCrime = (TextView) view.findViewById(R.id.possible_crime);
                possibleCrime.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_CRIME));

                //officer
                TextView officer = (TextView) view.findViewById(R.id.officer);
                officer.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER_TRANS));

                //inspector
                TextView inspector = (TextView) view.findViewById(R.id.inspector);
                inspector.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_AUTHOR));

                //civil
                TextView civil = (TextView) view.findViewById(R.id.civil);
                civil.setText(
                        feature.getFieldValueAsString(
                                Constants.FIELD_DOCUMENTS_DESC_DETECTOR));

                //description
                TextView description = (TextView) view.findViewById(R.id.description);
                description.setText(
                        feature.getFieldValueAsString(
                                Constants.FIELD_DOCUMENTS_DESCRIPTION));
            }
        }
        return view;
    }
}
