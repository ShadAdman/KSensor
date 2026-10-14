package com.ksensor.sample

import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun NativeScrollableContent(modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val horizontalRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                repeat(30) { index ->
                    addView(TextView(context).apply {
                        text = "Column $index"
                        setPadding(48, 48, 48, 48)
                    })
                }
            }

            val verticalList = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                repeat(100) { index ->
                    addView(TextView(context).apply {
                        text = "Row $index"
                        setPadding(48, 48, 48, 48)
                    })
                }
            }

            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(HorizontalScrollView(context).apply { addView(horizontalRow) })
                addView(
                    ScrollView(context).apply { addView(verticalList) },
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
                )
            }
        }
    )
}
