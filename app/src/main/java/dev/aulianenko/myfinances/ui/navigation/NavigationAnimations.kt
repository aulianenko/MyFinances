package dev.aulianenko.myfinances.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

/**
 * Standard animation duration for screen transitions
 */
private const val ANIMATION_DURATION = 300

/**
 * Slide in from right animation for entering a screen
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
}

/**
 * Slide out to left animation for exiting a screen
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
}

/**
 * Slide in from left animation for popping back to a screen
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromLeft(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
}

/**
 * Slide out to right animation for popping a screen
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToRight(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
}

/**
 * Fade in animation for entering a screen
 */
fun fadeIn(): EnterTransition {
    return fadeIn(animationSpec = tween(ANIMATION_DURATION))
}

/**
 * Fade out animation for exiting a screen
 */
fun fadeOut(): ExitTransition {
    return fadeOut(animationSpec = tween(ANIMATION_DURATION))
}
