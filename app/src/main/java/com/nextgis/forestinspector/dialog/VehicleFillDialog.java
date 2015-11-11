/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
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

package com.nextgis.forestinspector.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.VehicleActivity;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.LocationUtil;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import com.nextgis.styled_dialog.StyledDialogFragment;

import java.text.DecimalFormat;

import static com.nextgis.maplib.util.GeoConstants.GTMultiPoint;


public class VehicleFillDialog
        extends StyledDialogFragment
        implements GpsEventListener
{
    public static final String UNKNOWN_LOCATION = "-";

    protected Feature  mFeature;
    protected Location mFeatureLocation;

    protected String mName;
    protected String mDesc;
    protected String mNums;
    protected String mUser;

    protected EditText mNameView;
    protected EditText mDescView;
    protected EditText mNumsView;
    protected EditText mUserView;

    protected TextView mLatView;
    protected TextView mLongView;
    protected TextView mAltView;
    protected TextView mAccView;
    protected Location mLocation;

    protected GpsEventSource gpsEventSource;

    protected OnAddVehicleListener mOnAddVehicleListener;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        super.onCreate(savedInstanceState);


        IGISApplication app = (IGISApplication) getActivity().getApplication();
        gpsEventSource = app.getGpsEventSource();

        if (null != mFeature) {
            // TODO: make for another types
            GeoGeometry geometry = mFeature.getGeometry();
            switch (geometry.getType()) {
                case GTMultiPoint: {
                    GeoMultiPoint mpt = (GeoMultiPoint) geometry;
                    GeoPoint pt = new GeoPoint(mpt.get(0));
                    pt.setCRS(GeoConstants.CRS_WEB_MERCATOR);
                    pt.project(GeoConstants.CRS_WGS84);
                    mFeatureLocation = new Location("");
                    mFeatureLocation.setLatitude(pt.getY());
                    mFeatureLocation.setLongitude(pt.getX());
                    break;
                }
                default: {
                    mFeatureLocation = new Location(VehicleFillDialog.UNKNOWN_LOCATION);
                    break;
                }
            }

            mName = mFeature.getFieldValueAsString(Constants.FIELD_VEHICLE_NAME);
            mDesc = mFeature.getFieldValueAsString(Constants.FIELD_VEHICLE_DESCRIPTION);
            mNums = mFeature.getFieldValueAsString(Constants.FIELD_VEHICLE_ENGINE_NUM);
            mUser = mFeature.getFieldValueAsString(Constants.FIELD_VEHICLE_USER);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View view = View.inflate(getActivity(), R.layout.dialog_vehicle_fill, null);

        createLocationPanelView(view);

        mNameView = (EditText) view.findViewById(R.id.name);
        if (null != mName) {
            mNameView.setText(mName);
            mName = null;
        }

        mDescView = (EditText) view.findViewById(R.id.desc);
        if (null != mDesc) {
            mDescView.setText(mDesc);
            mDesc = null;
        }

        mNumsView = (EditText) view.findViewById(R.id.nums);
        if (null != mNums) {
            mNumsView.setText(mNums);
            mNums = null;
        }

        mUserView = (EditText) view.findViewById(R.id.user);
        if (null != mUser) {
            mUserView.setText(mUser);
            mUser = null;
        }


        setThemeDark(isAppThemeDark());

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_image_edit);
        } else {
            setIcon(R.drawable.ic_action_image_edit);
        }

        setView(view);

        if (null != mFeature) {
            setTitle(R.string.change_data);
            setPositiveText(R.string.save);
        } else {
            setTitle(R.string.add_vehicle);
            setPositiveText(R.string.add);
        }

        setNegativeText(R.string.cancel);

        setOnPositiveClickedListener(
                new OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        addVehicle();

                        if (null != mOnAddVehicleListener) {
                            mOnAddVehicleListener.onAddVehicle();
                        }
                    }
                });

        setOnNegativeClickedListener(
                new OnNegativeClickedListener()
                {
                    @Override
                    public void onNegativeClicked()
                    {
                        // cancel
                    }
                });

        return super.onCreateDialog(savedInstanceState);
    }


    @Override
    public void onPause()
    {
        gpsEventSource.removeListener(this);
        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        gpsEventSource.addListener(this);
    }


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
    }


    protected void createLocationPanelView(View view)
    {
        mLatView = (TextView) view.findViewById(com.nextgis.maplibui.R.id.latitude_view);
        mLongView = (TextView) view.findViewById(com.nextgis.maplibui.R.id.longitude_view);
        mAltView = (TextView) view.findViewById(com.nextgis.maplibui.R.id.altitude_view);
        mAccView = (TextView) view.findViewById(com.nextgis.maplibui.R.id.accuracy_view);

        FrameLayout accLocPanel =
                (FrameLayout) view.findViewById(com.nextgis.maplibui.R.id.accurate_location_panel);
        accLocPanel.setVisibility(View.GONE);

        final ImageButton refreshLocation =
                (ImageButton) view.findViewById(com.nextgis.maplibui.R.id.refresh);

        refreshLocation.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (null != mFeatureLocation) {
                            mFeatureLocation = null;
                        }

                        RotateAnimation rotateAnimation = new RotateAnimation(
                                0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        rotateAnimation.setDuration(700);
                        rotateAnimation.setRepeatCount(0);
                        refreshLocation.startAnimation(rotateAnimation);

                        Location location = gpsEventSource.getLastKnownLocation();
                        setLocationText(location);
                    }
                });


        if (null != mFeatureLocation) {
            if (mFeatureLocation.getProvider().equals(UNKNOWN_LOCATION)) {
                setLocationText(null);
            } else {
                setLocationText(mFeatureLocation);
            }

        } else {
            setLocationText(gpsEventSource.getLastKnownLocation());
        }
    }


    protected void setLocationText(Location location)
    {
        if (null == mLatView || null == mLongView || null == mAccView || null == mAltView) {
            return;
        }

        if (null == location) {

            mLatView.setText(
                    getString(com.nextgis.maplibui.R.string.latitude_caption_short) + ": " +
                    getString(com.nextgis.maplibui.R.string.n_a));
            mLongView.setText(
                    getString(com.nextgis.maplibui.R.string.longitude_caption_short) + ": " +
                    getString(com.nextgis.maplibui.R.string.n_a));
            mAltView.setText(
                    getString(com.nextgis.maplibui.R.string.altitude_caption_short) + ": " +
                    getString(com.nextgis.maplibui.R.string.n_a));
            mAccView.setText(
                    getString(com.nextgis.maplibui.R.string.accuracy_caption_short) + ": " +
                    getString(com.nextgis.maplibui.R.string.n_a));

            return;
        }

        mLocation = location;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        int nFormat = prefs.getInt(
                SettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int", Location.FORMAT_SECONDS);
        DecimalFormat df = new DecimalFormat("0.0");

        mLatView.setText(
                getString(com.nextgis.maplibui.R.string.latitude_caption_short) + ": " +
                LocationUtil.formatLatitude(location.getLatitude(), nFormat, getResources()));

        mLongView.setText(
                getString(com.nextgis.maplibui.R.string.longitude_caption_short) + ": " +
                LocationUtil.formatLongitude(location.getLongitude(), nFormat, getResources()));

        double altitude = location.getAltitude();
        mAltView.setText(
                getString(com.nextgis.maplibui.R.string.altitude_caption_short) + ": " +
                df.format(altitude) + " " +
                getString(com.nextgis.maplibui.R.string.unit_meter));

        float accuracy = location.getAccuracy();
        mAccView.setText(
                getString(com.nextgis.maplibui.R.string.accuracy_caption_short) + ": " +
                df.format(accuracy) + " " +
                getString(com.nextgis.maplibui.R.string.unit_meter));
    }


    @Override
    public void onLocationChanged(Location location)
    {

    }


    @Override
    public void onBestLocationChanged(Location location)
    {

    }


    @Override
    public void onGpsStatusChanged(int event)
    {

    }


    public void setFeature(Feature feature)
    {
        mFeature = feature;
    }


    public void addVehicle()
    {
        if (null == mLocation) {
            Toast.makeText(getActivity(), getString(R.string.error_no_location), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        VehicleActivity activity = (VehicleActivity) getActivity();
        if (null == activity) {
            return;
        }

        MapBase map = MapBase.getInstance();
        DocumentsLayer documentsLayer =
                (DocumentsLayer) map.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);
        if (null == documentsLayer) {
            return;
        }

        VectorLayer vehicleLayer =
                (VectorLayer) documentsLayer.getLayerByName(Constants.KEY_LAYER_VEHICLES);
        if (null == vehicleLayer) {
            return;
        }


        GeoPoint pt = new GeoPoint(mLocation.getLongitude(), mLocation.getLatitude());
        pt.setCRS(GeoConstants.CRS_WGS84);
        pt.project(GeoConstants.CRS_WEB_MERCATOR);
        GeoMultiPoint geometryValue = new GeoMultiPoint();
        geometryValue.add(pt);

        Feature feature;
        if (null != mFeature) {
            feature = mFeature;
        } else {
            feature = new Feature(
                    com.nextgis.maplib.util.Constants.NOT_FOUND, vehicleLayer.getFields());
            activity.getFeature().addSubFeature(Constants.KEY_LAYER_VEHICLES, feature);
        }

        feature.setFieldValue(
                Constants.FIELD_VEHICLE_NAME, mNameView.getText().toString());
        feature.setFieldValue(
                Constants.FIELD_VEHICLE_DESCRIPTION, mDescView.getText().toString());
        feature.setFieldValue(
                Constants.FIELD_VEHICLE_ENGINE_NUM, mNumsView.getText().toString());
        feature.setFieldValue(
                Constants.FIELD_VEHICLE_USER, mUserView.getText().toString());
        feature.setGeometry(geometryValue);
    }


    public void setOnAddVehicleListener(OnAddVehicleListener listener)
    {
        mOnAddVehicleListener = listener;
    }


    public interface OnAddVehicleListener
    {
        void onAddVehicle();
    }
}
