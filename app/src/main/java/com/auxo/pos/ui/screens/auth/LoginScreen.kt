package com.auxo.pos.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.auxo.pos.R
import com.auxo.pos.ui.components.AuthPillField
import com.auxo.pos.ui.components.LanguageChip
import com.auxo.pos.ui.components.OrDivider
import com.auxo.pos.ui.theme.AuthEmerald
import com.auxo.pos.ui.theme.AuthEmeraldBright
import com.auxo.pos.ui.theme.AuthInk
import com.auxo.pos.ui.theme.AuthInkMuted
import com.auxo.pos.ui.theme.AuthMint
import com.auxo.pos.ui.theme.Spacing
import com.auxo.pos.viewmodel.LoginMessage
import com.auxo.pos.viewmodel.LoginUiState
import com.auxo.pos.viewmodel.LoginViewModel
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.TextStyle

/** Width over height of auxo_mark.png (the icon glyph alone, cropped from the approved vertical lockup). */
private const val MARK_ASPECT = 261f / 191f


/** The sign-in screen: the first screen on every launch. [hasBusiness] is false until a business has been set up on this device. */
@Composable
fun LoginScreen(
    hasBusiness: Boolean = true,
    onSetUpBusiness: () -> Unit = {},
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var resetting by rememberSaveable { mutableStateOf(false) }
    if (resetting && hasBusiness) {
        PasswordResetScreen(onBack = { resetting = false })
    } else {
        LoginContent(
            state, viewModel::onIdentifierChange, viewModel::onPasswordChange, viewModel::signIn,
            // Nothing to recover when this device has no business yet.
            onForgotPassword = if (hasBusiness) ({ resetting = true }) else null,
            onSetUpBusiness = onSetUpBusiness,
            topEnd = { LanguageChip() },
        )
    }
}

/*
 * The sign-in page is laid out on a fixed design frame, like the onboarding pages: every position and size below
 * is in frame dp, and the whole frame is scaled to the screen's width, so any phone shows the same picture. The
 * frame scrolls (with the keyboard up, or on a short phone) and the green picture scrolls with it, so they never
 * drift apart. Form positions come from the design export; the area above them is the picture and its text.
 */
private const val FRAME_WIDTH = 402
private const val FRAME_HEIGHT = 874

/** The widest the frame is drawn; on a tablet it is centred rather than blown up. */
private val MAX_FRAME_SCREEN_WIDTH = 480.dp

@Composable
fun LoginContent(
    state: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onForgotPassword: (() -> Unit)? = null,
    onSetUpBusiness: (() -> Unit)? = null,
    topEnd: (@Composable () -> Unit)? = null,
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (dark) MaterialTheme.colorScheme.background else Color.White)
            .testTag("auth_screen"),
    ) {
        val scale = minOf(maxWidth, MAX_FRAME_SCREEN_WIDTH) / FRAME_WIDTH.dp
        val outer = LocalDensity.current
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompositionLocalProvider(LocalDensity provides Density(outer.density * scale, outer.fontScale)) {
                Box(modifier = Modifier.requiredSize(FRAME_WIDTH.dp, FRAME_HEIGHT.dp)) {
                    if (dark) {
                        Box(modifier = Modifier.fillMaxWidth().height(HERO_HEIGHT.dp).background(MaterialTheme.colorScheme.primary))
                    } else {
                        Image(
                            painter = painterResource(R.drawable.auth_login_bg),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    LoginHero()
                    LoginForm(
                        state = state,
                        dark = dark,
                        onIdentifierChange = onIdentifierChange,
                        onPasswordChange = onPasswordChange,
                        onSignIn = onSignIn,
                        onForgotPassword = onForgotPassword,
                        onSetUpBusiness = onSetUpBusiness,
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword },
                        rememberMe = rememberMe,
                        onToggleRememberMe = { rememberMe = it },
                    )
                }
            }
        }
        if (topEnd != null) {
            Box(modifier = Modifier.align(Alignment.TopEnd).safeDrawingPadding().padding(12.dp)) { topEnd() }
        }
    }
}

/** Where the picture's white sheet begins, in frame dp: the pitch text lives above this line. */
private const val HERO_HEIGHT = 470

/** The Auxo mark, name and tagline at the top, and the "Manage / Sell / Grow" pitch on the left, over the picture. */
@Composable
private fun LoginHero() {
    Column(
        modifier = Modifier.fillMaxWidth().height(HERO_HEIGHT.dp).statusBarsPadding().padding(horizontal = 28.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.auxo_mark),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.width(64.dp).aspectRatio(MARK_ASPECT),
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(stringResource(R.string.app_tagline), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.auth_login_hero_headline),
                style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 34.sp),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                stringResource(R.string.auth_login_hero_body),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = Spacing.xs).widthIn(max = 190.dp),
            )
        }
        Spacer(Modifier.height(36.dp))
    }
}

/**
 * The sign-in form, each piece at its frame position (x, y, width x height):
 * username (52, 551, 300 x 45), password (52, 593, 300 x 45) - both as exported - then the remember-me and
 * forgot-password row, the sign-in button (52, 659, 300 x 45) as exported, the messages, and set-up-a-business.
 */
@Composable
private fun LoginForm(
    state: LoginUiState,
    dark: Boolean,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onForgotPassword: (() -> Unit)?,
    onSetUpBusiness: (() -> Unit)?,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    rememberMe: Boolean,
    onToggleRememberMe: (Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().testTag("login")) {
        Text(
            stringResource(R.string.auth_login_welcome),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (dark) MaterialTheme.colorScheme.onBackground else AuthInk,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().offset(y = 484.dp),
        )
        Text(
            stringResource(R.string.auth_login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().offset(y = 518.dp),
        )
        AuthPillField(
            value = state.identifier,
            onValueChange = onIdentifierChange,
            placeholder = stringResource(R.string.auth_field_identifier),
            icon = R.drawable.ic_mail,
            keyboardType = KeyboardType.Email,
            enabled = !state.isSigningIn,
            compact = true,
            markError = state.identifierMissing,
            modifier = Modifier.offset(52.dp, 551.dp).width(300.dp).testTag("login_identifier"),
        )
        AuthPillField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.auth_field_password),
            icon = R.drawable.ic_lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { PasswordToggle(showPassword, onTogglePassword) },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { onSignIn() }),
            enabled = !state.isSigningIn,
            compact = true,
            markError = state.passwordMissing,
            modifier = Modifier.offset(52.dp, 593.dp).width(300.dp).testTag("login_password"),
        )
        // Remember me, on the left of the row the exported "Forgot password?" sits in.
        Row(
            modifier = Modifier.offset(52.dp, 641.dp).height(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = onToggleRememberMe,
                    colors = CheckboxDefaults.colors(checkedColor = AuthEmerald),
                    modifier = Modifier.size(18.dp).testTag("login_remember_me"),
                )
            }
            Text(
                stringResource(R.string.auth_login_remember_me),
                style = TextStyle(fontSize = 11.sp, color = AuthInkMuted),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (onForgotPassword != null) {
            // The exported button (250, 639, 84 x 21) with its 8 sp bold label.
            TextButton(
                onClick = onForgotPassword,
                enabled = !state.isSigningIn,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.offset(250.dp, 639.dp).size(84.dp, 21.dp).testTag("login_forgot"),
            ) {
                Text(
                    stringResource(R.string.auth_login_forgot),
                    color = AuthEmerald,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 10.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Button(
            onClick = onSignIn,
            enabled = !state.isSigningIn,
            shape = RoundedCornerShape(32.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
            modifier = Modifier
                .offset(52.dp, 659.dp)
                .size(300.dp, 45.dp)
                .background(Brush.horizontalGradient(listOf(AuthEmerald, AuthEmeraldBright)), RoundedCornerShape(32.dp))
                .testTag("login_submit"),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    stringResource(if (state.isSigningIn) R.string.auth_login_working else R.string.auth_login_action),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_forward),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        // What went wrong, under the button: missing fields first, then the sign-in result.
        Column(modifier = Modifier.offset(52.dp, 708.dp).width(300.dp)) {
            if (state.identifierMissing) {
                Text(stringResource(R.string.auth_error_login_identifier_required), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            if (state.passwordMissing) {
                Text(stringResource(R.string.auth_error_login_password_required), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            val message = state.message
            if (message != null) {
                Text(
                    text = when (message) {
                        LoginMessage.Rejected -> stringResource(R.string.auth_login_rejected)
                        LoginMessage.Deactivated -> stringResource(R.string.auth_login_deactivated)
                        is LoginMessage.LockedOut -> pluralStringResource(R.plurals.auth_login_locked, message.seconds, message.seconds)
                        LoginMessage.NoAccount -> stringResource(R.string.auth_login_no_account)
                        LoginMessage.Unavailable -> stringResource(R.string.auth_login_unavailable)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("login_error"),
                )
            }
        }
        // Always present, whether or not a business exists yet: it is how a new business is started.
        if (onSetUpBusiness != null) {
            OrDivider(stringResource(R.string.auth_login_setup_business), modifier = Modifier.offset(52.dp, 752.dp).width(300.dp))
            OutlinedButton(
                onClick = onSetUpBusiness,
                enabled = !state.isSigningIn,
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.offset(52.dp, 778.dp).size(300.dp, 45.dp).testTag("login_setup_business"),
            ) {
                Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(AuthMint), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_store), contentDescription = null, tint = AuthEmerald, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.auth_login_setup_business_action), color = AuthEmerald, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = AuthEmerald, modifier = Modifier.size(18.dp))
            }
        }
    }
}
