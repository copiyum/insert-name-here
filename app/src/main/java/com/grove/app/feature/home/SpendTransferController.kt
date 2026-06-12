package com.grove.app.feature.home

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.grove.app.feature.dashboard.DashboardSpendSnapshot
import kotlinx.coroutines.delay

@Stable
internal class SpendTransferController {
    var event by mutableStateOf<SpendMotionEvent?>(null)
        private set

    var snapshot by mutableStateOf<DashboardSpendSnapshot?>(null)
        private set

    var travelProgress by mutableFloatStateOf(1f)
        private set

    var impactProgress by mutableFloatStateOf(0f)
        private set

    private var serial by mutableLongStateOf(0L)

    fun settlementProgress(motionEnabled: Boolean): Float? =
        event?.let { active ->
            if (motionEnabled) spendMotionSettlementProgress(travelProgress, active.spec) else 1f
        }

    fun attachTargetBounds(bounds: Rect?) {
        val active = event
        if (active != null && active.targetBounds == null && bounds != null) {
            event = active.copy(targetBounds = bounds)
        }
    }

    fun start(
        amountMinor: Long,
        currency: String,
        categoryColor: Color,
        origin: Offset?,
        targetBounds: Rect?,
        snapshot: DashboardSpendSnapshot?,
    ) {
        serial += 1L
        this.snapshot = snapshot
        travelProgress = 0f
        impactProgress = 0f
        event =
            SpendMotionEvent(
                id = serial,
                amountMinor = amountMinor,
                currency = currency,
                categoryColor = categoryColor,
                origin = origin,
                targetBounds = targetBounds,
            )
    }

    suspend fun playCurrent(
        view: View,
        motionEnabled: Boolean,
    ) {
        val active = event ?: return
        if (!motionEnabled) {
            travelProgress = 1f
            impactProgress = 1f
            clear(active.id)
            return
        }

        travelProgress = 0f
        impactProgress = 0f
        val travel = Animatable(0f)
        travel.animateTo(
            targetValue = 1f,
            animationSpec = tween(active.durationMillis, easing = LinearEasing),
        ) {
            travelProgress = value
        }
        travelProgress = 1f
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)

        val impact = Animatable(0f)
        impact.animateTo(
            targetValue = 1f,
            animationSpec = tween(SpendMotionImpactMillis, easing = EaseOutCubic),
        ) {
            impactProgress = value
        }
        delay(SpendMotionFinishBufferMillis.toLong())
        clear(active.id)
    }

    private fun clear(id: Long) {
        if (event?.id != id) return
        event = null
        snapshot = null
        travelProgress = 1f
        impactProgress = 0f
    }
}

@Composable
internal fun rememberSpendTransferController(): SpendTransferController =
    remember { SpendTransferController() }
