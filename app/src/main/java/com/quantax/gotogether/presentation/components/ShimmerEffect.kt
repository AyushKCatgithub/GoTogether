package com.quantax.gotogether.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    baseColor: Color = Color.Gray.copy(alpha = 0.2f),
    highlightColor: Color = Color.White.copy(alpha = 0.5f)
) {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    start = Offset(10f, 10f),
                    end = Offset(translateAnim.value, translateAnim.value)
                )
                drawRect(brush = brush)
            }
    )
}

@Composable
fun TripCardShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Gray.copy(alpha = 0.1f))
            .padding(bottom = 16.dp)
    ) {
        ShimmerAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerAnimation(
                    modifier = Modifier
                        .width(100.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(50)
                )
                ShimmerAnimation(
                    modifier = Modifier
                        .width(100.dp)
                        .height(36.dp),
                    shape = RoundedCornerShape(50)
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
            .background(Color.Gray.copy(alpha = 0.1f))
            .padding(14.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        ShimmerAnimation(
            modifier = Modifier.size(48.dp),
            shape = androidx.compose.foundation.shape.CircleShape
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerAnimation(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShimmerAnimation(
                modifier = Modifier.size(38.dp),
                shape = androidx.compose.foundation.shape.CircleShape
            )
            ShimmerAnimation(
                modifier = Modifier.size(38.dp),
                shape = androidx.compose.foundation.shape.CircleShape
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
            .background(Color.Gray.copy(alpha = 0.1f))
            .padding(14.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        ShimmerAnimation(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(14.dp)
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
fun TripDetailShimmer() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero Image Shimmer
        ShimmerAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Host Card Shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            ShimmerAnimation(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.6f).height(18.dp))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.3f).height(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        // About Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.4f).height(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerAnimation(modifier = Modifier.fillMaxWidth().height(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        // Info Rows
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Gray.copy(alpha = 0.1f))
                        .padding(14.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    ShimmerAnimation(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp))
                    }
                }
            }
        }
    }
}
