package com.quantax.gotogether.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quantax.gotogether.ui.theme.GoTogetherTheme

@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    baseColor: Color? = null,
    highlightColor: Color? = null
) {
    val shimmerColors = listOf(
        (baseColor ?: GoTogetherTheme.colors.surfaceVariant).copy(alpha = 0.6f),
        (highlightColor ?: GoTogetherTheme.colors.surfaceVariant).copy(alpha = 0.2f),
        (baseColor ?: GoTogetherTheme.colors.surfaceVariant).copy(alpha = 0.6f),
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
            .background(GoTogetherTheme.colors.card)
            .padding(bottom = 12.dp)
    ) {
        // Hero Image Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(10.dp)
        ) {
            ShimmerAnimation(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(20.dp)
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
            .background(GoTogetherTheme.colors.card)
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
            .background(GoTogetherTheme.colors.card)
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
        modifier = Modifier.fillMaxSize().background(GoTogetherTheme.colors.background)
    ) {
        ShimmerAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.7f).height(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerAnimation(modifier = Modifier.fillMaxWidth(0.4f).height(18.dp))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Host Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerAnimation(modifier = Modifier.size(56.dp), shape = CircleShape)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    ShimmerAnimation(modifier = Modifier.width(100.dp).height(18.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerAnimation(modifier = Modifier.width(60.dp).height(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            ShimmerAnimation(modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(20.dp))
        }
    }
}
