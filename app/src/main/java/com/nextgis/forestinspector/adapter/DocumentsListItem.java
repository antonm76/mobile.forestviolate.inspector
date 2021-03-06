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

package com.nextgis.forestinspector.adapter;

import android.support.annotation.NonNull;

import java.util.Date;


public class DocumentsListItem
        implements Comparable<DocumentsListItem>
{
    public String mTypeName;
    public String mDesc;
    public Date   mDate;
    public int    mType;
    public int    mStatus;
    public long   mId;
    public long   mUserId;
    public boolean mIsSigned = false;


    @Override
    public int compareTo(
            @NonNull
            DocumentsListItem another)
    {
        return (-1) * mDate.compareTo(another.mDate);
    }
}
