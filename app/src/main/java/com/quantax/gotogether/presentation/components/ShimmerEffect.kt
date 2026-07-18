package com.quantax.gotogether.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    baseColor: Color? = null,
    highlightColor: Color? = null
) {
    val fallback = MaterialTheme.colorScheme.surfaceVariant
    val shimmerColors = listOf(
        (baseColor ?: fallback).copy(alpha = 0.6f),
        (highlightColor ?: MaterialTheme.colorScheme.surfaceContainerHighest).copy(alpha = 0.4f),
        (baseColor ?: fallback).copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun TripCardShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(bottom = 12.dp)
    ) {
        // Hero Image Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(10.dp)
        ) {
            ShimmerAnimation(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            )
            
            // Host Badge Shimmer
            ShimmerAnimation(
                modifier = Modifier
                    .padding(20.dp)
                    .width(120.dp)
                    .height(36.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    ShimmerAnimation(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(24.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Location & Date line
                    ShimmerAnimation(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
                
                // Price Tag
                ShimmerAnimation(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // description lines
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShimmerAnimation(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        ShimmerAnimation(modifier = Modifier.width(60.dp).height(12.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerAnimation(modifier = Modifier.width(40.dp).height(10.dp))
                    }
                }

                ShimmerAnimation(
                    modifier = Modifier
                        .width(100.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}

@Composable
fun NotificationShimmerItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerAnimation(
            modifier = Modifier.size(52.dp),
            shape = CircleShape
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
            )
        }
    }
}

@Composable
fun MessageShimmerItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerAnimation(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(18.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
            )
        }
    }
}

@Composable
fun TripDetailShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Hero Image Card Shimmer
        ShimmerAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title and Pricing Row Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            ShimmerAnimation(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(32.dp))
            ShimmerAnimation(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Location Info Shimmer
        ShimmerAnimation(
            modifier = Modifier
                .width(160.dp)
                .height(18.dp),
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Central Status Card Shimmer
        ShimmerAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Description Shimmer
        ShimmerAnimation(
            modifier = Modifier
                .width(120.dp)
                .height(24.dp),
            shape = RoundedCornerShape(4.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerAnimation(modifier = Modifier.fillMaxWidth().height(14.dp))
            ShimmerAnimation(modifier = Modifier.fillMaxWidth().height(14.dp))
            ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Booking Button Shimmer
        ShimmerAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
