package com.ksensor.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UILabel
import platform.UIKit.UIScrollView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeScrollableContent(modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            UIScrollView().apply {
                repeat(100) { row ->
                    repeat(10) { column ->
                        addSubview(UILabel().apply {
                            text = "R$row C$column"
                            setFrame(CGRectMake(column * 120.0, row * 50.0, 110.0, 40.0))
                        })
                    }
                }
                setContentSize(CGSizeMake(10 * 120.0, 100 * 50.0))
            }
        }
    )
}
