package de.deutschebahn.bahnhoflive.util

import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes


@Throws(InflateException::class)
fun ViewGroup.inflateLayout(@LayoutRes layout: Int) : View
{
  return  LayoutInflater.from(this.context).inflate(layout, this, false)
}

